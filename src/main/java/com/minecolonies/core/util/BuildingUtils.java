package com.minecolonies.core.util;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.util.InventoryUtils;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class BuildingUtils
{
    /**
     * Private constructor to hide public one.
     */
    private BuildingUtils()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Get the hut from the inventory.
     *
     * @param inventory the inventory to search.
     * @param hut       the hut to fetch.
     * @return the stack or if not found empty.
     */
    public static ItemStack getItemStackForHutFromInventory(final Inventory inventory, final String hut)
    {
        final int slot = InventoryUtils.findFirstSlotInProviderNotEmptyWith(inventory.player,
          item -> item.getItem() instanceof BlockItem && ((BlockItem) item.getItem()).getBlock() instanceof AbstractBlockHut && ForgeRegistries.BLOCKS.getKey(((BlockItem) item.getItem()).getBlock())
                  .getPath()
                  .endsWith(hut));

        if (slot != -1)
        {
            return inventory.getItem(slot);
        }
        return ItemStack.EMPTY;
    }
}
