package kaede.valineenergycore.common.config;

import kaede.valineenergycore.api.energy.BigEnergy;
import kaede.valineenergycore.api.energy.VEMemoryManager;
import net.minecraftforge.common.ForgeConfigSpec;
import java.math.BigInteger;

/**
 * ValineEnergy の設定クラス
 */
public class VEConfig {

    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        ForgeConfigSpec.Builder commonBuilder = new ForgeConfigSpec.Builder();
        COMMON = new Common(commonBuilder);
        COMMON_SPEC = commonBuilder.build();
    }

    public static class Common {

        // エネルギー変換レート設定
        public final ForgeConfigSpec.LongValue veToForgeEnergyNumerator;
        public final ForgeConfigSpec.LongValue veToForgeEnergyDenominator;
        public final ForgeConfigSpec.LongValue forgeEnergyToVENumerator;
        public final ForgeConfigSpec.LongValue forgeEnergyToVEDenominator;

        // メモリベースシステム設定
        public final ForgeConfigSpec.BooleanValue enableMemoryBasedLimits;
        public final ForgeConfigSpec.ConfigValue<String> vePerMB;
        public final ForgeConfigSpec.ConfigValue<String> minimumGuaranteedCapacity;

        // デバッグ設定
        public final ForgeConfigSpec.BooleanValue enableDebugLogging;
        public final ForgeConfigSpec.BooleanValue showMemoryWarnings;

        public Common(ForgeConfigSpec.Builder builder) {
            builder.comment("ValineEnergy Common Configuration").push("common");

            builder.comment("Energy Conversion Settings").push("conversion");

            veToForgeEnergyNumerator = builder
                    .comment("VE to Forge Energy conversion rate (numerator)")
                    .comment("Default: 1 VE = 0.2 FE (1/5)")
                    .defineInRange("veToForgeEnergyNumerator", 1L, 1L, Long.MAX_VALUE);

            veToForgeEnergyDenominator = builder
                    .comment("VE to Forge Energy conversion rate (denominator)")
                    .defineInRange("veToForgeEnergyDenominator", 5L, 1L, Long.MAX_VALUE);

            forgeEnergyToVENumerator = builder
                    .comment("Forge Energy to VE conversion rate (numerator)")
                    .comment("Default: 1 FE = 5 VE")
                    .defineInRange("forgeEnergyToVENumerator", 5L, 1L, Long.MAX_VALUE);

            forgeEnergyToVEDenominator = builder
                    .comment("Forge Energy to VE conversion rate (denominator)")
                    .defineInRange("forgeEnergyToVEDenominator", 1L, 1L, Long.MAX_VALUE);

            builder.pop();

            builder.comment("Memory-Based System Settings").push("memory");

            enableMemoryBasedLimits = builder
                    .comment("Enable memory-based capacity limits")
                    .comment("When true, max VE capacity scales with allocated memory")
                    .define("enableMemoryBasedLimits", true);

            vePerMB = builder
                    .comment("VE capacity per 1MB of allocated memory")
                    .comment("Default: 10^50 (100000000000000000000000000000000000000000000000000)")
                    .define("vePerMB", "100000000000000000000000000000000000000000000000000");

            minimumGuaranteedCapacity = builder
                    .comment("Minimum guaranteed VE capacity regardless of memory")
                    .comment("Default: 10^12")
                    .define("minimumGuaranteedCapacity", "1000000000000");

            builder.pop();

            builder.comment("Debug Settings").push("debug");

            enableDebugLogging = builder
                    .comment("Enable debug logging for VE operations")
                    .define("enableDebugLogging", false);

            showMemoryWarnings = builder
                    .comment("Show warnings when containers hit memory limits")
                    .define("showMemoryWarnings", true);

            builder.pop();
            builder.pop();
        }

        /**
         * 設定から VE per MB を BigInteger として取得
         */
        public BigInteger getVEPerMB() {
            try {
                return new BigInteger(vePerMB.get());
            } catch (NumberFormatException e) {
                return new BigInteger("100000000000000000000000000000000000000000000000000");
            }
        }

        /**
         * 設定から最小保証容量を BigEnergy として取得
         */
        public BigEnergy getMinimumGuaranteedCapacity() {
            try {
                return BigEnergy.create(new BigInteger(minimumGuaranteedCapacity.get()));
            } catch (NumberFormatException e) {
                return BigEnergy.create(new BigInteger("1000000000000"));
            }
        }
    }
}