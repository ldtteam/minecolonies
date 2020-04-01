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
 * Class which handles the tileEntity of our colonyBuildings.
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

    private void buildingInventoryChanged(boolean isEmpty) {
        IBuildingContainer building = getBuilding();
        if (building != null && !building.isPriorityStatic()) {
            if (isEmpty)
                building.alterPickUpPriority(MAX_PRIO*-1);
            else if (building instanceof IBuilding) {
                IBuilding iBuilding = (IBuilding) building;
                if (!iBuilding.isBeingGathered()) {
                    iBuilding.alterPickUpPriority(MAX_PRIO);
                }
            } else {
                building.alterPickUpPriority(MAX_PRIO);
            }
        }
    }

    public class NotifyingRackInventory extends RackInventory {
        public NotifyingRackInventory(final int defaultSize)
        {
            super(defaultSize);
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            buildingInventoryChanged(isEmpty());
        }
    }
}
