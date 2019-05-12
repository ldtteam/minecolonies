package com.minecolonies.coremod.inventory;

import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.colony.buildings.inventory.ItemStackHandlerWithIndex;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;

import static org.junit.Assert.assertEquals;

public class InventoryTest
{
    private IItemHandlerModifiable             inventory;
    private ItemStackHandlerWithIndex<Integer> indexInventory;

    @InjectMocks
    private BuildingTownHall building;

    @Before
    public void setupInventories()
    {
        final TileEntityColonyBuilding buildingTileEntity = new TileEntityColonyBuilding();
        buildingTileEntity.setBuilding(building);
        this.inventory = buildingTileEntity.getInventory();
        indexInventory = new ItemStackHandlerWithIndex<>(5);
    }

    @Test
    public void testEmptyInventory()
    {
        for (int i = 0; i < inventory.getSlots(); i++)
        {
            assertEquals("Inventory space wasn't empty", ItemStackUtils.EMPTY, inventory.getStackInSlot(i));
        }
        for (int i = 0; i < indexInventory.getSlots(); i++)
        {
            assertEquals("IndexInventory space wasn't empty", ItemStackUtils.EMPTY, indexInventory.getStackInSlot(i));
        }
    }

    @Test
    public void testAddStack()
    {
        // Using a null item allows us to get past the call to Blocks or Items.
   /*     final ItemStack itemStack = new ItemStack((Item) null);
        inventory.setStackInSlot(0, itemStack);
        final ItemStack returnedItemStack = inventory.getStackInSlot(0);
        assertNotEquals("The Item wasn't set", itemStack, ItemStackUtils.EMPTY);
        assertSame("Stack wasn't the same", itemStack, returnedItemStack);
/*
        indexInventory.setStackInSlot(0, itemStack);
        final ItemStack returned = indexInventory.getStackInSlot(0);
        assertNotEquals("The Item wasn't set", itemStack, ItemStackUtils.EMPTY);
        assertSame("Stack wasn't the same", itemStack, returned);*/
    }
}
