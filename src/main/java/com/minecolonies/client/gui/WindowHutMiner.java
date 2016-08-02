package com.minecolonies.client.gui;

import com.blockout.Pane;
import com.blockout.controls.Button;
import com.blockout.controls.Label;
import com.blockout.views.ScrollingList;
import com.blockout.views.SwitchView;
import com.minecolonies.MineColonies;
import com.minecolonies.colony.buildings.BuildingMiner;
import com.minecolonies.lib.Constants;
import com.minecolonies.network.messages.MinerSetLevelMessage;
import com.minecolonies.util.LanguageHandler;

import java.awt.*;

/**
 * Window for the miner hut
 */
public class WindowHutMiner extends AbstractWindowWorkerBuilding<BuildingMiner.View>
{
    private static final String LIST_LEVELS               = "levels";
    private static final String PAGE_LEVELS               = "levelActions";
    private static final String BUTTON_PREVPAGE           = "prevPage";
    private static final String BUTTON_NEXTPAGE           = "nextPage";
    private static final String BUTTON_CURRENTLEVEL       = "changeToLevel";
    private static final String VIEW_PAGES                = "pages";
    private static final String HUT_MINER_RESOURCE_SUFFIX = ":gui/windowHutMiner.xml";
    private Button             buttonPrevPage;
    private Button             buttonNextPage;
    private int[]              levels;
    private ScrollingList      levelList;
    private BuildingMiner.View miner;

    /**
     * Constructor for the window of the miner hut
     *
     * @param building {@link com.minecolonies.colony.buildings.BuildingMiner.View}
     */
    public WindowHutMiner(BuildingMiner.View building)
    {
        super(building, Constants.MOD_ID + HUT_MINER_RESOURCE_SUFFIX);
        this.miner = building;
        pullLevelsFromHut();
    }

    /**
     * Retrieve levels from the building to display in GUI
     */
    private void pullLevelsFromHut()
    {
        if (miner.getColony().getBuilding(miner.getID()) != null)
        {
            levels = miner.levels;
        }
    }

    @Override
    public void onOpened()
    {
        super.onOpened();


        findPaneOfTypeByID(BUTTON_PREVPAGE, Button.class).setEnabled(false);

        buttonNextPage = findPaneOfTypeByID(BUTTON_NEXTPAGE, Button.class);
        buttonPrevPage = findPaneOfTypeByID(BUTTON_PREVPAGE, Button.class);


        levelList = findPaneOfTypeByID(LIST_LEVELS, ScrollingList.class);
        levelList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return levels.length;
            }

            @Override
            public void updateElement(int index, Pane rowPane)
            {

                if (index == miner.current)
                {
                    rowPane.findPaneOfTypeByID("lvl", Label.class).setColor(Color.RED.getRGB());
                }
                else
                {
                    rowPane.findPaneOfTypeByID("lvl", Label.class).setColor(Color.BLACK.getRGB());
                }

                rowPane.findPaneOfTypeByID("lvl", Label.class).setText(Integer.toString(index));
                rowPane.findPaneOfTypeByID("nONodes", Label.class).setText(LanguageHandler.getString("com.minecolonies.gui.workerHuts.minerNode") + ": " + levels[index]);

            }
        });
    }

    @Override
    public void onButtonClicked(Button button)
    {
        switch (button.getID())
        {
            case BUTTON_PREVPAGE:
                findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).previousView();
                buttonPrevPage.setEnabled(false);
                buttonNextPage.setEnabled(true);
                break;
            case BUTTON_NEXTPAGE:
                findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).nextView();
                buttonPrevPage.setEnabled(true);
                buttonNextPage.setEnabled(false);
                break;
            case BUTTON_CURRENTLEVEL:
                int row = levelList.getListElementIndexByPane(button);
                if (row != miner.current && row >= 0 && row < levels.length)
                {
                    miner.current = row;
                    MineColonies.getNetwork().sendToServer(new MinerSetLevelMessage(miner, row));
                }
                break;
            default:
                super.onButtonClicked(button);
                break;
        }
    }

    @Override
    public void onUpdate()
    {

        String currentPage = findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).getCurrentView().getID();
        if (currentPage.equals(PAGE_LEVELS))
        {
            pullLevelsFromHut();
            window.findPaneOfTypeByID(LIST_LEVELS, ScrollingList.class).refreshElementPanes();
        }

    }

    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.gui.workerHuts.minerHut";
    }
}

