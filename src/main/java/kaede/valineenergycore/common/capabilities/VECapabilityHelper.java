package kaede.valineenergycore.common.capabilities;

import kaede.valineenergycore.api.energy.IVEContainer;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

/**
 * Capability のヘルパークラス
 */

public class VECapabilityHelper {

    /**
     * IVEContainer を Forge Energy にラップして取得
     */
    @Nullable
    public static IEnergyStorage wrapToForgeEnergy(@Nullable IVEContainer container) {
        if (container == null) return null;
        return new VEToForgeEnergyWrapper(container);
    }

    /**
     * Forge Energy を IVEContainer にラップして取得
     */
    @Nullable
    public static IVEContainer wrapFromForgeEnergy(@Nullable IEnergyStorage storage) {
        if (storage == null) return null;
        return new ForgeEnergyToVEWrapper(storage);
    }
}
