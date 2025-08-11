package com.minecolonies.api.colony.connections;

/**
 * Diplomacy Status between two colonies.
 */
public enum ConnectionEventType
{
    ALLY_REQUEST,
    ALLY_CONFIRMED,
    FEUD_STARTED,
    NEUTRAL_SET,
    DISCONNECTED;

    /**
     * Get translation key for the diplomacy status.
     * @return the string key.
     */
    public String translationKey()
    {
        return "com.minecolonies.core.gui.connectionevent." + name().toLowerCase();
    }
}
