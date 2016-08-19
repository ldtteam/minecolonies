package com.minecolonies.entity.ai.citizen.farmer;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.Field;
import com.minecolonies.colony.buildings.BuildingFarmer;
import com.minecolonies.colony.jobs.JobFarmer;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.entity.ai.util.AIState;
import com.minecolonies.entity.ai.util.AITarget;
import com.minecolonies.util.InventoryUtils;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import java.util.ArrayList;

import static com.minecolonies.entity.ai.util.AIState.*;

/**
 * Farmer AI class
 * Created: December 20, 2014
 */
public class EntityAIWorkFarmer extends AbstractEntityAIInteract<JobFarmer>
{
    /**
     * The tool the farmer always needs
     */
    private static final String TOOL_TYPE_HOE           = "hoe";

    /**
     * Return to chest after half a stack
     */
    private static final int MAX_BLOCKS_HARVESTED = 32;

    /**
     * The list of the fields the farmer manages.
     */
    private ArrayList<Field> farmerFields = new ArrayList<>();

    /**
     * Constructor for the Farmer.
     * Defines the tasks the Farmer executes.
     *
     * @param job a farmer job to use.
     */
    public EntityAIWorkFarmer(JobFarmer job)
    {
        super(job);
        super.registerTargets(
                new AITarget(IDLE, () -> START_WORKING),
                new AITarget(START_WORKING, this::startWorkingAtOwnBuilding),
                new AITarget(PREPARING, this::prepareForFarming)
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

    /**
     * Prepares the farmer for farming and
     * requests the tools and checks if the farmer has sufficient fields.
     * @return the next AIState
     */
    private AIState prepareForFarming()
    {
        if(farmerFields.size() < getOwnBuilding().getBuildingLevel())
        {
            searchAndAddFields();
        }

        if(farmerFields.isEmpty())
        {
            chatSpamFilter.talkWithoutSpam("entity.farmer.noFreeFields");
            return AIState.PREPARING;
        }

        if (checkForHoe())
        {
            return getState();
        }

        //if any field needs work -> work on them

        return AIState.FARMER_CHECK_FIELDS;
    }

    /**
     * Searches and adds a field that has not been taken yet for the farmer and then adds it to the list.
     */
    private void searchAndAddFields()
    {
        Colony colony = worker.getColony();
        if(colony != null)
        {
            Field newField = colony.getFreeField();

            if(newField!=null)
            {
                farmerFields.add(newField);
            }
        }
    }


    /**
     * Called to check when the InventoryShouldBeDumped
     * @return true if the conditions are met
     */
    @Override
    protected boolean wantInventoryDumped()
    {
        if (getBlocksMined() > MAX_BLOCKS_HARVESTED)
        {
            clearBlocksMined();
            return true;
        }
        return false;
    }

    /**
     * Returns the farmer's work building.
     * @return building instance
     */
    @Override
    protected BuildingFarmer getOwnBuilding()
    {
        return (BuildingFarmer) worker.getWorkBuilding();
    }

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
        return isStackHoe(stack);
    }

    /**
     * Checks if a given stack equals a fishingRod.
     * @param stack the stack to decide on
     * @return if the stack matches
     */
    private static boolean isStackHoe(ItemStack stack)
    {
        return stack != null && stack.getItem().getToolClasses(stack).contains(TOOL_TYPE_HOE);
    }

    /**
     * Sets the hoe as held item.
     */
    private void equipHoe()
    {
        worker.setHeldItem(getHoeSlot());
    }

    /**
     * Get's the slot in which the hoe is in.
     * @return slot number
     */
    private int getHoeSlot()
    {
        return InventoryUtils.getFirstSlotContainingTool(getInventory(), TOOL_TYPE_HOE);
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
