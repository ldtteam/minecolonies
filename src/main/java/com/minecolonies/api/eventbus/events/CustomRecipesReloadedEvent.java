package com.minecolonies.api.eventbus.events;

/**
 * This event is fired on the client side whenever the CustomRecipeManager has been
 * populated. This occurs once on world load/connect and again whenever data-packs are reloaded.
 */
public class CustomRecipesReloadedEvent extends AbstractModEvent
{
    public CustomRecipesReloadedEvent()
    {
    }
}
