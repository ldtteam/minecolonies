package com.minecolonies.api.colony.event;

import com.minecolonies.api.colony.IColony;
import org.jetbrains.annotations.NotNull;

/**
 * Colony information changed event.
 */
public class ColonyInformationChangedEvent extends AbstractColonyEvent
{
    /**
     * What type of information changed on the colony.
     */
    private final Type type;

    /**
     * Constructs a colony information changed event.
     *
     * @param colony the colony related to the event.
     * @param type   what type of information changed on the colony.
     */
    public ColonyInformationChangedEvent(final @NotNull IColony colony, final Type type)
    {
        super(colony);
        this.type = type;
    }

    /**
     * Get what type of information changed on the colony.
     *
     * @return the enum value.
     */
    public Type getType()
    {
        return type;
    }

    /**
     * What information of the colony changed.
     */
    public enum Type
    {
        NAME,
        TEAM_COLOR,
        FLAG
    }
}
