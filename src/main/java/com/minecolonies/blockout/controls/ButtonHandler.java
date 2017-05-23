package com.minecolonies.blockout.controls;

import java.util.function.Consumer;

/**
 * Used for windows that have buttons and want to respond to clicks.
 */
@FunctionalInterface
public interface ButtonHandler extends Consumer<Button>
{
    /**
     * Called when a button is clicked.
     *
     * @param button the button that was clicked.
     */
    void onButtonClicked(Button button);

    default void accept(Button button)
    {
        onButtonClicked(button);
    }
}
