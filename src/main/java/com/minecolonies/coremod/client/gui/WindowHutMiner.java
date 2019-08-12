package com.minecolonies.coremod.client.gui;

import com.ldtteam.structurize.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.ldtteam.blockout.Color;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.Label;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.blockout.views.SwitchView;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingMiner;
import com.minecolonies.coremod.network.messages.MinerSetLevelMessage;
import net.minecraft.util.Tuple;

import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Window for the miner hut.
 */
public class WindowHutMiner extends AbstractWindowWorkerBuilding<BuildingMiner.View>
{
    private static final String LIST_LEVELS               = "levels";
    private static final String PAGE_LEVELS               = "levelActions";
    private static final String BUTTON_CURRENTLEVEL       = "changeToLevel";
    private static final String VIEW_PAGES                = "pages";
    private static final String HUT_MINER_RESOURCE_SUFFIX = ":gui/windowhutminer.xml";
    private final BuildingMiner.View            miner;
    private       List<Tuple<Integer, Integer>> levelsInfo;
    private       ScrollingList                 levelList;

    /**
     * Constructor for the window of the miner hut.
     *
     * @param building {@link BuildingMiner.View}.
     */
    public WindowHutMiner(final BuildingMiner.View building)
    {
        super(building, Constants.MOD_ID + HUT_MINER_RESOURCE_SUFFIX);
        this.miner = building;
        pullLevelsFromHut();
    }

    /**
     * Retrieve levels from the building to display in GUI.
     */
    private void pullLevelsFromHut()
    {
        if (miner.getColony().getBuilding(miner.getID()) != null)
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
                    rowPane.findPaneOfTypeByID("lvl", Label.class).setColor(Color.getByName("red", 0));
                }

                rowPane.findPaneOfTypeByID("lvl", Label.class).setLabelText(Integer.toString(index));
                rowPane.findPaneOfTypeByID("nONodes", Label.class)
                       .setLabelText(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.minerNode") + ": " + levelsInfo.get(index).getA());
                rowPane.findPaneOfTypeByID("yLevel", Label.class)
                       .setLabelText("Y: " + (levelsInfo.get(index).getB() + 1));
                       // ^^ 1 is for Y depth fix
            }
        });
    }

    @NotNull
    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerHuts.minerHut";
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        final String currentPage = findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).getCurrentView().getID();
        if (currentPage.equals(PAGE_LEVELS))
        {
            pullLevelsFromHut();
            window.findPaneOfTypeByID(LIST_LEVELS, ScrollingList.class).refreshElementPanes();
        }
    }

    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        switch (button.getID())
        {
            case BUTTON_CURRENTLEVEL:
                final int row = levelList.getListElementIndexByPane(button);
                if (row != miner.current && row >= 0 && row < levelsInfo.size())
                {
                    miner.current = row;
                    Network.getNetwork().sendToServer(new MinerSetLevelMessage(miner, row));
                }
                break; 
            default:
                super.onButtonClicked(button);
                break;
        }
    }
}

