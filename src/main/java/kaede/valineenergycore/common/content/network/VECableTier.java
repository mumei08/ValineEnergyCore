package kaede.valineenergycore.common.content.network;

import kaede.valineenergycore.api.energy.BigEnergy;
import kaede.valineenergycore.common.config.VEConfig;

/**
 * ケーブルのTier定義 - Config対応版
 * 容量と転送レートは設定ファイルから取得
 */
public enum VECableTier {
    BASIC,
    ADVANCED,
    ELITE,
    ULTIMATE;

    /**
     * このTierのバッファ容量を取得
     * 設定ファイルから動的に取得
     */
    public BigEnergy getCapacity() {
        return switch (this) {
            case BASIC -> VEConfig.COMMON.getBasicCableCapacity();
            case ADVANCED -> VEConfig.COMMON.getAdvancedCableCapacity();
            case ELITE -> VEConfig.COMMON.getEliteCableCapacity();
            case ULTIMATE -> VEConfig.COMMON.getUltimateCableCapacity();
        };
    }

    /**
     * このTierの転送レート/tickを取得
     * 設定ファイルから動的に取得
     */
    public BigEnergy getTransferRate() {
        return switch (this) {
            case BASIC -> VEConfig.COMMON.getBasicCableTransferRate();
            case ADVANCED -> VEConfig.COMMON.getAdvancedCableTransferRate();
            case ELITE -> VEConfig.COMMON.getEliteCableTransferRate();
            case ULTIMATE -> VEConfig.COMMON.getUltimateCableTransferRate();
        };
    }

    /**
     * 次のTierを取得
     */
    public VECableTier getNext() {
        return switch (this) {
            case BASIC -> ADVANCED;
            case ADVANCED -> ELITE;
            case ELITE -> ULTIMATE;
            case ULTIMATE -> ULTIMATE;
        };
    }

    /**
     * 前のTierを取得
     */
    public VECableTier getPrevious() {
        return switch (this) {
            case BASIC -> BASIC;
            case ADVANCED -> BASIC;
            case ELITE -> ADVANCED;
            case ULTIMATE -> ELITE;
        };
    }

    /**
     * このTierより上位かどうか
     */
    public boolean isHigherThan(VECableTier other) {
        return this.ordinal() > other.ordinal();
    }

    /**
     * このTierより下位かどうか
     */
    public boolean isLowerThan(VECableTier other) {
        return this.ordinal() < other.ordinal();
    }

    /**
     * Tierのインデックスを取得（0-3）
     */
    public int getTierIndex() {
        return this.ordinal();
    }

    /**
     * Tier名を取得（表示用）
     */
    public String getDisplayName() {
        return switch (this) {
            case BASIC -> "Basic";
            case ADVANCED -> "Advanced";
            case ELITE -> "Elite";
            case ULTIMATE -> "Ultimate";
        };
    }

    /**
     * Tierの色コードを取得（GUI表示用）
     */
    public int getColor() {
        return switch (this) {
            case BASIC -> 0x7F7F7F;      // グレー
            case ADVANCED -> 0xFF5555;    // 赤
            case ELITE -> 0x55FFFF;       // シアン
            case ULTIMATE -> 0xAA00AA;    // 紫
        };
    }

    /**
     * インデックスからTierを取得
     */
    public static VECableTier fromIndex(int index) {
        VECableTier[] tiers = values();
        if (index < 0 || index >= tiers.length) {
            return BASIC;
        }
        return tiers[index];
    }

    /**
     * 文字列からTierを取得（大文字小文字を区別しない）
     */
    public static VECableTier fromString(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return BASIC;
        }
    }
}