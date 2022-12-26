package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules;

import com.minecolonies.coremod.colony.buildings.workerbuildings.fields.PlantationField;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.PlantationModule;
import com.minecolonies.coremod.entity.ai.citizen.planter.EntityAIWorkPlanter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

public class VerticalGrowingPlantModule extends PlantationModule
{
    private final int minimumPlantLength;

    public VerticalGrowingPlantModule(final String fieldTag, final String workTag, final Block block, final int maxPlants, final int plantsToRequest, final int minimumPlantLength)
    {
        super(fieldTag, workTag, block, maxPlants, plantsToRequest);
        this.minimumPlantLength = minimumPlantLength;
    }

    @Override
    @NotNull
    public PlantationModule.PlanterAIModuleResult workField(PlantationField field, final EntityAIWorkPlanter planterAI, BlockPos workPosition)
    {
        if (planterAI.planterWalkToBlock(workPosition))
        {
            return PlanterAIModuleResult.MOVING;
        }

        PlanterAIModuleResult action = decidedWorkAction(field, workPosition);
        return switch (action)
                 {
                     case NONE -> PlanterAIModuleResult.NONE;
                     case HARVESTING ->
                         // Tell the AI to mine a block, if we're harvesting we need to mine 1 block off the ground (2 high).
                       getHarvestingResultFromMiningResult(planterAI.planterMineBlock(workPosition.above(2), true));
                     case PLANTING ->
                     {
                         if (planterAI.planterPlaceBlock(workPosition.above(), getItem(), getPlantsToRequest()))
                         {
                             yield PlanterAIModuleResult.PLANTED;
                         }
                         yield PlanterAIModuleResult.REQUIRES_ITEMS;
                     }
                     case CLEARING ->
                         // Tell the AI to mine a block, if we're clearing an obstacle we need to clear the item at the position directly above (1 high).
                       getClearingResultFromMiningResult(planterAI.planterMineBlock(workPosition.above(1), false));
                     default -> PlanterAIModuleResult.INVALID;
                 };
    }

    /**
     * Responsible for deciding what action the AI is going to perform on a specific field position
     * depending on the state of the working position.
     *
     * @param field            the field to check for.
     * @param plantingPosition the specific position to check for.
     * @return the {@link PlanterAIModuleResult} that the AI is going to perform.
     */
    private PlanterAIModuleResult decidedWorkAction(PlantationField field, BlockPos plantingPosition)
    {
        if (isPositionEmpty(field, plantingPosition))
        {
            return PlanterAIModuleResult.PLANTING;
        }

        if (isPositionBlocked(field, plantingPosition))
        {
            return PlanterAIModuleResult.CLEARING;
        }

        if (hasPositionReachedHeight(field, plantingPosition))
        {
            return PlanterAIModuleResult.HARVESTING;
        }

        return PlanterAIModuleResult.NONE;
    }

    /**
     * Responsible for checking if the planting position is empty ({@link Blocks#AIR}).
     *
     * @param field            the field to check for.
     * @param plantingPosition the specific position to check for.
     * @return whether the planting position is empty.
     */
    private boolean isPositionEmpty(PlantationField field, BlockPos plantingPosition)
    {
        return field.getColony().getWorld().getBlockState(plantingPosition.above()).getBlock() == Blocks.AIR;
    }

    /**
     * Responsible for checking if the planting position is blocked by a foreign block which does not belong there.
     * This includes every block except the required block, this also include {@link Blocks#AIR}, however this condition
     * should be previously checked through {@link VerticalGrowingPlantModule#isPositionEmpty}.
     *
     * @param field            the field to check for.
     * @param plantingPosition the specific position to check for.
     * @return whether the planting position is blocked by a foreign block.
     */
    private boolean isPositionBlocked(final PlantationField field, BlockPos plantingPosition)
    {
        return field.getColony().getWorld().getBlockState(plantingPosition.above()).getBlock() != getBlock();
    }

    /**
     * Responsible for checking if the plant has reached the minimum plant height.
     * Only checks the block which is {@link VerticalGrowingPlantModule#minimumPlantLength} above the working position
     * because the assumption is made this is a continuous growing plant (which cannot have holes, like sugar cane which breaks completely if one block is removed).
     *
     * @param field            the field to check for.
     * @param plantingPosition the specific position to check for.
     * @return whether the plant has reached the minimum harvesting height.
     */
    private boolean hasPositionReachedHeight(final PlantationField field, BlockPos plantingPosition)
    {
        return field.getColony().getWorld().getBlockState(plantingPosition.above(minimumPlantLength)).getBlock() == getBlock();
    }

    @Override
    public BlockPos getNextWorkingPosition(PlantationField field)
    {
        for (BlockPos position : field.getWorkingPositions())
        {
            if (decidedWorkAction(field, position) != PlanterAIModuleResult.NONE)
            {
                return position;
            }
        }

        return null;
    }
}
