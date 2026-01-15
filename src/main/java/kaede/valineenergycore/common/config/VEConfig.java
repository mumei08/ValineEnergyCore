package kaede.valineenergycore.common.config;

import kaede.valineenergycore.api.energy.BigEnergy;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigInteger;

/**
 * ValineEnergy Core の完全版設定システム
 */
public class VEConfig {

    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        Pair<Common, ForgeConfigSpec> commonPair = new ForgeConfigSpec.Builder()
                .configure(Common::new);
        COMMON = commonPair.getLeft();
        COMMON_SPEC = commonPair.getRight();
    }

    public static class Common {

        // ========== エネルギー変換設定 ==========

        public final ForgeConfigSpec.ConfigValue<String> conversionSection;

        public final ForgeConfigSpec.LongValue veToForgeEnergyNumerator;
        public final ForgeConfigSpec.LongValue veToForgeEnergyDenominator;
        public final ForgeConfigSpec.LongValue forgeEnergyToVENumerator;
        public final ForgeConfigSpec.LongValue forgeEnergyToVEDenominator;

        // ========== メモリベースシステム設定 ==========

        public final ForgeConfigSpec.ConfigValue<String> memorySection;

        public final ForgeConfigSpec.BooleanValue enableMemoryBasedLimits;
        public final ForgeConfigSpec.ConfigValue<String> vePerMB;
        public final ForgeConfigSpec.ConfigValue<String> minimumGuaranteedCapacity;
        public final ForgeConfigSpec.DoubleValue memoryUsageWarningThreshold;

        // ========== ケーブル設定 ==========

        public final ForgeConfigSpec.ConfigValue<String> cableSection;

        // Basic Tier
        public final ForgeConfigSpec.ConfigValue<String> basicCableCapacity;
        public final ForgeConfigSpec.ConfigValue<String> basicCableTransferRate;

        // Advanced Tier
        public final ForgeConfigSpec.ConfigValue<String> advancedCableCapacity;
        public final ForgeConfigSpec.ConfigValue<String> advancedCableTransferRate;

        // Elite Tier
        public final ForgeConfigSpec.ConfigValue<String> eliteCableCapacity;
        public final ForgeConfigSpec.ConfigValue<String> eliteCableTransferRate;

        // Ultimate Tier
        public final ForgeConfigSpec.ConfigValue<String> ultimateCableCapacity;
        public final ForgeConfigSpec.ConfigValue<String> ultimateCableTransferRate;

        // ========== ネットワーク設定 ==========

        public final ForgeConfigSpec.ConfigValue<String> networkSection;

        public final ForgeConfigSpec.IntValue maxTransmittersPerNetwork;
        public final ForgeConfigSpec.IntValue maxAcceptorsPerNetwork;
        public final ForgeConfigSpec.BooleanValue enableNetworkMerging;
        public final ForgeConfigSpec.IntValue networkUpdateInterval;
        public final ForgeConfigSpec.BooleanValue enableEnergyLoss;
        public final ForgeConfigSpec.DoubleValue energyLossPercentPerBlock;

        // ========== パフォーマンス設定 ==========

        public final ForgeConfigSpec.ConfigValue<String> performanceSection;

        public final ForgeConfigSpec.BooleanValue enableMultithreadedNetworks;
        public final ForgeConfigSpec.IntValue maxNetworkCalculationsPerTick;
        public final ForgeConfigSpec.BooleanValue enableNetworkCaching;
        public final ForgeConfigSpec.IntValue cacheUpdateInterval;

        // ========== デバッグ設定 ==========

        public final ForgeConfigSpec.ConfigValue<String> debugSection;

        public final ForgeConfigSpec.BooleanValue enableDebugLogging;
        public final ForgeConfigSpec.BooleanValue showMemoryWarnings;
        public final ForgeConfigSpec.BooleanValue logNetworkOperations;
        public final ForgeConfigSpec.BooleanValue showEnergyTransferParticles;
        public final ForgeConfigSpec.BooleanValue enableNetworkVisualization;

        public Common(ForgeConfigSpec.Builder builder) {

            // ========== エネルギー変換設定 ==========

            builder.comment(
                    "===================================",
                    "Energy Conversion Settings",
                    "===================================",
                    "Configure conversion rates between VE and Forge Energy (FE/RF)"
            ).push("conversion");

            conversionSection = builder
                    .comment("This section configures energy conversion rates")
                    .define("_section_info", "Energy Conversion Settings");

            veToForgeEnergyNumerator = builder
                    .comment(
                            "VE to Forge Energy conversion rate (numerator)",
                            "Formula: FE = VE * (numerator / denominator)",
                            "Default: 1 VE = 0.2 FE (1/5)"
                    )
                    .defineInRange("veToForgeEnergyNumerator", 1L, 1L, Long.MAX_VALUE);

            veToForgeEnergyDenominator = builder
                    .comment("VE to Forge Energy conversion rate (denominator)")
                    .defineInRange("veToForgeEnergyDenominator", 5L, 1L, Long.MAX_VALUE);

            forgeEnergyToVENumerator = builder
                    .comment(
                            "Forge Energy to VE conversion rate (numerator)",
                            "Formula: VE = FE * (numerator / denominator)",
                            "Default: 1 FE = 5 VE"
                    )
                    .defineInRange("forgeEnergyToVENumerator", 5L, 1L, Long.MAX_VALUE);

            forgeEnergyToVEDenominator = builder
                    .comment("Forge Energy to VE conversion rate (denominator)")
                    .defineInRange("forgeEnergyToVEDenominator", 1L, 1L, Long.MAX_VALUE);

            builder.pop();

            // ========== メモリベースシステム設定 ==========

            builder.comment(
                    "===================================",
                    "Memory-Based System Settings",
                    "===================================",
                    "Configure how VE capacity scales with allocated memory"
            ).push("memory");

            memorySection = builder
                    .comment("This section configures memory-based capacity scaling")
                    .define("_section_info", "Memory-Based System Settings");

            enableMemoryBasedLimits = builder
                    .comment(
                            "Enable memory-based capacity limits",
                            "When true, max VE capacity scales with allocated memory",
                            "When false, containers can hold unlimited energy"
                    )
                    .define("enableMemoryBasedLimits", true);

            vePerMB = builder
                    .comment(
                            "VE capacity per 1MB of allocated memory",
                            "Default: 10^50",
                            "Example: With 4GB RAM = 4096MB * 10^50 = 4.096 * 10^53 VE max capacity"
                    )
                    .define("vePerMB", "100000000000000000000000000000000000000000000000000");

            minimumGuaranteedCapacity = builder
                    .comment(
                            "Minimum guaranteed VE capacity regardless of memory",
                            "Default: 10^12",
                            "Even with low memory, at least this much capacity is guaranteed"
                    )
                    .define("minimumGuaranteedCapacity", "1000000000000");

            memoryUsageWarningThreshold = builder
                    .comment(
                            "Memory usage percentage to trigger warnings (0.0 - 1.0)",
                            "When memory usage exceeds this threshold, warnings will be logged",
                            "Default: 0.9 (90%)"
                    )
                    .defineInRange("memoryUsageWarningThreshold", 0.9, 0.0, 1.0);

            builder.pop();

            // ========== ケーブル設定 ==========

            builder.comment(
                    "===================================",
                    "Cable Tier Settings",
                    "===================================",
                    "Configure capacity and transfer rates for each cable tier"
            ).push("cables");

            cableSection = builder
                    .comment("This section configures cable tier specifications")
                    .define("_section_info", "Cable Tier Settings");

            // Basic Tier
            builder.comment("Basic Tier Cable").push("basic");

            basicCableCapacity = builder
                    .comment(
                            "Basic Cable buffer capacity",
                            "Default: 10^48 VE"
                    )
                    .define("capacity", "1000000000000000000000000000000000000000000000000");

            basicCableTransferRate = builder
                    .comment(
                            "Basic Cable transfer rate per tick",
                            "Default: 10^44 VE/tick"
                    )
                    .define("transferRate", "100000000000000000000000000000000000000000000");

            builder.pop();

            // Advanced Tier
            builder.comment("Advanced Tier Cable").push("advanced");

            advancedCableCapacity = builder
                    .comment(
                            "Advanced Cable buffer capacity",
                            "Default: 10^49 VE"
                    )
                    .define("capacity", "10000000000000000000000000000000000000000000000000");

            advancedCableTransferRate = builder
                    .comment(
                            "Advanced Cable transfer rate per tick",
                            "Default: 10^45 VE/tick"
                    )
                    .define("transferRate", "1000000000000000000000000000000000000000000");

            builder.pop();

            // Elite Tier
            builder.comment("Elite Tier Cable").push("elite");

            eliteCableCapacity = builder
                    .comment(
                            "Elite Cable buffer capacity",
                            "Default: 10^50 VE"
                    )
                    .define("capacity", "100000000000000000000000000000000000000000000000000");

            eliteCableTransferRate = builder
                    .comment(
                            "Elite Cable transfer rate per tick",
                            "Default: 10^46 VE/tick"
                    )
                    .define("transferRate", "10000000000000000000000000000000000000000000");

            builder.pop();

            // Ultimate Tier
            builder.comment("Ultimate Tier Cable").push("ultimate");

            ultimateCableCapacity = builder
                    .comment(
                            "Ultimate Cable buffer capacity",
                            "Default: 10^51 VE"
                    )
                    .define("capacity", "1000000000000000000000000000000000000000000000000000");

            ultimateCableTransferRate = builder
                    .comment(
                            "Ultimate Cable transfer rate per tick",
                            "Default: 10^47 VE/tick"
                    )
                    .define("transferRate", "100000000000000000000000000000000000000000000");

            builder.pop();
            builder.pop();

            // ========== ネットワーク設定 ==========

            builder.comment(
                    "===================================",
                    "Network Settings",
                    "===================================",
                    "Configure network behavior and limits"
            ).push("network");

            networkSection = builder
                    .comment("This section configures network behavior")
                    .define("_section_info", "Network Settings");

            maxTransmittersPerNetwork = builder
                    .comment(
                            "Maximum number of transmitters (cables) per network",
                            "Set to -1 for unlimited",
                            "Default: -1 (unlimited, leveraging BigInteger advantage)"
                    )
                    .defineInRange("maxTransmittersPerNetwork", -1, -1, Integer.MAX_VALUE);

            maxAcceptorsPerNetwork = builder
                    .comment(
                            "Maximum number of acceptors (energy receivers) per network",
                            "Set to -1 for unlimited",
                            "Default: 1000"
                    )
                    .defineInRange("maxAcceptorsPerNetwork", 1000, -1, Integer.MAX_VALUE);

            enableNetworkMerging = builder
                    .comment(
                            "Enable automatic network merging when cables connect",
                            "Disable for better performance in massive networks"
                    )
                    .define("enableNetworkMerging", true);

            networkUpdateInterval = builder
                    .comment(
                            "How often to update network acceptors (in ticks)",
                            "Lower = more responsive, Higher = better performance",
                            "Default: 20 (1 second)"
                    )
                    .defineInRange("networkUpdateInterval", 20, 1, 600);

            enableEnergyLoss = builder
                    .comment(
                            "Enable energy loss over distance",
                            "When true, energy decreases slightly per block traveled",
                            "Default: false (lossless like Mekanism)"
                    )
                    .define("enableEnergyLoss", false);

            energyLossPercentPerBlock = builder
                    .comment(
                            "Energy loss percentage per block (only if enableEnergyLoss is true)",
                            "Default: 0.001 (0.1% per block)"
                    )
                    .defineInRange("energyLossPercentPerBlock", 0.001, 0.0, 0.1);

            builder.pop();

            // ========== パフォーマンス設定 ==========

            builder.comment(
                    "===================================",
                    "Performance Settings",
                    "===================================",
                    "Configure performance optimizations"
            ).push("performance");

            performanceSection = builder
                    .comment("This section configures performance optimizations")
                    .define("_section_info", "Performance Settings");

            enableMultithreadedNetworks = builder
                    .comment(
                            "Enable multithreaded network calculations",
                            "WARNING: Experimental feature",
                            "May improve performance on multi-core systems"
                    )
                    .define("enableMultithreadedNetworks", false);

            maxNetworkCalculationsPerTick = builder
                    .comment(
                            "Maximum number of networks to calculate per tick",
                            "Remaining networks will be calculated in next tick",
                            "Set to -1 for unlimited",
                            "Default: -1"
                    )
                    .defineInRange("maxNetworkCalculationsPerTick", -1, -1, 10000);

            enableNetworkCaching = builder
                    .comment(
                            "Enable caching of network calculations",
                            "Improves performance but may cause slight delays in updates"
                    )
                    .define("enableNetworkCaching", true);

            cacheUpdateInterval = builder
                    .comment(
                            "How often to update cached network data (in ticks)",
                            "Only applies if enableNetworkCaching is true",
                            "Default: 5"
                    )
                    .defineInRange("cacheUpdateInterval", 5, 1, 100);

            builder.pop();

            // ========== デバッグ設定 ==========

            builder.comment(
                    "===================================",
                    "Debug Settings",
                    "===================================",
                    "Configure debug and visualization options"
            ).push("debug");

            debugSection = builder
                    .comment("This section configures debug features")
                    .define("_section_info", "Debug Settings");

            enableDebugLogging = builder
                    .comment(
                            "Enable debug logging for VE operations",
                            "WARNING: May cause log spam"
                    )
                    .define("enableDebugLogging", false);

            showMemoryWarnings = builder
                    .comment(
                            "Show warnings when containers hit memory limits",
                            "Useful for detecting when you need more RAM"
                    )
                    .define("showMemoryWarnings", true);

            logNetworkOperations = builder
                    .comment(
                            "Log network operations (merge, split, etc.)",
                            "Useful for debugging network issues"
                    )
                    .define("logNetworkOperations", false);

            showEnergyTransferParticles = builder
                    .comment(
                            "Show particles when energy is transferred",
                            "Visual feedback for debugging"
                    )
                    .define("showEnergyTransferParticles", false);

            enableNetworkVisualization = builder
                    .comment(
                            "Enable network visualization overlay (F3 + N)",
                            "Shows network boundaries and statistics",
                            "May impact performance"
                    )
                    .define("enableNetworkVisualization", false);

            builder.pop();
        }

        // ========== ヘルパーメソッド ==========

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

        /**
         * Basic Cable の容量を取得
         */
        public BigEnergy getBasicCableCapacity() {
            try {
                return BigEnergy.create(new BigInteger(basicCableCapacity.get()));
            } catch (NumberFormatException e) {
                return BigEnergy.create(new BigInteger("1000000000000000000000000000000000000000000000000"));
            }
        }

        /**
         * Basic Cable の転送レートを取得
         */
        public BigEnergy getBasicCableTransferRate() {
            try {
                return BigEnergy.create(new BigInteger(basicCableTransferRate.get()));
            } catch (NumberFormatException e) {
                return BigEnergy.create(new BigInteger("100000000000000000000000000000000000000000000"));
            }
        }

        // 他のTierも同様のヘルパーメソッドを追加
        public BigEnergy getAdvancedCableCapacity() {
            try {
                return BigEnergy.create(new BigInteger(advancedCableCapacity.get()));
            } catch (NumberFormatException e) {
                return BigEnergy.create(new BigInteger("10000000000000000000000000000000000000000000000000"));
            }
        }

        public BigEnergy getAdvancedCableTransferRate() {
            try {
                return BigEnergy.create(new BigInteger(advancedCableTransferRate.get()));
            } catch (NumberFormatException e) {
                return BigEnergy.create(new BigInteger("1000000000000000000000000000000000000000000"));
            }
        }

        public BigEnergy getEliteCableCapacity() {
            try {
                return BigEnergy.create(new BigInteger(eliteCableCapacity.get()));
            } catch (NumberFormatException e) {
                return BigEnergy.create(new BigInteger("100000000000000000000000000000000000000000000000000"));
            }
        }

        public BigEnergy getEliteCableTransferRate() {
            try {
                return BigEnergy.create(new BigInteger(eliteCableTransferRate.get()));
            } catch (NumberFormatException e) {
                return BigEnergy.create(new BigInteger("10000000000000000000000000000000000000000000"));
            }
        }

        public BigEnergy getUltimateCableCapacity() {
            try {
                return BigEnergy.create(new BigInteger(ultimateCableCapacity.get()));
            } catch (NumberFormatException e) {
                return BigEnergy.create(new BigInteger("1000000000000000000000000000000000000000000000000000"));
            }
        }

        public BigEnergy getUltimateCableTransferRate() {
            try {
                return BigEnergy.create(new BigInteger(ultimateCableTransferRate.get()));
            } catch (NumberFormatException e) {
                return BigEnergy.create(new BigInteger("100000000000000000000000000000000000000000000"));
            }
        }
    }
}