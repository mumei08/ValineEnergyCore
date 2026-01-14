package kaede.valineenergycore.common.content.network;

import kaede.valineenergycore.api.energy.BigEnergy;

/**
 * ケーブルのTier定義
 */

public enum VECableTier {
    BASIC(
            BigEnergy.create("1000000000000000000000000000000000000000000000000"),  // 10^48
            BigEnergy.create("100000000000000000000000000000000000000000000")      // 10^44
    ),
    ADVANCED(
            BigEnergy.create("10000000000000000000000000000000000000000000000000"), // 10^49
            BigEnergy.create("1000000000000000000000000000000000000000000")        // 10^45
    ),
    ELITE(
            BigEnergy.create("100000000000000000000000000000000000000000000000000"), // 10^50
            BigEnergy.create("10000000000000000000000000000000000000000000")        // 10^46
    ),
    ULTIMATE(
            BigEnergy.create("1000000000000000000000000000000000000000000000000000"), // 10^51
            BigEnergy.create("100000000000000000000000000000000000000000000")        // 10^47
    );

    private final BigEnergy capacity;      // バッファ容量
    private final BigEnergy transferRate;  // 転送レート/tick

    VECableTier(BigEnergy capacity, BigEnergy transferRate) {
        this.capacity = capacity;
        this.transferRate = transferRate;
    }

    public BigEnergy getCapacity() {
        return capacity;
    }

    public BigEnergy getTransferRate() {
        return transferRate;
    }

    public VECableTier getNext() {
        return switch (this) {
            case BASIC -> ADVANCED;
            case ADVANCED -> ELITE;
            case ELITE -> ULTIMATE;
            case ULTIMATE -> ULTIMATE;
        };
    }
}