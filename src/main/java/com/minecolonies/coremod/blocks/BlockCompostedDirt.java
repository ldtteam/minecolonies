package com.minecolonies.coremod.blocks;

import com.minecolonies.api.blocks.AbstractBlockMinecolonies;
import com.minecolonies.api.blocks.interfaces.IBlockMinecolonies;
import com.minecolonies.coremod.tileentities.TileEntityCompostedDirt;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.IPlantable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * Block that if activated with BoneMeal or Compost by an AI will produce flowers by intervals until it deactivates
 */
public class BlockCompostedDirt extends AbstractBlockMinecolonies<BlockCompostedDirt>
{
    private static final String BLOCK_NAME = "composted_dirt";
    private static final float BLOCK_HARDNESS = 5f;
    private static final float RESISTANCE = 1f;
    private final static VoxelShape SHAPE = VoxelShapes.create(0,0,0,1,1,1);

    /**
     * The constructor of the block.
     */
    public BlockCompostedDirt()
    {
        super(Properties.create(Material.EARTH).hardnessAndResistance(BLOCK_HARDNESS, RESISTANCE));
        setRegistryName(BLOCK_NAME);
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new TileEntityCompostedDirt();
    }

    @NotNull
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos, final ISelectionContext context)
    {
        return SHAPE;
    }

    @Override
    public boolean canSustainPlant(@NotNull final BlockState state, @NotNull final IBlockReader world, final BlockPos pos, @NotNull final Direction facing, final IPlantable plantable)
    {
        return true;
    }
}
