package com.minecolonies.coremod.client.gui.townhall;

import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.views.View;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingTownHall;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.*;

import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.coremod.client.gui.modules.WindowBuilderResModule.BLACK;

/**
 * BOWindow for the town hall.
 */
public class WindowHappinessPage extends AbstractWindowTownHall
{
    /**
     * Constructor for the town hall window.
     *
     * @param townHall {@link BuildingTownHall.View}.
     */
    public WindowHappinessPage(final BuildingTownHall.View townHall)
    {
        super(townHall, "layouthappiness.xml");
    }

    /**
     * Executed when <code>WindowTownHall</code> is opened. Does tasks like setting buttons.
     */
    @Override
    public void onOpened()
    {
        super.onOpened();
        updateHappiness();
    }

    /**
     * Update the display for the happiness
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
            label.setText(new TranslatableComponent("com.minecolonies.coremod.gui.townhall.happiness." + entry.getKey()));

            if (value > 1.0)
            {
                image.setImage(GREEN_ICON);
            }
            else if (value == 1)
            {
                image.setImage(BLUE_ICON);
            }
            else if (value > 0.75)
            {
                image.setImage(YELLOW_ICON);
            }
            else
            {
                image.setImage(RED_ICON);
            }
            pane.addChild(image);
            pane.addChild(label);
            PaneBuilders.tooltipBuilder().hoverPane(label).append(new TranslatableComponent("com.minecolonies.coremod.gui.townhall.happiness.desc." + entry.getKey())).build();

            yPos += 12;
        }
    }

    @Override
    protected String getWindowId()
    {
        return BUTTON_HAPPINESS;
    }
}
