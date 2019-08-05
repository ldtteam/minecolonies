package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Block of the BarracksTower.
 */
public class BlockHutBarracksTower extends AbstractBlockHut<BlockHutBarracksTower>
{
    /**
     * Default constructor.
     */
    public BlockHutBarracksTower()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutBarracksTower";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.barracksTower;
    }
}
