package com.minecolonies.coremod.entity.ai.citizen.builder;

import com.ldtteam.structurize.placement.StructurePlacer;
import com.ldtteam.structurize.util.BlockUtils;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.workorders.IWorkOrder;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.modules.settings.BuilderModeSetting;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.coremod.colony.jobs.JobBuilder;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuild;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildBuilding;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildDecoration;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildRemoval;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIStructureWithWorkOrder;
import com.minecolonies.coremod.entity.ai.util.BuildingStructureHandler;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * AI class for the builder. Manages building and repairing buildings.
 */
public class EntityAIStructureBuilder extends AbstractEntityAIStructureWithWorkOrder<JobBuilder, BuildingBuilder>
{
    /**
     * Speed buff at 0 depth level.
     */
    private static final double SPEED_BUFF_0 = 0.5;

    /**
     * After how many actions should the builder dump his inventory.
     */
    private static final int ACTIONS_UNTIL_DUMP = 4096;

    /**
     * Building level to purge mobs at the build site.
     */
    private static final int LEVEL_TO_PURGE_MOBS = 4;

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
          new AITarget(START_WORKING, this::checkForWorkOrder, this::startWorkingAtOwnBuilding, 100)
        );
        worker.setCanPickUpLoot(true);
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
        if (job.getWorkOrder().getIteratorType().isEmpty())
        {
            final String mode = BuilderModeSetting.getActualValue(getOwnBuilding());
            job.getWorkOrder().setIteratorType(mode);
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
                WorldUtil.getEntitiesWithinBuilding(world, Monster.class, building, null).forEach(e -> e.remove(Entity.RemovalReason.DISCARDED));
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
    protected boolean mineBlock(@NotNull final BlockPos blockToMine, @NotNull final BlockPos safeStand)
    {
        return mineBlock(blockToMine, safeStand, true, !IColonyManager.getInstance().getCompatibilityManager().isOre(world.getBlockState(blockToMine)), null);
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
    public int getBlockMiningDelay(@NotNull final BlockState state, @NotNull final BlockPos pos)
    {
        return (int) (super.getBlockMiningDelay(state, pos) * SPEED_BUFF_0);
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
