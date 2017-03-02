package com.minecolonies.coremod.inventory;

import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

@RunWith(MockitoJUnitRunner.class)
public class InventoryTest
{
    private IInventory inventory;

    @InjectMocks
    private BuildingTownHall building;

    @Mock
    private Colony colony;

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
            assertNull("Inventory space wasn't empty", inventory.getStackInSlot(i));
        }
    }

    @Test
    public void testAddStack()
    {
        final ItemStack itemStack = PowerMockito.mock(ItemStack.class);
        inventory.setInventorySlotContents(0, itemStack);
        assertSame(itemStack, inventory.getStackInSlot(0));
    }
}
