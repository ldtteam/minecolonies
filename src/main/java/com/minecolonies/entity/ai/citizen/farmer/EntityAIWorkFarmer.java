package com.minecolonies.entity.ai.citizen.farmer;

import com.minecolonies.blocks.BlockHutField;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.buildings.BuildingFarmer;
import com.minecolonies.colony.jobs.JobFarmer;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.entity.ai.util.AIState;
import com.minecolonies.entity.ai.util.AITarget;
import com.minecolonies.util.BlockUtils;
import com.minecolonies.util.InventoryUtils;
import com.minecolonies.util.Utils;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.IPlantable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.entity.ai.citizen.farmer.Field.FieldStage.*;
import static com.minecolonies.entity.ai.util.AIState.*;

/**
 * Farmer AI class
 * Created: December 20, 2014
 */
public class EntityAIWorkFarmer extends AbstractEntityAIInteract<JobFarmer>
{
    /**
     * The standard delay the farmer should have.
     */
    private static final int     STANDARD_DELAY      = 5;
    /**
     * The bonus the farmer gains each update is level/divider.
     */
    private static final int     DELAY_DIVIDER       = 10;
    /**
     * Changed after finished harvesting in order to dump the inventory.
     */
    private              boolean shouldDumpInventory = false;
    /**
     * The offset to work at relative to the scarecrow.
     */
    @Nullable
    private BlockPos workingOffset;
    /**
     * The delay the farmer should have each action: hoeing, planting, harvesting.
     */
    private int workingDelay = STANDARD_DELAY - this.worker.getLevel() / DELAY_DIVIDER;

    /**
     * Defines if the farmer should request seeds for the current field.
     */
    private boolean requestSeeds = true;

    /**
     * Defines if the farmer should try to get the seeds from his chest.
     */
    private boolean shouldTryToGetSeed = true;

