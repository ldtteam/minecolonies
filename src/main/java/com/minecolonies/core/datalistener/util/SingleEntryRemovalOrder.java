package com.minecolonies.core.datalistener.util;

import net.minecraft.resources.ResourceLocation;

/**
 * Removal order for removing a single datapack entry.
 *
 * @param key the resource location to remove.
 */
public record SingleEntryRemovalOrder(ResourceLocation key) implements RemovalOrder
{
    @Override
    public boolean test(final ResourceLocation resourceLocation)
    {
        return resourceLocation.equals(key);
    }
}
