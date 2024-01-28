package com.minecolonies.api.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;

/**
 * Key mappings
 */
public class ModKeyMappings
{
    private static final String CATEGORY = "key.minecolonies.categories.general";

    /**
     * Toggle
     */
    public static final Lazy<KeyMapping> TOGGLE_GOGGLES = Lazy.of(() -> new KeyMapping("key.minecolonies.toggle_goggles",
            KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), CATEGORY));

    /**
     * Register key mappings
     */
    public static void register(@NotNull final RegisterKeyMappingsEvent event)
    {
        event.register(TOGGLE_GOGGLES.get());
    }

    /**
     * Private constructor to hide the implicit one.
     */
    private ModKeyMappings()
    {
        /*
         * Intentionally left empty.
         */
    }
}
