package com.minecolonies.core.datalistener.util;

import net.minecraft.resources.ResourceLocation;

import java.util.function.Predicate;

/**
 * Interface for implementing different removal orders.
 */
public interface RemovalOrder extends Predicate<ResourceLocation>
{
}
