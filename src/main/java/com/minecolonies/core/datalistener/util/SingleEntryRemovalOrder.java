package com.minecolonies.core.datalistener.util;

import net.minecraft.resources.Identifier;

/**
 * Removal order for removing a single datapack entry.
 *
 * @param key the resource location to remove.
 */
public record SingleEntryRemovalOrder(Identifier key) implements RemovalOrder
{
    @Override
    public boolean test(final Identifier resourceLocation)
    {
        return resourceLocation.equals(key);
    }
}
