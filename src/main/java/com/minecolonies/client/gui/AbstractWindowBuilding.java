package com.minecolonies.client.gui;

import com.blockout.controls.Button;
import com.blockout.controls.Label;
import com.minecolonies.MineColonies;
import com.minecolonies.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.colony.buildings.AbstractBuildingHut;
import com.minecolonies.network.messages.BuildRequestMessage;
import com.minecolonies.network.messages.OpenInventoryMessage;
import com.minecolonies.util.LanguageHandler;

/**
 * Manage windows associated with Buildings.
 *
 * @param <B>   Class extending {@link AbstractBuildingHut.View}.
 */
public abstract class AbstractWindowBuilding<B extends AbstractBuildingHut.View> extends AbstractWindowSkeleton
{
    private static final String BUTTON_BUILD        = "build";
    private static final String BUTTON_REPAIR       = "repair";
    private static final String BUTTON_INVENTORY    = "inventory";
    private static final String LABEL_BUILDING_NAME = "name";

    /**
     * Type B is a class that extends {@link AbstractBuildingWorker.View}.
     */
    protected final B building;

    /**
     * Constructor for the windows that are associated with buildings.
     *
     * @param building      Class extending {@link AbstractBuildingHut.View}.
     * @param resource      Resource location string.
     */
    public AbstractWindowBuilding(final B building, final String resource)
    {
        super(resource);

        this.building = building;
        registerButton(BUTTON_BUILD, this::buildClicked);
        registerButton(BUTTON_REPAIR, this::repairClicked);
        registerButton(BUTTON_INVENTORY, this::inventoryClicked);
    }

    /**
     * Action when build button is clicked
     *
     * @param ignored   Parameter is ignored, since some actions require a button.
     *                  This method does not
     */
    private void buildClicked(Button ignored)
    {
        MineColonies.getNetwork().sendToServer(new BuildRequestMessage(building, BuildRequestMessage.BUILD));
    }

    /**
     * Action when repair button is clicked
     *
     * @param ignored   Parameter is ignored, since some actions require a button.
     *                  This method does not
     */
    private void repairClicked(Button ignored)
    {
        MineColonies.getNetwork().sendToServer(new BuildRequestMessage(building, BuildRequestMessage.REPAIR));
    }

    /**
     * Action when a button opening an inventory is clicked
     *
     * @param ignored   Parameter is ignored, since some actions require a button.
     *                  This method does not
     */
    private void inventoryClicked(Button ignored)
    {
        MineColonies.getNetwork().sendToServer(new OpenInventoryMessage(building));
    }

    /**
     * Called when the Window is displayed.
     */
    @Override
    public void onOpened()
    {
        findPaneOfTypeByID(LABEL_BUILDING_NAME, Label.class).setText(LanguageHandler.getString(getBuildingName()));

        if (building.getBuildingLevel() == 0)
        {
            findPaneOfTypeByID(BUTTON_BUILD, Button.class).setLabel(LanguageHandler.getString("com.minecolonies.gui.workerHuts.build"));
            findPaneByID(BUTTON_REPAIR).disable();
        }
        else if (building.isBuildingMaxLevel())
        {
            Button button = findPaneOfTypeByID(BUTTON_BUILD, Button.class);
            button.setLabel(LanguageHandler.getString("com.minecolonies.gui.workerHuts.upgradeUnavailable"));
            button.disable();
        }
    }

    /**
     * Returns the name of a building
     *
     * @return Name of a building
     */
    public abstract String getBuildingName();
}
