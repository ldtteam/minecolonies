package com.minecolonies.api.colony.interactionhandling;

import net.minecraft.util.text.ITextComponent;

/**
 * Identifier for the interaction classes.
 */
public interface IInteractionIdentifier
{
    /**
     * Get the display name.
     * @return the display name.
     */
    ITextComponent getDisplayName();
}
