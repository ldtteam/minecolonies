package com.minecolonies.core.client.gui.townhall;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.CompactColonyReference;
import com.minecolonies.api.colony.managers.interfaces.IColonyConnectionManager;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.MessageUtils.MessagePriority;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.Network;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.core.colony.managers.ColonyConnectionManager;
import com.minecolonies.core.commands.ClickEventWithExecutable;
import com.minecolonies.core.network.messages.server.colony.TeleportToColonyMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.TranslationConstants.DO_REALLY_WANNA_TP;
import static com.minecolonies.api.util.constant.TranslationConstants.TH_TOO_LOW;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * BOWindow for the town hall ally list.
 */
public class WindowAlliancePage extends AbstractWindowTownHall
{
    private static final String REQUEST_ALLY = "requestally";
    private static final String START_FEUD = "startfeud";
    private static final String SET_NEUTRAL = "setneutral";

    /**
     * Special buttons
     */
    private static final String TELEPORT      = "tleport";
    private static final String LIST_DIRECT = "directcolonylist";
    private static final String LIST_INDIRECT = "indirectcolonylist";

    /**
     * Scrollinglists of connections.
     */
    private final ScrollingList directConnections;
    private final ScrollingList indirectConnections;

    /**
     * Lists with the data from connections.
     */
    private final List<IColonyConnectionManager.ConnectedColonyData> directConnectionData;
    private final List<IColonyConnectionManager.ConnectedColonyData> indirectConnectionData;

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

        directConnectionData = new ArrayList<>(building.getColony().getConnectionManager().getDirectlyConnectedColonies().values());
        indirectConnectionData = new ArrayList<>(building.getColony().getConnectionManager().getIndirectlyConnectedColonies().values());

        registerButton(REQUEST_ALLY, this::requestAlly);
        registerButton(START_FEUD, this::startFeud);
        registerButton(SET_NEUTRAL, this::setNeutral);

        updateConnections(directConnections, directConnectionData);
        updateConnections(indirectConnections, indirectConnectionData);
    }


    private void setNeutral(@NotNull final Button button)
    {
        final IColonyConnectionManager.ConnectedColonyData connectedColonyData = getColonyDataFromPane(button);

    }

    private void startFeud(@NotNull final Button button)
    {
        final IColonyConnectionManager.ConnectedColonyData connectedColonyData = getColonyDataFromPane(button);

    }

    private void requestAlly(@NotNull final Button button)
    {
        final IColonyConnectionManager.ConnectedColonyData connectedColonyData = getColonyDataFromPane(button);

    }

    private IColonyConnectionManager.ConnectedColonyData getColonyDataFromPane(final @NotNull Button button)
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


    /**
     * Updates the colony list.
     */
    private void updateConnections(final ScrollingList connectionScrollList, final List<IColonyConnectionManager.ConnectedColonyData> connectionData)
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
                final IColonyConnectionManager.ConnectedColonyData colonyData = connectionData.get(index);
                rowPane.findPaneOfTypeByID("name", Text.class).setText(Component.literal(colonyData.name()));
                rowPane.findPaneOfTypeByID("distance", Text.class)
                    .setText(Component.translatable("com.minecolonies.coremod.dist.blocks", (int) BlockPosUtil.dist(colonyData.pos(), buildingView.getColony().getCenter())));
                rowPane.findPaneOfTypeByID("state", Text.class).setText(Component.translatable(colonyData.diplomacyStatus().translationKey()));

                rowPane.findPaneOfTypeByID("requestally", Button.class).setVisible(colonyData.diplomacyStatus() == ColonyConnectionManager.DiplomacyStatus.NEUTRAL);
                rowPane.findPaneOfTypeByID("startfeud", Button.class).setVisible(colonyData.diplomacyStatus() == ColonyConnectionManager.DiplomacyStatus.NEUTRAL);
                rowPane.findPaneOfTypeByID("setneutral", Button.class).setVisible(colonyData.diplomacyStatus() != ColonyConnectionManager.DiplomacyStatus.NEUTRAL);
            }
        });
    }

    @Override
    protected String getWindowId()
    {
        return BUTTON_ALLIANCE;
    }
}
