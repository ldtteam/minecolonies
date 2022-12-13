package com.minecolonies.api.compatibility.dynmap;

/**
 * Compat class for when Dynmap is detected and the integration should be enabled.
 */
public class DynmapCompat extends DynmapProxy
{
    @Override
    public boolean isDynmapPresent()
    {
        return true;
    }
}