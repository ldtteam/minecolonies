package com.minecolonies.client.gui;

import com.blockout.controls.Button;
import com.blockout.controls.ButtonVanilla;
import com.blockout.views.SwitchView;
import com.minecolonies.MineColonies;
import com.minecolonies.colony.buildings.BuildingWarehouse;
import com.minecolonies.lib.Constants;
import com.minecolonies.util.LanguageHandler;

public class WindowHutWarehouse extends WindowWorkerBuilding<BuildingWarehouse.View> implements Button.Handler
{
    private static final    String BUTTON_BLACKSMITH_GOLD           = "blacksmithGold";
    private static final    String BUTTON_BLACKSMITH_DIAMOND        = "blacksmithDiamond";
    private static final    String BUTTON_STONEMASON_COBBLESTONE    = "stonemasonCobblestone";
    private static final    String BUTTON_STONEMASON_SAND           = "stonemasonSand";
    private static final    String BUTTON_STONEMASON_NETHERRACK     = "stonemasonNetherrack";
    private static final    String BUTTON_STONEMASON_QUARTZ         = "stonemasonQuartz";
    private static final    String BUTTON_GUARD_ARMOR               = "guardArmor";
    private static final    String BUTTON_GUARD_WEAPON              = "guardWeapon";
    private static final    String BUTTON_CITIZEN_CHESTS            = "citizenChests";
    private static final    String BUTTON_PREVPAGE                  = "prevPage";
    private static final    String BUTTON_NEXTPAGE                  = "nextPage";

    private static final    String VIEW_PAGES                       = "pages";

    /* Unused for now */
    //private static final String PAGE_ACTIONS = "pageActions";
    //private static final String PAGE_SETTINGS = "pageSettings";

    private static final    String HUT_WAREHOUSE_RESOURCE_SUFFIX    = ":gui/windowHutWarehouse.xml";

    private                 Button buttonPrevPage;
    private                 Button buttonNextPage;

    public WindowHutWarehouse(BuildingWarehouse.View building)
    {
        super(building, Constants.MOD_ID + HUT_WAREHOUSE_RESOURCE_SUFFIX);
    }

    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.gui.workerHuts.warehouse";
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        updateButtonLabels();
        try
        {
            findPaneOfTypeByID(BUTTON_PREVPAGE, Button.class).setEnabled(false);
        }
        catch (NullPointerException exc) {
            MineColonies.logger.error("findPane error, report to mod authors");
        }
        buttonPrevPage = findPaneOfTypeByID(BUTTON_PREVPAGE, Button.class);
        buttonNextPage = findPaneOfTypeByID(BUTTON_NEXTPAGE, Button.class);
    }

    /**
     * Update the labels on the buttons
     */
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
        catch (NullPointerException exc)
        {
            MineColonies.logger.error("findPane error, report to mod authors");
        }
    }

    @Override
    public void onButtonClicked(Button button)
    {
        switch (button.getID())
        {
            case BUTTON_BLACKSMITH_GOLD:
                building.blacksmithGold = !building.blacksmithGold;
                break;
            case BUTTON_BLACKSMITH_DIAMOND:
                building.blacksmithDiamond = !building.blacksmithDiamond;
                break;
            case BUTTON_STONEMASON_COBBLESTONE:
                building.stonemasonStone = !building.stonemasonStone;
                break;
            case BUTTON_STONEMASON_SAND:
                building.stonemasonSand = !building.stonemasonSand;
                break;
            case BUTTON_STONEMASON_NETHERRACK:
                building.stonemasonNetherrack = !building.stonemasonNetherrack;
                break;
            case BUTTON_STONEMASON_QUARTZ:
                building.stonemasonQuartz = !building.stonemasonQuartz;
                break;
            case BUTTON_GUARD_ARMOR:
                building.guardArmor = !building.guardArmor;
                break;
            case BUTTON_GUARD_WEAPON:
                building.guardWeapon = !building.guardWeapon;
                break;
            case BUTTON_CITIZEN_CHESTS:
                building.citizenVisit = !building.citizenVisit;
                break;
            default:
                try
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
                        default:
                            super.onButtonClicked(button);
                            break;
                    }
                    return;
                } catch (NullPointerException e)
                {
                    MineColonies.logger.error("findPane error, report to mod authors");
                }
                break;
        }

        updateButtonLabels();
    }

    /**
     * Returns specific string depending on the boolean value
     *
     * @param bool      Boolean value to check
     * @return          String depending on boolean value
     */
    private String getYesOrNo(boolean bool)
    {
        return bool ? LanguageHandler.format("gui.yes") : LanguageHandler.format("gui.no");
    }
}
