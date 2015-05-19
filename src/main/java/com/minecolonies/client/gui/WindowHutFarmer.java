package com.minecolonies.client.gui;

import com.blockout.controls.Button;
import com.blockout.controls.ButtonVanilla;
import com.blockout.views.SwitchView;
import com.minecolonies.MineColonies;
import com.minecolonies.colony.buildings.BuildingFarmer;
import com.minecolonies.lib.Constants;
import com.minecolonies.network.messages.FarmerCropTypeMessage;

public class WindowHutFarmer extends WindowWorkerBuilding<BuildingFarmer.View>
{
    private static String BUTTON_WHEAT = "wheat",
            BUTTON_POTATO = "potato",
            BUTTON_CARROT = "carrot",
            BUTTON_MELON = "melon",
            BUTTON_PUMPKIN = "pumpkin",
            BUTTON_PREVPAGE = "prevPage",
            BUTTON_NEXTPAGE = "nextPage",
            VIEW_PAGES = "pages";

    Button buttonPrevPage, buttonNextPage;


    public WindowHutFarmer(BuildingFarmer.View building)
    {
        super(building, Constants.MOD_ID + ":gui/windowHutFarmer.xml");
    }

    public String getBuildingName() { return "com.minecolonies.gui.workerHuts.farmer"; }


    @Override
    public void onOpened()
    {
        super.onOpened();

        updateButtonLabels();
        try
        {
            findPaneOfTypeByID(BUTTON_PREVPAGE, Button.class).setEnabled(false);
            buttonPrevPage = findPaneOfTypeByID(BUTTON_PREVPAGE, Button.class);
            buttonNextPage = findPaneOfTypeByID(BUTTON_NEXTPAGE, Button.class);
        }
        catch (NullPointerException exc) {}
    }

    private void updateButtonLabels()
    {
        try
        {
            findPaneOfTypeByID(BUTTON_WHEAT, ButtonVanilla.class).setLabel(""+building.wheat);
            findPaneOfTypeByID(BUTTON_POTATO, ButtonVanilla.class).setLabel(""+building.potato);
            findPaneOfTypeByID(BUTTON_CARROT, ButtonVanilla.class).setLabel("" + building.carrot);
            findPaneOfTypeByID(BUTTON_MELON, ButtonVanilla.class).setLabel(""+building.melon);
            findPaneOfTypeByID(BUTTON_PUMPKIN, ButtonVanilla.class).setLabel(""+building.pumpkin);

        }
        catch (NullPointerException exc)
        {}
    }

    @Override
    public void onButtonClicked(Button button)
    {
        if (button.getID().equals(BUTTON_WHEAT))
        {
            if(building.wheat < 100)
            {
                building.wheat++;
                removeOthers("wheat");
            }
        }
        else if (button.getID().equals(BUTTON_POTATO))
        {
            if (building.potato < 100)
            {
                building.potato++;
                removeOthers("potato");
            }
        }
        else if (button.getID().equals(BUTTON_CARROT))
        {
            if(building.carrot < 100)
            {
                building.carrot++;
                removeOthers("carrot");
            }
        }
        else if (button.getID().equals(BUTTON_MELON))
        {
            if(building.melon < 100)
            {
                building.melon++;
                removeOthers("melon");
            }
        }
        else if (button.getID().equals(BUTTON_PUMPKIN))
        {
            if(building.pumpkin < 100)
            {
                building.pumpkin++;
                removeOthers("pumpkin");
            }
        }
        else
        {
            if (button.getID().equals(BUTTON_PREVPAGE))
            {
                findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).previousView();
                buttonPrevPage.setEnabled(false);
                buttonNextPage.setEnabled(true);
            }
            else if (button.getID().equals(BUTTON_NEXTPAGE))
            {
                findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).nextView();
                buttonPrevPage.setEnabled(true);
                buttonNextPage.setEnabled(false);
            }
            else
            {
                super.onButtonClicked(button);
            }

            return;
        }

        updateButtonLabels();
    }

    public int sum()
    {
        return building.wheat + building.carrot + building.melon + building.potato + building.pumpkin;
    }

    public void removeOthers(String s)
    {
        while(sum() > 100)
        {

            int rand = (int)(Math.random()*5);

            if (building.potato != 0 && !s.equals("potato") && rand == 0)
            {
                building.potato--;
            }
            else if (building.wheat != 0 && !s.equals("wheat") && rand == 1)
            {
                building.wheat--;
            }
            else if (building.carrot != 0 && !s.equals("carrot") && rand == 2)
            {
                building.carrot--;
            }
            else if (building.melon != 0 && !s.equals("melon") && rand == 3)
            {
                building.melon--;
            }
            else if (building.pumpkin != 0 && !s.equals("pumpkin") && rand == 4)
            {
                building.pumpkin--;
            }
        }

        if(sum() < 100)
        {
            building.wheat = building.wheat + 100-sum();
        }

        MineColonies.network.sendToServer(new FarmerCropTypeMessage(building));
    }
}

