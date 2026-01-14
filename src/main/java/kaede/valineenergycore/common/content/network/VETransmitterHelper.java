package kaede.valineenergycore.common.content.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Set;

/**
 * VETransmitterに必要なヘルパーメソッド
 */

public abstract class VETransmitterHelper {

    /**
     * Transmitterが所属するワールドを取得
     */
    protected abstract Level getWorld();

    /**
     * 隣接する6方向のTransmitterを取得
     */
    protected Set<VETransmitter> findAdjacentTransmitters() {
        Set<VETransmitter> adjacent = new HashSet<>();
        BlockPos pos = getPosition();
        Level world = getWorld();

        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = pos.relative(direction);

            // その座標にネットワークがあるかチェック
            VENetwork network = VENetworkRegistry.getNetworkAt(adjacentPos);
            if (network != null) {
                // そのネットワークのTransmitterを探す
                for (VETransmitter transmitter : network.getTransmitters()) {
                    if (transmitter.getPosition().equals(adjacentPos)) {
                        adjacent.add(transmitter);
                        break;
                    }
                }
            }
        }

        return adjacent;
    }

    protected abstract BlockPos getPosition();
}
