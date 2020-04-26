package com.minecolonies.coremod.entity.ai.citizen.quarryMiner;

import com.minecolonies.coremod.colony.jobs.JobQuarryMiner;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIStructureWithWorkOrder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Class which handles the miner behaviour.
 */
public class EntityAIStructureQuarryMiner extends AbstractEntityAIStructureWithWorkOrder<JobQuarryMiner>
{
    /**
     * Constructor for the Miner.
     * Defines the tasks the miner executes.
     *
     * @param job a fisherman job to use.
     */
    public EntityAIStructureQuarryMiner(@NotNull final JobQuarryMiner job)
    {
        super(job);
        super.registerTargets(
          /*
           * If IDLE - switch to start working.
           */
          //new AITarget(IDLE, START_WORKING, 1)
        );
        worker.setCanPickUpLoot(true);
    }

    @Override
    protected boolean mineBlock(
      @NotNull final BlockPos blockToMine, @NotNull final BlockPos safeStand, final boolean damageTool, final boolean getDrops, final Runnable blockBreakAction)
    {
        return super.mineBlock(blockToMine, safeStand, damageTool, getDrops, blockBreakAction);
    }

    @Override
    protected void triggerMinedBlock(@NotNull final BlockState blockToMine)
    {
        super.triggerMinedBlock(blockToMine);
    }

    @Override
    public boolean shallReplaceSolidSubstitutionBlock(final Block worldBlock, final BlockState worldMetadata)
    {
        return false;
    }

    @Override
    public BlockState getSolidSubstitution(final BlockPos location)
    {
        return null;
    }
}
