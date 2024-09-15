package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

/**
 * Block of the BarracksTower.
 */
public class BlockHutBarracksTower extends AbstractBlockHut<BlockHutBarracksTower>
{
    @Override
    public boolean isVisible(@Nullable final CompoundTag beData)
    {
        return false;
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.barracksTower.get();
    }
}
