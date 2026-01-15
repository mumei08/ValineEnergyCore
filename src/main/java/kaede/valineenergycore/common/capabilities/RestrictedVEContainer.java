package kaede.valineenergycore.common.capabilities;

import kaede.valineenergycore.api.energy.BigEnergy;
import kaede.valineenergycore.api.energy.IVEContainer;

import javax.annotation.Nonnull;

/**
 * 入出力を制限するVEContainer
 */

public class RestrictedVEContainer implements IVEContainer {

    private final IVEContainer wrapped;
    private final boolean canInput;
    private final boolean canOutput;

    public RestrictedVEContainer(IVEContainer wrapped, boolean canInput, boolean canOutput) {
        this.wrapped = wrapped;
        this.canInput = canInput;
        this.canOutput = canOutput;
    }

    @Nonnull
    @Override
    public BigEnergy getEnergy() {
        return wrapped.getEnergy();
    }

    @Override
    public void setEnergy(@Nonnull BigEnergy energy) {
        wrapped.setEnergy(energy);
    }

    @Nonnull
    @Override
    public BigEnergy getMaxEnergy() {
        return wrapped.getMaxEnergy();
    }

    @Override
    public boolean canReceive() {
        return canInput && wrapped.canReceive();
    }

    @Override
    public boolean canExtract() {
        return canOutput && wrapped.canExtract();
    }

    @Nonnull
    @Override
    public BigEnergy insert(@Nonnull BigEnergy energy, Action action) {
        if (!canInput) {
            return BigEnergy.ZERO;
        }
        return wrapped.insert(energy, action);
    }

    @Nonnull
    @Override
    public BigEnergy extract(@Nonnull BigEnergy energy, Action action) {
        if (!canOutput) {
            return BigEnergy.ZERO;
        }
        return wrapped.extract(energy, action);
    }
}
