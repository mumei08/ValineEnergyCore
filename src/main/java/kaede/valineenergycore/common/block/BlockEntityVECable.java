package kaede.valineenergycore.common.block;

import kaede.valineenergycore.api.energy.BigEnergy;
import kaede.valineenergycore.common.capabilities.VECapabilityProvider;
import kaede.valineenergycore.common.capabilities.energy.BasicVEContainer;
import kaede.valineenergycore.common.content.network.*;
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
import net.minecraftforge.common.util.LazyOptional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/**
 * VEケーブルのBlockEntity - Capability完全統合版
 */
public class BlockEntityVECable extends BlockEntity {

    private VECableTransmitter transmitter;
    private VECableTier tier;

    // ケーブル自身のバッファ
    private BasicVEContainer buffer;

    // Capability Provider
    private VECapabilityProvider capabilityProvider;

    // BlockEntityType登録用のコンストラクタ
    public BlockEntityVECable(BlockPos pos, BlockState state) {
        super(VERegistration.VE_CABLE_BE.get(), pos, state);
        initializeTier(state);
        initializeComponents();
    }

    // 直接呼び出し用のコンストラクタ
    public BlockEntityVECable(BlockPos pos, BlockState state, VECableTier tier) {
        super(VERegistration.VE_CABLE_BE.get(), pos, state);
        this.tier = tier;
        initializeComponents();
    }

    /**
     * BlockStateからTierを判定
     */
    private void initializeTier(BlockState state) {
        if (state.getBlock() instanceof BlockVECable cable) {
            this.tier = cable.getTier();
        } else {
            this.tier = VECableTier.BASIC; // フォールバック
        }
    }

    /**
     * コンポーネントの初期化
     */
    private void initializeComponents() {
        // バッファコンテナの作成
        this.buffer = new BasicVEContainer(tier.getCapacity());

        // Transmitterの作成
        this.transmitter = new VECableTransmitter(worldPosition, tier, this);

        // Capability Providerの作成
        this.capabilityProvider = new VECapabilityProvider(buffer);
    }

    /**
     * Static tick method for 1.20.x
     */
    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntityVECable blockEntity) {
        if (!level.isClientSide) {
            blockEntity.serverTick();
        }
    }

    private void serverTick() {
        // ネットワークからバッファへの転送
        if (transmitter.getNetwork() != null) {
            VENetwork network = transmitter.getNetwork();

            // バッファが空なら、ネットワークから受け取る
            if (!buffer.isFull() && !network.getBuffer().isZero()) {
                BigEnergy toReceive = buffer.getNeeded().min(tier.getTransferRate());
                BigEnergy received = network.emit(toReceive); // ネットワークからemitではなくextractに変更すべき
                buffer.insert(received, BasicVEContainer.Action.EXECUTE);
            }
        }
    }

    public VECableTransmitter getTransmitter() {
        return transmitter;
    }

    public VECableTier getTier() {
        return tier;
    }

    public BasicVEContainer getBuffer() {
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

        if (tag.contains("Tier")) {
            String tierName = tag.getString("Tier");
            try {
                this.tier = VECableTier.valueOf(tierName);
            } catch (IllegalArgumentException e) {
                this.tier = VECableTier.BASIC;
            }
        }

        // バッファを読み込み
        if (tag.contains("Buffer")) {
            buffer.readFromNBT(tag.getCompound("Buffer"));
        }

        // コンポーネントを再初期化
        this.transmitter = new VECableTransmitter(worldPosition, tier, this);
        this.capabilityProvider = new VECapabilityProvider(buffer);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("Tier", tier.name());

        // バッファを保存
        CompoundTag bufferTag = new CompoundTag();
        buffer.writeToNBT(bufferTag);
        tag.put("Buffer", bufferTag);
    }

    // ========== ネットワーク同期 ==========

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putString("Tier", tier.name());
        return tag;
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
        if (transmitter != null) {
            transmitter.onRemove();
        }
        if (capabilityProvider != null) {
            capabilityProvider.invalidate();
        }
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        // チャンクがロードされた時の処理
        if (transmitter != null && level != null && !level.isClientSide) {
            transmitter.onPlace();
        }
    }
}