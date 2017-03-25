package com.minecolonies.coremod.util;

import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.entity.ai.citizen.deliveryman.EntityAIWorkDeliveryman;
import com.minecolonies.coremod.entity.ai.item.handling.ItemStorage;
import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
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

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
     * Private constructor to hide the implicit one.
     */
    private InventoryUtils()
    {
        /*
         * Intentionally left empty.
         */
    }

    /*
    ##################################################################START: IItemHandler Interaction##################################################################
    IItemHandler region of the methods.
    Handles all the interaction with the IItemHandlers directly.
    ##################################################################START: IItemHandler Interaction##################################################################
     */

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
     * Filters a list of items, matches the stack using {@link #compareItems(ItemStack, Item, int)}, in an {@link IItemHandler}.
     * Uses the MetaData and {@link #getItemFromBlock(Block)} as parameters for the Predicate.
     *
     * @param itemHandler Inventory to filter in
     * @param block       Block to filter
     * @param metaData    the damage value.
     * @return List of item stacks
     */
    @NotNull
    public static List<ItemStack> filterItemHandler(@NotNull final IItemHandler itemHandler, @Nonnull final Block block, int metaData)
    {
        return filterItemHandler(itemHandler, (ItemStack stack) -> compareItems(stack, getItemFromBlock(block), metaData));
    }

    /**
     * Filters a list of items, matches the stack using {@link #compareItems(ItemStack, Item, int)}, with targetItem and itemDamage as parameters, in an {@link IItemHandler}.
     *
     * @param itemHandler Inventory to get items from
     * @param targetItem  Item to look for
     * @param itemDamage  the damage value.
     * @return List of item stacks with the given item in inventory
     */
    @NotNull
    public static List<ItemStack> filterItemHandler(@NotNull final IItemHandler itemHandler, @Nonnull final Item targetItem, int itemDamage)
    {
        return filterItemHandler(itemHandler, (ItemStack stack) -> compareItems(stack, targetItem, itemDamage));
    }
    
    /**
     * Filters a list of items, that match the given predicate, in an {@link IItemHandler}.
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
     * Returns the index of the first occurrence of the block in the {@link IItemHandler}.
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
     * Returns the index of the first occurrence of the Item with the given ItemDamage in the {@link IItemHandler}.
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
     * Returns the index of the first occurrence of an ItemStack that matches the given predicate in the {@link IItemHandler}.
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
     * @param itemHandler  {@link IItemHandler} to scan.
     * @param block      The block to count
     * @param itemDamage the damage value
     * @return Amount of occurrences of stacks that match the given block and ItemDamage
     */
    public static int getItemCountInItemHandler(@NotNull final IItemHandler itemHandler, @NotNull final Block block, int itemDamage)
    {
        return getItemCountInItemHandler(itemHandler, getItemFromBlock(block), itemDamage);
    }

    /**
     * Returns the amount of occurrences in the {@link IItemHandler}.
     *
     * @param itemHandler  {@link IItemHandler} to scan.
     * @param targetItem Item to count
     * @param itemDamage the item damage value.
     * @return Amount of occurrences of stacks that match the given item and ItemDamage
     */
    public static int getItemCountInItemHandler(@NotNull final IItemHandler itemHandler, @NotNull final Item targetItem, int itemDamage)
    {
        return getItemCountInItemHandler(itemHandler, (ItemStack stack) -> compareItems(stack, targetItem, itemDamage));
    }


    /**
     * Returns the amount of occurrences in the {@link IItemHandler}.
     *
     * @param itemHandler  {@link IItemHandler} to scan.
     * @param itemStackSelectionPredicate The predicate used to select the stacks to count.
     * @return Amount of occurrences of stacks that match the given predicate.
     */
    public static int getItemCountInItemHandler(@NotNull final IItemHandler itemHandler, @NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        return filterItemHandler(itemHandler, itemStackSelectionPredicate).stream().mapToInt(stack -> stack.stackSize).sum();
    }


    /**
     * Checks if a player has a block in the {@link IItemHandler}.
     * Checked by {@link #getItemCountInItemHandler(IItemHandler, Block, int)} &gt; 0;
     *
     * @param itemHandler  {@link IItemHandler} to scan
     * @param block      Block to count
     * @param itemDamage the damage value.
     * @return True when in {@link IItemHandler}, otherwise false
     */
    public static boolean hasItemInItemHandler(@NotNull final IItemHandler itemHandler, @NotNull final Block block, int itemDamage)
    {
        return hasItemInItemHandler(itemHandler, getItemFromBlock(block), itemDamage);
    }

    /**
     * Checks if a player has an item in the {@link IItemHandler}.
     * Checked by {@link #getItemCountInItemHandler(IItemHandler, Item, int)} &gt; 0;
     *
     * @param itemHandler  {@link IItemHandler} to scan
     * @param item       Item to count
     * @param itemDamage the damage value of the item.
     * @return True when in {@link IItemHandler}, otherwise false
     */
    public static boolean hasItemInItemHandler(@NotNull final IItemHandler itemHandler, @NotNull final Item item, int itemDamage)
    {
        return hasItemInItemHandler(itemHandler, (ItemStack stack) -> compareItems(stack, item, itemDamage));
    }

    /**
     * Checks if a player has an item in the {@link IItemHandler}.
     * Checked by {@link #getItemCountInItemHandler(IItemHandler, Predicate<ItemStack>)} &gt; 0;
     *
     * @param itemHandler  {@link IItemHandler} to scan
     * @param itemStackSelectionPredicate The predicate to match the ItemStack to.                    
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
        return IntStream.range(0, itemHandler.getSlots()).filter(slot -> isItemStackEmpty(itemHandler.getStackInSlot(slot))).findFirst().orElse(-1);
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
     * Checks if the {@link IItemHandler} contains the following toolName with the given minimal Level.
     *
     * @param itemHandler    The {@link IItemHandler} to scan.
     * @param toolTypeName   The toolTypeName of the tool to find.
     * @param minimalLevel   The minimal level to find.
     * @return True if a Tool with the given toolTypeName was found in the given {@link IItemHandler}, false when not.
     */
    public static boolean isToolInItemHandler(@NotNull final IItemHandler itemHandler, @NotNull final String toolTypeName, int minimalLevel)
    {
        return hasItemInItemHandler(itemHandler, (ItemStack stack) -> Utils.isTool(stack, toolTypeName) && InventoryUtils.hasToolLevel(stack, toolTypeName, minimalLevel));
    }

    /**
     * Looks for a {@link ItemPickaxe} to mine a block of {@code requiredLevel}, in the given {@link IItemHandler}.
     *
     * @param itemHandler   {@link IItemHandler} to check in.
     * @param requiredLevel The minimal required {@link ItemPickaxe} level
     * @return True if the {@link IItemHandler} contains a {@link ItemPickaxe} with the given minimal required level.
     */
    public static boolean isPickaxeInItemHandler(IItemHandler itemHandler, final int requiredLevel)
    {
        return hasItemInItemHandler(itemHandler, (ItemStack stack) -> Utils.checkIfPickaxeQualifies(requiredLevel, Utils.getMiningLevel(stack, Utils.PICKAXE)));
    }

    /**
     * Looks for a {@link ItemPickaxe} to mine a block of {@code requiredLevel}, in the given {@link IItemHandler}.
     * The {@link ItemPickaxe} tool level cannot exceed the given {@code maximalLevel}.
     *
     * @param itemHandler   {@link IItemHandler} to check in.
     * @param requiredLevel The minimal required {@link ItemPickaxe} level
     * @param maximalLevel The maximal tool level of the {@link ItemPickaxe}
     * @return True if the {@link IItemHandler} contains a {@link ItemPickaxe} with the given minimal required level.
     */
    public static boolean isPickaxeInItemHandler(IItemHandler itemHandler, final int requiredLevel, final int maximalLevel)
    {
        return hasItemInItemHandler(itemHandler, (ItemStack stack) -> Utils.checkIfPickaxeQualifies(requiredLevel, Utils.getMiningLevel(stack, Utils.PICKAXE))
                                                                        && InventoryUtils.hasToolLevel(stack, Utils.PICKAXE, maximalLevel));
    }

    /**
     * Returns a slot number if a {@link IItemHandler} contains given ItemStack item that is not fully stacked and with which a stack made of the given block and itemDamage can be
     * merged.
     *
     * @param itemHandler the {@link IItemHandler} to check.
     * @param block The block to test against.
     * @param itemDamage The item damage of a stack with that block to test against.
     * @return slot number if found, -1 when not found.
     */
    public static int getFirstFillablePositionInItemHandler(@NotNull final IItemHandler itemHandler, @NotNull final Block block, int itemDamage)
    {
        return getFirstFillablePositionInItemHandler(itemHandler, getItemFromBlock(block), itemDamage);
    }

    /**
     * Returns a slot number if a {@link IItemHandler} contains given ItemStack item that is not fully stacke and with which a stack made of the given item and itemDamage can be
     * merged.
     *
     * @param itemHandler the {@link IItemHandler} to check.
     * @param item The item to test against.
     * @param itemDamage The item damage of a stack with that block to test against.
     * @return slot number if found, -1 when not found.
     */
    public static int getFirstFillablePositionInItemHandler(@NotNull final IItemHandler itemHandler, @NotNull final Item item, int itemDamage)
    {
        return getFirstFillablePositionInItemHandler(itemHandler, new ItemStack(item, 1, itemDamage));
    }

    /**
     * Returns a slot number if a {@link IItemHandler} contains given ItemStack item that is not fully stack and with which the given stack can be merged.
     *
     * @param itemHandler the {@link IItemHandler} to check.
     * @param stack The stack for which a fillable possition needs to be found.
     * @return slot number if found, -1 when not found.
     */
    public static int getFirstFillablePositionInItemHandler(@NotNull final IItemHandler itemHandler, @NotNull final ItemStack stack)
    {
        return getFirstFillablePositionInItemHandler(itemHandler, (ItemStack existingStack) -> areItemStacksMergable(existingStack, stack));
    }

    /**
     * Returns a slot number if a {@link IItemHandler} contains given ItemStack item that is not fully stacked.
     *
     * @param itemHandler the {@link IItemHandler} to check.
     * @param itemStackMergingPredicate Predicate used to test if a given stack should be merged.
     * @return slot number if found, -1 when not found.
     */
    public static int getFirstFillablePositionInItemHandler(@NotNull final IItemHandler itemHandler, @NotNull final Predicate<ItemStack> itemStackMergingPredicate)
    {
        return IntStream.range(0, itemHandler.getSlots()).filter(slot ->  {
            final ItemStack testStack = itemHandler.getStackInSlot(slot);
            return isItemStackEmpty(testStack) || (!isItemStackEmpty(testStack) && itemStackMergingPredicate.test(testStack));
        }
        ).findFirst().orElse(-1);
    }

    /*
    ###################################################################END: IItemHandler Interaction###################################################################
     */

    /*
    ##############################################################START: ICapabilityProvider Interaction###############################################################
    ICapabilityProvider region of the methods.
    Handles all the interaction with the ICapabilityProviders directly.
    Does not care about sides and works like the IInventory methods before Capabilities were introduced.
    ##############################################################START: ICapabilityProvider Interaction###############################################################
     */
    
    /**
     * Returns an {@link ICapabilityProvider} as list of item stacks.
     *
     * @param provider provider to convert.
     * @return List of item stacks.
     */
    @NotNull
    public static List<ItemStack> getInventoryAsListFromProvider(@NotNull final ICapabilityProvider provider)
    {
        return filterProvider(provider, (ItemStack stack) -> true);
    }
    
    /**
     * Filters a list of items, matches the stack using {@link #compareItems(ItemStack, Item, int)}, in an {@link ICapabilityProvider}.
     * Uses the MetaData and {@link #getItemFromBlock(Block)} as parameters for the Predicate.
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
     * Filters a list of items, matches the stack using {@link #compareItems(ItemStack, Item, int)}, with targetItem and itemDamage as parameters, in an {@link
     * ICapabilityProvider}.
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
     * Filters a list of items, that match the given predicate, in an {@link ICapabilityProvider}.
     *
     * @param provider                    The ICapabilityProvider to get items from.
     * @param itemStackSelectionPredicate The predicate to match the stack to.
     * @return List of item stacks that match the given predicate.
     */
    @NotNull
    public static List<ItemStack> filterProvider(@NotNull final ICapabilityProvider provider, @NotNull Predicate<ItemStack> itemStackSelectionPredicate)
    {
        return getFromProviderForAllSides(provider, itemStackSelectionPredicate);
    }

    /**
     * Returns the index of the first occurrence of the block in the {@link ICapabilityProvider}.
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
     * Returns the index of the first occurrence of the Item with the given ItemDamage in the {@link ICapabilityProvider}.
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
     * Returns the index of the first occurrence of an ItemStack that matches the given predicate in the {@link ICapabilityProvider}.
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
     * Returns the amount of occurrences in the {@link ICapabilityProvider}.
     *
     * @param provider  {@link ICapabilityProvider} to scan.
     * @param block      The block to count
     * @param itemDamage the damage value
     * @return Amount of occurrences of stacks that match the given block and ItemDamage
     */
    public static int getItemCountInProvider(@NotNull final ICapabilityProvider provider, @NotNull final Block block, int itemDamage)
    {
        return getItemCountInProvider(provider, getItemFromBlock(block), itemDamage);
    }

    /**
     * Returns the amount of occurrences in the {@link ICapabilityProvider}.
     *
     * @param provider  {@link ICapabilityProvider} to scan.
     * @param targetItem Item to count
     * @param itemDamage the item damage value.
     * @return Amount of occurrences of stacks that match the given item and ItemDamage
     */
    public static int getItemCountInProvider(@NotNull final ICapabilityProvider provider, @NotNull final Item targetItem, int itemDamage)
    {
        return getItemCountInProvider(provider, (ItemStack stack) -> compareItems(stack, targetItem, itemDamage));
    }


    /**
     * Returns the amount of occurrences in the {@link ICapabilityProvider}.
     *
     * @param provider  {@link ICapabilityProvider} to scan.
     * @param itemStackSelectionPredicate The predicate used to select the stacks to count.
     * @return Amount of occurrences of stacks that match the given predicate.
     */
    public static int getItemCountInProvider(@NotNull final ICapabilityProvider provider, @NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        return getItemHandlersFromProvider(provider).stream()
                    .mapToInt(handler -> filterItemHandler(handler, itemStackSelectionPredicate).stream().mapToInt(stack -> stack.stackSize).sum())
                    .sum();
    }


    /**
     * Checks if a player has a block in the {@link ICapabilityProvider}.
     * Checked by {@link #getItemCountInProvider(ICapabilityProvider, Block, int)} &gt; 0;
     *
     * @param Provider  {@link ICapabilityProvider} to scan
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
     * Checked by {@link #getItemCountInProvider(ICapabilityProvider, Item, int)} &gt; 0;
     *
     * @param Provider  {@link ICapabilityProvider} to scan
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
     * Checked by {@link #getItemCountInProvider(ICapabilityProvider, Predicate<ItemStack>)} &gt; 0;
     *
     * @param Provider  {@link ICapabilityProvider} to scan
     * @param itemStackSelectionPredicate The predicate to match the ItemStack to.                    
     * @return True when in {@link ICapabilityProvider}, otherwise false
     */
    public static boolean hasItemInProvider(@NotNull final ICapabilityProvider Provider, @NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        return getItemCountInProvider(Provider, itemStackSelectionPredicate) > 0;
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
     * Checks if the {@link ICapabilityProvider} contains the following toolName with the given minimal Level.
     *
     * @param provider    The {@link ICapabilityProvider} to scan.
     * @param toolTypeName   The toolTypeName of the tool to find.
     * @param minimalLevel   The minimal level to find.
     * @return True if a Tool with the given toolTypeName was found in the given {@link ICapabilityProvider}, false when not.
     */
    public static boolean isToolInProvider(@NotNull final ICapabilityProvider provider, @NotNull final String toolTypeName, int minimalLevel)
    {
        return hasItemInProvider(provider, (ItemStack stack) -> Utils.isTool(stack, toolTypeName) && InventoryUtils.hasToolLevel(stack, toolTypeName, minimalLevel));
    }

    /**
     * Looks for a {@link ItemPickaxe} to mine a block of {@code requiredLevel}, in the given {@link ICapabilityProvider}.
     *
     * @param provider   {@link ICapabilityProvider} to check in.
     * @param requiredLevel The minimal required {@link ItemPickaxe} level
     * @return True if the {@link ICapabilityProvider} contains a {@link ItemPickaxe} with the given minimal required level.
     */
    public static boolean isPickaxeInProvider(ICapabilityProvider provider, final int requiredLevel)
    {
        return hasItemInProvider(provider, (ItemStack stack) -> Utils.checkIfPickaxeQualifies(requiredLevel, Utils.getMiningLevel(stack, Utils.PICKAXE)));
    }

    /**
     * Looks for a {@link ItemPickaxe} to mine a block of {@code requiredLevel}, in the given {@link ICapabilityProvider}.
     * The {@link ItemPickaxe} tool level cannot exceed the given {@code maximalLevel}.
     *
     * @param provider   {@link ICapabilityProvider} to check in.
     * @param requiredLevel The minimal required {@link ItemPickaxe} level
     * @param maximalLevel The maximal tool level of the {@link ItemPickaxe}
     * @return True if the {@link ICapabilityProvider} contains a {@link ItemPickaxe} with the given minimal required level.
     */
    public static boolean isPickaxeInProvider(ICapabilityProvider provider, final int requiredLevel, final int maximalLevel)
    {
        return hasItemInProvider(provider, (ItemStack stack) -> Utils.checkIfPickaxeQualifies(requiredLevel, Utils.getMiningLevel(stack, Utils.PICKAXE))
                                                                        && InventoryUtils.hasToolLevel(stack, Utils.PICKAXE, maximalLevel));
    }

    /**
     * Returns a slot number if a {@link ICapabilityProvider} contains given ItemStack item that is not fully stacked and with which a stack made of the given block and itemDamage 
     * can be merged.
     *
     * @param provider the {@link ICapabilityProvider} to check.
     * @param block The block to test against.
     * @param itemDamage The item damage of a stack with that block to test against.
     * @return slot number if found, -1 when not found.
     */
    public static int getFirstFillablePositionInProvider(@NotNull final ICapabilityProvider provider, @NotNull final Block block, int itemDamage)
    {
        return getFirstFillablePositionInProvider(provider, getItemFromBlock(block), itemDamage);
    }

    /**
     * Returns a slot number if a {@link ICapabilityProvider} contains given ItemStack item that is not fully stacke and with which a stack made of the given item and itemDamage 
     * can be merged.
     *
     * @param provider the {@link ICapabilityProvider} to check.
     * @param item The item to test against.
     * @param itemDamage The item damage of a stack with that block to test against.
     * @return slot number if found, -1 when not found.
     */
    public static int getFirstFillablePositionInProvider(@NotNull final ICapabilityProvider provider, @NotNull final Item item, int itemDamage)
    {
        return getFirstFillablePositionInProvider(provider, new ItemStack(item, 1, itemDamage));
    }

    /**
     * Returns a slot number if a {@link ICapabilityProvider} contains given ItemStack item that is not fully stack and with which the given stack can be merged.
     *
     * @param provider the {@link ICapabilityProvider} to check.
     * @param stack The stack for which a fillable possition needs to be found.
     * @return slot number if found, -1 when not found.
     */
    public static int getFirstFillablePositionInProvider(@NotNull final ICapabilityProvider provider, @NotNull final ItemStack stack)
    {
        return getFirstFillablePositionInProvider(provider, (ItemStack existingStack) -> areItemStacksMergable(existingStack, stack));
    }

    /**
     * Returns a slot number if a {@link ICapabilityProvider} contains given ItemStack item that is not fully stacked.
     *
     * @param provider the {@link ICapabilityProvider} to check.
     * @param itemStackMergingPredicate Predicate used to test if a given stack should be merged.
     * @return slot number if found, -1 when not found.
     */
    public static int getFirstFillablePositionInProvider(@NotNull final ICapabilityProvider provider, @NotNull final Predicate<ItemStack> itemStackMergingPredicate)
    {
        return getItemHandlersFromProvider(provider).stream()
                 .mapToInt(handler -> getFirstFillablePositionInItemHandler(handler, itemStackMergingPredicate))
                 .filter(slotIndex -> slotIndex > -1)
                 .findFirst()
                 .orElse(-1);
    }




    /**
     * Method to process the given predicate for all {@link EnumFacing} of a {@link ICapabilityProvider}, including the internal one (passing null as argument).
     *
     * @param provider  The provider to process all the
     * @param predicate The predicate to match the ItemStacks in the {@link IItemHandler} for each side with.
     * @return A combined {@link List<ItemStack>} as if the given predicate was called on all ItemStacks in all IItemHandlers of the given provider.
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
     * Method used to check if a {@link ICapabilityProvider} has any {@link IItemHandler}
     *
     * @param provider The provider to check.
     * @return True when the provider has any {@link IItemHandler}, false when not.
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
     * @return True when the provider has multiple distinct IItemHandler of different sides (sidedness {@link TileEntityFurnace#hasCapability(Capability, EnumFacing)}), false when
     * not {@link TileEntityChest#hasCapability(Capability, EnumFacing)}
     */
    @NotNull
    public static boolean isProviderSided(@NotNull ICapabilityProvider provider)
    {
        return getItemHandlersFromProvider(provider).size() == 1;
    }


    /*
    ###############################################################END: ICapabilityProvider Interaction################################################################
     */
    
    /*
    ###########################################################START: ICapabilityProvider (Sided) Interaction##########################################################
    ICapabilityProvider region (sided specific) of the methods.
    Handles all the interaction with specific sides of an ICapabilityProviders directly.
    ###########################################################START: ICapabilityProvider (Sided) Interaction##########################################################
     */

    /**
     * Returns an {@link IItemHandler} as list of item stacks.
     *
     * @param provider The {@link ICapabilityProvider} that holds the {@link IItemHandler} for the given {@link EnumFacing}
     * @param facing   The facing to get the {@link IItemHandler} from. Can be null for the internal one {@link ICapabilityProvider#hasCapability(Capability, EnumFacing)}
     * @return List of item stacks.
     */
    @NotNull
    public static List<ItemStack> getInventoryAsListFromProviderForSide(@NotNull final ICapabilityProvider provider, @Nullable EnumFacing facing)
    {
        return filterItemHandler(provider.getCapability(ITEM_HANDLER_CAPABILITY, facing), (ItemStack stack) -> true);
    }

    /**
     * Filters a list of items, matches the stack using {@link #compareItems(ItemStack, Item, int)}, in an {@link IItemHandler}.
     * Uses the MetaData and {@link #getItemFromBlock(Block)} as parameters for the Predicate.
     *
     * @param provider The {@link ICapabilityProvider} that holds the {@link IItemHandler} for the given {@link EnumFacing}
     * @param facing   The facing to get the {@link IItemHandler} from. Can be null for the internal one {@link ICapabilityProvider#hasCapability(Capability, EnumFacing)}
     * @param block    Block to filter
     * @param metaData the damage value.
     * @return List of item stacks
     */
    @NotNull
    public static List<ItemStack> filterItemHandlerFromProviderForSide(@NotNull final ICapabilityProvider provider, @Nullable EnumFacing facing, @NotNull final Block block, int metaData)
    {
        return filterItemHandler(provider.getCapability(ITEM_HANDLER_CAPABILITY, facing), (ItemStack stack) -> compareItems(stack, getItemFromBlock(block), metaData));
    }

    /**
     * Filters a list of items, matches the stack using {@link #compareItems(ItemStack, Item, int)}, with targetItem and itemDamage as parameters, in an {@link IItemHandler}.
     *
     * @param provider   The {@link ICapabilityProvider} that holds the {@link IItemHandler} for the given {@link EnumFacing}
     * @param facing     The facing to get the {@link IItemHandler} from. Can be null for the internal one {@link ICapabilityProvider#hasCapability(Capability, EnumFacing)}
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
     * Filters a list of items, that match the given predicate, in an {@link IItemHandler}.
     *
     * @param provider                    The {@link ICapabilityProvider} that holds the {@link IItemHandler} for the given {@link EnumFacing}
     * @param facing                      The facing to get the {@link IItemHandler} from. Can be null for the internal one {@link ICapabilityProvider#hasCapability(Capability,
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
            return Collections.EMPTY_LIST;
        }

        return filterItemHandler(provider.getCapability(ITEM_HANDLER_CAPABILITY, facing), itemStackSelectionPredicate);
    }



    /**
     * Returns the index of the first occurrence of the block in the {@link ICapabilityProvider} for a given {@link EnumFacing}.
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
     * Returns the index of the first occurrence of the Item with the given ItemDamage in the {@link ICapabilityProvider} for a given {@link EnumFacing}.
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
     * Returns the index of the first occurrence of an ItemStack that matches the given predicate in the {@link ICapabilityProvider} for a given {@link EnumFacing}.
     *
     * @param provider                    Provider to check
     * @param facing                      The facing to check for.
     * @param itemStackSelectionPredicate The predicate to match.
     * @return Index of the first occurrence
     */
    public static int findFirstSlotInProviderForSideWith(@NotNull final ICapabilityProvider provider, @Nullable EnumFacing facing, @NotNull final  Predicate<ItemStack> itemStackSelectionPredicate)
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
     * Returns the amount of occurrences in the {@link ICapabilityProvider} for a given {@link EnumFacing}.
     *
     * @param provider  {@link ICapabilityProvider} to scan.
     * @param facing The facing to count in.
     * @param block      The block to count
     * @param itemDamage the damage value
     * @return Amount of occurrences of stacks that match the given block and ItemDamage
     */
    public static int getItemCountInProviderForSide(@NotNull final ICapabilityProvider provider, @Nullable EnumFacing facing, @NotNull final Block block, int itemDamage)
    {
        return getItemCountInProviderForSide(provider, facing, getItemFromBlock(block), itemDamage);
    }

    /**
     * Returns the amount of occurrences in the {@link ICapabilityProvider} for a given {@link EnumFacing}.
     *
     * @param provider  {@link ICapabilityProvider} to scan.
     * @param facing The facing to count in.
     * @param targetItem Item to count
     * @param itemDamage the item damage value.
     * @return Amount of occurrences of stacks that match the given item and ItemDamage
     */
    public static int getItemCountInProviderForSide(@NotNull final ICapabilityProvider provider, @Nullable EnumFacing facing, @NotNull final Item targetItem, int itemDamage)
    {
        return getItemCountInProviderForSide(provider, facing, (ItemStack stack) -> compareItems(stack, targetItem, itemDamage));
    }


    /**
     * Returns the amount of occurrences in the {@link ICapabilityProvider} for a given {@link EnumFacing}.
     *
     * @param provider  {@link ICapabilityProvider} to scan.
     * @param facing The facing to count in.                                             
     * @param itemStackSelectionPredicate The predicate used to select the stacks to count.
     * @return Amount of occurrences of stacks that match the given predicate.
     */
    public static int getItemCountInProviderForSide(@NotNull final ICapabilityProvider provider, @Nullable EnumFacing facing, @NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        if (!provider.hasCapability(ITEM_HANDLER_CAPABILITY, facing)) {
            return 0;
        }
        
        return filterItemHandler(provider.getCapability(ITEM_HANDLER_CAPABILITY, facing), itemStackSelectionPredicate).stream().mapToInt(stack -> stack.stackSize).sum();
    }

    /**
     * Checks if a player has a block in the {@link ICapabilityProvider}, for a given {@link EnumFacing}.
     * Checked by {@link #getItemCountInProvider(ICapabilityProvider, Block, int)} &gt; 0;
     *
     * @param provider  {@link ICapabilityProvider} to scan
     * @param facing The side to check for.                                            
     * @param block      Block to count
     * @param itemDamage the damage value.
     * @return True when in {@link ICapabilityProvider}, otherwise false
     */
    public static boolean hasItemInProviderForSide(@NotNull final ICapabilityProvider provider, @Nullable EnumFacing facing, @NotNull final Block block, int itemDamage)
    {
        return hasItemInProviderForSide(provider, facing, getItemFromBlock(block), itemDamage);
    }

    /**
     * Checks if a player has an item in the {@link ICapabilityProvider}, for a given {@link EnumFacing}.
     * Checked by {@link #getItemCountInProvider(ICapabilityProvider, Item, int)} &gt; 0;
     *
     * @param provider  {@link ICapabilityProvider} to scan
     * @param facing The side to check for.
     * @param item       Item to count
     * @param itemDamage the damage value of the item.
     * @return True when in {@link ICapabilityProvider}, otherwise false
     */
    public static boolean hasItemInProviderForSide(@NotNull final ICapabilityProvider provider, @Nullable EnumFacing facing, @NotNull final Item item, int itemDamage)
    {
        return hasItemInProviderForSide(provider, facing, (ItemStack stack) -> compareItems(stack, item, itemDamage));
    }

    /**
     * Checks if a player has an item in the {@link ICapabilityProvider}, for a given {@link EnumFacing}.
     * Checked by {@link #getItemCountInProvider(ICapabilityProvider, Predicate<ItemStack>)} &gt; 0;
     *
     * @param provider  {@link ICapabilityProvider} to scan
     * @param facing The side to check for.
     * @param itemStackSelectionPredicate The predicate to match the ItemStack to.                    
     * @return True when in {@link ICapabilityProvider}, otherwise false
     */
    public static boolean hasItemInProviderForSide(@NotNull final ICapabilityProvider provider, @Nullable EnumFacing facing, @NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        if (!provider.hasCapability(ITEM_HANDLER_CAPABILITY, facing)) {
            return false;
        }
            
        return getItemCountInItemHandler(provider.getCapability(ITEM_HANDLER_CAPABILITY, facing), itemStackSelectionPredicate) > 0;
    }

    /**
     * Returns the first open slot in the {@link ICapabilityProvider}, for a given {@link EnumFacing}.
     *
     * @param provider The {@link ICapabilityProvider} to check.
     * @param facing The side to check for.
     * @return slot index or -1 if none found.
     */
    public static int getFirstOpenSlotFromProviderForSide(@NotNull final ICapabilityProvider provider, @Nullable EnumFacing facing)
    {
        if (!provider.hasCapability(ITEM_HANDLER_CAPABILITY, facing)) {
            return -1;
        }

        return getFirstOpenSlotFromItemHandler(provider.getCapability(ITEM_HANDLER_CAPABILITY, facing));
    }

    /**
     * Returns if the {@link ICapabilityProvider} is full, for a given {@link EnumFacing}.
     *
     * @param provider The {@link ICapabilityProvider}.
     * @param facing The side to check for.
     * @return True if the {@link ICapabilityProvider} is full, false when not.
     */
    public static boolean isProviderFull(@NotNull final ICapabilityProvider provider, @Nullable EnumFacing facing)
    {
        return getFirstOpenSlotFromProviderForSide(provider, facing) == -1;
    }

    /**
     * Checks if the {@link ICapabilityProvider} contains the following toolName with the given minimal Level, for a given {@link EnumFacing}.
     *
     * @param provider    The {@link ICapabilityProvider} to scan.
     * @param facing The side to check for.
     * @param toolTypeName   The toolTypeName of the tool to find.
     * @param minimalLevel   The minimal level to find.
     * @return True if a Tool with the given toolTypeName was found in the given {@link ICapabilityProvider}, false when not.
     */
    public static boolean isToolInProviderForSide(@NotNull final ICapabilityProvider provider, @Nullable EnumFacing facing, @NotNull final String toolTypeName, int minimalLevel)
    {
        if (!provider.hasCapability(ITEM_HANDLER_CAPABILITY, facing)) {
            return false;
        }

        return isToolInItemHandler(provider.getCapability(ITEM_HANDLER_CAPABILITY, facing), toolTypeName, minimalLevel);
    }

    /**
     * Returns a slot number if a {@link ICapabilityProvider} contains given ItemStack item that is not fully stacked and with which a stack made of the given block and itemDamage can be
     * merged.
     *
     * @param provider the {@link ICapabilityProvider} to check.
     * @param block The block to test against.
     * @param itemDamage The item damage of a stack with that block to test against.
     * @return slot number if found, -1 when not found.
     */
    public static int getFirstFillablePositionInProviderForSide(@NotNull final ICapabilityProvider provider, @Nullable EnumFacing facing, @NotNull final Block block, int itemDamage)
    {
        return getFirstFillablePositionInProviderForSide(provider, facing, getItemFromBlock(block), itemDamage);
    }

    /**
     * Returns a slot number if a {@link ICapabilityProvider} contains given ItemStack item that is not fully stacke and with which a stack made of the given item and itemDamage can be
     * merged.
     *
     * @param provider the {@link ICapabilityProvider} to check.
     * @param item The item to test against.
     * @param itemDamage The item damage of a stack with that block to test against.
     * @return slot number if found, -1 when not found.
     */
    public static int getFirstFillablePositionInProviderForSide(@NotNull final ICapabilityProvider provider, @Nullable EnumFacing facing, @NotNull final Item item, int itemDamage)
    {
        return getFirstFillablePositionInProviderForSide(provider, facing, new ItemStack(item, 1, itemDamage));
    }

    /**
     * Returns a slot number if a {@link ICapabilityProvider} contains given ItemStack item that is not fully stack and with which the given stack can be merged.
     *
     * @param provider the {@link ICapabilityProvider} to check.
     * @param stack The stack for which a fillable possition needs to be found.
     * @return slot number if found, -1 when not found.
     */
    public static int getFirstFillablePositionInProviderForSide(@NotNull final ICapabilityProvider provider, @Nullable EnumFacing facing, @NotNull final ItemStack stack)
    {
        return getFirstFillablePositionInProviderForSide(provider, facing, (ItemStack existingStack) -> areItemStacksMergable(existingStack, stack));
    }

    /**
     * Returns a slot number if a {@link ICapabilityProvider} contains given ItemStack item that is not fully stacked.
     *
     * @param provider the {@link ICapabilityProvider} to check.
     * @param itemStackMergingPredicate Predicate used to test if a given stack should be merged.
     * @return slot number if found, -1 when not found.
     */
    public static int getFirstFillablePositionInProviderForSide(@NotNull final ICapabilityProvider provider, @Nullable EnumFacing facing, @NotNull final Predicate<ItemStack> itemStackMergingPredicate)
    {
        if (!provider.hasCapability(ITEM_HANDLER_CAPABILITY, facing)) {
            return -1;
        }

        return getFirstFillablePositionInItemHandler(provider.getCapability(ITEM_HANDLER_CAPABILITY, facing), itemStackMergingPredicate);
    }

    /*
    ############################################################END: ICapabilityProvider (Sided) Interaction###########################################################
     */

    /**
     * Compares whether or not the item in an itemstack is equal to a given item.
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
     * Verifies if there is one tool with an acceptable level
     * in a worker's inventory.
     *
     * @param toolName     the type of tool needed
     * @param stack    the stack to test.
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
        if (isItemStackEmpty(itemStack) || minimalLevel > FREE_TOOL_CHOICE_LEVEL)
        {
            return true;
        }
        else if (itemStack.isItemEnchanted() && minimalLevel <= EFFECT_TOOL_CHOICE_LEVEL)
        {
            return false;
        }
        else if (minimalLevel >= toolLevel)
        {
            return true;
        }

        return false;
    }


    /**
     * Transfers a single Item (An ItemStack with stacksize 1) from the given sender to the given receiver.
     * Swapping the ItemStacks if their is already a stack in the receiver.
     *
     * @param sendingHandler   {@link IItemHandler} of sender
     * @param receivingHandler {@link IItemHandler} of receiver
     * @param slotIndex       Slot ID to take from
     * @return True if item is swapped, otherwise false
     */
    public static boolean transferSingleItemFromItemHandlerToItemHandler(@NotNull final IItemHandler sendingHandler, @NotNull final IItemHandler receivingHandler, final int slotIndex)
    {
        return takeStackInSlot(sendingHandler, receivingHandler, slotIndex, 1, true);
    }

    /**
     * Transfers an ItemStack from the given sender to the given receiver.
     * If <code>takeAll</code> is true, the entire slot will we transferred.
     * This only applied when at least <code>amount</code> can be taken.
     *
     * @param sendingHandler   {@link IItemHandler} of sender
     * @param receivingHandler {@link IItemHandler} of receiver
     * @param slotIndex       Slot ID to take from
     * @param amount       Amount to swap
     * @param takeAll      Whether or not the entire stack of the sender should be emptied if possible
     *                     Only applies when <code>amount</code> is sufficient
     * @return True if item is swapped, otherwise false
     */
    public static boolean transferStackFromItemHandlerToItemHandler(
                                           @NotNull final IItemHandler sendingHandler, @NotNull final IItemHandler receivingHandler,
                                           final int slotIndex, final int amount, final boolean takeAll)
    {
        if (slotIndex >= 0 && amount > 0)
        {
            // gets itemstack in slot, and decreases stacksize
            @Nullable ItemStack stack = sendingHandler.extractItem(slotIndex, amount, false);
            // stack is null if no itemstack was in slot
            if (!isItemStackEmpty(stack))
            {
                // puts stack in receiving inventory
                stack = setStack(receivingInv, stack);
                // checks for leftovers
                if (stack == null)
                {
                    if (takeAll)
                    {
                        // gets itemstack in slot
                        stack = sendingInv.getStackInSlot(slotIndex);
                        // checks if itemstack is still in slot
                        if (stack != null)
                        {
                            stack = sendingInv.decrStackSize(slotIndex, stack.stackSize);
                            stack = setStack(receivingInv, stack);
                            setStack(sendingInv, stack);
                        }
                    }

                    // puts leftovers back in sending inventory
                    return true;
                }
                setStack(sendingInv, stack);
                return false;
            }
        }
        return false;
    }

    /**
     * Tries to put an item into an {@link IItemHandler}.
     *
     * @param itemHandler The {@link IItemHandler} to set the stack in.
     * @param stack     Item stack with items to be transferred
     * @return returns null if successful, or stack of remaining items
     */
    @Nullable
    public static ItemStack transferStackToItemHandler(@NotNull final IItemHandler itemHandler, @Nullable ItemStack stack)
    {
        if (!isItemStackEmpty(stack) && stack.stackSize > stack.getMaxStackSize())
        {
            Log.getLogger().warn("InventoryUtils.setStack: stack size bigger than the max stack size. Please contact a minecolonnies developer.");
        }

        if (!isItemStackEmpty(stack))
        {
            int slot;
            while ((slot = getFirstFillablePositionInItemHandler(itemHandler, stack)) != -1 && stack != null)
            {
                stack = itemHandler.insertItem(slot, stack, false);
            }

            while ((slot = getFirstOpenSlotFromItemHandler(itemHandler)) != -1 && stack != null)
            {
                stack = itemHandler.insertItem(slot, stack, false);
            }
            return stack;
        }
        return null;
    }



    /**
     * {@link #setStack(IInventory, ItemStack)}.
     * Tries to put an itemStack into Inventory, unlike setStack, allow to use a ItemStack bigger than the maximum stack size allowed for the item
     *
     * @param inventory the inventory to set the stack in.
     * @param stack     Item stack with items to be transferred, the stack can be bigger than allowed
     * @return returns null if successful, or stack of remaining items, BE AWARE that the remaining stack can be bigger than the maximum stack size
     */
    @Nullable
    public static ItemStack setOverSizedStack(@NotNull final IInventory inventory, @Nullable final ItemStack stack)
    {
        int stackSize = stack.stackSize;
        while (stackSize > 0)
        {
            final int itemCount = Math.min(stackSize, stack.getMaxStackSize());
            final ItemStack items = new ItemStack(stack.getItem(), itemCount, stack.getItemDamage());
            stackSize -= itemCount;
            final ItemStack remainingItems = setStack(inventory, items);
            if (remainingItems != null)
            {
                stackSize += remainingItems.stackSize;
                if (items.stackSize == remainingItems.stackSize)
                {
                    break;
                }
            }
        }
        return new ItemStack(stack.getItem(), stackSize, stack.getItemDamage());
    }

    /**
     * {@link #takeStackInSlot(IInventory, IInventory, int, int, boolean)}.
     * Default:
     * takeAll: false
     *
     * @param sendingInv   Inventory of sender
     * @param receivingInv Inventory of receiver
     * @param slotID       Slot ID to take from
     * @param amount       Amount to swap
     * @return True if item is swapped, otherwise false
     */
    public static boolean takeStackInSlot(final IInventory sendingInv, final IInventory receivingInv, final int slotID, final int amount)
    {
        return takeStackInSlot(sendingInv, receivingInv, slotID, amount, false);
    }

    /**
     * Returns all <code>ItemStack</code>s in an inventory.
     * Stores this in an array.
     *
     * @param inventory Inventory to return all item stacks from.
     * @return Array of item stacks.
     */
    @NotNull
    public static ItemStack[] getAllItemStacks(@NotNull final IInventory inventory)
    {
        @NotNull final ItemStack[] itemStack = new ItemStack[inventory.getSizeInventory()];
        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            itemStack[i] = inventory.getStackInSlot(i);
        }
        return itemStack;
    }

    /**
     * Returns the amount of item stacks in an inventory.
     * This equals {@link #getAllItemStacks(IInventory)}<code>.length();</code>.
     *
     * @param inventory Inventory to count item stacks of.
     * @return Amount of item stacks in inventory.
     */
    public static int getAmountOfStacks(@NotNull final IInventory inventory)
    {
        int count = 0;
        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            final ItemStack is = inventory.getStackInSlot(i);

            if (is != null)
            {
                count++;
            }
        }
        return count;
    }

    /**
     * Clears an entire inventory.
     *
     * @param inventory Inventory to clear.
     */
    public static void clear(@NotNull final IInventory inventory)
    {
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++)
        {
            inventory.setInventorySlotContents(slot, null);
        }
    }

    /**
     * Returns a slot number if an inventory contains given tool type.
     *
     * @param inventory the inventory to get the slot from.
     * @param tool      the tool type to look for.
     * @return slot number if found, -1 if not found.
     */
    public static int getFirstSlotContainingTool(@NotNull final IInventory inventory, @NotNull final String tool)
    {
        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            final ItemStack item = inventory.getStackInSlot(i);
            //Only classic fishingRod recognized as a fishingTool
            if (item != null && (item.getItem().getToolClasses(item).contains(tool) || ("hoe".equals(tool) && item.getUnlocalizedName().contains("hoe"))
                                   || ("rod".equals(tool) && item.getUnlocalizedName().contains("fishingRod"))))
            {
                return i;
            }
        }
        return -1;
    }

    /**
     * Verifies if there is one tool with an acceptable level
     * in a worker's inventory.
     *
     * @param tool      the type of tool needed
     * @param inventory the worker's inventory
     * @param hutLevel  the worker's hut level
     * @return true if tool is acceptable
     */
    public static boolean hasToolLevel(final String tool, @NotNull final IInventory inventory, final int hutLevel)
    {
        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            final ItemStack item = inventory.getStackInSlot(i);
            final int level = Utils.getMiningLevel(item, tool);

            if (Utils.isTool(item, tool) && verifyToolLevel(item, level, hutLevel))
            {
                return true;
            }
        }
        return false;
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
     * Adapted from {@link net.minecraft.entity.player.InventoryPlayer#addItemStackToInventory(ItemStack)}.
     *
     * @param inventory Inventory to add itemstack to.
     * @param itemStack ItemStack to add.
     * @param building  the building.
     * @return itemStack which has been replaced.
     */
    @Nullable
    public static ItemStack forceItemStackToInventory(@NotNull final IInventory inventory, @NotNull final ItemStack itemStack, @NotNull final AbstractBuilding building)
    {
        if (!addItemStackToInventory(inventory, itemStack))
        {
            final List<ItemStorage> localAlreadyKept = new ArrayList<>();
            for (int i = 0; i < inventory.getSizeInventory(); i++)
            {
                final ItemStack localStack = inventory.getStackInSlot(i);
                if (!EntityAIWorkDeliveryman.workerRequiresItem(building, localStack, localAlreadyKept))
                {
                    final ItemStack removedStack = inventory.removeStackFromSlot(i);
                    inventory.setInventorySlotContents(i, itemStack.copy());
                    return removedStack.copy();
                }
            }
        }
        return null;
    }

    /**
     * Adapted from {@link net.minecraft.entity.player.InventoryPlayer#addItemStackToInventory(ItemStack)}.
     *
     * @param inventory Inventory to add itemstack to.
     * @param itemStack ItemStack to add.
     * @return True if successful, otherwise false.
     */
    public static boolean addItemStackToInventory(@NotNull final IInventory inventory, @Nullable final ItemStack itemStack)
    {
        if (itemStack != null && itemStack.stackSize != 0 && itemStack.getItem() != null)
        {
            int stackSize;

            if (itemStack.isItemDamaged())
            {
                stackSize = getOpenSlot(inventory);

                if (stackSize >= 0)
                {
                    final ItemStack copy = ItemStack.copyItemStack(itemStack);
                    copy.animationsToGo = 5;
                    inventory.setInventorySlotContents(stackSize, copy);

                    itemStack.stackSize = 0;
                    return true;
                }
                else
                {
                    return false;
                }
            }
            else
            {
                do
                {
                    stackSize = itemStack.stackSize;
                    itemStack.stackSize = storePartialItemStack(inventory, itemStack);
                }
                while (itemStack.stackSize > 0 && itemStack.stackSize < stackSize);


                return itemStack.stackSize < stackSize;
            }
        }
        else
        {
            return false;
        }
    }

    /**
     * Adapted from {@link net.minecraft.entity.player.InventoryPlayer#storePartialItemStack(ItemStack)}.
     * <p>
     * This function stores as many items of an ItemStack as possible in a matching slot and returns the quantity of
     * left over items.
     *
     * @param inventory Inventory to add stack to.
     * @param itemStack Item stack to store in inventory.
     * @return Leftover items in itemstack.
     */
    private static int storePartialItemStack(@NotNull final IInventory inventory, @NotNull final ItemStack itemStack)
    {
        final Item item = itemStack.getItem();
        int stackSize = itemStack.stackSize;
        int slot;

        if (itemStack.getMaxStackSize() == 1)
        {
            slot = getOpenSlot(inventory);

            if (slot < 0)
            {
                return stackSize;
            }
            else
            {
                if (inventory.getStackInSlot(slot) == null)
                {
                    inventory.setInventorySlotContents(slot, ItemStack.copyItemStack(itemStack));
                }

                return 0;
            }
        }
        else
        {
            slot = findSlotForItemStack(inventory, itemStack);

            if (slot < 0)
            {
                slot = getOpenSlot(inventory);
            }

            if (slot < 0)
            {
                return stackSize;
            }
            else
            {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (stack == null)
                {
                    stack = new ItemStack(item, 0, itemStack.getItemDamage());

                    if (itemStack.hasTagCompound())
                    {
                        stack.setTagCompound(itemStack.getTagCompound().copy());
                    }
                }

                int inventoryStackSpace = stackSize;

                if (stackSize > stack.getMaxStackSize() - stack.stackSize)
                {
                    inventoryStackSpace = stack.getMaxStackSize() - stack.stackSize;
                }

                if (inventoryStackSpace > inventory.getInventoryStackLimit() - stack.stackSize)
                {
                    inventoryStackSpace = inventory.getInventoryStackLimit() - stack.stackSize;
                }

                if (inventoryStackSpace == 0)
                {
                    return stackSize;
                }
                else
                {
                    stackSize -= inventoryStackSpace;
                    stack.stackSize += inventoryStackSpace;
                    stack.animationsToGo = 5;
                    inventory.setInventorySlotContents(slot, stack);
                    return stackSize;
                }
            }
        }
    }

    /**
     * Adapted from {@link net.minecraft.entity.player.InventoryPlayer#storeItemStack(ItemStack)}.
     * <p>
     * find a slot to store an ItemStack in.
     *
     * @param inventory Inventory to look in.
     * @param itemStack Item Stack to look for.
     * @return Index of the item stack. If not found, returns -1.
     */
    private static int findSlotForItemStack(@NotNull final IInventory inventory, @NotNull final ItemStack itemStack)
    {
        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            final ItemStack inventoryItem = inventory.getStackInSlot(i);
            if (inventoryItem != null
                  && inventoryItem.getItem() == itemStack.getItem()
                  && inventoryItem.isStackable()
                  && inventoryItem.stackSize < inventoryItem.getMaxStackSize()
                  && inventoryItem.stackSize < inventory.getInventoryStackLimit()
                  && (!inventoryItem.getHasSubtypes() || inventoryItem.getItemDamage() == itemStack.getItemDamage())
                  && ItemStack.areItemStackTagsEqual(inventoryItem, itemStack))
            {
                return i;
            }
        }

        return -1;
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
        return !(stack != null && stack.getItem() != null && stack.stackSize > 0);
    }

    /**
     * Method to compare to stacks, ignoring their stacksize.
     * @param itemStack1 The left stack to compare.
     * @param itemStack2 The right stack to compare.
     * @return True when they are equal except the stacksize, false when not.
     */
    @NotNull
    public static Boolean compareItemStacksIgnoreStackSize(ItemStack itemStack1, ItemStack itemStack2) {
        if (!isItemStackEmpty(itemStack1) && !isItemStackEmpty(itemStack2)) {
            // Sort on itemID
            if (Item.getIdFromItem(itemStack1.getItem()) - Item.getIdFromItem(itemStack2.getItem()) == 0) {
                // Sort on item
                if (itemStack1.getItem() == itemStack2.getItem()) {
                    // Then sort on meta
                    if (itemStack1.getItemDamage() == itemStack2.getItemDamage()) {
                        // Then sort on NBT
                        if (itemStack1.hasTagCompound() && itemStack2.hasTagCompound()) {
                            // Then sort on stack size
                            if (ItemStack.areItemStackTagsEqual(itemStack1, itemStack2)) {
                                return true;
                            }
                        } else {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Method to check if two ItemStacks can be merged together.
     * @param existingStack The existing stack.
     * @param mergingStack The merging stack
     * @return True when they can be merged, false when not.
     */
    @NotNull
    public static Boolean areItemStacksMergable(ItemStack existingStack, ItemStack mergingStack) {
        if (!compareItemStacksIgnoreStackSize(existingStack, mergingStack)) {
            return false;
        }
        
        return existingStack.getMaxStackSize() >= (existingStack.stackSize + mergingStack.stackSize);
    }

}
