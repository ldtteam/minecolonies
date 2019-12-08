package com.minecolonies.api.colony.interactionhandling;

import net.minecraft.util.text.ITextComponent;

/**
 * The text component based unique interaction identifier.
 */
public class TextInteractionId implements IInteractionIdentifier
{
    /**
     * The text component which works at the identifier.
     */
    private final ITextComponent component;

    /**
     * Create a InteractionIdentifier based on a textcomponent.
     * @param component the text component to base it on.
     */
    public TextInteractionId(final ITextComponent component)
    {
        this.component = component;
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return this.component;
    }

    @Override
    public boolean equals(final Object o)
    {
        if ( ! (o instanceof TextInteractionId))
        {
            return false;
        }
        return component.equals(((TextInteractionId) o).component);
    }

    @Override
    public int hashCode()
    {
        return component.hashCode();
    }
}
