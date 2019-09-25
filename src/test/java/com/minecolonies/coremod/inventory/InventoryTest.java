package com.minecolonies.coremod.inventory;

import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.workerbuildings.ITownHall;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.blocks.huts.BlockHutTownHall;
import com.minecolonies.coremod.test.AbstractMockStaticsTest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.when;

public class InventoryTest extends AbstractMockStaticsTest
{
    private IItemHandlerModifiable inventory;

    @Mock
    private ITownHall building;

    @Before
    public void setupInventories()
    {
        BuildingEntry entry = new BuildingEntry.Builder().setBuildingBlock(new BlockHutTownHall())
                                .setBuildingProducer((c, p) -> null)
                                .setBuildingViewProducer(() -> (c, p) -> null)
                                .setRegistryName(new ResourceLocation(ModBuildings.TOWNHALL_ID))
                                .createBuildingEntry();

        when(building.getBuildingRegistryEntry()).thenReturn(entry);

        final TileEntityColonyBuilding buildingTileEntity = new TileEntityColonyBuilding(building.getBuildingRegistryEntry().getRegistryName());
        buildingTileEntity.setBuilding(building);
        this.inventory = buildingTileEntity.getInventory();
    }

    @Test
    public void testEmptyInventory()
    {
        for (int i = 0; i < inventory.getSlots(); i++)
        {
            assertEquals("Inventory space wasn't empty", ItemStackUtils.EMPTY, inventory.getStackInSlot(i));
        }
    }

    @Test
    public void testAddStack()
    {
        // Using a null item allows us to get past the call to Blocks or Items.
        final ItemStack itemStack = new ItemStack((Item) null);
        inventory.setStackInSlot(0, itemStack);
        final ItemStack returnedItemStack = inventory.getStackInSlot(0);
        assertNotEquals("The Item wasn't set", itemStack, ItemStackUtils.EMPTY);
        assertSame("Stack wasn't the same", itemStack, returnedItemStack);
    }
}
