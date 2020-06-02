package com.minecolonies.api.tileentities;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.items.ItemStackHandler;

import static com.minecolonies.api.colony.requestsystem.requestable.deliveryman.AbstractDeliverymanRequestable.getMaxBuildingPriority;
import static com.minecolonies.api.util.constant.Constants.DEFAULT_SIZE;

/**
 * Class which handles the tileEntity for the Stash block.
 */
public class TileEntityStash extends TileEntityColonyBuilding
{

    /**
     * Constructor of the stash based on a tile entity type
     *
     * @param type tile entity type
     */
    public TileEntityStash(final TileEntityType type)
    {
        super(type);
        inventory = new NotifyingRackInventory(DEFAULT_SIZE);
    }

    /**
     * Default constructor of the stash
     */
    public TileEntityStash()
    {
        super(MinecoloniesTileEntities.STASH);
        inventory = new NotifyingRackInventory(DEFAULT_SIZE);
    }

    /**
     * An {@link ItemStackHandler} that notifies the container TileEntity when it's inventory has changed.
     */
    public class NotifyingRackInventory extends RackInventory
    {
        public NotifyingRackInventory(final int defaultSize)
        {
            super(defaultSize);
        }

        @Override
        protected void onContentsChanged(final int slot)
        {
            super.onContentsChanged(slot);

            if (world != null && !world.isRemote && IColonyManager.getInstance().isCoordinateInAnyColony(world, pos))
            {
                final IColony colony = IColonyManager.getInstance().getClosestColony(world, pos);
                if (colony != null)
                {
                    final IBuilding building = colony.getBuildingManager().getBuilding(pos);
                    if (!freeStacks())
                    {
                        // Note that createPickupRequest will make sure to only create on request per building.
                        building.createPickupRequest(getMaxBuildingPriority(true));
                    }
                }
            }
        }
    }
}
