package com.minecolonies.core.client.gui.townhall;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.connections.*;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.core.Network;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.core.colony.managers.ColonyConnectionManager;
import com.minecolonies.core.network.messages.server.colony.TriggerConnectionEventMessage;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * BOWindow for the town hall ally list.
 */
public class WindowAlliancePage extends AbstractWindowTownHall
{
    /**
     * Buttons to alter ally status on colony list.
     */
    private static final String REQUEST_ALLY = "requestally";
    private static final String START_FEUD = "startfeud";
    private static final String SET_NEUTRAL = "setneutral";

    // Button to accept ally request.
    private static final String ACCEPT_ALLY = "acceptally";

    /**
     * Special buttons
     */
    private static final String LIST_DIRECT = "directcolonylist";
    private static final String LIST_INDIRECT = "indirectcolonylist";
    private static final String LIST_EVENTS = "connectioneventlist";

    /**
     * Scrollinglists of connections.
     */
    private final ScrollingList directConnections;
    private final ScrollingList indirectConnections;

    private final ScrollingList connectionEvents;

    /**
     * Lists with the data from connections.
     */
    private List<ColonyConnection> directConnectionData;
    private List<ColonyConnection> indirectConnectionData;

    /**
     * Constructor for the town hall window.
     *
     * @param building {@link BuildingTownHall.View}.
     */
    public WindowAlliancePage(final BuildingTownHall.View building)
    {
        super(building, "layoutalliance.xml");

        directConnections = findPaneOfTypeByID(LIST_DIRECT, ScrollingList.class);
        indirectConnections = findPaneOfTypeByID(LIST_INDIRECT, ScrollingList.class);
        connectionEvents = findPaneOfTypeByID(LIST_EVENTS, ScrollingList.class);

        directConnectionData = new ArrayList<>(building.getColony().getConnectionManager().getDirectlyConnectedColonies().values());
        indirectConnectionData = new ArrayList<>(building.getColony().getConnectionManager().getIndirectlyConnectedColonies().values());

        registerButton(REQUEST_ALLY, this::requestAlly);
        registerButton(START_FEUD, this::startFeud);
        registerButton(SET_NEUTRAL, this::setNeutral);
        registerButton(ACCEPT_ALLY, this::acceptAlly);

        updateConnections(directConnections, directConnectionData);
        updateConnections(indirectConnections, indirectConnectionData);
        updateEvents();
    }

    private void setNeutral(@NotNull final Button button)
    {
        final ColonyConnection connectedColonyData = getColonyDataFromPane(button);
        Network.getNetwork().sendToServer(new TriggerConnectionEventMessage(building.getColony(), new ConnectionEvent(building.getColony().getID(), building.getColony().getName(),
            ConnectionEventType.NEUTRAL_SET), connectedColonyData.id));
    }

    private void startFeud(@NotNull final Button button)
    {
        final ColonyConnection connectedColonyData = getColonyDataFromPane(button);
        Network.getNetwork().sendToServer(new TriggerConnectionEventMessage(building.getColony(), new ConnectionEvent(building.getColony().getID(), building.getColony().getName(),
            ConnectionEventType.FEUD_STARTED), connectedColonyData.id));
    }

    private void requestAlly(@NotNull final Button button)
    {
        final ColonyConnection connectedColonyData = getColonyDataFromPane(button);
        Network.getNetwork().sendToServer(new TriggerConnectionEventMessage(building.getColony(), new ConnectionEvent(building.getColony().getID(), building.getColony().getName(),
            ConnectionEventType.ALLY_REQUEST), connectedColonyData.id));
    }

    private void acceptAlly(@NotNull final Button button)
    {
        final ConnectionEvent connectedColonyData = building.getColony().getConnectionManager().getConnectionEvents().get(connectionEvents.getListElementIndexByPane(button));
        Network.getNetwork().sendToServer(new TriggerConnectionEventMessage(building.getColony(), new ConnectionEvent(building.getColony().getID(), building.getColony().getName(),
            ConnectionEventType.ALLY_CONFIRMED), connectedColonyData.id()));
    }

    private ColonyConnection getColonyDataFromPane(final @NotNull Button button)
    {
        final int directRow = directConnections.getListElementIndexByPane(button);
        if (directRow != -1)
        {
            return directConnectionData.get(directRow);
        }
        else
        {
            final int indirectRow = indirectConnections.getListElementIndexByPane(button);
            return indirectConnectionData.get(indirectRow);
        }
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        directConnectionData = new ArrayList<>(building.getColony().getConnectionManager().getDirectlyConnectedColonies().values());
        indirectConnectionData = new ArrayList<>(building.getColony().getConnectionManager().getIndirectlyConnectedColonies().values());
        updateConnections(directConnections, directConnectionData);
        updateConnections(indirectConnections, indirectConnectionData);
        updateEvents();
    }

    /**
     * Updates the colony list.
     */
    private void updateConnections(final ScrollingList connectionScrollList, final List<ColonyConnection> connectionData)
    {
        connectionScrollList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             *
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return connectionData.size();
            }

            /**
             * Inserts the elements into each row.
             *
             * @param index   the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final ColonyConnection colonyData = connectionData.get(index);
                rowPane.findPaneOfTypeByID("name", Text.class).setText(Component.literal(colonyData.name));
                rowPane.findPaneOfTypeByID("distance", Text.class)
                    .setText(Component.translatable("com.minecolonies.coremod.dist.blocks", (int) BlockPosUtil.dist(colonyData.pos, buildingView.getColony().getCenter())));
                rowPane.findPaneOfTypeByID("state", Text.class).setText(Component.translatable(colonyData.diplomacyStatus.translationKey()));

                rowPane.findPaneOfTypeByID("requestally", Button.class).setVisible(colonyData.diplomacyStatus == DiplomacyStatus.NEUTRAL);
                rowPane.findPaneOfTypeByID("startfeud", Button.class).setVisible(colonyData.diplomacyStatus == DiplomacyStatus.NEUTRAL);
                rowPane.findPaneOfTypeByID("setneutral", Button.class).setVisible(colonyData.diplomacyStatus != DiplomacyStatus.NEUTRAL);
            }
        });
    }

    /**
     * Updates the colony list.
     */
    private void updateEvents()
    {
        connectionEvents.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             *
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return building.getColony().getConnectionManager().getConnectionEvents().size();
            }

            /**
             * Inserts the elements into each row.
             *
             * @param index   the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final List<ConnectionEvent> list = building.getColony().getConnectionManager().getConnectionEvents();
                final int revIndex = list.size() - 1 - index;
                final ConnectionEvent eventData = list.get(revIndex);
                final DiplomacyStatus diplomacyStatus = building.getColony().getConnectionManager().getColonyDiplomacyStatus(eventData.id());
                rowPane.findPaneOfTypeByID("name", Text.class).setText(Component.literal(eventData.name()));
                rowPane.findPaneOfTypeByID("desc", Text.class).setText(Component.translatable(eventData.connectionEventType().translationKey()));

                rowPane.findPaneOfTypeByID("acceptally", Button.class).setVisible(eventData.connectionEventType() == ConnectionEventType.ALLY_REQUEST && diplomacyStatus != DiplomacyStatus.ALLIES);
            }
        });
    }


    @Override
    protected String getWindowId()
    {
        return BUTTON_ALLIANCE;
    }
}
