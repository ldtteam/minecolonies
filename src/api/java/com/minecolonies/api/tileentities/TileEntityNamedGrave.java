package com.minecolonies.api.tileentities;

import net.minecraft.tileentity.TileEntityType;

public class TileEntityNamedGrave extends AbstractTileEntityNamedGrave
{
    /**
     * Default constructor used to create a new TileEntity via reflection. Do not use.
     */
    public TileEntityNamedGrave()
    {
        this(MinecoloniesTileEntities.NAMED_GRAVE);
    }

    /**
     * Alternative overriden constructor.
     *
     * @param type the entity type.
     */
    public TileEntityNamedGrave(final TileEntityType<? extends TileEntityNamedGrave> type)
    {
        super(type);
    }
}
