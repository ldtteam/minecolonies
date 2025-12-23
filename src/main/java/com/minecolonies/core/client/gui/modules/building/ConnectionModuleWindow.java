package com.minecolonies.core.client.gui.modules.building;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.connections.ColonyConnection;
import com.minecolonies.api.colony.connections.DiplomacyStatus;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.AbstractBuildingWindow;
import com.minecolonies.core.commands.ClickEventWithExecutable;
import com.minecolonies.core.network.messages.server.colony.TeleportToColonyMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ConnectionModuleWindow extends AbstractBuildingWindow<IBuildingView>
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
     * If the player is part of the colony or external.
     */
    private final boolean externalPlayer;

    /**
     * Constructor for the minimum stock window view.
     *
     * @param buildingView the building view.
     */
    public ConnectionModuleWindow(final IBuildingView buildingView, final boolean externalPlayer)
    {
        super(buildingView, new ResourceLocation(Constants.MOD_ID, "gui/layouthuts/layoutcolonyconnection.xml"));

        if (externalPlayer)
        {
            for (final Pane child : children)
            {
                if (child.getID().contains("modules"))
                {
                    child.setVisible(false);
                }
            }
        }

        this.externalPlayer = externalPlayer;

        directConnections = findPaneOfTypeByID(LIST_DIRECT, ScrollingList.class);
        indirectConnections = findPaneOfTypeByID(LIST_INDIRECT, ScrollingList.class);

        directConnectionData = new ArrayList<>(buildingView.getColony().getConnectionManager().getDirectlyConnectedColonies().values());
        indirectConnectionData = new ArrayList<>(buildingView.getColony().getConnectionManager().getIndirectlyConnectedColonies().values());

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
        final int dist = (int) BlockPosUtil.dist(connectedColonyData.pos, buildingView.getPosition());
        final int itemCount = externalPlayer ? dist/125 : 0;

        MessageUtils.format("com.minecolonies.core.gui.colonylist.travel.really", connectedColonyData.name)
            .withPriority(MessageUtils.MessagePriority.IMPORTANT)
            .withClickEvent(new ClickEventWithExecutable(() -> Network.getNetwork().sendToServer(new TeleportToColonyMessage(mc.level.dimension(), connectedColonyData.id, connectedColonyData.pos, buildingView.getColony().getID(), itemCount))))
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

                final int dist = (int) BlockPosUtil.dist(colonyData.pos, buildingView.getPosition());
                final int itemCount = dist/125;
                final ItemIcon itemIcon = rowPane.findPaneOfTypeByID("icon", ItemIcon.class);
                if (externalPlayer)
                {
                    itemIcon.setItem(new ItemStack(Items.GOLD_NUGGET, itemCount));
                    itemIcon.show();
                }
                else
                {
                    itemIcon.hide();
                }

                final Button button = rowPane.findPaneOfTypeByID(TRAVEL, Button.class);
                if (externalPlayer && InventoryUtils.getItemCountInItemHandler(new InvWrapper(Minecraft.getInstance().player.getInventory()), Items.GOLD_NUGGET) <= itemCount)
                {
                    button.setEnabled(false);
                    PaneBuilders.tooltipBuilder().hoverPane(button).build().setText(Component.translatable("com.ldtteam.gatehouse.travel.cost"));
                }
                else
                {
                    button.setEnabled(colonyData.diplomacyStatus == DiplomacyStatus.ALLIES && !colonyData.pos.equals(BlockPos.ZERO));
                }
            }
        });
    }
}
