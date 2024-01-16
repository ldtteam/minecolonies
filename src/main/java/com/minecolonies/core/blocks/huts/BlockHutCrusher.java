package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the crusher. No different from {@link AbstractBlockHut}
 */
public class BlockHutCrusher extends AbstractBlockHut<BlockHutCrusher>
{
    @NotNull
    @Override
    public String getHutName()
    {
        return "blockhutcrusher";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.crusher.get();
    }
}
