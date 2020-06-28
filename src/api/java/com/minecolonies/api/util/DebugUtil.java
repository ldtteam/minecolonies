package com.minecolonies.api.util;

import net.minecraft.client.Minecraft;

/**
 * Debug utility class.
 */
public final class DebugUtil
{
    /**
     * Private constructor to hide implicit one.
     */
    private DebugUtil()
    {
        throw new IllegalStateException("Tried to initialize: DebugUtil but this is a Utility class.");
    }

    /**
     * Called either in watchlist or debug condition to release the mouse automatically.
     * @return true always on call.
     */
    public static boolean hasReleaseMouse()
    {
        Minecraft.getInstance().mouseHelper.ungrabMouse();
        return true;
    }
}
