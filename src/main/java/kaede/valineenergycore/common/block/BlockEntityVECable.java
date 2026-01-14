package kaede.valineenergycore.common.block;

import kaede.valineenergycore.common.content.network.*;
import kaede.valineenergycore.common.registration.VERegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;

/**
 * VEケーブルのBlockEntity (修正版)
 * 登録システムに対応
 */
public class BlockEntityVECable extends BlockEntity {

    private VECableTransmitter transmitter;
    private VECableTier tier;

    // BlockEntityType登録用のコンストラクタ
    public BlockEntityVECable(BlockPos pos, BlockState state) {
        super(VERegistration.VE_CABLE_BE.get(), pos, state);
        initializeTier(state);
        this.transmitter = new VECableTransmitter(pos, tier, this);
    }

    // 直接呼び出し用のコンストラクタ
    public BlockEntityVECable(BlockPos pos, BlockState state, VECableTier tier) {
        super(VERegistration.VE_CABLE_BE.get(), pos, state);
        this.tier = tier;
        this.transmitter = new VECableTransmitter(pos, tier, this);
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
     * Static tick method for 1.20.x
     */
    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntityVECable blockEntity) {
        if (!level.isClientSide) {
            blockEntity.serverTick();
        }
    }

    private void serverTick() {
        // 必要に応じてtick処理を追加
        // ネットワークのtickはVENetworkRegistryが一括管理するので、
        // ここでは個別のケーブルの処理のみ
    }

    public VECableTransmitter getTransmitter() {
        return transmitter;
    }

    public VECableTier getTier() {
        return tier;
    }

    // ========== NBT保存/読込 ==========

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (tag.contains("Tier")) {
            String tierName = tag.getString("Tier");
            try {
                this.tier = VECableTier.valueOf(tierName);
                // Transmitterを再生成
                this.transmitter = new VECableTransmitter(worldPosition, tier, this);
            } catch (IllegalArgumentException e) {
                this.tier = VECableTier.BASIC;
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("Tier", tier.name());
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