package com.minecolonies.core.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.api.colony.managers.interfaces.IStatisticsManager;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.modules.WindowStatsModule;
import com.minecolonies.core.colony.managers.StatisticsManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

/**
 * Building statistic module.
 */
public class BuildingStatisticsModuleView extends AbstractBuildingModuleView
{
    /**
     * List of all beds.
     */
    private IStatisticsManager statisticsManager = new StatisticsManager();

    @Override
    public void deserialize(final @NotNull FriendlyByteBuf buf)
    {
        statisticsManager.deserialize(buf);
    }

    @Override
    public BOWindow getWindow()
    {
        return new WindowStatsModule(getBuildingView(), this);
    }

    @Override
    public ResourceLocation getIconResourceLocation()
    {
        return new ResourceLocation(Constants.MOD_ID, "textures/gui/modules/stats.png");
    }

    @Override
    public String getDesc()
    {
        return "com.minecolonies.core.gui.modules.stats";
    }

    /**
     * Get the statistic manager of the building.
     * @return the manager.
     */
    public IStatisticsManager getBuildingStatisticsManager()
    {
        return statisticsManager;
    }
}
