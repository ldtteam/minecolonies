package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.colony.buildings.workerbuildings.fields.PlantationField;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.PlantationModule;
import com.minecolonies.coremod.entity.ai.citizen.planter.EntityAIWorkPlanter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Plantation module for plants that grow vertically upwards, similar to sugar cane.
 * For a plant to fit this module it needs the following requirements.
 * - Grow vertically without any outcroppings.
 * - Grow uninterrupted (no gaps in between the plant).
 * - Must break all blocks above when a lower block is destroyed.
 */
public class VerticalUpwardsGrowingPlantModule extends PlantationModule
{
    /**
     * The minimum length this plant should grow to before considered harvestable.
     */
    private final int minimumPlantLength;

    /**
     * Default constructor.
     *
     * @param fieldTag               the tag of the field anchor block.
     * @param workTag                the tag of the working positions.
     * @param block                  the block which is harvested.
     * @param maxPlants              the maximum allowed plants.
     * @param plantsToRequest        the amount of plants to request when the planter has none left.
     * @param requiredResearchEffect the research effect required before this field type can be used.
     * @param minimumPlantLength     the minimum length for this plant to grow to before it can be harvested.
     */
    private VerticalUpwardsGrowingPlantModule(
      final String fieldTag,
      final String workTag,
      final Block block,
      final int maxPlants,
      final int plantsToRequest,
      final ResourceLocation requiredResearchEffect,
      final int minimumPlantLength)
    {
        super(fieldTag, workTag, block, maxPlants, plantsToRequest, requiredResearchEffect);
        this.minimumPlantLength = minimumPlantLength;
    }

    @Override
    public PlanterAIModuleResult workField(
      final @NotNull PlantationField field,
      final @NotNull EntityAIWorkPlanter planterAI,
      final @NotNull AbstractEntityCitizen worker,
      final @NotNull BlockPos workPosition,
      final @NotNull FakePlayer fakePlayer)
    {
        if (planterAI.planterWalkToBlock(workPosition))
        {
            return PlanterAIModuleResult.MOVING;
        }

        PlanterAIModuleState action = decideWorkAction(field, workPosition);
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
    private PlanterAIModuleState decideWorkAction(PlantationField field, BlockPos plantingPosition)
    {
        Block blockAbove = field.getColony().getWorld().getBlockState(plantingPosition.above()).getBlock();
        Block blockAtMinHeight = field.getColony().getWorld().getBlockState(plantingPosition.above(minimumPlantLength)).getBlock();
        if (blockAbove == Blocks.AIR)
        {
            return PlanterAIModuleState.PLANTING;
        }

        if (blockAbove != getBlock())
        {
            return PlanterAIModuleState.CLEARING;
        }

        if (blockAtMinHeight == getBlock())
        {
            return PlanterAIModuleState.HARVESTING;
        }

        return PlanterAIModuleState.NONE;
    }

    @Override
    public BlockPos getNextWorkingPosition(PlantationField field)
    {
        for (BlockPos position : field.getWorkingPositions())
        {
            if (decideWorkAction(field, position) != PlanterAIModuleState.NONE)
            {
                return position;
            }
        }

        return null;
    }

    public static class Builder extends PlantationModule.Builder<VerticalUpwardsGrowingPlantModule.Builder>
    {
        /**
         * The minimum length this plant should grow to before considered harvestable.
         * Defaults to 3.
         */
        private int minimumPlantLength = 3;

        /**
         * Default constructor.
         *
         * @param fieldTag the tag of the field anchor block.
         * @param workTag  the tag of the working positions.
         * @param block    the block which is harvested.
         */
        public Builder(final String fieldTag, final String workTag, final Block block)
        {
            super(fieldTag, workTag, block);
        }

        /**
         * Sets the minimum length plants should be before the plants can be harvested.
         *
         * @param minimumPlantLength the minimum plant length.
         * @return the builder instance.
         */
        public Builder withMinimumPlantLength(int minimumPlantLength)
        {
            this.minimumPlantLength = minimumPlantLength;
            return this;
        }

        @Override
        public PlantationModule build()
        {
            return new VerticalUpwardsGrowingPlantModule(fieldTag, workTag, block, maxPlants, plantsToRequest, requiredResearchEffect, minimumPlantLength);
        }
    }
}
