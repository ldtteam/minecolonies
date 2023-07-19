package com.minecolonies.coremod.entity.ai.citizen.planter;

import com.ldtteam.structurize.util.BlockUtils;
import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.colony.fields.plantation.IPlantationModule;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.requestsystem.requestable.StackList;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.CitizenConstants;
import com.minecolonies.api.util.constant.translation.RequestSystemTranslationConstants;
import com.minecolonies.coremod.colony.buildings.modules.FieldsModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingPlantation;
import com.minecolonies.coremod.colony.fields.PlantationField;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteraction;
import com.minecolonies.coremod.colony.jobs.JobPlanter;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAICrafting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.CitizenConstants.TICKS_20;
import static com.minecolonies.api.util.constant.TranslationConstants.NO_FREE_FIELDS;

/**
 * Planter AI class.
 */
public class EntityAIWorkPlanter extends AbstractEntityAICrafting<JobPlanter, BuildingPlantation>
{
    /**
     * The amount of bonemeal the worker should have at any time.
     */
    protected static final int BONEMEAL_TO_KEEP = 16;

    /**
     * Xp per harvesting block
     */
    private static final double XP_PER_HARVEST = 1;

    /**
     * The amount of actions performed on the current field.
     */
    private int currentFieldActionCount = 0;

    /**
     * The active result object.
     */
    private IPlantationModule.PlantationModuleResult activeModuleResult = null;

    /**
     * Constructor for the planter.
     *
     * @param job a planter job to use.
     */
    public EntityAIWorkPlanter(@NotNull final JobPlanter job)
    {
        super(job);
        super.registerTargets(
          new AITarget(PREPARING, this::prepare, STANDARD_DELAY),
          new AITarget(PLANTATION_PICK_FIELD, this::pickField, TICKS_20),
          new AITarget(PLANTATION_MOVE_TO_FIELD, this::moveToField, TICKS_20),
          new AITarget(PLANTATION_DECIDE_FIELD_WORK, this::decideFieldWork, TICKS_20),
          new AITarget(PLANTATION_WORK_FIELD, this::workField, TICKS_20));
        worker.setCanPickUpLoot(true);
    }

    private IAIState prepare()
    {
        if (activeModuleResult != null)
        {
            return PLANTATION_WORK_FIELD;
        }
        return PLANTATION_PICK_FIELD;
    }

    /**
     * Start the AI off by picking a field which to move towards.
     *
     * @return next state to go to.
     */
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

