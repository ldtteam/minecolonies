package com.minecolonies.core.datalistener.util;

import net.minecraft.resources.ResourceLocation;

/**
 * Removal order for removing all other mods their datapack entries, except the given mod id.
 *
 * @param modId the given mod id.
 */
public record OtherModsRemovalOrder(String modId) implements RemovalOrder
{
    @Override
    public boolean test(final ResourceLocation resourceLocation)
    {
        return !resourceLocation.getNamespace().equals(modId);
    }
}
