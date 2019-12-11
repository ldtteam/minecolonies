package com.minecolonies.api.colony.interactionhandling;

public interface IChatPriority
{
    /**
     * Get the priority of the interaction.
     * @return an int, the higher, the more important.
     */
    int getPriority();
}
