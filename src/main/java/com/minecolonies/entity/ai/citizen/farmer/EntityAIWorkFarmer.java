package com.minecolonies.entity.ai.citizen.farmer;

import com.minecolonies.colony.buildings.BuildingFarmer;
import com.minecolonies.colony.jobs.JobFarmer;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.entity.ai.util.AIState;
import com.minecolonies.entity.ai.util.AITarget;
import com.minecolonies.util.BlockPosUtil;
import com.minecolonies.util.InventoryUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.minecolonies.entity.ai.util.AIState.*;

/**
 * Farmer AI class
 * Created: December 20, 2014
 */
public class EntityAIWorkFarmer extends AbstractEntityAIInteract<JobFarmer>
{

    /**
     * Constructor for the Fisherman.
     * Defines the tasks the fisherman executes.
     *
     * @param job a fisherman job to use.
     */
    public EntityAIWorkFarmer(JobFarmer job)
    {
        super(job);
        super.registerTargets(
                new AITarget(IDLE, () -> START_WORKING),
                new AITarget(START_WORKING, this::startWorkingAtOwnBuilding),
                new AITarget(PREPARING, this::prepareForFishing)
                //new AITarget(FISHERMAN_CHECK_WATER, this::tryDifferentAngles),
                //new AITarget(FISHERMAN_SEARCHING_WATER, this::findWater),
                //new AITarget(FISHERMAN_WALKING_TO_WATER, this::getToWater),
                //new AITarget(FISHERMAN_START_FISHING, this::doFishing)
        );
        worker.setSkillModifier(2*worker.getCitizenData().getEndurance() + worker.getCitizenData().getCharisma());
    }

    /**
     * Redirects the fisherman to his building.
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


    //todo request the stuff for the fields
    /**
     * Prepares the fisherman for fishing and
     * requests fishingRod and checks if the fisherman already had found a pond.
     * @return the next AIState
     */
    private AIState prepareForFishing()
    {
        if (checkOrRequestItems(new ItemStack(Items.fishing_rod)))
        {
            return getState();
        }
        //if (job.getWater() == null)
        {
            return FISHERMAN_SEARCHING_WATER;
        }
        //return FISHERMAN_WALKING_TO_WATER;
    }

    //todo after crop count.
    /**
     * After the fisherman has caught 10 fishes -> dump inventory.
     * @return true if the inventory should be dumped
     */
    @Override
    protected boolean wantInventoryDumped()
    {
        /*if (fishesCaught > MAX_FISHES_IN_INV)
        {
            fishesCaught = 0;
            job.setWater(null);

            return true;
        }*/
        return false;
    }

    /**
     * Returns the fisherman's work building.
     * @return building instance
     */
    @Override
    protected BuildingFarmer getOwnBuilding()
    {
        return (BuildingFarmer) worker.getWorkBuilding();
    }

    //todo set the needed items
    /**
     * Override this method if you want to keep some items in inventory.
     * When the inventory is full, everything get's dumped into the building chest.
     * But you can use this method to hold some stacks back.
     *
     * @param stack the stack to decide on
     * @return true if the stack should remain in inventory
     */
    @Override
    protected boolean neededForWorker(ItemStack stack)
    {
        return isStackRod(stack);
    }


    //todo check for hoe and shovel
    /**
     * Checks if a given stack equals a fishingRod.
     * @param stack the stack to decide on
     * @return if the stack matches
     */
    private static boolean isStackRod(ItemStack stack)
    {
        return stack != null && stack.getItem().equals(Items.fishing_rod);
    }

    //todo walk to field
    /**
     * Let's the fisherman walk to the water if the water object in his job class already has been filled.
     *
     * @return true if the fisherman has arrived at the water
     */
    private boolean walkToWater()
    {
        return true /*!(job.getWater() == null || job.getWater() == null) && walkToBlock(job.getWater());*/;
    }

    //todo equip shovel/hoe
    /**
     * Sets the rod as held item.
     */
    private void equipRod()
    {
        worker.setHeldItem(getRodSlot());
    }

    //todo return shovel/hoe slot
    /**
     * Get's the slot in which the rod is in.
     * @return slot number
     */
    private int getRodSlot()
    {
        return -1;//return InventoryUtils.getFirstSlotContainingTool(getInventory(), TOOL_TYPE_ROD);
    }



    /**
     * Returns the fisherman's worker instance. Called from outside this class.
     * @return citizen object
     */
    public EntityCitizen getCitizen()
    {
        return worker;
    }

}