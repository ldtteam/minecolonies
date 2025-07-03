package com.minecolonies.core.client.gui.modules;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ButtonImage;
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
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * BOWindow for the miner hut.
 */
public class WindowStatsModule extends AbstractModuleWindow
{
    /**
     * Map of intervals.
     */
    private static final LinkedHashMap<String, Integer> INTERVAL = new LinkedHashMap<>();

    /**
     * ID of the assign button inside the GUI.
     */
    private static final String TAG_BUTTON_HIDEZERO = "hidezero";

    /**
     * Texture of the assign button when it's on.
     */
    private static final String TEXTURE_ASSIGN_ON_NORMAL = "minecolonies:textures/gui/builderhut/builder_button_mini_check.png";

    /**
     * Texture of the assign button when it's on and disabled.
     */
    private static final String TEXTURE_ASSIGN_ON_DISABLED = "minecolonies:textures/gui/builderhut/builder_button_mini_disabled_check.png";

    /**
     * Texture of the assign button when it's off.
     */
    private static final String TEXTURE_ASSIGN_OFF_NORMAL = "minecolonies:textures/gui/builderhut/builder_button_mini.png";

    /**
     * Texture of the assign button when it's off and disabled.
     */
    private static final String TEXTURE_ASSIGN_OFF_DISABLED = "minecolonies:textures/gui/builderhut/builder_button_mini_disabled.png";

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

    /*
     * Module view
     */
    private BuildingStatisticsModuleView moduleView = null;

    /*
     * Flag to indicate whether recorded stats with no occurrence 
     * within the filtered interval should be hidden.
     * Useful on buildings with a high number of stats (like the builder).
     */
    private boolean hideZeroStats = false;

    /**
     * Constructor for the window of the miner hut.
     *
     * @param moduleView {@link MinerLevelManagementModuleView}.
     */
    public WindowStatsModule(final IBuildingView building, final BuildingStatisticsModuleView moduleView)
    {
        super(building, Constants.MOD_ID + HUT_RESOURCE_SUFFIX);
        this.moduleView = moduleView;
        registerButton(TAG_BUTTON_HIDEZERO, this::hideZeroClicked);
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
        final IStatisticsManager statisticsManager = moduleView.getBuildingStatisticsManager();
        final @NotNull List<String> stats = new ArrayList<>(statisticsManager.getStatTypes());
        findPaneOfTypeByID("stats", ScrollingList.class).setDataProvider(new ScrollingList.DataProvider()
        {

            private List <String> filteredStats = new ArrayList<>();

            {
                int interval = INTERVAL.get(selectedInterval);

                if (hideZeroStats) 
                {
                    for (int i = 0; i < stats.size(); i++) 
                    {
                        if (interval > 0)
                        {
                            if (statisticsManager.getStatsInPeriod(stats.get(i), buildingView.getColony().getDay() - interval, buildingView.getColony().getDay()) > 0)
                            {
                                filteredStats.add(stats.get(i));
                            }
                        } 
                        else
                        {
                            if (statisticsManager.getStatTotal(stats.get(i)) > 0)
                            {
                                filteredStats.add(stats.get(i));
                            }
                        }
                    }
                } 
                else 
                {
                    filteredStats.addAll(stats);
                }
            }

            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {  

                
                return filteredStats.size();
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                int stat = statisticsManager.getStatTotal(filteredStats.get(index));
                int interval = INTERVAL.get(selectedInterval);
                if (interval > 0)
                {
                    stat = statisticsManager.getStatsInPeriod(filteredStats.get(index), buildingView.getColony().getDay() - interval, buildingView.getColony().getDay());
                }

                final Text resourceLabel = rowPane.findPaneOfTypeByID("desc", Text.class);
                final String id = filteredStats.get(index);
                if (id.contains(";"))
                {
                    final String[] split = id.split(";");
                    if (id.contains("'"))
                    {
                        //todo remove in 1.20.4
                        final String[] split2 = split[1].split("'");
                        resourceLabel.setText(Component.translatable(PARTIAL_STATS_MODIFIER_NAME + split[0], stat, Component.translatable(split2[1])));
                    }
                    else
                    {
                        resourceLabel.setText(Component.translatable(PARTIAL_STATS_MODIFIER_NAME + split[0], stat, Component.translatable(split[1])));
                    }
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

    /**
     * Fired when assign has been clicked in the field list.
     *
     * @param button clicked button.
     */
    private void hideZeroClicked(@NotNull final Button button)
    {
        final ButtonImage hideButton = findPaneOfTypeByID(TAG_BUTTON_HIDEZERO, ButtonImage.class);
        hideZeroStats = !hideZeroStats;

        if (hideZeroStats)
        {
            hideButton.setImage(new ResourceLocation(TEXTURE_ASSIGN_ON_NORMAL), true);
            hideButton.setImageDisabled(new ResourceLocation(TEXTURE_ASSIGN_ON_DISABLED), true);
        }
        else
        {
            hideButton.setImage(new ResourceLocation(TEXTURE_ASSIGN_OFF_NORMAL), true);
            hideButton.setImageDisabled(new ResourceLocation(TEXTURE_ASSIGN_OFF_DISABLED), true);
        }

        updateStats();
    }
}