        return PLANTATION_DECIDE_FIELD_WORK;
    }

    /**
     * Start moving the AI towards a specific field and decide what to do on this field.
     *
     * @return next state to go to.
     */
    private IAIState decideFieldWork()
    {
        PlantationField currentPlantationField = getCurrentField();
        if (currentPlantationField == null)
        {
            return PLANTATION_PICK_FIELD;
        }

        IPlantationModule planterModule = currentPlantationField.getModule();
        BlockPos position = planterModule.getNextWorkingPosition(world);
        if (position == null)
        {
            resetActiveField();
            return IDLE;
        }

        IPlantationModule.PlantationModuleResult.Builder result = planterModule.decideFieldWork(world, position);
        activeModuleResult = result.build(planterModule, position);
        return PLANTATION_WORK_FIELD;
    }

    /**
     * Start moving the AI towards a specific field and decide what to do on this field.
     *
     * @return next state to go to.
     */
    private IAIState workField()
    {
        IPlantationModule planterModule = activeModuleResult.getModule();
        if (walkToBlock(planterModule.getPositionToWalkTo(world, activeModuleResult.getActionPosition())))
        {
            return PLANTATION_WORK_FIELD;
        }

        boolean isFinished = switch (activeModuleResult.getAction())
        {
            case NONE -> true;
            case PLANT -> handlePlantingAction();
            case BONEMEAL -> handleBonemealAction();
            case HARVEST -> handleMiningAction(true);
            case CLEAR -> handleMiningAction(false);
        };

        if (isFinished)
        {
            worker.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

            if (activeModuleResult.getAction().increasesActionCount())
            {
                currentFieldActionCount++;
                incrementActionsDoneAndDecSaturation();
            }

            IAIState result = PLANTATION_WORK_FIELD;
            if (activeModuleResult.shouldResetWorkingPosition())
            {
                result = PLANTATION_DECIDE_FIELD_WORK;
            }
            if (activeModuleResult.shouldResetCurrentField() || currentFieldActionCount >= planterModule.getActionLimit())
            {
                resetActiveField();

                result = PLANTATION_PICK_FIELD;
            }

            activeModuleResult = null;
            return result;
        }

        return PLANTATION_WORK_FIELD;
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

    private void resetActiveField()
    {
        FieldsModule fieldsModule = building.getFirstModuleOccurance(FieldsModule.class);
        fieldsModule.resetCurrentField();
        currentFieldActionCount = 0;
    }

    /**
     * Handles actions for planting an item on the ground.
     *
     * @return true if the item was planted, false is the worker is still busy planting (either doing the animation or requesting the item).
     */
    private boolean handlePlantingAction()
    {
        if (!Objects.isNull(activeModuleResult.getActionPosition()))
        {
            IPlantationModule planterModule = activeModuleResult.getModule();

            ItemStack currentStack = new ItemStack(planterModule.getItem());
            if (!checkIfRequestForItemExistOrCreateAsync(currentStack, planterModule.getPlantsToRequest(), planterModule.getPlantsToRequest()))
            {
                return false;
            }

            worker.setItemInHand(InteractionHand.MAIN_HAND, currentStack);

            BlockState blockState = planterModule.getPlantingBlockState(world, activeModuleResult.getWorkingPosition(), BlockUtils.getBlockStateFromStack(currentStack));
            if (world.setBlockAndUpdate(activeModuleResult.getActionPosition(), blockState))
            {
                InventoryUtils.reduceStackInItemHandler(worker.getInventoryCitizen(), currentStack);
                worker.getCitizenItemHandler().removeHeldItem();
                return true;
            }

            return false;
        }
        return true;
    }

    /**
     * Handles actions for using bonemeal on the ground.
     *
     * @return true if bonemeal was used, false if planter has no bonemeal and needs to request some.
     */
    private boolean handleBonemealAction()
    {
        if (!Objects.isNull(activeModuleResult.getActionPosition()))
        {
            IPlantationModule planterModule = activeModuleResult.getModule();

            List<ItemStack> bonemeal = planterModule.getValidBonemeal().stream().map(ItemStack::new).toList();
            if (!checkIfRequestForItemExistOrCreate(new StackList(bonemeal,
              RequestSystemTranslationConstants.REQUEST_TYPE_FERTILIZER,
              BONEMEAL_TO_KEEP * 4,
              BONEMEAL_TO_KEEP)))
            {
                return false;
            }

            final int boneMealSlot =
              InventoryUtils.findFirstSlotInItemHandlerWith(worker.getInventoryCitizen(), stack -> planterModule.getValidBonemeal().contains(stack.getItem()));
            final ItemStack stackInSlot = worker.getInventoryCitizen().getStackInSlot(boneMealSlot);
            planterModule.applyBonemeal(worker, activeModuleResult.getActionPosition(), stackInSlot, getFakePlayer());
        }
        return true;
    }

    /**
     * Handles actions for harvesting/clearing plants/blocks.
     *
     * @return true if the item was harvested, false is the worker is still busy harvesting (either doing the animation or requesting the tools).
     */
    private boolean handleMiningAction(boolean isHarvest)
    {
        if (!Objects.isNull(activeModuleResult.getActionPosition()))
        {
            if (!holdEfficientTool(world.getBlockState(activeModuleResult.getActionPosition()), activeModuleResult.getActionPosition()))
            {
                return false;
            }

            boolean mineResult = mineBlock(activeModuleResult.getActionPosition());
            if (mineResult)
            {
                worker.getCitizenItemHandler().pickupItems();

                if (isHarvest)
                {
                    worker.getCitizenExperienceHandler().addExperience(XP_PER_HARVEST);
                }
            }

            return mineResult;
        }
        return true;
    }

    @Override
    protected void updateRenderMetaData()
    {
        worker.setRenderMetadata(getState() == CRAFT
                                   || getState() == PLANTATION_WORK_FIELD
                                   || getState() == PLANTATION_DECIDE_FIELD_WORK ? RENDER_META_WORKING : "");
    }

    @Override
    protected IAIState decide()
    {
        IAIState state = super.decide();

        if (state == IDLE)
        {
            return PREPARING;
        }
        return state;
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        if (getState() != PLANTATION_DECIDE_FIELD_WORK && getState() != PLANTATION_WORK_FIELD)
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
}