package com.minecolonies.api.util;

import net.minecraft.client.Minecraft;

public final class DebugUtil {

    private DebugUtil() {
        throw new IllegalStateException("Tried to initialize: DebugUtil but this is a Utility class.");
    }

    public static boolean hasReleaseMouse() {
        Minecraft.getInstance().mouseHelper.ungrabMouse();
        return true;
    }
}
