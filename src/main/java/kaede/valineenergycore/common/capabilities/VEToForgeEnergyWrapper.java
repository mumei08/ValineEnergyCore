package kaede.valineenergycore.common.capabilities;

import kaede.valineenergycore.api.energy.BigEnergy;
import kaede.valineenergycore.api.energy.IVEContainer;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * IVEContainer を Forge の IEnergyStorage にラップ
 * 他MODとの互換性のため
 */

public class VEToForgeEnergyWrapper implements IEnergyStorage {

    private final IVEContainer container;

    public VEToForgeEnergyWrapper(IVEContainer container) {
        this.container = container;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!container.canReceive() || maxReceive <= 0) {
            return 0;
        }

        BigEnergy veAmount = VECapabilities.convertFromForgeEnergy(maxReceive);
        BigEnergy inserted = container.insert(
                veAmount,
                simulate ? IVEContainer.Action.SIMULATE : IVEContainer.Action.EXECUTE
        );

        return VECapabilities.convertToForgeEnergy(inserted);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (!container.canExtract() || maxExtract <= 0) {
            return 0;
        }

        BigEnergy veAmount = VECapabilities.convertFromForgeEnergy(maxExtract);
        BigEnergy extracted = container.extract(
                veAmount,
                simulate ? IVEContainer.Action.SIMULATE : IVEContainer.Action.EXECUTE
        );

        return VECapabilities.convertToForgeEnergy(extracted);
    }

    @Override
    public int getEnergyStored() {
        return VECapabilities.convertToForgeEnergy(container.getEnergy());
    }

    @Override
    public int getMaxEnergyStored() {
        return VECapabilities.convertToForgeEnergy(container.getMaxEnergy());
    }

    @Override
    public boolean canExtract() {
        return container.canExtract();
    }

    @Override
    public boolean canReceive() {
        return container.canReceive();
    }
}
