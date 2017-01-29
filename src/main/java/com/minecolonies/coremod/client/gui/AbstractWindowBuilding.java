package com.minecolonies.coremod.client.gui;

import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.Label;
import com.minecolonies.blockout.views.SwitchView;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingHut;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.network.messages.BuildRequestMessage;
import com.minecolonies.coremod.network.messages.OpenInventoryMessage;
import com.minecolonies.coremod.util.LanguageHandler;
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
    private static final String BUTTON_PREVPAGE             = "prevPage";
    private static final String BUTTON_NEXTPAGE             = "nextPage";
    private static final String VIEW_PAGES = "pages";

    private final Button buttonPrevPage;
    private final Button buttonNextPage;

    /**
     * Type B is a class that extends {@link AbstractBuildingWorker.View}.
     */
    protected final B building;

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
        buttonNextPage = findPaneOfTypeByID(BUTTON_NEXTPAGE, Button.class);
        buttonPrevPage = findPaneOfTypeByID(BUTTON_PREVPAGE, Button.class);
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
     * Called when the Window is displayed.
     */
    @Override
    public void onOpened()
    {
        if(buttonPrevPage != null)
        {
            findPaneOfTypeByID(BUTTON_PREVPAGE, Button.class).setEnabled(false);
        }
        findPaneOfTypeByID(LABEL_BUILDING_NAME, Label.class).setLabelText(LanguageHandler.format(getBuildingName()) + " " + building.getBuildingLevel());

        if (building.getBuildingLevel() == 0)
        {
            findPaneOfTypeByID(BUTTON_BUILD, Button.class).setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.build"));
            findPaneByID(BUTTON_REPAIR).disable();
        }
        else if (building.isBuildingMaxLevel())
        {
            final Button button = findPaneOfTypeByID(BUTTON_BUILD, Button.class);
            button.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.upgradeUnavailable"));
            button.disable();
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
