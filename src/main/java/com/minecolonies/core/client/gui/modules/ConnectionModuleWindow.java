package com.minecolonies.core.client.gui.modules;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.connections.ColonyConnection;
import com.minecolonies.api.colony.connections.DiplomacyStatus;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.AbstractModuleWindow;
import com.minecolonies.core.colony.buildings.moduleviews.ColonyConnectionModuleView;
import com.minecolonies.core.colony.managers.ColonyConnectionManager;
import com.minecolonies.core.commands.ClickEventWithExecutable;
import com.minecolonies.core.network.messages.server.colony.TeleportToColonyMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ConnectionModuleWindow extends AbstractModuleWindow
{
    /**
     * Special buttons
     */
    private static final String TRAVEL      = "travel";
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
    private final List<ColonyConnection> directConnectionData;
    private final List<ColonyConnection> indirectConnectionData;

    /**
     * Constructor for the minimum stock window view.
     *
     * @param building class extending
     * @param moduleView the module view.
     */
    public ConnectionModuleWindow(final String res, final IBuildingView building, final ColonyConnectionModuleView moduleView)
    {
        super(building, res);

        directConnections = findPaneOfTypeByID(LIST_DIRECT, ScrollingList.class);
        indirectConnections = findPaneOfTypeByID(LIST_INDIRECT, ScrollingList.class);

        directConnectionData = new ArrayList<>(building.getColony().getConnectionManager().getDirectlyConnectedColonies().values());
        indirectConnectionData = new ArrayList<>(building.getColony().getConnectionManager().getIndirectlyConnectedColonies().values());

        registerButton(TRAVEL, this::teleportToColony);

        updateConnections(directConnections, directConnectionData);
        updateConnections(indirectConnections, indirectConnectionData);
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

    private void teleportToColony(@NotNull final Button button)
    {
        final ColonyConnection connectedColonyData = getColonyDataFromPane(button);

        MessageUtils.format("com.minecolonies.core.gui.colonylist.travel.really", connectedColonyData.name)
            .withPriority(MessageUtils.MessagePriority.IMPORTANT)
            .withClickEvent(new ClickEventWithExecutable(() -> Network.getNetwork().sendToServer(new TeleportToColonyMessage(mc.level.dimension(), connectedColonyData.id, connectedColonyData.pos))))
            .sendTo(Minecraft.getInstance().player);
        this.close();
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
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return connectionData.size();
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final ColonyConnection colonyData = connectionData.get(index);
                rowPane.findPaneOfTypeByID("name", Text.class).setText(Component.literal(colonyData.name));
                rowPane.findPaneOfTypeByID("distance", Text.class).setText(Component.translatable("com.minecolonies.coremod.dist.blocks", (int) BlockPosUtil.dist(colonyData.pos, buildingView.getColony().getCenter())));
                rowPane.findPaneOfTypeByID("state", Text.class).setText(Component.translatable(colonyData.diplomacyStatus.translationKey()));

                rowPane.findPaneOfTypeByID(TRAVEL, Button.class).setEnabled(colonyData.diplomacyStatus == DiplomacyStatus.ALLIES
                    && !colonyData.pos.equals(BlockPos.ZERO));
            }
        });
    }
}
