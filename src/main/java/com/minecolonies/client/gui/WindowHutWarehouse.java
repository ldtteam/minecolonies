package com.minecolonies.client.gui;

import com.blockout.controls.Button;
import com.blockout.controls.ButtonVanilla;
import com.blockout.views.SwitchView;
import com.minecolonies.colony.buildings.BuildingWarehouse;
import com.minecolonies.lib.Constants;
import com.minecolonies.util.LanguageHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Window for the farmer hut.
 */
public class WindowHutWarehouse extends AbstractWindowWorkerBuilding<BuildingWarehouse.View> implements Button.Handler
{
    private static final String BUTTON_BLACKSMITH_GOLD        = "blacksmithGold";
    private static final String BUTTON_BLACKSMITH_DIAMOND     = "blacksmithDiamond";
    private static final String BUTTON_STONEMASON_COBBLESTONE = "stonemasonCobblestone";
    private static final String BUTTON_STONEMASON_SAND        = "stonemasonSand";
    private static final String BUTTON_STONEMASON_NETHERRACK  = "stonemasonNetherrack";
    private static final String BUTTON_STONEMASON_QUARTZ      = "stonemasonQuartz";
    private static final String BUTTON_GUARD_ARMOR            = "guardArmor";
    private static final String BUTTON_GUARD_WEAPON           = "guardWeapon";
    private static final String BUTTON_CITIZEN_CHESTS         = "citizenChests";
    private static final String BUTTON_PREVPAGE               = "prevPage";
    private static final String BUTTON_NEXTPAGE               = "nextPage";
    private static final String VIEW_PAGES                    = "pages";

    /* Unused for now */
    //private static final String PAGE_ACTIONS = "pageActions";
    //private static final String PAGE_SETTINGS = "pageSettings";

    private static final String HUT_WAREHOUSE_RESOURCE_SUFFIX = ":gui/windowHutWarehouse.xml";

    private Button buttonPrevPage;
    private Button buttonNextPage;

    /**
     * Constructor for the window of the warehouse hut.
     *
     * @param building {@link com.minecolonies.colony.buildings.BuildingWarehouse.View}
     */
    public WindowHutWarehouse(@NotNull final BuildingWarehouse.View building)
    {
        super(building, Constants.MOD_ID + HUT_WAREHOUSE_RESOURCE_SUFFIX);
        super.registerButton(BUTTON_BLACKSMITH_GOLD, b -> building.blacksmithGold = !building.blacksmithGold);
        super.registerButton(BUTTON_BLACKSMITH_DIAMOND, b -> building.blacksmithDiamond = !building.blacksmithDiamond);
        super.registerButton(BUTTON_STONEMASON_COBBLESTONE, b -> building.stonemasonStone = !building.stonemasonStone);
        super.registerButton(BUTTON_STONEMASON_SAND, b -> building.stonemasonSand = !building.stonemasonSand);
        super.registerButton(BUTTON_STONEMASON_NETHERRACK, b -> building.stonemasonNetherrack = !building.stonemasonNetherrack);
        super.registerButton(BUTTON_STONEMASON_QUARTZ, b -> building.stonemasonQuartz = !building.stonemasonQuartz);
        super.registerButton(BUTTON_GUARD_ARMOR, b -> building.guardArmor = !building.guardArmor);
        super.registerButton(BUTTON_GUARD_WEAPON, b -> building.guardWeapon = !building.guardWeapon);
        super.registerButton(BUTTON_CITIZEN_CHESTS, b -> building.citizenVisit = !building.citizenVisit);
        super.registerButton(BUTTON_BLACKSMITH_GOLD, b -> building.blacksmithGold = !building.blacksmithGold);
    }

    @NotNull
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

        findPaneOfTypeByID(BUTTON_PREVPAGE, Button.class).setEnabled(false);

        buttonPrevPage = findPaneOfTypeByID(BUTTON_PREVPAGE, Button.class);
        buttonNextPage = findPaneOfTypeByID(BUTTON_NEXTPAGE, Button.class);
    }

    /**
     * Update the labels on the buttons.
     */
    private void updateButtonLabels()
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

    /**
     * Returns specific string depending on the boolean value.
     *
     * @param bool Boolean value to check.
     * @return String depending on boolean value.
     */
    private static String getYesOrNo(final boolean bool)
    {
        return bool ? LanguageHandler.format("gui.yes") : LanguageHandler.format("gui.no");
    }

    @Override
    public void onButtonClicked(@NotNull final Button button)
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

        updateButtonLabels();
    }
}
