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
 * Plantation module for plants that grow vertically upwards, similar to sugar cane.
 * For a plant to fit this module it needs the following requirements.
 * - Grow vertically without any outcroppings.
 * - Grow uninterrupted (no gaps in between the plant).
 * - Must break all blocks above when a lower block is destroyed.
 */
public abstract class UpwardsGrowingPlantModule extends PlantationModule
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
    protected UpwardsGrowingPlantModule(
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
                       getClearingResultFromMiningResult(planterAI.planterMineBlock(workPosition.above(), false));
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
        if (isPlantable(field, plantingPosition))
        {
            return PlanterAIModuleState.PLANTING;
        }

        if (isClearable(field, plantingPosition))
        {
            return PlanterAIModuleState.CLEARING;
        }

        if (isHarvestable(field, plantingPosition))
        {
            return PlanterAIModuleState.HARVESTING;
        }

        return PlanterAIModuleState.NONE;
    }

    /**
     * Checks if the provided block at the given location is plantable.
     *
     * @param field            the field to check for.
     * @param plantingPosition the specific position to check for.
     * @return true if plant is plantable.
     */
    protected boolean isPlantable(PlantationField field, BlockPos plantingPosition)
    {
        return field.getColony().getWorld().getBlockState(plantingPosition.above()).isAir();
    }

    /**
     * Checks if the provided block at the given location is clearable.
     *
     * @param field            the field to check for.
     * @param plantingPosition the specific position to check for.
     * @return true if plant is plantable.
     */
    protected boolean isClearable(PlantationField field, BlockPos plantingPosition)
    {
        return !isValidBlock(field.getColony().getWorld().getBlockState(plantingPosition.above()).getBlock());
    }

    /**
     * Checks if the provided block at the given location is harvestable.
     *
     * @param field            the field to check for.
     * @param plantingPosition the specific position to check for.
     * @return true if plant is harvestable.
     */
    protected boolean isHarvestable(PlantationField field, BlockPos plantingPosition)
    {
        int minimumPlantLength = getMinimumPlantLength();
        Integer maximumPlantLength = getMaximumPlantLength();

        if (maximumPlantLength != null)
        {
            float currentHeight = 0;
            for (int height = minimumPlantLength; height <= maximumPlantLength; height++)
            {
                BlockState blockState = field.getColony().getWorld().getBlockState(plantingPosition.above(height));
                if (isValidBlock(blockState.getBlock()))
                {
                    break;
                }
                currentHeight = height;
            }

            float chance = currentHeight / maximumPlantLength;
            return random.nextFloat() < chance;
        }
        else
        {
            BlockState blockAtMinHeight = field.getColony().getWorld().getBlockState(plantingPosition.above(minimumPlantLength));
            return isValidBlock(blockAtMinHeight.getBlock());
        }
    }

    /**
     * Check if the block is a correct block for harvesting.
     *
     * @return the block
     */
    protected abstract boolean isValidBlock(Block block);

    /**
     * Get the minimum length this plant should grow to before considered harvestable.
     * Defaults to {@link UpwardsGrowingPlantModule#DEFAULT_MINIMUM_PLANT_LENGTH}.
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
