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

import static com.minecolonies.entity.ai.util.AIState.*;

import java.util.List;

/**
 * Farmer AI class.
 * Created: December 20, 2014
 */
public class EntityAIWorkFarmer extends AbstractEntityAIInteract<JobFarmer>
{
    /**
     * The standard delay the farmer should have.
     */
    private static final int     STANDARD_DELAY      = 7;
    /**
     * The bonus the farmer gains each update is level/divider.
     */
    private static final int     DELAY_DIVIDER       = 10;
    /**
      * The EXP Earned per harvest.
      */
    private static final double  XP_PER_HARVEST      = 0.5;
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
          new AITarget(FARMER_WORK, this::cycle)
        );
        worker.setSkillModifier(2 * worker.getCitizenData().getEndurance() + worker.getCitizenData().getCharisma());
        worker.setCanPickUpLoot(true);
    }

    /**
     * Redirects the farmer to his building.
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
        @Nullable final BuildingFarmer building = getOwnBuilding();

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

        @Nullable final Field currentField = building.getCurrentField();

        if (currentField.needsWork())
        {
            if(!checkForHoe() && canGoPlanting(currentField, building))
            {
                return walkToBlock(currentField.getLocation()) ? AIState.PREPARING : AIState.FARMER_WORK;
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
        final Colony colony = worker.getColony();
        if (colony != null)
        {
            @Nullable final Field newField = colony.getFreeField(worker.getName());

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
    private boolean canGoPlanting(@NotNull final Field currentField, @NotNull final BuildingFarmer buildingFarmer)
    {
        if (currentField.getSeed() == null)
        {
            chatSpamFilter.talkWithoutSpam("entity.farmer.noSeedSet");
            buildingFarmer.setCurrentField(null);
            return false;
        }

        if (shouldTryToGetSeed)
        {
            final int slot = worker.findFirstSlotInInventoryWith(currentField.getSeed());
            if (slot != -1)
            {
                requestSeeds = false;
            }
            if (!walkToBuilding())
            {
                if (isInHut(new ItemStack(currentField.getSeed())))
                {
                    requestSeeds = false;
                }
                shouldTryToGetSeed = requestSeeds;
            }
        }

        return !shouldTryToGetSeed;
    }

    /**
      * The main work cycle of the Famer.
      * This checks each block, harvests, tills, and plants.
      */
    private AIState cycle()
    {
        @Nullable final BuildingFarmer buildingFarmer = getOwnBuilding();

        if (buildingFarmer == null || checkForHoe())
        {
            return AIState.PREPARING;
        }

        @Nullable final Field field = buildingFarmer.getCurrentField();

        if (field == null)
        {
            return AIState.PREPARING;
        }

        if (workingOffset != null)
        {
            final BlockPos position = field.getLocation().down().south(workingOffset.getZ()).east(workingOffset.getX());
            // Still moving to the block
            if (walkToBlock(position.up()))
            {
                return AIState.FARMER_WORK;
            }

            // harvest the block if able to.
            boolean interupt = harvestIfAble(position);
            if (interupt)
            {
                setDelay(STANDARD_DELAY - this.worker.getLevel() / DELAY_DIVIDER);
                return AIState.FARMER_WORK;
            }

            // hoe the block if able to.
            interupt = hoeIfAble(position, field);
            if (interupt)
            {
                setDelay(STANDARD_DELAY - this.worker.getLevel() / DELAY_DIVIDER);
                return AIState.FARMER_WORK;
            }

            if (shouldPlant(position, field) && !plantCrop(field.getSeed(), position))
            {
                resetVariables();
                buildingFarmer.getCurrentField().setNeedsWork(false);
                return terminatePlanting(buildingFarmer, field);
            }
        }

        if (!handleOffset(field))
        {
            resetVariables();
            buildingFarmer.getCurrentField().setNeedsWork(false);
            return AIState.IDLE;
        }

        // Set the delay based off the standard - level / divider. This was workingDelay
        setDelay(STANDARD_DELAY - this.worker.getLevel() / DELAY_DIVIDER);
        return AIState.FARMER_WORK;
    }

    /**
      * Checks if we can harvest, and does so if we can.
      *
      * @return true if we harvested.
      */
    private boolean harvestIfAble(final BlockPos position)
    {
        if (shouldHarvest(position))
        {
            worker.addExperience(XP_PER_HARVEST);
            return harvestCrop(position.up());
        }
        return false;
    }

    /**
      * Checks if we can hoe, and does so if we can.
      *
      * @param position the position to check
      * @param field the field that we are working with.
      */
    private boolean hoeIfAble(final BlockPos position, final Field field)
    {
        if (shouldHoe(position, field))
        {
            equipHoe();
            worker.swingArm(worker.getActiveHand());
            world.setBlockState(position, Blocks.FARMLAND.getDefaultState());
            worker.damageItemInHand(1);
            mineBlock(position.up());
            return true;
        }
        return false;
    }

    /**
     * Checks if the ground should be hoed and the block above removed.
     *
     * @param position the position to check.
     * @param field    the field close to this position.
     * @return true if should be hoed.
     */
    private boolean shouldHoe(@NotNull final BlockPos position, @NotNull final Field field)
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
    private boolean handleOffset(@NotNull final Field field)
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
     * Checks if the ground should be planted.
     *
     * @param position the position to check.
     * @param field    the field close to this position.
     * @return true if should be hoed.
     */
    private boolean shouldPlant(@NotNull final BlockPos position, @NotNull final Field field)
    {
        @Nullable final ItemStack itemStack = BlockUtils.getItemStackFromBlockState(world.getBlockState(position.up()));

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
        final int slot = worker.findFirstSlotInInventoryWith(item);
        if (slot == -1)
        {
            return false;
        }
        else
        {
            @NotNull final IPlantable seed = (IPlantable) item;
            world.setBlockState(position.up(), seed.getPlant(world, position));
            getInventory().decrStackSize(slot, 1);
            requestSeeds = false;
            //Flag 1+2 is needed for updates
            return true;
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
    private AIState terminatePlanting(@NotNull final BuildingFarmer buildingFarmer, @NotNull final Field field)
    {
        if (requestSeeds)
        {
            chatSpamFilter.talkWithoutSpam("entity.farmer.NeedSeed", field.getSeed().getItemStackDisplayName(new ItemStack(field.getSeed())));
        }
        else
        {
            buildingFarmer.getCurrentField().setNeedsWork(false);
        }

        resetVariables();
        return AIState.PREPARING;
    }

    /**
     * Checks if the crop should be harvested.
     *
     * @param position the position to check.
     * @return true if should be hoed.
     */
    private boolean shouldHarvest(@NotNull final BlockPos position)
    {
        final IBlockState state = world.getBlockState(position.up());

        if (state.getBlock() instanceof IGrowable && state.getBlock() instanceof BlockCrops)
        {
            @NotNull final BlockCrops block = (BlockCrops) state.getBlock();
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
    protected boolean neededForWorker(@Nullable final ItemStack stack)
    {
        return stack != null && Utils.isHoe(stack);
    }

    /**
     * Returns the farmer's worker instance. Called from outside this class.
     *
     * @return citizen object
     */
    @Nullable
    public EntityCitizen getCitizen()
    {
        return worker;
    }

    /**
      * This method allows us to harvest crops and leave the plant there.
      * Credit goes to RightClickHarvest mod
      *
      * @params position the position of the crop to harvest
      */
    private boolean harvestCrop(final BlockPos position)
    {
        final IBlockState curBlockState = world.getBlockState(position);

        if (!(curBlockState.getBlock() instanceof IGrowable) || !(curBlockState.getBlock() instanceof BlockCrops))
        {
            return false;
        }

        final BlockCrops crops = (BlockCrops) curBlockState.getBlock();

        if (!crops.isMaxAge(curBlockState))
        {
            return false;
        }

        final ItemStack tool = worker.getHeldItemMainhand();

        //calculate fortune enchantment
        final int fortune = Utils.getFortuneOf(tool);

        final List<ItemStack> drops = crops.getDrops(world, position, curBlockState, fortune);

        world.setBlockState(position, crops.withAge(0));

        //add the drops to the citizen
        for (final ItemStack item : drops)
        {
            InventoryUtils.setStack(worker.getInventoryCitizen(), item);
        }

        this.incrementActionsDone();
        return true;

    }
}
