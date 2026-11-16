package com.minecolonies.api.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.core.tileentities.TileEntityRack;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.ldtteam.structurize.api.util.ItemStackUtils.getItemHandlersFromProvider;
import static com.minecolonies.api.util.constant.TranslationConstants.MESSAGE_INFO_PLAYER_INVENTORY_FULL_HOTBAR_INSERT;

/**
 * Utility methods for the inventories.
 */
public class InventoryUtils
{
    /**
     * Private constructor to hide the implicit one.
     */
    private InventoryUtils()
    {
        /*
         * Intentionally left empty.
         */
    }

    /// ----------- Start Read Only Methods ----------- ///

    /**
     * Check if an inventory contains a target item/block/predicate.
     * @param itemHandler the inventory.
     * @param targetItem what to check for.
     * @return true if contains.
     */
    public static boolean hasItem(@NotNull final IItemHandler itemHandler, @NotNull final Item targetItem) { return hasItem(itemHandler, (ItemStack stack) -> !stack.isEmpty() && stack.getItem() == targetItem); }
    public static boolean hasItem(@NotNull final IItemHandler itemHandler, @NotNull final Block block) { return hasItem(itemHandler, Item.byBlock(block)); }
    public static boolean hasItem(@NotNull final IItemHandler itemHandler, @NotNull final Predicate<ItemStack> predicate)
    {
       return getMatchingSlot(itemHandler, predicate) != -1;
    }

    /**
     * Get the slot in an inventory for a target item/block/predicate.
     * @param itemHandler the inventory.
     * @param targetItem what to check for.
     * @return the slot.
     */
    public static int getMatchingSlot(@NotNull final IItemHandler itemHandler, @NotNull final Item targetItem) { return getMatchingSlot(itemHandler,stack -> !stack.isEmpty() && stack.getItem() == targetItem); }
    public static int getMatchingSlot(@NotNull final IItemHandler itemHandler, @NotNull final Block block) { return getMatchingSlot(itemHandler, Item.byBlock(block)); }
    public static int getMatchingSlot(@NotNull final IItemHandler itemHandler, @NotNull final Predicate<ItemStack> predicate)
    {
        for (int slot = 0; slot < itemHandler.getSlots(); slot++)
        {
            if (predicate.test(itemHandler.getStackInSlot(slot)))
            {
                return slot;
            }
        }

        return -1;
    }

    /**
     * Returns the number of matching items in an {@link IItemHandler}.
     *
     * @param itemHandler {@link IItemHandler} to scan.
     * @param targetItem  what to check for.
     * @return quantity of items.
     */
    public static int getItemCount(@Nullable final IItemHandler itemHandler, @NotNull final Item targetItem) { return getItemCount(itemHandler, stack -> !stack.isEmpty() && stack.getItem() == targetItem); }
    public static int getItemCount(@Nullable final IItemHandler itemHandler, @NotNull final Predicate<ItemStack> predicate)
    {
        if (itemHandler == null)
        {
            Log.getLogger().error("This is not supposed to happen, please notify the developers!", new Exception("getItemCount got a null itemHandler"));
            return 0;
        }

        int count = 0;
        for (int i = 0; i < itemHandler.getSlots(); i++)
        {
            final ItemStack stack = itemHandler.getStackInSlot(i);
            if (predicate.test(stack))
            {
                count+= stack.getCount();
            }
        }

        return count;
    }

    /**
     * Returns the number of matching items in a list of {@link IItemHandler}.
     *
     * @param itemHandlers list of {@link IItemHandler} to scan.
     * @param predicate    The predicate used to select the stacks to count.
     * @return quantity of items.
     */
    public static int getItemCount(@Nullable final Collection<IItemHandler> itemHandlers, @NotNull final Predicate<ItemStack> predicate)
    {
        int count = 0;
        for (final IItemHandler handler : itemHandlers)
        {
            count += getItemCount(handler, predicate);
        }
        return count;
    }


    /// ----------- End Read Only Methods ----------- ///

    /**
     * Clears an entire {@link IItemHandler}.
     *
     * @param itemHandler {@link IItemHandler} to clear.
     */
    public static void clearInventory(@NotNull final IItemHandler itemHandler)
    {
        for (int slotIndex = 0; slotIndex < itemHandler.getSlots(); slotIndex++)
        {
            itemHandler.extractItem(slotIndex, Integer.MAX_VALUE, false);
        }
    }

    /**
     * Drop an actual itemHandler in the world.
     *
     * @param handler the handler.
     * @param world   the world.
     * @param x       the x pos.
     * @param y       the y pos.
     * @param z       the z pos.
     */
    public static void dropItemHandler(final IItemHandler handler, final Level world, final int x, final int y, final int z)
    {
        for (int i = 0; i < handler.getSlots(); ++i)
        {
            final ItemStack itemstack = handler.getStackInSlot(i);
            if (!ItemStackUtils.isEmpty(itemstack))
            {
                spawnItemStack(world, x, y, z, itemstack);
            }
        }
    }





    /**
     * Force stack to handler.
     *
     * @param itemHandler              {@link IItemHandler} to add itemstack to.
     * @param itemStack                ItemStack to add.
     * @param itemStackToKeepPredicate The {@link Predicate} that determines which ItemStacks to keep in the inventory. Return false to replace.
     * @return itemStack which has been replaced, null if none has been replaced.
     */
    @Nullable
    public static ItemStack forceItemStackToItemHandler(
      @NotNull final IItemHandler itemHandler,
      @NotNull final ItemStack itemStack,
      @NotNull final Predicate<ItemStack> itemStackToKeepPredicate)
    {
        final ItemStack standardInsertionResult = addItemStackToItemHandlerWithResult(itemHandler, itemStack);

        if (!ItemStackUtils.isEmpty(standardInsertionResult))
        {
            for (int i = 0; i < itemHandler.getSlots() && !ItemStackUtils.isEmpty(standardInsertionResult); i++)
            {
                final ItemStack localStack = itemHandler.getStackInSlot(i);
                if (ItemStackUtils.isEmpty(localStack) || !itemStackToKeepPredicate.test(localStack))
                {
                    final ItemStack removedStack = itemHandler.extractItem(i, Integer.MAX_VALUE, false);
                    final ItemStack localInsertionResult = itemHandler.insertItem(i, standardInsertionResult, false);

                    if (ItemStackUtils.isEmpty(localInsertionResult))
                    {
                        //Insertion successful. Returning the extracted stack.
                        return removedStack.copy();
                    }
                    else
                    {
                        //Insertion failed. The inserted stack was not accepted completely. Undo the extraction.
                        itemHandler.insertItem(i, removedStack, false);
                    }
                }
            }
        }
        return standardInsertionResult;
    }

