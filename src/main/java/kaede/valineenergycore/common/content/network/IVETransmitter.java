package kaede.valineenergycore.common.content.network;

import kaede.valineenergycore.api.energy.BigEnergy;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;

/**
 * Transmitterのインターフェース
 */

public interface IVETransmitter {

    BlockPos getPosition();

    VENetwork getNetwork();

    void setNetwork(@Nullable VENetwork network);

    BigEnergy getCapacity();

    VECableTier getTier();

}