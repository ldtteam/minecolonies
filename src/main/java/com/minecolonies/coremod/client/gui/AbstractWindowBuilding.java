package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.Label;
import com.minecolonies.blockout.views.SwitchView;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingHut;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.network.messages.BuildRequestMessage;
import com.minecolonies.coremod.network.messages.OpenInventoryMessage;
import org.jetbrains.annotations.NotNull;

/**
 * Manage windows associated with Buildings.
 *
 * @param <B> Class extending {@link AbstractBuildingHut.View}.
 */
public abstract class AbstractWindowBuilding<B extends AbstractBuildingHut.View> extends AbstractWindowSkeleton
{
    private static final String BUTTON_BUILD        = "build";
    private static final String BUTTON_REPAIR       = "repair";
    private static final String BUTTON_INVENTORY    = "inventory";
    private static final String LABEL_BUILDING_NAME = "name";
    private static final String BUTTON_PREVPAGE     = "prevPage";
    private static final String BUTTON_NEXTPAGE     = "nextPage";
    private static final String VIEW_PAGES          = "pages";
    private static final String PAGE_ACTIONS        = "pageActions";

    /**
     * Type B is a class that extends {@link AbstractBuildingWorker.View}.
     */
    protected final B building;
    private final SwitchView switchView;
    private final Label title;
    private final Button buttonPrevPage;
    private final Button buttonNextPage;
    private final Button buttonBuild;
    private final Button buttonRepair;

    /**
     * Constructor for the windows that are associated with buildings.
     *
     * @param building Class extending {@link AbstractBuildingHut.View}.
     * @param resource Resource location string.
     */
    public AbstractWindowBuilding(final B building, final String resource)
    {
        super(resource);

        this.building = building;
        registerButton(BUTTON_BUILD, this::buildClicked);
        registerButton(BUTTON_REPAIR, this::repairClicked);
        registerButton(BUTTON_INVENTORY, this::inventoryClicked);
        switchView     = findPaneOfTypeByID(VIEW_PAGES, SwitchView.class);
        title          = findPaneOfTypeByID(LABEL_BUILDING_NAME, Label.class);
        buttonNextPage = findPaneOfTypeByID(BUTTON_NEXTPAGE, Button.class);
        buttonPrevPage = findPaneOfTypeByID(BUTTON_PREVPAGE, Button.class);
        buttonBuild    = findPaneOfTypeByID(BUTTON_BUILD, Button.class);
        buttonRepair   = findPaneOfTypeByID(BUTTON_REPAIR, Button.class);

    }

    /**
     * Action when build button is clicked.
     */
    private void buildClicked()
    {
        MineColonies.getNetwork().sendToServer(new BuildRequestMessage(building, BuildRequestMessage.BUILD));
    }

    /**
     * Action when repair button is clicked.
     */
    private void repairClicked()
    {
        MineColonies.getNetwork().sendToServer(new BuildRequestMessage(building, BuildRequestMessage.REPAIR));
    }

    /**
     * Action when a button opening an inventory is clicked.
     */
    private void inventoryClicked()
    {
        MineColonies.getNetwork().sendToServer(new OpenInventoryMessage(building));
    }

    /**
     * Update the state and label for the Build button.
     */
    private void updateButtonBuild(final AbstractBuilding.View buildingView)
    {
        if (buttonBuild == null)
        {
            return;
        }

        buttonBuild.setEnabled(!buildingView.isBuildingMaxLevel() && !buildingView.isRepairing());
        if (buildingView.isBuildingMaxLevel())
        {
            buttonBuild.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.upgradeUnavailable"));
        }
        else if (buildingView.isBuilding())
        {
            if (buildingView.getBuildingLevel() == 0)
            {
                buttonBuild.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.cancelBuild"));
            }
            else
            {
                buttonBuild.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.cancelUpgrade"));
            }
        }
        else
        {
            if (buildingView.getBuildingLevel() == 0)
            {
                buttonBuild.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.build"));
            }
            else
            {
                buttonBuild.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.upgrade"));
            }
        }
    }

    /**
     * Update the state and label for the Repair button.
     */
    private void updateButtonRepair(final AbstractBuilding.View buildingView)
    {
        if (buttonRepair == null)
        {
            return;
        }

        buttonRepair.setEnabled(buildingView.getBuildingLevel() != 0 && !buildingView.isBuilding());
        if (buildingView.isRepairing())
        {
            buttonRepair.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.cancelRepair"));
        }
        else
        {
            buttonRepair.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.repair"));
        }
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        // Check if there is no page switcher
        // Or that we are on the correct page
        if (switchView == null || switchView.getCurrentView().getID().equals(PAGE_ACTIONS))
        {
            final AbstractBuilding.View buildingView = building.getColony().getBuilding(building.getID());

            if (buttonPrevPage != null)
            {
                buttonPrevPage.disable();
            }

            if (title != null)
            {
                title.setLabelText(LanguageHandler.format(getBuildingName()) + " " + buildingView.getBuildingLevel());
            }

            updateButtonBuild(buildingView);
            updateButtonRepair(buildingView);
        }
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
    }

    /**
     * Returns the name of a building.
     *
     * @return Name of a building.
     */
    public abstract String getBuildingName();
}
