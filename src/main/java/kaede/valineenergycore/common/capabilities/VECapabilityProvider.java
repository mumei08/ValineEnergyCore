package kaede.valineenergycore.common.capabilities;

import kaede.valineenergycore.api.energy.IVEContainer;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * VE Capability Provider
 * BlockEntityにアタッチして使用
 */

public class VECapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    private final IVEContainer container;
    private final LazyOptional<IVEContainer> veCapability;
    private final LazyOptional<IEnergyStorage> forgeEnergyCapability;

    public VECapabilityProvider(IVEContainer container) {
        this.container = container;
        this.veCapability = LazyOptional.of(() -> container);
        this.forgeEnergyCapability = LazyOptional.of(() -> new VEToForgeEnergyWrapper(container));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        // VE Capability
        if (cap == net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY) {
            return forgeEnergyCapability.cast();
        }

        // TODO: VE独自のCapabilityを追加する場合はここに
        // if (cap == VECapabilities.VE_CONTAINER) {
        //     return veCapability.cast();
        // }

        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        container.writeToNBT(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        container.readFromNBT(nbt);
    }

    /**
     * BlockEntityが削除される時に呼ぶ
     */
    public void invalidate() {
        veCapability.invalidate();
        forgeEnergyCapability.invalidate();
    }
}
