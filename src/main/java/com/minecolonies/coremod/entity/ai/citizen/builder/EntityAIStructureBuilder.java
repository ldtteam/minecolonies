package com.minecolonies.coremod.entity.ai.citizen.builder;

import com.ldtteam.structurize.placement.StructurePlacer;
import com.ldtteam.structurize.util.BlockUtils;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.ISettingsModule;
import com.minecolonies.api.colony.workorders.IWorkOrder;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.util.*;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.modules.BuildingResourcesModule;
import com.minecolonies.coremod.colony.buildings.utils.BuilderBucket;
import com.minecolonies.coremod.colony.buildings.utils.BuildingBuilderResource;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.coremod.colony.jobs.JobBuilder;
import com.minecolonies.coremod.colony.workorders.*;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIStructureWithWorkOrder;
import com.minecolonies.coremod.entity.ai.util.BuildingStructureHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.CitizenConstants.MIN_OPEN_SLOTS;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_BUILDCOMPLETE;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_BUILDCOMPLETE_MANUAL;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_DECOCOMPLETE;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_DECOCOMPLETE_MANUAL;

/**
 * AI class for the builder. Manages building and repairing buildings.
 */
public class EntityAIStructureBuilder extends AbstractEntityAIStructureWithWorkOrder<JobBuilder, BuildingBuilder>
{
    /**
     * over this y level the builder will be faster.
     */
    private static final int DEPTH_LEVEL_0 = 60;

    /**
     * At this y level the builder will be slower.
     */
    private static final int DEPTH_LEVEL_1 = 30;

    /**
     * At this y level the builder will be way slower..
     */
    private static final int DEPTH_LEVEL_2 = 15;

    /**
     * Speed buff at 0 depth level.
     */
    private static final double SPEED_BUFF_0 = 0.5;

    /**
     * Speed buff at first depth level.
     */
    private static final int SPEED_BUFF_1 = 2;

    /**
     * Speed buff at second depth level.
     */
    private static final int SPEED_BUFF_2 = 4;

    /**
     * After how many actions should the builder dump his inventory.
     */
    private static final int ACTIONS_UNTIL_DUMP = 4096;

    /**
     * Building level to purge mobs at the build site.
     */
    private static final int LEVEL_TO_PURGE_MOBS = 4;

    /**
     * The id in the list of the last picked up item.
     */
    private int pickUpCount = 0;

