package com.minecolonies.client.gui;

import com.blockout.controls.Button;
import com.blockout.controls.ButtonVanilla;
import com.blockout.views.SwitchView;
import com.minecolonies.colony.buildings.BuildingWarehouse;
import com.minecolonies.lib.Constants;
import com.minecolonies.util.LanguageHandler;

public class WindowHutWarehouse extends WindowWorkerBuilding<BuildingWarehouse.View> implements Button.Handler
{
    private static String BUTTON_BLACKSMITH_GOLD = "blacksmithGold",
            BUTTON_BLACKSMITH_DIAMOND = "blacksmithDiamond",
            BUTTON_STONEMASON_COBBLESTONE = "stonemasonCobblestone",
            BUTTON_STONEMASON_SAND = "stonemasonSand",
            BUTTON_STONEMASON_NETHERRACK = "stonemasonNetherrack",
            BUTTON_STONEMASON_QUARTZ = "stonemasonQuartz",
            BUTTON_GUARD_ARMOR = "guardArmor",
            BUTTON_GUARD_WEAPON = "guardWeapon",
            BUTTON_CITIZEN_CHESTS = "citizenChests",
            BUTTON_PREVPAGE = "prevPage",
            BUTTON_NEXTPAGE = "nextPage",

            VIEW_PAGES = "pages",
            PAGE_ACTIONS = "pageActions",
            PAGE_SETTINGS = "pageSettings";;

    Button buttonPrevPage, buttonNextPage;

    public WindowHutWarehouse(BuildingWarehouse.View building)
    {
        super(building, Constants.MODID + ":gui/windowHutWarehouse.xml");
    }

    public String getBuildingName() { return "com.minecolonies.gui.workerHuts.warehouse"; }

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
            findPaneOfTypeByID(BUTTON_BLACKSMITH_GOLD, ButtonVanilla.class).setLabel(getYesOrNo(building.blacksmithGold));
            findPaneOfTypeByID(BUTTON_BLACKSMITH_DIAMOND, ButtonVanilla.class).setLabel(getYesOrNo(building.blacksmithDiamond));
            findPaneOfTypeByID(BUTTON_STONEMASON_COBBLESTONE, ButtonVanilla.class).setLabel(getYesOrNo(building.stonemasonStone));
            findPaneOfTypeByID(BUTTON_STONEMASON_SAND, ButtonVanilla.class).setLabel(getYesOrNo(building.stonemasonSand));
            findPaneOfTypeByID(BUTTON_STONEMASON_NETHERRACK, ButtonVanilla.class).setLabel(getYesOrNo(building.stonemasonNetherrack));
            findPaneOfTypeByID(BUTTON_STONEMASON_QUARTZ, ButtonVanilla.class).setLabel(getYesOrNo(building.stonemasonQuartz));
            findPaneOfTypeByID(BUTTON_GUARD_ARMOR, ButtonVanilla.class).setLabel(getYesOrNo(building.guardArmor));
            findPaneOfTypeByID(BUTTON_GUARD_WEAPON, ButtonVanilla.class).setLabel(getYesOrNo(building.guardWeapon));
            findPaneOfTypeByID(BUTTON_CITIZEN_CHESTS, ButtonVanilla.class).setLabel(getYesOrNo(building.citizenVisit));
        }
        catch (NullPointerException exc) {}
    }

    @Override
    public void onButtonClicked(Button button)
    {
        if (button.getID().equals(BUTTON_BLACKSMITH_GOLD))              building.blacksmithGold = !building.blacksmithGold;
        else if (button.getID().equals(BUTTON_BLACKSMITH_DIAMOND))      building.blacksmithDiamond = !building.blacksmithDiamond;
        else if (button.getID().equals(BUTTON_STONEMASON_COBBLESTONE))  building.stonemasonStone = !building.stonemasonStone;
        else if (button.getID().equals(BUTTON_STONEMASON_SAND))         building.stonemasonSand = !building.stonemasonSand;
        else if (button.getID().equals(BUTTON_STONEMASON_NETHERRACK))   building.stonemasonNetherrack = !building.stonemasonNetherrack;
        else if (button.getID().equals(BUTTON_STONEMASON_QUARTZ))       building.stonemasonQuartz = !building.stonemasonQuartz;
        else if (button.getID().equals(BUTTON_GUARD_ARMOR))             building.guardArmor = !building.guardArmor;
        else if (button.getID().equals(BUTTON_GUARD_WEAPON))            building.guardWeapon = !building.guardWeapon;
        else if (button.getID().equals(BUTTON_CITIZEN_CHESTS))          building.citizenVisit = !building.citizenVisit;
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

    private String getYesOrNo(boolean bool)
    {
        return bool ? LanguageHandler.format("gui.yes") : LanguageHandler.format("gui.no");
    }
}
