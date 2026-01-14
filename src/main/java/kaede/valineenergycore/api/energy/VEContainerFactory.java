package kaede.valineenergycore.api.energy;

import java.math.BigInteger;

/**
 * VEコンテナのファクトリークラス
 */

public class VEContainerFactory {

    /**
     * メモリ制限を考慮したコンテナを作成
     * @param requestedCapacity 要求する容量
     * @return 作成されたコンテナ
     */
    public static MemoryLimitedVEContainer createMemoryLimited(BigEnergy requestedCapacity) {
        return new MemoryLimitedVEContainer(requestedCapacity);
    }

    /**
     * メモリ容量を基準にしたコンテナを作成
     * @param memoryPercentage 利用可能なメモリの何パーセントを使用するか（0.0～1.0）
     * @return 作成されたコンテナ
     */
    public static MemoryLimitedVEContainer createFromMemoryPercentage(double memoryPercentage) {
        if (memoryPercentage < 0.0 || memoryPercentage > 1.0) {
            throw new IllegalArgumentException("Memory percentage must be between 0.0 and 1.0");
        }

        BigEnergy maxCapacity = VEMemoryManager.getMaxVECapacity();
        BigEnergy requestedCapacity = maxCapacity.multiply(memoryPercentage);

        return new MemoryLimitedVEContainer(requestedCapacity);
    }

    /**
     * 特定のMB数に対応する容量のコンテナを作成
     * @param memoryMB 対応するメモリ量（MB）
     * @return 作成されたコンテナ
     */
    public static MemoryLimitedVEContainer createFromMemoryMB(long memoryMB) {
        // 1MB = 10^50 VE
        BigInteger capacity = new BigInteger("100000000000000000000000000000000000000000000000000")
                .multiply(BigInteger.valueOf(memoryMB));

        return new MemoryLimitedVEContainer(BigEnergy.create(capacity));
    }
}
