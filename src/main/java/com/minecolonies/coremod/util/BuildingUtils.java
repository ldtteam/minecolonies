package com.minecolonies.coremod.util;

import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.LoadOnlyStructureHandler;
import com.minecolonies.coremod.colony.buildings.AbstractSchematicProvider;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Mirror;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class BuildingUtils
{
    /**
     * Private constructor to hide public one.
     */
    private BuildingUtils()
    {
        /**
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
    public static ItemStack getItemStackForHutFromInventory(final PlayerInventory inventory, final String hut)
    {
        final int slot = InventoryUtils.findFirstSlotInProviderNotEmptyWith(inventory.player,
          item -> item.getItem() instanceof BlockItem && ((BlockItem) item.getItem()).getBlock() instanceof AbstractBlockHut && ((BlockItem) item.getItem()).getBlock()
                                                                                                                                  .getRegistryName()
                                                                                                                                  .getPath()
                                                                                                                                  .endsWith(hut));

        if (slot != -1)
        {
            return inventory.getStackInSlot(slot);
        }
        return ItemStack.EMPTY;
    }
}
