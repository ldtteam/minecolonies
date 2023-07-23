package com.minecolonies.coremod.client.gui.townhall;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.CompactColonyReference;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.map.WindowColonyMap;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.coremod.commands.ClickEventWithExecutable;
import com.minecolonies.coremod.network.messages.server.colony.TeleportToColonyMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.network.chat.*;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.Constants.TICKS_FOURTY_MIN;
import static com.minecolonies.api.util.constant.TranslationConstants.DO_REALLY_WANNA_TP;
import static com.minecolonies.api.util.constant.TranslationConstants.TH_TOO_LOW;
import static com.minecolonies.api.util.constant.WindowConstants.*;

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
     * Label for the colony name.
     */
    private final Text title;

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

        title = findPaneOfTypeByID(LABEL_BUILDING_NAME, Text.class);

        registerButton(BUTTON_CHANGE_SPEC, this::doNothing);
        registerButton(BUTTON_RENAME, this::renameClicked);
        registerButton(BUTTON_MERCENARY, this::mercenaryClicked);
        registerButton(BUTTON_TOWNHALLMAP, this::mapButtonClicked);
        registerButton(BUTTON_PATREON, this::patreonClicked);

        registerButton(BUTTON_TP, this::teleportToColony);
    }

    /**
     * On Patreon button clicked. Open website link to patreon.
     */
    private void patreonClicked()
    {
        Minecraft.getInstance().setScreen(new ConfirmLinkScreen((check) -> {
            if (check) {
                Util.getPlatform().openUri("https://www.patreon.com/Minecolonies");
            }

            Minecraft.getInstance().setScreen(this.screen);
        }, "https://www.patreon.com/Minecolonies", true));
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
     * Executed when <code>WindowTownHall</code> is opened. Does tasks like setting buttons.
     */
    @Override
    public void onOpened()
    {
        super.onOpened();
        fillAlliesAndFeudsList();

        title.setText(Component.literal(building.getColony().getName()));

        if (building.getColony().getMercenaryUseTime() != 0 && building.getColony().getWorld().getGameTime() - building.getColony().getMercenaryUseTime() < TICKS_FOURTY_MIN)
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

    /**
     * Opens the map on button clicked
     */
    private void mapButtonClicked()
    {
        @NotNull final WindowColonyMap window = new WindowColonyMap(building);
        window.open();
    }

    @Override
    protected String getWindowId()
    {
        return BUTTON_ACTIONS;
    }
}
