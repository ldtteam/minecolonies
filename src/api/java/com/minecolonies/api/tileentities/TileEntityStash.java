package com.minecolonies.api.tileentities;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import static com.minecolonies.api.util.constant.BuildingConstants.MAX_PRIO;
import static com.minecolonies.api.util.constant.Constants.DEFAULT_SIZE;

/**
 * Class which handles the tileEntity for the Stash block.
 */

public class TileEntityStash extends TileEntityColonyBuilding
{

    public TileEntityStash(final TileEntityType type)
    {
        super(type);
        inventory = new NotifyingRackInventory(DEFAULT_SIZE);
    }

    public TileEntityStash()
    {
        super(MinecoloniesTileEntities.STASH);
        inventory = new NotifyingRackInventory(DEFAULT_SIZE);
    }

    /**
     * Called when the inventory of the tileEntity it holds it's changed
     *
     * @param isEmpty
     */
    private void buildingInventoryChanged(boolean isEmpty)
    {
        IBuildingContainer building = getBuilding();
        if (!isEmpty && building != null && !building.isPriorityStatic())
        {
            if (building instanceof IBuilding)
            {
                IBuilding iBuilding = (IBuilding) building;
                if (!iBuilding.isBeingGathered())
                {
                    iBuilding.alterPickUpPriority(MAX_PRIO);
                }
            } else
            {
                building.alterPickUpPriority(MAX_PRIO);
            }
        }
    }

    /**
     * An {@ItemStackHandler} that notifies the container TileEntity when it's inventory has changed.
     */
    public class NotifyingRackInventory extends RackInventory
    {
        public NotifyingRackInventory(final int defaultSize)
        {
            super(defaultSize);
        }

        @Override
        protected void onContentsChanged(int slot)
        {
            super.onContentsChanged(slot);
            buildingInventoryChanged(isEmpty());
        }
    }
}
