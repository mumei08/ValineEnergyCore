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
 * 面ごとに異なるCapabilityを提供するProvider
 */

public class SidedVECapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    private final IVEContainer container;
    private final Direction[] allowedSides;
    private final boolean canInput;
    private final boolean canOutput;

    private final LazyOptional<IEnergyStorage> forgeEnergyCapability;

    /**
     * @param container VEコンテナ
     * @param allowedSides 接続を許可する面 (nullの場合は全面)
     * @param canInput 入力可能か
     * @param canOutput 出力可能か
     */
    public SidedVECapabilityProvider(IVEContainer container,
                                     @Nullable Direction[] allowedSides,
                                     boolean canInput,
                                     boolean canOutput) {
        this.container = container;
        this.allowedSides = allowedSides;
        this.canInput = canInput;
        this.canOutput = canOutput;

        // 入出力制限を考慮したラッパーを作成
        IVEContainer restrictedContainer = new RestrictedVEContainer(container, canInput, canOutput);
        this.forgeEnergyCapability = LazyOptional.of(() -> new VEToForgeEnergyWrapper(restrictedContainer));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        // 指定された面からのアクセスが許可されているかチェック
        if (allowedSides != null && side != null) {
            boolean allowed = false;
            for (Direction allowedSide : allowedSides) {
                if (allowedSide == side) {
                    allowed = true;
                    break;
                }
            }
            if (!allowed) {
                return LazyOptional.empty();
            }
        }

        if (cap == net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY) {
            return forgeEnergyCapability.cast();
        }

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

    public void invalidate() {
        forgeEnergyCapability.invalidate();
    }
}