    /**
     * Initialize the builder and add all his tasks.
     *
     * @param job the job he has.
     */
    public EntityAIStructureBuilder(@NotNull final JobBuilder job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING, 100),
          new AITarget(START_WORKING, this::checkForWorkOrder, this::startWorkingAtOwnBuilding, 100),
          new AITarget(PICK_UP, this::pickUpMaterial, 5)
        );
        worker.setCanPickUpLoot(true);
    }

    @Override
    public IAIState getStateAfterPickUp()
    {
        return PICK_UP;
    }

    @Override
    public int getBreakSpeedLevel()
    {
        return getSecondarySkillLevel();
    }

    @Override
    public int getPlaceSpeedLevel()
    {
        return getPrimarySkillLevel();
    }


    /**
     * State to pick up material before going back to work.
     *
     * @return the next state to go to.
     */
    public IAIState pickUpMaterial()
    {
        if (structurePlacer == null || !structurePlacer.getB().hasBluePrint())
        {
            return IDLE;
        }
        
        if (structurePlacer.getB().getStage() == null || structurePlacer.getB().getStage() == BuildingStructureHandler.Stage.CLEAR)
        {
            pickUpCount = 0;
            return START_WORKING;
        }

        final BuildingBuilder building = getOwnBuilding();
        final List<Tuple<Predicate<ItemStack>, Integer>> neededItemsList = new ArrayList<>();

        final BuilderBucket neededRessourcesMap = building.getRequiredResources();
        final BuildingResourcesModule module = getOwnBuilding().getFirstModuleOccurance(BuildingResourcesModule.class);
        if (neededRessourcesMap != null)
        {
            for (final Map.Entry<String, Integer> entry : neededRessourcesMap.getResourceMap().entrySet())
            {
                final BuildingBuilderResource res = module.getResourceFromIdentifier(entry.getKey());
                if (res != null)
                {
                    int amount = entry.getValue();
                    neededItemsList.add(new Tuple<>(itemstack -> ItemStackUtils.compareItemStacksIgnoreStackSize(res.getItemStack(), itemstack, true, true), amount));
                }
            }
        }

        if (neededItemsList.size() <= pickUpCount || InventoryUtils.openSlotCount(worker.getInventoryCitizen()) <= MIN_OPEN_SLOTS)
        {
            getOwnBuilding().checkOrRequestBucket(getOwnBuilding().getRequiredResources(), worker.getCitizenData(), true);
            getOwnBuilding().checkOrRequestBucket(getOwnBuilding().getNextBucket(), worker.getCitizenData(), false);
            pickUpCount = 0;
            return START_WORKING;
        }

        needsCurrently = neededItemsList.get(pickUpCount);
        pickUpCount++;

        if (InventoryUtils.hasItemInProvider(building.getTileEntity(), needsCurrently.getA()))
        {
            return GATHERING_REQUIRED_MATERIALS;
        }

        return pickUpMaterial();
    }

    @Override
    public Class<BuildingBuilder> getExpectedBuildingClass()
    {
        return BuildingBuilder.class;
    }

    /**
     * Checks if we got a valid workorder.
     *
     * @return true if we got a workorder to work with
     */
    private boolean checkForWorkOrder()
    {
        if (!job.hasWorkOrder())
        {
            getOwnBuilding().searchWorkOrder();
            getOwnBuilding().setProgressPos(null, BuildingStructureHandler.Stage.CLEAR);
            return false;
        }

        final WorkOrderBuildDecoration wo = job.getWorkOrder();

        if (wo == null)
        {
            job.setWorkOrder(null);
            getOwnBuilding().setProgressPos(null, null);
            return false;
        }

        final IBuilding building = job.getColony().getBuildingManager().getBuilding(wo.getSchematicLocation());
        if (building == null && wo instanceof WorkOrderBuild && !(wo instanceof WorkOrderBuildRemoval))
        {
            job.complete();
            return false;
        }

        return true;
    }

    @Override
    public void setStructurePlacer(final BuildingStructureHandler<JobBuilder, BuildingBuilder> structure)
    {
        if (job.getWorkOrder().getIteratorType().isEmpty() && getOwnBuilding().hasModule(ISettingsModule.class) && getOwnBuilding().getSetting(BuildingBuilder.BUILDING_MODE) != null)
        {
            job.getWorkOrder().setIteratorType(getOwnBuilding().getSetting(BuildingBuilder.BUILDING_MODE).getValue());
        }
        structurePlacer = new Tuple<>(new StructurePlacer(structure, job.getWorkOrder().getIteratorType()), structure);
    }

    @Override
    public boolean isAfterDumpPickupAllowed()
    {
        return !checkForWorkOrder();
    }

    private IAIState startWorkingAtOwnBuilding()
    {
        if (walkToBuilding())
        {
            return getState();
        }
        return LOAD_STRUCTURE;
    }

    /**
     * Kill all mobs at the building site.
     */
    private void killMobs()
    {
        if (getOwnBuilding().getBuildingLevel() >= LEVEL_TO_PURGE_MOBS && job.getWorkOrder() instanceof WorkOrderBuildBuilding)
        {
            final BlockPos buildingPos = job.getWorkOrder().getSchematicLocation();
            final IBuilding building = worker.getCitizenColonyHandler().getColony().getBuildingManager().getBuilding(buildingPos);
            if (building != null)
            {
                WorldUtil.getEntitiesWithinBuilding(world, MonsterEntity.class, building, null).forEach(Entity::remove);
            }
        }
    }

    @Override
    public void checkForExtraBuildingActions()
    {
        if (!getOwnBuilding().hasPurgedMobsToday())
        {
            killMobs();
            getOwnBuilding().setPurgedMobsToday(true);
        }
    }

    @Override
    public IAIState afterRequestPickUp()
    {
        return INVENTORY_FULL;
    }

    @Override
    public IAIState afterDump()
    {
        return PICK_UP;
    }

    @Override
    public boolean walkToConstructionSite(final BlockPos currentBlock)
    {
        if (workFrom == null)
        {
            workFrom = findRandomPositionToWalkTo(5, currentBlock);
            if (workFrom == null && pathBackupFactor > 10)
            {
                workFrom = worker.blockPosition();
            }
            return false;
        }

        if (walkToBlock(workFrom))
        {
            return false;
        }

        if (BlockPosUtil.getDistance2D(worker.blockPosition(), currentBlock) > 5 + 5 * pathBackupFactor)
        {
            workFrom = null;
            return false;
        }

        if (pathBackupFactor > 1)
        {
            pathBackupFactor--;
        }

        return true;
    }

    @Override
    public boolean shallReplaceSolidSubstitutionBlock(final Block worldBlock, final BlockState worldMetadata)
    {
        return false;
    }

    @Override
    public BlockState getSolidSubstitution(@NotNull final BlockPos location)
    {
        return BlockUtils.getSubstitutionBlockAtWorld(world, location).getBlockState();
    }

    @Override
    public int getBlockMiningDelay(@NotNull final BlockState state, @NotNull final BlockPos pos)
    {
        final int initialDelay = super.getBlockMiningDelay(state, pos);

        if (pos.getY() > DEPTH_LEVEL_0 || !MineColonies.getConfig().getServer().restrictBuilderUnderground.get())
        {
            return (int) (initialDelay * SPEED_BUFF_0);
        }

        if (pos.getY() > DEPTH_LEVEL_1)
        {
            return initialDelay;
        }

        if (pos.getY() < DEPTH_LEVEL_2)
        {
            return initialDelay * SPEED_BUFF_2;
        }
        return initialDelay * SPEED_BUFF_1;
    }

    /**
     * Calculates after how many actions the ai should dump it's inventory.
     *
     * @return the number of actions done before item dump.
     */
    @Override
    protected int getActionsDoneUntilDumping()
    {
        return ACTIONS_UNTIL_DUMP;
    }

    @Override
    protected void sendCompletionMessage(final WorkOrderBuildDecoration wo)
    {
        super.sendCompletionMessage(wo);

        final BlockPos position = wo.getSchematicLocation();
        if (getOwnBuilding().getManualMode())
        {
            boolean hasInQueue = false;
            for (final IWorkOrder workorder : getOwnBuilding().getColony().getWorkManager().getWorkOrders().values())
            {
                if (workorder.getID() != wo.getID() && workorder.isClaimedBy(worker.getCitizenData()))
                {
                    hasInQueue = true;
                }
            }

            if (!hasInQueue)
            {
                if (wo instanceof WorkOrderBuildBuilding)
                {
                    worker.getCitizenChatHandler().sendLocalizedChat(
                      COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_BUILDCOMPLETE_MANUAL,
                      wo.getDisplayName(),
                      position.getX(),
                      position.getY(),
                      position.getZ());
                }
                else
                {
                    worker.getCitizenChatHandler().sendLocalizedChat(
                      COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_DECOCOMPLETE_MANUAL,
                      wo.getDisplayName(),
                      position.getX(),
                      position.getY(),
                      position.getZ());
                }
                return;
            }
        }

        if (wo instanceof WorkOrderBuildBuilding)
        {
            worker.getCitizenChatHandler().sendLocalizedChat(COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_BUILDCOMPLETE, wo.getDisplayName(), position.getX(), position.getY(), position.getZ());
        }
        else
        {
            worker.getCitizenChatHandler().sendLocalizedChat(
              COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_DECOCOMPLETE,
              wo.getDisplayName(),
              position.getX(),
              position.getY(),
              position.getZ());
        }
    }
}
