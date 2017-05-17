package com.minecolonies.coremod.entity.ai.citizen.baker;

import com.minecolonies.blockout.Log;
import com.minecolonies.coremod.colony.buildings.BuildingBaker;
import com.minecolonies.coremod.colony.jobs.JobBaker;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAISkill;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.coremod.entity.ai.util.AIState.*;

/**
 * Fisherman AI class.
 * <p>
 * A fisherman has some ponds where
 * he randomly selects one and fishes there.
 * <p>
 * To keep it immersive he chooses his place at random around the pond.
 */
public class EntityAIWorkBaker extends AbstractEntityAISkill<JobBaker>
{
    /**
     * How often should intelligence factor into the fisherman's skill modifier.
     */
    private static final int INTELLIGENCE_MULTIPLIER = 2;

    /**
     * How often should dexterity factor into the fisherman's skill modifier.
     */
    private static final int DEXTERITY_MULTIPLIER = 1;

    /**
     * Constructor for the Fisherman.
     * Defines the tasks the fisherman executes.
     *
     * @param job a fisherman job to use.
     */
    public EntityAIWorkBaker(@NotNull final JobBaker job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING),
          new AITarget(START_WORKING, this::startWorkingAtOwnBuilding),
          new AITarget(PREPARING, this::prepareForBaking),
          new AITarget(BAKER_KNEADING, this::kneadTheDough),
          new AITarget(BAKER_BAKING, this::BakeBread)
        );
        worker.setSkillModifier(
          INTELLIGENCE_MULTIPLIER * worker.getCitizenData().getIntelligence()
            + DEXTERITY_MULTIPLIER * worker.getCitizenData().getDexterity());
        worker.setCanPickUpLoot(true);
    }

    private BlockPos SearchOven(BlockPos HutBlock)
    {
        for (int xn = HutBlock.getX()-9; xn <= HutBlock.getX()+9; xn++)
            for (int yn = HutBlock.getY(); yn <= HutBlock.getY()+1; yn++)
                for (int zn = HutBlock.getZ()-9; zn <= HutBlock.getZ()+9; zn++)
                {
                    BlockPos block = new BlockPos(xn, yn, zn);
                    if (world.getBlockState(block).getBlock() == Blocks.FURNACE)
                    {
                        return block;
                    }
                }
        return null;
    }


    /**
     * Prepares the baker for baking and requests ingredients.
     *
     * @return the next AIState
     */
    private AIState kneadTheDough()
    {
       walkToBlock(getOwnBuilding().getLocation());
       int i = 0;
       while (i <= 10)
       {
           if (hasNotDelayed(100))
           {
               worker.hitBlockWithToolInHand(getOwnBuilding().getLocation());
               i++;
           }
       }
       return BAKER_BAKING;
    }

    /**
     * Prepares the baker for baking and requests ingredients.
     *
     * @return the next AIState
     */
    private AIState BakeBread()
    {
        BlockPos oven = SearchOven(getOwnBuilding().getLocation());
        walkToBlock(oven);
        worker.hitBlockWithToolInHand(oven);
        return PREPARING;
    }

    /**
     * Prepares the baker for baking and requests ingredients.
     *
     * @return the next AIState
     */
    private AIState prepareForBaking()
    {
        Log.getLogger().info("SUCK MY DICK");

        if(true)
        {
            return BAKER_KNEADING;
        }
        return getState();
    }



    /**
     * Redirects the fisherman to his building.
     *
     * @return the next state.
     */
    private AIState startWorkingAtOwnBuilding()
    {
        if (walkToBuilding())
        {
            return getState();
        }
        return PREPARING;
    }

    /**
     * Returns the fisherman's work building.
     *
     * @return building instance
     */
    @Override
    protected BuildingBaker getOwnBuilding()
    {
        return (BuildingBaker) worker.getWorkBuilding();
    }

    /**
     * Returns the fisherman's worker instance. Called from outside this class.
     *
     * @return citizen object.
     */
    @Nullable
    public EntityCitizen getCitizen()
    {
        return worker;
    }
}
