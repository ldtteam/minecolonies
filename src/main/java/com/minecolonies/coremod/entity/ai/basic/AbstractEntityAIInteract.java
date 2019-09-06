package com.minecolonies.coremod.entity.ai.basic;

import com.ldtteam.structurize.util.BlockUtils;
import com.minecolonies.api.entity.ai.citizen.builder.IBuilderUndestroyable;
import com.minecolonies.api.util.*;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is the base class of all worker AIs.
 * Every AI implements this class with it's job type.
 * There are some utilities within the class:
 * - The AI will clear a full inventory at the building chest.
 * - The AI will animate mining a block (with delay)
 * - The AI will request items and tools automatically
 * (and collect them from the building chest)
 *
 * @param <J> the job type this AI has to do.
 */
public abstract class AbstractEntityAIInteract<J extends AbstractJob> extends AbstractEntityAISkill<J>
{
    /**
     * The amount of xp the entity gains per block mined.
     */
    public static final double XP_PER_BLOCK = 0.05D;

    /**
     * The percentage of time needed if we are one level higher.
     */
    private static final double LEVEL_MODIFIER = 0.85D;

    /**
     * The minimum range the builder has to reach in order to construct or clear.
     */
    private static final int MIN_WORKING_RANGE = 12;

    /**
     * Range around the worker to pickup items.
     */
    private static final int ITEM_PICKUP_RANGE = 3;

    /**
     * Ticks to wait until discovering that is stuck.
     */
    private static final int STUCK_WAIT_TICKS = 20;

    /**
     * The amount of time to wait while walking to items.
     */
    private static final int WAIT_WHILE_WALKING = 5;

    /**
     * Horizontal range in which the worker picks up items.
     */
    public static final float RANGE_HORIZONTAL_PICKUP = 45.0F;

    /**
     * Vertical range in which the worker picks up items.
     */
    public static final float RANGE_VERTICAL_PICKUP = 3.0F;

    /**
     * Number of ticks the worker is standing still.
     */
    private int stillTicks = 0;

    /**
     * Used to store the path index
     * to check if the worker is still walking.
     */
    private int previousIndex = 0;

    /**
     * Positions of all items that have to be collected.
     */
    @Nullable
    private List<BlockPos> items;

    /**
     * Creates the abstract part of the AI.
     * Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public AbstractEntityAIInteract(@NotNull final J job)
    {
        super(job);
        super.registerTargets(
          //no new targets for now
        );
    }

    /**
     * Will simulate mining a block with particles ItemDrop etc.
     * Attention:
     * Because it simulates delay, it has to be called 2 times.
     * So make sure the code path up to this function is reachable a second time.
     * And make sure to immediately exit the update function when this returns false.
     *
     * @param blockToMine the block that should be mined
     * @return true once we're done
     */
    protected final boolean mineBlock(@NotNull final BlockPos blockToMine)
    {
        return mineBlock(blockToMine, worker.getCurrentPosition());
    }

    /**
     * Will simulate mining a block with particles ItemDrop etc.
     * Attention:
     * Because it simulates delay, it has to be called 2 times.
     * So make sure the code path up to this function is reachable a second time.
     * And make sure to immediately exit the update function when this returns false.
     *
     * @param blockToMine the block that should be mined
     * @param safeStand   the block we want to stand on to do that
     * @return true once we're done
     */
    protected final boolean mineBlock(@NotNull final BlockPos blockToMine, @NotNull final BlockPos safeStand)
    {
        return mineBlock(blockToMine, safeStand, true, true, null);
    }

