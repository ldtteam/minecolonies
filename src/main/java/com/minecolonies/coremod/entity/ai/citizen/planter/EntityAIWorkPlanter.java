package com.minecolonies.coremod.entity.ai.citizen.planter;

import com.ldtteam.structurize.util.BlockUtils;
import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.colony.fields.plantation.BasicPlanterAI;
import com.minecolonies.api.colony.fields.plantation.IPlantationModule;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.CitizenConstants;
import com.minecolonies.coremod.colony.buildings.modules.FieldsModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingPlantation;
import com.minecolonies.coremod.colony.fields.PlantationField;
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
public class EntityAIWorkPlanter extends AbstractEntityAICrafting<JobPlanter, BuildingPlantation> implements BasicPlanterAI
{
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
     * The amount of actions performed on the current field.
     */
    private int currentFieldActionCount = 0;

    /**
     * Constructor for the planter.
     *
     * @param job a planter job to use.
     */
    public EntityAIWorkPlanter(@NotNull final JobPlanter job)
    {
        super(job);
        super.registerTargets(
          new AITarget(PREPARING, () -> PLANTATION_PICK_FIELD, STANDARD_DELAY),
          new AITarget(PLANTATION_PICK_FIELD, this::pickField, TICKS_20),
          new AITarget(PLANTATION_MOVE_TO_FIELD, this::moveToField, TICKS_20),
          new AITarget(PLANTATION_WORK_FIELD, this::workField, TICKS_20 * 3));
        worker.setCanPickUpLoot(true);
    }

    private IAIState pickField()
    {
        worker.getCitizenData().setIdleAtJob(true);

        if (building == null || building.getBuildingLevel() < 1)
        {
            return IDLE;
        }

        FieldsModule module = building.getFirstModuleOccurance(FieldsModule.class);
        module.claimFields();

        if (module.hasNoFields())
        {
            if (worker.getCitizenData() != null)
            {
                worker.getCitizenData().triggerInteraction(new StandardInteraction(Component.translatable(NO_FREE_FIELDS), ChatPriority.BLOCKING));
            }
            return IDLE;
        }

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
                currentFieldActionCount = 0;
            }

            worker.getCitizenData().setIdleAtJob(false);
            worker.getCitizenData().setVisibleStatus(VisibleCitizenStatus.WORKING);

            return PLANTATION_MOVE_TO_FIELD;
        }

        return IDLE;
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
            return PLANTATION_PICK_FIELD;
        }

        if (walkToBlock(currentPlantationField.getPosition(), CitizenConstants.DEFAULT_RANGE_FOR_DELAY))
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
            return PLANTATION_PICK_FIELD;
        }

        IPlantationModule planterModule = currentPlantationField.getModule();
        if (currentWorkingPosition == null)
        {
            currentWorkingPosition = planterModule.getNextWorkingPosition(currentPlantationField);
        }

        if (currentWorkingPosition != null)
        {
            IPlantationModule.PlanterAIModuleResult result = planterModule.workField(currentPlantationField, this, worker, currentWorkingPosition, getFakePlayer());
            if (result.getModuleState().hasPerformedAction())
            {
                currentFieldActionCount++;
                incrementActionsDoneAndDecSaturation();
            }

            if (result.shouldResetWorkingPosition())
            {
                currentWorkingPosition = null;
            }
            if (result.shouldResetCurrentField() || currentFieldActionCount >= planterModule.getActionLimit())
            {
                // In certain scenarios the module may request to immediately release the current field, disregarding whether the next tick still has work or not.
                // Alternatively, if the maximum action count is reached, the field must be reset as well.
                FieldsModule fieldsModule = building.getFirstModuleOccurance(FieldsModule.class);
                fieldsModule.resetCurrentField();
                currentFieldActionCount = 0;
            }

            return result.getModuleState() == IPlantationModule.PlanterAIModuleState.REQUIRES_ITEMS ? IDLE : PLANTATION_WORK_FIELD;
        }

        return PLANTATION_PICK_FIELD;
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
    protected void updateRenderMetaData()
    {
        worker.setRenderMetadata(getState() == CRAFT
                                   || getState() == PLANTATION_WORK_FIELD
                                   || getState() == PLANTATION_MOVE_TO_FIELD ? RENDER_META_WORKING : "");
    }

    @Override
    protected IAIState decide()
    {
        IAIState state = super.decide();

        if (state == IDLE)
        {
            return PLANTATION_PICK_FIELD;
        }
        return state;
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        if (getState() != PLANTATION_WORK_FIELD)
        {
            return super.getActionsDoneUntilDumping();
        }

        PlantationField currentPlantationField = getCurrentField();
        if (currentPlantationField == null)
        {
            return super.getActionsDoneUntilDumping();
        }

        return currentPlantationField.getModule().getActionLimit();
    }

    @Override
    public Class<BuildingPlantation> getExpectedBuildingClass()
    {
        return BuildingPlantation.class;
    }

    @Override
    public boolean planterWalkToBlock(final BlockPos blockPos)
    {
        return planterWalkToBlock(blockPos, CitizenConstants.DEFAULT_RANGE_FOR_DELAY);
    }

    @Override
    public boolean planterWalkToBlock(final BlockPos blockPos, final int range)
    {
        return walkToBlock(blockPos, range);
    }

    @Override
    public IPlantationModule.PlanterMineBlockResult planterMineBlock(final BlockPos blockPos, boolean isHarvest)
    {
        boolean mineResult = mineBlock(blockPos);

        if (!holdEfficientTool(world.getBlockState(blockPos), blockPos))
        {
            return IPlantationModule.PlanterMineBlockResult.NO_TOOL;
        }

        if (mineResult)
        {
            worker.getCitizenItemHandler().pickupItems();

            if (isHarvest)
            {
                worker.getCitizenExperienceHandler().addExperience(XP_PER_HARVEST);
            }
        }

        return mineResult ? IPlantationModule.PlanterMineBlockResult.MINED : IPlantationModule.PlanterMineBlockResult.MINING;
    }

    @Override
    public boolean planterPlaceBlock(final BlockPos blockToPlaceAt, final Item item, final int numberToRequest)
    {
        return planterPlaceBlock(blockToPlaceAt, item, numberToRequest, t -> t);
    }

    @Override
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

    @Override
    public boolean requestItems(final IDeliverable deliverable)
    {
        return this.checkIfRequestForItemExistOrCreate(deliverable);
    }
}