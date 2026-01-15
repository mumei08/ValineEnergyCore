package kaede.valineenergycore.common.block;

import kaede.valineenergycore.common.content.network.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * VEケーブルのブロック - Capability完全対応版
 */
public class BlockVECable extends Block implements EntityBlock {

    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");

    private static final Map<Direction, BooleanProperty> PROPERTY_MAP = new HashMap<>();

    static {
        PROPERTY_MAP.put(Direction.NORTH, NORTH);
        PROPERTY_MAP.put(Direction.SOUTH, SOUTH);
        PROPERTY_MAP.put(Direction.EAST, EAST);
        PROPERTY_MAP.put(Direction.WEST, WEST);
        PROPERTY_MAP.put(Direction.UP, UP);
        PROPERTY_MAP.put(Direction.DOWN, DOWN);
    }

    private final VECableTier tier;

    private static final double CENTER_SIZE = 6.0;
    private static final double CENTER_MIN = (16.0 - CENTER_SIZE) / 2.0;
    private static final double CENTER_MAX = CENTER_MIN + CENTER_SIZE;

    private static final double CONNECTION_SIZE = 4.0;
    private static final double CONNECTION_MIN = (16.0 - CONNECTION_SIZE) / 2.0;
    private static final double CONNECTION_MAX = CONNECTION_MIN + CONNECTION_SIZE;

    public BlockVECable(Properties properties, VECableTier tier) {
        super(properties);
        this.tier = tier;

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(EAST, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return getConnectionState(context.getLevel(), context.getClickedPos());
    }

    /**
     * 隣接ブロックに基づいて接続状態を計算 - Capability対応
     */
    private BlockState getConnectionState(BlockGetter level, BlockPos pos) {
        BlockState state = this.defaultBlockState();

        for (Direction direction : Direction.values()) {
            boolean shouldConnect = canConnectTo(level, pos, direction);
            state = state.setValue(PROPERTY_MAP.get(direction), shouldConnect);
        }

        return state;
    }

    /**
     * 指定方向に接続可能かチェック - Capability対応
     */
    private boolean canConnectTo(BlockGetter level, BlockPos pos, Direction direction) {
        BlockPos adjacentPos = pos.relative(direction);
        BlockState adjacentState = level.getBlockState(adjacentPos);
        Block adjacentBlock = adjacentState.getBlock();

        // 同じVEケーブルには常に接続
        if (adjacentBlock instanceof BlockVECable) {
            return true;
        }

        // BlockEntityが Forge Energy Capability を持っているかチェック
        if (level instanceof Level world) {
            BlockEntity be = world.getBlockEntity(adjacentPos);
            if (be != null) {
                return hasEnergyCapability(be, direction.getOpposite());
            }
        }

        return false;
    }

    /**
     * BlockEntityがForge Energy Capabilityを持っているかチェック
     */
    private boolean hasEnergyCapability(BlockEntity be, Direction side) {
        return be.getCapability(ForgeCapabilities.ENERGY, side).isPresent();
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos,
                                Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            BlockState newState = getConnectionState(level, pos);
            if (newState != state) {
                level.setBlock(pos, newState, 3);
            }

            // ネットワークのAcceptorを更新
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BlockEntityVECable cableBE) {
                VENetwork network = cableBE.getTransmitter().getNetwork();
                if (network != null) {
                    network.updateAcceptors();
                }
            }
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos,
                        BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);

        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BlockEntityVECable cableBE) {
                cableBE.getTransmitter().onPlace();
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BlockEntityVECable cableBE) {
                cableBE.getTransmitter().onRemove();
            }
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                               CollisionContext context) {
        return getVoxelShape(state);
    }

    private VoxelShape getVoxelShape(BlockState state) {
        VoxelShape shape = Block.box(CENTER_MIN, CENTER_MIN, CENTER_MIN,
                CENTER_MAX, CENTER_MAX, CENTER_MAX);

        if (state.getValue(NORTH)) {
            shape = Shapes.or(shape, Block.box(CONNECTION_MIN, CONNECTION_MIN, 0,
                    CONNECTION_MAX, CONNECTION_MAX, CENTER_MIN));
        }
        if (state.getValue(SOUTH)) {
            shape = Shapes.or(shape, Block.box(CONNECTION_MIN, CONNECTION_MIN, CENTER_MAX,
                    CONNECTION_MAX, CONNECTION_MAX, 16));
        }
        if (state.getValue(WEST)) {
            shape = Shapes.or(shape, Block.box(0, CONNECTION_MIN, CONNECTION_MIN,
                    CENTER_MIN, CONNECTION_MAX, CONNECTION_MAX));
        }
        if (state.getValue(EAST)) {
            shape = Shapes.or(shape, Block.box(CENTER_MAX, CONNECTION_MIN, CONNECTION_MIN,
                    16, CONNECTION_MAX, CONNECTION_MAX));
        }
        if (state.getValue(DOWN)) {
            shape = Shapes.or(shape, Block.box(CONNECTION_MIN, 0, CONNECTION_MIN,
                    CONNECTION_MAX, CENTER_MIN, CONNECTION_MAX));
        }
        if (state.getValue(UP)) {
            shape = Shapes.or(shape, Block.box(CONNECTION_MIN, CENTER_MAX, CONNECTION_MIN,
                    CONNECTION_MAX, 16, CONNECTION_MAX));
        }

        return shape;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityVECable(pos, state, tier);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof BlockEntityVECable cable) {
                BlockEntityVECable.tick(lvl, pos, st, cable);
            }
        };
    }

    public VECableTier getTier() {
        return tier;
    }
}