package com.minecolonies.client.gui;

import com.blockout.controls.Button;
import com.blockout.views.Window;
import com.minecolonies.colony.buildings.BuildingWorker;

import java.util.HashMap;
import java.util.function.Consumer;

/**
 * manage windows and their events
 */
public abstract class AbstractWindowSkeleton<B extends BuildingWorker.View> extends Window implements Button.Handler
{
    private final HashMap<String, Consumer<Button>> buttons;

    public AbstractWindowSkeleton(final B building, final String resource)
    {
        super(resource);
        buttons = new HashMap<String, Consumer<Button>>();
    }

    public void registerButton(String id, Consumer<Button> action)
    {
        buttons.put(id, action);
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

    public final void doNothing(Button ignored)
    {
        //do nothing with that event
    }

}