    /**
     * Add stack to item handler.
     *
     * @param itemHandler {@link IItemHandler} to add itemstack to.
     * @param itemStack   ItemStack to add.
     * @return True if successful, otherwise false.
     */
    public static boolean addItemStackToItemHandler(@NotNull final IItemHandler itemHandler, @Nullable final ItemStack itemStack)
    {
        if (itemHandler.getSlots() == 0)
        {
            return false;
        }

        if (!ItemStackUtils.isEmpty(itemStack))
        {
            if (itemStack.isDamaged())
            {
                int slot = getFirstOpenSlotFromItemHandler(itemHandler);

                if (slot >= 0)
                {
                    itemHandler.insertItem(slot, itemStack, false);
                    return true;
                }
                else
                {
                    return false;
                }
            }
            else
            {
                ItemStack resultStack = itemStack.copy();
                int slot = 0;
                boolean placed = false;

                while (!ItemStackUtils.isEmpty(resultStack) && slot < itemHandler.getSlots())
                {
                    resultStack = itemHandler.insertItem(slot, resultStack, true);
                    if (ItemStackUtils.isEmpty(resultStack))
                    {
                        placed = true;
                        break;
                    }
                    slot++;
                }

                if (!placed)
                {
                    return false;
                }

                slot = 0;
                resultStack = itemStack;
                while (!ItemStackUtils.isEmpty(resultStack) && slot < itemHandler.getSlots())
                {
                    resultStack = itemHandler.insertItem(slot, resultStack, false);
                    if (ItemStackUtils.isEmpty(resultStack))
                    {
                        return true;
                    }
                    slot++;
                }

                // This can never happen! We checked if it is possible. This is not possible.
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    /**
     * Add stack to handler with result.
     *
     * @param itemHandler {@link IItemHandler} to add itemstack to.
     * @param itemStack   ItemStack to add.
     * @return Empty when fully transfered, otherwise return the remain of a partial transfer of the itemStack.
     */
    public static ItemStack addItemStackToItemHandlerWithResult(@NotNull final IItemHandler itemHandler, @Nullable final ItemStack itemStack)
    {
        if (!ItemStackUtils.isEmpty(itemStack))
        {
            int slot;

            if (itemStack.isDamaged())
            {
                slot = getFirstOpenSlotFromItemHandler(itemHandler);

                if (slot >= 0)
                {
                    itemHandler.insertItem(slot, itemStack.copy(), false);
                    return ItemStackUtils.EMPTY;
                }
                else
                {
                    return itemStack;
                }
            }
            else
            {
                ItemStack leftOver = itemStack;
                slot = itemHandler.getSlots() == 0 ? -1 : 0;
                while (!ItemStackUtils.isEmpty(leftOver) && slot != -1 && slot != itemHandler.getSlots())
                {
                    leftOver = itemHandler.insertItem(slot, leftOver.copy(), false);
                    if (!ItemStackUtils.isEmpty(leftOver))
                    {
                        slot++;
                    }
                }

                return leftOver;
            }
        }
        else
        {
            return itemStack;
        }
    }


    /**
     * Method to swap the ItemStacks from the given source {@link IItemHandler} to the given target {@link ICapabilityProvider}.
     *
     * @param sourceHandler  The {@link IItemHandler} that works as Source.
     * @param sourceIndex    The index of the slot that is being extracted from.
     * @param targetProvider The {@link ICapabilityProvider} that works as Target.
     * @return True when the swap was successful, false when not.
     */
    public static boolean transferItemStackIntoNextFreeSlotInProvider(
      @NotNull final IItemHandler sourceHandler,
      final int sourceIndex,
      @NotNull final ICapabilityProvider targetProvider)
    {
        for (final IItemHandler handler : getItemHandlersFromProvider(targetProvider))
        {
            if (transferItemStackIntoNextFreeSlotInItemHandler(sourceHandler, sourceIndex, handler))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Method to swap the ItemStacks from the given source {@link IItemHandler} to the given target {@link IItemHandler}.
     *
     * @param sourceHandler The {@link IItemHandler} that works as Source.
     * @param sourceIndex   The index of the slot that is being extracted from.
     * @param targetHandler The {@link IItemHandler} that works as Target.
     * @return True when the swap was successful, false when not.
     */
    public static boolean transferItemStackIntoNextFreeSlotInItemHandler(
      @NotNull final IItemHandler sourceHandler,
      final int sourceIndex,
      @NotNull final IItemHandler targetHandler)
    {
        ItemStack sourceStack = sourceHandler.extractItem(sourceIndex, Integer.MAX_VALUE, true);

        if (ItemStackUtils.isEmpty(sourceStack))
        {
            return true;
        }

        boolean success = false;
        for (int i = 0; i < targetHandler.getSlots(); i++)
        {
            sourceStack = targetHandler.insertItem(i, sourceStack, true);
            if (ItemStackUtils.isEmpty(sourceStack))
            {
                success = true;
                break;
            }
        }

        if (!success)
        {
            return false;
        }

        sourceStack = sourceHandler.extractItem(sourceIndex, Integer.MAX_VALUE, false);

        for (int i = 0; i < targetHandler.getSlots(); i++)
        {
            sourceStack = targetHandler.insertItem(i, sourceStack, false);
            if (ItemStackUtils.isEmpty(sourceStack))
            {
                return true;
            }
        }

        sourceHandler.insertItem(sourceIndex, sourceStack, false);
        return false;
    }

    /**
     * Method to swap the ItemStacks from the given source {@link IItemHandler} to the given target {@link IItemHandler}.
     *
     * @param sourceHandler The {@link IItemHandler} that works as Source.
     * @param sourceIndex   The index of the slot that is being extracted from.
     * @param count         the quantity.
     * @param targetHandler The {@link IItemHandler} that works as Target.
     * @return True when the swap was successful, false when not.
     */
    public static boolean transferXOfItemStackIntoNextFreeSlotInItemHandler(
      @NotNull final IItemHandler sourceHandler,
      final int sourceIndex,
      final int count,
      @NotNull final IItemHandler targetHandler)
    {
        ItemStack sourceStack = sourceHandler.extractItem(sourceIndex, count, true);

        if (ItemStackUtils.isEmpty(sourceStack))
        {
            return true;
        }

        boolean success = false;
        for (int i = 0; i < targetHandler.getSlots(); i++)
        {
            sourceStack = targetHandler.insertItem(i, sourceStack, true);
            if (ItemStackUtils.isEmpty(sourceStack))
            {
                success = true;
                break;
            }
        }

        if (!success)
        {
            return false;
        }

        sourceStack = sourceHandler.extractItem(sourceIndex, count, false);

        for (int i = 0; i < targetHandler.getSlots(); i++)
        {
            sourceStack = targetHandler.insertItem(i, sourceStack, false);
            if (ItemStackUtils.isEmpty(sourceStack))
            {
                return true;
            }
        }

        sourceHandler.insertItem(sourceIndex, sourceStack, false);
        return false;
    }

    /**
     * Method to swap the ItemStacks from the given source {@link IItemHandler} to the given target {@link IItemHandler}. Trying to merge existing itemStacks if possible.
     *
     * @param sourceHandler The {@link IItemHandler} that works as Source.
     * @param sourceIndex   The index of the slot that is being extracted from.
     * @param targetHandler The {@link IItemHandler} that works as Target.
     * @return True when the swap was successful, false when not.
     */
    public static boolean transferItemStackIntoNextBestSlotInItemHandler(
      @NotNull final IItemHandler sourceHandler,
      final int sourceIndex,
      @NotNull final IItemHandler targetHandler)
    {
        ItemStack sourceStack = sourceHandler.extractItem(sourceIndex, Integer.MAX_VALUE, true);

        if (ItemStackUtils.isEmpty(sourceStack))
        {
            return true;
        }

        if (addItemStackToItemHandler(targetHandler, sourceStack))
        {
            sourceHandler.extractItem(sourceIndex, Integer.MAX_VALUE, false);
            return true;
        }
        return false;
    }

    /**
     * Method to transfer an ItemStacks from the given source {@link IItemHandler} to the given target {@link IItemHandler}.
     *
     * @param sourceHandler The {@link IItemHandler} that works as Source.
     * @param predicate     the predicate for the stack.
     * @param targetHandler The {@link IItemHandler} that works as Target.
     * @return true when the swap was successful, false when not.
     */
    public static boolean transferItemStackIntoNextBestSlotInItemHandler(
      @NotNull final IItemHandler sourceHandler,
      final Predicate<ItemStack> predicate,
      @NotNull final IItemHandler targetHandler)
    {
        for (int i = 0; i < sourceHandler.getSlots(); i++)
        {
            if (predicate.test(sourceHandler.getStackInSlot(i)))
            {
                ItemStack sourceStack = sourceHandler.extractItem(i, Integer.MAX_VALUE, true);
                if (!sourceStack.isEmpty() && addItemStackToItemHandler(targetHandler, sourceStack))
                {
                    sourceHandler.extractItem(i, Integer.MAX_VALUE, false);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Method to transfer an ItemStacks from the given source {@link IBuilding} to the given target {@link IItemHandler}.
     *
     * @param building      The {@link IBuilding} that works as Source.
     * @param storage       the ItemStorage.
     * @param qty           the qty.
     * @param targetHandler The {@link IItemHandler} that works as Target.
     * @return true when the swap was successful, false when not.
     */
    public static boolean transferItemStackIntoNextBestSlotInItemHandler(
      @NotNull final IBuilding building,
      final ItemStorage storage,
      final int qty,
      @NotNull final IItemHandler targetHandler)
    {
        final Level level = building.getColony().getWorld();
        for (final BlockPos pos : building.getContainers())
        {
            if (WorldUtil.isBlockLoaded(level, pos))
            {
                final BlockEntity entity = level.getBlockEntity(pos);
                if (entity instanceof TileEntityRack tileEntityRack)
                {
                    if (tileEntityRack.hasItemStorage(storage, 1))
                    {
                        final IItemHandler sourceHandler = tileEntityRack.getInventory();
                        for (int i = 0; i < sourceHandler.getSlots(); i++)
                        {
                            if (storage.equals(new ItemStorage(sourceHandler.getStackInSlot(i))))
                            {
                                ItemStack sourceStack = sourceHandler.extractItem(i, qty, true);
                                if (!sourceStack.isEmpty() && addItemStackToItemHandler(targetHandler, sourceStack))
                                {
                                    sourceHandler.extractItem(i, qty, false);
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Method to transfer an ItemStacks from the given source {@link IBuilding} to the given target {@link IItemHandler}.
     *
     * @param building      The {@link IBuilding} that works as Source.
     * @param storage       the ItemStorage.
     * @param targetHandler The {@link IItemHandler} that works as Target.
     * @return true when the swap was successful, false when not.
     */
    public static boolean transferItemStackIntoNextBestSlotInItemHandler(
      @NotNull final IBuilding building,
      final ItemStorage storage,
      @NotNull final IItemHandler targetHandler)
    {
        return transferItemStackIntoNextBestSlotInItemHandler(building, storage, Integer.MAX_VALUE, targetHandler);
    }

    /**
     * Method to transfer an ItemStacks to a Building.
     *
     * @param building The {@link IBuilding} that works as Source.
     * @param stack    the ItemStack.
     * @param qty      the quantity
     * @return the remainder quantity that was not transferred.
     */
    public static int transferItemStackToBuilding(
        final IBuilding building,
        final ItemStack stack,
        final int qty)
    {
        int tempQty = qty;
        final Level level = building.getColony().getWorld();
        for (int i = 0; i < Math.max(1, Math.ceil((double)qty/stack.getMaxStackSize())); i++)
        {
            final int before = tempQty;
            for (final BlockPos pos : building.getContainers())
            {
                if (WorldUtil.isBlockLoaded(level, pos))
                {
                    final BlockEntity entity = level.getBlockEntity(pos);
                    if (entity instanceof TileEntityRack tileEntityRack)
                    {
                        int localQty = Math.min(tempQty, stack.getMaxStackSize());
                        final ItemStack itemStack = stack.copy();
                        itemStack.setCount(localQty);
                        tempQty -= localQty - transferItemStackIntoNextBestSlotInItemHandlerWithResult(itemStack, tileEntityRack.getInventory()).getCount();

                        if (tempQty <= 0)
                        {
                            return 0;
                        }
                    }
                }
            }
            if (tempQty == before)
            {
                break;
            }
        }

        return tempQty;
    }

    /**
     * Method to transfer an ItemStacks to a Building.
     *
     * @param building The {@link IBuilding} that works as Source.
     * @param stack    the ItemStack.
     * @param qty      the quantity
     * @return the remainder quantity that was not transferred.
     */
    public static int transferItemStackFromItemHandlerToBuilding(
        final IItemHandler handler,
        final IBuilding building,
        final ItemStack stack,
        final int qty)
    {
        int tempQty = qty;
        final Level level = building.getColony().getWorld();
        for (final BlockPos pos : building.getContainers())
        {
            if (WorldUtil.isBlockLoaded(level, pos))
            {
                final BlockEntity entity = level.getBlockEntity(pos);
                if (entity instanceof TileEntityRack tileEntityRack)
                {
                    tempQty = transferXOfFirstSlotInItemHandlerWithIntoNextFreeSlotInItemHandlerWithResult(handler, stack1 -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack1, stack, true, true), tempQty, tileEntityRack.getInventory());
                    if (tempQty <= 0)
                    {
                        return 0;
                    }
                }
            }
        }

        return tempQty;
    }

    /**
     * Method to put a given Itemstack in a given target {@link IItemHandler}. Trying to merge existing itemStacks if possible.
     *
     * @param stack         the itemStack to transfer.
     * @param targetHandler The {@link IItemHandler} that works as Target.
     * @return True when the swap was successful, false when not.
     */
    public static boolean transferItemStackIntoNextBestSlotInItemHandler(final ItemStack stack, @NotNull final IItemHandler targetHandler)
    {
        return transferItemStackIntoNextBestSlotInItemHandlerWithResult(stack, targetHandler).isEmpty();
    }

    /**
     * Method to put a given Itemstack in a given target {@link IItemHandler}. Trying to merge existing itemStacks if possible.
     *
     * @param stack         the itemStack to transfer.
     * @param targetHandler The {@link IItemHandler} that works as Target.
     * @return the rest of the stack.
     */
    public static ItemStack transferItemStackIntoNextBestSlotInItemHandlerWithResult(final ItemStack stack, @NotNull final IItemHandler targetHandler)
    {
        ItemStack sourceStack = stack.copy();

        if (ItemStackUtils.isEmpty(sourceStack))
        {
            return sourceStack;
        }

        sourceStack = mergeItemStackIntoNextBestSlotInItemHandlers(sourceStack, targetHandler);

        if (ItemStackUtils.isEmpty(sourceStack))
        {
            return sourceStack;
        }

        for (int i = 0; i < targetHandler.getSlots(); i++)
        {
            sourceStack = targetHandler.insertItem(i, sourceStack, false);
            if (ItemStackUtils.isEmpty(sourceStack))
            {
                return sourceStack;
            }
        }

        return sourceStack;
    }

    /**
     * Method to merge the ItemStacks from the given source {@link IItemHandler} to the given target {@link IItemHandler}. Trying to merge itemStacks or returning stack if not
     * possible.
     *
     * @param stack         the stack to add.
     * @param targetHandler The {@link IItemHandler} that works as Target.
     * @return an empty stack if all was transfered else the leftover.
     */
    public static ItemStack mergeItemStackIntoNextBestSlotInItemHandlers(
      final ItemStack stack,
      @NotNull final IItemHandler targetHandler)
    {
        if (ItemStackUtils.isEmpty(stack))
        {
            return stack;
        }
        ItemStack sourceStack = stack.copy();

        for (int i = 0; i < targetHandler.getSlots(); i++)
        {
            if (!ItemStackUtils.isEmpty(targetHandler.getStackInSlot(i)) && ItemStackUtils.compareItemStacksIgnoreStackSize(targetHandler.getStackInSlot(i), sourceStack))
            {
                sourceStack = targetHandler.insertItem(i, sourceStack, false);
                if (ItemStackUtils.isEmpty(sourceStack))
                {
                    return sourceStack;
                }
            }
        }
        return sourceStack;
    }

    public static boolean transferXOfFirstSlotInProviderWithIntoNextFreeSlotInItemHandler(
      @NotNull final ICapabilityProvider sourceProvider,
      @NotNull final Predicate<ItemStack> itemStackSelectionPredicate,
      final int amount, @NotNull final IItemHandler targetHandler)
    {
        return transferXOfFirstSlotInProviderWithIntoNextFreeSlotInItemHandlerWithResult(sourceProvider, itemStackSelectionPredicate, amount, targetHandler) == 0;
    }

    /**
     * Transfer items over and return the leftover that couldn't be transfered (0 if full amount was transfered).
     * @param sourceProvider
     * @param itemStackSelectionPredicate
     * @param amount
     * @param targetHandler
     * @return missing reminder compared to amount.
     */
    public static int transferXOfFirstSlotInProviderWithIntoNextFreeSlotInItemHandlerWithResult(
      @NotNull final ICapabilityProvider sourceProvider,
      @NotNull final Predicate<ItemStack> itemStackSelectionPredicate,
      final int amount, @NotNull final IItemHandler targetHandler)
    {
        int currentAmount = amount;
        for (final IItemHandler handler : getItemHandlersFromProvider(sourceProvider))
        {
            currentAmount = transferXOfFirstSlotInItemHandlerWithIntoNextFreeSlotInItemHandlerWithResult(handler, itemStackSelectionPredicate, currentAmount, targetHandler);

            if (currentAmount <= 0)
            {
                return 0;
            }
        }

        return currentAmount;
    }

    public static boolean transferXOfFirstSlotInItemHandlerWithIntoNextFreeSlotInItemHandler(
      @NotNull final IItemHandler sourceHandler,
      @NotNull final Predicate<ItemStack> itemStackSelectionPredicate,
      final int amount, @NotNull final IItemHandler targetHandler)
    {
        return transferXOfFirstSlotInItemHandlerWithIntoNextFreeSlotInItemHandlerWithResult(sourceHandler, itemStackSelectionPredicate, amount, targetHandler) == 0;
    }

    public static int transferXOfFirstSlotInItemHandlerWithIntoNextFreeSlotInItemHandlerWithResult(
      @NotNull final IItemHandler sourceHandler,
      @NotNull final Predicate<ItemStack> itemStackSelectionPredicate,
      final int amount, @NotNull final IItemHandler targetHandler)
    {
        int currentAmount = amount;
        int slot = 0;
        while (currentAmount > 0 && slot < sourceHandler.getSlots())
        {
            final int desiredItemSlot = InventoryUtils.getMatchingSlot(sourceHandler,
              itemStackSelectionPredicate::test);

            if (desiredItemSlot == -1)
            {
                return currentAmount;
            }

            final ItemStack returnStack = sourceHandler.extractItem(desiredItemSlot, currentAmount, false);

            if (!ItemStackUtils.isEmpty(returnStack))
            {
                final ItemStack result = InventoryUtils.transferItemStackIntoNextBestSlotInItemHandlerWithResult(returnStack, targetHandler);
                if (!result.isEmpty())
                {
                    sourceHandler.insertItem(desiredItemSlot, result, false);
                }

                if (result.getCount() == returnStack.getCount())
                {
                    return currentAmount;
                }

                currentAmount -= returnStack.getCount() - result.getCount();
            }

            slot++;
        }

        return currentAmount;
    }

    /**
     * Takes an item matching a predicate and moves it form one handler across multiple slots to the other to a specific slot.
     *
     * @param sourceHandler               the source handler.
     * @param itemStackSelectionPredicate the predicate.
     * @param amount                      the max amount to extract
     * @param targetHandler               the target.
     * @param slot                        the slot to put it in.
     * @return the count of items actually transferred
     */
    public static int transferXInItemHandlerIntoSlotInItemHandler(
      final IItemHandler sourceHandler,
      final Predicate<ItemStack> itemStackSelectionPredicate,
      final int amount,
      final IItemHandler targetHandler, final int slot)
    {
        int actualTransferred = 0;
        while (actualTransferred < amount)
        {
            final int transferred = InventoryUtils.transferXOfFirstSlotInItemHandlerWithIntoInItemHandler(
              sourceHandler,
              itemStackSelectionPredicate,
              amount - actualTransferred,
              targetHandler,
              slot);
            if (transferred <= 0)
            {
                break;
            }
            actualTransferred += transferred;
        }
        return actualTransferred;
    }

    /**
     * Takes an item matching a predicate and moves it form one handler to the other to a specific slot.
     *
     * @param sourceHandler               the source handler.
     * @param itemStackSelectionPredicate the predicate.
     * @param amount                      the max amount to extract
     * @param targetHandler               the target.
     * @param slot                        the slot to put it in.
     * @return the count of items actually transferred
     */
    public static int transferXOfFirstSlotInItemHandlerWithIntoInItemHandler(
      final IItemHandler sourceHandler,
      final Predicate<ItemStack> itemStackSelectionPredicate,
      final int amount,
      final IItemHandler targetHandler, final int slot)
    {
        final int desiredItemSlot = InventoryUtils.getMatchingSlot(sourceHandler,
          itemStackSelectionPredicate);

        if (desiredItemSlot == -1)
        {
            return 0;
        }
        final ItemStack returnStack = sourceHandler.extractItem(desiredItemSlot, amount, false);
        if (ItemStackUtils.isEmpty(returnStack))
        {
            return 0;
        }

        final ItemStack insertResult = targetHandler.insertItem(slot, returnStack, false);
        if (!ItemStackUtils.isEmpty(insertResult))
        {
            sourceHandler.insertItem(desiredItemSlot, insertResult, false);
            return returnStack.getCount() - insertResult.getCount();
        }
        return returnStack.getCount();
    }

    /**
     * Method to swap the ItemStacks from the given source {@link ICapabilityProvider} to the given target {@link IItemHandler}.
     *
     * @param sourceProvider The {@link ICapabilityProvider} that works as Source.
     * @param sourceIndex    The index of the slot that is being extracted from.
     * @param targetHandler  The {@link IItemHandler} that works as Target.
     * @return True when the swap was successful, false when not.
     */
    public static boolean transferItemStackIntoNextFreeSlotFromProvider(
      @NotNull final ICapabilityProvider sourceProvider,
      final int sourceIndex,
      @NotNull final IItemHandler targetHandler)
    {
        for (final IItemHandler handler : getItemHandlersFromProvider(sourceProvider))
        {
            if (transferItemStackIntoNextFreeSlotInItemHandler(handler, sourceIndex, targetHandler))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Method to swap the ItemStacks from the given source {@link IItemHandler} to the given target {@link IItemHandler}.
     *
     * @param handler The {@link IItemHandler} that works as Source.
     * @param stackPredicate The type of stack to pickup.
     * @param count how much to pick up.
     * @param targetHandler  The {@link IItemHandler} that works as Target.
     * @return True when the swap was successful, false when not.
     */
    public static boolean transferItemStackIntoNextFreeSlotFromItemHandler(
      @NotNull final IItemHandler handler,
      @NotNull final Predicate<ItemStack> stackPredicate,
      final int count,
      @NotNull final IItemHandler targetHandler)
    {
        int totalCount = count;

        int index = getMatchingSlot(handler, stackPredicate);
        while (index != -1)
        {
            final int localCount = Math.min(totalCount, handler.getStackInSlot(index).getCount());
            final boolean transferred = transferXOfItemStackIntoNextFreeSlotInItemHandler(handler, index, localCount, targetHandler);

            if (transferred)
            {
                totalCount -= localCount;
            }
            else
            {
                return false;
            }

            if (totalCount <= 0)
            {
                return true;
            }
            index = getMatchingSlot(handler, stackPredicate);
        }

        return false;
    }

    /**
     * Method to swap the ItemStacks from the given source {@link ICapabilityProvider} to the given target {@link IItemHandler}.
     *
     * @param sourceProvider The {@link ICapabilityProvider} that works as Source.
     * @param sourceIndex    The index of the slot that is being extracted from.
     * @param targetHandler  The {@link IItemHandler} that works as Target.
     * @return True when the swap was successful, false when not.
     */
    public static boolean transferItemStackIntoNextBestSlotFromProvider(
      @NotNull final ICapabilityProvider sourceProvider,
      final int sourceIndex,
      @NotNull final IItemHandler targetHandler)
    {
        for (final IItemHandler handler : getItemHandlersFromProvider(sourceProvider))
        {
            if (transferItemStackIntoNextBestSlotInItemHandler(handler, sourceIndex, targetHandler))
            {
                return true;
            }
        }

        return false;
    }


    /**
     * Remove a list of stacks from a given Itemhandler
     *
     * @param handler the itemHandler.
     * @param input   the list of stacks.
     * @return true if succesful.
     */
    public static boolean removeStacksFromItemHandler(final IItemHandler handler, final List<ItemStack> input)
    {
        final List<ItemStack> list = new ArrayList<>();
        int maxTries = 0;
        for (final ItemStack stack : input)
        {
            maxTries += ItemStackUtils.getSize(stack);
            list.add(stack.copy());
        }

        boolean success = true;
        int i = 0;
        int tries = 0;
        while (i < list.size() && tries < maxTries)
        {
            final ItemStack stack = list.get(i);
            final int slot = getMatchingSlot(handler, lStack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, lStack));

            if (slot == -1)
            {
                success = false;
                i++;
                continue;
            }

            final int removedSize = ItemStackUtils.getSize(handler.extractItem(slot, ItemStackUtils.getSize(stack), false));

            if (removedSize == ItemStackUtils.getSize(stack))
            {
                i++;
            }
            else
            {
                ItemStackUtils.changeSize(stack, -removedSize);
            }
            tries++;
        }

        return success && i >= list.size();
    }

    /**
     * Tries to remove a stack with its size from a given Itemhandler. Only removes sth if the whole size can be removed.
     *
     * @param handler the itemHandler.
     * @param input   the stack to remove.
     * @return true if removed the stack
     */
    public static boolean tryRemoveStackFromItemHandler(final IItemHandler handler, final ItemStack input)
    {
        int amount = input.getCount();

        for (int i = 0; i < handler.getSlots(); i++)
        {
            if (ItemStackUtils.compareItemStacksIgnoreStackSize(handler.getStackInSlot(i), input))
            {
                amount = amount - handler.extractItem(i, amount, false).getCount();

                if (amount == 0)
                {
                    return true;
                }
            }
        }

        final ItemStack revertStack = input.copy();
        revertStack.setCount(input.getCount() - amount);
        addItemStackToItemHandler(handler, revertStack);
        return false;
    }

    /**
     * Force remove a stack with a certain amount from a given Itemhandler
     *
     * @param handler the itemHandler.
     * @param input   the stack to remove.
     * @param count   the amount to remove.
     */
    public static void removeStackFromItemHandler(final IItemHandler handler, final ItemStack input, final int count)
    {
        final ItemStack workingStack = input.copy();
        int localCount = count;
        int tries = 0;
        while (tries < count)
        {
            final int slot = getMatchingSlot(handler, stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(workingStack, stack));
            if (slot == -1)
            {
                return;
            }

            final int removedSize = ItemStackUtils.getSize(handler.extractItem(slot, localCount, false));

            if (removedSize == count)
            {
                return;
            }
            else
            {
                localCount -= removedSize;
            }
            tries++;
        }
    }


    /**
     * Attempt to transfer as much item as possible from origin to target inventory
     *
     * @param origin the handler.
     * @param target   the world.
     * @return true if all item transfered, false if some item remain in origin
     */
    public static boolean transferAllItemHandler(final IItemHandler origin, final IItemHandler target)
    {
        for (int i = 0; i < origin.getSlots(); ++i)
        {
            final ItemStack itemStack = origin.getStackInSlot(i);
            if(!ItemStackUtils.isEmpty(itemStack))
            {
                if(addItemStackToItemHandler(target, itemStack))
                {
                    removeStackFromItemHandler(origin, itemStack, itemStack.getCount());
                }
                else return false;
            }
        }

        return true;
    }

    /**
     * Checks if all stacks given in the list are in at least one of the given the itemhandlers
     *
     * @param stacks   The stacks that should be in the itemhandlers
     * @param handlers The itemhandlers to check in
     * @return True when all stacks are in at least one of the handlers, false when not
     */
    public static boolean areAllItemsInItemHandlerList(@NotNull final List<ItemStack> stacks, @NotNull final Collection<IItemHandler> handlers)
    {
        if (stacks.isEmpty())
        {
            return true;
        }

        if (handlers.isEmpty())
        {
            return false;
        }

        final Map<ItemStack, Integer> requiredCountForStacks = getMergedCountedStacksFromList(stacks);

        return requiredCountForStacks.keySet().stream().allMatch(itemStack -> {
            final int countInHandlerList =
              handlers.stream().mapToInt(handler -> getItemCount(handler, itemStack1 -> ItemStackUtils.compareItemStacksIgnoreStackSize(itemStack, itemStack1))).sum();
            return countInHandlerList >= requiredCountForStacks.get(itemStack);
        });
    }

    /**
     * This method calculates the amount of items in itemstacks are contained within a list.
     *
     * @param stacks The stacks to count.
     * @return A map with a entry for each unique unified itemstack and its count in the list.
     */
    public static Map<ItemStack, Integer> getMergedCountedStacksFromList(@NotNull final List<ItemStack> stacks)
    {
        final Map<ItemStack, Integer> requiredCountForStacks = Maps.newHashMap();
        stacks.forEach(targetStack -> {
            final Optional<ItemStack>
              alreadyContained = requiredCountForStacks.keySet().stream().filter(itemStack -> ItemStackUtils.compareItemStacksIgnoreStackSize(itemStack, targetStack)).findFirst();

            if (alreadyContained.isPresent())
            {
                requiredCountForStacks.put(alreadyContained.get(), requiredCountForStacks.get(alreadyContained.get()) + targetStack.getCount());
            }
            else
            {
                requiredCountForStacks.put(targetStack, targetStack.getCount());
            }
        });

        return requiredCountForStacks;
    }

    /**
     * This method splits a map with an entry for each unique unified itemstack and its count into a list of itemstacks that represent the maps, taken the max stack size into
     * account.
     *
     * @param mergedCountedStacks the map with the unique unified itemstacks and their counts.
     * @return The list of itemstacks that represent the map, taken the max stack size into account.
     */
    public static List<ItemStack> splitMergedCountedStacksIntoMaxContentStacks(@NotNull final Map<ItemStack, Integer> mergedCountedStacks)
    {
        final List<ItemStack> list = Lists.newArrayList();
        for (final Map.Entry<ItemStack, Integer> itemStackIntegerEntry : mergedCountedStacks.entrySet())
        {
            final int minimalFullStacks = itemStackIntegerEntry.getValue() / itemStackIntegerEntry.getKey().getMaxStackSize();
            final int residualStackSize = itemStackIntegerEntry.getValue() % itemStackIntegerEntry.getKey().getMaxStackSize();

            for (int i = 0; i < minimalFullStacks; i++)
            {
                final ItemStack tobeAdded = itemStackIntegerEntry.getKey().copy();
                tobeAdded.setCount(tobeAdded.getMaxStackSize());

                list.add(tobeAdded);
            }

            if (residualStackSize > 0)
            {
                final ItemStack tobeAdded = itemStackIntegerEntry.getKey().copy();
                tobeAdded.setCount(residualStackSize);

                list.add(tobeAdded);
            }
        }

        return list;
    }

    /**
     * Searches a given itemhandler for the stacks given and returns the list that is contained in the itemhandler
     *
     * @param stacks  The stacks to search for
     * @param handler The handler to search in
     * @return The sublist of the stacks list contained in the itemhandler.
     */
    public static List<ItemStack> getContainedFromItemHandler(@NotNull final List<ItemStack> stacks, @NotNull final IItemHandler handler)
    {
        final List<ItemStack> result = Lists.newArrayList();

        final Map<ItemStack, Integer> inputCounts = getMergedCountedStacksFromList(stacks);
        final Map<ItemStack, Integer> inventoryCounts = getMergedCountedStacksFromList(getItemHandlerAsList(handler));

        final Map<ItemStack, Integer> resultingContained = new HashMap<>();
        inputCounts
          .forEach((itemStack, count) -> {

              int remainingCount = count;
              for (Map.Entry<ItemStack, Integer> entry : inventoryCounts.entrySet())
              {
                  ItemStack containedStack = entry.getKey();
                  final Integer containedCount = entry.getValue();
                  if (ItemStackUtils.compareItemStacksIgnoreStackSize(itemStack, containedStack))
                  {
                      remainingCount -= containedCount;
                  }
              }

              if (remainingCount <= 0)
              {
                  resultingContained.put(itemStack, count);
              }
          });

        resultingContained
          .forEach((itemStack, count) -> {
              final int fullStackCount = count / itemStack.getMaxStackSize();
              final int missingPartialCount = count % itemStack.getMaxStackSize();

              for (int i = 0; i < fullStackCount; i++)
              {
                  final ItemStack targetStack = itemStack.copy();
                  targetStack.setCount(targetStack.getMaxStackSize());

                  result.add(targetStack);
              }

              if (missingPartialCount != 0)
              {
                  final ItemStack targetStack = itemStack.copy();
                  targetStack.setCount(missingPartialCount);

                  result.add(targetStack);
              }
          });

        return result;
    }

    /**
     * Unifies a list of stacks so that they are all packed to together to the max stack size.
     *
     * @param stacks The stacks to pack.
     * @return The packed stacks
     */
    public static List<ItemStack> processItemStackListAndMerge(@NotNull final List<ItemStack> stacks)
    {
        return splitMergedCountedStacksIntoMaxContentStacks(getMergedCountedStacksFromList(stacks));
    }

    /**
     * Attempts a swap with the given itemstacks, from the source to the target inventory. Itemstacks in the target that match the given toKeepInTarget predicate will not be
     * swapped out, if swapping is needed
     *
     * @param targetInventory   The target inventory.
     * @param sourceInventories The source inventory.
     * @param toSwap            The list of stacks to swap.
     * @param toKeepInTarget    The predicate that determines what not to swap in the target.
     * @return True when moving was successfull, false when not
     */
    public static boolean moveItemStacksWithPossibleSwap(
      @NotNull final IItemHandler targetInventory,
      @NotNull final Collection<IItemHandler> sourceInventories,
      @NotNull final List<ItemStack> toSwap,
      @NotNull final Predicate<ItemStack> toKeepInTarget)
    {
        if (targetInventory.getSlots() < toSwap.size())
        {
            return false;
        }

        final Predicate<ItemStack> wantToKeep = toKeepInTarget.or(stack -> ItemStackUtils.compareItemStackListIgnoreStackSize(toSwap, stack));

        for (final ItemStack itemStack : toSwap)
        {
            for (final IItemHandler sourceInventory : sourceInventories)
            {
                if (tryRemoveStackFromItemHandler(sourceInventory, itemStack))
                {
                    ItemStack forcingResult = forceItemStackToItemHandler(targetInventory, itemStack, wantToKeep);

                    if (forcingResult != null && !forcingResult.isEmpty())
                    {
                        addItemStackToItemHandler(sourceInventory, forcingResult);
                    }
                }
            }
            return false;
        }

        return true;
    }

    /**
     * Search for a certain itemStack in the inventory and decrease it by 1.
     *
     * @param invWrapper the inventory item handler.
     * @param itemStack  the itemStack to decrease.
     */
    public static void reduceStackInItemHandler(final IItemHandler invWrapper, final ItemStack itemStack)
    {
        reduceStackInItemHandler(invWrapper, itemStack, 1);
    }

    /**
     * Search for a certain itemStack in the inventory and decrease it by a certain quantity.
     *
     * @param invWrapper the inventory item handler.
     * @param itemStack  the itemStack to decrease.
     * @param quantity   the quantity.
     */
    public static void reduceStackInItemHandler(final IItemHandler invWrapper, final ItemStack itemStack, final int quantity)
    {
        for (int i = 0; i < invWrapper.getSlots(); i++)
        {
            if (ItemStackUtils.compareItemStacksIgnoreStackSize(invWrapper.getStackInSlot(i), itemStack))
            {
                invWrapper.getStackInSlot(i).shrink(quantity);
                return;
            }
        }
    }

    /**
     * Search for a certain itemStack in the inventory and decrease it by a certain quantity.
     * This is "bucket-aware", in that if a full bucket is used by the decrease, an empty
     * bucket replaces it.
     *
     * @param invWrapper the inventory item handler.
     * @param itemStack  the itemStack to decrease.
     * @param quantity   the quantity.
     */
    public static void reduceBucketAwareStackInItemHandler(final IItemHandler invWrapper, final ItemStack itemStack, final int quantity)
    {
        if  (attemptReduceStackInItemHandler(invWrapper, itemStack, quantity)) 
        {
            if (itemStack.getItem() instanceof BucketItem && itemStack.getItem() != Items.BUCKET) 
            {
                addItemStackToItemHandler(invWrapper, new ItemStack(Items.BUCKET, quantity));
            }
        }
    }

    /**
     * Search for a certain itemStack in the inventory and decrease it by a certain quantity.
     *
     * @param invWrapper the inventory item handler.
     * @param itemStack  the itemStack to decrease.
     * @param quantity   the quantity.
     * @return true if successfully.
     */
    public static boolean attemptReduceStackInItemHandler(final IItemHandler invWrapper, final ItemStack itemStack, final int quantity)
    {
        return attemptReduceStackInItemHandler(invWrapper, itemStack, quantity, false, false);
    }

    /**
     * Search for a certain itemStack in the inventory and decrease it by a certain quantity.
     *
     * @param invWrapper the inventory item handler.
     * @param itemStack  the itemStack to decrease.
     * @param quantity   the quantity.
     * @param ignoreDamage ignore damage values.
     * @param ignoreNBT ignore NBT values.
     * @return true if successfully.
     */
    public static boolean attemptReduceStackInItemHandler(final IItemHandler invWrapper, final ItemStack itemStack, final int quantity, final boolean ignoreDamage, final boolean ignoreNBT)
    {
        if (getItemCount(invWrapper, stack -> !stack.isEmpty() && ItemStackUtils.compareItemStacksIgnoreStackSize(stack, itemStack, !ignoreDamage, !ignoreNBT)) < quantity)
        {
            return false;
        }

        int qty = quantity;
        for (int i = 0; i < invWrapper.getSlots(); i++)
        {
            final ItemStack stack = invWrapper.getStackInSlot(i);
            if (ItemStackUtils.compareItemStacksIgnoreStackSize(stack, itemStack, !ignoreDamage, !ignoreNBT))
            {
                if (stack.getCount() >= qty)
                {
                    invWrapper.extractItem(i, qty, false);
                    return true;
                }
                else
                {
                    qty -= stack.getCount();
                    invWrapper.extractItem(i, stack.getCount(), false);
                }
            }
        }
        return false;
    }

    /**
     * Sums up all items in the given provider/handlers
     *
     * @param provider inventory provider
     * @param handlers inventory handlers
     * @return Map of IdentityItemstorage
     */
    public static Map<ItemStorage, ItemStorage> getAllItemsForProviders(final ICapabilityProvider provider, final IItemHandler... handlers)
    {
        final Set<IItemHandler> providerHandlers = getItemHandlersFromProvider(provider);
        if (handlers != null)
        {
            providerHandlers.addAll(Arrays.asList(handlers));
        }

        return getAllItemsForProviders(providerHandlers);
    }

    /**
     * Sums up all items in the given handlers
     *
     * @param handlerList inventory handlers
     * @return Map of IdentityItemstorage
     */
    public static Map<ItemStorage, ItemStorage> getAllItemsForProviders(Set<IItemHandler> handlerList)
    {
        final Map<ItemStorage, ItemStorage> storageMap = new HashMap<>();
        for (final IItemHandler handler : handlerList)
        {
            for (int i = 0; i < handler.getSlots(); i++)
            {
                final ItemStack containedStack = handler.getStackInSlot(i);
                if (!ItemStackUtils.isEmpty(containedStack))
                {
                    final ItemStorage storage = new ItemStorage(containedStack.copy(), false, false);

                    if (storageMap.containsKey(storage))
                    {
                        final ItemStorage existing = storageMap.get(storage);
                        existing.setAmount(existing.getAmount() + storage.getAmount());
                    }
                    else
                    {
                        storageMap.put(storage, storage);
                    }
                }
            }
        }

        return storageMap;
    }

    /**
     * Returns whether two sets of itemstorage has the same content
     *
     * @param first     First set of item storages
     * @param second    Second set of item storages
     * @param showTrace whther to print a stacktrace on false
     * @return true if matching
     */
    public static boolean doStorageSetsMatch(Map<ItemStorage, ItemStorage> first, Map<ItemStorage, ItemStorage> second, boolean showTrace)
    {

        for (final ItemStorage storage : first.keySet())
        {
            final ItemStorage compareStorage = second.get(storage);

            if (compareStorage == null || storage.getAmount() != compareStorage.getAmount())
            {
                if (showTrace)
                {
                    Log.getLogger().warn("Possible inventory issue, not matching:", new Exception());
                }
                return false;
            }
        }

        for (final ItemStorage storage : second.keySet())
        {
            final ItemStorage compareStorage = first.get(storage);

            if (compareStorage == null || storage.getAmount() != compareStorage.getAmount())
            {
                if (showTrace)
                {
                    Log.getLogger().warn("Possible inventory issue, not matching:", new Exception());
                }
                return false;
            }
        }

        return true;
    }

    /**
     * Transfers food items from the source with the required saturation value, or as much as possible.
     *
     * @param source             to extract items from
     * @param target             to insert intems into
     * @param requiredSaturation required saturation value
     * @param foodPredicate      food choosing predicate
     * @returns a map of transferred items
     */
    public static Object2IntMap<ItemStack> transferFoodUpToSaturation(
      final ICapabilityProvider source,
      final IItemHandler target,
      final int requiredSaturation,
      final Predicate<ItemStack> foodPredicate)
    {
        Set<IItemHandler> handlers = getItemHandlersFromProvider(source);

        int foundSaturation = 0;

        Object2IntOpenHashMap<ItemStack> transferredItemMap = new Object2IntOpenHashMap<>();
        transferredItemMap.defaultReturnValue(0); // avoid nulls on get()

        for (final IItemHandler handler : handlers)
        {
            for (int i = 0; i < handler.getSlots(); i++)
            {
                final ItemStack stack = handler.getStackInSlot(i);

                if (!ItemStackUtils.isEmpty(stack) && foodPredicate.test(stack))
                {
                    // Found food
                    final FoodProperties itemFood = stack.getItem().getFoodProperties(stack, null);
                    if (itemFood == null)
                    {
                        continue;
                    }

                    int amount = (int) Math.round(Math.ceil((requiredSaturation - foundSaturation) / (float) itemFood.getNutrition()));

                    final ItemStack extractedFood;
                    if (amount > stack.getCount())
                    {
                        // Not enough yet
                        foundSaturation += stack.getCount() * itemFood.getNutrition();
                        extractedFood = handler.extractItem(i, stack.getCount(), false);
                    }
                    else
                    {
                        // Stack is sufficient
                        extractedFood = handler.extractItem(i, amount, false);
                        foundSaturation = requiredSaturation;
                    }

                    if (!ItemStackUtils.isEmpty(extractedFood)) 
                    {
                        transferredItemMap.addTo(extractedFood, extractedFood.getCount());
                    }
                    
                    if (!ItemStackUtils.isEmpty(extractedFood))
                    {
                        if (!addItemStackToItemHandler(target, extractedFood))
                        {
                            // Swap if need
                            final int slot = getMatchingSlot(target, s -> !foodPredicate.test(s));
                            if (slot != -1)
                            {
                                final ItemStack swappedItem = target.extractItem(slot, target.getStackInSlot(slot).getCount(), false);
                                addItemStackToProvider(source, swappedItem);
                                addItemStackToItemHandler(target, extractedFood);
                            }
                        }
                    }

                    if (foundSaturation >= requiredSaturation)
                    {
                        return transferredItemMap;
                    }
                }
            }
        }

        return transferredItemMap;
    }

    /**
     * Tries to put given itemstack in hotbar and select it, fails when player inventory is full, successes otherwise.
     *
     * @param itemStack   itemstack to put into player's inv
     * @param player player entity
     * @return true if item was put into player's inv, false if dropped
     */
    public static boolean putItemToHotbarAndSelectOrDrop(final ItemStack itemStack, final Player player)
    {
        final Inventory playerInv = player.getInventory();

        final int emptySlot = playerInv.getFreeSlot();
        if (emptySlot == -1) // try full inv first
        {
            player.drop(itemStack, false);
            MessageUtils.format(itemStack.getDisplayName().copy())
                .append(MESSAGE_INFO_PLAYER_INVENTORY_FULL_HOTBAR_INSERT)
                .sendTo(player);
            return false;
        }
        else
        {
            final int hotbarSlot = playerInv.getSuitableHotbarSlot();
            final ItemStack curHotbarItem = playerInv.getItem(hotbarSlot);

            // check if we need to make space first
            if (!curHotbarItem.isEmpty())
            {
                playerInv.setItem(emptySlot, curHotbarItem);
            }

            playerInv.setItem(hotbarSlot, itemStack);
            playerInv.selected = hotbarSlot;
            playerInv.setChanged();
            updateHeldItemFromServer(player);
            return true;
        }
    }

    /**
     * If item is already in inventory then it's moved to hotbar and returned.
     * Else {@link #putItemToHotbarAndSelectOrDrop} is called with itemstack created from given factory.
     *
     * @param item             item to search for
     * @param player           player inventory to check and use
     * @param itemStackFactory factory for new item if not found
     * @return itemstack in hotbar or dropped in front of player
     */
    public static ItemStack getOrCreateItemAndPutToHotbarAndSelectOrDrop(final Item item,
        final Player player,
        final Supplier<ItemStack> itemStackFactory)
    {
        final Inventory playerInv = player.getInventory();

        for (int slot = 0; slot < playerInv.items.size(); slot++)
        {
            final ItemStack itemSlot = playerInv.getItem(slot);
            if (itemSlot.getItem() == item)
            {
                if (!Inventory.isHotbarSlot(slot))
                {
                    playerInv.pickSlot(slot);
                }
                else
                {
                    playerInv.selected = slot;
                }
                playerInv.setChanged();
                updateHeldItemFromServer(player);
                return itemSlot;
            }
        }

        final ItemStack newItem = itemStackFactory.get();
        putItemToHotbarAndSelectOrDrop(newItem, player);

        return newItem;
    }

    /**
     * Updates held item slot on client. Client autoupdates server once per tick.
     *
     * @param player player to sync
     */
    private static void updateHeldItemFromServer(final Player player)
    {
        if (player instanceof ServerPlayer)
        {
            ((ServerPlayer) player).server.getPlayerList().sendAllPlayerInfo((ServerPlayer) player);
        }
    }

    /**
     * Gets the first matching itemstack from a list
     *
     * @param stacks
     * @param stackPredicate
     * @return
     */
    public static ItemStack getFirstMatch(final List<ItemStack> stacks, Predicate<ItemStack> stackPredicate)
    {
        for (final ItemStack stack : stacks)
        {
            if (stackPredicate.test(stack))
            {
                return stack;
            }
        }

        return null;
    }

    /**
     * This method will first try to remove items from the building inventory, then from the player inventory.
     * If the building inventory has enough items, then the player inventory will not be touched.
     *
     * @param building the building to take items from.
     * @param player the player to take items from.
     * @param itemsToTake the items to take from the building and player inventory.
     * @return a ItemStorage (item and amount) which could not be satisfied from the building and player inventory. For successful complete removal, the amount will be 0.
     */
    public static ItemStorage reduceBuildingThenPlayerInventory(final IBuilding building, final Player player, final ItemStorage itemsToTake, final Predicate<ItemStack> buildingPredicate, final Predicate<ItemStack> playerPredicate)
    {
        final InvWrapper playerInv = new InvWrapper(player.getInventory());

        int toRemoveLeft = itemsToTake.getAmount();
        Item item = itemsToTake.getItemStack().getItem();

        if (building != null)
        {
            final Map<IItemHandler,List<Integer>> buildingSlotsWithMaterial = InventoryUtils.findAllSlotsInProviderWith(building, buildingPredicate);
            if (!buildingSlotsWithMaterial.isEmpty())
            {
                for (Map.Entry<IItemHandler, List<Integer>> entry : buildingSlotsWithMaterial.entrySet())
                {
                    final IItemHandler univInventory = entry.getKey();
                    for (Integer slotNum : entry.getValue())
                    {
                        toRemoveLeft = toRemoveLeft - univInventory.extractItem(slotNum, toRemoveLeft, false).getCount();
                        if (toRemoveLeft <= 0)
                        {
                            break;
                        }
                    }
                    if (toRemoveLeft <= 0)
                    {
                        break;
                    }
                }
            }
        }

        if (toRemoveLeft > 0)
        {
            final List<Integer> playerSlotsWithMaterial = InventoryUtils.findAllSlotsInItemHandlerWith(playerInv, playerPredicate);
            for (Integer slotNum : playerSlotsWithMaterial)
            {
                toRemoveLeft = toRemoveLeft - playerInv.extractItem(slotNum, toRemoveLeft, false).getCount();
                if (toRemoveLeft <= 0)
                {
                    break;
                }
            }
        }

        final ItemStorage unsatisfiedItems = new ItemStorage(item, toRemoveLeft);

        return unsatisfiedItems;
    }
}
