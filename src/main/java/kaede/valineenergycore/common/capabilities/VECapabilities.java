package kaede.valineenergycore.common.capabilities;

import kaede.valineenergycore.api.energy.BigEnergy;
import kaede.valineenergycore.common.config.VEConfig;

/**
 * ValineEnergy の Capability 完全実装
 * Forge Energy との相互運用も提供
 */
public class VECapabilities {

    /**
     * Forge Energy から VE への変換
     */
    public static BigEnergy convertFromForgeEnergy(int forgeEnergy) {
        long numerator = VEConfig.COMMON.forgeEnergyToVENumerator.get();
        long denominator = VEConfig.COMMON.forgeEnergyToVEDenominator.get();

        // forgeEnergy * (numerator / denominator)
        BigEnergy energy = BigEnergy.create(forgeEnergy);
        energy = energy.multiply(numerator);
        if (denominator != 1) {
            energy = energy.divide(denominator);
        }
        return energy;
    }

    /**
     * VE を Forge Energy に変換
     */
    public static int convertToForgeEnergy(BigEnergy ve) {
        long numerator = VEConfig.COMMON.veToForgeEnergyNumerator.get();
        long denominator = VEConfig.COMMON.veToForgeEnergyDenominator.get();

        // ve * (numerator / denominator)
        double result = ve.doubleValue() * numerator / (double) denominator;

        // int の範囲に制限
        if (result > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (result < 0) return 0;
        return (int) result;
    }
}