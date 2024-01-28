package com.minecolonies.core.tileentities;

import com.minecolonies.api.tileentities.AbstractTileEntityNamedGrave;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Represent a TileEntity that will be built by a graveyard undertaker in a graveyard to honor a dead citizen
 */
public class TileEntityNamedGrave extends AbstractTileEntityNamedGrave
{
    /**
     * Default constructor used to create a new TileEntity via reflection. Do not use.
     */
    public TileEntityNamedGrave(final BlockPos pos, final BlockState state)
    {
        this(MinecoloniesTileEntities.NAMED_GRAVE.get(), pos, state);
    }

    /**
     * Alternative overriden constructor.
     *
     * @param type the entity type.
     */
    public TileEntityNamedGrave(final BlockEntityType<? extends TileEntityNamedGrave> type, final BlockPos pos, final BlockState state)
    {
        super(type, pos, state);
    }
}
