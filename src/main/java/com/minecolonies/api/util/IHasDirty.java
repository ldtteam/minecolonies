package com.minecolonies.api.util;

/**
 * Interface for all classes that require some kind of dirty handling.
 */
public interface IHasDirty
{
    /**
     * Specific dirty marking of modules (separate from building dirty).
     */
    void markDirty();

    /**
     * Check if one of the modules is dirty.
     * @return true if so.
     */
    boolean checkDirty();

    /**
     * Clear the dirty setting of the module.
     */
    void clearDirty();
}
