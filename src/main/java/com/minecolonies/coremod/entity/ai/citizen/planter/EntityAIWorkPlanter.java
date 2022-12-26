package com.minecolonies.coremod.entity.ai.citizen.planter;

import com.ldtteam.structurize.util.BlockUtils;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.CitizenConstants;
import com.minecolonies.coremod.colony.buildings.modules.FieldModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingPlantation;
import com.minecolonies.coremod.colony.buildings.workerbuildings.fields.PlantationField;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.PlantationModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.PlantationModuleRegistry;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteraction;
import com.minecolonies.coremod.colony.jobs.JobPlanter;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAICrafting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        worker.getCitizenData().setVisibleStatus(VisibleCitizenStatus.WORKING);

        FieldModule module = building.getFirstModuleOccurance(FieldModule.class);
        if (module.hasNoFields())
        {
            if (worker.getCitizenData() != null)
            {
                worker.getCitizenData().triggerInteraction(new StandardInteraction(new TranslatableComponent(NO_FREE_FIELDS), ChatPriority.BLOCKING));
            }
            worker.getCitizenData().setIdleAtJob(true);
            return PREPARING;
        }
        worker.getCitizenData().setIdleAtJob(false);

        // If the planter has no currentField and there is no field which needs work, check fields.
        if (module.getCurrentField() == null && module.getFieldToWorkOn() == null)
        {
            return IDLE;
        }

        PlantationField currentPlantationField = getCurrentField();
        if (currentPlantationField != null && currentPlantationField.needsWork())
        {
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

        if (walkToBlock(currentPlantationField.getPosition().above(), CitizenConstants.DEFAULT_RANGE_FOR_DELAY * 2))
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
            return switch (planterModule.workField(currentPlantationField, this, currentWorkingPosition))
                     {
                         // Either when the AI has harvested, planted, cleared the position, failed to compute or simply nothing to do, return to the decision state.
                         case INVALID, NONE, HARVESTED, PLANTED, CLEARED ->
                         {
                             // Reset the current working position so that next working iteration a new field/position will be selected.
                             currentWorkingPosition = null;
                             yield PREPARING;
                         }
                         // When the AI requires items move the AI into the GATHERING_REQUIRED_MATERIALS state to pick up the requested items.
                         case REQUIRES_ITEMS -> GATHERING_REQUIRED_MATERIALS;
                         // Either when the AI is moving to, harvesting or clearing a position, keep working on that position.
                         case MOVING, HARVESTING, PLANTING, CLEARING -> PLANTATION_WORK_FIELD;
                     };
        }
        else
        {
            return PREPARING;
        }
    }

    @Nullable
    private PlantationField getCurrentField()
    {
        FieldModule fieldModule = building.getFirstModuleOccurance(FieldModule.class);
        if (fieldModule.getCurrentField() instanceof PlantationField field)
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
        return walkToBlock(blockPos, CitizenConstants.DEFAULT_RANGE_FOR_DELAY / 2);
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
        ItemStack currentStack = new ItemStack(item);
        int plantInInv = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), itemStack -> itemStack.sameItem(currentStack));
        if (plantInInv <= 0)
        {
            int plantInBuilding = InventoryUtils.getCountFromBuilding(building, itemStack -> itemStack.sameItem(currentStack));
            if (plantInBuilding <= 0)
            {
                worker.getCitizenData().createRequestAsync(new Stack(new ItemStack(item, numberToRequest)));
            }
            needsCurrently = new Tuple<>(it -> !it.isEmpty() && it.getItem().equals(item), numberToRequest);

            return false;
        }

        worker.getCitizenItemHandler().setHeldItem(InteractionHand.MAIN_HAND, InventoryUtils.findFirstSlotInItemHandlerWith(worker.getInventoryCitizen(), item));

        if (world.setBlockAndUpdate(blockToPlaceAt, BlockUtils.getBlockStateFromStack(currentStack)))
        {
            InventoryUtils.reduceStackInItemHandler(worker.getInventoryCitizen(), currentStack);
            worker.getCitizenItemHandler().removeHeldItem();
            return true;
        }

        return false;
    }
}
