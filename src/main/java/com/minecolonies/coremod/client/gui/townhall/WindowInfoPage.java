package com.minecolonies.coremod.client.gui.townhall;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.colonyEvents.descriptions.IBuildingEventDescription;
import com.minecolonies.api.colony.colonyEvents.descriptions.ICitizenEventDescription;
import com.minecolonies.api.colony.colonyEvents.descriptions.IColonyEventDescription;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.coremod.colony.colonyEvents.citizenEvents.CitizenDiedEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;


import java.util.*;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * BOWindow for the town hall.
 */
public class WindowInfoPage extends AbstractWindowTownHall
{
    /**
     * The ScrollingList of the events.
     */
    private ScrollingList eventList;

    /**
     * Constructor for the town hall window.
     *
     * @param building {@link BuildingTownHall.View}.
     */
    public WindowInfoPage(final BuildingTownHall.View building)
    {
        super(building, "layoutinfo.xml");
    }

    /**
     * Executed when <code>WindowTownHall</code> is opened. Does tasks like setting buttons.
     */
    @Override
    public void onOpened()
    {
        super.onOpened();

        fillEventsList();
    }

    private void fillEventsList()
    {
        eventList = findPaneOfTypeByID(EVENTS_LIST, ScrollingList.class);
        eventList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return building.getColonyEvents().size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final Text nameLabel = rowPane.findPaneOfTypeByID(NAME_LABEL, Text.class);
                final Text actionLabel = rowPane.findPaneOfTypeByID(ACTION_LABEL, Text.class);

                final List<IColonyEventDescription> colonyEvents = building.getColonyEvents();
                Collections.reverse(colonyEvents);
                final IColonyEventDescription event = colonyEvents.get(index);
                if (event instanceof CitizenDiedEvent)
                {
                    actionLabel.setText(Component.literal(((CitizenDiedEvent) event).getDeathCause()));
                }
                else
                {
                    actionLabel.setText(Component.literal(event.getName()));
                }
                if (event instanceof ICitizenEventDescription)
                {
                    nameLabel.setText(Component.literal(((ICitizenEventDescription) event).getCitizenName()));
                }
                else if (event instanceof IBuildingEventDescription)
                {
                    IBuildingEventDescription buildEvent = (IBuildingEventDescription) event;
                    nameLabel.setText(MessageUtils.format(buildEvent.getBuildingName()).append(" " + buildEvent.getLevel()).create());
                }
                rowPane.findPaneOfTypeByID(POS_LABEL, Text.class)
                  .setText(Component.literal(event.getEventPos().getX() + " " + event.getEventPos().getY() + " " + event.getEventPos().getZ()));
                rowPane.findPaneOfTypeByID(BUTTON_ADD_PLAYER_OR_FAKEPLAYER, Button.class).hide();
            }
        });
    }

    @Override
    protected String getWindowId()
    {
        return BUTTON_INFOPAGE;
    }
}