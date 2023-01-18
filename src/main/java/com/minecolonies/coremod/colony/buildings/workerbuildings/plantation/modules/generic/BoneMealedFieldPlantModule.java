package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.generic;

import com.minecolonies.api.colony.requestsystem.requestable.StackList;
import com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.translation.RequestSystemTranslationConstants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.fields.PlantationField;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.PlantationModule;
import com.minecolonies.coremod.entity.ai.citizen.planter.EntityAIWorkPlanter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Plantation module for plants that grow on a flat piece of land when bone-mealed.
 * - Soil blocks must be the same so plants can grow on them.
 * - The defined "block" on the module should be the primary plant harvested on this field.
 */
public class BoneMealedFieldPlantModule extends PlantationModule
{
    /**
     * The amount of bonemeal the worker should have at any time.
     */
    private static final int BONEMEAL_TO_KEEP = 16;

    /**
     * The chance in percentages for this field to trigger an action.
     */
    private final int percentChance;

    /**
     * The internal random used to decide whether to work this field or not.
     */
    private final Random random;

    /**
     * Default constructor.
     *
     * @param fieldTag               the tag of the field anchor block.
     * @param workTag                the tag of the working positions.
     * @param block                  the block which is harvested.
     * @param maxPlants              the maximum allowed plants.
     * @param plantsToRequest        the amount of plants to request when the planter has none left.
     * @param requiredResearchEffect the research effect required before this field type can be used.
     * @param percentChance          the chance in percentages for work to occur on this field.
     */
    protected BoneMealedFieldPlantModule(
      final String fieldTag,
      final String workTag,
      final Block block,
      final int maxPlants,
      final int plantsToRequest,
      final @Nullable ResourceLocation requiredResearchEffect,
      final int percentChance)
    {
        super(fieldTag, workTag, block, maxPlants, plantsToRequest, requiredResearchEffect);
        this.percentChance = Math.max(Math.min(percentChance, 100), 1);
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
        if (!checkForBoneMeal(planterAI))
        {
            return PlanterAIModuleResult.REQUIRES_ITEMS;
        }

        PlanterAIModuleState action = decideWorkAction(field, workPosition);
        return switch (action)
                 {
                     case NONE -> PlanterAIModuleResult.NONE;
                     case HARVESTING ->
                     {
                         // Walk to the position where work has to happen.
                         if (planterAI.planterWalkToBlock(workPosition.above()))
                         {
                             yield PlanterAIModuleResult.MOVING;
                         }

                         // Harvest the field block by block.
                         final PlanterAIModuleResult harvestResult = getHarvestingResultFromMiningResult(planterAI.planterMineBlock(workPosition.above(), true));
                         if (harvestResult != PlanterAIModuleResult.HARVESTED)
                         {
                             yield harvestResult;
                         }

                         // Check how many other positions still require to be harvested,
                         // if there's any left we keep harvesting
                         final long remainingPositions =
                           field.getWorkingPositions().stream().map(BlockPos::above).filter(f -> !worker.getLevel().getBlockState(f).isAir()).count();

                         if (remainingPositions > 0)
                         {
                             // Return a custom result here indicates that we harvested something, however we do want to keep working on the field
                             // but on another position.
                             yield new PlanterAIModuleResult(PlanterAIModuleState.HARVESTED, AIWorkerState.PLANTATION_WORK_FIELD, true, false);
                         }
                         else
                         {
                             // Return a custom result here indicates that we harvested something, however we want to completely abort working on the field right now.
                             // Therefore, we need to release the current field so that next tick a new field will be selected, running the needsWork check on the field again.
                             yield new PlanterAIModuleResult(PlanterAIModuleState.HARVESTED, AIWorkerState.PREPARING, false, true);
                         }
                     }
                     case PLANTING ->
                     {
                         if (planterAI.planterWalkToBlock(workPosition))
                         {
                             yield PlanterAIModuleResult.MOVING;
                         }

                         final int boneMealSlot = InventoryUtils.findFirstSlotInItemHandlerWith(worker.getInventoryCitizen(), stack -> stack.getItem() instanceof BoneMealItem);
                         final ItemStack stackInSlot = worker.getInventoryCitizen().getStackInSlot(boneMealSlot);
                         BoneMealItem.applyBonemeal(stackInSlot, worker.getLevel(), workPosition, fakePlayer);

                         // Return a custom result that indicates we planted something, but we want to remain working on the field, at a different position.
                         yield new PlanterAIModuleResult(PlanterAIModuleState.PLANTED, AIWorkerState.PLANTATION_WORK_FIELD, true, false);
                     }
                     default -> PlanterAIModuleResult.INVALID;
                 };
    }

