package com.minecolonies.coremod.client.gui.townhall;

import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.views.View;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingTownHall;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

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
                happinessMap.put(modifier, happinessMap.getOrDefault(modifier, 0.0) + data.getHappinessHandler().getModifier(modifier).getFactor());
            }
        }

        final View pane = findPaneOfTypeByID("happinesspage", View.class);
        final Text titleLabel = new Text();
        titleLabel.setSize(136, 11);
        titleLabel.setPosition(25, 42);
        titleLabel.setColors(BLACK);
        titleLabel.setText(Component.translatable("com.minecolonies.coremod.gui.townhall.currenthappiness"));
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
                image.setImage(new ResourceLocation(GREEN_ICON), false);
            }
            else if (value == 1)
            {
                image.setImage(new ResourceLocation(BLUE_ICON), false);
            }
            else if (value > 0.75)
            {
                image.setImage(new ResourceLocation(YELLOW_ICON), false);
            }
            else
            {
                image.setImage(new ResourceLocation(RED_ICON), false);
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
        final View pane = findPaneOfTypeByID("statspage", View.class);
        int yPos = 65;

        for (final String entry : building.getColony().getStatisticsManager().getStatTypes())
        {
            final Text label = new Text();
            label.setSize(136, 11);
            label.setPosition(25, yPos);
            label.setColors(BLACK);
            label.setText(Component.translatable(PARTIAL_STATS_MODIFIER_NAME + entry, building.getColony().getStatisticsManager().getStatTotal(entry)));
            pane.addChild(label);

            yPos += 12;
        }
    }

    @Override
    protected String getWindowId()
    {
        return BUTTON_STATS;
    }
}
