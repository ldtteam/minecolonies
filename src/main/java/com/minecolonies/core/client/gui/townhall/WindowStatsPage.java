package com.minecolonies.core.client.gui.townhall;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.views.DropDownList;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.CitizenConstants;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.colony.buildings.moduleviews.CombinedHiringLimitModuleView;
import com.minecolonies.core.colony.buildings.moduleviews.WorkerBuildingModuleView;
import com.minecolonies.core.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingTownHall;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.research.util.ResearchConstants.CITIZEN_CAP;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_TOWNHALL_POPULATION_CHILDS;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * BOWindow for the town hall.
 */
public class WindowStatsPage extends AbstractWindowTownHall
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
     * Constructor for the town hall window.
     *
     * @param townHall {@link BuildingTownHall.View}.
     */
    public WindowStatsPage(final BuildingTownHall.View townHall)
    {
        super(townHall, "layoutstats.xml");
    }

    /**
     * Executed when <code>WindowTownHall</code> is opened. Does tasks like setting buttons.
     */
    @Override
    public void onOpened()
    {
        super.onOpened();
        updateStats();
        createAndSetStatistics();
    }

    /**
     *
     * Creates several statistics and sets them in the building GUI.
     */
    private void createAndSetStatistics()
    {
        final int citizensSize = building.getColony().getCitizens().size();
        final int citizensCap;

        if (MinecoloniesAPIProxy.getInstance().getGlobalResearchTree().hasResearchEffect(CITIZEN_CAP))
        {
            final int max = Math.max(CitizenConstants.CITIZEN_LIMIT_DEFAULT, (int) this.building.getColony().getResearchManager().getResearchEffects().getEffectStrength(CITIZEN_CAP));
            citizensCap = Math.min(max, MineColonies.getConfig().getServer().maxCitizenPerColony.get());
        }
        else
        {
            citizensCap = MineColonies.getConfig().getServer().maxCitizenPerColony.get();
        }

        final Text totalCitizenLabel = findPaneOfTypeByID(TOTAL_CITIZENS_LABEL, Text.class);
        totalCitizenLabel.setText(Component.translatableEscape(COM_MINECOLONIES_COREMOD_GUI_TOWNHALL_POPULATION_TOTALCITIZENS_COUNT,
          citizensSize,
          Math.max(citizensSize, building.getColony().getCitizenCountLimit())));
        List<MutableComponent> hoverText = new ArrayList<>();
        if(citizensSize < (citizensCap * 0.9) && citizensSize < (building.getColony().getCitizenCountLimit() * 0.9))
        {
            totalCitizenLabel.setColors(DARKGREEN);
        }
        else if(citizensSize < citizensCap)
        {
            hoverText.add(Component.translatableEscape(WARNING_POPULATION_NEEDS_HOUSING, this.building.getColony().getName()));
            totalCitizenLabel.setColors(ORANGE);
        }
        else
        {
            if(citizensCap < MineColonies.getConfig().getServer().maxCitizenPerColony.get())
            {
                hoverText.add(Component.translatableEscape(WARNING_POPULATION_RESEARCH_LIMITED, this.building.getColony().getName()));
            }
            else
            {
                hoverText.add(Component.translatableEscape( WARNING_POPULATION_CONFIG_LIMITED, this.building.getColony().getName()));
            }
            totalCitizenLabel.setText(Component.translatableEscape(COM_MINECOLONIES_COREMOD_GUI_TOWNHALL_POPULATION_TOTALCITIZENS_COUNT, citizensSize, citizensCap));
            totalCitizenLabel.setColors(RED);
        }
        PaneBuilders.tooltipBuilder().hoverPane(totalCitizenLabel).build().setText(hoverText);

        int children = 0;
        final Map<String, Tuple<Integer, Integer>> jobMaxCountMap = new HashMap<>();
        for (@NotNull final IBuildingView building : building.getColony().getBuildings())
        {
            if (building instanceof AbstractBuildingView)
            {
                for (final WorkerBuildingModuleView module : building.getModuleViews(WorkerBuildingModuleView.class))
                {
                    int alreadyAssigned = -1;
                    if (module instanceof CombinedHiringLimitModuleView)
                    {
                        alreadyAssigned = 0;
                        for (final WorkerBuildingModuleView combinedModule : building.getModuleViews(WorkerBuildingModuleView.class))
                        {
                            alreadyAssigned += combinedModule.getAssignedCitizens().size();
                        }
                    }
                    int max = module.getMaxInhabitants();
                    if (alreadyAssigned != -1)
                    {
                        max -= alreadyAssigned;
                        max += module.getAssignedCitizens().size();
                    }
                    int workers = module.getAssignedCitizens().size();

                    final String jobName = module.getJobDisplayName().toLowerCase(Locale.US);

                    final Tuple<Integer, Integer> tuple = jobMaxCountMap.getOrDefault(jobName, new Tuple<>(0, 0));
                    jobMaxCountMap.put(jobName, new Tuple<>(tuple.getA() + workers, tuple.getB() + max));
                }
            }
        }

        //calculate number of children
        int unemployed = 0;
        for (ICitizenDataView iCitizenDataView : building.getColony().getCitizens().values())
        {
            if (iCitizenDataView.isChild())
            {
                children++;
            }
            else if (iCitizenDataView.getJobView() == null)
            {
                unemployed++;
            }
        }

        final int childCount = children;
        final int unemployedCount = unemployed;

        final ScrollingList list = findPaneOfTypeByID("citizen-stats", ScrollingList.class);
        if (list == null)
        {
            return;
        }

        final int maxJobs = jobMaxCountMap.size();
        final List<Map.Entry<String, Tuple<Integer, Integer>>> theList = new ArrayList<>(jobMaxCountMap.entrySet());
        theList.sort(Map.Entry.comparingByKey());

        list.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return maxJobs + 2;
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final Text label = rowPane.findPaneOfTypeByID(CITIZENS_AMOUNT_LABEL, Text.class);
                // preJobsHeaders = number of all unemployed citizens

                if (index < theList.size())
                {
                    final Map.Entry<String, Tuple<Integer, Integer>> entry = theList.get(index);
                    final String jobString = Component.translatableEscape(entry.getKey()).getString();
                    final String formattedJobString = jobString.substring(0, 1).toUpperCase(Locale.US) + jobString.substring(1);

                    final Component numberOfWorkers = Component.translatableEscape(COM_MINECOLONIES_COREMOD_GUI_TOWNHALL_POPULATION_EACH, formattedJobString, entry.getValue().getA(), entry.getValue().getB());
                    label.setText(numberOfWorkers);
                }
                else
                {
                    if (index == maxJobs + 1)
                    {
                        label.setText(Component.translatableEscape(COM_MINECOLONIES_COREMOD_GUI_TOWNHALL_POPULATION_UNEMPLOYED, unemployedCount));
                    }
                    else
                    {
                        label.setText(Component.translatableEscape(COM_MINECOLONIES_COREMOD_GUI_TOWNHALL_POPULATION_CHILDS, childCount));
                    }
                }
            }
        });
    }

    /**
     * Update the display for the stats.
     */
    private void updateStats()
    {
        final @NotNull List<String> stats = new ArrayList<>(building.getColony().getStatisticsManager().getStatTypes());

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
                int stat = building.getColony().getStatisticsManager().getStatTotal(stats.get(index));
                int interval = INTERVAL.get(selectedInterval);
                if (interval > 0)
                {
                    stat = building.getColony().getStatisticsManager().getStatsInPeriod(stats.get(index), building.getColony().getDay() - interval, building.getColony().getDay());
                }

                final Text resourceLabel = rowPane.findPaneOfTypeByID("desc", Text.class);
                resourceLabel.setText(Component.translatableEscape(PARTIAL_STATS_MODIFIER_NAME + stats.get(index), stat));
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
            public MutableComponent getLabel(final int index)
            {
                return Component.translatableEscape((String) INTERVAL.keySet().toArray()[index]);
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

    @Override
    protected String getWindowId()
    {
        return BUTTON_STATS;
    }
}
