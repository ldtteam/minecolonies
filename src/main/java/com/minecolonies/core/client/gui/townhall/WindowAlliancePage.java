package com.minecolonies.core.client.gui.townhall;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.CompactColonyReference;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.Network;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.core.commands.ClickEventWithExecutable;
import com.minecolonies.core.network.messages.server.colony.TeleportToColonyMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * BOWindow for the town hall ally list.
 */
public class WindowAlliancePage extends AbstractWindowTownHall
{
    /**
     * The ScrollingList of all feuds.
     */
    private final ScrollingList feudsList;
    /**
     * The ScrollingList of all allies.
     */
    private final ScrollingList alliesList;

    /**
     * Constructor for the town hall window.
     *
     * @param building {@link BuildingTownHall.View}.
     */
    public WindowAlliancePage(final BuildingTownHall.View building)
    {
        super(building, "layoutalliance.xml");

        alliesList = findPaneOfTypeByID(LIST_ALLIES, ScrollingList.class);
        feudsList = findPaneOfTypeByID(LIST_FEUDS, ScrollingList.class);

        registerButton(BUTTON_TP, this::teleportToColony);
        fillAlliesAndFeudsList();
    }

    /**
     * On Button click teleport to the colony..
     *
     * @param button the clicked button.
     */
    private void teleportToColony(@NotNull final Button button)
    {
        final int row = alliesList.getListElementIndexByPane(button);
        final CompactColonyReference ally = building.getColony().getAllies().get(row);

        MessageUtils.format(DO_REALLY_WANNA_TP, ally.name)
          .with(Style.EMPTY.withClickEvent(new ClickEventWithExecutable(ClickEvent.Action.RUN_COMMAND,
            "",
            () -> Network.getNetwork().sendToServer(new TeleportToColonyMessage(ally.dimension, ally.id)))))
          .with(ChatFormatting.BOLD, ChatFormatting.GOLD)
          .sendTo(Minecraft.getInstance().player);
        this.close();
    }

    /**
     * Fills the allies and feuds lists.
     */
    private void fillAlliesAndFeudsList()
    {
        alliesList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return building.getColony().getAllies().size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final CompactColonyReference colonyReference = building.getColony().getAllies().get(index);
                rowPane.findPaneOfTypeByID(NAME_LABEL, Text.class).setText(Component.literal(colonyReference.name));
                final long distance = BlockPosUtil.getDistance2D(colonyReference.center, building.getPosition());
                rowPane.findPaneOfTypeByID(DIST_LABEL, Text.class).setText(Component.literal((int) distance + "b"));
                final Button button = rowPane.findPaneOfTypeByID(BUTTON_TP, Button.class);
                if (colonyReference.hasTownHall && (building.getBuildingLevel() < MineColonies.getConfig().getServer().minThLevelToTeleport.get() || !building.canPlayerUseTP()))
                {
                    button.setText(Component.translatable(TH_TOO_LOW));
                    button.disable();
                }
                else
                {
                    button.enable();
                }
            }
        });

        feudsList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return building.getColony().getFeuds().size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final CompactColonyReference colonyReference = building.getColony().getFeuds().get(index);
                rowPane.findPaneOfTypeByID(NAME_LABEL, Text.class).setText(Component.literal(colonyReference.name));
                final long distance = BlockPosUtil.getDistance2D(colonyReference.center, building.getPosition());
                rowPane.findPaneOfTypeByID(DIST_LABEL, Text.class).setText(Component.literal(String.valueOf((int) distance)));
            }
        });
    }

    @Override
    protected String getWindowId()
    {
        return BUTTON_ALLIANCE;
    }
}
