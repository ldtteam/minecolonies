package com.minecolonies.coremod.colony.buildings.utils;

import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.blockout.Log;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingContainer;
import com.minecolonies.coremod.colony.buildings.inventory.ItemSearchResult;
import com.minecolonies.coremod.colony.buildings.inventory.ItemStorageIndex;
import com.minecolonies.coremod.tileentities.TileEntityRack;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_ENTITY_WORKER_INVENTORYFULLCHEST;
import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

/**
 * Utils class for accessing all building inventories.
 */
public final class BuildingInventoryUtils
{
    private BuildingInventoryUtils()
    {
        // Intentionally disabled
    }

    /**
     * Method used to check if this building holds a certain count of the requested itemstacks.
     *
     * @param itemStackSelectionPredicate The predicate to check with.
     * @return Boolean, Items boolean true if enough items in the building.
     */
    public static Tuple<Boolean, List<ItemSearchResult>> getMatchingItemStacksWithCount(
      @NotNull AbstractBuildingContainer building,
      @NotNull final Predicate<ItemStack> itemStackSelectionPredicate,
      int count)
    {
        int itemCount = 0;
        List<ItemSearchResult> results = new ArrayList<>();

        for (Map.Entry<ItemStorage, Set<TileEntityRack>> entry : building.getInventoryIndex().getIndexMap().entrySet())
        {
            // Check for a matching stack
            if (itemStackSelectionPredicate.test(entry.getKey().getItemStack()))
            {
                // Count stacks
                for (TileEntityRack rack : entry.getValue())
                {
                    // Add to results for future use
                    results.add(new ItemSearchResult(entry.getKey(), rack, rack.getInventory().getSlotsForItem(entry.getKey())));
                    for (Integer slot : rack.getInventory().getSlotsForItem(entry.getKey()))
                    {

                        itemCount += rack.getInventory().getStackInSlot(slot).getCount();
                        if (itemCount >= count)
                        {
                            return new Tuple(true, results);
                        }
                    }
                }
            }
        }
        return new Tuple(false, results);
    }

    /**
     * Method to get all matching ItemStack in the Building.
     *
     * @param itemStackSelectionPredicate The predicate to select the ItemStack with.
     * @return The first matching ItemStack.
     */
    @NotNull
    public static List<ItemSearchResult> getAllMatchingStacks(
      @NotNull final AbstractBuildingContainer building,
      @NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        List<ItemSearchResult> stacks = new ArrayList<>();
        ItemStorageIndex<TileEntityRack> index = building.getInventoryIndex();

        for (ItemStorage storage : index.getAllMatchingItems(itemStackSelectionPredicate))
        {
            for (TileEntityRack rack : index.getValueForKey(storage))
            {
                stacks.add(new ItemSearchResult(storage, rack, rack.getInventory().getSlotsForItem(storage)));
            }
        }
        return stacks;
    }

    /**
     * Gets the first stack for a given predicate
     *
     * @param building           Building to search in
     * @param itemStackPredicate search predicate
     * @return Itemstack we were looking for
     */
    public ItemStack getFirstStackForPredicate(@NotNull final AbstractBuildingContainer building, @NotNull final Predicate<ItemStack> itemStackPredicate)
    {
        Tuple<ItemStorage, Set<TileEntityRack>> result = building.getInventoryIndex().getFirstEntryForItemStackPredicate(itemStackPredicate);
        if (result != null)
        {
            return result.getFirst().getItemStack();
        }
        return null;
    }

    /**
     * Check for a certain item and return the position of the chest containing it.
     *
     * @param stack the stack to search for.
     * @return the position or null.
     */
    @Nullable
    public static BlockPos getPositionOfContainerWithItemStack(@NotNull final AbstractBuildingContainer building, @NotNull final ItemStack stack)
    {
        ItemStorage storage = new ItemStorage(stack);
        Set<TileEntityRack> racks = building.getInventoryIndex().getValueForKey(storage);

        if (racks.isEmpty())
        {
            return null;
        }

        return racks.iterator().next().getPos();
    }

