package com.minecolonies.coremod.entity.ai.citizen.planter;

import com.ldtteam.structurize.util.BlockUtils;
import com.minecolonies.api.colony.buildings.workerbuildings.fields.IField;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.CitizenConstants;
import com.minecolonies.coremod.colony.buildings.modules.FieldsModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingPlantation;
import com.minecolonies.coremod.colony.buildings.workerbuildings.fields.PlantationField;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.PlantationModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.PlantationModuleRegistry;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteraction;
import com.minecolonies.coremod.colony.jobs.JobPlanter;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAICrafting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.CitizenConstants.TICKS_20;
import static com.minecolonies.api.util.constant.TranslationConstants.NO_FREE_FIELDS;

/**
 * Planter AI class.
 */
public class EntityAIWorkPlanter extends AbstractEntityAICrafting<JobPlanter, BuildingPlantation>
{
    /**
     * Return to chest after this amount of stacks.
     */
    private static final int MAX_BLOCKS_MINED = 64;

    /**
     * Xp per harvesting block
     */
    private static final double XP_PER_HARVEST = 1;

    /**
     * The current position on the field the planter is working at.
     */
    @Nullable
    private BlockPos currentWorkingPosition;

    /**
     * Constructor for the planter.
     *
     * @param job a planter job to use.
     */
    public EntityAIWorkPlanter(@NotNull final JobPlanter job)
    {
        super(job);
        super.registerTargets(
          new AITarget(PREPARING, this::prepareForFieldWork, TICKS_20),
          new AITarget(PLANTATION_MOVE_TO_FIELD, this::moveToField, TICKS_20),
          new AITarget(PLANTATION_WORK_FIELD, this::workField, TICKS_20 * 3));
        worker.setCanPickUpLoot(true);
    }

    private IAIState prepareForFieldWork()
    {
        if (building == null || building.getBuildingLevel() < 1)
        {
            return PREPARING;
        }

        if (!job.getTaskQueue().isEmpty() || getActionsDoneUntilDumping() <= job.getActionsDone())
        {
            return START_WORKING;
        }

        FieldsModule module = building.getFirstModuleOccurance(FieldsModule.class);
        module.claimFields();

        if (module.hasNoFields())
        {
            if (worker.getCitizenData() != null)
            {
                worker.getCitizenData().triggerInteraction(new StandardInteraction(Component.translatable(NO_FREE_FIELDS), ChatPriority.BLOCKING));
            }
            worker.getCitizenData().setIdleAtJob(true);
            return PREPARING;
        }

        worker.getCitizenData().setIdleAtJob(false);
        worker.getCitizenData().setVisibleStatus(VisibleCitizenStatus.WORKING);

        // Get the next field to work on, if any.
        final IField lastField = module.getCurrentField();
        final IField fieldToWork = module.getFieldToWorkOn();
        if (fieldToWork != null)
        {
            // If we suddenly have to work on a new field, always reset the working position.
            // This is because if a field is unassigned from the worker in the middle of an ongoing action inside a module
            // the AI may not be able to return the appropriate information and accidentally end up in a situation
            // where he thinks his working position is still on another field.
            if (lastField != fieldToWork)
            {
                currentWorkingPosition = null;
            }

            return PLANTATION_MOVE_TO_FIELD;
        }

        return START_WORKING;
    }

    /**
     * Start moving the AI towards a specific field to start working on said field.
     *
     * @return next state to go to.
     */
    private IAIState moveToField()
    {
        PlantationField currentPlantationField = getCurrentField();
        if (currentPlantationField == null)
        {
            return PREPARING;
        }

        if (walkToBlock(currentPlantationField.getPosition().above(), CitizenConstants.DEFAULT_RANGE_FOR_DELAY))
        {
            return getState();
        }

        return PLANTATION_WORK_FIELD;
    }

