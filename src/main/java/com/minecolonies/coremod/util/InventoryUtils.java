package com.minecolonies.coremod.util;

import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.entity.ai.citizen.deliveryman.EntityAIWorkDeliveryman;
import com.minecolonies.coremod.entity.ai.item.handling.ItemStorage;
import com.minecolonies.coremod.inventory.InventoryCitizen;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * Returns an inventory as list of item stacks.
     *
     * @param inventory Inventory to convert.
     * @return List of item stacks.
     */
    @NotNull
    public static List<ItemStack> getInventoryAsList(@NotNull final IItemHandler inventory)
    {
        @NotNull final ArrayList<ItemStack> filtered = new ArrayList<>();
        for (int slot = 0; slot < inventory.getSlots(); slot++)
        {
            final ItemStack stack = inventory.getStackInSlot(slot);
            if (stack != null)
            {
                filtered.add(inventory.getStackInSlot(slot));
            }
        }
        return filtered;
    }

    /**
     * {@link #filterInventory(IItemHandler, Item, int)}.
     *
     * @param inventory Inventory to filter in
     * @param block     Block to filter
     * @param itemDamage the damage value.
     * @return List of item stacks
     */
    @NotNull
    public static List<ItemStack> filterInventory(@NotNull final IItemHandler inventory, final Block block, int itemDamage)
    {
        return filterInventory(inventory, getItemFromBlock(block), itemDamage);
    }

    /**
     * Filters a list of items, equal to given parameter, in an {@link IItemHandler}.
     *
     * @param inventory  Inventory to get items from
     * @param targetItem Item to look for
     * @param itemDamage the damage value.
     * @return List of item stacks with the given item in inventory
     */
    @NotNull
    public static List<ItemStack> filterInventory(@NotNull final IItemHandler inventory, @Nullable final Item targetItem, int itemDamage)
    {
        @NotNull final ArrayList<ItemStack> filtered = new ArrayList<>();
        if (targetItem == null)
        {
            return filtered;
        }
        //Check every inventory slot
        for (int slot = 0; slot < inventory.getSlots(); slot++)
        {
            final ItemStack stack = inventory.getStackInSlot(slot);
            if (compareItems(stack, targetItem, itemDamage))
            {
                filtered.add(stack);
            }
        }
        return filtered;
    }

    /**
     * Converts a Block to its Item so it can be compared.
     *
     * @param block the block to convert
     * @return an item from the registry
     */
    public static Item getItemFromBlock(final Block block)
    {
        return new ItemStack(block).getItem();
    }

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
        return itemStack != null && itemStack.getItem() == targetItem && (itemStack.getItemDamage() == itemDamage || itemDamage == -1);
    }

    /**
     * Returns the index of the first occurrence of the block in the inventory.
     *
     * @param inventory Inventory to check.
     * @param block     Block to find.
     * @param itemDamage the damage value.
     * @return Index of the first occurrence.
     */
    public static int findFirstSlotInInventoryWith(@NotNull final IItemHandler inventory, final Block block, int itemDamage)
    {
        return findFirstSlotInInventoryWith(inventory, getItemFromBlock(block), itemDamage);
    }

    /**
     * {@link #findFirstSlotInInventoryWith(IItemHandler, Block, int)}.
     *
     * @param inventory  Inventory to check
     * @param targetItem Item to find
     * @param itemDamage the damage value.
     * @return Index of the first occurrence
     */
    public static int findFirstSlotInInventoryWith(@NotNull final IItemHandler inventory, final Item targetItem, int itemDamage)
    {
        for (int slot = 0; slot < inventory.getSlots(); slot++)
        {
            if (compareItems(inventory.getStackInSlot(slot), targetItem, itemDamage))
            {
                return slot;
            }
        }
        return -1;
        //TODO: Later harden contract to remove compare on slot := -1
        //throw new IllegalStateException("Item "+targetItem.getUnlocalizedName() + " not found in Inventory!");
    }

    /**
     * {@link #getItemCountInInventory(IItemHandler, Item, int)}.
     *
     * @param inventory Inventory to scan
     * @param block     block to count
     * @param itemDamage the damage value
     * @return Amount of occurences
     */
    public static int getItemCountInInventory(@NotNull final IItemHandler inventory, final Block block, int itemDamage)
    {
        return getItemCountInInventory(inventory, getItemFromBlock(block), itemDamage);
    }

    /**
     * Returns the amount of occurrences in the inventory.
     *
     * @param inventory  Inventory to scan
     * @param targetitem Item to count
     * @param itemDamage the item damage value.
     * @return Amount of occurences
     */
    public static int getItemCountInInventory(@NotNull final IItemHandler inventory, final Item targetitem, int itemDamage)
    {
        int count = 0;
        for (@NotNull final ItemStack is : filterInventory(inventory, targetitem, itemDamage))
        {
            count += is.stackSize;
        }
        return count;
    }

    /**
     * Checks if a player has an block in the inventory.
     * Checked by {@link #getItemCountInInventory(IItemHandler, Block, int)} &gt; 0;
     *
     * @param inventory Inventory to scan
     * @param block     Block to count
     * @param itemDamage the damage value.
     * @return True when in inventory, otherwise false
     */
    public static boolean hasItemInInventory(@NotNull final IItemHandler inventory, final Block block, int itemDamage)
    {
        return hasItemInInventory(inventory, getItemFromBlock(block), itemDamage);
    }


    //TODO: Check if this conversion is always safe
    //But seems like ItemStack does it right...

    /**
     * Checks if a player has an item in the inventory.
     * Checked by {@link #getItemCountInInventory(IItemHandler, Item, int)} &gt; 0;
     *
     * @param inventory Inventory to scan
     * @param item      Item to count
     * @param itemDamage the damage value of the item.
     * @return True when in inventory, otherwise false
     */
    public static boolean hasItemInInventory(@NotNull final IItemHandler inventory, final Item item, int itemDamage)
    {
        return getItemCountInInventory(inventory, item, itemDamage) > 0;
    }

    /**
     * Returns if the inventory is full.
     *
     * @param inventory the inventory
     * @return true if the inventory is full
     */
    public static boolean isInventoryFull(@NotNull final IItemHandler inventory)
    {
        return getOpenSlot(inventory) == -1;
    }

    /**
     * returns first open slot in the inventory.
     *
     * @param inventory the inventory to check.
     * @return slot number or -1 if none found.
     */
    public static int getOpenSlot(@NotNull final IItemHandler inventory)
    {
        for (int slot = 0; slot < inventory.getSlots(); slot++)
        {
            if (inventory.getStackInSlot(slot) == null)
            {
                return slot;
            }
        }
        return -1;
    }

    /**
     * Checks if the inventory contains the following tool.
     * @param entity the tileEntity chest or building.
     * @param tool the tool.
     * @param toolLevel to check.
     * @return true if found the tool.
     */
    public static boolean isToolInTileEntity(IItemHandler entity, final String tool, int toolLevel)
    {
        return InventoryFunctions.matchFirstInInventoryWithInventory(
                entity,
                stack -> Utils.isTool(stack, tool) && InventoryUtils.hasToolLevel(tool, stack, toolLevel),
                InventoryFunctions::doNothing
        );
    }

    /**
     * Looks for a pickaxe to mine a block of {@code minLevel}.
     *
     * @param entity inventory to check in.
     * @param minlevel the needed pickaxe level
     * @return true if a pickaxe was found
     */
    public static boolean isPickaxeInTileEntity(@Nonnull TileEntityChest entity, final int minlevel)
    {
        return InventoryFunctions.matchFirstInInventoryWithInventory(
                entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null),
                stack -> stack != null && Utils.checkIfPickaxeQualifies(
                        minlevel,
                        Utils.getMiningLevel(stack, Utils.PICKAXE)
                ),
                InventoryFunctions::doNothing
        );
    }

    /**
     * Looks for a pickaxe to mine a block of {@code minLevel}.
     *
     * @param entity inventory to check in.
     * @param minlevel the needed pickaxe level
     * @param maxLevel the tools max level.
     * @return true if a pickaxe was found
     */
    public static boolean isPickaxeInTileEntity(IItemHandler entity, final int minlevel, final int maxLevel)
    {
        return InventoryFunctions.matchFirstInInventoryWithInventory(
                entity,
                stack -> stack != null && Utils.checkIfPickaxeQualifies(
                        minlevel,
                        Utils.getMiningLevel(stack, Utils.PICKAXE)) && InventoryUtils.hasToolLevel(Utils.PICKAXE, stack, maxLevel
                ),
                InventoryFunctions::doNothing
        );
    }

    /**
     * Looks for a pickaxe to mine a block of {@code minLevel}.
     *
     * @param entity inventory to check in.
     * @param minlevel the needed pickaxe level
     * @return true if a pickaxe was found
     */
    public static boolean isPickaxeInTileEntity(InventoryCitizen entity, final int minlevel)
    {
        return InventoryFunctions.matchFirstInInventoryWithInventory(
                entity,
                stack -> Utils.checkIfPickaxeQualifies(
                        minlevel,
                        Utils.getMiningLevel(stack, Utils.PICKAXE)
                ),
                InventoryFunctions::doNothing
        );
    }

    /**
     * Checks if the inventory contains the following tool.
     * @param inventoryCitizen the inventory citizen.
     * @param tool the tool.
     * @return true if found the tool.
     */
    public static boolean isToolInTileEntity(InventoryCitizen inventoryCitizen, final String tool)
    {
        return InventoryFunctions.matchFirstInInventoryWithInventory(
                inventoryCitizen,
                stack -> Utils.isTool(stack, tool),
                InventoryFunctions::doNothing
        );
    }

    /**
     * {@link #takeStackInSlot(IItemHandler, IItemHandler, int, int, boolean)}.
     * Default:
     * amount: 1
     * takeAll: true
     *
     * @param sendingInv   Inventory of sender
     * @param receivingInv Inventory of receiver
     * @param slotID       Slot ID to take from
     * @return True if item is swapped, otherwise false
     */
    public static boolean takeStackInSlot(final IItemHandler sendingInv, final IItemHandler receivingInv, final int slotID)
    {
        return takeStackInSlot(sendingInv, receivingInv, slotID, 1, true);
    }

    /**
     * Gives an item from an slot index from an inventory and puts it in a receiving inventory.
     * If <code>takeAll</code> is true, the entire slot will we transferred.
     * This only applied when at least <code>amount</code> can be taken.
     *
     * @param sendingInv   Inventory of sender
     * @param receivingInv Inventory of receiver
     * @param slotID       Slot ID to take from
     * @param amount       Amount to swap
     * @param takeAll      Whether or not the entire stack of the sender should be emptied if possible
     *                     Only applies when <code>amount</code> is sufficient
     * @return True if item is swapped, otherwise false
     */
    public static boolean takeStackInSlot(
                                           @Nullable final IItemHandler sendingInv, @Nullable final IItemHandler receivingInv,
                                           final int slotID, final int amount, final boolean takeAll)
    {
        if (receivingInv != null && sendingInv != null && slotID >= 0 && amount >= 0)
        {
            // gets itemstack in slot, and decreases stacksize
            @Nullable ItemStack stack = sendingInv.extractItem(slotID, takeAll ? 64 : amount, true);
            @Nullable ItemStack leftover = ItemHandlerHelper.insertItemStacked(receivingInv, stack, true);
            int sizeToSend = stack.stackSize - (leftover != null ? leftover.stackSize : 0);
            if (sizeToSend < amount)
            {
                return false;
            }

            stack = sendingInv.extractItem(slotID, sizeToSend, false);
            ItemHandlerHelper.insertItemStacked(receivingInv, stack, false);
            return true;
        }
        return false;
    }

    /**
     * {@link #setOverSizedStack(IItemHandler, ItemStack)}.
     * Tries to put an itemStack into Inventory, unlike setStack, allow to use a ItemStack bigger than the maximum stack size allowed for the item
     *
     * @param inventory the inventory to set the stack in.
     * @param stack     Item stack with items to be transferred, the stack can be bigger than allowed
     * @return returns null if successful, or stack of remaining items, BE AWARE that the remaining stack can be bigger than the maximum stack size
     */
    @Nullable
    public static ItemStack setOverSizedStack(@NotNull final IItemHandler inventory, @Nullable final ItemStack stack)
    {
        int stackSize = stack.stackSize;
        while (stackSize > 0)
        {
            final int itemCount = Math.min(stackSize, stack.getMaxStackSize());
            final ItemStack items = new ItemStack(stack.getItem(), itemCount, stack.getItemDamage());
            stackSize-=itemCount;
            final ItemStack remainingItems = ItemHandlerHelper.insertItemStacked(inventory, items, false);
            if(remainingItems != null)
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
     * Returns a slot number if a chest contains given ItemStack item that is not fully stacked.
     *
     * @param inventory the inventory to check.
     * @param stack     the stack to check for.
     * @return returns slot number if found, -1 when not found.
     */
    public static int containsPartialStack(@NotNull final IItemHandler inventory, final ItemStack stack)
    {
        for (int i = 0; i < inventory.getSlots(); i++)
        {
            final ItemStack testStack = inventory.getStackInSlot(i);
            if (testStack != null && testStack.isItemEqual(stack) && testStack.stackSize != testStack.getMaxStackSize())
            {
                return i;
            }
        }
        return -1;
    }

    /**
     * {@link #takeStackInSlot(IItemHandler, IItemHandler, int, int, boolean)}.
     * Default:
     * takeAll: false
     *
     * @param sendingInv   Inventory of sender
     * @param receivingInv Inventory of receiver
     * @param slotID       Slot ID to take from
     * @param amount       Amount to swap
     * @return True if item is swapped, otherwise false
     */
    public static boolean takeStackInSlot(final IItemHandler sendingInv, final IItemHandler receivingInv, final int slotID, final int amount)
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
    public static ItemStack[] getAllItemStacks(@NotNull final IItemHandler inventory)
    {
        @NotNull final ItemStack[] itemStack = new ItemStack[inventory.getSlots()];
        for (int i = 0; i < inventory.getSlots(); i++)
        {
            itemStack[i] = inventory.getStackInSlot(i);
        }
        return itemStack;
    }

    /**
     * Returns the amount of item stacks in an inventory.
     * This equals {@link #getAllItemStacks(IItemHandler)}<code>.length();</code>.
     *
     * @param inventory Inventory to count item stacks of.
     * @return Amount of item stacks in inventory.
     */
    public static int getAmountOfStacks(@NotNull final IItemHandler inventory)
    {
        int count = 0;
        for (int i = 0; i < inventory.getSlots(); i++)
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
     * Returns a slot number if an inventory contains given tool type.
     *
     * @param inventory the inventory to get the slot from.
     * @param tool      the tool type to look for.
     * @return slot number if found, -1 if not found.
     */
    public static int getFirstSlotContainingTool(@NotNull final IItemHandler inventory, @NotNull final String tool)
    {
        for (int i = 0; i < inventory.getSlots(); i++)
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

    public static boolean addItemStackToInventory(@NotNull final IItemHandler inventory, @Nullable final ItemStack itemStack)
    {
        if (ItemHandlerHelper.insertItemStacked(inventory, itemStack, true) != null)
        {
            return false;
        }
        return ItemHandlerHelper.insertItemStacked(inventory, itemStack, false) == null;
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
    public static boolean hasToolLevel(final String tool, @NotNull final IItemHandler inventory, final int hutLevel)
    {
        for (int i = 0; i < inventory.getSlots(); i++)
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
     * Verifies if there is one tool with an acceptable level
     * in a worker's inventory.
     *
     * @param tool      the type of tool needed
     * @param stack     the stack to test.
     * @param hutLevel  the worker's hut level
     * @return true if tool is acceptable
     */
    public static boolean hasToolLevel(final String tool, @Nullable final ItemStack stack, final int hutLevel)
    {
        if (stack == null)
        {
            return false;
        }

        final int level = Utils.getMiningLevel(stack, tool);
        if (Utils.isTool(stack, tool) && verifyToolLevel(stack, level, hutLevel))
        {
            return true;
        }

        return false;
    }

    /**
     * Verifies if an item has an appropriated grade.
     *
     * @param item     the type of tool needed
     * @param level    the type of tool needed
     * @param hutLevel the worker's hut level
     * @return true if tool is acceptable
     */
    public static boolean verifyToolLevel(final ItemStack item, int level, final int hutLevel)
    {
        if (item == null || hutLevel > FREE_TOOL_CHOICE_LEVEL)
        {
            return true;
        }
        else if (item.isItemEnchanted() && hutLevel <= EFFECT_TOOL_CHOICE_LEVEL)
        {
            return false;
        }
        else if (hutLevel >= level)
        {
            return true;
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
                return "";
        }
    }

    /**
     * Adapted from {@link net.minecraft.entity.player.InventoryPlayer#addItemStackToInventory(ItemStack)}.
     *
     * @param inventory Inventory to add itemstack to.
     * @param itemStack ItemStack to add.
     * @param building the building.
     * @return itemStack which has been replaced.
     */
    @Nullable
    public static ItemStack forceItemStackToInventory(@NotNull final IItemHandler handler, @NotNull final ItemStack itemStack, @NotNull final AbstractBuilding building)
    {
        if(!addItemStackToInventory(handler, itemStack))
        {
            final List<ItemStorage> localAlreadyKept = new ArrayList<>();
            for(int i = 0; i < handler.getSlots(); i++)
            {
                final ItemStack localStack = handler.getStackInSlot(i);
                if(!EntityAIWorkDeliveryman.workerRequiresItem(building, localStack, localAlreadyKept))
                {
                    final ItemStack removedStack = handler.extractItem(i, localStack.stackSize, false);
                    if (handler.insertItem(i, itemStack.copy(), false) != null)
                    {
                        MineColonies.getLogger().error("forceItemStackToInventory failed forcing! This is a bug - please report to MineColonies developers!");
                    }
                    return removedStack.copy();
                }
            }
        }
        return null;
    }

    /**
     * Allows opening inventories.
     */
    public static void openGui(EntityPlayer player, ICapabilityProvider provider) {
        if (provider instanceof TileEntity) {
            player.openGui(MineColonies.instance, 1, ((TileEntity) provider).getWorld(),
                    ((TileEntity) provider).getPos().getX(),
                    ((TileEntity) provider).getPos().getY(),
                    ((TileEntity) provider).getPos().getZ());
        } else if (provider instanceof Entity) {
            player.openGui(MineColonies.instance, 2, ((Entity) provider).getEntityWorld(),
                    ((Entity) provider).getEntityId(),
                    0,
                    0);
        }
    }

    public static void dropItemHandlerItems(World worldIn, double x, double y, double z, IItemHandler inventory)
    {
        for (int i = 0; i < inventory.getSlots(); i++)
        {
            ItemStack itemstack = inventory.getStackInSlot(i);

            if (itemstack != null)
            {
                InventoryHelper.spawnItemStack(worldIn, x, y, z, itemstack);
            }
        }
    }
}
