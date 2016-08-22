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
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

import static com.minecolonies.colony.Field.FieldStage.*;
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
                new AITarget(PREPARING, this::prepareForFarming),
                new AITarget(FARMER_CHECK_FIELDS, this::checkFields),
                new AITarget(FARMER_HOE, this::hoe),
                new AITarget(FARMER_PLANT, this::plant),
                new AITarget(FARMER_HARVEST, this::harvest)
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
        if(getOwnBuilding() != null && getOwnBuilding().getBuildingLevel() < 1)
        {
            return AIState.PREPARING;
        }

        if(job.getFarmerFields().size() < getOwnBuilding().getBuildingLevel())
        {
            searchAndAddFields();
        }

        if(job.getFarmerFields().isEmpty())
        {
            chatSpamFilter.talkWithoutSpam("entity.farmer.noFreeFields");
            return AIState.PREPARING;
        }

        //If the farmer has no currentField and there is no field which needs work, check fields.
        if(job.getCurrentField() == null && job.getFieldToWorkOn() == null)
        {
            return AIState.FARMER_CHECK_FIELDS;
        }

        if(job.getCurrentField().needsWork() && !walkToBlock(job.getCurrentField().getLocation()))
        {
            switch (job.getCurrentField().getFieldStage())
            {
                case EMPTY:
                    if(checkForHoe())
                    {
                        return AIState.FARMER_HOE;
                    }
                case HOED:
                    //todo get seed from hut chest
                    return AIState.FARMER_PLANT;
                case PLANTED:
                    return AIState.FARMER_HARVEST;
            }
        }
        else
        {
            job.setCurrentField(null);
        }

        if (checkForHoe())
        {
            return getState();
        }

        //if any field needs work -> work on them

        return AIState.PREPARING;
    }

    /**
     * Executes the hoeing of the field.
     * @return the next state.
     */
    private AIState hoe()
    {
        //todo request hoe
        Field    field    = job.getCurrentField();
        BlockPos position = field.getLocation();

        //hoe it, set work false, set status hoed
        //todo important only harvest, plant, hoe if the field has no crops in it.

        //todo if work is done
        if(true)
        {
            job.getCurrentField().setNeedsWork(false);
            job.getCurrentField().setFieldStage(HOED);
        }

        return AIState.FARMER_HOE;
    }

    /**
     * Executes the planting of the field.
     * @return the next state.
     */
    private AIState plant()
    {

        Field    field    = job.getCurrentField();
        BlockPos position = field.getLocation();

        //todo If not the right seed in inventory and if nothing planted on field yet, request seed and setFieldNull

        //calculate position t check
        //if it applies to our field rule
        //if(job.getCurrentField().isPartOfField(world, positionOff))

        //plant it, set work false, set status seeded

        //todo if work is done
        if(true)
        {
            job.getCurrentField().setNeedsWork(false);
            job.getCurrentField().setFieldStage(PLANTED);
        }

        return AIState.FARMER_PLANT;
    }

    /**
     * Executes the harvesting of the field.
     * @return the next state.
     */
    private AIState harvest()
    {
        Field    field    = job.getCurrentField();
        BlockPos position = field.getLocation();

        //harvest it, set work false, set status empty

        //todo if work is done
        if(true)
        {
            job.getCurrentField().setNeedsWork(false);
            job.getCurrentField().setFieldStage(EMPTY);
        }

        return AIState.FARMER_HARVEST;
    }

    private AIState checkFields()
    {
        //todo goTo current field
        //check status
        //if status = empty, check if hoed if not set workTodo else set hoed.

        //if status = hoed -> check if seeded, if not set workTodo else, set seeded
        //if status = seeded -> if more than 50% ready, set workTodo

        return AIState.FARMER_HOE;
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
                job.getFarmerFields().add(newField);
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
        /*if (getBlocksMined() > MAX_BLOCKS_HARVESTED)
        {
            clearBlocksMined();
            return true;
        }*/
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
