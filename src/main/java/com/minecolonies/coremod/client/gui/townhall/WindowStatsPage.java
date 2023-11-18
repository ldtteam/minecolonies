package com.minecolonies.coremod.client.gui.townhall;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.views.DropDownList;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.blockui.views.View;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingTownHall;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

import static com.minecolonies.api.util.constant.TranslationConstants.PARTIAL_HAPPINESS_MODIFIER_NAME;
import static com.minecolonies.api.util.constant.TranslationConstants.PARTIAL_STATS_MODIFIER_NAME;
import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.coremod.client.gui.modules.WindowBuilderResModule.BLACK;

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
        updateHappiness();
        updateStats();
    }

    /**
     * Update the display for the happiness.
     */
    private void updateHappiness()
    {
        final Map<String, Double> happinessMap = new HashMap<>();

        for (final ICitizenDataView data : building.getColony().getCitizens().values())
        {
            for (final String modifier : data.getHappinessHandler().getModifiers())
            {
                happinessMap.put(modifier, happinessMap.getOrDefault(modifier, 0.0) + data.getHappinessHandler().getModifier(modifier).getFactor(null));
            }
        }

        final DecimalFormat df = new DecimalFormat("#.#");
        df.setRoundingMode(RoundingMode.CEILING);

        final String roundedHappiness = df.format(building.getColony().getOverallHappiness());

        final View pane = findPaneOfTypeByID("happinesspage", View.class);
        final Text titleLabel = new Text();
        titleLabel.setSize(136, 11);
        titleLabel.setPosition(25, 42);
        titleLabel.setColors(BLACK);
        titleLabel.setText(Component.translatable("com.minecolonies.coremod.gui.townhall.currenthappiness", roundedHappiness));
        pane.addChild(titleLabel);

        int yPos = 60;
        for (final Map.Entry<String, Double> entry : happinessMap.entrySet())
        {
            final double value = entry.getValue() / building.getColony().getCitizenCount();
            final Image image = new Image();
            image.setSize(11, 11);
            image.setPosition(0, yPos);

            final Text label = new Text();
            label.setSize(136, 11);
            label.setPosition(25, yPos);
            label.setColors(BLACK);
            label.setText(Component.translatable(PARTIAL_HAPPINESS_MODIFIER_NAME + entry.getKey()));

            if (value > 1.0)
            {
                image.setImage(new ResourceLocation(HAPPY_ICON), false);
            }
            else if (value == 1)
            {
                image.setImage(new ResourceLocation(SATISFIED_ICON), false);
            }
            else if (value > 0.75)
            {
                image.setImage(new ResourceLocation(UNSATISFIED_ICON), false);
            }
            else
            {
                image.setImage(new ResourceLocation(UNHAPPY_ICON), false);
            }
            pane.addChild(image);
            pane.addChild(label);
            PaneBuilders.tooltipBuilder().hoverPane(label).append(Component.translatable("com.minecolonies.coremod.gui.townhall.happiness.desc." + entry.getKey())).build();

            yPos += 12;
        }
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
                resourceLabel.setText(Component.translatable(PARTIAL_STATS_MODIFIER_NAME + stats.get(index), stat));
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

    @Override
    protected String getWindowId()
    {
        return BUTTON_STATS;
    }
}
