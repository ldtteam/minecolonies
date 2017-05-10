package com.minecolonies.coremod.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
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

    public static final int FREE_TOOL_CHOICE_LEVEL   = 4;
    public static final int EFFECT_TOOL_CHOICE_LEVEL = 2;

    /**
     * Variable representing the empty itemstack in 1.10.
     * Used for easy updating to 1.11
     */
    public static final ItemStack EMPTY = ItemStack.EMPTY;

    /**
     * Predicate to check if an itemStack is empty.
     */
    @NotNull
    private static final Predicate<ItemStack> EMPTY_PREDICATE = InventoryUtils::isItemStackEmpty;

    /**
     * Negation of the itemStack empty predicate (not empty).
     */
    @NotNull
    private static final Predicate<ItemStack> NOT_EMPTY_PREDICATE = EMPTY_PREDICATE.negate();

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
    public static List<ItemStack> filterItemHandler(@NotNull final IItemHandler itemHandler, @NotNull final Block block, int metaData)
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
            if (!isItemStackEmpty(stack) && itemStackSelectionPredicate.test(stack))
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
    private static boolean compareItems(@Nullable final ItemStack itemStack, final Item targetItem, int itemDamage)
    {
        return !isItemStackEmpty(itemStack) && itemStack.getItem() == targetItem && (itemStack.getItemDamage() == itemDamage || itemDamage == -1);
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
     * Wrapper method to check if a stack is empty.
     * Used for easy updating to 1.11.
     *
     * @param stack The stack to check.
     * @return True when the stack is empty, false when not.
     */
    @NotNull
    public static Boolean isItemStackEmpty(@Nullable ItemStack stack)
    {
        return !(stack != EMPTY && stack.getItem() != null && stack.getCount() > 0);
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
    public static List<ItemStack> filterItemHandler(@NotNull final IItemHandler itemHandler, @NotNull final Item targetItem, int itemDamage)
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
    public static int findFirstSlotInItemHandlerWith(@NotNull final IItemHandler itemHandler, @NotNull final Block block, int itemDamage)
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
    public static int findFirstSlotInItemHandlerWith(@NotNull final IItemHandler itemHandler, @NotNull final Item targetItem, int itemDamage)
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
        //throw new IllegalStateException("Item "+targetItem.getUnlocalizedName() + " not found in ItemHandler!");
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
    public static int getItemCountInItemHandler(@NotNull final IItemHandler itemHandler, @NotNull final Block block, int itemDamage)
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
    public static int getItemCountInItemHandler(@NotNull final IItemHandler itemHandler, @NotNull final Item targetItem, int itemDamage)
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
        return filterItemHandler(itemHandler, itemStackSelectionPredicate).stream().mapToInt(InventoryUtils::getItemStackSize).sum();
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
    public static boolean hasItemInItemHandler(@NotNull final IItemHandler itemHandler, @NotNull final Block block, int itemDamage)
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
    public static boolean hasItemInItemHandler(@NotNull final IItemHandler itemHandler, @NotNull final Item item, int itemDamage)
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
     * Returns the first open slot in the {@link IItemHandler}.
     *
     * @param itemHandler The {@link IItemHandler} to check.
     * @return slot index or -1 if none found.
     */
    public static int getFirstOpenSlotFromItemHandler(@NotNull final IItemHandler itemHandler)
    {
        //Test with two different ItemStacks to insert in simulation mode.
        return IntStream.range(0, itemHandler.getSlots())
                .filter(slot -> isItemStackEmpty(itemHandler.getStackInSlot(slot)))
                .findFirst()
                .orElse(-1);
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
     * Looks for a {@link ItemPickaxe} to mine a block of {@code requiredLevel},
     * in the given {@link IItemHandler}.
     *
     * @param itemHandler   {@link IItemHandler} to check in.
     * @param requiredLevel The minimal required {@link ItemPickaxe} level
     * @return True if the {@link IItemHandler} contains a {@link ItemPickaxe}
     * with the given minimal required level.
     */
    public static boolean isPickaxeInItemHandler(IItemHandler itemHandler, final int requiredLevel)
    {
        return hasItemInItemHandler(itemHandler, (ItemStack stack) -> Utils.checkIfPickaxeQualifies(requiredLevel, Utils.getMiningLevel(stack, Utils.PICKAXE)));
    }

    /**
     * Looks for a {@link ItemPickaxe} to mine a block of {@code requiredLevel},
     * in the given {@link IItemHandler}. The {@link ItemPickaxe} tool level
     * cannot exceed the given {@code maximalLevel}.
     *
     * @param itemHandler   {@link IItemHandler} to check in.
     * @param requiredLevel The minimal required {@link ItemPickaxe} level
     * @param maximalLevel  The maximal tool level of the {@link ItemPickaxe}
     * @return True if the {@link IItemHandler} contains a {@link ItemPickaxe}
     * with the given minimal required level.
     */
    public static boolean isPickaxeInItemHandler(IItemHandler itemHandler, final int requiredLevel, final int maximalLevel)
    {
        return hasItemInItemHandler(itemHandler, (ItemStack stack) -> Utils.checkIfPickaxeQualifies(requiredLevel, Utils.getMiningLevel(stack, Utils.PICKAXE))
                                                                        && InventoryUtils.hasToolLevel(stack, Utils.PICKAXE, maximalLevel));
    }

    /**
     * Verifies if there is one tool with an acceptable level
     * in a worker's inventory.
     *
     * @param toolName     the type of tool needed
     * @param stack        the stack to test.
     * @param minimalLevel the worker's hut level
     * @return true if tool is acceptable
     */
    public static boolean hasToolLevel(@Nullable ItemStack stack, final String toolName, final int minimalLevel)
    {
        if (isItemStackEmpty(stack))
        {
            return false;
        }

        final int level = Utils.getMiningLevel(stack, toolName);
        if (Utils.isTool(stack, toolName) && verifyToolLevel(stack, level, minimalLevel))
        {
            return true;
        }

        return false;
    }

    /**
     * Verifies if an item has an appropriated grade.
     *
     * @param itemStack    the type of tool needed
     * @param toolLevel    the type of tool needed
     * @param minimalLevel the worker's hut level
     * @return true if tool is acceptable
     */
    public static boolean verifyToolLevel(@NotNull final ItemStack itemStack, int toolLevel, final int minimalLevel)
    {
        if (isItemStackEmpty(itemStack) || minimalLevel > FREE_TOOL_CHOICE_LEVEL || minimalLevel >= toolLevel)
        {
            return true;
        }
        else if (itemStack.isItemEnchanted() && minimalLevel <= EFFECT_TOOL_CHOICE_LEVEL)
        {
            return false;
        }

        return false;
    }

    /**
     * Adapted from {@link net.minecraft.entity.player.InventoryPlayer#addItemStackToInventory(ItemStack)}.
     *
     * @param itemHandler {@link IItemHandler} to add itemstack to.
     * @param itemStack   ItemStack to add.
     * @return True if successful, otherwise false.
     */
    public static boolean addItemStackToItemHandler(@NotNull final IItemHandler itemHandler, @Nullable ItemStack itemStack)
    {
        if (!isItemStackEmpty(itemStack))
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
                while (!isItemStackEmpty(resultStack) && slot != -1 && slot != itemHandler.getSlots())
                {
                    resultStack = itemHandler.insertItem(slot, resultStack, false);
                    if (!isItemStackEmpty(resultStack))
                    {
                        slot++;
                    }
                }


                return isItemStackEmpty(resultStack);
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
        ItemStack standardInsertionResult = addItemStackToItemHandlerWithResult(itemHandler, itemStack);

        if (!isItemStackEmpty(standardInsertionResult))
        {
            for (int i = 0; i < itemHandler.getSlots() && !isItemStackEmpty(standardInsertionResult); i++)
            {
                final ItemStack localStack = itemHandler.getStackInSlot(i);
                if (isItemStackEmpty(localStack) || !itemStackToKeepPredicate.test(localStack))
                {
                    final ItemStack removedStack = itemHandler.extractItem(i, Integer.MAX_VALUE, false);
                    ItemStack localInsertionResult = itemHandler.insertItem(i, standardInsertionResult, true);

                    if (isItemStackEmpty(localInsertionResult))
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
    public static List<ItemStack> filterProvider(@NotNull final ICapabilityProvider provider, final Block block, int metaData)
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
    public static List<ItemStack> filterProvider(@NotNull final ICapabilityProvider provider, @NotNull Predicate<ItemStack> itemStackSelectionPredicate)
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
    private static List<ItemStack> getFromProviderForAllSides(@NotNull final ICapabilityProvider provider, @NotNull Predicate<ItemStack> predicate)
    {
        final ArrayList<ItemStack> combinedList = new ArrayList<>();

        for (IItemHandler handler : getItemHandlersFromProvider(provider))
        {
            combinedList.addAll(filterItemHandler(handler, predicate));
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
    public static List<IItemHandler> getItemHandlersFromProvider(@NotNull ICapabilityProvider provider)
    {
        final ArrayList<IItemHandler> handlerList = Arrays.stream(EnumFacing.VALUES)
                                                      .filter(facing -> provider.hasCapability(ITEM_HANDLER_CAPABILITY, facing))
                                                      .map(facing -> provider.getCapability(ITEM_HANDLER_CAPABILITY, facing))
                                                      .distinct()
                                                      .collect(Collectors.toCollection(ArrayList::new));

        if (provider.hasCapability(ITEM_HANDLER_CAPABILITY, null))
        {
            final IItemHandler nullHandler = provider.getCapability(ITEM_HANDLER_CAPABILITY, null);
            if (!handlerList.contains(nullHandler))
            {
                handlerList.add(nullHandler);
            }
        }

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
    public static List<ItemStack> filterProvider(@NotNull final ICapabilityProvider provider, @Nullable final Item targetItem, int itemDamage)
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
    public static int findFirstSlotInProviderWith(@NotNull final ICapabilityProvider provider, final Block block, int itemDamage)
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
    public static int findFirstSlotInProviderWith(@NotNull final ICapabilityProvider provider, final Item targetItem, int itemDamage)
    {
        return findFirstSlotInProviderWith(provider, (ItemStack stack) -> compareItems(stack, targetItem, itemDamage));
    }

    /**
     * Returns the index of the first occurrence of an ItemStack that matches
     * the given predicate in the {@link ICapabilityProvider}.
     *
     * @param provider                    Provider to check
     * @param itemStackSelectionPredicate The predicate to match.
     * @return Index of the first occurrence
     */
    public static int findFirstSlotInProviderWith(@NotNull final ICapabilityProvider provider, Predicate<ItemStack> itemStackSelectionPredicate)
    {
        for (IItemHandler handler : getItemHandlersFromProvider(provider))
        {
            int foundSlot = findFirstSlotInItemHandlerWith(handler, itemStackSelectionPredicate);
            //TODO: When contract is hardened later: Replace this -1 check with a try-catch block.
            if (foundSlot > -1)
            {
                return foundSlot;
            }
        }

        return -1;
        //TODO: Later harden contract to remove compare on slot := -1
        //throw new IllegalStateException("Item "+targetItem.getUnlocalizedName() + " not found in ItemHandler!");
    }

    /**
     * Returns the index of the first occurrence of an ItemStack that matches
     * the given predicate in the {@link ICapabilityProvider}.
     *
     * @param provider                    Provider to check
     * @param itemStackSelectionPredicate The predicate to match.
     * @return Index of the first occurrence
     */
    public static int findFirstSlotInProviderNotEmptyWith(@NotNull final ICapabilityProvider provider, Predicate<ItemStack> itemStackSelectionPredicate)
    {
        for (IItemHandler handler : getItemHandlersFromProvider(provider))
        {
            int foundSlot = findFirstSlotInItemHandlerNotEmptyWith(handler, itemStackSelectionPredicate);
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
     * @param itemStackSelectionPredicate The predicate to match.
     * @return Index of the first occurrence
     */
    public static int findFirstSlotInItemHandlerNotEmptyWith(@NotNull final IItemHandler itemHandler, @NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        @NotNull final Predicate<ItemStack> firstWorthySlotPredicate = NOT_EMPTY_PREDICATE.and(itemStackSelectionPredicate);

        for (int slot = 0; slot < itemHandler.getSlots(); slot++)
        {
            if (firstWorthySlotPredicate.test(itemHandler.getStackInSlot(slot)))
            {
                return slot;
            }
        }

        return -1;
        //TODO: Later harden contract to remove compare on slot := -1
        //throw new IllegalStateException("Item "+targetItem.getUnlocalizedName() + " not found in ItemHandler!");
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
    public static int getItemCountInProvider(@NotNull final ICapabilityProvider provider, @NotNull final Block block, int itemDamage)
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
    public static int getItemCountInProvider(@NotNull final ICapabilityProvider provider, @NotNull final Item targetItem, int itemDamage)
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
        return getItemHandlersFromProvider(provider).stream()
                 .mapToInt(handler -> filterItemHandler(handler, itemStackSelectionPredicate).stream().mapToInt(InventoryUtils::getItemStackSize).sum())
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
    public static boolean hasItemInProvider(@NotNull final ICapabilityProvider Provider, @NotNull final Block block, int itemDamage)
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
    public static boolean hasItemInProvider(@NotNull final ICapabilityProvider Provider, @NotNull final Item item, int itemDamage)
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
        return getItemHandlersFromProvider(provider).stream().mapToInt(InventoryUtils::getFirstOpenSlotFromItemHandler).filter(slotIndex -> slotIndex > -1).findFirst().orElse(-1);
    }

    /**
     * Checks if the {@link ICapabilityProvider} contains the following toolName
     * with the given minimal Level.
     *
     * @param provider     The {@link ICapabilityProvider} to scan.
     * @param toolTypeName The toolTypeName of the tool to find.
     * @param minimalLevel The minimal level to find.
     * @return True if a Tool with the given toolTypeName was found in the given
     * {@link ICapabilityProvider}, false when not.
     */
    public static boolean isToolInProvider(@NotNull final ICapabilityProvider provider, @NotNull final String toolTypeName, int minimalLevel)
    {
        return hasItemInProvider(provider, (ItemStack stack) -> Utils.isTool(stack, toolTypeName) && InventoryUtils.hasToolLevel(stack, toolTypeName, minimalLevel));
    }

    /**
     * Looks for a {@link ItemPickaxe} to mine a block of {@code requiredLevel},
     * in the given {@link ICapabilityProvider}.
     *
     * @param provider      {@link ICapabilityProvider} to check in.
     * @param requiredLevel The minimal required {@link ItemPickaxe} level
     * @return True if the {@link ICapabilityProvider} contains a {@link
     * ItemPickaxe} with the given minimal required level.
     */
    public static boolean isPickaxeInProvider(ICapabilityProvider provider, final int requiredLevel)
    {
        return hasItemInProvider(provider, (ItemStack stack) -> Utils.checkIfPickaxeQualifies(requiredLevel, Utils.getMiningLevel(stack, Utils.PICKAXE)));
    }

    /**
     * Looks for a {@link ItemPickaxe} to mine a block of {@code requiredLevel},
     * in the given {@link ICapabilityProvider}. The {@link ItemPickaxe} tool
     * level cannot exceed the given {@code maximalLevel}.
     *
     * @param provider      {@link ICapabilityProvider} to check in.
     * @param requiredLevel The minimal required {@link ItemPickaxe} level
     * @param maximalLevel  The maximal tool level of the {@link ItemPickaxe}
     * @return True if the {@link ICapabilityProvider} contains a {@link
     * ItemPickaxe} with the given minimal required level.
     */
    public static boolean isPickaxeInProvider(ICapabilityProvider provider, final int requiredLevel, final int maximalLevel)
    {
        return hasItemInProvider(provider, (ItemStack stack) -> Utils.checkIfPickaxeQualifies(requiredLevel, Utils.getMiningLevel(stack, Utils.PICKAXE))
                                                                  && InventoryUtils.hasToolLevel(stack, Utils.PICKAXE, maximalLevel));
    }

    /**
     * Adapted from {@link net.minecraft.entity.player.InventoryPlayer#addItemStackToInventory(ItemStack)}.
     *
     * @param provider  {@link ICapabilityProvider} to add itemstack to.
     * @param itemStack ItemStack to add.
     * @return True if successful, otherwise false.
     */
    public static boolean addItemStackToProvider(@NotNull final ICapabilityProvider provider, @Nullable ItemStack itemStack)
    {
        return getItemHandlersFromProvider(provider).stream().anyMatch(handler -> addItemStackToItemHandler(handler, itemStack));
    }

    /**
     * Adapted from {@link net.minecraft.entity.player.InventoryPlayer#addItemStackToInventory(ItemStack)}.
     *
     * @param provider  {@link ICapabilityProvider} to add itemstack to.
     * @param itemStack ItemStack to add.
     * @return True if successful, otherwise false.
     */
    public static ItemStack addItemStackToProviderWithResult(@NotNull final ICapabilityProvider provider, @Nullable ItemStack itemStack)
    {
        ItemStack activeStack = itemStack;

        if (isItemStackEmpty(activeStack))
        {
            return EMPTY;
        }

        for (IItemHandler handler : getItemHandlersFromProvider(provider))
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
     * @return True if successful, otherwise false.
     */
    public static ItemStack addItemStackToItemHandlerWithResult(@NotNull final IItemHandler itemHandler, @Nullable ItemStack itemStack)
    {
        if (!isItemStackEmpty(itemStack))
        {
            int slot;

            if (itemStack.isItemDamaged())
            {
                slot = getFirstOpenSlotFromItemHandler(itemHandler);

                if (slot >= 0)
                {
                    itemHandler.insertItem(slot, itemStack, false);
                    return EMPTY;
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
                while (!isItemStackEmpty(resultStack) && slot != -1 && slot != itemHandler.getSlots())
                {
                    resultStack = itemHandler.insertItem(slot, resultStack, false);
                    if (!isItemStackEmpty(resultStack))
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

        if (!isItemStackEmpty(standardInsertionResult))
        {
            ItemStack resultStack = standardInsertionResult.copy();
            Iterator<IItemHandler> iterator = getItemHandlersFromProvider(provider).iterator();
            while (iterator.hasNext() && !isItemStackEmpty(resultStack))
            {
                resultStack = forceItemStackToItemHandler(iterator.next(), resultStack, itemStackToKeepPredicate);
            }

            return resultStack;
        }

        return EMPTY;
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
    public static boolean hasProviderIItemHandler(@NotNull ICapabilityProvider provider)
    {
        return !getItemHandlersFromProvider(provider).isEmpty();
    }

    /**
     * Method used to check if this provider is sided.
     *
     * @param provider The provider to check for.
     * @return True when the provider has multiple distinct IItemHandler of
     * different sides (sidedness {@link TileEntityFurnace#hasCapability(Capability,
     * EnumFacing)}), false when not {@link TileEntityChest#hasCapability(Capability,
     * EnumFacing)}
     */
    @NotNull
    public static boolean isProviderSided(@NotNull ICapabilityProvider provider)
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
    public static List<ItemStack> getInventoryAsListFromProviderForSide(@NotNull final ICapabilityProvider provider, @Nullable EnumFacing facing)
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
                                                                        @Nullable EnumFacing facing,
                                                                        @NotNull final Block block,
                                                                        int metaData)
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
                                                                        @Nullable EnumFacing facing,
                                                                        @NotNull final Item targetItem,
                                                                        int itemDamage)
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
                                                                        @Nullable EnumFacing facing,
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
    public static int findFirstSlotInProviderForSideWith(@NotNull final ICapabilityProvider provider, @Nullable EnumFacing facing, @NotNull final Block block, int itemDamage)
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
    public static int findFirstSlotInProviderForSideWith(@NotNull final ICapabilityProvider provider, @Nullable EnumFacing facing, @NotNull final Item targetItem, int itemDamage)
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
                                                          @Nullable EnumFacing facing,
                                                          @NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        if (!provider.hasCapability(ITEM_HANDLER_CAPABILITY, facing))
        {
            return -1;
            //TODO: Later harden contract to remove compare on slot := -1
            //throw new IllegalStateException("Item "+targetItem.getUnlocalizedName() + " not found in ItemHandler!");
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
    public static int getItemCountInProviderForSide(@NotNull final ICapabilityProvider provider, @Nullable EnumFacing facing, @NotNull final Block block, int itemDamage)
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
    public static int getItemCountInProviderForSide(@NotNull final ICapabilityProvider provider, @Nullable EnumFacing facing, @NotNull final Item targetItem, int itemDamage)
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
                                                     @Nullable EnumFacing facing,
                                                     @NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        if (!provider.hasCapability(ITEM_HANDLER_CAPABILITY, facing))
        {
            return 0;
        }

        return filterItemHandler(provider.getCapability(ITEM_HANDLER_CAPABILITY, facing), itemStackSelectionPredicate).stream().mapToInt(InventoryUtils::getItemStackSize).sum();
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
    public static boolean hasItemInProviderForSide(@NotNull final ICapabilityProvider provider, @Nullable EnumFacing facing, @NotNull final Block block, int itemDamage)
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
    public static boolean hasItemInProviderForSide(@NotNull final ICapabilityProvider provider, @Nullable EnumFacing facing, @NotNull final Item item, int itemDamage)
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
                                                    @Nullable EnumFacing facing,
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
    public static boolean isProviderFull(@NotNull final ICapabilityProvider provider, @Nullable EnumFacing facing)
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
    public static int getFirstOpenSlotFromProviderForSide(@NotNull final ICapabilityProvider provider, @Nullable EnumFacing facing)
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
     * @param toolTypeName The toolTypeName of the tool to find.
     * @param minimalLevel The minimal level to find.
     * @return True if a Tool with the given toolTypeName was found in the given
     * {@link ICapabilityProvider}, false when not.
     */
    public static boolean isToolInProviderForSide(@NotNull final ICapabilityProvider provider, @Nullable EnumFacing facing, @NotNull final String toolTypeName, int minimalLevel)
    {
        if (!provider.hasCapability(ITEM_HANDLER_CAPABILITY, facing))
        {
            return false;
        }

        return isToolInItemHandler(provider.getCapability(ITEM_HANDLER_CAPABILITY, facing), toolTypeName, minimalLevel);
    }

    /**
     * Checks if the {@link IItemHandler} contains the following toolName with
     * the given minimal Level.
     *
     * @param itemHandler  The {@link IItemHandler} to scan.
     * @param toolTypeName The toolTypeName of the tool to find.
     * @param minimalLevel The minimal level to find.
     * @return True if a Tool with the given toolTypeName was found in the given
     * {@link IItemHandler}, false when not.
     */
    public static boolean isToolInItemHandler(@NotNull final IItemHandler itemHandler, @NotNull final String toolTypeName, int minimalLevel)
    {
        return hasItemInItemHandler(itemHandler, (ItemStack stack) -> Utils.isTool(stack, toolTypeName) && InventoryUtils.hasToolLevel(stack, toolTypeName, minimalLevel));
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
     * @param itemHandler the {@link IItemHandler} to get the slot from.
     * @param tool        the tool type to look for.
     * @return slot number if found, -1 if not found.
     */
    public static int getFirstSlotOfItemHandlerContainingTool(@NotNull final IItemHandler itemHandler, @NotNull final String tool)
    {
        return findFirstSlotInItemHandlerWith(itemHandler,
          (ItemStack stack) -> (!isItemStackEmpty(stack) && (stack.getItem().getToolClasses(stack).contains(tool) || ("hoe".equals(tool) && stack.getUnlocalizedName()
                                                                                                                                              .contains("hoe"))
                                                               || ("rod".equals(tool) && stack.getUnlocalizedName().contains("fishingRod")))));
    }

    /**
     * Verifies if there is one tool with an acceptable level
     * in a worker's inventory.
     *
     * @param toolTypeName  the type of tool needed
     * @param itemHandler   the worker's inventory
     * @param requiredLevel the worker's hut level
     * @return true if tool is acceptable
     */
    public static boolean hasItemHandlerToolWithLevel(@NotNull final IItemHandler itemHandler, final String toolTypeName, final int requiredLevel)
    {
        return findFirstSlotInItemHandlerWith(itemHandler,
          (ItemStack stack) -> (!isItemStackEmpty(stack) && (Utils.isTool(stack, toolTypeName) && verifyToolLevel(stack,
            Utils.getMiningLevel(stack, toolTypeName),
            requiredLevel)))) > -1;
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
                                                                       @NotNull int sourceIndex,
                                                                       @NotNull ICapabilityProvider targetProvider)
    {
        for (IItemHandler handler : getItemHandlersFromProvider(targetProvider))
        {
            if (transferItemStackIntoNextFreeSlotInItemHandlers(sourceHandler, sourceIndex, handler))
            {
                return true;
            }
        }

        return false;
    }

    public static boolean transferXOfFirstSlotInProviderWithIntoNextFreeSlotInItemHandler(@NotNull final IItemHandler sourceHandler,
            @NotNull final Predicate<ItemStack> itemStackSelectionPredicate,
            @NotNull int amount, @NotNull IItemHandler targetHandler)
    {
        final int desiredItemSlot = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(sourceHandler,
                itemStackSelectionPredicate::test);

        if(desiredItemSlot == -1)
        {
            return false;
        }
        final ItemStack returnStack = sourceHandler.extractItem(desiredItemSlot, amount, false);
        if(InventoryUtils.isItemStackEmpty(returnStack))
        {
            return false;
        }
        return InventoryUtils.addItemStackToItemHandler(targetHandler, returnStack);
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
    public static boolean transferItemStackIntoNextFreeSlotInItemHandlers(@NotNull final IItemHandler sourceHandler, @NotNull int sourceIndex, @NotNull IItemHandler targetHandler)
    {
        ItemStack sourceStack = sourceHandler.extractItem(sourceIndex, Integer.MAX_VALUE, true);
        final ItemStack originalStack = sourceStack.copy();

        for (int i = 0; i < targetHandler.getSlots(); i++)
        {
            sourceStack = targetHandler.insertItem(i, sourceStack, false);
            if (isItemStackEmpty(sourceStack))
            {
                sourceHandler.extractItem(sourceIndex, Integer.MAX_VALUE, false);
                return true;
            }
        }

        if (!ItemStack.areItemStacksEqual(sourceStack, originalStack) && compareItemStacksIgnoreStackSize(sourceStack, originalStack))
        {
            int usedAmount = getItemStackSize(sourceStack) - getItemStackSize(originalStack);
            sourceHandler.extractItem(sourceIndex, usedAmount, false);
            return true;
        }

        return false;
    }

    /**
     * Method to compare to stacks, ignoring their stacksize.
     *
     * @param itemStack1 The left stack to compare.
     * @param itemStack2 The right stack to compare.
     * @return True when they are equal except the stacksize, false when not.
     */
    @NotNull
    public static Boolean compareItemStacksIgnoreStackSize(ItemStack itemStack1, ItemStack itemStack2)
    {
        if (!isItemStackEmpty(itemStack1) &&
            !isItemStackEmpty(itemStack2) &&
            itemStack1.getItem() == itemStack2.getItem() &&
            itemStack1.getItemDamage() == itemStack2.getItemDamage())
        {
            // Then sort on NBT
            if (itemStack1.hasTagCompound() && itemStack2.hasTagCompound())
            {
                // Then sort on stack size
                return ItemStack.areItemStackTagsEqual(itemStack1, itemStack2);
            }
            else
            {
                return true;
            }
        }
        return false;
    }

    @NotNull
    public static int getItemStackSize(ItemStack stack)
    {
        if (isItemStackEmpty(stack))
        {
            return 0;
        }

        return stack.getCount();
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
                                                                         @NotNull int sourceIndex,
                                                                         @NotNull IItemHandler targetHandler)
    {
        for (IItemHandler handler : getItemHandlersFromProvider(sourceProvider))
        {
            if (transferItemStackIntoNextFreeSlotInItemHandlers(handler, sourceIndex, targetHandler))
            {
                return true;
            }
        }

        return false;
    }

    //TODO (UPDATE To 1.11): Update next two methods to reflect 1.11 Changes.

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
                                                        @NotNull int sourceIndex,
                                                        @NotNull IItemHandler targetHandler,
                                                        @NotNull int targetIndex)
    {
        ItemStack targetStack = targetHandler.extractItem(targetIndex, Integer.MAX_VALUE, false);
        ItemStack sourceStack = sourceHandler.extractItem(sourceIndex, Integer.MAX_VALUE, true);

        ItemStack resultSourceSimulationInsertion = targetHandler.insertItem(targetIndex, sourceStack, true);
        if (isItemStackEmpty(resultSourceSimulationInsertion) || isItemStackEmpty(targetStack))
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
     * Assigns a string containing the grade of the toolGrade.
     *
     * @param toolGrade the number of the grade of a tool
     * @return a string corresponding to the tool
     */
    public static String swapToolGrade(final int toolGrade)
    {
        switch (toolGrade)
        {
            case 0:
                return "Wood or Gold";
            case 1:
                return "Stone";
            case 2:
                return "Iron";
            case 3:
                return "Diamond";
            default:
                return "Better than Diamond";
        }
    }

    /**
     * Method to check if two ItemStacks can be merged together.
     *
     * @param existingStack The existing stack.
     * @param mergingStack  The merging stack
     * @return True when they can be merged, false when not.
     */
    @NotNull
    public static Boolean areItemStacksMergable(ItemStack existingStack, ItemStack mergingStack)
    {
        if (!compareItemStacksIgnoreStackSize(existingStack, mergingStack))
        {
            return false;
        }

        return existingStack.getMaxStackSize() >= (getItemStackSize(existingStack) + getItemStackSize(mergingStack));
    }
}
