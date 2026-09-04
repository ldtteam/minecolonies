package com.minecolonies.core.datalistener.util;

import net.minecraft.resources.Identifier;

import java.util.function.Predicate;

/**
 * Interface for implementing different removal orders.
 */
public interface RemovalOrder extends Predicate<Identifier>
{
}
