package com.minecolonies.coremod.client.gui.huts;

import com.ldtteam.blockout.Color;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.AbstractModuleWindow;
import com.minecolonies.coremod.colony.buildings.moduleviews.MinerLevelManagementModuleView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingMiner;
import com.minecolonies.coremod.network.messages.server.colony.building.miner.MinerRepairLevelMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.miner.MinerSetLevelMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Window for the miner hut.
 */
public class WindowHutMinerModule extends AbstractModuleWindow
{
    /**
     * Util tags.
     */
    private static final String LIST_LEVELS         = "levels";
    private static final String BUTTON_CURRENTLEVEL = "changeToLevel";
    private static final String BUTTON_REPAIR       = "repair";

    private static final String HUT_MINER_RESOURCE_SUFFIX = ":gui/layouthuts/layoutminermodule.xml";

    private final MinerLevelManagementModuleView miner;
    private       List<Tuple<Integer, Integer>>  levelsInfo;
    private       ScrollingList                  levelList;

    /**
     * Constructor for the window of the miner hut.
     *
     * @param building {@link BuildingMiner.View}.
     */
    public WindowHutMinerModule(final IBuildingView building, final MinerLevelManagementModuleView moduleView)
    {
        super(building, Constants.MOD_ID + HUT_MINER_RESOURCE_SUFFIX);
        this.miner = moduleView;
        pullLevelsFromHut();

        registerButton(BUTTON_CURRENTLEVEL, this::currentLevelClicked);
        registerButton(BUTTON_REPAIR, this::repairClicked);
    }

    private void repairClicked(final Button button)
    {
        final int row = levelList.getListElementIndexByPane(button);
        Network.getNetwork().sendToServer(new MinerRepairLevelMessage(buildingView, row));
        LanguageHandler.sendPlayerMessage(Minecraft.getInstance().player, "com.minecolonies.coremod.gui.workerhuts.miner.repair.enqueued");
    }

    private void currentLevelClicked(final Button button)
    {
        final int row = levelList.getListElementIndexByPane(button);
        if (row != miner.current && row >= 0 && row < levelsInfo.size())
        {
            miner.current = row;
            Network.getNetwork().sendToServer(new MinerSetLevelMessage(buildingView, row));
        }
    }

    /**
     * Retrieve levels from the building to display in GUI.
     */
    private void pullLevelsFromHut()
    {
        if (miner.getColony().getBuilding(buildingView.getID()) != null)
        {
            levelsInfo = miner.levelsInfo;
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
                return levelsInfo.size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                if (index == miner.current)
                {
                    rowPane.findPaneOfTypeByID("lvl", Text.class).setColors(Color.getByName("red", 0));
                }

                if (miner.doesWorkOrderExist(index))
                {
                    rowPane.findPaneOfTypeByID("repair", Button.class).hide();
                }

                rowPane.findPaneOfTypeByID("lvl", Text.class).setText(Integer.toString(index));
                rowPane.findPaneOfTypeByID("nONodes", Text.class)
                  .setText(LanguageHandler.format("com.minecolonies.coremod.gui.workerhuts.minerNode") + ": " + levelsInfo.get(index).getA());
                rowPane.findPaneOfTypeByID("yLevel", Text.class)
                  .setText("Y: " + (levelsInfo.get(index).getB() + 1));
                // ^^ 1 is for Y depth fix
            }
        });
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        pullLevelsFromHut();
    }
}
