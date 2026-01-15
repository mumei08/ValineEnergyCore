package kaede.valineenergycore.common.block;

import kaede.valineenergycore.api.energy.BigEnergy;
import kaede.valineenergycore.api.energy.VEMemoryManager;
import kaede.valineenergycore.common.capabilities.VECapabilityProvider;
import kaede.valineenergycore.common.capabilities.energy.InfiniteVEContainer;
import kaede.valineenergycore.common.registration.VERegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * VEエネルギーキューブ
 * BigEnergyの最大値まで保存可能
 */
public class BlockVEEnergyCube extends Block implements EntityBlock {

    public BlockVEEnergyCube(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BlockEntityVEEnergyCube cube) {
                // エネルギー情報を表示
                BigEnergy stored = cube.getBuffer().getEnergy();
                BigEnergy max = cube.getBuffer().getMaxEnergy();
                double percentage = max.isZero() ? 0.0 : (stored.doubleValue() / max.doubleValue() * 100.0);

                player.displayClientMessage(
                        Component.literal(String.format("VE Energy Cube: %s / %s (%.2f%%)",
                                stored, max, percentage)),
                        false
                );
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityVEEnergyCube(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof BlockEntityVEEnergyCube cube) {
                BlockEntityVEEnergyCube.tick(lvl, pos, st, cube);
            }
        };
    }

    /**
     * キューブの最大容量を取得（メモリベース）
     */
    public static BigEnergy getMaxCapacity() {
        return VEMemoryManager.getMaxVECapacity();
    }
}