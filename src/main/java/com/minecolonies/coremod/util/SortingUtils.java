package com.minecolonies.coremod.util;

import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.inventory.api.CombinedItemHandler;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utilities for sorting item handlers.
 */
public final class SortingUtils
{
    /**
     * Private constructor to hide implicit one.
     */
    private SortingUtils()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Sort a combined item handler by certain conditions.
     * Group into creative tabs if possible.
     * @param inv the item handler to sort.
     */
    public static void sort(final CombinedItemHandler inv)
    {
        final AtomicInteger runCount = new AtomicInteger(0);

        final Map<ItemStorage, Integer> map = new HashMap<>();
        if (inv != null)
        {
            for (int i = 0; i < inv.getSlots(); i++)
            {
                if (ItemStackUtils.isEmpty(inv.getStackInSlot(i)))
                {
                    continue;
                }
                final ItemStorage storage = new ItemStorage(inv.extractItem(i, 64, false));
                int amount = storage.getAmount();
                if (map.containsKey(storage))
                {
                    amount += map.remove(storage);
                }
                map.put(storage, amount);
            }

            final Tuple<AtomicInteger, Map<Integer, Integer>> tuple = SortingUtils.calcRequiredSlots(map);
            final double totalSlots = inv.getSlots();
            final int totalReq = tuple.getA().get();
            map.entrySet().stream().sorted(SortingUtils::compare)
              .forEach(entry -> SortingUtils.pushIntoInv(runCount, entry, inv, tuple.getA(), totalSlots, totalReq, tuple.getB()));
        }
    }

    /**
     * Pushes a item storage to an inventory following certain rules.
     * @param currentSlot the starting slot to start pushing.
     * @param entry the map entry with storage and size.
     * @param inv the inventory to push it to.
     * @param requiredSlots the required slots in total to be pushed to (counting down).
     * @param totalSlots the total available slots.
     * @param totalRequirement the required slots in total to be pushed to.
     * @param creativeTabs the creative tabs information for the items.
     */
    private static void pushIntoInv(
      final AtomicInteger currentSlot,
      final Map.Entry<ItemStorage, Integer> entry,
      final CombinedItemHandler inv,
      final AtomicInteger requiredSlots,
      final double totalSlots, final double totalRequirement, final Map<Integer, Integer> creativeTabs)
    {
        final int creativeTabId = entry.getKey().getCreativeTabIndex().get(0);

        int slotLimit = 0;
        final ItemStack stack = entry.getKey().getItemStack();
        int tempSize = entry.getValue();
        while (tempSize > 0)
        {
            final ItemStack tempStack = stack.copy();
            tempStack.setCount(Math.min(tempSize, tempStack.getMaxStackSize()));
            slotLimit = inv.getLastIndex(currentSlot.get());
            while (!inv.insertItem(currentSlot.getAndIncrement(), tempStack, false).isEmpty())
            {
                Log.getLogger().error("Trying to dump into same slot again!");
            }
            tempSize -= tempStack.getCount();
            requiredSlots.decrementAndGet();
            creativeTabs.put(creativeTabId, creativeTabs.get(creativeTabId) - 1);
        }

        if (creativeTabs.get(creativeTabId) <= 0 && (totalSlots - slotLimit) >= requiredSlots.get())
        {
            final double dumpedSlots = (totalRequirement - requiredSlots.get());
            final double usageFactor = totalSlots/dumpedSlots;
            final double theoreticalJumpFactor = (totalSlots - slotLimit) / requiredSlots.get();

            if (theoreticalJumpFactor <= usageFactor || theoreticalJumpFactor > 4)
            {
                currentSlot.set(slotLimit);
            }
        }
    }

    /**
     * Compared to itemStorage entries.
     * Based on:
     * - Creative tab
     * - Id
     * - Damage value
     * @param t1 the first itemStorage entry.
     * @param t2 the second itemStorage entry.
     * @return an integer which describes the difference.
     */
    private static int compare(final Map.Entry<ItemStorage, Integer> t1, final Map.Entry<ItemStorage, Integer> t2)
    {
        final int creativeTabId1 = t1.getKey().getCreativeTabIndex().get(0);
        final int creativeTabId2 = t2.getKey().getCreativeTabIndex().get(0);

        if (creativeTabId1 != creativeTabId2)
        {
            return creativeTabId1 - creativeTabId2;
        }

        final int id1 = getId(t1.getKey().getItem());
        final int id2 = getId(t2.getKey().getItem());

        if (id1 == id2)
        {
            return t1.getKey().getDamageValue() - t2.getKey().getDamageValue();
        }
        return id1 - id2;
    }

    /**
     * Get the item ID of an item.
     * @param item the item to check.
     * @return the integer id of minecraft.
     */
    private static int getId(final Item item)
    {
        return ((ForgeRegistry<Item>) ForgeRegistries.ITEMS).getID(item);
    }

    /**
     * Calculate how many slots in total will be required for a map of item storages.
     * Also calculate and return how many creativeTabs are involved and how many items per tab.
     * @param map the map of itemStorages with amount.
     * @return a tuple containing the required information.
     */
    private static Tuple<AtomicInteger, Map<Integer, Integer>> calcRequiredSlots(final Map<ItemStorage, Integer> map)
    {
        final Map<Integer, Integer> creativeTabs = new HashMap<>();
        int sum = 0;
        for (final Map.Entry<ItemStorage, Integer> entry : map.entrySet())
        {
            sum += Math.ceil((double) entry.getValue() / entry.getKey().getItemStack().getMaxStackSize());
            final int index = entry.getKey().getCreativeTabIndex().isEmpty() ? 0 : entry.getKey().getCreativeTabIndex().get(0);
            creativeTabs.put(index, creativeTabs.getOrDefault(index, 0) + (int) Math.ceil((double) entry.getValue() / entry.getKey().getItemStack().getMaxStackSize()));
        }

        return new Tuple<>(new AtomicInteger(sum), creativeTabs);
    }

}
