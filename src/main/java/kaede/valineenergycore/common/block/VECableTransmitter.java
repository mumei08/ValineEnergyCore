package kaede.valineenergycore.common.block;

import kaede.valineenergycore.common.content.network.VECableTier;
import kaede.valineenergycore.common.content.network.VETransmitter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashSet;
import java.util.Set;

/**
 * ケーブル用のTransmitter実装
 */

public class VECableTransmitter extends VETransmitter {

    private final BlockEntityVECable blockEntity;

    public VECableTransmitter(BlockPos position, VECableTier tier, BlockEntityVECable blockEntity) {
        super(position, tier);
        this.blockEntity = blockEntity;
    }

    @Override
    public Set<VETransmitter> getAdjacentTransmitters() {
        Set<VETransmitter> adjacent = new HashSet<>();
        Level level = blockEntity.getLevel();

        if (level == null) {
            return adjacent;
        }

        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = position.relative(direction);
            BlockEntity be = level.getBlockEntity(adjacentPos);

            if (be instanceof BlockEntityVECable cable) {
                adjacent.add(cable.getTransmitter());
            }
        }

        return adjacent;
    }

    public Level getWorld() {
        return blockEntity.getLevel();
    }
}