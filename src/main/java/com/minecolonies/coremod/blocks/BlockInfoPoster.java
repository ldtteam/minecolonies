package com.minecolonies.coremod.blocks;

import com.minecolonies.api.blocks.AbstractBlockMinecoloniesContainer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import static com.minecolonies.api.util.constant.Suppression.DEPRECATION;
import static net.minecraft.util.Direction.NORTH;
import static net.minecraft.util.Direction.fromAngle;

/**
 * Class for the minecolonies info Poster.
 */
public class BlockInfoPoster extends AbstractBlockMinecoloniesContainer<BlockInfoPoster>
{
    public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;

    /**
     * This blocks name.
     */
    private static final String BLOCK_NAME = "blockInfoPoster";

    /**
     * Constructor for the Substitution block.
     * sets the creative tab, as well as the resistance and the hardness.
     */
    public BlockInfoPoster()
    {
        super(Properties.create(Material.WOOD).doesNotBlockMovement());
        this.setDefaultState(this.getDefaultState().with(FACING, NORTH));
        setRegistryName(BLOCK_NAME);

    }

    @Override
    @SuppressWarnings(DEPRECATION)
    public BlockState rotate(final BlockState state, final Rotation rot)
    {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    @Override
    @SuppressWarnings(DEPRECATION)
    public BlockState mirror(final BlockState state, final Mirror mirrorIn)
    {
        return state.rotate(mirrorIn.toRotation(state.get(FACING)));
    }

    @Override
    public boolean doesSideBlockRendering(final BlockState state, final IEnviromentBlockReader world, final BlockPos pos, final Direction face)
    {
        return false;
    }

    @NotNull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public void onBlockPlacedBy(final World worldIn, final BlockPos pos, final BlockState state, @Nullable final LivingEntity placer, final ItemStack stack)
    {
        @NotNull final Direction Direction = (placer == null) ? NORTH : fromAngle(placer.rotationYaw);
        this.getDefaultState().with(FACING, Direction);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }
}
