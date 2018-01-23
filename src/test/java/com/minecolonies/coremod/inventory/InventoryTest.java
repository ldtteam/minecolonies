package com.minecolonies.coremod.inventory;

import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.BuildingTownHall;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.Assert.*;

public class InventoryTest
{
    private IInventory inventory;

    @InjectMocks
    private BuildingTownHall building;

    @Before
    public void setupInventories()
    {
        final TileEntityColonyBuilding buildingTileEntity = new TileEntityColonyBuilding();
        buildingTileEntity.setBuilding(building);
        this.inventory = buildingTileEntity;
    }

    @Test
    public void testEmptyInventory()
    {
        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            assertEquals("Inventory space wasn't empty", ItemStackUtils.EMPTY, inventory.getStackInSlot(i));
        }
    }

    @Test
    public void testAddStack()
    {
        // Using a null item allows us to get past the call to Blocks or Items.
        final ItemStack itemStack = new ItemStack((Item) null);
        inventory.setInventorySlotContents(0, itemStack);
        final ItemStack returnedItemStack = inventory.getStackInSlot(0);
        assertNotEquals("The Item wasn't set", itemStack, ItemStackUtils.EMPTY);
        assertSame("Stack wasn't the same", itemStack, returnedItemStack);
    }
}
