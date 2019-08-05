package com.minecolonies.coremod.blocks;

import com.minecolonies.api.blocks.AbstractBlockMinecoloniesContainer;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.tileentities.TileEntityInfoPoster;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.DirectionProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.LivingEntityBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import static com.minecolonies.api.util.constant.Suppression.DEPRECATION;
import static net.minecraft.util.Direction.NORTH;
import static net.minecraft.util.Direction.fromAngle;

/**
 * Class for the minecolonies info Poster.
 */
public class BlockInfoPoster extends AbstractBlockMinecoloniesContainer<BlockInfoPoster>
{
    public static final DirectionProperty FACING = HorizontalBlock.FACING;

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
        super(Material.WOOD);
        initBlock();
    }

    /**
     * initialize the block
     * sets the creative tab, as well as the resistance and the hardness.
     */
    private void initBlock()
    {
        this.setDefaultState(this.blockState.getBaseState().with(FACING, NORTH));
        setRegistryName(Constants.MOD_ID.toLowerCase() + ":" + BLOCK_NAME);
        setTranslationKey(String.format("%s.%s", Constants.MOD_ID.toLowerCase(Locale.ENGLISH), BLOCK_NAME));
    }

    @Override
    public TileEntity createNewTileEntity(final World worldIn, final int meta)
    {
        return new TileEntityInfoPoster();
    }

    @Override
    @SuppressWarnings(DEPRECATION)
    public BlockState getStateFromMeta(final int meta)
    {
        Direction Direction = Direction.byIndex(meta);

        if (Direction.getAxis() == Direction.Axis.Y)
        {
            Direction = NORTH;
        }

        return this.getDefaultState().with(FACING, Direction);
    }

    @Override
    public int getMetaFromState(final BlockState state)
    {
        return state.get(FACING).getIndex();
    }

    @Override
    @SuppressWarnings(DEPRECATION)
    public BlockState withRotation(final BlockState state, final Rotation rot)
    {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    @Override
    @SuppressWarnings(DEPRECATION)
    public BlockState withMirror(final BlockState state, final Mirror mirrorIn)
    {
        return state.withRotation(mirrorIn.toRotation(state.get(FACING)));
    }

    @Override
    @SuppressWarnings(DEPRECATION)
    public boolean isFullCube(final BlockState state)
    {
        return false;
    }

    @Override
    public boolean isPassable(final IBlockAccess worldIn, final BlockPos pos)
    {
        return true;
    }

    @Override
    @SuppressWarnings(DEPRECATION)
    public boolean isOpaqueCube(final BlockState state)
    {
        return false;
    }

    @Override
    public void onBlockPlacedBy(final World worldIn, final BlockPos pos, final BlockState state, final LivingEntityBase placer, final ItemStack stack)
    {
        @NotNull final Direction Direction = (placer == null) ? NORTH : fromAngle(placer.rotationYaw);
        this.getDefaultState().with(FACING, Direction);
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }
}
