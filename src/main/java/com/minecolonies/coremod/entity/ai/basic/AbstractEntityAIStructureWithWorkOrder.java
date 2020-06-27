package com.minecolonies.coremod.entity.ai.basic;

import com.ldtteam.structurize.placement.BlockPlacementResult;
import com.ldtteam.structurize.placement.StructurePhasePlacementResult;
import com.ldtteam.structurize.placement.StructurePlacer;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.LoadOnlyStructureHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.coremod.colony.buildings.utils.BuildingBuilderResource;
import com.minecolonies.coremod.colony.jobs.AbstractJobStructure;
import com.minecolonies.coremod.colony.workorders.*;
import com.minecolonies.coremod.entity.ai.util.BuildingStructureHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.ldtteam.structurize.placement.BlueprintIterator.NULL_POS;
import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.PICK_UP_RESIDUALS;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_BUILDCOMPLETE;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_BUILDSTART;


/**
 * AI class for the builder.
 * Manages building and repairing buildings.
 */
public abstract class AbstractEntityAIStructureWithWorkOrder<J extends AbstractJobStructure<?, J>, B extends AbstractBuildingStructureBuilder> extends AbstractEntityAIStructure<J, B>
{
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
    public void initiate()
    {
        if (!job.hasBlueprint())
        {
            loadStructure();
            final WorkOrderBuildDecoration wo = job.getWorkOrder();
            if (wo == null)
            {
                Log.getLogger().error(
                  String.format("Worker (%d:%d) ERROR - Starting and missing work order(%d)",
                    worker.getCitizenColonyHandler().getColony().getID(),
                    worker.getCitizenData().getId(), job.getWorkOrderId()), new Exception());
                return;
            }

            if (wo instanceof WorkOrderBuildBuilding)
            {
                final IBuilding building = job.getColony().getBuildingManager().getBuilding(wo.getBuildingLocation());
                if (building == null)
                {
                    Log.getLogger().error(
                      String.format("Worker (%d:%d) ERROR - Starting and missing building(%s)",
                        worker.getCitizenColonyHandler().getColony().getID(), worker.getCitizenData().getId(), wo.getBuildingLocation()), new Exception());
                    return;
                }

                worker.getCitizenChatHandler().sendLocalizedChat(COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_BUILDSTART, job.getBlueprint().getName());

                //Don't go through the CLEAR stage for repairs and upgrades
                if (building.getBuildingLevel() > 0)
                {
                    wo.setCleared(true);
                }
            }
            else if(!(wo instanceof WorkOrderBuildMiner))
            {
                worker.getCitizenChatHandler().sendLocalizedChat(COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_BUILDSTART, wo.getName());
            }
        }
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
        workOrder.setRequested(false);

        //We need to deal with materials
        requestMaterialsState();
    }

    /**
     * State for material requesting.
     */
    private void requestMaterialsState()
    {
        if (MineColonies.getConfig().getCommon().builderInfiniteResources.get() || job.getWorkOrder().isRequested() || job.getWorkOrder() instanceof WorkOrderBuildRemoval)
        {
            return;
        }
        requestMaterials();

        final AbstractBuildingStructureBuilder buildingWorker = getOwnBuilding();
        job.getWorkOrder().setRequested(true);

        if (job.getWorkOrder().getAmountOfRes() == 0)
        {
            job.getWorkOrder().setAmountOfRes(buildingWorker.getNeededResources().values().stream()
                                                .mapToInt(ItemStorage::getAmount).sum());
        }
    }

    /**
     * Iterates through all the required resources and stores them in the building.
     * Suppressing Sonar Rule Squid:S135
     * The rule thinks we should have less continue and breaks.
     * But in this case the rule does not apply because code would become unreadable and uneffective without.
     */
    private void requestMaterials()
    {
        final AbstractBuildingStructureBuilder buildingWorker = getOwnBuilding();
        buildingWorker.resetNeededResources();

        StructurePhasePlacementResult result;
        final LoadOnlyStructureHandler structure = new LoadOnlyStructureHandler(world, structurePlacer.getB().getWorldPos(), structurePlacer.getB().getBluePrint(), new PlacementSettings(), true);
        final StructurePlacer placer = new StructurePlacer(structure);
        BlockPos progressPos = NULL_POS;

        do
        {
            result = placer.executeStructureStep(world, null, progressPos, StructurePlacer.Operation.GET_RES_REQUIREMENTS,
              () -> placer.getIterator().increment(DONT_TOUCH_PREDICATE.and((info, pos, handler) -> false)), true);
            progressPos = result.getIteratorPos();

            for (final ItemStack stack : result.getBlockResult().getRequiredItems())
            {
                getOwnBuilding().addNeededResource(stack, stack.getCount());
            }

        }
        while (result.getBlockResult().getResult() != BlockPlacementResult.Result.FINISHED);
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
            worker.getCitizenChatHandler().sendLocalizedChat(COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_BUILDCOMPLETE, structureName);
        }
        else if (wo instanceof WorkOrderBuildRemoval)
        {
            worker.getCitizenChatHandler().sendLocalizedChat(COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_BUILDCOMPLETE, structureName);
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
        return job.getWorkOrder() != null && (!world.isBlockPresent(job.getWorkOrder().getBuildingLocation())) && getState() != PICK_UP_RESIDUALS;
    }

    @Override
    protected boolean isAlreadyCleared()
    {
        return job.getWorkOrder() != null && job.getWorkOrder().isCleared();
    }

    @Override
    protected void onStartWithoutStructure()
    {
        if (job.getWorkOrder() != null)
        {
            loadStructure();
        }
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

        if(resource == null)
        {
            requestMaterials();
            resource = buildingWorker.getNeededResources().get(stack.getTranslationKey() + "-" + hashCode);
        }

        if(resource == null)
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
