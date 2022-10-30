package com.minecolonies.coremod.client.gui.townhall;

import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.views.View;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingTownHall;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.minecolonies.api.util.constant.TranslationConstants.PARTIAL_HAPPINESS_MODIFIER_NAME;
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
        int yPos = 62;
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
            label.setText(new TranslatableComponent(PARTIAL_HAPPINESS_MODIFIER_NAME + entry.getKey()));

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
            PaneBuilders.tooltipBuilder().hoverPane(label).append(new TranslatableComponent("com.minecolonies.coremod.gui.townhall.happiness.desc." + entry.getKey())).build();

            yPos += 12;
        }
    }

    /**
     * Update the display for the stats.
     */
    private void updateStats()
    {
        for (final Map.Entry<String, List<Long>> entry : building.getColony().getStatisticsManager().getStats().entrySet())
        {

        }
    }

    @Override
    protected String getWindowId()
    {
        return BUTTON_STATS;
    }
}
