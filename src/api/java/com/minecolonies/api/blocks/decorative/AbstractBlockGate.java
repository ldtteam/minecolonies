package com.minecolonies.api.blocks.decorative;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.state.properties.BlockStateProperties.WATERLOGGED;

/**
 * Block class for gates, which expand and retract and act as one big door
 */
public abstract class AbstractBlockGate extends DoorBlock
{
    /**
     * Variant reg names
     */
    public static final String IRON_GATE   = "gate_iron";
    public static final String WOODEN_GATE = "gate_wood";

    /**
     * Max X gate size
     */
    private final int maxWidth;

    /**
     * Max Y gate size
     */
    private final int maxHeight;

    private final float hardness;

    /**
     * The bounding boxes.
     */
    protected static final VoxelShape E_W_SHAPE = VoxelShapes.create(0.3D, 0.0D, 0.0D, 0.7D, 1.0D, 1.0D);
    protected static final VoxelShape N_S_SHAPE = VoxelShapes.create(0.0D, 0.0D, 0.3D, 1.0D, 1.0D, 0.7D);

    public AbstractBlockGate(final String name, final float hardness, final int maxWidth, final int maxHeight)
    {
        super(Properties.create(Material.WOOD).hardnessAndResistance(hardness, hardness * 5).notSolid());
        this.setRegistryName(name);
        setDefaultState(getDefaultState());

        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.hardness = hardness;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
    {
        if (this.material == Material.IRON)
        {
            return ActionResultType.PASS;
        }
        else
        {
            toggleGate(worldIn, pos, state.get(FACING).rotateY());
            worldIn.playEvent(player, state.get(OPEN) ? 1005 : 1011, pos, 0);
            return ActionResultType.SUCCESS;
        }
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player)
    {
        int count = removeGate(worldIn, pos, state.get(FACING).rotateY());
        for (int i = 0; i < count; i++)
        {
            Block.spawnDrops(state, worldIn, pos, null, player, player.getHeldItemMainhand());
        }
    }

    /**
     * Removes the whole gate blocks
     *
     * @param world    world to use
     * @param startPos start pos
     * @param facing   gate facing
     * @return amount of removed blocks
     */
    public int removeGate(final World world, final BlockPos startPos, final Direction facing)
    {
        final BlockPos lowerLeftCorner = findLowerLeftCorner(world, facing, startPos);
        int amount = 0;
        // Remove gate
        for (int hor = 0; hor < maxWidth; hor++)
        {
            final BlockPos current = lowerLeftCorner.offset(facing, hor);
            if (world.getBlockState(current).getBlock() != this)
            {
                break;
            }

            for (int vert = 0; vert < maxHeight; vert++)
            {
                final BlockPos currentPos = current.up(vert);
                if (world.getBlockState(currentPos).getBlock() == this)
                {
                    amount++;
                    world.setBlockState(currentPos, Blocks.AIR.getDefaultState(), 35);
                }
                else
                {
                    break;
                }
            }
        }

        return amount;
    }

    @Deprecated
    public float getBlockHardness(BlockState blockState, IBlockReader worldIn, BlockPos pos)
    {
        // Combined hardness
        final Direction facing = blockState.get(FACING).rotateY();
        final BlockPos start = findLowerLeftCorner(worldIn, facing, pos);
        int count = 0;

        for (int hor = 0; hor < maxWidth; hor++)
        {
            final BlockPos hPos = start.offset(facing, hor);
            if (worldIn.getBlockState(hPos).getBlock() != this)
            {
                break;
            }

            for (int vert = 0; vert < maxHeight; vert++)
            {
                final BlockPos worldPos = hPos.add(0, vert, 0);

                final BlockState state = worldIn.getBlockState(worldPos);
                if (state.getBlock() != this)
                {
                    break;
                }
                else
                {
                    count++;
                }
            }
        }

        return count * hardness;
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos)
    {
        return stateIn;
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos)
    {
        // Don't allow next to any existing gate
        BlockPos tPos = pos.add(-1, -1, -1);
        for (int x = 0; x < 3; x++)
        {
            for (int y = 0; y < 3; y++)
            {
                for (int z = 0; z < 3; z++)
                {
                    if (worldIn.getBlockState(tPos.add(x, y, z)).getBlock() == this)
                    {
                        return false;
                    }
                }
            }
        }

        final BlockPos blockpos = pos.down();
        final BlockState blockstate = worldIn.getBlockState(blockpos);
        return blockstate.isSolidSide(worldIn, blockpos, Direction.UP);
    }

    @Override
    public void onBlockPlacedBy(@NotNull final World worldIn, @NotNull final BlockPos pos, final BlockState state, final LivingEntity placer, final ItemStack stack)
    {
        // Fills the rest of the gate upon placement
        final Direction facing = state.get(FACING).rotateY();

        // pos
        fillYStates(worldIn, pos, state, stack);

        // Left of pos
        int canPlace = maxWidth - 1;
        for (int hor = 1; hor < maxWidth; hor++)
        {
            final BlockPos checkPos = pos.offset(facing.getOpposite(), hor);
            final BlockState checkState = worldIn.getBlockState(checkPos);
            if (checkState.isAir(worldIn, checkPos) && canPlace > 0 && worldIn.getBlockState(checkPos.offset(facing.getOpposite())).getBlock() != this)
            {
                if (stack.getCount() > 1)
                {
                    stack.setCount(stack.getCount() - 1);
                    worldIn.setBlockState(checkPos, state);
                }
                fillYStates(worldIn, checkPos, state, stack);
                canPlace--;
            }
            else
            {
                break;
            }
        }

        if (canPlace <= 0)
        {
            return;
        }

        // Right of pos
        for (int hor = 1; hor < maxWidth; hor++)
        {
            final BlockPos checkPos = pos.offset(facing, hor);
            final BlockState checkState = worldIn.getBlockState(checkPos);
            if (checkState.getBlock() != this && checkState.isAir(worldIn, checkPos) && canPlace > 0
                  && worldIn.getBlockState(checkPos.offset(facing)).getBlock() != this)
            {
                if (stack.getCount() > 1)
                {
                    stack.setCount(stack.getCount() - 1);
                    worldIn.setBlockState(checkPos, state);
                }
                fillYStates(worldIn, checkPos, state, stack);
                canPlace--;
            }
            else
            {
                break;
            }
        }
    }

    /**
     * Fills gate blocks up to max Y or obstacle
     *
     * @param world world to use
     * @param base  base block we start from
     * @param state state to put
     */
    private void fillYStates(final World world, final BlockPos base, final BlockState state, final ItemStack stack)
    {
        for (int vert = 1; vert < maxHeight; vert++)
        {
            final BlockPos checkPos = base.add(0, vert, 0);
            final BlockState checkState = world.getBlockState(checkPos);
            if (checkState.isAir(world, checkPos) && world.getBlockState(checkPos.up()).getBlock() != this)
            {
                if (stack.getCount() > 1)
                {
                    stack.setCount(stack.getCount() - 1);
                    world.setBlockState(checkPos, state);
                }
            }
            else
            {
                break;
            }
        }
    }

    /**
     * Register the block type to the registry
     *
     * @param registry registry to register to
     * @return block
     */
    public AbstractBlockGate registerBlock(final IForgeRegistry<Block> registry)
    {
        registry.register(this);
        return this;
    }

    @NotNull
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
    {
        return getShapeForState(state);
    }

    /**
     * Gets the right rotated shape
     *
     * @param state state to check
     * @return shape
     */
    private VoxelShape getShapeForState(final BlockState state)
    {
        final Direction direction = state.get(FACING);
        switch (direction)
        {
            case EAST:
            case WEST:
                return E_W_SHAPE;
            case SOUTH:
            case NORTH:
            default:
                return N_S_SHAPE;
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
    {
        if (state.get(OPEN))
        {
            return VoxelShapes.empty();
        }
        return getShapeForState(state);
    }

    @Override
    public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos)
    {
        return getShapeForState(state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state)
    {
        if (state.get(OPEN))
        {
            return BlockRenderType.INVISIBLE;
        }
        else
        {
            return BlockRenderType.MODEL;
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(HALF, FACING, OPEN, HINGE, POWERED, WATERLOGGED);
    }

    /**
     * Finds the bottom left corner
     *
     * @param facing   direction to check in
     * @param blockPos start pos
     * @return bottom left corner pos
     */
    private BlockPos findLowerLeftCorner(final IBlockReader world, final Direction facing, final BlockPos blockPos)
    {
        BlockPos tePos = blockPos;

        for (int vert = 0; vert < maxHeight; vert++)
        {
            final BlockPos tempPos = tePos.add(0, -vert, 0);

            if (world.getBlockState(tempPos.down()).getBlock() != this)
            {
                tePos = tempPos;
                break;
            }
        }

        for (int hor = 0; hor < maxWidth; hor++)
        {

            if (world.getBlockState(tePos.offset(facing.getOpposite(), hor + 1)).getBlock() != this)
            {
                tePos = tePos.offset(facing.getOpposite(), hor);
                break;
            }
        }

        return tePos;
    }

    @Override
    public void toggleDoor(World worldIn, BlockPos pos, boolean open)
    {
        BlockState blockstate = worldIn.getBlockState(pos);
        if (blockstate.getBlock() == this && blockstate.get(OPEN) != open)
        {
            toggleGate(worldIn, pos, blockstate.get(FACING).rotateY());
        }
    }

    /**
     * Used for activating the gate.
     *
     * @param world        world to use
     * @param clickedBlock block thats clicked/used
     * @param facing       facing to check
     */
    public void toggleGate(final World world, final BlockPos clickedBlock, final Direction facing)
    {
        final BlockPos lowerLeftCorner = findLowerLeftCorner(world, facing, clickedBlock);

        for (int hor = 0; hor < maxWidth; hor++)
        {
            final BlockPos hPos = lowerLeftCorner.offset(facing, hor);
            if (world.getBlockState(hPos).getBlock() != this)
            {
                break;
            }

            for (int vert = 0; vert < maxHeight; vert++)
            {
                final BlockPos worldPos = hPos.add(0, vert, 0);

                final BlockState state = world.getBlockState(worldPos);
                if (state.getBlock() != this)
                {
                    break;
                }

                // Set top blocks to spikes
                if (world.getBlockState(worldPos.up()).getBlock() != this)
                {
                    world.setBlockState(worldPos, state.cycle(DoorBlock.HINGE), 2);
                }
                else
                {
                    world.setBlockState(worldPos, state.cycle(BlockStateProperties.OPEN), 2);
                }
            }
        }
    }

    /**
     * Mostly redstone stuff for opening
     */
    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving)
    {
        boolean powered = worldIn.isBlockPowered(pos);
        if (powered != state.get(OPEN))
        {
            toggleDoor(worldIn, pos, powered);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        BlockPos blockpos = context.getPos();

        if (blockpos.getY() < 255 && context.getWorld().getBlockState(blockpos.up()).isReplaceable(context))
        {
            return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing());
        }
        else
        {
            return null;
        }
    }
}
