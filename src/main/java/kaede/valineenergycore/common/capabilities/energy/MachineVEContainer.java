package kaede.valineenergycore.common.capabilities.energy;

import kaede.valineenergycore.api.energy.BigEnergy;

import javax.annotation.Nonnull;

/**
 * 機械用のVEコンテナ
 * 入力レートと使用量の制限を持つ
 */

public class MachineVEContainer extends BasicVEContainer {

    protected final BigEnergy maxInputRate;
    protected final BigEnergy usageRate;

    public MachineVEContainer(@Nonnull BigEnergy maxEnergy,
                              @Nonnull BigEnergy maxInputRate,
                              @Nonnull BigEnergy usageRate) {
        super(maxEnergy);
        this.maxInputRate = maxInputRate;
        this.usageRate = usageRate;
    }

    @Nonnull
    @Override
    public BigEnergy insert(@Nonnull BigEnergy energy, Action action) {
        // 最大入力レートで制限
        BigEnergy limited = energy.min(maxInputRate);
        return super.insert(limited, action);
    }

    /**
     * 機械を動かすのに十分なエネルギーがあるか
     */
    public boolean hasEnoughEnergy() {
        return stored.greaterOrEqual(usageRate);
    }

    /**
     * 使用量分のエネルギーを消費
     *
     * @return 消費に成功したらtrue
     */
    public boolean consumeEnergy() {
        if (hasEnoughEnergy()) {
            stored = stored.subtract(usageRate);
            onContentsChanged();
            return true;
        }
        return false;
    }

    public BigEnergy getMaxInputRate() {
        return maxInputRate;
    }

    public BigEnergy getUsageRate() {
        return usageRate;
    }
}