    /**
     * Will simulate mining a block with particles ItemDrop etc.
     * Attention:
     * Because it simulates delay, it has to be called 2 times.
     * So make sure the code path up to this function is reachable a second time.
     * And make sure to immediately exit the update function when this returns false.
     *
     * @param blockToMine      the block that should be mined
     * @param safeStand        the block we want to stand on to do that
     * @param damageTool       boolean wether we want to damage the tool used
     * @param getDrops         boolean wether we want to get Drops
     * @param blockBreakAction Runnable that is used instead of the default block break action, can be null
     * @return true once we're done
     */
    protected final boolean mineBlock(
      @NotNull final BlockPos blockToMine,
      @NotNull final BlockPos safeStand,
      @NotNull final boolean damageTool,
      @NotNull final boolean getDrops,
      final Runnable blockBreakAction)
    {
        final BlockState curBlockState = world.getBlockState(blockToMine);
        @Nullable final Block curBlock = curBlockState.getBlock();
        if (curBlock == null
              || curBlock.equals(Blocks.AIR)
              || curBlock instanceof IBuilderUndestroyable
              || curBlock == Blocks.BEDROCK)
        {
            if (curBlock != null
                  && curBlockState.getMaterial().isLiquid())
            {
                world.removeBlock(blockToMine, false);
            }
            //no need to mine block...
            return true;
        }

        if (checkMiningLocation(blockToMine, safeStand))
        {
            //we have to wait for delay
            return false;
        }

        final ItemStack tool = worker.getHeldItemMainhand();

        if (getDrops)
        {
            //calculate fortune enchantment
            final int fortune = ItemStackUtils.getFortuneOf(tool);

            //check if tool has Silk Touch
            final boolean silkTouch = ItemStackUtils.hasSilkTouch(tool);

            //create list for all item drops to be stored in
            final List<ItemStack> localItems = new ArrayList<ItemStack>();

            //Checks to see if the equipped tool has Silk Touch AND if the blocktoMine has a viable Item SilkTouch can get.
            if (silkTouch && Item.getItemFromBlock(BlockPosUtil.getBlock(world, blockToMine)) != null)
            {
                //Stores Silk Touch Block in localItems
                final ItemStack silkItem = new ItemStack(Item.getItemFromBlock(BlockPosUtil.getBlock(world, blockToMine)), 1);
                localItems.add(silkItem);
            }
            //If Silk Touch doesn't work, get blocks with Fortune value as normal.
            else
            {
                localItems.addAll(BlockPosUtil.getBlockDrops(world, blockToMine, fortune, tool));
            }

            //add the drops to the citizen
            for (final ItemStack item : localItems)
            {
                InventoryUtils.transferItemStackIntoNextBestSlotInItemHandler(item, worker.getInventoryCitizen());
            }
        }

        if (blockBreakAction == null)
        {
            //Break the block
            worker.getCitizenItemHandler().breakBlockWithToolInHand(blockToMine);
        }
        else
        {
            blockBreakAction.run();
        }


        if (tool != null && damageTool)
        {
            tool.getItem().inventoryTick(tool, world, worker, worker.getCitizenInventoryHandler().findFirstSlotInInventoryWith(tool.getItem()), true);
        }
        worker.getCitizenExperienceHandler().addExperience(XP_PER_BLOCK);
        this.incrementActionsDone();
        return true;
    }

    /**
     * Checks for the right tools and waits for an appropriate delay.
     *
     * @param blockToMine the block to mine eventually
     * @param safeStand   a safe stand to mine from (empty Block!)
     */
    private boolean checkMiningLocation(@NotNull final BlockPos blockToMine, @NotNull final BlockPos safeStand)
    {
        final Block curBlock = world.getBlockState(blockToMine).getBlock();

        if (!holdEfficientTool(curBlock, blockToMine))
        {
            //We are missing a tool to harvest this block...
            return true;
        }

        if (walkToBlock(safeStand) && MathUtils.twoDimDistance(worker.getPosition(), safeStand) > MIN_WORKING_RANGE)
        {
            return true;
        }
        currentWorkingLocation = blockToMine;
        currentStandingLocation = safeStand;


        return hasNotDelayed(getBlockMiningDelay(curBlock, blockToMine));
    }

