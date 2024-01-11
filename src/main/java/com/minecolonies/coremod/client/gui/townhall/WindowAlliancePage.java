package com.minecolonies.coremod.client.gui.townhall;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.CompactColonyReference;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.MessageUtils.MessagePriority;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.coremod.commands.ClickEventWithExecutable;
import com.minecolonies.coremod.network.messages.server.colony.TeleportToColonyMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.TranslationConstants.DO_REALLY_WANNA_TP;
import static com.minecolonies.api.util.constant.TranslationConstants.TH_TOO_LOW;
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
          .withPriority(MessagePriority.IMPORTANT)
          .withClickEvent(new ClickEventWithExecutable(() -> Network.getNetwork().sendToServer(new TeleportToColonyMessage(ally.dimension, ally.id))))
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
