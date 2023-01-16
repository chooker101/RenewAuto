package net.fabricmc.renew_auto;

import net.minecraft.block.BlockState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.util.ActionResult;
import net.minecraft.world.BlockView;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.world.World;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.ItemScatterer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.entity.ai.pathing.NavigationType;
import org.jetbrains.annotations.Nullable;

public class CopperPipeBlock extends BlockWithEntity {
    public static final DirectionProperty FACING;
    public static final DirectionProperty CONNECTION;
    public static final BooleanProperty ENABLED;
    
    public CopperPipeBlock(Settings settings) {
		super(settings);
		setDefaultState(((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.UP)).with(CONNECTION, Direction.DOWN).with(ENABLED, true));
	}

    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CopperPipeBlockEntity(pos, state);
    }
 
	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext ctx) {
		Direction facing = state.get(FACING);
        Direction connection = state.get(CONNECTION);

		if((facing == Direction.NORTH && connection == Direction.SOUTH) || (facing == Direction.SOUTH && connection == Direction.NORTH)) {
            return VoxelShapes.union(VoxelShapes.union(Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 4.0D), Block.createCuboidShape(2.0D, 2.0D, 4.0D, 14.0D, 14.0D, 12.0D)), Block.createCuboidShape(0.0D, 0.0D, 12.0D, 16.0D, 16.0D, 16.0D));
        }
        if((facing == Direction.EAST && connection == Direction.WEST) || (facing == Direction.WEST && connection == Direction.EAST)) {
            return VoxelShapes.union(VoxelShapes.union(Block.createCuboidShape(0.0D, 0.0D, 0.0D, 4.0D, 16.0D, 16.0D), Block.createCuboidShape(4.0D, 2.0D, 2.0D, 12.0D, 14.0D, 14.0D)), Block.createCuboidShape(12.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D));
        }
        if((facing == Direction.UP && connection == Direction.DOWN) || (facing == Direction.DOWN && connection == Direction.UP)) {
            return VoxelShapes.union(VoxelShapes.union(Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D), Block.createCuboidShape(2.0D, 4.0D, 2.0D, 14.0D, 12.0D, 14.0D)), Block.createCuboidShape(0.0D, 12.0D, 0.0D, 16.0D, 16.0D, 16.0D));
        }
        if((facing == Direction.NORTH && connection == Direction.EAST) || facing == Direction.EAST && connection == Direction.NORTH) {
            return VoxelShapes.union(VoxelShapes.union(Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 4.0D), Block.createCuboidShape(2.0D, 2.0D, 2.0D, 14.0D, 14.0D, 14.0D)), Block.createCuboidShape(12.0D, 0.0D, 4.0D, 16.0D, 16.0D, 16.0D));
        }
        if((facing == Direction.NORTH && connection == Direction.WEST) || facing == Direction.WEST && connection == Direction.NORTH) {
            return VoxelShapes.union(VoxelShapes.union(Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 4.0D), Block.createCuboidShape(2.0D, 2.0D, 2.0D, 14.0D, 14.0D, 14.0D)), Block.createCuboidShape(0.0D, 0.0D, 4.0D, 4.0D, 16.0D, 16.0D));
        }
        if((facing == Direction.NORTH && connection == Direction.UP) || facing == Direction.UP && connection == Direction.NORTH) {
            return VoxelShapes.union(VoxelShapes.union(Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 4.0D), Block.createCuboidShape(2.0D, 2.0D, 2.0D, 14.0D, 14.0D, 14.0D)), Block.createCuboidShape(0.0D, 12.0D, 4.0D, 16.0D, 16.0D, 16.0D));
        }
        if((facing == Direction.NORTH && connection == Direction.DOWN) || facing == Direction.DOWN && connection == Direction.NORTH) {
            return VoxelShapes.union(VoxelShapes.union(Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 4.0D), Block.createCuboidShape(2.0D, 2.0D, 2.0D, 14.0D, 14.0D, 14.0D)), Block.createCuboidShape(0.0D, 0.0D, 4.0D, 16.0D, 4.0D, 16.0D));
        }
        if((facing == Direction.SOUTH && connection == Direction.EAST) || facing == Direction.EAST && connection == Direction.SOUTH) {
            return VoxelShapes.union(VoxelShapes.union(Block.createCuboidShape(0.0D, 0.0D, 12.0D, 16.0D, 16.0D, 16.0D), Block.createCuboidShape(2.0D, 2.0D, 2.0D, 14.0D, 14.0D, 14.0D)), Block.createCuboidShape(12.0D, 0.0D, 0.0D, 16.0D, 16.0D, 12.0D));
        }
        if((facing == Direction.SOUTH && connection == Direction.WEST) || facing == Direction.WEST && connection == Direction.SOUTH) {
            return VoxelShapes.union(VoxelShapes.union(Block.createCuboidShape(0.0D, 0.0D, 12.0D, 16.0D, 16.0D, 16.0D), Block.createCuboidShape(2.0D, 2.0D, 2.0D, 14.0D, 14.0D, 14.0D)), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 4.0D, 16.0D, 12.0D));
        }
        if((facing == Direction.SOUTH && connection == Direction.UP) || facing == Direction.UP && connection == Direction.SOUTH) {
            return VoxelShapes.union(VoxelShapes.union(Block.createCuboidShape(0.0D, 0.0D, 12.0D, 16.0D, 16.0D, 16.0D), Block.createCuboidShape(2.0D, 2.0D, 2.0D, 14.0D, 14.0D, 14.0D)), Block.createCuboidShape(0.0D, 12.0D, 0.0D, 16.0D, 16.0D, 12.0D));
        }
        if((facing == Direction.SOUTH && connection == Direction.DOWN) || facing == Direction.DOWN && connection == Direction.SOUTH) {
            return VoxelShapes.union(VoxelShapes.union(Block.createCuboidShape(0.0D, 0.0D, 12.0D, 16.0D, 16.0D, 16.0D), Block.createCuboidShape(2.0D, 2.0D, 2.0D, 14.0D, 14.0D, 14.0D)), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 12.0D));
        }
        if((facing == Direction.DOWN && connection == Direction.WEST) || facing == Direction.WEST && connection == Direction.DOWN) {
            return VoxelShapes.union(VoxelShapes.union(Block.createCuboidShape(0.0D, 0.0D, 0.0D, 4.0D, 16.0D, 16.0D), Block.createCuboidShape(2.0D, 2.0D, 2.0D, 14.0D, 14.0D, 14.0D)), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D));
        }
        if((facing == Direction.DOWN && connection == Direction.EAST) || facing == Direction.EAST && connection == Direction.DOWN) {
            return VoxelShapes.union(VoxelShapes.union(Block.createCuboidShape(12.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D), Block.createCuboidShape(2.0D, 2.0D, 2.0D, 14.0D, 14.0D, 14.0D)), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D));
        }
        if((facing == Direction.UP && connection == Direction.WEST) || facing == Direction.WEST && connection == Direction.UP) {
            return VoxelShapes.union(VoxelShapes.union(Block.createCuboidShape(0.0D, 0.0D, 0.0D, 4.0D, 16.0D, 16.0D), Block.createCuboidShape(2.0D, 2.0D, 2.0D, 14.0D, 14.0D, 14.0D)), Block.createCuboidShape(0.0D, 12.0D, 0.0D, 16.0D, 16.0D, 16.0D));
        }
        if((facing == Direction.UP && connection == Direction.EAST) || facing == Direction.EAST && connection == Direction.UP) {
            return VoxelShapes.union(VoxelShapes.union(Block.createCuboidShape(12.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D), Block.createCuboidShape(2.0D, 2.0D, 2.0D, 14.0D, 14.0D, 14.0D)), Block.createCuboidShape(0.0D, 12.0D, 0.0D, 16.0D, 16.0D, 16.0D));
        }

        return VoxelShapes.fullCube();
	}
 
    @Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState blockState = (BlockState)this.getDefaultState().with(FACING, ctx.getPlayerLookDirection().getOpposite());
		return (BlockState)blockState.with(CONNECTION, getConnectionDirection(blockState, ctx.getWorld(), ctx.getBlockPos()));
	}

    @Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, CONNECTION, ENABLED);
	}

    public static boolean isPipeable(BlockState state) {
        return state.getBlock() instanceof CopperPipeBlock;
    }

    private static Direction getConnectionDirection(BlockState state, BlockView world, BlockPos pos) {
        Direction facing = (Direction)state.get(FACING);
        BlockState other = world.getBlockState(pos.offset(facing));
        Direction direction2;
        if(pos.up() != pos.offset(facing)) {
            other = world.getBlockState(pos.up());
            if(isPipeable(other)) {
                direction2 = (Direction)other.get(FACING);
                if(direction2.getAxis() != ((Direction)state.get(FACING)).getAxis()) {
                    return Direction.UP;
                }
            }
        }
        if(pos.down() != pos.offset(facing)) {
            other = world.getBlockState(pos.down());
            if(isPipeable(other)) {
                direction2 = (Direction)other.get(FACING);
                if(direction2.getAxis() != ((Direction)state.get(FACING)).getAxis()) {
                    return Direction.DOWN;
                }
            }
        }
        if(pos.north() != pos.offset(facing)) {
            other = world.getBlockState(pos.north());
            if(isPipeable(other)) {
                direction2 = (Direction)other.get(FACING);
                if(direction2.getAxis() != ((Direction)state.get(FACING)).getAxis()) {
                    return Direction.NORTH;
                }
            }
        }
        if(pos.east() != pos.offset(facing)) {
            other = world.getBlockState(pos.east());
            if(isPipeable(other)) {
                direction2 = (Direction)other.get(FACING);
                if(direction2.getAxis() != ((Direction)state.get(FACING)).getAxis()) {
                    return Direction.EAST;
                }
            }
        }
        if(pos.south() != pos.offset(facing)) {
            other = world.getBlockState(pos.south());
            if(isPipeable(other)) {
                direction2 = (Direction)other.get(FACING);
                if(direction2.getAxis() != ((Direction)state.get(FACING)).getAxis()) {
                    return Direction.SOUTH;
                }
            }
        }
        if(pos.west() != pos.offset(facing)) {
            other = world.getBlockState(pos.west());
            if(isPipeable(other)) {
                direction2 = (Direction)other.get(FACING);
                if(direction2.getAxis() != ((Direction)state.get(FACING)).getAxis()) {
                    return Direction.WEST;
                }
            }
        }

        return facing.getOpposite();
    }

    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
           return ActionResult.SUCCESS;
        } else {
           BlockEntity blockEntity = world.getBlockEntity(pos);
           if (blockEntity instanceof CopperPipeBlockEntity) {
              player.openHandledScreen((CopperPipeBlockEntity)blockEntity);
           }
  
           return ActionResult.CONSUME;
        }
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
       return world.isClient ? null : checkType(type, RenewAutoInitialize.COPPER_PIPE_BLOCK_ENTITY, CopperPipeBlockEntity::serverTick);
    }

    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
       BlockEntity blockEntity = world.getBlockEntity(pos);
       return blockEntity instanceof NamedScreenHandlerFactory ? (NamedScreenHandlerFactory)blockEntity : null;
    }

    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
     }

    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }
    
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
       return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
    }

    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!oldState.isOf(state.getBlock())) {
           this.updateEnabled(world, pos, state);
        }
    }

    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        this.updateEnabled(world, pos, state);
    }

    private void updateEnabled(World world, BlockPos pos, BlockState state) {
        boolean bl = !world.isReceivingRedstonePower(pos);
        if (bl != (Boolean)state.get(ENABLED)) {
           world.setBlockState(pos, (BlockState)state.with(ENABLED, bl), Block.NO_REDRAW);
        }
  
    }

    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
           BlockEntity blockEntity = world.getBlockEntity(pos);
           if (blockEntity instanceof CopperPipeBlockEntity) {
               ItemScatterer.spawn(world, pos, (CopperPipeBlockEntity)blockEntity);
               world.updateComparators(pos,this);
           }
        }
    }

    static {
        FACING = Properties.FACING;
        CONNECTION = DirectionProperty.of("connection", Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN);
        ENABLED = Properties.ENABLED;
    }
}
