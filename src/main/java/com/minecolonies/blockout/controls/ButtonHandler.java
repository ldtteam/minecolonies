package com.minecolonies.blockout.controls;

/**
 * Used for windows that have buttons and want to respond to clicks.
 */
@FunctionalInterface
public interface ButtonHandler
{
    /**
     * Called when a button is clicked.
     *
     * @param button the button that was clicked.
     */
    void onButtonClicked(Button button);
}
