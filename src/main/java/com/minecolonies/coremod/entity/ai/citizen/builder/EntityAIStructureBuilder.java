package com.minecolonies.coremod.entity.ai.citizen.builder;

import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.jobs.JobBuilder;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuild;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildDecoration;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIStructure;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import com.minecolonies.coremod.util.LanguageHandler;
import com.minecolonies.coremod.util.Log;
import com.minecolonies.coremod.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.coremod.entity.ai.util.AIState.*;

/**
 * AI class for the builder.
 * Manages building and repairing buildings.
 */
public class EntityAIStructureBuilder extends AbstractEntityAIStructure<JobBuilder>
{
    /**
     * How often should intelligence factor into the builders skill modifier.
     */
    private static final int    INTELLIGENCE_MULTIPLIER       = 2;

    /**
     * How often should strength factor into the builders skill modifier.
     */
    private static final int    STRENGTH_MULTIPLIER           = 1;

    /**
     * After how many actions should the builder dump his inventory.
     */
    private static final int    ACTIONS_UNTIL_DUMP            = 1024;



    /**
     * Position where the Builders constructs from.
     */
    @Nullable
    private              BlockPos workFrom       = null;

    /**
     * Initialize the builder and add all his tasks.
     *
     * @param job the job he has.
     */
    public EntityAIStructureBuilder(@NotNull final JobBuilder job)
    {
        super(job);
        super.registerTargets(
          /**
           * If IDLE - switch to start working.
           */
          new AITarget(IDLE, START_WORKING),
          new AITarget(this::checkIfExecute, this::getState),
          new AITarget(START_WORKING, this::startWorkingAtOwnBuilding)
        );
        worker.setSkillModifier(INTELLIGENCE_MULTIPLIER * worker.getCitizenData().getIntelligence()
                                  + STRENGTH_MULTIPLIER * worker.getCitizenData().getStrength());
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Block getSolidSubstitution(BlockPos location)
    {
        final IBlockState filler = world.getBiome(location).fillerBlock;
        if(filler.getBlock() instanceof BlockFalling)
        {
            return Blocks.DIRT;
        }
        return filler.getBlock();
    }

    private boolean checkIfExecute()
    {
        setDelay(1);

        if (!job.hasWorkOrder())
        {
            return true;
        }

        final WorkOrderBuild wo = job.getWorkOrder();

        if (job.getColony().getBuilding(wo.getBuildingLocation()) == null && !(wo instanceof WorkOrderBuildDecoration))
        {
            job.complete();
            return true;
        }

        if (!job.hasStructure())
        {
            initiate();
        }

        return false;
    }

    private void initiate()
    {
        if (!job.hasStructure())
        {
            workFrom = null;
            loadStructure();

            final WorkOrderBuild wo = job.getWorkOrder();
            if (wo == null)
            {
                Log.getLogger().error(
                  String.format("Builder (%d:%d) ERROR - Starting and missing work order(%d)",
                    worker.getColony().getID(),
                    worker.getCitizenData().getId(), job.getWorkOrderId()));
                return;
            }

            if (wo instanceof WorkOrderBuildDecoration)
            {
                LanguageHandler.sendPlayersLocalizedMessage(worker.getColony().getMessageEntityPlayers(),
                  "entity.builder.messageBuildStart",
                  job.getStructure().getName());
            }
            else
            {
                final AbstractBuilding building = job.getColony().getBuilding(wo.getBuildingLocation());
                if (building == null)
                {
                    Log.getLogger().error(
                      String.format("Builder (%d:%d) ERROR - Starting and missing building(%s)",
                        worker.getColony().getID(), worker.getCitizenData().getId(), wo.getBuildingLocation()));
                    return;
                }

                LanguageHandler.sendPlayersLocalizedMessage(worker.getColony().getMessageEntityPlayers(),
                  "entity.builder.messageBuildStart",
                  job.getStructure().getName());

                //Don't go through the CLEAR stage for repairs and upgrades
                if (building.getBuildingLevel() > 0)
                {
                    wo.setCleared(true);
                }
            }
        }
    }

    private AIState startWorkingAtOwnBuilding()
    {
        if (walkToBuilding())
        {
            return getState();
        }
        return START_BUILDING;
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

    /**
     * Can be overriden by implementations to specify which tools are useful for the worker.
     * When dumping he will keep these.
     *
     * @param stack the stack to decide on
     * @return if should be kept or not.
     */
    @Override
    protected boolean neededForWorker(@Nullable final ItemStack stack)
    {
        return Utils.isMiningTool(stack);
    }
}
