package com.minecolonies.core.blocks;

import com.minecolonies.api.blocks.interfaces.IMinecoloniesTickableBlock;
import com.minecolonies.core.tileentities.TileEntityCompostedDirt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.util.TriState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * Block that if activated with BoneMeal or Compost by an AI will produce flowers by intervals until it deactivates
 */
public class BlockCompostedDirt extends Block implements IMinecoloniesTickableBlock
{
    private final static VoxelShape SHAPE = Shapes.box(0, 0, 0, 1, 1, 1);

    public BlockCompostedDirt(final Properties properties)
    {
        super(properties);
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
    public TriState canSustainPlant(final BlockState state, final BlockGetter level, final BlockPos soilPosition, final Direction facing, final BlockState plant)
    {
        return TriState.TRUE;
    }
}
