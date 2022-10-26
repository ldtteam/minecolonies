package com.minecolonies.api.colony.busevents;

public interface IColonyStateEvent extends IColonyEvent
{
    /**
     * Return the new active state the colony is going into
     *
     * @return true if active
     */
    boolean isColonyActive();
}
