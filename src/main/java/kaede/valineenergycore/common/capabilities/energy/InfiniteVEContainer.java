package kaede.valineenergycore.common.capabilities.energy;

import kaede.valineenergycore.api.energy.BigEnergy;
import kaede.valineenergycore.api.energy.IVEContainer;
import kaede.valineenergycore.api.energy.VEMemoryManager;
import javax.annotation.Nonnull;

/**
 * 無限容量VEコンテナ
 * BigEnergyの最大値（メモリベース）まで保持可能
 */
public class InfiniteVEContainer implements IVEContainer {

    protected BigEnergy stored = BigEnergy.ZERO;
    protected Runnable onContentsChangedCallback;

    public InfiniteVEContainer() {
        // コンストラクタは何もしない
    }

    @Nonnull
    @Override
    public BigEnergy getEnergy() {
        return stored;
    }

    @Override
    public void setEnergy(@Nonnull BigEnergy energy) {
        // メモリベースの最大値でクランプ
        BigEnergy newEnergy = VEMemoryManager.clampToLimit(energy).max(BigEnergy.ZERO);

        if (!newEnergy.equals(this.stored)) {
            this.stored = newEnergy;
            onContentsChanged();
        }
    }

    @Nonnull
    @Override
    public BigEnergy getMaxEnergy() {
        // メモリベースの最大容量を返す
        return VEMemoryManager.getMaxVECapacity();
    }

    public InfiniteVEContainer setOnContentsChanged(Runnable callback) {
        this.onContentsChangedCallback = callback;
        return this;
    }

    @Override
    public void onContentsChanged() {
        if (onContentsChangedCallback != null) {
            onContentsChangedCallback.run();
        }
    }

    @Override
    public boolean canReceive() {
        // 常に受け取り可能
        return true;
    }

    @Override
    public boolean canExtract() {
        // エネルギーがある限り抽出可能
        return !stored.isZero();
    }
}