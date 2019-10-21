package com.minecolonies.blockout.controls;

import java.util.function.Consumer;

/**
 * Used for windows that have buttons and want to respond to clicks.
 */
@FunctionalInterface
public interface ButtonHandler extends Consumer<Button>
{
    default void accept(final Button button)
    {
        onButtonClicked(button);
    }

    /**
     * Called when a button is clicked.
     *
     * @param button the button that was clicked.
     */
    void onButtonClicked(Button button);
}
