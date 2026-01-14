package kaede.valineenergycore.common.capabilities;

import kaede.valineenergycore.api.energy.BigEnergy;
import kaede.valineenergycore.api.energy.IVEContainer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

/**
 * ValineEnergy の Capability 定義
 * Forge Energy との相互運用も提供
 */
public class VECapabilities {

    /**
     * VE の Capability
     */
    public static final Capability<IVEContainer> VE_CONTAINER =
            CapabilityManager.get(new CapabilityToken<>(){});

    /**
     * Forge Energy から VE への変換レート
     * デフォルト: 1 FE = 1 / Integer.MAX_VALUE VE
     */
    public static BigEnergy FE_TO_VE_RATE = BigEnergy.create(1 / Integer.MAX_VALUE);

    /**
     * VE から Forge Energy への変換レート
     * デフォルト: 1 VE = Integer.MAX_VALUE FE
     */
    public static double VE_TO_FE_RATE = Integer.MAX_VALUE;

    /**
     * Forge Energy を VE に変換
     */
    public static BigEnergy convertFromForgeEnergy(int forgeEnergy) {
        return BigEnergy.create(forgeEnergy).multiply(FE_TO_VE_RATE);
    }

    /**
     * VE を Forge Energy に変換
     */
    public static int convertToForgeEnergy(BigEnergy ve) {
        double result = ve.doubleValue() * VE_TO_FE_RATE;
        // int の範囲に制限
        if (result > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return (int) result;
    }
}
