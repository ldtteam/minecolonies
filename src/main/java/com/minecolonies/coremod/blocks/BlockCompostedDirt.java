package com.minecolonies.coremod.blocks;

import com.minecolonies.api.blocks.AbstractBlockMinecolonies;
import com.minecolonies.api.blocks.interfaces.ITickableBlockMinecolonies;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.tileentities.TileEntityCompostedDirt;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraftforge.common.IPlantable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

/**
 * Block that if activated with BoneMeal or Compost by an AI will produce flowers by intervals until it deactivates
 */
public class BlockCompostedDirt extends AbstractBlockMinecolonies<BlockCompostedDirt> implements ITickableBlockMinecolonies
{
    private static final String     BLOCK_NAME     = "composted_dirt";
    private static final float      BLOCK_HARDNESS = 5f;
    private static final float      RESISTANCE     = 1f;
    private final static VoxelShape SHAPE          = Shapes.box(0, 0, 0, 1, 1, 1);

    /**
     * The constructor of the block.
     */
    public BlockCompostedDirt()
    {
        super(Properties.of().mapColor(MapColor.DIRT).sound(SoundType.ROOTED_DIRT).strength(BLOCK_HARDNESS, RESISTANCE).sound(SoundType.GRAVEL));
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return new ResourceLocation(Constants.MOD_ID, BLOCK_NAME);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull final BlockPos blockPos, @NotNull final BlockState blockState)
    {
        return new TileEntityCompostedDirt(blockPos, blockState);
    }

    @NotNull
    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos, final CollisionContext context)
    {
        return SHAPE;
    }

    @Override
    public boolean canSustainPlant(
      @NotNull final BlockState state,
      @NotNull final BlockGetter world,
      final BlockPos pos,
      @NotNull final Direction facing,
      final IPlantable plantable)
    {
        return true;
    }
}
