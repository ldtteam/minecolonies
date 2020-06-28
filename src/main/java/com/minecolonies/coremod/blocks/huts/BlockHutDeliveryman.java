package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the warehouse.
 * No different from {@link AbstractBlockHut}
 */

public class BlockHutDeliveryman extends AbstractBlockHut<BlockHutDeliveryman>
{
    public BlockHutDeliveryman()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockhutdeliveryman";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.deliveryman;
    }
}
