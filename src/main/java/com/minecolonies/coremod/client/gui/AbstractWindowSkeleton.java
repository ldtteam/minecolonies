package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.Log;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.ButtonHandler;
import com.minecolonies.blockout.views.Window;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Manage windows and their events.
 */
public abstract class AbstractWindowSkeleton extends Window implements ButtonHandler
{
    @NotNull
    private final HashMap<String, Consumer<Button>> buttons;

    /**
     * Constructor for the skeleton class of the windows.
     *
     * @param resource Resource location string.
     */
    public AbstractWindowSkeleton(final String resource)
    {
        super(resource);

        buttons = new HashMap<>();
    }

    /**
     * Register a button on the window.
     *
     * @param id     Button ID.
     * @param action Consumer with the action to be performed.
     */
    public final void registerButton(String id, Runnable action)
    {
        registerButton(id, (button) -> action.run());
    }

    /**
     * Register a button on the window.
     *
     * @param id     Button ID.
     * @param action Consumer with the action to be performed.
     */
    public final void registerButton(final String id, final Consumer<Button> action)
    {
        buttons.put(id, action);
    }

    /**
     * Handle a button clicked event.
     * Find the registered event and execute that.
     * <p>
     * todo: make final once migration is complete
     *
     * @param button the button that was clicked.
     */
    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        if (buttons.containsKey(button.getID()))
        {
            buttons.get(button.getID()).accept(button);
        }
        else
        {
            Log.getLogger().warn(this.getClass().getName() + ": Unhandled Button ID:" + button.getID());
        }
    }

    /**
     * Button clicked without an action. Method does nothing.
     *
     * @param ignored Parameter is ignored. Since some actions require a button, we must accept a button parameter.
     */
    public final void doNothing(final Button ignored)
    {
        //do nothing with that event
    }
}
