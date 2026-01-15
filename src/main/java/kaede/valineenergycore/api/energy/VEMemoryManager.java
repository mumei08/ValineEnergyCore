package kaede.valineenergycore.api.energy;

import kaede.valineenergycore.common.config.VEConfig;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import java.math.BigInteger;

/**
 * ValineEnergy (VE) のメモリベース最大値管理システム - Config対応版
 * 設定ファイルから値を動的に取得
 */
public class VEMemoryManager {

    private static final Logger LOGGER = LogUtils.getLogger();

    // キャッシュされた最大VE値
    private static BigEnergy cachedMaxVE = null;
    private static long cachedMaxMemoryMB = 0;
    private static long lastWarningTime = 0;
    private static final long WARNING_COOLDOWN_MS = 60000; // 1分

    /**
     * 現在利用可能な最大VE容量を取得
     * 設定でメモリベース制限が無効の場合は無制限
     */
    public static BigEnergy getMaxVECapacity() {
        // メモリベース制限が無効の場合は事実上無制限
        if (!VEConfig.COMMON.enableMemoryBasedLimits.get()) {
            // 極めて大きな値を返す（実質無制限）
            return BigEnergy.create(new BigInteger("9".repeat(100))); // 10^100に近い値
        }

        long currentMaxMemoryMB = getMaxMemoryMB();

        // キャッシュが有効ならそれを返す
        if (cachedMaxVE != null && cachedMaxMemoryMB == currentMaxMemoryMB) {
            return cachedMaxVE;
        }

        // 再計算
        cachedMaxMemoryMB = currentMaxMemoryMB;
        cachedMaxVE = calculateMaxVE(currentMaxMemoryMB);

        return cachedMaxVE;
    }

    /**
     * 割り当てメモリ（MB単位）を取得
     */
    private static long getMaxMemoryMB() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemoryBytes = runtime.maxMemory();
        return maxMemoryBytes / (1024 * 1024);
    }

    /**
     * メモリ量から最大VE容量を計算
     * Configから設定値を取得
     */
    private static BigEnergy calculateMaxVE(long memoryMB) {
        if (memoryMB <= 0) {
            return VEConfig.COMMON.getMinimumGuaranteedCapacity();
        }

        // Configから VE per MB を取得
        BigInteger vePerMB = VEConfig.COMMON.getVEPerMB();

        // メモリMB × VE per MB
        BigInteger result = vePerMB.multiply(BigInteger.valueOf(memoryMB));
        BigEnergy calculated = BigEnergy.create(result);

        // 最小保証値を下回らないようにする
        BigEnergy minimum = VEConfig.COMMON.getMinimumGuaranteedCapacity();
        return calculated.max(minimum);
    }

    /**
     * 現在のメモリ使用状況を取得
     */
    public static MemoryInfo getMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        return new MemoryInfo(maxMemory, totalMemory, usedMemory, freeMemory);
    }

    /**
     * 指定されたVE量が現在の最大容量を超えていないか検証
     */
    public static boolean isWithinLimit(BigEnergy energy) {
        if (!VEConfig.COMMON.enableMemoryBasedLimits.get()) {
            return true; // 制限が無効なら常にtrue
        }
        return energy.smallerOrEqual(getMaxVECapacity());
    }

    /**
     * VE量を現在の最大容量でクランプ
     */
    public static BigEnergy clampToLimit(BigEnergy energy) {
        if (!VEConfig.COMMON.enableMemoryBasedLimits.get()) {
            return energy; // 制限が無効ならそのまま返す
        }
        return energy.min(getMaxVECapacity());
    }

    /**
     * メモリ使用率が閾値を超えているか確認し、必要なら警告
     */
    public static void checkMemoryUsage() {
        if (!VEConfig.COMMON.showMemoryWarnings.get()) {
            return;
        }

        MemoryInfo info = getMemoryInfo();
        double usageRatio = info.getUsagePercentage() / 100.0;
        double threshold = VEConfig.COMMON.memoryUsageWarningThreshold.get();

        if (usageRatio > threshold) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastWarningTime > WARNING_COOLDOWN_MS) {
                LOGGER.warn(
                        "VE Memory Warning: Memory usage is at {:.1f}% (threshold: {:.1f}%). " +
                                "Consider allocating more RAM or reducing VE container usage.",
                        usageRatio * 100,
                        threshold * 100
                );
                lastWarningTime = currentTime;
            }
        }
    }

    /**
     * キャッシュをクリア（Config変更時に呼ぶ）
     */
    public static void clearCache() {
        cachedMaxVE = null;
        cachedMaxMemoryMB = 0;
        LOGGER.info("VE Memory Manager cache cleared");
    }

    /**
     * メモリ情報を保持するクラス
     */
    public static class MemoryInfo {
        private final long maxMemoryBytes;
        private final long totalMemoryBytes;
        private final long usedMemoryBytes;
        private final long freeMemoryBytes;

        public MemoryInfo(long maxMemory, long totalMemory, long usedMemory, long freeMemory) {
            this.maxMemoryBytes = maxMemory;
            this.totalMemoryBytes = totalMemory;
            this.usedMemoryBytes = usedMemory;
            this.freeMemoryBytes = freeMemory;
        }

        public long getMaxMemoryMB() {
            return maxMemoryBytes / (1024 * 1024);
        }

        public long getTotalMemoryMB() {
            return totalMemoryBytes / (1024 * 1024);
        }

        public long getUsedMemoryMB() {
            return usedMemoryBytes / (1024 * 1024);
        }

        public long getFreeMemoryMB() {
            return freeMemoryBytes / (1024 * 1024);
        }

        public double getUsagePercentage() {
            if (maxMemoryBytes == 0) return 0.0;
            return (double) usedMemoryBytes / maxMemoryBytes * 100.0;
        }

        public BigEnergy getMaxVECapacity() {
            return VEMemoryManager.getMaxVECapacity();
        }

        @Override
        public String toString() {
            return String.format(
                    "Memory: %d/%d MB (%.1f%%) | Max VE Capacity: %s | Memory-based limits: %s",
                    getUsedMemoryMB(),
                    getMaxMemoryMB(),
                    getUsagePercentage(),
                    getMaxVECapacity(),
                    VEConfig.COMMON.enableMemoryBasedLimits.get() ? "Enabled" : "Disabled"
            );
        }
    }
}