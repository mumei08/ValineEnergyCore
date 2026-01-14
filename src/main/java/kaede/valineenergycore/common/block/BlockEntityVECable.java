package kaede.valineenergycore.common.block;

import kaede.valineenergycore.common.content.network.VECableTier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * VEケーブルのBlockEntity
 */

public class BlockEntityVECable extends BlockEntity {

    private final VECableTransmitter transmitter;

    public BlockEntityVECable(BlockPos pos, BlockState state, VECableTier tier) {
        super(null, pos, state); // TODO: BlockEntityTypeを登録
        this.transmitter = new VECableTransmitter(pos, tier, this);
    }

    public void tick() {
        // 必要に応じてtick処理
    }

    public VECableTransmitter getTransmitter() {
        return transmitter;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        transmitter.onRemove();
    }
}
