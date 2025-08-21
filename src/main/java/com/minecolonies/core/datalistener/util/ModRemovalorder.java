package com.minecolonies.core.datalistener.util;

import net.minecraft.resources.ResourceLocation;

/**
 * Removal order for removing all datapack entries from an entire mod namespace.
 *
 * @param modId the given mod id.
 */
public record ModRemovalorder(String modId) implements RemovalOrder
{
    @Override
    public boolean test(final ResourceLocation resourceLocation)
    {
        return resourceLocation.getNamespace().equals(modId);
    }
}
