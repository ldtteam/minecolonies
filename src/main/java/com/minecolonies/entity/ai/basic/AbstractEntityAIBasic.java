package com.minecolonies.entity.ai.basic;

import com.minecolonies.colony.buildings.BuildingWorker;
import com.minecolonies.colony.jobs.Job;
import com.minecolonies.entity.ai.util.AIState;
import com.minecolonies.entity.ai.util.AITarget;
import com.minecolonies.util.EntityUtils;
import com.minecolonies.util.InventoryFunctions;
import com.minecolonies.util.InventoryUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.entity.ai.util.AIState.*;

/**
 * This class provides basic ai functionality.
 */
public abstract class AbstractEntityAIBasic<J extends Job> extends AbstractAISkeleton<J>
{

    /**
     * Time in ticks to wait until the next check for items.
     */
    protected static final int DELAY_RECHECK = 10;

    /**
     * The default range for any walking to blocks.
     */
    private static final int DEFAULT_RANGE_FOR_DELAY = 3;

    /**
     * The block the ai is currently working at or wants to work.
     */
    protected BlockPos currentWorkingLocation = null;

    /**
     * The time in ticks until the next action is made
     */
    protected int delay = 0;

    /**
     * The block the ai is currently standing at or wants to stand.
     */
    protected BlockPos currentStandingLocation = null;

    /**
     * A list of ItemStacks with needed items and their quantity.
     * This list is a diff between @see #itemsNeeded and
     * the players inventory and their hut combined.
     * So look here for what is currently still needed
     * to fulfill the workers needs.
     * <p>
     * Will be cleared on restart, be aware!
     */
    protected List<ItemStack> itemsCurrentlyNeeded = new ArrayList<>();

    /**
     * The list of all items and their quantity that were requested by the worker.
     * Warning: This list does not change, if you need to see what is currently missing,
     * look at @see #itemsCurrentlyNeeded for things the miner needs.
     * <p>
     * Will be cleared on restart, be aware!
     */
    protected List<ItemStack> itemsNeeded = new ArrayList<>();

    /**
     * Sets up some important skeleton stuff for every ai.
     *
     * @param job the job class
     */
    protected AbstractEntityAIBasic(final J job)
    {
        super(job);
        super.registerTargets(
                /**
                 * Init safety checks and transition to IDLE
                 */
                new AITarget(INIT, this::initSafetyChecks),
                /**
                 * Update chestbelt and nametag
                 * Will be executed every time
                 * and does not stop execution
                 */
                new AITarget(this::updateVisualState),
                /**
                 * If waitingForSomething returns true
                 * stop execution to wait for it.
                 * this keeps the current state
                 * (returning null would not stop execution)
                 */
                new AITarget(this::waitingForSomething, this::getState)
                             );
    }

    /**
     * Check for null on important variables to prevent crashes.
     *
     * @return IDLE if all ready, else stay in INIT
     */
    private AIState initSafetyChecks()
    {
        //Something fatally wrong? Wait for re-init...
        if (null == getOwnBuilding())
        {
            //TODO: perhaps destroy this task? will see...
            return INIT;
        }
        return IDLE;
    }

    /**
     * Can be overridden in implementations to return the exact building type.
     *
     * @return the building associated with this AI's worker.
     */
    @Nullable
    protected BuildingWorker getOwnBuilding()
    {
        return worker.getWorkBuilding();
    }

    /**
     * Updates the visual state of the worker.
     * Updates render meta data.
     * Updates the current state on the nametag.
     *
     * @return null to execute more targets.
     */
    private AIState updateVisualState()
    {
        //Update the current state the worker is in.
        job.setNameTag(this.getState().toString());
        //Update torch, seeds etc. in chestbelt etc.
        updateRenderMetaData();
        return null;
    }

    /**
     * Can be overridden in implementations.
     * <p>
     * Here the AI can check if the chestBelt has to be re rendered and do it.
     */
    protected void updateRenderMetaData()
    {
        worker.setRenderMetadata("");
    }

    /**
     * This method will return true if the AI is waiting for something.
     * In that case, don't execute any more AI code, until it returns false.
     * Call this exactly once per tick to get the delay right.
     * The worker will move and animate correctly while he waits.
     *
     * @return true if we have to wait for something
     * @see #currentStandingLocation @see #currentWorkingLocation
     * @see #DEFAULT_RANGE_FOR_DELAY @see #delay
     */
    private boolean waitingForSomething()
    {
        if (delay > 0)
        {
            if (currentStandingLocation != null &&
                !worker.isWorkerAtSiteWithMove(currentStandingLocation, DEFAULT_RANGE_FOR_DELAY))
            {
                //Don't decrease delay as we are just walking...
                return true;
            }
            worker.hitBlockWithToolInHand(currentWorkingLocation);
            delay--;
            return true;
        }
        clearWorkTarget();
        return false;
    }

