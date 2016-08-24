package com.minecolonies.entity.ai.citizen.farmer;

import com.minecolonies.blocks.BlockHutField;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.Field;
import com.minecolonies.colony.buildings.BuildingFarmer;
import com.minecolonies.colony.jobs.JobFarmer;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.entity.ai.util.AIState;
import com.minecolonies.entity.ai.util.AITarget;
import com.minecolonies.util.InventoryUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSeeds;
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
     * Changed after finished harvesting in order to dump the inventory.
     */
    private boolean shouldDumpInventory = false;

    /**
     * The offset to work at relative to the scarecrow.
     */
    private BlockPos workingOffset;

    /**
     * The standard delay the farmer should have.
     */
    private static final int STANDARD_DELAY = 5;

    /**
     * The bonus the farmer gains each update is level/divider.
     */
    private static final int DELAY_DIVIDER = 10;

    /**
     * The delay the farmer should have each action: hoeing, planting, harvesting.
     */
    private int workingDelay = STANDARD_DELAY - this.worker.getLevel() / DELAY_DIVIDER;

    /**
     * Defines if the farmer should request seeds for the current field.
     */
    private boolean requestSeeds = true;

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
        BuildingFarmer building = getOwnBuilding();

        if(building == null || building.getBuildingLevel() < 1)
        {
            return AIState.PREPARING;
        }

        building.synchWithColony();

        if(building.getFarmerFields().size() < getOwnBuilding().getBuildingLevel())
        {
            searchAndAddFields();
        }

        if(building.hasNoFields())
        {
            chatSpamFilter.talkWithoutSpam("entity.farmer.noFreeFields");
            return AIState.PREPARING;
        }

        Field currentField = building.getCurrentField();

        //If the farmer has no currentField and there is no field which needs work, check fields.
        if(currentField == null || building.getFieldToWorkOn() == null)
        {
            return AIState.FARMER_CHECK_FIELDS;
        }

        if(currentField.needsWork() && !walkToBlock(currentField.getLocation()))
        {
            switch (currentField.getFieldStage())
            {
                case EMPTY:
                    if(!checkForHoe())
                    {
                        return AIState.FARMER_HOE;
                    }
                    break;
                case HOED:
                    if(canGoPlanting(currentField, building))
                    {
                        return AIState.FARMER_PLANT;
                    }
                    break;
                case PLANTED:
                    return AIState.FARMER_HARVEST;
            }
        }
        else
        {
            getOwnBuilding().setCurrentField(null);
        }
        return AIState.PREPARING;
    }

    /**
     * Executes the hoeing of the field.
     * @return the next state.
     */
    private AIState hoe()
    {
        BuildingFarmer buildingFarmer = getOwnBuilding();

        if(buildingFarmer == null || buildingFarmer.getCurrentField() == null)
        {
            return AIState.PREPARING;
        }
        Field field = getOwnBuilding().getCurrentField();

        if(workingOffset != null)
        {
            BlockPos position = field.getLocation().down().north(workingOffset.getZ()).west(workingOffset.getX());
            if (walkToBlock(position.up()))
            {
                return AIState.FARMER_HOE;
            }

            if (shouldHoe(position, field))
            {
                //todo use hoe for that.
                world.setBlockState(position, Blocks.farmland.getDefaultState());
                mineBlock(position.up());
            }
        }

        if(!handleOffset(field))
        {
            buildingFarmer.getCurrentField().setNeedsWork(true);
            buildingFarmer.getCurrentField().setFieldStage(HOED);
            return AIState.IDLE;
        }
        BlockPos position = field.getLocation().down().north(workingOffset.getZ()).west(workingOffset.getX());
        if (shouldHoe(position, field))
        {
            mineBlock(position.up());
        }

        setDelay(workingDelay);
        return AIState.FARMER_HOE;
    }

    /**
     * Handles the offset of the field for the farmer.
     * @param field the field object.
     * @return true if successful.
     */
    private boolean handleOffset(Field field)
    {
        if(workingOffset == null)
        {
            workingOffset = new BlockPos(-field.getLengthMinusX(), 0, -field.getWidthMinusZ());
        }
        else
        {
            if(workingOffset.getX() >= field.getLengthPlusX())
            {
                workingOffset = new BlockPos(-field.getLengthMinusX(), 0, workingOffset.getZ()+1);
            }
            else if(workingOffset.getZ() >= field.getWidthPlusZ())
            {
                workingOffset = null;
                return false;
            }
            else
            {
                workingOffset = new BlockPos(workingOffset.getX()+1, 0, workingOffset.getZ());
            }
        }
        return true;
    }

    /**
     * Checks if the farmer is ready to plant.
     * @param currentField the field to plant.
     * @return true if he is ready.
     */
    private boolean canGoPlanting(Field currentField, BuildingFarmer buildingFarmer)
    {
        if(currentField.getSeed() == null)
        {
            //todo please define a crop in the scarecrow inventory.
            buildingFarmer.setCurrentField(null);
            return false;
        }

        //todo have to get to hut without terminating this AI
        isInHut(new ItemStack(currentField.getSeed()));

        return true;
    }

    /**
     * Executes the planting of the field.
     * @return the next state.
     */
    private AIState plant()
    {
        BuildingFarmer buildingFarmer = getOwnBuilding();

        if(buildingFarmer == null || buildingFarmer.getCurrentField() == null)
        {
            return AIState.PREPARING;
        }
        Field field = getOwnBuilding().getCurrentField();

        if(workingOffset != null)
        {
            BlockPos position = field.getLocation().down().north(workingOffset.getZ()).west(workingOffset.getX());
            if (walkToBlock(position.up()))
            {
                return AIState.FARMER_PLANT;
            }

            if (shouldPlant(position, field))
            {
                plantCrop(buildingFarmer.getCurrentField().getSeed(), position);
                mineBlock(position.up());
            }
        }

        if(!handleOffset(field))
        {
            if(requestSeeds)
            {
                //todo request seed
            }
            shouldDumpInventory = true;
            buildingFarmer.getCurrentField().setNeedsWork(false);
            buildingFarmer.getCurrentField().setFieldStage(PLANTED);
            return AIState.IDLE;
        }

        setDelay(workingDelay);

        return AIState.FARMER_PLANT;
    }

    /**
     * Plants the crop at a given location.
     * @param item the crop.
     * @param position the location.
     */
    private void plantCrop(Item item, BlockPos position)
    {
        int slot = worker.findFirstSlotInInventoryWith(item);
        if (slot != -1)
        {
            //todo isn't working.
            world.setBlockState(position, Block.getBlockFromItem(item).getDefaultState());
            getInventory().decrStackSize(slot, 1);
            //Flag 1+2 is needed for updates
        }
    }

    /**
     * Executes the harvesting of the field.
     * @return the next state.
     */
    private AIState harvest()
    {
        //harvest it, set work false, set status empty

        return AIState.FARMER_HOE;
    }

    private AIState checkFields()
    {
        //todo goTo current field
        //check status
        //if status = empty, check if hoed if not set workTodo else set hoed.

        //if status = hoed -> check if seeded, if not set workTodo else, set seeded
        //if status = seeded -> if more than 50% ready, set workTodo

        getOwnBuilding().setCurrentField(getOwnBuilding().getFarmerFields().get(0));
        getOwnBuilding().getCurrentField().setNeedsWork(true);
        return AIState.PREPARING;
    }

    /**
     * Checks if the ground should be planted.
     * @param position the position to check.
     * @param field the field close to this position.
     * @return true if should be hoed.
     */
    private boolean shouldPlant(BlockPos position, Field field)
    {
        if(ItemSeeds.getItemFromBlock(world.getBlockState(position).getBlock()) == field.getSeed())
        {
            requestSeeds = false;
        }
        return !field.isNoPartOfField(world, position) && !(world.getBlockState(position) instanceof BlockCrops)
               && !(world.getBlockState(position).getBlock() instanceof BlockHutField) && world.getBlockState(position).getBlock() == Blocks.farmland;
    }

    /**
     * Checks if the ground should be hoed and the block above removed.
     * @param position the position to check.
     * @param field the field close to this position.
     * @return true if should be hoed.
     */
    private boolean shouldHoe(BlockPos position, Field field)
    {
        return !field.isNoPartOfField(world, position) && !(world.getBlockState(position) instanceof BlockCrops)
               && !(world.getBlockState(position).getBlock() instanceof BlockHutField);
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
                getOwnBuilding().addFarmerFields(newField);
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
        if(shouldDumpInventory)
        {
            shouldDumpInventory = false;
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
