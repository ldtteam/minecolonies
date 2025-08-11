package com.minecolonies.core.colony.buildings.modules;

import java.util.List;
import com.ldtteam.structurize.api.util.Log;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.managers.interfaces.IStatisticsManager;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingBarracks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import static com.minecolonies.core.colony.buildings.modules.BuildingModules.STATS_MODULE;

/**
 * Building statistic module.
 */
public class BarracksStatisticsModule extends BuildingStatisticsModule
{
    /**
     * Serializes the composite stats of all towers associated with this barracks
     * 
     * @param buf      the buffer to write to
     * @param fullSync whether to serialize the full stats or only the dirty ones
     */
    @Override
    public void serializeToView(final FriendlyByteBuf buf, final boolean fullSync)
    {
        this.getBuildingStatisticsManager().clear();
        List<BlockPos> towers = ((BuildingBarracks) building).getTowers();

        for (final BlockPos towerPos : towers)
        {
            IBuilding tower = building.getColony().getBuildingManager().getBuilding(towerPos);
            if (tower != null)
            {
                BuildingStatisticsModule towerStats = tower.getModule(STATS_MODULE);
                IStatisticsManager.aggregateStats(this.getBuildingStatisticsManager(), towerStats.getBuildingStatisticsManager());
            }
        }

        this.getBuildingStatisticsManager().serialize(buf, fullSync);
    }
}
