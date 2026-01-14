package kaede.valineenergycore.common.capabilities.energy;

import kaede.valineenergycore.api.energy.BigEnergy;
import kaede.valineenergycore.api.energy.IVEContainer;
import javax.annotation.Nonnull;
import java.util.function.Predicate;

/**
 * IVEContainer の基本実装
 * Mekanismの BasicEnergyContainer を参考にした設計
 */
public class BasicVEContainer implements IVEContainer {

    protected BigEnergy stored = BigEnergy.ZERO;
    protected final BigEnergy maxEnergy;
    protected final Predicate<BigEnergy> canExtract;
    protected final Predicate<BigEnergy> canReceive;
    protected Runnable onContentsChangedCallback;

    /**
     * 基本コンストラクタ
     * @param maxEnergy 最大容量
     */
    public BasicVEContainer(@Nonnull BigEnergy maxEnergy) {
        this(maxEnergy, alwaysTrue(), alwaysTrue());
    }

    /**
     * 詳細コンストラクタ
     * @param maxEnergy 最大容量
     * @param canExtract 抽出可能条件
     * @param canReceive 受取可能条件
     */
    public BasicVEContainer(@Nonnull BigEnergy maxEnergy,
                            Predicate<BigEnergy> canExtract,
                            Predicate<BigEnergy> canReceive) {
        this.maxEnergy = maxEnergy;
        this.canExtract = canExtract;
        this.canReceive = canReceive;
    }

    /**
     * ビルダーパターンでコールバック設定
     */
    public BasicVEContainer setOnContentsChanged(Runnable callback) {
        this.onContentsChangedCallback = callback;
        return this;
    }

    @Nonnull
    @Override
    public BigEnergy getEnergy() {
        return stored;
    }

    @Override
    public void setEnergy(@Nonnull BigEnergy energy) {
        if (!energy.equals(this.stored)) {
            this.stored = energy.min(maxEnergy).max(BigEnergy.ZERO);
            onContentsChanged();
        }
    }

    @Nonnull
    @Override
    public BigEnergy getMaxEnergy() {
        return maxEnergy;
    }

    @Override
    public boolean canExtract() {
        return canExtract.test(stored);
    }

    @Override
    public boolean canReceive() {
        return canReceive.test(stored);
    }

    @Override
    public void onContentsChanged() {
        if (onContentsChangedCallback != null) {
            onContentsChangedCallback.run();
        }
    }

    // ========== ヘルパーメソッド ==========

    protected static Predicate<BigEnergy> alwaysTrue() {
        return energy -> true;
    }

    protected static Predicate<BigEnergy> alwaysFalse() {
        return energy -> false;
    }

    protected static Predicate<BigEnergy> notEmpty() {
        return energy -> !energy.isZero();
    }

    protected static Predicate<BigEnergy> extractPredicate() {
        return notEmpty();
    }

    protected static Predicate<BigEnergy> receivePredicate(BigEnergy max) {
        return energy -> energy.smallerThan(max);
    }
}