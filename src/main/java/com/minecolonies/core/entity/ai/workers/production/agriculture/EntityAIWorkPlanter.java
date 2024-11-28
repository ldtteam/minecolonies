package com.minecolonies.core.entity.ai.workers.production.agriculture;

import com.ldtteam.structurize.util.BlockUtils;
import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.colony.fields.plantation.IPlantationModule;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IConcreteDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.colony.requestsystem.requestable.StackList;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.CitizenConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.api.util.constant.translation.RequestSystemTranslationConstants;
import com.minecolonies.core.colony.buildings.modules.FieldsModule;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingPlantation;
import com.minecolonies.core.colony.fields.PlantationField;
import com.minecolonies.core.colony.interactionhandling.StandardInteraction;
import com.minecolonies.core.colony.jobs.JobPlanter;
import com.minecolonies.core.entity.ai.workers.crafting.AbstractEntityAICrafting;
import com.minecolonies.core.util.citizenutils.CitizenItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.CitizenConstants.TICKS_20;
import static com.minecolonies.api.util.constant.StatisticsConstants.ITEM_OBTAINED;
import static com.minecolonies.api.util.constant.TranslationConstants.NO_FREE_FIELDS;
import static com.minecolonies.core.colony.buildings.modules.BuildingModules.STATS_MODULE;

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

    private IConcreteDeliverable currentDeliverable;

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
          new AITarget(PLANTATION_WORK_FIELD, this::workField, TICKS_20),
          new AITarget(PLANTATION_RETURN_TO_BUILDING, this::returnToBuilding, TICKS_20));
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

    @Override
    public void onBlockDropReception(final List<ItemStack> blockDrops)
    {
        super.onBlockDropReception(blockDrops);
        for (final ItemStack stack : blockDrops)
        {
            building.getModule(STATS_MODULE).incrementBy(ITEM_OBTAINED + ";" + stack.getItem().getDescriptionId(), stack.getCount());
        }
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
        if (!Objects.isNull(activeModuleResult.getActionPosition()) && walkToBlock(planterModule.getPositionToWalkTo(world, activeModuleResult.getActionPosition())))
        {
            return PLANTATION_WORK_FIELD;
        }

        ActionHandlerResult handlerResult = switch (activeModuleResult.getAction())
                                              {
                                                  case NONE -> ActionHandlerResult.FINISHED;
                                                  case PLANT -> handlePlantingAction();
                                                  case BONEMEAL -> handleBonemealAction();
                                                  case HARVEST -> handleMiningAction(true);
                                                  case CLEAR -> handleMiningAction(false);
                                              };

        if (handlerResult.equals(ActionHandlerResult.FINISHED))
        {
            CitizenItemUtils.removeHeldItem(worker);

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
        else if (handlerResult.equals(ActionHandlerResult.NEEDS_ITEM))
        {
            activeModuleResult = null;
            return PLANTATION_RETURN_TO_BUILDING;
        }

        return PLANTATION_WORK_FIELD;
    }

    /**
     * Start moving the AI back to their building for pickup requests, if no pickup request was made, return to IDLE.
     *
     * @return next state to go to.
     */
    private IAIState returnToBuilding()
    {
        if (walkToBuilding())
        {
            return getState();
        }

        if (needsCurrently != null)
        {
            return GATHERING_REQUIRED_MATERIALS;
        }

        return IDLE;
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
     * @return finished if the item was planted, busy if the worker is still busy planting, needs item if the item to plant does not exist.
     */
    private ActionHandlerResult handlePlantingAction()
    {
        if (!Objects.isNull(activeModuleResult.getActionPosition()))
        {
            IPlantationModule planterModule = activeModuleResult.getModule();

            ItemStack currentStack = new ItemStack(planterModule.getItem());
            if (checkIfItemsUnavailable(new Stack(currentStack, planterModule.getPlantsToRequest(), 1)))
            {
                return ActionHandlerResult.NEEDS_ITEM;
            }

            final int slot = InventoryUtils.findFirstSlotInItemHandlerWith(worker.getItemHandlerCitizen(), currentStack.getItem());
            CitizenItemUtils.setMainHeldItem(worker, slot);

            BlockState blockState = planterModule.getPlantingBlockState(world, activeModuleResult.getWorkingPosition(), BlockUtils.getBlockStateFromStack(currentStack));
            if (world.setBlockAndUpdate(activeModuleResult.getActionPosition(), blockState))
            {
                InventoryUtils.reduceStackInItemHandler(worker.getItemHandlerCitizen(), currentStack);
                CitizenItemUtils.removeHeldItem(worker);
                return ActionHandlerResult.FINISHED;
            }

            return ActionHandlerResult.BUSY;
        }
        return ActionHandlerResult.FINISHED;
    }

    /**
     * Handles actions for using bonemeal on the ground.
     *
     * @return finished result if bonemeal was used, busy if the planter is busy planting, needs item if he's missing bonemeal.
     */
    private ActionHandlerResult handleBonemealAction()
    {
        if (!Objects.isNull(activeModuleResult.getActionPosition()))
        {
            IPlantationModule planterModule = activeModuleResult.getModule();

            List<ItemStack> bonemeal = planterModule.getValidBonemeal().stream().map(ItemStack::new).toList();
            if (checkIfItemsUnavailable(new StackList(bonemeal,
              RequestSystemTranslationConstants.REQUEST_TYPE_FERTILIZER,
              BONEMEAL_TO_KEEP,
              1)))
            {
                return ActionHandlerResult.NEEDS_ITEM;
            }

            final int boneMealSlot =
              InventoryUtils.findFirstSlotInItemHandlerWith(worker.getInventoryCitizen(), stack -> planterModule.getValidBonemeal().contains(stack.getItem()));
            final ItemStack stackInSlot = worker.getInventoryCitizen().getStackInSlot(boneMealSlot);
            planterModule.applyBonemeal(worker, activeModuleResult.getActionPosition(), stackInSlot, getFakePlayer());
        }
        return ActionHandlerResult.FINISHED;
    }

    /**
     * Handles actions for harvesting/clearing plants/blocks.
     *
     * @return finished if the item was harvested, busy if the worker is still busy harvesting, needs item if he's missing the necessary tool.
     */
    private ActionHandlerResult handleMiningAction(boolean isHarvest)
    {
        if (!Objects.isNull(activeModuleResult.getActionPosition()))
        {
            if (!holdEfficientTool(world.getBlockState(activeModuleResult.getActionPosition()), activeModuleResult.getActionPosition()))
            {
                return ActionHandlerResult.NEEDS_ITEM;
            }

            boolean mineResult = mineBlock(activeModuleResult.getActionPosition());
            if (mineResult)
            {
                CitizenItemUtils.pickupItems(worker);

                if (isHarvest)
                {
                    worker.getCitizenExperienceHandler().addExperience(XP_PER_HARVEST);
                }
            }

            return mineResult ? ActionHandlerResult.FINISHED : ActionHandlerResult.BUSY;
        }
        return ActionHandlerResult.FINISHED;
    }

    /**
     * Checks if the planter has enough of a certain item in their inventory, or in their hut.
     *
     * @param deliverable the request that needs to be delivered.
     * @return false if the items are present in their own inventory, true if the items are in the hut or not present at all.
     */
    private boolean checkIfItemsUnavailable(final IConcreteDeliverable deliverable)
    {
        final int invCount =
          InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), deliverable::matches);
        if (invCount >= deliverable.getMinimumCount())
        {
            return false;
        }

        currentDeliverable = deliverable;
        needsCurrently = new Tuple<>(deliverable::matches, deliverable.getCount());
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
    public IAIState getStateAfterPickUp()
    {
        if (currentDeliverable != null && !InventoryUtils.hasItemInItemHandler(worker.getInventoryCitizen(), currentDeliverable.getResult().getItem())
              && building.getOpenRequestsOfTypeFiltered(worker.getCitizenData(), TypeConstants.DELIVERABLE,
          (IRequest<? extends IDeliverable> r) -> currentDeliverable.getRequestedItems().stream().anyMatch(r.getRequest()::matches)).isEmpty()
              && building.getCompletedRequestsOfTypeFiltered(worker.getCitizenData(), TypeConstants.DELIVERABLE,
          (IRequest<? extends IDeliverable> r) -> currentDeliverable.getRequestedItems().stream().anyMatch(r.getRequest()::matches)).isEmpty())
        {
            worker.getCitizenData().createRequestAsync(currentDeliverable);
        }
        currentDeliverable = null;
        needsCurrently = null;

        return super.getStateAfterPickUp();
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

    private enum ActionHandlerResult
    {
        FINISHED,
        BUSY,
        NEEDS_ITEM,
    }
}