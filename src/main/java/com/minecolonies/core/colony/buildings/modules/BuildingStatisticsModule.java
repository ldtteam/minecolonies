package com.minecolonies.core.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import com.minecolonies.api.colony.managers.interfaces.IStatisticsManager;
import com.minecolonies.api.util.MathUtils;
import com.minecolonies.core.colony.managers.StatisticsManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Building statistic module.
 */
public class BuildingStatisticsModule extends AbstractBuildingModule implements IPersistentModule
{
    /**
     * List of all beds.
     */
    private IStatisticsManager statisticsManager = new StatisticsManager();

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        statisticsManager.readFromNBT(compound);
    }

    @Override
    public void serializeNBT(final CompoundTag compound)
    {
        statisticsManager.writeToNBT(compound);
    }

    @Override
    public void serializeToView(final FriendlyByteBuf buf, final boolean fullSync)
    {
        statisticsManager.serialize(buf, fullSync);
    }

    /**
     * Get the statistic manager of the building.
     * @return the manager.
     */
    public IStatisticsManager getBuildingStatisticsManager()
    {
        return statisticsManager;
    }

    /**
     * Helper method for incrementation of the stats.
     * @param s the stat id to increment.
     */
    public void increment(final String s)
    {
       statisticsManager.increment(s, building.getColony().getDay());
       if (MathUtils.RANDOM.nextInt(10) == 0)
       {
           markDirty();
       }
    }

    /**
     * Helper method for incrementation of the stats by a count.
     * @param s the stat id to increment.
     * @param count the count to increment it by.
     */
    public void incrementBy(final String s, final int count)
    {
        statisticsManager.incrementBy(s, count, building.getColony().getDay());
        if (MathUtils.RANDOM.nextInt(10) <= count)
        {
            markDirty();
        }
    }
}