    /**
     * Remove the current working block and it's delay.
     */
    private void clearWorkTarget()
    {
        this.currentStandingLocation = null;
        this.currentWorkingLocation = null;
        this.delay = 0;
    }

    /**
     * Looks for needed items as long as not all of them are there.
     * Also waits for DELAY_RECHECK.
     *
     * @return NEEDS_ITEM
     */
    protected AIState waitForNeededItems()
    {

        delay = DELAY_RECHECK;
        return lookForNeededItems();
    }


    /**
     * Utility method to search for items currently needed.
     * Poll this until all items are there.
     */
    private AIState lookForNeededItems()
    {
        syncNeededItemsWithInventory();
        if (itemsCurrentlyNeeded.isEmpty())
        {
            itemsNeeded.clear();
            job.clearItemsNeeded();
            return IDLE;
        }
        if (walkToBuilding())
        {
            delay += DELAY_RECHECK;
            ItemStack first = itemsCurrentlyNeeded.get(0);
            //Takes one Stack from the hut if existent
            if (isInHut(first))
            {
                return NEEDS_ITEM;
            }

            requestWithoutSpam(first.getDisplayName());
        }
        return NEEDS_ITEM;
    }

    /**
     * Updates the itemsCurrentlyNeeded with current values.
     */
    private void syncNeededItemsWithInventory()
    {
        job.clearItemsNeeded();
        itemsNeeded.forEach(job::addItemNeeded);
        InventoryUtils.getInventoryAsList(worker.getInventoryCitizen()).forEach(job::removeItemNeeded);
        itemsCurrentlyNeeded = new ArrayList<>(job.getItemsNeeded());
    }

    /**
     * Finds the first @see ItemStack the type of {@code is}.
     * It will be taken from the chest and placed in the workers inventory.
     * Make sure that the worker stands next the chest to not break immersion.
     * Also make sure to have inventory space for the stack.
     *
     * @param is the type of item requested (amount is ignored)
     * @return true if a stack of that type was found
     */
    protected boolean isInHut(final ItemStack is)
    {
        final BuildingWorker buildingMiner = getOwnBuilding();
        return buildingMiner != null &&
               is != null &&
               InventoryFunctions
                       .matchFirstInInventory(
                               buildingMiner.getTileEntity(),
                               stack -> stack != null && is.isItemEqual(stack),
                               this::takeItemStackFromChest
                                             );
    }

    /**
     * Request an Item without spamming the chat.
     *
     * @param chat the Item Name
     */
    protected void requestWithoutSpam(String chat)
    {
        chatSpamFilter.requestWithoutSpam(chat);
    }

    /**
     * Walk the worker to it's building chest.
     * Please return immediately if this returns true.
     *
     * @return false if the worker is at his building
     */
    protected final boolean walkToBuilding()
    {
        final @Nullable BuildingWorker ownBuilding = getOwnBuilding();
        //Return true if the building is null to stall the worker
        return ownBuilding == null
               || walkToBlock(ownBuilding.getLocation());
    }

    /**
     * Sets the block the AI is currently walking to.
     *
     * @param stand where to walk to
     * @return true while walking to the block
     */
    protected final boolean walkToBlock(final BlockPos stand)
    {
        return walkToBlock(stand, DEFAULT_RANGE_FOR_DELAY);
    }

    /**
     * Sets the block the AI is currently walking to.
     *
     * @param stand where to walk to
     * @return true while walking to the block
     */
    protected final boolean walkToBlock(final BlockPos stand, final int range)
    {
        if (!EntityUtils.isWorkerAtSite(worker, stand.getX(), stand.getY(), stand.getZ(), range))
        {
            //only walk to the block, work=null
            workOnBlock(null, stand, 1);
            return true;
        }
        return false;
    }

    /**
     * Sets the block the AI is currently working on.
     * This block will receive animation hits on delay.
     *
     * @param target  the block that will be hit
     * @param stand   the block the worker will walk to
     * @param timeout the time in ticks to hit the block
     */
    private void workOnBlock(final BlockPos target, final BlockPos stand, final int timeout)
    {
        this.currentWorkingLocation = target;
        this.currentStandingLocation = stand;
        this.delay = timeout;
    }

    /**
     * Takes whatever is in that slot of the workers chest and puts it in his inventory.
     * If the inventory is full, only the fitting part will be moved.
     *
     * @param slot the slot in the buildings inventory
     */
    protected void takeItemStackFromChest(final int slot)
    {
        final @Nullable BuildingWorker ownBuilding = getOwnBuilding();
        if (ownBuilding == null)
        {
            return;
        }
        InventoryUtils.takeStackInSlot(ownBuilding.getTileEntity(), worker.getInventoryCitizen(), slot);
    }
}
