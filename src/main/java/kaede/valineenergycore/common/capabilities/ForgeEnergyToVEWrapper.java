package kaede.valineenergycore.common.capabilities;

import kaede.valineenergycore.api.energy.BigEnergy;
import kaede.valineenergycore.api.energy.IVEContainer;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;

/**
 * Forge の IEnergyStorage を IVEContainer にラップ
 * 逆方向の互換性のため
 */

public class ForgeEnergyToVEWrapper implements IVEContainer {

    private final IEnergyStorage storage;

    public ForgeEnergyToVEWrapper(IEnergyStorage storage) {
        this.storage = storage;
    }

    @Nonnull
    @Override
    public BigEnergy getEnergy() {
        return VECapabilities.convertFromForgeEnergy(storage.getEnergyStored());
    }

    @Override
    public void setEnergy(@Nonnull BigEnergy energy) {
        // IEnergyStorage には直接設定メソッドがないため
        // extractとreceiveで調整
        int target = VECapabilities.convertToForgeEnergy(energy);
        int current = storage.getEnergyStored();

        if (target > current) {
            storage.receiveEnergy(target - current, false);
        } else if (target < current) {
            storage.extractEnergy(current - target, false);
        }
    }

    @Nonnull
    @Override
    public BigEnergy getMaxEnergy() {
        return VECapabilities.convertFromForgeEnergy(storage.getMaxEnergyStored());
    }

    @Override
    public boolean canExtract() {
        return storage.canExtract();
    }

    @Override
    public boolean canReceive() {
        return storage.canReceive();
    }

    @Nonnull
    @Override
    public BigEnergy insert(@Nonnull BigEnergy energy, Action action) {
        int feAmount = VECapabilities.convertToForgeEnergy(energy);
        int inserted = storage.receiveEnergy(feAmount, action.simulate());
        return VECapabilities.convertFromForgeEnergy(inserted);
    }

    @Nonnull
    @Override
    public BigEnergy extract(@Nonnull BigEnergy energy, Action action) {
        int feAmount = VECapabilities.convertToForgeEnergy(energy);
        int extracted = storage.extractEnergy(feAmount, action.simulate());
        return VECapabilities.convertFromForgeEnergy(extracted);
    }
}
