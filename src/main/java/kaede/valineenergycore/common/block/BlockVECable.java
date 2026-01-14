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
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * VEケーブルのブロック
 * 6方向への接続を持つ
 */
public class BlockVECable extends Block implements EntityBlock {

    // 各方向への接続プロパティ
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

    // ケーブルのTier
    private final VECableTier tier;

    // 中央の芯のサイズ
    private static final double CENTER_SIZE = 6.0; // 6/16 = 0.375
    private static final double CENTER_MIN = (16.0 - CENTER_SIZE) / 2.0;
    private static final double CENTER_MAX = CENTER_MIN + CENTER_SIZE;

    // 接続部分のサイズ
    private static final double CONNECTION_SIZE = 4.0; // 4/16 = 0.25
    private static final double CONNECTION_MIN = (16.0 - CONNECTION_SIZE) / 2.0;
    private static final double CONNECTION_MAX = CONNECTION_MIN + CONNECTION_SIZE;

    public BlockVECable(Properties properties, VECableTier tier) {
        super(properties);
        this.tier = tier;

        // デフォルトステート（全方向接続なし）
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
     * 隣接ブロックに基づいて接続状態を計算
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
     * 指定方向に接続可能かチェック
     */
    private boolean canConnectTo(BlockGetter level, BlockPos pos, Direction direction) {
        BlockPos adjacentPos = pos.relative(direction);
        BlockState adjacentState = level.getBlockState(adjacentPos);
        Block adjacentBlock = adjacentState.getBlock();

        // 同じVEケーブルには常に接続
        if (adjacentBlock instanceof BlockVECable) {
            return true;
        }

        // BlockEntityが VE Capability を持っているかチェック
        BlockEntity be = level.getBlockEntity(adjacentPos);
        if (be != null) {
            return hasVECapability(be, direction.getOpposite());
        }

        return false;
    }

    /**
     * BlockEntityがVE Capabilityを持っているかチェック
     */
    private boolean hasVECapability(BlockEntity be, Direction side) {
        // TODO: Capability実装後に修正
        return false;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos,
                                Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            // 隣接ブロックが変更されたら接続状態を更新
            BlockState newState = getConnectionState(level, pos);
            if (newState != state) {
                level.setBlock(pos, newState, 3);
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

    // ========== 当たり判定・描画 ==========

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                               CollisionContext context) {
        return getVoxelShape(state);
    }

    /**
     * 接続状態に基づいてVoxelShapeを生成
     */
    private VoxelShape getVoxelShape(BlockState state) {
        // 中央の芯
        VoxelShape shape = Block.box(CENTER_MIN, CENTER_MIN, CENTER_MIN,
                CENTER_MAX, CENTER_MAX, CENTER_MAX);

        // 各方向の接続
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

    // ========== BlockEntity ==========

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
                BlockEntityVECable.tick(level, pos, state, cable);
            }
        };
    }

    public VECableTier getTier() {
        return tier;
    }
}