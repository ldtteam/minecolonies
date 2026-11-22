package com.minecolonies.api.util;

import com.minecolonies.api.colony.buildings.IBuilding;
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
import net.minecraftforge.items.IItemHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.minecolonies.api.util.ItemStackUtils.spawnItemStack;
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
     * Get the slot in an inventory for a target item/block/predicate.
     * @param itemHandler the inventory.
     * @param targetItem what to check for.
     * @return the slot.
     */
    public static List<Integer> getAllMatchingSlot(@NotNull final IItemHandler itemHandler, @NotNull final Item targetItem) { return getAllMatchingSlot(itemHandler,stack -> !stack.isEmpty() && stack.getItem() == targetItem); }
    public static List<Integer> getAllMatchingSlot(@NotNull final IItemHandler itemHandler, @NotNull final Block block) { return getAllMatchingSlot(itemHandler, Item.byBlock(block)); }
    public static List<Integer> getAllMatchingSlot(@NotNull final IItemHandler itemHandler, @NotNull final Predicate<ItemStack> predicate)
    {
        final List<Integer> matchingSlots = new ArrayList<>();
        for (int slot = 0; slot < itemHandler.getSlots(); slot++)
        {
            if (predicate.test(itemHandler.getStackInSlot(slot)))
            {
                return matchingSlots;
            }
        }

        return matchingSlots;
    }

    /**
     * Returns the number of matching items in an {@link IItemHandler}.
     *
     * @param itemHandler {@link IItemHandler} to scan.
     * @param targetItem  what to check for.
     * @return quantity of items.
     */
    public static int getItemCount(@Nullable final IItemHandler itemHandler, @NotNull final Item targetItem) { return getItemCount(itemHandler, stack -> !stack.isEmpty() && stack.getItem() == targetItem); }
    public static int getItemCount(@Nullable final IItemHandler itemHandler, @NotNull final ItemStack targetStack) { return getItemCount(itemHandler, stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, targetStack)); }

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
     * Insert a list of itemStacks into an inventory.
     * @param handler the inventory.
     * @param stacks the stacks.
     * @return the leftover.
     */
    public static List<ItemStack> insert(final IItemHandler handler, final List<ItemStack> stacks)
    {
        final List<ItemStack> remainder = new ArrayList<>();
        for (final ItemStack stack : stacks)
        {
            final ItemStack leftOver = insert(handler, stack);
            if (!ItemStackUtils.isEmpty(leftOver))
            {
                remainder.add(leftOver);
            }
        }
        return remainder;
    }

    /**
     * Insert an itemstack into an inventory.
     * @param handler the inventory.
     * @param stack the stack.
     * @return the leftover.
     */
    public static ItemStack insert(final IItemHandler handler, final ItemStack stack)
    {
        ItemStack insertStack = stack;
        for (int slot = 0; slot < handler.getSlots(); slot++)
        {
            final ItemStack stackInSlot = handler.getStackInSlot(slot);
            if (ItemStackUtils.compareItemStacksIgnoreStackSize(stackInSlot, insertStack))
            {
                insertStack = handler.insertItem(slot, insertStack, false);
            }
            if (insertStack.isEmpty())
            {
                return ItemStack.EMPTY;
            }
        }

        for (int slot = 0; slot < handler.getSlots(); slot++)
        {
            insertStack = handler.insertItem(slot, insertStack, false);
            if (insertStack.isEmpty())
            {
                return ItemStack.EMPTY;
            }
        }

        return insertStack;
    }

    /**
     * Extract a qty of items from an inventory.
     * @param handler the inventory.
     * @param stack the stack to match on.
     * @param qty the qty of items.
     * @return a tuple of list of extracted stacks & qty that was actually extracted.
     */
    public static Tuple<List<ItemStack>, Integer> extract(final IItemHandler handler, final ItemStack stack, final int qty) { return extract(handler, st -> ItemStackUtils.compareItemStacksIgnoreStackSize(st, stack), qty); }
    public static Tuple<List<ItemStack>, Integer> extract(final IItemHandler handler, final Predicate<ItemStack> stackPredicate, final int qty)
    {
        final List<ItemStack> extractedStacks = new ArrayList<>();
        int progressQty = 0;

        for (int slot = 0; slot < handler.getSlots(); slot++)
        {
            final ItemStack stackInSlot = handler.getStackInSlot(slot);
            if (stackPredicate.test(stackInSlot))
            {
                final ItemStack extractedStack = handler.extractItem(slot, qty - progressQty, false);
                if (extractedStack.isEmpty())
                {
                    continue;
                }
                progressQty += extractedStack.getCount();
                extractedStacks.add(extractedStack);
                if (progressQty >= qty)
                {
                    return new Tuple<>(extractedStacks, progressQty);
                }
            }
        }
        return new Tuple<>(extractedStacks, progressQty);
    }

    /**
     * Transfer some itemStacks from an origin inventory to a destination inventory.
     * @param origin the origin inventory.
     * @param destination the destination.
     * @param stack the stack to be transferred.
     * @param qty the qty to be transferred of that stack.
     * @return the actually transferred qty
     */
    public static int transfer(final IItemHandler origin, final IItemHandler destination, ItemStack stack, final int qty) { return transfer(origin, destination, st -> ItemStackUtils.compareItemStacksIgnoreStackSize(st, stack), qty); }
    public static int transfer(final IItemHandler origin, final IItemHandler destination, ItemStack stack) { return transfer(origin, destination, st -> ItemStackUtils.compareItemStacksIgnoreStackSize(st, stack), stack.getCount()); }
    public static int transfer(final IItemHandler origin, final IItemHandler destination, final Predicate<ItemStack> predicate, final int qty)
    {
        final Tuple<List<ItemStack>, Integer> extractedStacks = extract(origin, predicate, qty);
        int transferedQty = extractedStacks.getB();
        final List<ItemStack> leftOver = new ArrayList<>();
        for (final ItemStack stack : extractedStacks.getA())
        {
            leftOver.add(insert(destination, stack));
        }

        // Re-insert left-overs into origin.
        for (final ItemStack stack : leftOver)
        {
            transferedQty -= stack.getCount();
            insert(origin, stack);
        }

        return transferedQty;
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
                    tempQty -= transfer(handler, tileEntityRack.getInventory(), stack1 -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack1, stack, true, true), tempQty);
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
     * Attempt to transfer as much item as possible from origin to target inventory
     *
     * @param origin the handler.
     * @param target the target.
     * @return true if all item transfered, false if some item remain in origin
     */
    public static boolean transferAllItemHandler(final IItemHandler origin, final IItemHandler target)
    {
        boolean fullSuccess = true;
        for (int i = 0; i < origin.getSlots(); ++i)
        {
            final ItemStack itemStack = origin.getStackInSlot(i);
            final int count = itemStack.getCount();
            if (transfer(origin, target, itemStack, count) < count)
            {
                fullSuccess = false;
            }
        }

        return fullSuccess;
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
        final Tuple<List<ItemStack>, Integer> result = extract(invWrapper, itemStack, quantity);
        if (result.getB() > 0 && itemStack.getItem() instanceof BucketItem && itemStack.getItem() != Items.BUCKET)
        {
            insert(invWrapper, new ItemStack(Items.BUCKET, result.getB()));
        }
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
      final IItemHandler source,
      final IItemHandler target,
      final int requiredSaturation,
      final Predicate<ItemStack> foodPredicate)
    {
        int foundSaturation = 0;

        Object2IntOpenHashMap<ItemStack> transferredItemMap = new Object2IntOpenHashMap<>();
        transferredItemMap.defaultReturnValue(0); // avoid nulls on get()

        for (int i = 0; i < source.getSlots(); i++)
        {
            final ItemStack stack = source.getStackInSlot(i);

            if (!ItemStackUtils.isEmpty(stack) && foodPredicate.test(stack))
            {
                // Found food
                final FoodProperties itemFood = stack.getItem().getFoodProperties(stack, null);
                if (itemFood == null || itemFood.getNutrition() <= 0)
                {
                    continue;
                }

                int amount = (int) Math.ceil((requiredSaturation - foundSaturation) / (float) itemFood.getNutrition());

                final ItemStack extractedFood;
                if (amount > stack.getCount())
                {
                    // Not enough yet
                    foundSaturation += stack.getCount() * itemFood.getNutrition();
                    extractedFood = source.extractItem(i, stack.getCount(), false);
                }
                else
                {
                    // Stack is sufficient
                    extractedFood = source.extractItem(i, amount, false);
                    foundSaturation = requiredSaturation;
                }

                if (!ItemStackUtils.isEmpty(extractedFood))
                {
                    transferredItemMap.addTo(extractedFood, extractedFood.getCount());
                }

                if (!ItemStackUtils.isEmpty(extractedFood))
                {
                    final ItemStack leftOver = insert(target, extractedFood);
                    if (!ItemStackUtils.isEmpty(leftOver))
                    {
                        // Swap if need
                        final int slot = getMatchingSlot(target, s -> !foodPredicate.test(s));
                        if (slot != -1)
                        {
                            final ItemStack swappedItem = target.extractItem(slot, target.getStackInSlot(slot).getCount(), false);
                            insert(source, swappedItem);
                            insert(target, leftOver);
                        }
                        else
                        {
                            insert(source, leftOver);
                        }
                    }
                }

                if (foundSaturation >= requiredSaturation)
                {
                    return transferredItemMap;
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
     * Get number of empty slots in inventory.
     * @param handler the inventory.
     * @return number of empty slots.
     */
    public static int getEmptySlots(final IItemHandler handler)
    {
        int emptySlots = 0;
        for (int slot = 0; slot < handler.getSlots(); slot++)
        {
            if (ItemStackUtils.isEmpty(handler.getStackInSlot(slot)))
            {
                emptySlots++;
            }
        }
        return emptySlots;
    }
}
