package kaede.valineenergycore.api.energy;

/**
 * メモリ制限を考慮したVEコンテナ
 */

public class MemoryLimitedVEContainer implements IVEContainer {

    protected BigEnergy stored = BigEnergy.ZERO;
    protected final BigEnergy declaredMaxEnergy;
    protected Runnable onContentsChangedCallback;

    /**
     * @param declaredMaxEnergy 宣言上の最大容量（メモリ制限でさらに制限される）
     */
    public MemoryLimitedVEContainer(BigEnergy declaredMaxEnergy) {
        this.declaredMaxEnergy = declaredMaxEnergy;
    }

    @Override
    public BigEnergy getEnergy() {
        return stored;
    }

    @Override
    public void setEnergy(BigEnergy energy) {
        // メモリ制限と宣言された最大値の小さい方で制限
        BigEnergy effectiveMax = getMaxEnergy();
        BigEnergy newEnergy = energy.min(effectiveMax).max(BigEnergy.ZERO);

        if (!newEnergy.equals(this.stored)) {
            this.stored = newEnergy;
            onContentsChanged();
        }
    }

    @Override
    public BigEnergy getMaxEnergy() {
        // 宣言された最大値とメモリベースの最大値の小さい方を返す
        BigEnergy memoryLimit = VEMemoryManager.getMaxVECapacity();
        return declaredMaxEnergy.min(memoryLimit);
    }

    /**
     * 宣言された最大容量を取得（メモリ制限を無視）
     */
    public BigEnergy getDeclaredMaxEnergy() {
        return declaredMaxEnergy;
    }

    /**
     * メモリ制限によって容量が制限されているかどうか
     */
    public boolean isLimitedByMemory() {
        return declaredMaxEnergy.greaterThan(VEMemoryManager.getMaxVECapacity());
    }

    public MemoryLimitedVEContainer setOnContentsChanged(Runnable callback) {
        this.onContentsChangedCallback = callback;
        return this;
    }

    @Override
    public void onContentsChanged() {
        if (onContentsChangedCallback != null) {
            onContentsChangedCallback.run();
        }
    }
}