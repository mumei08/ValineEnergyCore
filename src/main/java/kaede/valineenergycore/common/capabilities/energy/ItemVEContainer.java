package kaede.valineenergycore.common.capabilities.energy;

import kaede.valineenergycore.api.energy.BigEnergy;

import javax.annotation.Nonnull;

/**
 * アイテム用のVEコンテナ
 * Energy Tablet的なアイテムで使用
 */
public class ItemVEContainer extends BasicVEContainer {

    protected final BigEnergy maxTransferRate;

    public ItemVEContainer(@Nonnull BigEnergy maxEnergy,
                           @Nonnull BigEnergy maxTransferRate) {
        super(maxEnergy);
        this.maxTransferRate = maxTransferRate;
    }

    @Nonnull
    @Override
    public BigEnergy insert(@Nonnull BigEnergy energy, Action action) {
        BigEnergy limited = energy.min(maxTransferRate);
        return super.insert(limited, action);
    }

    @Nonnull
    @Override
    public BigEnergy extract(@Nonnull BigEnergy energy, Action action) {
        BigEnergy limited = energy.min(maxTransferRate);
        return super.extract(limited, action);
    }

    /**
     * 充電率を取得（0.0～1.0）
     */
    public double getChargeRatio() {
        if (maxEnergy.isZero()) return 0.0;
        return stored.doubleValue() / maxEnergy.doubleValue();
    }

    /**
     * 充電率をパーセントで取得（0～100）
     */
    public int getChargePercent() {
        return (int) (getChargeRatio() * 100);
    }
}