    /**
     * Start moving the AI towards a specific field to start working on said field.
     *
     * @return next state to go to.
     */
    private IAIState workField()
    {
        PlantationField currentPlantationField = getCurrentField();
        if (currentPlantationField == null)
        {
            return PREPARING;
        }

        PlantationModule planterModule = PlantationModuleRegistry.getPlantationModule(currentPlantationField.getPlantationFieldType());
        if (planterModule == null)
        {
            throw new IllegalStateException("Planter AI could not load module for field, plantation type is likely null.");
        }

        if (currentWorkingPosition == null)
        {
            currentWorkingPosition = planterModule.getNextWorkingPosition(currentPlantationField);
        }

        if (currentWorkingPosition != null)
        {
            PlantationModule.PlanterAIModuleResult result = planterModule.workField(currentPlantationField, this, worker, currentWorkingPosition, getFakePlayer());
            if (result.shouldResetWorkingPosition())
            {
                currentWorkingPosition = null;
            }
            if (result.shouldResetCurrentField())
            {
                // In certain scenarios the module may request to immediately release the current field, disregarding whether the next tick still has work or not.
                FieldsModule fieldsModule = building.getFirstModuleOccurance(FieldsModule.class);
                fieldsModule.resetCurrentField();
                currentWorkingPosition = null;
            }
            return result.getNextState();
        }
        else
        {
            return PREPARING;
        }
    }

    @Nullable
    private PlantationField getCurrentField()
    {
        FieldsModule fieldsModule = building.getFirstModuleOccurance(FieldsModule.class);
        if (fieldsModule.getCurrentField() instanceof PlantationField field)
        {
            return field;
        }
        return null;
    }

    @Override
    protected int getActionRewardForCraftingSuccess()
    {
        return MAX_BLOCKS_MINED;
    }

    @Override
    protected void updateRenderMetaData()
    {
        worker.setRenderMetadata(getState() == CRAFT
                                   || getState() == PLANTATION_WORK_FIELD
                                   || getState() == PLANTATION_MOVE_TO_FIELD ? RENDER_META_WORKING : "");
    }

    @Override
    protected IAIState decide()
    {
        final IAIState nextState = super.decide();
        if (nextState != START_WORKING && nextState != IDLE)
        {
            return nextState;
        }

        if (wantInventoryDumped())
        {
            // Wait to dump before continuing.
            return getState();
        }

        if (job.getTaskQueue().isEmpty())
        {
            return PREPARING;
        }

        if (job.getCurrentTask() == null)
        {
            return PREPARING;
        }

        if (currentRequest != null && currentRecipeStorage != null)
        {
            return QUERY_ITEMS;
        }

        return GET_RECIPE;
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return MAX_BLOCKS_MINED;
    }

    @Override
    public Class<BuildingPlantation> getExpectedBuildingClass()
    {
        return BuildingPlantation.class;
    }

    public boolean planterWalkToBlock(final BlockPos blockPos)
    {
        return planterWalkToBlock(blockPos, CitizenConstants.DEFAULT_RANGE_FOR_DELAY);
    }

    public boolean planterWalkToBlock(final BlockPos blockPos, final int range)
    {
        return walkToBlock(blockPos, range);
    }

    public PlantationModule.PlanterMineBlockResult planterMineBlock(final BlockPos blockPos, boolean isHarvest)
    {
        boolean mineResult = mineBlock(blockPos);

        if (!holdEfficientTool(world.getBlockState(blockPos), blockPos))
        {
            return PlantationModule.PlanterMineBlockResult.NO_TOOL;
        }

        if (mineResult)
        {
            worker.getCitizenItemHandler().pickupItems();

            if (isHarvest)
            {
                worker.getCitizenExperienceHandler().addExperience(XP_PER_HARVEST);
            }
        }

        return mineResult ? PlantationModule.PlanterMineBlockResult.MINED : PlantationModule.PlanterMineBlockResult.MINING;
    }

    public boolean planterPlaceBlock(final BlockPos blockToPlaceAt, final Item item, final int numberToRequest)
    {
        return planterPlaceBlock(blockToPlaceAt, item, numberToRequest, t -> t);
    }

    public boolean planterPlaceBlock(final BlockPos blockToPlaceAt, final Item item, final int numberToRequest, UnaryOperator<BlockState> blockStateModifier)
    {
        ItemStack currentStack = new ItemStack(item);
        if (!checkIfRequestForItemExistOrCreateAsync(currentStack, numberToRequest, numberToRequest, true))
        {
            return false;
        }

        worker.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item));

        if (world.setBlockAndUpdate(blockToPlaceAt, blockStateModifier.apply(BlockUtils.getBlockStateFromStack(currentStack))))
        {
            InventoryUtils.reduceStackInItemHandler(worker.getInventoryCitizen(), currentStack);
            worker.getCitizenItemHandler().removeHeldItem();
            return true;
        }

        return false;
    }
}