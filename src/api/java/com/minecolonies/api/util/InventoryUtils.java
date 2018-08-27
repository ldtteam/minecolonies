package com.minecolonies.api.util;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.IToolType;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

/**
 * Utility methods for the inventories.
 */
public class InventoryUtils
{
    /**
     * Several values to spawn items in the world.
     */
    private static final double SPAWN_MODIFIER    = 0.8D;
    private static final double SPAWN_ADDITION    = 0.1D;
    private static final int    MAX_RANDOM_SPAWN  = 21;
    private static final int    MIN_RANDOM_SPAWN  = 10;
    private static final double MOTION_MULTIPLIER = 0.05000000074505806D;
    private static final double MOTION_Y_MIN      = 0.20000000298023224D;

    /**
     * Private constructor to hide the implicit one.
     */
    private InventoryUtils()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Filters a list of items, matches the stack using {@link
     * #compareItems(ItemStack, Item, int)}, in an {@link IItemHandler}. Uses
     * the MetaData and {@link #getItemFromBlock(Block)} as parameters for the
     * Predicate.
     *
     * @param itemHandler Inventory to filter in
     * @param block       Block to filter
     * @param metaData    the damage value.
     * @return List of item stacks
     */
    @NotNull
    public static List<ItemStack> filterItemHandler(@NotNull final IItemHandler itemHandler, @NotNull final Block block, final int metaData)
    {
        return filterItemHandler(itemHandler, (ItemStack stack) -> compareItems(stack, getItemFromBlock(block), metaData));
    }

    /**
     * Filters a list of items, that match the given predicate, in an {@link
     * IItemHandler}.
     *
     * @param itemHandler                 The IItemHandler to get items from.
     * @param itemStackSelectionPredicate The predicate to match the stack to.
     * @return List of item stacks that match the given predicate.
     */
    @NotNull
    public static List<ItemStack> filterItemHandler(@NotNull final IItemHandler itemHandler, @NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        @NotNull final ArrayList<ItemStack> filtered = new ArrayList<>();
        //Check every itemHandler slot
        for (int slot = 0; slot < itemHandler.getSlots(); slot++)
        {
            final ItemStack stack = itemHandler.getStackInSlot(slot);
            if (!ItemStackUtils.isEmpty(stack) && itemStackSelectionPredicate.test(stack))
            {
                filtered.add(stack);
            }
        }
        return filtered;
    }

    /**
     * Compares whether or not the item in an itemstack is equal to a given
     * item.
     *
     * @param itemStack  ItemStack to check.
     * @param targetItem Item to check.
     * @param itemDamage the item damage value.
     * @return True when item in item stack is equal to target item.
     */
    private static boolean compareItems(@Nullable final ItemStack itemStack, final Item targetItem, final int itemDamage)
    {
        return !ItemStackUtils.isEmpty(itemStack) && itemStack.getItem() == targetItem && (itemStack.getItemDamage() == itemDamage || itemDamage == -1);
    }

    /**
     * Converts a Block to its Item so it can be compared.
     *
     * @param block the block to convert
     * @return an item from the registry
     */
    public static Item getItemFromBlock(final Block block)
    {
        return Item.getItemFromBlock(block);
    }

    /**
     * Filters a list of items, matches the stack using {@link
     * #compareItems(ItemStack, Item, int)}, with targetItem and itemDamage as
     * parameters, in an {@link IItemHandler}.
     *
     * @param itemHandler Inventory to get items from
     * @param targetItem  Item to look for
     * @param itemDamage  the damage value.
     * @return List of item stacks with the given item in inventory
     */
    @NotNull
    public static List<ItemStack> filterItemHandler(@NotNull final IItemHandler itemHandler, @NotNull final Item targetItem, final int itemDamage)
    {
        return filterItemHandler(itemHandler, (ItemStack stack) -> compareItems(stack, targetItem, itemDamage));
    }

    /**
     * Returns the index of the first occurrence of the block in the {@link
     * IItemHandler}.
     *
     * @param itemHandler {@link IItemHandler} to check.
     * @param block       Block to find.
     * @param itemDamage  the damage value.
     * @return Index of the first occurrence.
     */
    public static int findFirstSlotInItemHandlerWith(@NotNull final IItemHandler itemHandler, @NotNull final Block block, final int itemDamage)
    {
        return findFirstSlotInItemHandlerWith(itemHandler, getItemFromBlock(block), itemDamage);
    }

    /**
     * Returns the index of the first occurrence of the Item with the given
     * ItemDamage in the {@link IItemHandler}.
     *
     * @param itemHandler {@link IItemHandler} to check
     * @param targetItem  Item to find.
     * @param itemDamage  The damage value to match on the stack.
     * @return Index of the first occurrence
     */
    public static int findFirstSlotInItemHandlerWith(@NotNull final IItemHandler itemHandler, @NotNull final Item targetItem, final int itemDamage)
    {
        return findFirstSlotInItemHandlerWith(itemHandler, (ItemStack stack) -> compareItems(stack, targetItem, itemDamage));
    }

    /**
     * Returns the index of the first occurrence of an ItemStack that matches
     * the given predicate in the {@link IItemHandler}.
     *
     * @param itemHandler                 ItemHandler to check
     * @param itemStackSelectionPredicate The predicate to match.
     * @return Index of the first occurrence
     */
    public static int findFirstSlotInItemHandlerWith(@NotNull final IItemHandler itemHandler, @NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        for (int slot = 0; slot < itemHandler.getSlots(); slot++)
        {
            if (itemStackSelectionPredicate.test(itemHandler.getStackInSlot(slot)))
            {
                return slot;
            }
        }

        return -1;
        //TODO: Later harden contract to remove compare on slot := -1
        //throw new IllegalStateException("Item "+targetItem.getTranslationKey() + " not found in ItemHandler!");
    }

    /**
     * Returns the indexes of all occurrences of an ItemStack that matches
     * the given predicate in the {@link IItemHandler}.
     *
     * @param itemHandler                 ItemHandler to check
     * @param itemStackSelectionPredicate The predicate to match.
     * @return list of Indexes of the occurrences
     */
    public static List<Integer> findAllSlotsInItemHandlerWith(@NotNull final IItemHandler itemHandler, @NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        final List<Integer> returnList = new ArrayList<>();
        for (int slot = 0; slot < itemHandler.getSlots(); slot++)
        {
            if (itemStackSelectionPredicate.test(itemHandler.getStackInSlot(slot)))
            {
                returnList.add(slot);
            }
        }

        return returnList;
    }

    /**
     * Returns the amount of occurrences in the {@link IItemHandler}.
     *
     * @param itemHandler {@link IItemHandler} to scan.
     * @param block       The block to count
     * @param itemDamage  the damage value
     * @return Amount of occurrences of stacks that match the given block and
     * ItemDamage
     */
    public static int getItemCountInItemHandler(@NotNull final IItemHandler itemHandler, @NotNull final Block block, final int itemDamage)
    {
        return getItemCountInItemHandler(itemHandler, getItemFromBlock(block), itemDamage);
    }

    /**
     * Returns the amount of occurrences in the {@link IItemHandler}.
     *
     * @param itemHandler {@link IItemHandler} to scan.
     * @param targetItem  Item to count
     * @param itemDamage  the item damage value.
     * @return Amount of occurrences of stacks that match the given item and
     * ItemDamage
     */
    public static int getItemCountInItemHandler(@NotNull final IItemHandler itemHandler, @NotNull final Item targetItem, final int itemDamage)
    {
        return getItemCountInItemHandler(itemHandler, (ItemStack stack) -> compareItems(stack, targetItem, itemDamage));
    }

    /**
     * Returns the amount of occurrences in the {@link IItemHandler}.
     *
     * @param itemHandler                 {@link IItemHandler} to scan.
     * @param itemStackSelectionPredicate The predicate used to select the
     *                                    stacks to count.
     * @return Amount of occurrences of stacks that match the given predicate.
     */
    public static int getItemCountInItemHandler(@NotNull final IItemHandler itemHandler, @NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        return filterItemHandler(itemHandler, itemStackSelectionPredicate).stream().mapToInt(ItemStackUtils::getSize).sum();
    }

    /**
     * Checks if a player has a block in the {@link IItemHandler}. Checked by
     * {@link #getItemCountInItemHandler(IItemHandler, Block, int)} &gt; 0;
     *
     * @param itemHandler {@link IItemHandler} to scan
     * @param block       Block to count
     * @param itemDamage  the damage value.
     * @return True when in {@link IItemHandler}, otherwise false
     */
    public static boolean hasItemInItemHandler(@NotNull final IItemHandler itemHandler, @NotNull final Block block, final int itemDamage)
    {
        return hasItemInItemHandler(itemHandler, getItemFromBlock(block), itemDamage);
    }

    /**
     * Checks if a player has an item in the {@link IItemHandler}. Checked by
     * {@link #getItemCountInItemHandler(IItemHandler, Item, int)} &gt; 0;
     *
     * @param itemHandler {@link IItemHandler} to scan
     * @param item        Item to count
     * @param itemDamage  the damage value of the item.
     * @return True when in {@link IItemHandler}, otherwise false
     */
    public static boolean hasItemInItemHandler(@NotNull final IItemHandler itemHandler, @NotNull final Item item, final int itemDamage)
    {
        return hasItemInItemHandler(itemHandler, (ItemStack stack) -> compareItems(stack, item, itemDamage));
    }

    /**
     * Checks if a player has an item in the {@link IItemHandler}. Checked by
     * {@link InventoryUtils#getItemCountInItemHandler(IItemHandler, Predicate)}
     * &gt; 0;
     *
     * @param itemHandler                 {@link IItemHandler} to scan
     * @param itemStackSelectionPredicate The predicate to match the ItemStack
     *                                    to.
     * @return True when in {@link IItemHandler}, otherwise false
     */
    public static boolean hasItemInItemHandler(@NotNull final IItemHandler itemHandler, @NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        return getItemCountInItemHandler(itemHandler, itemStackSelectionPredicate) > 0;
    }

    /**
     * Returns if the {@link IItemHandler} is full.
     *
     * @param itemHandler The {@link IItemHandler}.
     * @return True if the {@link IItemHandler} is full, false when not.
     */
    public static boolean isItemHandlerFull(@NotNull final IItemHandler itemHandler)
    {
        return getFirstOpenSlotFromItemHandler(itemHandler) == -1;
    }

    /**
     * Returns the first open slot in the {@link IItemHandler}.
     *
     * @param itemHandler The {@link IItemHandler} to check.
     * @return slot index or -1 if none found.
     */
    public static int getFirstOpenSlotFromItemHandler(@NotNull final IItemHandler itemHandler)
    {
        //Test with two different ItemStacks to insert in simulation mode.
        return IntStream.range(0, itemHandler.getSlots())
                 .filter(slot -> ItemStackUtils.isEmpty(itemHandler.getStackInSlot(slot)))
                 .findFirst()
                 .orElse(-1);
    }

    /**
     * Count all open slots in inventory.
     * @param itemHandler the inventory.
     * @return the amount of open slots.
     */
    public static long openSlotCount(final IItemHandler itemHandler)
    {
        return IntStream.range(0, itemHandler.getSlots())
                 .filter(slot -> ItemStackUtils.isEmpty(itemHandler.getStackInSlot(slot)))
                 .count();
    }

    /**
     * Adapted from {@link net.minecraft.entity.player.InventoryPlayer#addItemStackToInventory(ItemStack)}.
     *
     * @param itemHandler              {@link IItemHandler} to add itemstack
     *                                 to.
     * @param itemStack                ItemStack to add.
     * @param itemStackToKeepPredicate The {@link Predicate} that determines
     *                                 which ItemStacks to keep in the
     *                                 inventory. Return false to replace.
     * @return itemStack which has been replaced, null if none has been
     * replaced.
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
     * Returns the amount of item stacks in an inventory. This equals {@link
     * #getItemHandlerAsList(IItemHandler)}<code>.length();</code>.
     *
     * @param itemHandler {@link IItemHandler} to count item stacks of.
     * @return Amount of item stacks in the {@link IItemHandler}.
     */
    public static int getAmountOfStacksInItemHandler(@NotNull final IItemHandler itemHandler)
    {
        return getItemHandlerAsList(itemHandler).size();
    }

    /**
     * Returns an {@link IItemHandler} as list of item stacks.
     *
     * @param itemHandler Inventory to convert.
     * @return List of item stacks.
     */
    @NotNull
    public static List<ItemStack> getItemHandlerAsList(@NotNull final IItemHandler itemHandler)
    {
        return filterItemHandler(itemHandler, (ItemStack stack) -> true);
    }

    /**
     * Filters a list of items, matches the stack using {@link
     * #compareItems(ItemStack, Item, int)}, in an {@link ICapabilityProvider}.
     * Uses the MetaData and {@link #getItemFromBlock(Block)} as parameters for
     * the Predicate.
     *
     * @param provider Provider to filter in
     * @param block    Block to filter
     * @param metaData the damage value.
     * @return List of item stacks
     */
    @NotNull
    public static List<ItemStack> filterProvider(@NotNull final ICapabilityProvider provider, final Block block, final int metaData)
    {
        return filterProvider(provider, (ItemStack stack) -> compareItems(stack, getItemFromBlock(block), metaData));
    }

    /**
     * Filters a list of items, that match the given predicate, in an {@link
     * ICapabilityProvider}.
     *
     * @param provider                    The ICapabilityProvider to get items
     *                                    from.
     * @param itemStackSelectionPredicate The predicate to match the stack to.
     * @return List of item stacks that match the given predicate.
     */
    @NotNull
    public static List<ItemStack> filterProvider(@NotNull final ICapabilityProvider provider, @NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        return getFromProviderForAllSides(provider, itemStackSelectionPredicate);
    }

    /**
     * Method to process the given predicate for all {@link EnumFacing} of a
     * {@link ICapabilityProvider}, including the internal one (passing null as
     * argument).
     *
     * @param provider  The provider to process all the
     * @param predicate The predicate to match the ItemStacks in the {@link
     *                  IItemHandler} for each side with.
     * @return A combined {@link List<ItemStack>} as if the given predicate was
     * called on all ItemStacks in all IItemHandlers of the given provider.
     */
    @NotNull
    private static List<ItemStack> getFromProviderForAllSides(@NotNull final ICapabilityProvider provider, @NotNull final Predicate<ItemStack> predicate)
    {
        final ArrayList<ItemStack> combinedList = new ArrayList<>();

        for (final IItemHandler handler : getItemHandlersFromProvider(provider))
        {
            if(handler != null)
            {
                combinedList.addAll(filterItemHandler(handler, predicate));
            }
        }
        return combinedList;
    }

    /**
     * Method to get all the IItemHandlers from a given Provider.
     *
     * @param provider The provider to get the IItemHandlers from.
     * @return A list with all the unique IItemHandlers a provider has.
     */
    @NotNull
    public static Set<IItemHandler> getItemHandlersFromProvider(@NotNull final ICapabilityProvider provider)
    {
        final Set<IItemHandler> handlerList = Arrays.stream(EnumFacing.VALUES)
                                                .filter(facing -> provider.hasCapability(ITEM_HANDLER_CAPABILITY, facing))
                                                .map(facing -> provider.getCapability(ITEM_HANDLER_CAPABILITY, facing))
                                                .filter(Objects::nonNull)
                                                .collect(Collectors.toSet());

        if (provider.hasCapability(ITEM_HANDLER_CAPABILITY, null))
        {
            final IItemHandler nullHandler = provider.getCapability(ITEM_HANDLER_CAPABILITY, null);
            if (!handlerList.contains(nullHandler))
            {
                handlerList.add(nullHandler);
            }
        }

        handlerList.removeIf(Objects::isNull);

        return handlerList;
    }

    /**
     * Filters a list of items, matches the stack using {@link
     * #compareItems(ItemStack, Item, int)}, with targetItem and itemDamage as
     * parameters, in an {@link ICapabilityProvider}.
     *
     * @param provider   Provider to get items from
     * @param targetItem Item to look for
     * @param itemDamage the damage value.
     * @return List of item stacks with the given item in inventory
     */
    @NotNull
    public static List<ItemStack> filterProvider(@NotNull final ICapabilityProvider provider, @Nullable final Item targetItem, final int itemDamage)
    {
        return filterProvider(provider, (ItemStack stack) -> compareItems(stack, targetItem, itemDamage));
    }

    /**
     * Returns the index of the first occurrence of the block in the {@link
     * ICapabilityProvider}.
     *
     * @param provider   {@link ICapabilityProvider} to check.
     * @param block      Block to find.
     * @param itemDamage the damage value.
     * @return Index of the first occurrence.
     */
    public static int findFirstSlotInProviderWith(@NotNull final ICapabilityProvider provider, final Block block, final int itemDamage)
    {
        return findFirstSlotInProviderWith(provider, getItemFromBlock(block), itemDamage);
    }

    /**
     * Returns the index of the first occurrence of the Item with the given
     * ItemDamage in the {@link ICapabilityProvider}.
     *
     * @param provider   {@link ICapabilityProvider} to check
     * @param targetItem Item to find.
     * @param itemDamage The damage value to match on the stack.
     * @return Index of the first occurrence
     */
    public static int findFirstSlotInProviderWith(@NotNull final ICapabilityProvider provider, final Item targetItem, final int itemDamage)
    {
        return findFirstSlotInProviderNotEmptyWith(provider, (ItemStack stack) -> compareItems(stack, targetItem, itemDamage));
    }

    /**
     * Returns the index of the first occurrence of an ItemStack that matches
     * the given predicate in the {@link ICapabilityProvider}.
     *
     * @param provider                    Provider to check
     * @param itemStackSelectionPredicate The predicate to match.
     * @return Index of the first occurrence
     */
    public static Map<IItemHandler, List<Integer>> findAllSlotsInProviderWith(@NotNull final ICapabilityProvider provider, final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        final Map<IItemHandler, List<Integer>> map = new HashMap<>();
        for (final IItemHandler handler : getItemHandlersFromProvider(provider))
        {
            final List<Integer> tempList = findAllSlotsInItemHandlerWith(handler, itemStackSelectionPredicate);
            if (!tempList.isEmpty())
            {
                map.put(handler, tempList);
            }
        }

        return map;
    }

    /**
     * Returns the index of the first occurrence of an ItemStack that matches
     * the given predicate in the {@link ICapabilityProvider}.
     *
     * @param provider                    Provider to check
     * @param itemStackSelectionPredicate The predicate to match.
     * @return Index of the first occurrence
     */
    public static int findFirstSlotInProviderNotEmptyWith(@NotNull final ICapabilityProvider provider, final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        for (final IItemHandler handler : getItemHandlersFromProvider(provider))
        {
            final int foundSlot = findFirstSlotInItemHandlerNotEmptyWith(handler, itemStackSelectionPredicate);
            if (foundSlot > -1)
            {
                return foundSlot;
            }
        }

        return -1;
    }

    /**
     * Returns the index of the first occurrence of an ItemStack that matches
     * the given predicate in the {@link ICapabilityProvider}.
     *
     * @param provider                    Provider to check
     * @param itemStackSelectionPredicate The list of predicates to match.
     * @return Index of the first occurrence
     */
    public static int findFirstSlotInProviderNotEmptyWith(@NotNull final ICapabilityProvider provider, final List<Predicate<ItemStack>> itemStackSelectionPredicate)
    {
        for (final IItemHandler handler : getItemHandlersFromProvider(provider))
        {
            final int foundSlot = findFirstSlotInItemHandlerNotEmptyWith(handler, itemStackSelectionPredicate);
            if (foundSlot > -1)
            {
                return foundSlot;
            }
        }

        return -1;
    }

    /**
     * Returns the index of the first occurrence of an ItemStack that matches
     * the given predicate in the {@link IItemHandler}.
     * Also applies the not empty check.
     *
     * @param itemHandler                 ItemHandler to check
     * @param itemStackSelectionPredicate The list of predicates to match.
     * @return Index of the first occurrence
     */
    private static int findFirstSlotInItemHandlerNotEmptyWith(final IItemHandler itemHandler, final List<Predicate<ItemStack>> itemStackSelectionPredicate)
    {
        for (final Predicate<ItemStack> predicate : itemStackSelectionPredicate)
        {
            for (int slot = 0; slot < itemHandler.getSlots(); slot++)
            {
                if (ItemStackUtils.NOT_EMPTY_PREDICATE.and(predicate).test(itemHandler.getStackInSlot(slot)))
                {
                    return slot;
                }
            }
        }

        return -1;
    }

    /**
     * Returns the index of the first occurrence of an ItemStack that matches
     * the given predicate in the {@link IItemHandler}.
     * Also applies the not empty check.
     *
     * @param itemHandler                 ItemHandler to check
     * @param itemStackSelectionPredicate The predicate to match.
     * @return Index of the first occurrence
     */
    public static int findFirstSlotInItemHandlerNotEmptyWith(@NotNull final IItemHandler itemHandler, @NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        @NotNull final Predicate<ItemStack> firstWorthySlotPredicate = ItemStackUtils.NOT_EMPTY_PREDICATE.and(itemStackSelectionPredicate);

        for (int slot = 0; slot < itemHandler.getSlots(); slot++)
        {
            if (firstWorthySlotPredicate.test(itemHandler.getStackInSlot(slot)))
            {
                return slot;
            }
        }

        return -1;
        //TODO: Later harden contract to remove compare on slot := -1
        //throw new IllegalStateException("Item "+targetItem.getTranslationKey() + " not found in ItemHandler!");
    }

    /**
     * Returns the amount of occurrences in the {@link ICapabilityProvider}.
     *
     * @param provider   {@link ICapabilityProvider} to scan.
     * @param block      The block to count
     * @param itemDamage the damage value
     * @return Amount of occurrences of stacks that match the given block and
     * ItemDamage
     */
    public static int getItemCountInProvider(@NotNull final ICapabilityProvider provider, @NotNull final Block block, final int itemDamage)
    {
        return getItemCountInProvider(provider, getItemFromBlock(block), itemDamage);
    }

    /**
     * Returns the amount of occurrences in the {@link ICapabilityProvider}.
     *
     * @param provider   {@link ICapabilityProvider} to scan.
     * @param targetItem Item to count
     * @param itemDamage the item damage value.
     * @return Amount of occurrences of stacks that match the given item and
     * ItemDamage
     */
    public static int getItemCountInProvider(@NotNull final ICapabilityProvider provider, @NotNull final Item targetItem, final int itemDamage)
    {
        return getItemCountInProvider(provider, (ItemStack stack) -> compareItems(stack, targetItem, itemDamage));
    }

    /**
     * Returns the amount of occurrences in the {@link ICapabilityProvider}.
     *
     * @param provider                    {@link ICapabilityProvider} to scan.
     * @param itemStackSelectionPredicate The predicate used to select the
     *                                    stacks to count.
     * @return Amount of occurrences of stacks that match the given predicate.
     */
    public static int getItemCountInProvider(@NotNull final ICapabilityProvider provider, @NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        return getItemHandlersFromProvider(provider).stream().filter(Objects::nonNull)
                 .mapToInt(handler -> filterItemHandler(handler, itemStackSelectionPredicate).stream().mapToInt(ItemStackUtils::getSize).sum())
                 .sum();
    }

    /**
     * Checks if a player has a block in the {@link ICapabilityProvider}.
     * Checked by {@link #getItemCountInProvider(ICapabilityProvider, Block,
     * int)} &gt; 0;
     *
     * @param Provider   {@link ICapabilityProvider} to scan
     * @param block      Block to count
     * @param itemDamage the damage value.
     * @return True when in {@link ICapabilityProvider}, otherwise false
     */
    public static boolean hasItemInProvider(@NotNull final ICapabilityProvider Provider, @NotNull final Block block, final int itemDamage)
    {
        return hasItemInProvider(Provider, getItemFromBlock(block), itemDamage);
    }

    /**
     * Checks if a player has an item in the {@link ICapabilityProvider}.
     * Checked by {@link #getItemCountInProvider(ICapabilityProvider, Item,
     * int)} &gt; 0;
     *
     * @param Provider   {@link ICapabilityProvider} to scan
     * @param item       Item to count
     * @param itemDamage the damage value of the item.
     * @return True when in {@link ICapabilityProvider}, otherwise false
     */
    public static boolean hasItemInProvider(@NotNull final ICapabilityProvider Provider, @NotNull final Item item, final int itemDamage)
    {
        return hasItemInProvider(Provider, (ItemStack stack) -> compareItems(stack, item, itemDamage));
    }

    /**
     * Checks if a player has an item in the {@link ICapabilityProvider}.
     * Checked by {@link InventoryUtils#getItemCountInProvider(ICapabilityProvider,
     * Predicate)} &gt; 0;
     *
     * @param Provider                    {@link ICapabilityProvider} to scan
     * @param itemStackSelectionPredicate The predicate to match the ItemStack
     *                                    to.
     * @return True when in {@link ICapabilityProvider}, otherwise false
     */
    public static boolean hasItemInProvider(@NotNull final ICapabilityProvider Provider, @NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        return getItemCountInProvider(Provider, itemStackSelectionPredicate) > 0;
    }

    /**
     * Returns if the {@link ICapabilityProvider} is full.
     *
     * @param provider The {@link ICapabilityProvider}.
     * @return True if the {@link ICapabilityProvider} is full, false when not.
     */
    public static boolean isProviderFull(@NotNull final ICapabilityProvider provider)
    {
        return getFirstOpenSlotFromProvider(provider) == -1;
    }

    /**
     * Returns the first open slot in the {@link ICapabilityProvider}.
     *
     * @param provider The {@link ICapabilityProvider} to check.
     * @return slot index or -1 if none found.
     */
    public static int getFirstOpenSlotFromProvider(@NotNull final ICapabilityProvider provider)
    {
        return getItemHandlersFromProvider(provider).stream()
                 .mapToInt(InventoryUtils::getFirstOpenSlotFromItemHandler)
                 .filter(slotIndex -> slotIndex > -1)
                 .findFirst()
                 .orElse(-1);
    }

    /**
     * Checks if the {@link ICapabilityProvider} contains the following toolName
     * with the given minimal Level.
     *
     * @param provider     The {@link ICapabilityProvider} to scan.
     * @param toolType     The toolTypeName of the tool to find.
     * @param minimalLevel The minimal level to find.
     * @param maximumLevel The maximum level to find.
     * @return True if a Tool with the given toolTypeName was found in the given
     * {@link ICapabilityProvider}, false when not.
     */
    public static boolean isToolInProvider(@NotNull final ICapabilityProvider provider, @NotNull final IToolType toolType, final int minimalLevel, final int maximumLevel)
    {
        return hasItemInProvider(provider, (ItemStack stack) -> ItemStackUtils.hasToolLevel(stack, toolType, minimalLevel, maximumLevel));
    }

    /**
     * Adapted from {@link net.minecraft.entity.player.InventoryPlayer#addItemStackToInventory(ItemStack)}.
     *
     * @param provider  {@link ICapabilityProvider} to add itemstack to.
     * @param itemStack ItemStack to add.
     * @return True if successful, otherwise false.
     */
    public static boolean addItemStackToProvider(@NotNull final ICapabilityProvider provider, @Nullable final ItemStack itemStack)
    {
        return getItemHandlersFromProvider(provider).stream().anyMatch(handler -> addItemStackToItemHandler(handler, itemStack));
    }

    /**
     * Adapted from {@link net.minecraft.entity.player.InventoryPlayer#addItemStackToInventory(ItemStack)}.
     *
     * @param itemHandler {@link IItemHandler} to add itemstack to.
     * @param itemStack   ItemStack to add.
     * @return True if successful, otherwise false.
     */
    public static boolean addItemStackToItemHandler(@NotNull final IItemHandler itemHandler, @Nullable final ItemStack itemStack)
    {
        if (!ItemStackUtils.isEmpty(itemStack))
        {
            int slot;

            if (itemStack.isItemDamaged())
            {
                slot = getFirstOpenSlotFromItemHandler(itemHandler);

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
                ItemStack resultStack = itemStack;
                slot = itemHandler.getSlots() == 0 ? -1 : 0;
                while (!ItemStackUtils.isEmpty(resultStack) && slot != -1 && slot != itemHandler.getSlots())
                {
                    resultStack = itemHandler.insertItem(slot, resultStack, false);
                    if (!ItemStackUtils.isEmpty(resultStack))
                    {
                        slot++;
                    }
                }


                return ItemStackUtils.isEmpty(resultStack);
            }
        }
        else
        {
            return false;
        }
    }

    /**
     * Adapted from {@link net.minecraft.entity.player.InventoryPlayer#addItemStackToInventory(ItemStack)}.
     *
     * @param provider  {@link ICapabilityProvider} to add itemstack to.
     * @param itemStack ItemStack to add.
     * @return Empty when fully transfered without swapping, otherwise return the remain of a partial transfer or the itemStack it has been swapped with.
     */
    public static ItemStack addItemStackToProviderWithResult(@NotNull final ICapabilityProvider provider, @Nullable final ItemStack itemStack)
    {
        ItemStack activeStack = itemStack;

        if (ItemStackUtils.isEmpty(activeStack))
        {
            return ItemStackUtils.EMPTY;
        }

        for (final IItemHandler handler : getItemHandlersFromProvider(provider))
        {
            activeStack = addItemStackToItemHandlerWithResult(handler, activeStack);
        }

        return activeStack;
    }

    /**
     * Adapted from {@link net.minecraft.entity.player.InventoryPlayer#addItemStackToInventory(ItemStack)}.
     *
     * @param itemHandler {@link IItemHandler} to add itemstack to.
     * @param itemStack   ItemStack to add.
     * @return Empty when fully transfered without swapping, otherwise return the remain of a partial transfer or the itemStack it has been swapped with.
     */
    public static ItemStack addItemStackToItemHandlerWithResult(@NotNull final IItemHandler itemHandler, @Nullable final ItemStack itemStack)
    {
        if (!ItemStackUtils.isEmpty(itemStack))
        {
            int slot;

            if (itemStack.isItemDamaged())
            {
                slot = getFirstOpenSlotFromItemHandler(itemHandler);

                if (slot >= 0)
                {
                    itemHandler.insertItem(slot, itemStack, false);
                    return ItemStackUtils.EMPTY;
                }
                else
                {
                    return itemStack;
                }
            }
            else
            {
                ItemStack resultStack = itemStack;
                slot = itemHandler.getSlots() == 0 ? -1 : 0;
                while (!ItemStackUtils.isEmpty(resultStack) && slot != -1 && slot != itemHandler.getSlots())
                {
                    resultStack = itemHandler.insertItem(slot, resultStack, false);
                    if (!ItemStackUtils.isEmpty(resultStack))
                    {
                        slot++;
                    }
                }

                return resultStack;
            }
        }
        else
        {
            return itemStack;
        }
    }

    /**
     * Adapted from {@link net.minecraft.entity.player.InventoryPlayer#addItemStackToInventory(ItemStack)}.
     *
     * @param provider                 {@link ICapabilityProvider} to add
     *                                 itemstack to.
     * @param itemStack                ItemStack to add.
     * @param itemStackToKeepPredicate The {@link Predicate} that determines
     *                                 which ItemStacks to keep in the
     *                                 inventory. Return false to replace.
     * @return itemStack which has been replaced.
     */
    @Nullable
    public static ItemStack forceItemStackToProvider(
                                                      @NotNull final ICapabilityProvider provider,
                                                      @NotNull final ItemStack itemStack,
                                                      @NotNull final Predicate<ItemStack> itemStackToKeepPredicate)
    {
        final ItemStack standardInsertionResult = addItemStackToProviderWithResult(provider, itemStack);

        if (!ItemStackUtils.isEmpty(standardInsertionResult))
        {
            ItemStack resultStack = standardInsertionResult.copy();
            final Iterator<IItemHandler> iterator = getItemHandlersFromProvider(provider).iterator();
            while (iterator.hasNext() && !ItemStackUtils.isEmpty(resultStack))
            {
                resultStack = forceItemStackToItemHandler(iterator.next(), resultStack, itemStackToKeepPredicate);
            }

            return resultStack;
        }

        return ItemStackUtils.EMPTY;
    }

    /**
     * Returns the amount of item stacks in an inventory. This equals {@link
     * #getProviderAsList(ICapabilityProvider)}<code>.length();</code>.
     *
     * @param provider {@link ICapabilityProvider} to count item stacks of.
     * @return Amount of item stacks in the {@link ICapabilityProvider}.
     */
    public static int getAmountOfStacksInProvider(@NotNull final ICapabilityProvider provider)
    {
        return getProviderAsList(provider).size();
    }

    /**
     * Returns an {@link ICapabilityProvider} as list of item stacks.
     *
     * @param provider provider to convert.
     * @return List of item stacks.
     */
    @NotNull
    public static List<ItemStack> getProviderAsList(@NotNull final ICapabilityProvider provider)
    {
        return filterProvider(provider, (ItemStack stack) -> true);
    }

    /**
     * Method used to check if a {@link ICapabilityProvider} has any {@link
     * IItemHandler}
     *
     * @param provider The provider to check.
     * @return True when the provider has any {@link IItemHandler}, false when
     * not.
     */
    @NotNull
    public static boolean hasProviderIItemHandler(@NotNull final ICapabilityProvider provider)
    {
        return !getItemHandlersFromProvider(provider).isEmpty();
    }

    /**
     * Method used to check if this provider is sided.
     *
     * @param provider The provider to check for.
     * @return True when the provider has multiple distinct IItemHandler of
     * different sides (sidedness {@link ICapabilityProvider#hasCapability(Capability,
     * EnumFacing)}), false when not
     */
    @NotNull
    public static boolean isProviderSided(@NotNull final ICapabilityProvider provider)
    {
        return getItemHandlersFromProvider(provider).size() > 1;
    }

    /**
     * Returns an {@link IItemHandler} as list of item stacks.
     *
     * @param provider The {@link ICapabilityProvider} that holds the {@link
     *                 IItemHandler} for the given {@link EnumFacing}
     * @param facing   The facing to get the {@link IItemHandler} from. Can be
     *                 null for the internal one {@link ICapabilityProvider#hasCapability(Capability,
     *                 EnumFacing)}
     * @return List of item stacks.
     */
    @NotNull
    public static List<ItemStack> getInventoryAsListFromProviderForSide(@NotNull final ICapabilityProvider provider, @Nullable final EnumFacing facing)
    {
        return filterItemHandler(provider.getCapability(ITEM_HANDLER_CAPABILITY, facing), (ItemStack stack) -> true);
    }

    /**
     * Filters a list of items, matches the stack using {@link
     * #compareItems(ItemStack, Item, int)}, in an {@link IItemHandler}. Uses
     * the MetaData and {@link #getItemFromBlock(Block)} as parameters for the
     * Predicate.
     *
     * @param provider The {@link ICapabilityProvider} that holds the {@link
     *                 IItemHandler} for the given {@link EnumFacing}
     * @param facing   The facing to get the {@link IItemHandler} from. Can be
     *                 null for the internal one {@link ICapabilityProvider#hasCapability(Capability,
     *                 EnumFacing)}
     * @param block    Block to filter
     * @param metaData the damage value.
     * @return List of item stacks
     */
    @NotNull
    public static List<ItemStack> filterItemHandlerFromProviderForSide(
                                                                        @NotNull final ICapabilityProvider provider,
                                                                        @Nullable final EnumFacing facing,
                                                                        @NotNull final Block block,
                                                                        final int metaData)
    {
        return filterItemHandler(provider.getCapability(ITEM_HANDLER_CAPABILITY, facing), (ItemStack stack) -> compareItems(stack, getItemFromBlock(block), metaData));
    }

    /**
     * Filters a list of items, matches the stack using {@link
     * #compareItems(ItemStack, Item, int)}, with targetItem and itemDamage as
     * parameters, in an {@link IItemHandler}.
     *
     * @param provider   The {@link ICapabilityProvider} that holds the {@link
     *                   IItemHandler} for the given {@link EnumFacing}
     * @param facing     The facing to get the {@link IItemHandler} from. Can be
     *                   null for the internal one {@link ICapabilityProvider#hasCapability(Capability,
     *                   EnumFacing)}
     * @param targetItem Item to look for
     * @param itemDamage the damage value.
     * @return List of item stacks with the given item in inventory
     */
    @NotNull
    public static List<ItemStack> filterItemHandlerFromProviderForSide(
                                                                        @NotNull final ICapabilityProvider provider,
                                                                        @Nullable final EnumFacing facing,
                                                                        @NotNull final Item targetItem,
                                                                        final int itemDamage)
    {
        return filterItemHandler(provider.getCapability(ITEM_HANDLER_CAPABILITY, facing), (ItemStack stack) -> compareItems(stack, targetItem, itemDamage));
    }

    /**
     * Filters a list of items, that match the given predicate, in an {@link
     * IItemHandler}.
     *
     * @param provider                    The {@link ICapabilityProvider} that
     *                                    holds the {@link IItemHandler} for the
     *                                    given {@link EnumFacing}
     * @param facing                      The facing to get the {@link
     *                                    IItemHandler} from. Can be null for
     *                                    the internal one {@link ICapabilityProvider#hasCapability(Capability,
     *                                    EnumFacing)}
     * @param itemStackSelectionPredicate The predicate to match the stack to.
     * @return List of item stacks that match the given predicate.
     */
    @NotNull
    public static List<ItemStack> filterItemHandlerFromProviderForSide(
                                                                        @NotNull final ICapabilityProvider provider,
                                                                        @Nullable final EnumFacing facing,
                                                                        @NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        if (!provider.hasCapability(ITEM_HANDLER_CAPABILITY, facing))
        {
            return Collections.emptyList();
        }

        return filterItemHandler(provider.getCapability(ITEM_HANDLER_CAPABILITY, facing), itemStackSelectionPredicate);
    }

    /**
     * Returns the index of the first occurrence of the block in the {@link
     * ICapabilityProvider} for a given {@link EnumFacing}.
     *
     * @param provider   {@link ICapabilityProvider} to check.
     * @param facing     The facing to check for.
     * @param block      Block to find.
     * @param itemDamage the damage value.
     * @return Index of the first occurrence.
     */
    public static int findFirstSlotInProviderForSideWith(
                                                          @NotNull final ICapabilityProvider provider,
                                                          @Nullable final EnumFacing facing,
                                                          @NotNull final Block block,
                                                          final int itemDamage)
    {
        return findFirstSlotInProviderForSideWith(provider, facing, getItemFromBlock(block), itemDamage);
    }

    /**
     * Returns the index of the first occurrence of the Item with the given
     * ItemDamage in the {@link ICapabilityProvider} for a given {@link
     * EnumFacing}.
     *
     * @param provider   {@link ICapabilityProvider} to check
     * @param facing     The facing to check for.
     * @param targetItem Item to find.
     * @param itemDamage The damage value to match on the stack.
     * @return Index of the first occurrence
     */
    public static int findFirstSlotInProviderForSideWith(
                                                          @NotNull final ICapabilityProvider provider,
                                                          @Nullable final EnumFacing facing,
                                                          @NotNull final Item targetItem,
                                                          final int itemDamage)
    {
        return findFirstSlotInProviderForSideWith(provider, facing, (ItemStack stack) -> compareItems(stack, targetItem, itemDamage));
    }

    /**
     * Returns the index of the first occurrence of an ItemStack that matches
     * the given predicate in the {@link ICapabilityProvider} for a given {@link
     * EnumFacing}.
     *
     * @param provider                    Provider to check
     * @param facing                      The facing to check for.
     * @param itemStackSelectionPredicate The predicate to match.
     * @return Index of the first occurrence
     */
    public static int findFirstSlotInProviderForSideWith(
                                                          @NotNull final ICapabilityProvider provider,
                                                          @Nullable final EnumFacing facing,
                                                          @NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        if (!provider.hasCapability(ITEM_HANDLER_CAPABILITY, facing))
        {
            return -1;
            //TODO: Later harden contract to remove compare on slot := -1
            //throw new IllegalStateException("Item "+targetItem.getTranslationKey() + " not found in ItemHandler!");
        }

        return findFirstSlotInItemHandlerWith(provider.getCapability(ITEM_HANDLER_CAPABILITY, facing), itemStackSelectionPredicate);
    }

    /**
     * Returns the amount of occurrences in the {@link ICapabilityProvider} for
     * a given {@link EnumFacing}.
     *
     * @param provider   {@link ICapabilityProvider} to scan.
     * @param facing     The facing to count in.
     * @param block      The block to count
     * @param itemDamage the damage value
     * @return Amount of occurrences of stacks that match the given block and
     * ItemDamage
     */
    public static int getItemCountInProviderForSide(
                                                     @NotNull final ICapabilityProvider provider,
                                                     @Nullable final EnumFacing facing,
                                                     @NotNull final Block block,
                                                     final int itemDamage)
    {
        return getItemCountInProviderForSide(provider, facing, getItemFromBlock(block), itemDamage);
    }

    /**
     * Returns the amount of occurrences in the {@link ICapabilityProvider} for
     * a given {@link EnumFacing}.
     *
     * @param provider   {@link ICapabilityProvider} to scan.
     * @param facing     The facing to count in.
     * @param targetItem Item to count
     * @param itemDamage the item damage value.
     * @return Amount of occurrences of stacks that match the given item and
     * ItemDamage
     */
    public static int getItemCountInProviderForSide(
                                                     @NotNull final ICapabilityProvider provider,
                                                     @Nullable final EnumFacing facing,
                                                     @NotNull final Item targetItem,
                                                     final int itemDamage)
    {
        return getItemCountInProviderForSide(provider, facing, (ItemStack stack) -> compareItems(stack, targetItem, itemDamage));
    }

    /**
     * Returns the amount of occurrences in the {@link ICapabilityProvider} for
     * a given {@link EnumFacing}.
     *
     * @param provider                    {@link ICapabilityProvider} to scan.
     * @param facing                      The facing to count in.
     * @param itemStackSelectionPredicate The predicate used to select the
     *                                    stacks to count.
     * @return Amount of occurrences of stacks that match the given predicate.
     */
    public static int getItemCountInProviderForSide(
                                                     @NotNull final ICapabilityProvider provider,
                                                     @Nullable final EnumFacing facing,
                                                     @NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        if (!provider.hasCapability(ITEM_HANDLER_CAPABILITY, facing))
        {
            return 0;
        }

        return filterItemHandler(provider.getCapability(ITEM_HANDLER_CAPABILITY, facing), itemStackSelectionPredicate).stream().mapToInt(ItemStackUtils::getSize).sum();
    }

    /**
     * Checks if a player has a block in the {@link ICapabilityProvider}, for a
     * given {@link EnumFacing}. Checked by {@link #getItemCountInProvider(ICapabilityProvider,
     * Block, int)} &gt; 0;
     *
     * @param provider   {@link ICapabilityProvider} to scan
     * @param facing     The side to check for.
     * @param block      Block to count
     * @param itemDamage the damage value.
     * @return True when in {@link ICapabilityProvider}, otherwise false
     */
    public static boolean hasItemInProviderForSide(@NotNull final ICapabilityProvider provider, @Nullable final EnumFacing facing, @NotNull final Block block, final int itemDamage)
    {
        return hasItemInProviderForSide(provider, facing, getItemFromBlock(block), itemDamage);
    }

    /**
     * Checks if a player has an item in the {@link ICapabilityProvider}, for a
     * given {@link EnumFacing}. Checked by {@link #getItemCountInProvider(ICapabilityProvider,
     * Item, int)} &gt; 0;
     *
     * @param provider   {@link ICapabilityProvider} to scan
     * @param facing     The side to check for.
     * @param item       Item to count
     * @param itemDamage the damage value of the item.
     * @return True when in {@link ICapabilityProvider}, otherwise false
     */
    public static boolean hasItemInProviderForSide(@NotNull final ICapabilityProvider provider, @Nullable final EnumFacing facing, @NotNull final Item item, final int itemDamage)
    {
        return hasItemInProviderForSide(provider, facing, (ItemStack stack) -> compareItems(stack, item, itemDamage));
    }

    /**
     * Checks if a player has an item in the {@link ICapabilityProvider}, for a
     * given {@link EnumFacing}. Checked by {@link InventoryUtils#getItemCountInProvider(ICapabilityProvider,
     * Predicate)} &gt; 0;
     *
     * @param provider                    {@link ICapabilityProvider} to scan
     * @param facing                      The side to check for.
     * @param itemStackSelectionPredicate The predicate to match the ItemStack
     *                                    to.
     * @return True when in {@link ICapabilityProvider}, otherwise false
     */
    public static boolean hasItemInProviderForSide(
                                                    @NotNull final ICapabilityProvider provider,
                                                    @Nullable final EnumFacing facing,
                                                    @NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        if (!provider.hasCapability(ITEM_HANDLER_CAPABILITY, facing))
        {
            return false;
        }

        return getItemCountInItemHandler(provider.getCapability(ITEM_HANDLER_CAPABILITY, facing), itemStackSelectionPredicate) > 0;
    }

    /**
     * Returns if the {@link ICapabilityProvider} is full, for a given {@link
     * EnumFacing}.
     *
     * @param provider The {@link ICapabilityProvider}.
     * @param facing   The side to check for.
     * @return True if the {@link ICapabilityProvider} is full, false when not.
     */
    public static boolean isProviderFull(@NotNull final ICapabilityProvider provider, @Nullable final EnumFacing facing)
    {
        return getFirstOpenSlotFromProviderForSide(provider, facing) == -1;
    }

    /**
     * Returns the first open slot in the {@link ICapabilityProvider}, for a
     * given {@link EnumFacing}.
     *
     * @param provider The {@link ICapabilityProvider} to check.
     * @param facing   The side to check for.
     * @return slot index or -1 if none found.
     */
    public static int getFirstOpenSlotFromProviderForSide(@NotNull final ICapabilityProvider provider, @Nullable final EnumFacing facing)
    {
        if (!provider.hasCapability(ITEM_HANDLER_CAPABILITY, facing))
        {
            return -1;
        }

        return getFirstOpenSlotFromItemHandler(provider.getCapability(ITEM_HANDLER_CAPABILITY, facing));
    }

    /**
     * Checks if the {@link ICapabilityProvider} contains the following toolName
     * with the given minimal Level, for a given {@link EnumFacing}.
     *
     * @param provider     The {@link ICapabilityProvider} to scan.
     * @param facing       The side to check for.
     * @param toolType     The tool type to find.
     * @param minimalLevel The minimal level to find.
     * @param maximumLevel The maximum level to find.
     * @return True if a Tool with the given toolTypeName was found in the given
     * {@link ICapabilityProvider}, false when not.
     */
    public static boolean isToolInProviderForSide(
                                                   @NotNull final ICapabilityProvider provider, @Nullable final EnumFacing facing, @NotNull final IToolType toolType,
                                                   final int minimalLevel, final int maximumLevel)
    {
        if (!provider.hasCapability(ITEM_HANDLER_CAPABILITY, facing))
        {
            return false;
        }

        return isToolInItemHandler(provider.getCapability(ITEM_HANDLER_CAPABILITY, facing), toolType, minimalLevel, maximumLevel);
    }

    /**
     * Checks if the {@link IItemHandler} contains the following toolName with
     * the given minimal Level.
     *
     * @param itemHandler  The {@link IItemHandler} to scan.
     * @param toolType     The toolType of the tool to find.
     * @param minimalLevel The minimal level to find.
     * @param maximumLevel The maximum level to find.
     * @return True if a Tool with the given toolTypeName was found in the given
     * {@link IItemHandler}, false when not.
     */
    public static boolean isToolInItemHandler(@NotNull final IItemHandler itemHandler, @NotNull final IToolType toolType, final int minimalLevel, final int maximumLevel)
    {
        return hasItemInItemHandler(itemHandler, (ItemStack stack) ->
                                                   ItemStackUtils.hasToolLevel(stack, toolType, minimalLevel, maximumLevel));
    }

    /**
     * Clears an entire {@link IItemHandler}.
     *
     * @param itemHandler {@link IItemHandler} to clear.
     */
    public static void clearItemHandler(@NotNull final IItemHandler itemHandler)
    {
        for (int slotIndex = 0; slotIndex < itemHandler.getSlots(); slotIndex++)
        {
            itemHandler.extractItem(slotIndex, Integer.MAX_VALUE, false);
        }
    }

    /**
     * Returns a slot number if an {@link IItemHandler} contains given tool
     * type.
     *
     * @param itemHandler  the {@link IItemHandler} to get the slot from.
     * @param toolType     the tool type to look for.
     * @param minimalLevel The minimal level to find.
     * @param maximumLevel The maximum level to find.
     * @return slot number if found, -1 if not found.
     */
    public static int getFirstSlotOfItemHandlerContainingTool(
                                                               @NotNull final IItemHandler itemHandler, @NotNull final IToolType toolType, final int minimalLevel,
                                                               final int maximumLevel)
    {
        return findFirstSlotInItemHandlerWith(itemHandler,
          (ItemStack stack) -> ItemStackUtils.hasToolLevel(stack, toolType, minimalLevel, maximumLevel));
    }

    /**
     * Verifies if there is one tool with an acceptable level
     * in a worker's inventory.
     *
     * @param itemHandler   the worker's inventory
     * @param toolType      the type of tool needed
     * @param requiredLevel the minimum tool level
     * @param maximumLevel  the worker's hut level
     * @return true if tool is acceptable
     */
    public static boolean hasItemHandlerToolWithLevel(@NotNull final IItemHandler itemHandler, final IToolType toolType, final int requiredLevel, final int maximumLevel)
    {
        return findFirstSlotInItemHandlerWith(itemHandler,
          (ItemStack stack) -> (!ItemStackUtils.isEmpty(stack) && (ItemStackUtils.isTool(stack, toolType) && ItemStackUtils.verifyToolLevel(stack,
            ItemStackUtils.getMiningLevel(stack, toolType),
            requiredLevel, maximumLevel)))) > -1;
    }

    /**
     * Method to swap the ItemStacks from the given source {@link IItemHandler}
     * to the given target {@link ICapabilityProvider}.
     *
     * @param sourceHandler  The {@link IItemHandler} that works as Source.
     * @param sourceIndex    The index of the slot that is being extracted
     *                       from.
     * @param targetProvider The {@link ICapabilityProvider} that works as
     *                       Target.
     * @return True when the swap was successful, false when not.
     */
    public static boolean transferItemStackIntoNextFreeSlotInProvider(
                                                                       @NotNull final IItemHandler sourceHandler,
                                                                       @NotNull final int sourceIndex,
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
     * Method to swap the ItemStacks from the given source {@link IItemHandler}
     * to the given target {@link IItemHandler}.
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

        if(ItemStackUtils.isEmpty(sourceStack))
        {
            return true;
        }

        for (int i = 0; i < targetHandler.getSlots(); i++)
        {
            sourceStack = targetHandler.insertItem(i, sourceStack, false);
            if (ItemStackUtils.isEmpty(sourceStack))
            {
                sourceHandler.extractItem(sourceIndex, Integer.MAX_VALUE, false);
                return true;
            }
        }

        final ItemStack originalStack = sourceStack.copy();
        if (!ItemStack.areItemStacksEqual(sourceStack, originalStack) && ItemStackUtils.compareItemStacksIgnoreStackSize(sourceStack, originalStack))
        {
            final int usedAmount = ItemStackUtils.getSize(sourceStack) - ItemStackUtils.getSize(originalStack);
            sourceHandler.extractItem(sourceIndex, usedAmount, false);
            return true;
        }

        return false;
    }

    /**
     * Method to swap the ItemStacks from the given source {@link IItemHandler}
     * to the given target {@link IItemHandler}. Trying to merge existing itemStacks if possible.
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

        if(ItemStackUtils.isEmpty(sourceStack))
        {
            return true;
        }

        sourceStack = mergeItemStackIntoNextBestSlotInItemHandlers(sourceHandler, sourceIndex, targetHandler);

        if(ItemStackUtils.isEmpty(sourceStack))
        {
            return true;
        }

        for (int i = 0; i < targetHandler.getSlots(); i++)
        {
            sourceStack = targetHandler.insertItem(i, sourceStack, false);
            if (ItemStackUtils.isEmpty(sourceStack))
            {
                sourceHandler.extractItem(sourceIndex, Integer.MAX_VALUE, false);
                return true;
            }
        }

        final ItemStack originalStack = sourceStack.copy();
        if (!ItemStack.areItemStacksEqual(sourceStack, originalStack) && ItemStackUtils.compareItemStacksIgnoreStackSize(sourceStack, originalStack))
        {
            final int usedAmount = ItemStackUtils.getSize(sourceStack) - ItemStackUtils.getSize(originalStack);
            sourceHandler.extractItem(sourceIndex, usedAmount, false);
            return true;
        }

        return false;
    }

    /**
     * Method to put a given Itemstack in a given target {@link IItemHandler}. Trying to merge existing itemStacks if possible.
     *
     * @param stack   the itemStack to transfer.
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
     * @param stack   the itemStack to transfer.
     * @param targetHandler The {@link IItemHandler} that works as Target.
     * @return the rest of the stack.
     */
    public static ItemStack transferItemStackIntoNextBestSlotInItemHandlerWithResult(final ItemStack stack, @NotNull final IItemHandler targetHandler)
    {
        ItemStack sourceStack = stack.copy();

        if(ItemStackUtils.isEmpty(sourceStack))
        {
            return sourceStack;
        }

        sourceStack = mergeItemStackIntoNextBestSlotInItemHandlers(sourceStack, targetHandler);

        if(ItemStackUtils.isEmpty(sourceStack))
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
     * Method to merge the ItemStacks from the given source {@link IItemHandler}
     * to the given target {@link IItemHandler}. Trying to merge itemStacks or returning stack if not possible.
     *
     * @param sourceHandler The {@link IItemHandler} that works as Source.
     * @param sourceIndex   The index of the slot that is being extracted from.
     * @param targetHandler The {@link IItemHandler} that works as Target.
     * @return True when the swap was successful, false when not.
     */
    public static ItemStack mergeItemStackIntoNextBestSlotInItemHandlers(
      @NotNull final IItemHandler sourceHandler,
      final int sourceIndex,
      @NotNull final IItemHandler targetHandler)
    {
        ItemStack sourceStack = sourceHandler.extractItem(sourceIndex, Integer.MAX_VALUE, true);

        if(ItemStackUtils.isEmpty(sourceStack))
        {
            return sourceStack;
        }

        for (int i = 0; i < targetHandler.getSlots(); i++)
        {
            if (!ItemStackUtils.isEmpty(targetHandler.getStackInSlot(i)) && targetHandler.getStackInSlot(i).isItemEqual(sourceStack))
            {
                sourceStack = targetHandler.insertItem(i, sourceStack, false);
                if (ItemStackUtils.isEmpty(sourceStack))
                {
                    sourceHandler.extractItem(sourceIndex, Integer.MAX_VALUE, false);
                    return sourceStack;
                }
            }
        }
        return sourceStack;
    }

    /**
     * Method to merge the ItemStacks from the given source {@link IItemHandler}
     * to the given target {@link IItemHandler}. Trying to merge itemStacks or returning stack if not possible.
     *
     * @param stack the stack to add.
     * @param targetHandler The {@link IItemHandler} that works as Target.
     * @return True when the swap was successful, false when not.
     */
    public static ItemStack mergeItemStackIntoNextBestSlotInItemHandlers(
      final ItemStack stack,
      @NotNull final IItemHandler targetHandler)
    {
        ItemStack sourceStack = stack.copy();

        if(ItemStackUtils.isEmpty(sourceStack))
        {
            return sourceStack;
        }

        for (int i = 0; i < targetHandler.getSlots(); i++)
        {
            if (!ItemStackUtils.isEmpty(targetHandler.getStackInSlot(i)) && targetHandler.getStackInSlot(i).isItemEqual(sourceStack))
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

    public static boolean transferXOfFirstSlotInProviderWithIntoNextFreeSlotInProvider(
      @NotNull final ICapabilityProvider sourceProvider,
      @NotNull final Predicate<ItemStack> itemStackSelectionPredicate,
      @NotNull final int amount, @NotNull final ICapabilityProvider targetProvider)
    {
        return transferXOfFirstSlotInProviderWithIntoNextFreeSlotInProviderWithResult(sourceProvider, itemStackSelectionPredicate, amount, targetProvider) == 0;
    }

    public static int transferXOfFirstSlotInProviderWithIntoNextFreeSlotInProviderWithResult(
      @NotNull final ICapabilityProvider sourceProvider,
      @NotNull final Predicate<ItemStack> itemStackSelectionPredicate,
      @NotNull final int amount, @NotNull final ICapabilityProvider targetProvider)
    {
        int currentAmount = amount;

        for(final IItemHandler handler : getItemHandlersFromProvider(targetProvider))
        {
            currentAmount = transferXOfFirstSlotInProviderWithIntoNextFreeSlotInItemHandlerWithResult(sourceProvider, itemStackSelectionPredicate, amount, handler);

            if (currentAmount <= 0)
            {
                return 0;
            }
        }

        return currentAmount;
    }

    public static boolean transferXOfFirstSlotInProviderWithIntoNextFreeSlotInItemHandler(
      @NotNull final ICapabilityProvider sourceProvider,
      @NotNull final Predicate<ItemStack> itemStackSelectionPredicate,
      @NotNull final int amount, @NotNull final IItemHandler targetHandler)
    {
        return transferXOfFirstSlotInProviderWithIntoNextFreeSlotInItemHandlerWithResult(sourceProvider, itemStackSelectionPredicate, amount, targetHandler) == 0;
    }

    public static int transferXOfFirstSlotInProviderWithIntoNextFreeSlotInItemHandlerWithResult(
      @NotNull final ICapabilityProvider sourceProvider,
      @NotNull final Predicate<ItemStack> itemStackSelectionPredicate,
      @NotNull final int amount, @NotNull final IItemHandler targetHandler)
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
                                                                                           @NotNull final int amount, @NotNull final IItemHandler targetHandler)
    {
        return transferXOfFirstSlotInItemHandlerWithIntoNextFreeSlotInItemHandlerWithResult(sourceHandler, itemStackSelectionPredicate, amount, targetHandler) == 0;
    }

    public static int transferXOfFirstSlotInItemHandlerWithIntoNextFreeSlotInItemHandlerWithResult(
      @NotNull final IItemHandler sourceHandler,
      @NotNull final Predicate<ItemStack> itemStackSelectionPredicate,
      @NotNull final int amount, @NotNull final IItemHandler targetHandler)
    {
        int currentAmount = amount;
        int tries = 0;
        while (currentAmount > 0)
        {
            tries++;
            if(tries > sourceHandler.getSlots())
            {
                break;
            }

            final int desiredItemSlot = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(sourceHandler,
              itemStackSelectionPredicate::test);

            if (desiredItemSlot == -1)
            {
                break;
            }


            final ItemStack returnStack = sourceHandler.extractItem(desiredItemSlot, currentAmount, false);

            if (!ItemStackUtils.isEmpty(returnStack))
            {
                currentAmount -= returnStack.getCount();
                if (!InventoryUtils.addItemStackToItemHandler(targetHandler, returnStack))
                {
                    break;
                }
            }
        }

        return currentAmount;
    }

    /**
     * Takes an item matching a predicate and moves it form one handler to the other to a specific slot.
     * @param sourceHandler the source handler.
     * @param itemStackSelectionPredicate the predicate.
     * @param amount the amount.
     * @param targetHandler the target.
     * @param slot the slot to put it in.
     * @return true if succesful.
     */
    public static boolean transferXOfFirstSlotInItemHandlerWithIntoInItemHandler(
            final IItemHandler sourceHandler,
            final Predicate<ItemStack> itemStackSelectionPredicate,
            final int amount,
            final IItemHandler targetHandler, final int slot)
    {
        final int desiredItemSlot = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(sourceHandler,
                itemStackSelectionPredicate::test);

        if (desiredItemSlot == -1)
        {
            return false;
        }
        final ItemStack returnStack = sourceHandler.extractItem(desiredItemSlot, amount, false);
        if (ItemStackUtils.isEmpty(returnStack))
        {
            return false;
        }
        targetHandler.insertItem(slot, returnStack, false);
        return true;
    }

    /**
     * Method to swap the ItemStacks from the given source {@link
     * ICapabilityProvider} to the given target {@link IItemHandler}.
     *
     * @param sourceProvider The {@link ICapabilityProvider} that works as
     *                       Source.
     * @param sourceIndex    The index of the slot that is being extracted
     *                       from.
     * @param targetHandler  The {@link IItemHandler} that works as Target.
     * @return True when the swap was successful, false when not.
     */
    public static boolean transferItemStackIntoNextFreeSlotFromProvider(
                                                                         @NotNull final ICapabilityProvider sourceProvider,
                                                                         @NotNull final int sourceIndex,
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
     * Method to swap the ItemStacks from the given source {@link
     * ICapabilityProvider} to the given target {@link IItemHandler}.
     *
     * @param sourceProvider The {@link ICapabilityProvider} that works as
     *                       Source.
     * @param sourceIndex    The index of the slot that is being extracted
     *                       from.
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
            if(ItemStackUtils.isEmpty(mergeItemStackIntoNextBestSlotInItemHandlers(handler, sourceIndex, targetHandler)))
            {
                return true;
            }
        }

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
     * Method to swap the ItemStacks from the given source {@link IItemHandler}
     * to the given target {@link IItemHandler}.
     *
     * @param sourceHandler The {@link IItemHandler} that works as Source.
     * @param sourceIndex   The index of the slot that is being extracted from.
     * @param targetHandler The {@link IItemHandler} that works as Target.
     * @param targetIndex   The index of the slot that is being inserted into.
     * @return True when the swap was successful, false when not.
     */
    public static boolean swapItemStacksInItemHandlers(
                                                        @NotNull final IItemHandler sourceHandler,
                                                        @NotNull final int sourceIndex,
                                                        @NotNull final IItemHandler targetHandler,
                                                        @NotNull final int targetIndex)
    {
        final ItemStack targetStack = targetHandler.extractItem(targetIndex, Integer.MAX_VALUE, false);
        final ItemStack sourceStack = sourceHandler.extractItem(sourceIndex, Integer.MAX_VALUE, true);

        final ItemStack resultSourceSimulationInsertion = targetHandler.insertItem(targetIndex, sourceStack, true);
        if (ItemStackUtils.isEmpty(resultSourceSimulationInsertion) || ItemStackUtils.isEmpty(targetStack))
        {
            targetHandler.insertItem(targetIndex, sourceStack, false);
            sourceHandler.extractItem(sourceIndex, Integer.MAX_VALUE, false);
            sourceHandler.insertItem(sourceIndex, targetStack, false);

            return true;
        }
        else
        {
            targetHandler.insertItem(targetIndex, targetStack, false);

            return false;
        }
    }

    /**
     * Remove a list of stacks from a given provider
     *
     * @param provider the provider.
     * @param input    the list of stacks.
     * @return true if succesful.
     */
    public static boolean removeStacksFromProvider(final ICapabilityProvider provider, final List<ItemStack> input)
    {
        for (final IItemHandler handler : getItemHandlersFromProvider(provider))
        {
            if (!removeStacksFromItemHandler(handler, input))
            {
                return false;
            }
        }

        return true;
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
            final int slot = findFirstSlotInItemHandlerNotEmptyWith(handler, stack::isItemEqual);

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
     * Remove a list of stacks from a given Itemhandler
     *
     * @param handler the itemHandler.
     * @param input   the stack to remove.
     * @return true if succesful.
     */
    public static boolean removeStackFromItemHandler(final IItemHandler handler, final ItemStack input)
    {
        int maxTries = 0;
        maxTries += ItemStackUtils.getSize(input);

        boolean success = true;
        int i = 0;
        int tries = 0;
        while (tries < maxTries)
        {
            final int slot = findFirstSlotInItemHandlerNotEmptyWith(handler, input::isItemEqual);

            if (slot == -1)
            {
                success = false;
                i++;
                continue;
            }

            final int removedSize = ItemStackUtils.getSize(handler.extractItem(slot, ItemStackUtils.getSize(input), false));

            if (removedSize == ItemStackUtils.getSize(input))
            {
                i++;
            }
            else
            {
                ItemStackUtils.changeSize(input, -removedSize);
            }
            tries++;
        }

        return success && i >= 1;
    }

    /**
     * Check if a certain item is in the provider but without the provider being full.
     *
     * @param provider   the provider to check.
     * @param item       the item.
     * @param itemDamage its damage.
     * @param amount     stack size to be considered.
     * @return the slot or -1.
     */
    public static int findSlotInProviderNotFullWithItem(final ICapabilityProvider provider, final Item item, final int itemDamage, final int amount)
    {
        for (final IItemHandler handler : getItemHandlersFromProvider(provider))
        {
            final int foundSlot = findSlotInItemHandlerNotFullWithItem(handler, (ItemStack stack) -> compareItems(stack, item, itemDamage), amount);
            //TODO: When contract is hardened later: Replace this -1 check with a try-catch block.
            if (foundSlot > -1)
            {
                return foundSlot;
            }
        }

        return -1;
    }

    /**
     * Check if a certain item is in the handler but without the provider being full.
     * Return as soon as an empty slot and a matching slot has been found.
     * Returns the last matching slot it found.
     *
     * @param handler                     the handler to check.
     * @param itemStackSelectionPredicate the selection predicate..
     * @param amount                      stack size to be considered.
     * @return the slot or -1.
     */
    public static int findSlotInItemHandlerNotFullWithItem(
                                                            final IItemHandler handler,
                                                            @NotNull final Predicate<ItemStack> itemStackSelectionPredicate,
                                                            final int amount)
    {
        boolean foundEmptySlot = false;
        boolean foundItem = false;
        int itemSlot = -1;
        for (int slot = 0; slot < handler.getSlots(); slot++)
        {
            final ItemStack stack = handler.getStackInSlot(slot);
            if (ItemStackUtils.isEmpty(stack))
            {
                foundEmptySlot = true;
            }
            else if (itemStackSelectionPredicate.test(stack))
            {
                if (ItemStackUtils.getSize(stack) + amount <= Constants.STACKSIZE)
                {
                    foundEmptySlot = true;
                }
                foundItem = true;
                itemSlot = slot;
            }

            if (foundItem && foundEmptySlot)
            {
                return itemSlot;
            }
        }

        return -1;
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
    public static void dropItemHandler(final IItemHandler handler, final World world, final int x, final int y, final int z)
    {
        for (int i = 0; i < handler.getSlots(); ++i)
        {
            final ItemStack itemstack = handler.getStackInSlot(i);

            if (itemstack != null)
            {
                spawnItemStack(world, x, y, z, itemstack);
            }
        }
    }

    /**
     * Spawn an itemStack in the world.
     *
     * @param worldIn the world.
     * @param x       the x pos.
     * @param y       the y pos.
     * @param z       the z pos.
     * @param stack   the stack to drop.
     */
    public static void spawnItemStack(final World worldIn, final double x, final double y, final double z, final ItemStack stack)
    {
        final Random random = new Random();
        final double spawnX = random.nextDouble() * SPAWN_MODIFIER + SPAWN_ADDITION;
        final double spawnY = random.nextDouble() * SPAWN_MODIFIER + SPAWN_ADDITION;
        final double spawnZ = random.nextDouble() * SPAWN_MODIFIER + SPAWN_ADDITION;

        while (stack.getCount() > 0)
        {
            final int randomSplitStackSize = random.nextInt(MAX_RANDOM_SPAWN) + MIN_RANDOM_SPAWN;
            final EntityItem entityitem = new EntityItem(worldIn, x + spawnX, y + spawnY, z + spawnZ, stack.splitStack(randomSplitStackSize));

            entityitem.motionX = random.nextGaussian() * MOTION_MULTIPLIER;
            entityitem.motionY = random.nextGaussian() * MOTION_MULTIPLIER + MOTION_Y_MIN;
            entityitem.motionZ = random.nextGaussian() * MOTION_MULTIPLIER;
            worldIn.spawnEntity(entityitem);
        }
    }
}
