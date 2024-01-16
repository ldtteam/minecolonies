package com.minecolonies.core.client.gui.modules;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.DropDownList;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.managers.interfaces.IStatisticsManager;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.AbstractModuleWindow;
import com.minecolonies.core.colony.buildings.moduleviews.BuildingStatisticsModuleView;
import com.minecolonies.core.colony.buildings.moduleviews.MinerLevelManagementModuleView;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.core.colony.buildings.modules.BuildingModules.STATS_MODULE;

/**
 * BOWindow for the miner hut.
 */
public class WindowStatsModule extends AbstractModuleWindow
{
    /**
     * Map of intervals.
     */
    private static final LinkedHashMap<String, Integer> INTERVAL = new LinkedHashMap<>();

    static
    {
        INTERVAL.put("com.minecolonies.coremod.gui.interval.yesterday", 1);
        INTERVAL.put("com.minecolonies.coremod.gui.interval.lastweek", 7);
        INTERVAL.put("com.minecolonies.coremod.gui.interval.100days", 100);
        INTERVAL.put("com.minecolonies.coremod.gui.interval.alltime", -1);
    }

    /**
     * Drop down list for interval.
     */
    private DropDownList intervalDropdown;

    /**
     * Current selected interval.
     */
    public String selectedInterval = "com.minecolonies.coremod.gui.interval.yesterday";

    /**
     * Util tags.
     */
    private static final String HUT_RESOURCE_SUFFIX = ":gui/layouthuts/layoutstatsmodule.xml";

    /**
     * Constructor for the window of the miner hut.
     *
     * @param moduleView {@link MinerLevelManagementModuleView}.
     */
    public WindowStatsModule(final IBuildingView building, final BuildingStatisticsModuleView moduleView)
    {
        super(building, Constants.MOD_ID + HUT_RESOURCE_SUFFIX);
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        updateStats();
    }

    /**
     * Update the display for the stats.
     */
    private void updateStats()
    {
        final IStatisticsManager statisticsManager = buildingView.getModuleView(STATS_MODULE).getBuildingStatisticsManager();
        final @NotNull List<String> stats = new ArrayList<>(statisticsManager.getStatTypes());
        findPaneOfTypeByID("stats", ScrollingList.class).setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return stats.size();
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                int stat = statisticsManager.getStatTotal(stats.get(index));
                int interval = INTERVAL.get(selectedInterval);
                if (interval > 0)
                {
                    stat = statisticsManager.getStatsInPeriod(stats.get(index), buildingView.getColony().getDay() - interval, buildingView.getColony().getDay());
                }

                final Text resourceLabel = rowPane.findPaneOfTypeByID("desc", Text.class);
                final String id = stats.get(index);
                if (id.contains(";"))
                {
                    final String[] split = id.split(";");
                    resourceLabel.setText(Component.translatable(PARTIAL_STATS_MODIFIER_NAME + split[0], stat, Component.translatable(split[1])));
                }
                else
                {
                    resourceLabel.setText(Component.translatable(PARTIAL_STATS_MODIFIER_NAME + id, stat));
                }
            }
        });

        intervalDropdown = findPaneOfTypeByID(DROPDOWN_INTERVAL_ID, DropDownList.class);
        intervalDropdown.setHandler(this::onDropDownListChanged);

        intervalDropdown.setDataProvider(new DropDownList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return INTERVAL.size();
            }

            @Override
            public String getLabel(final int index)
            {
                return Component.translatable((String) INTERVAL.keySet().toArray()[index]).getString();
            }
        });
        intervalDropdown.setSelectedIndex(new ArrayList<>(INTERVAL.keySet()).indexOf(selectedInterval));
    }

    private void onDropDownListChanged(final DropDownList dropDownList)
    {
        final String temp = (String) INTERVAL.keySet().toArray()[dropDownList.getSelectedIndex()];
        if (!temp.equals(selectedInterval))
        {
            selectedInterval = temp;
            updateStats();
        }
    }
}