    /**
     * Constructor for the Farmer.
     * Defines the tasks the Farmer executes.
     *
     * @param job a farmer job to use.
     */
    public EntityAIWorkFarmer(@NotNull JobFarmer job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, () -> START_WORKING),
          new AITarget(START_WORKING, this::startWorkingAtOwnBuilding),
          new AITarget(PREPARING, this::prepareForFarming),
          new AITarget(FARMER_HOE, this::hoe),
          new AITarget(FARMER_PLANT, this::plant),
          new AITarget(FARMER_HARVEST, this::harvest)
        );
        worker.setSkillModifier(2 * worker.getCitizenData().getEndurance() + worker.getCitizenData().getCharisma());
        worker.setCanPickUpLoot(true);
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
     * Prepares the farmer for farming.
     * Also requests the tools and checks if the farmer has sufficient fields.
     *
     * @return the next AIState
     */
    @NotNull
    private AIState prepareForFarming()
    {
        @Nullable BuildingFarmer building = getOwnBuilding();

        if (building == null || building.getBuildingLevel() < 1)
        {
            return AIState.PREPARING;
        }

        building.syncWithColony(world);

        if (building.getFarmerFields().size() < getOwnBuilding().getBuildingLevel() && !building.assignManually())
        {
            searchAndAddFields();
        }

        if (building.hasNoFields())
        {
            chatSpamFilter.talkWithoutSpam("entity.farmer.noFreeFields");
            return AIState.PREPARING;
        }

        //If the farmer has no currentField and there is no field which needs work, check fields.
        if (building.getCurrentField() == null && building.getFieldToWorkOn() == null)
        {
            building.resetFields();
            return AIState.IDLE;
        }

        @Nullable Field currentField = building.getCurrentField();

        if (currentField.needsWork())
        {
            switch (currentField.getFieldStage())
            {
                case EMPTY:
                    if (!checkForHoe())
                    {
                        return walkToBlock(currentField.getLocation()) ? AIState.PREPARING : AIState.FARMER_HOE;
                    }
                    break;
                case HOED:
                    if (canGoPlanting(currentField, building))
                    {
                        return walkToBlock(currentField.getLocation()) ? AIState.PREPARING : AIState.FARMER_PLANT;
                    }
                    break;
                case PLANTED:
                    return walkToBlock(currentField.getLocation()) ? AIState.PREPARING : AIState.FARMER_HARVEST;
                default:
                    break;
            }
        }
        else
        {
            getOwnBuilding().setCurrentField(null);
        }
        return AIState.PREPARING;
    }

    /**
     * Searches and adds a field that has not been taken yet for the farmer and then adds it to the list.
     */
    private void searchAndAddFields()
    {
        Colony colony = worker.getColony();
        if (colony != null)
        {
            @Nullable Field newField = colony.getFreeField(worker.getName());

            if (newField != null && getOwnBuilding() != null)
            {
                getOwnBuilding().addFarmerFields(newField);
            }
        }
    }

    /**
     * Checks if the farmer is ready to plant.
     *
     * @param currentField the field to plant.
     * @return true if he is ready.
     */
    private boolean canGoPlanting(@NotNull Field currentField, @NotNull BuildingFarmer buildingFarmer)
    {
        if (currentField.getSeed() == null)
        {
            chatSpamFilter.talkWithoutSpam("entity.farmer.noSeedSet");
            buildingFarmer.setCurrentField(null);
            return false;
        }

        if (shouldTryToGetSeed)
        {
            if (walkToBuilding())
            {
                return false;
            }
            isInHut(new ItemStack(currentField.getSeed()));
            shouldTryToGetSeed = false;
        }

        return true;
    }

    /**
     * Executes the hoeing of the field.
     *
     * @return the next state.
     */
    @NotNull
    private AIState hoe()
    {
        @Nullable BuildingFarmer buildingFarmer = getOwnBuilding();

        if (buildingFarmer == null || buildingFarmer.getCurrentField() == null)
        {
            return AIState.PREPARING;
        }
        @Nullable Field field = getOwnBuilding().getCurrentField();

        if (workingOffset != null)
        {
            BlockPos position = field.getLocation().down().south(workingOffset.getZ()).east(workingOffset.getX());
            if (walkToBlock(position.up()))
            {
                return AIState.FARMER_HOE;
            }

            if (shouldHoe(position, field))
            {
                if (checkForHoe())
                {
                    return AIState.PREPARING;
                }
                equipHoe();
                worker.swingArm(worker.getActiveHand());
                world.setBlockState(position, Blocks.FARMLAND.getDefaultState());
                worker.damageItemInHand(1);
                mineBlock(position.up());
            }
        }

        if (!handleOffset(field))
        {
            resetVariables();
            buildingFarmer.getCurrentField().setNeedsWork(true);
            buildingFarmer.getCurrentField().setFieldStage(HOED);
            return AIState.IDLE;
        }
        BlockPos position = field.getLocation().down().south(workingOffset.getZ()).east(workingOffset.getX());
        if (shouldHoe(position, field))
        {
            mineBlock(position.up());
        }

        setDelay(workingDelay);
        return AIState.FARMER_HOE;
    }

    /**
     * Checks if the ground should be hoed and the block above removed.
     *
     * @param position the position to check.
     * @param field    the field close to this position.
     * @return true if should be hoed.
     */
    private boolean shouldHoe(@NotNull BlockPos position, @NotNull Field field)
    {
        return !field.isNoPartOfField(world, position)
                 && !BlockUtils.isBlockSeed(world, position.up())
                 && !(world.getBlockState(position).getBlock() instanceof BlockHutField)
                 && (world.getBlockState(position).getBlock() == Blocks.DIRT || world.getBlockState(position).getBlock() == Blocks.GRASS);
    }

    /**
     * Sets the hoe as held item.
     */
    private void equipHoe()
    {
        worker.setHeldItem(getHoeSlot());
    }

    /**
     * Handles the offset of the field for the farmer.
     *
     * @param field the field object.
     * @return true if successful.
     */
    private boolean handleOffset(@NotNull Field field)
    {
        if (workingOffset == null)
        {
            workingOffset = new BlockPos(field.getLengthPlusX(), 0, field.getWidthPlusZ());
        }
        else
        {
            if (workingOffset.getZ() <= -field.getWidthMinusZ() && workingOffset.getX() <= -field.getLengthMinusX())
            {
                workingOffset = null;
                return false;
            }
            else if (workingOffset.getX() <= -field.getLengthMinusX())
            {
                workingOffset = new BlockPos(field.getLengthPlusX(), 0, workingOffset.getZ() - 1);
            }
            else
            {
                workingOffset = new BlockPos(workingOffset.getX() - 1, 0, workingOffset.getZ());
            }
        }
        return true;
    }

    /**
     * Resets the basic variables of the class.
     */
    private void resetVariables()
    {
        requestSeeds = true;
        shouldDumpInventory = true;
        shouldTryToGetSeed = true;
    }

    /**
     * Get's the slot in which the hoe is in.
     *
     * @return slot number
     */
    private int getHoeSlot()
    {
        return InventoryUtils.getFirstSlotContainingTool(getInventory(), Utils.HOE);
    }

    /**
     * Executes the planting of the field.
     *
     * @return the next state.
     */
    @NotNull
    private AIState plant()
    {
        @Nullable BuildingFarmer buildingFarmer = getOwnBuilding();

        if (buildingFarmer == null || buildingFarmer.getCurrentField() == null)
        {
            return AIState.PREPARING;
        }
        @Nullable Field field = getOwnBuilding().getCurrentField();

        if (workingOffset != null)
        {
            BlockPos position = field.getLocation().down().south(workingOffset.getZ()).east(workingOffset.getX());
            if (walkToBlock(position.up()))
            {
                return AIState.FARMER_PLANT;
            }

            if (shouldPlant(position, field) && !plantCrop(field.getSeed(), position) && !requestSeeds)
            {
                workingOffset = null;
                resetVariables();
                buildingFarmer.getCurrentField().setNeedsWork(false);
                buildingFarmer.getCurrentField().setFieldStage(PLANTED);
                return AIState.IDLE;
            }
        }

        if (!handleOffset(field))
        {
            return terminatePlanting(buildingFarmer, field);
        }

        setDelay(workingDelay);

        return AIState.FARMER_PLANT;
    }

    /**
     * Checks if the ground should be planted.
     *
     * @param position the position to check.
     * @param field    the field close to this position.
     * @return true if should be hoed.
     */
    private boolean shouldPlant(@NotNull BlockPos position, @NotNull Field field)
    {
        @Nullable ItemStack itemStack = BlockUtils.getItemStackFromBlockState(world.getBlockState(position.up()));

        if (itemStack != null && itemStack.getItem() == field.getSeed())
        {
            requestSeeds = false;
        }

        return !field.isNoPartOfField(world, position) && !(world.getBlockState(position.up()).getBlock() instanceof BlockCrops)
                 && !(world.getBlockState(position).getBlock() instanceof BlockHutField) && world.getBlockState(position).getBlock() == Blocks.FARMLAND;
    }

    /**
     * Plants the crop at a given location.
     *
     * @param item     the crop.
     * @param position the location.
     */
    private boolean plantCrop(Item item, @NotNull BlockPos position)
    {
        int slot = worker.findFirstSlotInInventoryWith(item);
        if (slot != -1)
        {
            @NotNull IPlantable seed = (IPlantable) item;
            world.setBlockState(position.up(), seed.getPlant(world, position));
            getInventory().decrStackSize(slot, 1);
            requestSeeds = false;
            //Flag 1+2 is needed for updates
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Terminates the planting process and resets the task.
     *
     * @param buildingFarmer the building of the farmer.
     * @param field          the field being planted.
     * @return the next state.
     */
    @NotNull
    private AIState terminatePlanting(@NotNull BuildingFarmer buildingFarmer, @NotNull Field field)
    {
        if (requestSeeds)
        {
            chatSpamFilter.talkWithoutSpam("entity.farmer.NeedSeed", field.getSeed().getItemStackDisplayName(new ItemStack(field.getSeed())));
        }
        else
        {
            buildingFarmer.getCurrentField().setNeedsWork(false);
            buildingFarmer.getCurrentField().setFieldStage(PLANTED);
        }

        resetVariables();
        return AIState.IDLE;
    }

    /**
     * Executes the harvesting of the field.
     *
     * @return the next state.
     */
    @NotNull
    private AIState harvest()
    {
        @Nullable BuildingFarmer buildingFarmer = getOwnBuilding();

        if (buildingFarmer == null || buildingFarmer.getCurrentField() == null)
        {
            return AIState.PREPARING;
        }
        @Nullable Field field = getOwnBuilding().getCurrentField();

        if (workingOffset != null)
        {
            BlockPos position = field.getLocation().down().south(workingOffset.getZ()).east(workingOffset.getX());
            if (walkToBlock(position.up()))
            {
                return AIState.FARMER_HARVEST;
            }

            if (shouldHarvest(position))
            {
                worker.addExperience(0.5);
                mineBlock(position.up());
            }
        }

        if (!handleOffset(field))
        {
            buildingFarmer.getCurrentField().setNeedsWork(false);
            buildingFarmer.getCurrentField().setFieldStage(EMPTY);
            return AIState.IDLE;
        }
        BlockPos position = field.getLocation().down().south(workingOffset.getZ()).east(workingOffset.getX());
        if (shouldHarvest(position))
        {
            mineBlock(position.up());
        }

        setDelay(workingDelay);
        return AIState.FARMER_HARVEST;
    }

    /**
     * Checks if the crop should be harvested.
     *
     * @param position the position to check.
     * @return true if should be hoed.
     */
    private boolean shouldHarvest(@NotNull BlockPos position)
    {
        IBlockState state = world.getBlockState(position.up());

        if (state.getBlock() instanceof IGrowable && state.getBlock() instanceof BlockCrops)
        {
            @NotNull BlockCrops block = (BlockCrops) state.getBlock();
            return !block.canGrow(world, position.up(), state, false);
        }

        return false;
    }

    /**
     * Called to check when the InventoryShouldBeDumped.
     *
     * @return true if the conditions are met
     */
    @Override
    protected boolean wantInventoryDumped()
    {
        if (shouldDumpInventory)
        {
            shouldDumpInventory = false;
            return true;
        }
        return false;
    }

    /**
     * Returns the farmer's work building.
     *
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
    protected boolean neededForWorker(@Nullable ItemStack stack)
    {
        return stack != null && Utils.isHoe(stack);
    }

    /**
     * Returns the fisherman's worker instance. Called from outside this class.
     *
     * @return citizen object
     */
    @Nullable
    public EntityCitizen getCitizen()
    {
        return worker;
    }
}
