package com.minecolonies.coremod.client.gui.townhall;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.CompactColonyReference;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.coremod.commands.ClickEventWithExecutable;
import com.minecolonies.coremod.network.messages.server.colony.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.*;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.Constants.TICKS_FOURTY_MIN;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.*;

import net.minecraft.ChatFormatting;

/**
 * BOWindow for the town hall.
 */
public class WindowMainPage extends AbstractWindowTownHall
{
    /**
     * The ScrollingList of all allies.
     */
    private final ScrollingList alliesList;

    /**
     * The ScrollingList of all feuds.
     */
    private final ScrollingList feudsList;

    /**
     * Constructor for the town hall window.
     *
     * @param building {@link BuildingTownHall.View}.
     */
    public WindowMainPage(final BuildingTownHall.View building)
    {
        super(building, "layoutactions.xml");

        alliesList = findPaneOfTypeByID(LIST_ALLIES, ScrollingList.class);
        feudsList = findPaneOfTypeByID(LIST_FEUDS, ScrollingList.class);

        registerButton(BUTTON_CHANGE_SPEC, this::doNothing);
        registerButton(BUTTON_RENAME, this::renameClicked);
        registerButton(BUTTON_MERCENARY, this::mercenaryClicked);

        registerButton(BUTTON_TP, this::teleportToColony);
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
        final Component teleport = new TranslatableComponent(DO_REALLY_WANNA_TP, ally.name)
                                          .setStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.GOLD).withClickEvent(
                                            new ClickEventWithExecutable(ClickEvent.Action.RUN_COMMAND, "",
                                              () -> Network.getNetwork().sendToServer(new TeleportToColonyMessage(
                                                ally.dimension, ally.id)))));

        Minecraft.getInstance().player.sendMessage(teleport, Minecraft.getInstance().player.getUUID());
        this.close();
    }

    /**
     * Executed when <code>WindowTownHall</code> is opened. Does tasks like setting buttons.
     */
    @Override
    public void onOpened()
    {
        super.onOpened();
        fillAlliesAndFeudsList();

        if (building.getColony().getMercenaryUseTime() != 0
              && building.getColony().getWorld().getGameTime() - building.getColony().getMercenaryUseTime() < TICKS_FOURTY_MIN)
        {
            findPaneOfTypeByID(BUTTON_MERCENARY, Button.class).disable();
        }
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
                rowPane.findPaneOfTypeByID(NAME_LABEL, Text.class).setText(colonyReference.name);
                final long distance = BlockPosUtil.getDistance2D(colonyReference.center, building.getPosition());
                rowPane.findPaneOfTypeByID(DIST_LABEL, Text.class).setText((int) distance + "b");
                final Button button = rowPane.findPaneOfTypeByID(BUTTON_TP, Button.class);
                if (colonyReference.hasTownHall && (building.getBuildingLevel() < MineColonies.getConfig().getServer().minThLevelToTeleport.get() || !building.canPlayerUseTP()))
                {
                    button.setText(new TranslatableComponent(TH_TOO_LOW));
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
                rowPane.findPaneOfTypeByID(NAME_LABEL, Text.class).setText(colonyReference.name);
                final long distance = BlockPosUtil.getDistance2D(colonyReference.center, building.getPosition());
                rowPane.findPaneOfTypeByID(DIST_LABEL, Text.class).setText(String.valueOf((int) distance));
            }
        });
    }

    /**
     * Action performed when rename button is clicked.
     */
    private void renameClicked()
    {
        @NotNull final WindowTownHallNameEntry window = new WindowTownHallNameEntry(building.getColony());
        window.open();
    }

    /**
     * Action performed when mercenary button is clicked.
     */
    private void mercenaryClicked()
    {
        @NotNull final WindowTownHallMercenary window = new WindowTownHallMercenary(building.getColony());
        window.open();
    }

    @Override
    protected String getWindowId()
    {
        return BUTTON_ACTIONS;
    }
}
