package kaede.valineenergycore.api.energy;

import java.math.BigInteger;

/**
 * ValineEnergy (VE) のメモリベース最大値管理システム
 * 割り当てメモリ 1MB = 10^50 VE の容量
 */
public class VEMemoryManager {

    // 1MBあたりのVE容量: 10^50
    private static final BigInteger VE_PER_MB = new BigInteger("100000000000000000000000000000000000000000000000000");

    // キャッシュされた最大VE値
    private static BigEnergy cachedMaxVE = null;
    private static long cachedMaxMemoryMB = 0;

    // 最小保証容量（メモリが少なくても最低限保証）
    private static final BigEnergy MINIMUM_GUARANTEED = BigEnergy.create(new BigInteger("1000000000000")); // 10^12

    /**
     * 現在利用可能な最大VE容量を取得
     * @return 現在の最大VE容量
     */
    public static BigEnergy getMaxVECapacity() {
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
     * @return 最大メモリ容量（MB）
     */
    private static long getMaxMemoryMB() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemoryBytes = runtime.maxMemory();
        return maxMemoryBytes / (1024 * 1024); // バイトをMBに変換
    }

    /**
     * メモリ量から最大VE容量を計算
     * @param memoryMB メモリ容量（MB）
     * @return 最大VE容量
     */
    private static BigEnergy calculateMaxVE(long memoryMB) {
        if (memoryMB <= 0) {
            return MINIMUM_GUARANTEED;
        }

        // メモリMB × 10^50
        BigInteger result = VE_PER_MB.multiply(BigInteger.valueOf(memoryMB));
        BigEnergy calculated = BigEnergy.create(result);

        // 最小保証値を下回らないようにする
        return calculated.max(MINIMUM_GUARANTEED);
    }

    /**
     * 現在のメモリ使用状況を取得
     * @return メモリ使用情報
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
     * @param energy 検証するVE量
     * @return 超えていなければtrue
     */
    public static boolean isWithinLimit(BigEnergy energy) {
        return energy.smallerOrEqual(getMaxVECapacity());
    }

    /**
     * VE量を現在の最大容量でクランプ
     * @param energy クランプするVE量
     * @return クランプされたVE量
     */
    public static BigEnergy clampToLimit(BigEnergy energy) {
        return energy.min(getMaxVECapacity());
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
                    "Memory: %d/%d MB (%.1f%%) | Max VE Capacity: %s",
                    getUsedMemoryMB(),
                    getMaxMemoryMB(),
                    getUsagePercentage(),
                    getMaxVECapacity()
            );
        }
    }
}