    /**
     * Dump the inventory into the Building.
     * Go through all items and search the right chest to dump it in.
     *
     * @param inventoryCitizen the inventory to dump
     */
    public static void dumpInventoryIntoBuilding(@NotNull final AbstractBuildingContainer building, @NotNull final IInventory inventoryCitizen)
    {
        for (int i = 0; i < new InvWrapper(inventoryCitizen).getSlots(); i++)
        {
            final ItemStack stack = inventoryCitizen.getStackInSlot(i);
            if (ItemStackUtils.isEmpty(stack))
            {
                continue;
            }
            @Nullable final TileEntity chest = searchRightChestForStack(building, stack);
            if (chest == null)
            {
                LanguageHandler.sendPlayersMessage(building.getColony().getMessageEntityPlayers(), COM_MINECOLONIES_COREMOD_ENTITY_WORKER_INVENTORYFULLCHEST);
                return;
            }
            InventoryUtils.transferItemStackIntoNextBestSlotInItemHandler(new InvWrapper(inventoryCitizen), i, chest.getCapability(ITEM_HANDLER_CAPABILITY, null));
        }
    }

    /**
     * Search the right chest for an itemStack.
     *
     * @param stack the stack to dump.
     * @return the tile entity of the chest
     */
    @Nullable
    private static TileEntity searchRightChestForStack(@NotNull final AbstractBuildingContainer building, @NotNull final ItemStack stack)
    {
        ItemStorage search = new ItemStorage(stack);

        // Check existing stacks for space
        Set<TileEntityRack> racksFound = building.getInventoryIndex().getValueForKey(search);
        if (racksFound != null)
        {
            for (TileEntityRack rack : racksFound)
            {
                for (Integer slot : rack.getInventory().getSlotsForItem(search))
                {
                    if (ItemStackUtils.getSize(rack.getInventory().getStackInSlot(slot)) + ItemStackUtils.getSize(stack) <= stack.getMaxStackSize())
                    {
                        Log.getLogger().warn("SearchRightChest found matching stack:" + rack.getInventory().getStackInSlot(slot));
                        return rack;
                    }
                }
            }
        }

        @Nullable final TileEntity chest = searchChestWithSimilarItem(building, stack);
        return chest == null ? searchMostEmptySlot(building) : chest;
    }

    /**
     * Searches a chest with a similar item as the incoming stack.
     *
     * @param stack the stack.
     * @return the entity of the chest.
     */
    @Nullable
    private static TileEntity searchChestWithSimilarItem(@NotNull final AbstractBuildingContainer building, final ItemStack stack)
    {
        for (Map.Entry<ItemStorage, Set<TileEntityRack>> entry : building.getInventoryIndex().getIndexMap().entrySet())
        {
            if (entry.getKey().getItemStack().getItem() == stack.getItem())
            {
                for (TileEntityRack rack : entry.getValue())
                {
                    if (rack.getFreeSlots() > 0)
                    {
                        return rack;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Search for the rack with the least items in it.
     *
     * @return the tileEntity of this chest.
     */
    @Nullable
    private static TileEntity searchMostEmptySlot(@NotNull final AbstractBuildingContainer building)
    {
        int freeSlots = 0;
        TileEntity emptiestChest = null;
        for (@NotNull final BlockPos pos : building.getAdditionalCountainers())
        {
            final TileEntity entity = building.getColony().getWorld().getTileEntity(pos);
            if (entity == null)
            {
                building.removeContainerPosition(pos);
                continue;
            }
            final int tempFreeSlots;
            if (entity instanceof TileEntityRack)
            {
                if (((TileEntityRack) entity).isEmpty())
                {
                    return entity;
                }

                tempFreeSlots = ((TileEntityRack) entity).getFreeSlots();
                if (freeSlots < tempFreeSlots)
                {
                    freeSlots = tempFreeSlots;
                    emptiestChest = entity;
                }
            }
        }

        return emptiestChest;
    }
}
