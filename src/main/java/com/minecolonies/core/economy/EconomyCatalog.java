package com.minecolonies.core.economy;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The v1 catalog intentionally uses vanilla items only. Adding another item
 * is a data change here and does not require a client-side registry or screen.
 */
public final class EconomyCatalog
{
    private static final List<EconomyCommodity> COMMODITIES = List.of(
        new EconomyCommodity("minecraft:wheat", 32, 1, 2, 12, 3, 64),
        new EconomyCommodity("minecraft:carrot", 32, 1, 2, 12, 3, 64),
        new EconomyCommodity("minecraft:potato", 32, 1, 2, 12, 3, 64),
        new EconomyCommodity("minecraft:oak_log", 16, 2, 3, 10, 2, 32),
        new EconomyCommodity("minecraft:cobblestone", 64, 1, 2, 12, 3, 128),
        new EconomyCommodity("minecraft:coal", 16, 2, 3, 8, 2, 32),
        new EconomyCommodity("minecraft:iron_ingot", 8, 4, 6, 6, 1, 16),
        new EconomyCommodity("minecraft:bread", 16, 2, 3, 8, 2, 32),
        new EconomyCommodity("minecraft:paper", 16, 2, 3, 8, 2, 32),
        new EconomyCommodity("minecraft:white_wool", 16, 2, 3, 8, 2, 32),
        new EconomyCommodity("minecraft:leather", 8, 3, 5, 6, 1, 16),
        new EconomyCommodity("minecraft:cod", 8, 2, 3, 6, 1, 16)
    );

    private static final Map<String, EconomyCommodity> BY_ITEM = COMMODITIES.stream()
        .collect(Collectors.toUnmodifiableMap(EconomyCommodity::itemId, Function.identity()));

    private EconomyCatalog()
    {
    }

    public static List<EconomyCommodity> commodities()
    {
        return COMMODITIES;
    }

    public static EconomyCommodity byItemId(final String itemId)
    {
        return BY_ITEM.get(itemId);
    }
}