    /**
     * Calculate how long it takes to mine this block.
     *
     * @param block the block type
     * @param pos   coordinate
     * @return the delay in ticks
     */
    public int getBlockMiningDelay(@NotNull final Block block, @NotNull final BlockPos pos)
    {
        if (worker.getHeldItemMainhand() == null)
        {
            return (int) world.getBlockState(pos).getBlockHardness(world, pos);
        }

        return (int) ((MineColonies.getConfig().getCommon().pvp_mode.get() ? MineColonies.getConfig().getCommon().blockMiningDelayModifier.get() / 2 : MineColonies.getConfig().getCommon().blockMiningDelayModifier.get() * Math.pow(LEVEL_MODIFIER, worker.getCitizenExperienceHandler().getLevel())) * (double) world.getBlockState(pos).getBlockHardness(world, pos) / (double) (worker.getHeldItemMainhand().getItem().getDestroySpeed(worker.getHeldItemMainhand(), block.getDefaultState())));
    }

    /**
     * Fill the list of the item positions to gather.
     */
    public void fillItemsList()
    {
        searchForItems(worker.getBoundingBox()
                         .expand(RANGE_HORIZONTAL_PICKUP, RANGE_VERTICAL_PICKUP, RANGE_HORIZONTAL_PICKUP)
                         .expand(-RANGE_HORIZONTAL_PICKUP, -RANGE_VERTICAL_PICKUP, -RANGE_HORIZONTAL_PICKUP));
    }

    /**
     * Search for all items around the worker.
     * and store them in the items list.
     *
     * @param boundingBox the area to search.
     */
    public void searchForItems(final AxisAlignedBB boundingBox)
    {
        items = world.getEntitiesWithinAABB(ItemEntity.class, boundingBox)
                  .stream()
                  .filter(item -> item != null && item.isAlive() &&
                                    (!item.getPersistantData().keySet().contains("PreventRemoteMovement") || !item.getPersistantData().getBoolean("PreventRemoteMovement")))
                  .map(BlockPosUtil::fromEntity)
                  .collect(Collectors.toList());
    }

    /**
     * Collect one item by walking to it.
     */
    public void gatherItems()
    {
        worker.setCanPickUpLoot(true);
        if (worker.getNavigator().noPath())
        {
            final BlockPos pos = getAndRemoveClosestItemPosition();
            worker.isWorkerAtSiteWithMove(pos, ITEM_PICKUP_RANGE);
            return;
        }
        if (worker.getNavigator().getPath() == null)
        {
            setDelay(WAIT_WHILE_WALKING);
            return;
        }

        final int currentIndex = worker.getNavigator().getPath().getCurrentPathIndex();
        //We moved a bit, not stuck
        if (currentIndex != previousIndex)
        {
            stillTicks = 0;
            previousIndex = currentIndex;
            return;
        }

        stillTicks++;
        //Stuck for too long
        if (stillTicks > STUCK_WAIT_TICKS)
        {
            //Skip this item
            worker.getNavigator().clearPath();
            if (items != null && !items.isEmpty())
            {
                items.remove(0);
            }
        }
    }

    /**
     * Find the closest item and remove it from the list.
     *
     * @return the closest item
     */
    private BlockPos getAndRemoveClosestItemPosition()
    {
        int index = 0;
        double distance = Double.MAX_VALUE;

        for (int i = 0; i < items.size(); i++)
        {
            final double tempDistance = items.get(i).distanceSq(worker.getPosition());
            if (tempDistance < distance)
            {
                index = i;
                distance = tempDistance;
            }
        }

        return items.remove(index);
    }

    /**
     * Reset the gathering items to null.
     */
    public void resetGatheringItems()
    {
        items = null;
    }

    /**
     * Get the items to gather list.
     *
     * @return a copy of it.
     */
    @Nullable
    public List<BlockPos> getItemsForPickUp()
    {
        return items == null ? null : new ArrayList<>(items);
    }
}
