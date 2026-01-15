package kaede.valineenergycore.common.block;

import kaede.valineenergycore.api.energy.BigEnergy;
import kaede.valineenergycore.api.energy.IVEContainer;
import kaede.valineenergycore.common.capabilities.VECapabilityProvider;
import kaede.valineenergycore.common.capabilities.energy.InfiniteVEContainer;
import kaede.valineenergycore.common.registration.VERegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * VE無限ケーブルのBlockEntity
 * 受け入れ可能量全体を転送し、余剰を少数で処理
 */
public class BlockEntityVECableInfinite extends BlockEntity {

    private InfiniteVEContainer buffer;
    private VECapabilityProvider capabilityProvider;

    // 隣接する受容側のキャッシュ
    private final List<AdjacentAcceptor> adjacentAcceptors = new ArrayList<>();
    private int ticksSinceLastScan = 0;
    private static final int SCAN_INTERVAL = 20; // 1秒ごとにスキャン

    public BlockEntityVECableInfinite(BlockPos pos, BlockState state) {
        super(VERegistration.VE_CABLE_INFINITE_BE.get(), pos, state);
        initializeComponents();
    }

    private void initializeComponents() {
        this.buffer = new InfiniteVEContainer();
        this.capabilityProvider = new VECapabilityProvider(buffer);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntityVECableInfinite blockEntity) {
        if (!level.isClientSide) {
            blockEntity.serverTick();
        }
    }

    private void serverTick() {
        ticksSinceLastScan++;

        // 定期的に隣接ブロックをスキャン
        if (ticksSinceLastScan >= SCAN_INTERVAL) {
            scanAdjacentAcceptors();
            ticksSinceLastScan = 0;
        }

        // エネルギー転送処理
        if (!buffer.isEmpty() && !adjacentAcceptors.isEmpty()) {
            distributeEnergy();
        }
    }

    /**
     * 隣接する6方向のエネルギー受容側をスキャン
     */
    private void scanAdjacentAcceptors() {
        adjacentAcceptors.clear();

        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = worldPosition.relative(direction);
            BlockEntity be = level.getBlockEntity(adjacentPos);

            // 自分自身のケーブルは除外
            if (be instanceof BlockEntityVECableInfinite) {
                continue;
            }

            if (be != null) {
                LazyOptional<IEnergyStorage> energyCap = be.getCapability(
                        ForgeCapabilities.ENERGY,
                        direction.getOpposite()
                );

                energyCap.ifPresent(storage -> {
                    if (storage.canReceive()) {
                        adjacentAcceptors.add(new AdjacentAcceptor(storage, direction));
                    }
                });
            }
        }
    }

    /**
     * 新しいエネルギー分配アルゴリズム
     * 受け入れ可能量全体を転送し、余剰は少数で配分
     */
    private void distributeEnergy() {
        BigEnergy available = buffer.getEnergy();
        if (available.isZero()) {
            return;
        }

        // 各受容側の受け入れ可能量を計算
        List<EnergyDemand> demands = new ArrayList<>();
        BigEnergy totalDemand = BigEnergy.ZERO;

        for (AdjacentAcceptor acceptor : adjacentAcceptors) {
            IEnergyStorage storage = acceptor.storage();
            int maxReceiveFE = storage.getMaxEnergyStored() - storage.getEnergyStored();

            if (maxReceiveFE > 0) {
                BigEnergy demandVE = convertFromFE(maxReceiveFE);
                demands.add(new EnergyDemand(acceptor, demandVE, maxReceiveFE));
                totalDemand = totalDemand.add(demandVE);
            }
        }

        if (demands.isEmpty() || totalDemand.isZero()) {
            return;
        }

        // 供給量が需要量より多い場合は全て送る
        if (available.greaterOrEqual(totalDemand)) {
            for (EnergyDemand demand : demands) {
                int sent = demand.acceptor().storage().receiveEnergy(demand.maxReceiveFE(), false);
                buffer.extract(convertFromFE(sent), IVEContainer.Action.EXECUTE);
            }
        } else {
            // 供給量が不足している場合は比例配分
            for (EnergyDemand demand : demands) {
                // 比率計算: この受容側が受け取る量 = available * (demand / totalDemand)
                double ratio = demand.demandVE().doubleValue() / totalDemand.doubleValue();
                BigEnergy toSend = available.multiply(ratio);

                // FEに変換して送信（doubleを使って小数点以下も考慮）
                double toSendFE = toSend.doubleValue() * getVEToFERatio();
                int toSendFEInt = (int) Math.min(toSendFE, demand.maxReceiveFE());

                if (toSendFEInt > 0) {
                    int sent = demand.acceptor().storage().receiveEnergy(toSendFEInt, false);
                    buffer.extract(convertFromFE(sent), IVEContainer.Action.EXECUTE);
                }
            }
        }
    }

    /**
     * FEからVEへの変換
     */
    private BigEnergy convertFromFE(int fe) {
        return kaede.valineenergycore.common.capabilities.VECapabilities.convertFromForgeEnergy(fe);
    }

    /**
     * VEからFEへの変換比率を取得
     */
    private double getVEToFERatio() {
        long numerator = kaede.valineenergycore.common.config.VEConfig.COMMON.veToForgeEnergyNumerator.get();
        long denominator = kaede.valineenergycore.common.config.VEConfig.COMMON.veToForgeEnergyDenominator.get();
        return (double) numerator / denominator;
    }

    public InfiniteVEContainer getBuffer() {
        return buffer;
    }

    // ========== Capability ==========

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (!this.remove) {
            LazyOptional<T> capability = capabilityProvider.getCapability(cap, side);
            if (capability.isPresent()) {
                return capability;
            }
        }
        return super.getCapability(cap, side);
    }

    // ========== NBT保存/読込 ==========

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (tag.contains("Buffer")) {
            buffer.readFromNBT(tag.getCompound("Buffer"));
        }

        // コンポーネントを再初期化
        this.capabilityProvider = new VECapabilityProvider(buffer);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        CompoundTag bufferTag = new CompoundTag();
        buffer.writeToNBT(bufferTag);
        tag.put("Buffer", bufferTag);
    }

    // ========== ネットワーク同期 ==========

    @Override
    public CompoundTag getUpdateTag() {
        return super.getUpdateTag();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // ========== ライフサイクル ==========

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (capabilityProvider != null) {
            capabilityProvider.invalidate();
        }
    }

    public void onPlace() {
        // 設置時の処理
        scanAdjacentAcceptors();
    }

    public void onRemove() {
        // 削除時の処理
        adjacentAcceptors.clear();
    }

    // ========== ヘルパークラス ==========

    private record AdjacentAcceptor(IEnergyStorage storage, Direction direction) {}

    private record EnergyDemand(
            AdjacentAcceptor acceptor,
            BigEnergy demandVE,
            int maxReceiveFE
    ) {}
}