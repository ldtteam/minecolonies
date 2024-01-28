package com.minecolonies.api.colony.interactionhandling;

/**
 * Different priority types of the interactions.
 */
public enum ChatPriority implements IChatPriority
{
    HIDDEN,
    CHITCHAT,
    PENDING,
    IMPORTANT,
    BLOCKING;

    @Override
    public int getPriority()
    {
        return this.ordinal();
    }
}