    @Override
    public boolean needsWork(final PlantationField field)
    {
        // A bone-mealed field has no growth stage, since the growth stage is instant.
        // Therefore, we need to prevent the worker from constantly running around on this field only.
        // This is achieved by only allowing work when a percentage chance is met,
        // so that the field is not often considered as "needing work".
        boolean willWork = random.nextFloat() < (percentChance / 100F);
        return willWork && !field.getWorkingPositions().isEmpty();
    }

    @Override
    public @Nullable BlockPos getNextWorkingPosition(final PlantationField field)
    {
        // If there is anything to harvest, return the first position where a non-air block is present.
        for (BlockPos position : field.getWorkingPositions())
        {
            if (!field.getColony().getWorld().getBlockState(position.above()).isAir())
            {
                return position;
            }
        }

        // Get a random position on the field, which will act as the planting position.
        int idx = random.nextInt(0, field.getWorkingPositions().size());
        return field.getWorkingPositions().get(idx);
    }

    /**
     * Check if the planter has bonemeal available
     *
     * @param planterAI the AI class of the planter so instructions can be ordered to it.
     * @return whether the planter has bonemeal available.
     */
    private boolean checkForBoneMeal(EntityAIWorkPlanter planterAI)
    {
        final List<ItemStack> compostAbleItems = new ArrayList<>();
        compostAbleItems.add(new ItemStack(ModItems.compost, 1));
        compostAbleItems.add(new ItemStack(Items.BONE_MEAL, 1));
        return planterAI.checkIfRequestForItemExistOrCreate(new StackList(compostAbleItems,
          RequestSystemTranslationConstants.REQUEST_TYPE_FERTILIZER,
          BONEMEAL_TO_KEEP,
          BONEMEAL_TO_KEEP));
    }

    /**
     * Responsible for deciding what action the AI is going to perform on the field.
     *
     * @param field        the field to check for.
     * @param workPosition the position that has been chosen for work.
     * @return the {@link PlanterAIModuleResult} that the AI is going to perform.
     */
    private PlanterAIModuleState decideWorkAction(PlantationField field, BlockPos workPosition)
    {
        if (!field.getColony().getWorld().getBlockState(workPosition.above()).isAir())
        {
            return PlanterAIModuleState.HARVESTING;
        }

        return PlanterAIModuleState.PLANTING;
    }

    public static class Builder extends PlantationModule.Builder<BoneMealedFieldPlantModule.Builder>
    {

        /**
         * The chance in percentages for this field to trigger an action.
         * Defaults to 10%.
         */
        private int percentChance;

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
            this.percentChance = 10;
        }

        /**
         * Sets the minimum length plants should be before the plants can be harvested.
         * Defaults to 10%.
         *
         * @param percentChance the new percentage chance to trigger work on this field.
         * @return the builder instance.
         */
        public BoneMealedFieldPlantModule.Builder withPercentageChance(int percentChance)
        {
            this.percentChance = percentChance;
            return this;
        }

        @Override
        public PlantationModule build()
        {
            return new BoneMealedFieldPlantModule(fieldTag, workTag, block, maxPlants, plantsToRequest, requiredResearchEffect, percentChance);
        }
    }
}
