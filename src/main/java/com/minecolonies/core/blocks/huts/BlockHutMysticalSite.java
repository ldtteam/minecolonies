package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

/**
 * Hut for the mystical site. No different from {@link AbstractBlockHut}
 */
public class BlockHutMysticalSite extends AbstractBlockHut<BlockHutMysticalSite>
{
    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.mysticalSite.get();
    }
}
