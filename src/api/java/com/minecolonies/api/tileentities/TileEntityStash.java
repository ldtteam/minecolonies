package com.minecolonies.api.tileentities;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.items.ItemStackHandler;

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
     * An {@link ItemStackHandler} that notifies the container TileEntity when it's inventory has changed.
     */
    public class NotifyingRackInventory extends RackInventory
    {
        public NotifyingRackInventory(final int defaultSize)
        {
            super(defaultSize);
        }
    }
}
