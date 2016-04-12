package com.minecolonies.client.gui;

import com.blockout.controls.Button;
import com.blockout.controls.Label;
import com.blockout.views.Window;
import com.minecolonies.MineColonies;
import com.minecolonies.colony.buildings.BuildingHut;
import com.minecolonies.network.messages.BuildRequestMessage;
import com.minecolonies.network.messages.OpenInventoryMessage;
import com.minecolonies.util.LanguageHandler;

import java.util.HashMap;
import java.util.function.Consumer;

/**
 * manage windows and their events
 * @param <B>   Class extending {@link com.minecolonies.colony.buildings.BuildingHut.View}
 */
public abstract class AbstractWindowSkeleton<B extends BuildingHut.View> extends Window implements Button.Handler
{
    private static final String BUTTON_BUILD        = "build";
    private static final String BUTTON_REPAIR       = "repair";
    private static final String BUTTON_INVENTORY    = "inventory";
    private static final String LABEL_BUILDING_NAME = "name";
    /**
     * Type B is a class that extends {@link com.minecolonies.colony.buildings.BuildingWorker.View}
     */
    protected final B                                 building;
    private final   HashMap<String, Consumer<Button>> buttons;

    /**
     * Constructor for the skeleton class of the windows
     *
     * @param building      Class extending {@link com.minecolonies.colony.buildings.BuildingHut.View}
     * @param resource      Resource location string
     */
    public AbstractWindowSkeleton(final B building, final String resource)
    {
        super(resource);
        this.buttons = new HashMap<>();
        this.building = building;
        registerButton(BUTTON_BUILD, this::buildClicked);
        registerButton(BUTTON_REPAIR, this::repairClicked);
        registerButton(BUTTON_INVENTORY, this::inventoryClicked);
    }

    /**
     * Register a button on the window
     *
     * @param id        Button ID
     * @param action    Consumer with the action to be performed
     */
    public void registerButton(String id, Consumer<Button> action)
    {
        buttons.put(id, action);
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
     * Handle a button clicked event.
     * Find the registered event and execute that.
     * <p>
     * todo: make final once migration is complete
     *
     * @param button the button that was clicked
     */
    @Override
    public void onButtonClicked(Button button)
    {
        if (buttons.containsKey(button.getID()))
        {
            buttons.get(button.getID()).accept(button);
        }
    }

    /**
     * Button clicked without an action. Method is ignored
     *
     * @param ignored   Parameter is ignored, since some actions require a button.
     *                  This method does not
     */
    public final void doNothing(Button ignored)
    {
        //do nothing with that event
    }

    /**
     * Called when the Window is displayed.
     */
    @Override
    public void onOpened()
    {
        findPaneOfTypeByID(LABEL_BUILDING_NAME, Label.class).setLabel(
                LanguageHandler.getString(getBuildingName()));
        if (building.getBuildingLevel() == 0)
        {
            findPaneOfTypeByID(BUTTON_BUILD, Button.class).setLabel(
                    LanguageHandler.getString("com.minecolonies.gui.workerHuts.build"));
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
