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

public class WindowHutMiner extends WindowWorkerBuilding<BuildingMiner.View>
{
    private static final String LIST_LEVELS = "levels", PAGE_LEVELS = "levelActions", BUTTON_PREVPAGE = "prevPage", BUTTON_NEXTPAGE = "nextPage", BUTTON_CURRENTLEVEL = "changeToLevel",

    VIEW_PAGES = "pages";
    Button buttonPrevPage, buttonNextPage;
    private ScrollingList      levelList;
    private int[]              levels;
    private BuildingMiner.View miner;

    public WindowHutMiner(BuildingMiner.View building)
    {
        super(building, Constants.MOD_ID + ":gui/windowHutMiner.xml");
        this.miner = building;
        updateUsers();
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        try
        {
            findPaneOfTypeByID(BUTTON_PREVPAGE, Button.class).setEnabled(false);
            buttonPrevPage = findPaneOfTypeByID(BUTTON_PREVPAGE, Button.class);
            buttonNextPage = findPaneOfTypeByID(BUTTON_NEXTPAGE, Button.class);
        }
        catch(NullPointerException exc){}


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
                try
                {
                    if(index == miner.current)
                    {
                        rowPane.findPaneOfTypeByID("lvl", Label.class).setColor(Color.RED.getRGB());
                    }
                    else
                    {
                        rowPane.findPaneOfTypeByID("lvl", Label.class).setColor(Color.BLACK.getRGB());
                    }

                    rowPane.findPaneOfTypeByID("lvl", Label.class).setLabel("" + index);
                    rowPane.findPaneOfTypeByID("nONodes", Label.class).setLabel(LanguageHandler.getString("com.minecolonies.gui.workerHuts.minerNode") + ": " + levels[index]);
                }
                catch(NullPointerException exc)
                {
                }
            }
        });
    }

    @Override
    public void onButtonClicked(Button button)
    {
        if(button.getID().equals(BUTTON_PREVPAGE))
        {
            findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).previousView();
            buttonPrevPage.setEnabled(false);
            buttonNextPage.setEnabled(true);
        }
        else if(button.getID().equals(BUTTON_NEXTPAGE))
        {
            findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).nextView();
            buttonPrevPage.setEnabled(true);
            buttonNextPage.setEnabled(false);
        }
        else if(button.getID().equals(BUTTON_CURRENTLEVEL))
        {
            int row = levelList.getListElementIndexByPane(button);
            if(row != miner.current && row >= 0 && row < levels.length)
            {
                miner.current = row;
                MineColonies.getNetwork().sendToServer(new MinerSetLevelMessage(miner, row));
            }
        }
        else
        {
            super.onButtonClicked(button);
        }
    }

    @Override
    public void onUpdate()
    {
        String currentPage = findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).getCurrentView().getID();
        if(currentPage.equals(PAGE_LEVELS))
        {
            updateUsers();
            window.findPaneOfTypeByID(LIST_LEVELS, ScrollingList.class).refreshElementPanes();
        }
    }

    private void updateUsers()
    {
        if(miner.getColony().getBuilding(miner.getID()) != null)
        {
            levels = new int[miner.levels.length];
            levels = miner.levels;
        }
    }

    public String getBuildingName()
    {
        return "com.minecolonies.gui.workerHuts.minerHut";
    }
}

