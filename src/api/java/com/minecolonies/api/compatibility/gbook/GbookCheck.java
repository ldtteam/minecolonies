package com.minecolonies.api.compatibility.gbook;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import gigaherz.guidebook.client.BookRegistryEvent;

/**
 * This class is for guidebook integration
 */
public final class GbookCheck
{
    private static final String GBOOK = "gbook";

    // Adds our book to gbook registry
    @SubscribeEvent
    @Optional.Method(modid = GBOOK)
    public static void registerBook(final BookRegistryEvent event) {
        event.register(new ResourceLocation(Constants.MOD_ID + ":book/minecolonies.xml"));
    }
}