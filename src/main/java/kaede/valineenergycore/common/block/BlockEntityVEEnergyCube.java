package kaede.valineenergycore.common.block;

import kaede.valineenergycore.common.capabilities.VECapabilityProvider;
import kaede.valineenergycore.common.capabilities.energy.InfiniteVEContainer;
import kaede.valineenergycore.common.registration.VERegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * VEエネルギーキューブのBlockEntity
 */

public class BlockEntityVEEnergyCube extends BlockEntity {

    private InfiniteVEContainer buffer;
    private VECapabilityProvider capabilityProvider;

    public BlockEntityVEEnergyCube(BlockPos pos, BlockState state) {
        super(VERegistration.VE_ENERGY_CUBE_BE.get(), pos, state);
        initializeComponents();
    }

    private void initializeComponents() {
        this.buffer = new InfiniteVEContainer();
        this.capabilityProvider = new VECapabilityProvider(buffer);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntityVEEnergyCube blockEntity) {
        if (!level.isClientSide) {
            blockEntity.serverTick();
        }
    }

    private void serverTick() {
        // キューブは基本的に受動的なので、特別な処理は不要
    }

    public InfiniteVEContainer getBuffer() {
        return buffer;
    }

    // ========== Capability ==========

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (!this.remove) {
            LazyOptional<T> capability = capabilityProvider.getCapability(cap, side);
            if (capability.isPresent()) {
                return capability;
            }
        }
        return super.getCapability(cap, side);
    }

    // ========== NBT保存/読込 ==========

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (tag.contains("Buffer")) {
            buffer.readFromNBT(tag.getCompound("Buffer"));
        }

        this.capabilityProvider = new VECapabilityProvider(buffer);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        CompoundTag bufferTag = new CompoundTag();
        buffer.writeToNBT(bufferTag);
        tag.put("Buffer", bufferTag);
    }

    // ========== ネットワーク同期 ==========

    @Override
    public CompoundTag getUpdateTag() {
        return super.getUpdateTag();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // ========== ライフサイクル ==========

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (capabilityProvider != null) {
            capabilityProvider.invalidate();
        }
    }
}
