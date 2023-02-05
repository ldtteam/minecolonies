package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.specific;

import com.minecolonies.coremod.colony.buildings.workerbuildings.fields.PlantationField;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.generic.TreeSidePlantModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;
import java.util.stream.Stream;

import static com.minecolonies.api.research.util.ResearchConstants.PLANTATION_JUNGLE;

/**
 * Planter module for growing {@link Items#COCOA_BEANS}.
 * <br/>
 * Requirements:
 * <ol>
 *     <li>All requirements from {@link TreeSidePlantModule}</li>
 * </ol>
 */
public class CocoaPlantModule extends TreeSidePlantModule
{
    /**
     * Default constructor.
     */
    public CocoaPlantModule()
    {
        super("cocoa_field", "cocoa", Items.COCOA_BEANS);
    }

    @Override
    public ResourceLocation getRequiredResearchEffect()
    {
        return PLANTATION_JUNGLE;
    }

    @Override
    protected BlockState generatePlantingBlockState(final PlantationField field, final BlockPos workPosition, final BlockState blockState)
    {
        return Stream.of(workPosition.north(), workPosition.south(), workPosition.west(), workPosition.east())
                 .filter(position -> field.getColony().getWorld().getBlockState(position).getBlock() == Blocks.JUNGLE_LOG)
                 .map(position -> Optional.ofNullable(Direction.fromNormal(position.subtract(workPosition))).orElse(Direction.NORTH))
                 .findFirst()
                 .map(direction -> blockState.setValue(HorizontalDirectionalBlock.FACING, direction))
                 .orElse(blockState);
    }

    @Override
    protected boolean isValidClearingBlock(final BlockState blockState)
    {
        return blockState.getBlock() != Blocks.COCOA;
    }

    @Override
    protected boolean isValidHarvestBlock(final BlockState blockState)
    {
        Block block = blockState.getBlock();
        if (block instanceof CocoaBlock cocoa)
        {
            return !cocoa.isRandomlyTicking(blockState);
        }
        return false;
    }
}
