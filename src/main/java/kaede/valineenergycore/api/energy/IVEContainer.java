package kaede.valineenergycore.api.energy;

import net.minecraft.nbt.CompoundTag;
import javax.annotation.Nonnull;

/**
 * ValineEnergy (VE) を保持するコンテナのインターフェース
 * Mekanismの IEnergyContainer を参考にした設計
 */
public interface IVEContainer {

    /**
     * 現在保持しているエネルギー量を取得
     * @return 現在のVE量
     */
    @Nonnull
    BigEnergy getEnergy();

    /**
     * エネルギー量を設定
     * @param energy 設定するVE量
     */
    void setEnergy(@Nonnull BigEnergy energy);

    /**
     * 最大容量を取得
     * @return 最大VE容量
     */
    @Nonnull
    BigEnergy getMaxEnergy();

    /**
     * エネルギーを挿入
     * @param energy 挿入するVE量
     * @param action シミュレーションか実行か
     * @return 実際に挿入されたVE量
     */
    @Nonnull
    default BigEnergy insert(@Nonnull BigEnergy energy, Action action) {
        if (energy.isZero() || !canReceive()) {
            return BigEnergy.ZERO;
        }

        BigEnergy needed = getNeeded();
        BigEnergy toInsert = energy.min(needed);

        if (action.execute()) {
            setEnergy(getEnergy().add(toInsert));
            onContentsChanged();
        }

        return toInsert;
    }

    /**
     * エネルギーを抽出
     * @param energy 抽出するVE量
     * @param action シミュレーションか実行か
     * @return 実際に抽出されたVE量
     */
    @Nonnull
    default BigEnergy extract(@Nonnull BigEnergy energy, Action action) {
        if (energy.isZero() || !canExtract()) {
            return BigEnergy.ZERO;
        }

        BigEnergy current = getEnergy();
        BigEnergy toExtract = energy.min(current);

        if (action.execute()) {
            setEnergy(current.subtract(toExtract));
            onContentsChanged();
        }

        return toExtract;
    }

    /**
     * まだ受け入れ可能なエネルギー量を取得
     * @return 受け入れ可能なVE量
     */
    @Nonnull
    default BigEnergy getNeeded() {
        return getMaxEnergy().subtract(getEnergy());
    }

    /**
     * エネルギーを受け取れるかどうか
     * @return 受け取り可能ならtrue
     */
    default boolean canReceive() {
        return true;
    }

    /**
     * エネルギーを出力できるかどうか
     * @return 出力可能ならtrue
     */
    default boolean canExtract() {
        return true;
    }

    /**
     * コンテナが空かどうか
     * @return 空ならtrue
     */
    default boolean isEmpty() {
        return getEnergy().isZero();
    }

    /**
     * コンテナが満タンかどうか
     * @return 満タンならtrue
     */
    default boolean isFull() {
        return getEnergy().greaterOrEqual(getMaxEnergy());
    }

    /**
     * 内容が変更された時のコールバック
     */
    default void onContentsChanged() {
        // オーバーライドして使用
    }

    /**
     * NBTに保存
     * @param tag 保存先のCompoundTag
     */
    default void writeToNBT(@Nonnull CompoundTag tag) {
        getEnergy().writeToNBT(tag, "Energy");
    }

    /**
     * NBTから読み込み
     * @param tag 読み込み元のCompoundTag
     */
    default void readFromNBT(@Nonnull CompoundTag tag) {
        setEnergy(BigEnergy.readFromNBT(tag, "Energy"));
    }

    /**
     * アクションタイプ（実行かシミュレーションか）
     */
    enum Action {
        EXECUTE(true),
        SIMULATE(false);

        private final boolean execute;

        Action(boolean execute) {
            this.execute = execute;
        }

        public boolean execute() {
            return execute;
        }

        public boolean simulate() {
            return !execute;
        }
    }
}