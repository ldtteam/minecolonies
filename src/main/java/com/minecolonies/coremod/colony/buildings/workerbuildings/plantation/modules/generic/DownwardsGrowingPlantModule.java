package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.generic;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.colony.buildings.workerbuildings.fields.PlantationField;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.PlantationModule;
import com.minecolonies.coremod.entity.ai.citizen.planter.EntityAIWorkPlanter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * Plantation module for plants that grow vertically downwards, similar to sugar glowberries.
 * For a plant to fit this module it needs the following requirements.
 * - Grow vertically without any outcroppings.
 * - Grow uninterrupted (no gaps in between the plant).
 * - Must break all blocks below when a higher block is destroyed.
 */
public abstract class DownwardsGrowingPlantModule extends PlantationModule
{
    /**
     * The default minimum plant length.
     */
    protected static final int DEFAULT_MINIMUM_PLANT_LENGTH = 3;

    /**
     * The internal random used to decide whether to work this field or not.
     */
    private final Random random;

    /**
     * Default constructor.
     *
     * @param fieldTag the tag of the field anchor block.
     * @param workTag  the tag of the working positions.
     * @param item     the item which is harvested.
     */
    protected DownwardsGrowingPlantModule(
      final String fieldTag,
      final String workTag,
      final Item item)
    {
        super(fieldTag, workTag, item);
        this.random = new Random();
    }

    @Override
    public PlanterAIModuleResult workField(
      final @NotNull PlantationField field,
      final @NotNull EntityAIWorkPlanter planterAI,
      final @NotNull AbstractEntityCitizen worker,
      final @NotNull BlockPos workPosition,
      final @NotNull FakePlayer fakePlayer)
    {
        if (walkToWorkPosition(planterAI, field.getColony().getWorld(), workPosition))
        {
            return PlanterAIModuleResult.MOVING;
        }

        PlanterAIModuleState action = decideWorkAction(field, workPosition);
        return switch (action)
                 {
                     case NONE -> PlanterAIModuleResult.NONE;
                     case HARVESTING ->
                         // Tell the AI to mine a block, if we're harvesting we need to mine 1 block from the ceiling (2 high).
                       getHarvestingResultFromMiningResult(planterAI.planterMineBlock(workPosition.below(2), true));
                     case PLANTING ->
                     {
                         if (planterAI.planterPlaceBlock(workPosition.below(), getItem(), getPlantsToRequest()))
                         {
                             yield PlanterAIModuleResult.PLANTED;
                         }
                         yield PlanterAIModuleResult.REQUIRES_ITEMS;
                     }
                     case CLEARING ->
                         // Tell the AI to mine a block, if we're clearing an obstacle we need to clear the item at the position directly below (1 high).
                       getClearingResultFromMiningResult(planterAI.planterMineBlock(workPosition.below(), false));
                     default -> PlanterAIModuleResult.INVALID;
                 };
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

    /**
     * Logic to walk to a work position.
     *
     * @param planterAI    the AI class of the planter so instructions can be ordered to it.
     * @param world        the world the position is located in.
     * @param workPosition the position that has been chosen for work.
     * @return true if
     */
    protected boolean walkToWorkPosition(EntityAIWorkPlanter planterAI, Level world, BlockPos workPosition)
    {
        return planterAI.planterWalkToBlock(workPosition);
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
        BlockState blockBelow = field.getColony().getWorld().getBlockState(plantingPosition.below());
        if (blockBelow.isAir())
        {
            return PlanterAIModuleState.PLANTING;
        }

        if (blockBelow.getBlock() != getExpectedBlock())
        {
            return PlanterAIModuleState.CLEARING;
        }

        int minimumPlantLength = getMinimumPlantLength();
        Integer maximumPlantLength = getMaximumPlantLength();

        if (maximumPlantLength != null)
        {
            float currentHeight = 0;
            for (int height = minimumPlantLength; height <= maximumPlantLength; height++)
            {
                BlockState blockState = field.getColony().getWorld().getBlockState(plantingPosition.below(height));
                if (blockState.getBlock() != getExpectedBlock())
                {
                    break;
                }
                currentHeight = height;
            }

            float chance = currentHeight / maximumPlantLength;
            if (random.nextFloat() < chance)
            {
                return PlanterAIModuleState.HARVESTING;
            }
        }
        else
        {
            BlockState blockAtMinHeight = field.getColony().getWorld().getBlockState(plantingPosition.below(minimumPlantLength));
            if (blockAtMinHeight.getBlock() == getExpectedBlock())
            {
                return PlanterAIModuleState.HARVESTING;
            }
        }

        return PlanterAIModuleState.NONE;
    }

    /**
     * Get the expected block which should be present for harvesting.
     *
     * @return the block
     */
    @NotNull
    protected abstract Block getExpectedBlock();

    /**
     * Get the minimum length this plant should grow to before considered harvestable.
     * Defaults to {@link DownwardsGrowingPlantModule#DEFAULT_MINIMUM_PLANT_LENGTH}.
     *
     * @return the minimum plant length
     */
    protected int getMinimumPlantLength()
    {
        return DEFAULT_MINIMUM_PLANT_LENGTH;
    }

    /**
     * Get the maximum length this plant can grow.
     * Defaults to null.
     *
     * @return the maximum plant length
     */
    @Nullable
    protected Integer getMaximumPlantLength()
    {
        return null;
    }
}
