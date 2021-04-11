package com.minecolonies.coremod.entity.ai.basic;

import com.ldtteam.structurize.placement.BlockPlacementResult;
import com.ldtteam.structurize.placement.StructurePhasePlacementResult;
import com.ldtteam.structurize.placement.StructurePlacer;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.util.*;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.coremod.colony.buildings.utils.BuildingBuilderResource;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.coremod.colony.colonyEvents.buildingEvents.BuildingBuiltEvent;
import com.minecolonies.coremod.colony.colonyEvents.buildingEvents.BuildingDeconstructedEvent;
import com.minecolonies.coremod.colony.colonyEvents.buildingEvents.BuildingUpgradedEvent;
import com.minecolonies.coremod.colony.jobs.AbstractJobStructure;
import com.minecolonies.coremod.colony.workorders.*;
import com.minecolonies.coremod.entity.ai.util.BuildingStructureHandler;
import com.minecolonies.coremod.entity.ai.util.WorkerLoadOnlyStructureHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.ldtteam.structurize.placement.BlueprintIterator.NULL_POS;
import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.IDLE;
import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.PICK_UP_RESIDUALS;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * AI class for the builder. Manages building and repairing buildings.
 */
public abstract class AbstractEntityAIStructureWithWorkOrder<J extends AbstractJobStructure<?, J>, B extends AbstractBuildingStructureBuilder>
  extends AbstractEntityAIStructure<J, B>
{
    /**
     * Possible request stages
     */
    private enum RequestStage
    {
        SOLID,
        DECO,
        ENTITIES
    }

    /**
     * The current request state (0 is solid
     */
    private RequestStage requestState = RequestStage.SOLID;

    /**
     * Request progress pos.
     */
    private BlockPos requestProgress = null;

    /**
     * Initialize the builder and add all his tasks.
     *
     * @param job the job he has.
     */
    public AbstractEntityAIStructureWithWorkOrder(@NotNull final J job)
    {
        super(job);
        worker.setCanPickUpLoot(true);
    }

    @Override
    public void storeProgressPos(final BlockPos blockPos, final BuildingStructureHandler.Stage stage)
    {
        getOwnBuilding().setProgressPos(blockPos, stage);
    }

    @Override
    public Tuple<BlockPos, BuildingStructureHandler.Stage> getProgressPos()
    {
        return getOwnBuilding().getProgress();
    }

    /**
     * Takes the existing workorder, loads the structure and tests the worker order if it is valid.
     */
    @Override
    public IAIState loadRequirements()
    {
        if (!job.hasBlueprint() || structurePlacer == null)
        {
            loadStructure();
            final WorkOrderBuildDecoration wo = job.getWorkOrder();
            if (wo == null)
            {
                Log.getLogger().error(
                  String.format("Worker (%d:%d) ERROR - Starting and missing work order(%d)",
                    worker.getCitizenColonyHandler().getColony().getID(),
                    worker.getCitizenData().getId(), job.getWorkOrderId()), new Exception());
                job.setWorkOrder(null);
                return IDLE;
            }

            if (wo instanceof WorkOrderBuildBuilding)
            {
                final IBuilding building = job.getColony().getBuildingManager().getBuilding(wo.getBuildingLocation());
                if (building == null)
                {
                    Log.getLogger().error(
                      String.format("Worker (%d:%d) ERROR - Starting and missing building(%s)",
                        worker.getCitizenColonyHandler().getColony().getID(), worker.getCitizenData().getId(), wo.getBuildingLocation()), new Exception());
                    return IDLE;
                }

                worker.getCitizenChatHandler().sendLocalizedChat(COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_BUILDSTART, job.getBlueprint().getName());

                //Don't go through the CLEAR stage for repairs and upgrades
                if (building.getBuildingLevel() > 0)
                {
                    wo.setCleared(true);
                }
            }
            else if (!(wo instanceof WorkOrderBuildMiner))
            {
                worker.getCitizenChatHandler().sendLocalizedChat(COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_BUILDSTART, wo.getName());
            }
            return getState();
        }

        if (job.getWorkOrder().isRequested())
        {
            return afterStructureLoading();
        }

        //We need to deal with materials
        requestMaterialsState();
        return getState();
    }

    /**
     * Load the struction into the AI.
     */
    private void loadStructure()
    {
        final WorkOrderBuildDecoration workOrder = job.getWorkOrder();

        if (workOrder == null)
        {
            return;
        }

        final BlockPos pos = workOrder.getBuildingLocation();
        if (workOrder instanceof WorkOrderBuildBuilding && worker.getCitizenColonyHandler().getColony().getBuildingManager().getBuilding(pos) == null)
        {
            Log.getLogger().warn("AbstractBuilding does not exist - removing build request");
            worker.getCitizenColonyHandler().getColony().getWorkManager().removeWorkOrder(workOrder);
            return;
        }

        final int tempRotation = workOrder.getRotation(world);
        final boolean removal = workOrder instanceof WorkOrderBuildRemoval;

        super.loadStructure(workOrder.getStructureName(), tempRotation, pos, workOrder.isMirrored(), removal);
        workOrder.setCleared(false);
        workOrder.setRequested(removal);
    }

    /**
     * State for material requesting.
     */
    private void requestMaterialsState()
    {
        if (MineColonies.getConfig().getServer().builderInfiniteResources.get() || job.getWorkOrder().isRequested() || job.getWorkOrder() instanceof WorkOrderBuildRemoval)
        {
            return;
        }

        final AbstractBuildingStructureBuilder buildingWorker = getOwnBuilding();
        if (requestMaterials())
        {
            job.getWorkOrder().setRequested(true);
        }
        int newQuantity = buildingWorker.getNeededResources().values().stream().mapToInt(ItemStorage::getAmount).sum();
        if (job.getWorkOrder().getAmountOfRes() == 0 || newQuantity > job.getWorkOrder().getAmountOfRes())
        {
            job.getWorkOrder().setAmountOfRes(newQuantity);
        }
    }

    @Override
    public boolean requestMaterials()
    {
        StructurePhasePlacementResult result;
        final WorkerLoadOnlyStructureHandler structure =
          new WorkerLoadOnlyStructureHandler(world, structurePlacer.getB().getWorldPos(), structurePlacer.getB().getBluePrint(), new PlacementSettings(), true, this);
        final StructurePlacer placer = new StructurePlacer(structure);

        if (requestProgress == null)
        {
            final AbstractBuildingStructureBuilder buildingWorker = getOwnBuilding();
            buildingWorker.resetNeededResources();
            requestProgress = NULL_POS;
            requestState = RequestStage.SOLID;
        }

        final RequestStage currState = requestState;
        switch (currState)
        {
            case SOLID:
                result = placer.executeStructureStep(world,
                  null,
                  requestProgress,
                  StructurePlacer.Operation.GET_RES_REQUIREMENTS,
                  () -> placer.getIterator()
                          .increment(DONT_TOUCH_PREDICATE.or((info, pos, handler) -> !info.getBlockInfo().getState().getMaterial().isSolid() || isDecoItem(info.getBlockInfo()
                                                                                                                                                  .getState()
                                                                                                                                                  .getBlock()))),
                  false);
                requestProgress = result.getIteratorPos();

                for (final ItemStack stack : result.getBlockResult().getRequiredItems())
                {
                    getOwnBuilding().addNeededResource(stack, stack.getCount());
                }

                if (result.getBlockResult().getResult() == BlockPlacementResult.Result.FINISHED)
                {
                    requestState = RequestStage.DECO;
                }
                return false;
            case DECO:
                result = placer.executeStructureStep(world,
                  null,
                  requestProgress,
                  StructurePlacer.Operation.GET_RES_REQUIREMENTS,
                  () -> placer.getIterator()
                          .increment(DONT_TOUCH_PREDICATE.or((info, pos, handler) -> info.getBlockInfo().getState().getMaterial().isSolid() && !isDecoItem(info.getBlockInfo()
                                                                                                                                                   .getState()
                                                                                                                                                   .getBlock()))),
                  false);
                requestProgress = result.getIteratorPos();

                for (final ItemStack stack : result.getBlockResult().getRequiredItems())
                {
                    getOwnBuilding().addNeededResource(stack, stack.getCount());
                }

                if (result.getBlockResult().getResult() == BlockPlacementResult.Result.FINISHED)
                {
                    requestState = RequestStage.ENTITIES;
                }
                return false;
            case ENTITIES:
                    result = placer.executeStructureStep(world, null, requestProgress, StructurePlacer.Operation.GET_RES_REQUIREMENTS,
                      () -> placer.getIterator().increment(DONT_TOUCH_PREDICATE.or((info, pos, handler) -> info.getEntities().length == 0)), true);
                    requestProgress = result.getIteratorPos();

                    for (final ItemStack stack : result.getBlockResult().getRequiredItems())
                    {
                        getOwnBuilding().addNeededResource(stack, stack.getCount());
                    }

                if (result.getBlockResult().getResult() == BlockPlacementResult.Result.FINISHED)
                {
                    requestState = RequestStage.SOLID;
                    requestProgress = null;
                    return true;
                }
                return false;
            default:
                return true;
        }
    }

    @Override
    public void registerBlockAsNeeded(final ItemStack stack)
    {
        final int hashCode = stack.hasTag() ? stack.getTag().hashCode() : 0;
        if (getOwnBuilding().getNeededResources().get(stack.getTranslationKey() + "-" + hashCode) == null)
        {
            getOwnBuilding().addNeededResource(stack, 1);
        }
    }

    @Override
    public int getTotalRequiredAmount(final ItemStack deliveredItemStack)
    {
        if (ItemStackUtils.isEmpty(deliveredItemStack))
        {
            return 0;
        }
        final int hashCode = deliveredItemStack.hasTag() ? deliveredItemStack.getTag().hashCode() : 0;
        final BuildingBuilderResource resource = getOwnBuilding().getNeededResources().get(deliveredItemStack.getTranslationKey() + "-" + hashCode);
        if (resource != null)
        {
            return resource.getAmount();
        }

        return super.getTotalRequiredAmount(deliveredItemStack);
    }

    @Override
    public void executeSpecificCompleteActions()
    {
        if (job.getBlueprint() == null && job.hasWorkOrder())
        {
            //fix for bad structures
            job.complete();
        }

        if (job.getBlueprint() == null)
        {
            return;
        }

        final String structureName = job.getBlueprint().getName();
        final WorkOrderBuildDecoration wo = job.getWorkOrder();

        if (wo instanceof WorkOrderBuildBuilding)
        {
            BlockPos position = wo.getBuildingLocation();
            if (getOwnBuilding() instanceof BuildingBuilder && ((BuildingBuilder) getOwnBuilding()).getManualMode())
            {
            	worker.getCitizenChatHandler().sendLocalizedChat(COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_BUILDCOMPLETE_MANUAL, structureName, position.getX(), position.getY(), position.getZ());
            }
            else
            {
                worker.getCitizenChatHandler().sendLocalizedChat(COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_BUILDCOMPLETE, structureName, position.getX(), position.getY(), position.getZ());
            }

            WorkOrderBuild wob = (WorkOrderBuild) wo;
            String buildingName = wo.getStructureName();
            buildingName = buildingName.substring(buildingName.indexOf('/') + 1, buildingName.lastIndexOf('/')) + " " +
                  buildingName.substring(buildingName.lastIndexOf('/') + 1, buildingName.lastIndexOf(String.valueOf(wob.getUpgradeLevel())));
            job.getColony().getEventDescriptionManager().addEventDescription(wob.getUpgradeLevel() > 1 ? new BuildingUpgradedEvent(wo.getBuildingLocation(), buildingName,
                  wob.getUpgradeLevel()) : new BuildingBuiltEvent(wo.getBuildingLocation(), buildingName, wob.getUpgradeLevel()));
        }
        else if (wo instanceof WorkOrderBuildRemoval)
        {
            worker.getCitizenChatHandler().sendLocalizedChat(COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_DECONSTRUCTION_COMPLETE, structureName);

            WorkOrderBuild wob = (WorkOrderBuild) wo;
            String buildingName = wo.getStructureName();
            buildingName = buildingName.substring(buildingName.indexOf('/') + 1, buildingName.lastIndexOf('/')) + " " +
                  buildingName.substring(buildingName.lastIndexOf('/') + 1, buildingName.indexOf(String.valueOf(wob.getUpgradeLevel())));
            job.getColony().getEventDescriptionManager().addEventDescription(new BuildingDeconstructedEvent(wo.getBuildingLocation(), buildingName, wob.getUpgradeLevel()));
        }

        if (wo == null)
        {
            Log.getLogger().error(String.format("Worker (%d:%d) ERROR - Finished, but missing work order(%d)",
              worker.getCitizenColonyHandler().getColony().getID(),
              worker.getCitizenData().getId(),
              job.getWorkOrderId()));
        }
        else
        {
            job.complete();

            if (wo instanceof WorkOrderBuildBuilding)
            {
                final IBuilding building = job.getColony().getBuildingManager().getBuilding(wo.getBuildingLocation());
                if (building == null)
                {
                    Log.getLogger().error(String.format("Builder (%d:%d) ERROR - Finished, but missing building(%s)",
                      worker.getCitizenColonyHandler().getColony().getID(),
                      worker.getCitizenData().getId(),
                      wo.getBuildingLocation()));
                }
                else
                {
                    building.setBuildingLevel(((WorkOrderBuildBuilding) wo).getUpgradeLevel());
                }
            }
            else if (wo instanceof WorkOrderBuildRemoval)
            {
                final IBuilding building = job.getColony().getBuildingManager().getBuilding(wo.getBuildingLocation());
                if (building == null)
                {
                    Log.getLogger().error(String.format("Builder (%d:%d) ERROR - Finished, but missing building(%s)",
                      worker.getCitizenColonyHandler().getColony().getID(),
                      worker.getCitizenData().getId(),
                      wo.getBuildingLocation()));
                }
                else
                {
                    building.setDeconstructed();
                }
            }
        }
        getOwnBuilding().resetNeededResources();
    }

    @Override
    public void reduceNeededResources(final ItemStack stack)
    {
        getOwnBuilding().reduceNeededResource(stack, 1);
    }

    @Override
    protected boolean checkIfCanceled()
    {
        if ((job.getWorkOrder() == null && job.getBlueprint() != null) || (structurePlacer != null && !structurePlacer.getB().hasBluePrint()))
        {
            job.setBlueprint(null);
            if (job.hasWorkOrder())
            {
                job.getColony().getWorkManager().removeWorkOrder(job.getWorkOrderId());
            }
            job.setWorkOrder(null);
            resetCurrentStructure();
            getOwnBuilding().setProgressPos(null, BuildingStructureHandler.Stage.CLEAR);
            return true;
        }
        return job.getWorkOrder() != null && (!WorldUtil.isBlockLoaded(world, job.getWorkOrder().getBuildingLocation())) && getState() != PICK_UP_RESIDUALS;
    }

    @Override
    protected boolean isAlreadyCleared()
    {
        return job.getWorkOrder() != null && job.getWorkOrder().isCleared();
    }

    /**
     * Check how much of a certain stuck is actually required.
     *
     * @param stack the stack to check.
     * @return the new stack with the correct amount.
     */
    @Override
    @Nullable
    public ItemStack getTotalAmount(@Nullable final ItemStack stack)
    {
        if (ItemStackUtils.isEmpty(stack))
        {
            return null;
        }
        final int hashCode = stack.hasTag() ? stack.getTag().hashCode() : 0;
        final AbstractBuildingStructureBuilder buildingWorker = getOwnBuilding();
        BuildingBuilderResource resource = buildingWorker.getNeededResources().get(stack.getTranslationKey() + "-" + hashCode);

        if (resource == null)
        {
            requestMaterials();
            resource = buildingWorker.getNeededResources().get(stack.getTranslationKey() + "-" + hashCode);
        }

        if (resource == null)
        {
            return stack;
        }

        final ItemStack resStack = new ItemStack(resource.getItem(), Math.min(STACKSIZE, resource.getAmount()));
        resStack.setTag(resource.getItemStack().getTag());
        return resStack;
    }

    @Override
    public void handleSpecificCancelActions()
    {
        getOwnBuilding().getColony().getWorkManager().removeWorkOrder(job.getWorkOrderId());
        job.setWorkOrder(null);
    }
}
