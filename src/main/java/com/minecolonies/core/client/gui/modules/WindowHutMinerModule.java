package com.minecolonies.core.client.gui.modules;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.AbstractModuleWindow;
import com.minecolonies.core.colony.buildings.moduleviews.MinerLevelManagementModuleView;
import com.minecolonies.core.network.messages.server.colony.building.miner.MinerRepairLevelMessage;
import com.minecolonies.core.network.messages.server.colony.building.miner.MinerSetLevelMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * BOWindow for the miner hut.
 */
public class WindowHutMinerModule extends AbstractModuleWindow
{
    /**
     * Util tags.
     */
    private static final String LIST_LEVELS       = "levels";
    private static final String TEXT_LEVEL        = "level";
    private static final String BUTTON_MINE_LEVEL = "mine";
    private static final String BUTTON_REPAIR     = "repair";
    private static final String TEXT_DEPTH        = "depth";
    private static final String TEXT_NODE_COUNT   = "nodes";

    private static final String HUT_MINER_RESOURCE_SUFFIX = ":gui/layouthuts/layoutminermodule.xml";

    /**
     * The underlying miner management module.
     */
    private final MinerLevelManagementModuleView miner;

    /**
     * The list containing the levels.
     */
    private ScrollingList levelList;

    /**
     * Constructor for the window of the miner hut.
     *
     * @param moduleView {@link MinerLevelManagementModuleView}.
     */
    public WindowHutMinerModule(final IBuildingView building, final MinerLevelManagementModuleView moduleView)
    {
        super(building, Constants.MOD_ID + HUT_MINER_RESOURCE_SUFFIX);
        this.miner = moduleView;

        registerButton(BUTTON_REPAIR, this::repairClicked);
        registerButton(BUTTON_MINE_LEVEL, this::mineLevelClicked);
    }

    /**
     * Handler for clicking on any of the repair buttons.
     *
     * @param button the input button clicked.
     */
    private void repairClicked(final Button button)
    {
        final int row = levelList.getListElementIndexByPane(button);
        Network.getNetwork().sendToServer(new MinerRepairLevelMessage(buildingView, row));
        MessageUtils.format(MINER_REPAIR_ENQUEUED).sendTo(Minecraft.getInstance().player);
    }

    /**
     * Handler for clicking on any of the mine level buttons.
     *
     * @param button the input button clicked.
     */
    private void mineLevelClicked(final Button button)
    {
        final int row = levelList.getListElementIndexByPane(button);
        if (row != miner.current && row >= 0 && row < miner.levelsInfo.size())
        {
            miner.current = row;
            Network.getNetwork().sendToServer(new MinerSetLevelMessage(buildingView, row));
        }
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        levelList = findPaneOfTypeByID(LIST_LEVELS, ScrollingList.class);
        levelList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return miner.levelsInfo.size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final boolean isCurrentLevel = index == miner.current;
                rowPane.findPaneOfTypeByID(TEXT_LEVEL, Text.class).setText(Component.literal(String.format("%02d", index + 1)));

                rowPane.findPaneOfTypeByID(BUTTON_REPAIR, Button.class).setEnabled(!miner.doesWorkOrderExist(index));
                rowPane.findPaneOfTypeByID(BUTTON_MINE_LEVEL, Button.class).setEnabled(!isCurrentLevel);

                // Extra 1 is for Y depth fix
                rowPane.findPaneOfTypeByID(TEXT_DEPTH, Text.class)
                  .setText(Component.translatable(MINER_LEVEL_DEPTH)
                             .append(Component.literal(": "))
                             .append(Component.literal(String.valueOf(miner.levelsInfo.get(index).getB() + 1))));
                rowPane.findPaneOfTypeByID(TEXT_NODE_COUNT, Text.class)
                  .setText(Component.translatable(MINER_NODES).append(": ").append(String.valueOf(miner.levelsInfo.get(index).getA())));
            }
        });
    }
}
