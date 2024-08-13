package com.minecolonies.api.loot;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.minecolonies.api.colony.jobs.ModJobs.FISHERMAN_ID;
import static com.minecolonies.api.util.constant.Constants.MAX_BUILDING_LEVEL;

/** List of custom loot tables used by the mod (other than those used in recipes) */
public final class ModLootTables
{
    /** Fisherman primary loot table */
    public static final ResourceKey<LootTable> FISHING = ResourceKey.create(Registries.LOOT_TABLE, FISHERMAN_ID);

    /** Fisherman secondary fish table */
    public static final ResourceKey<LootTable> FISHING_FISH = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(FISHERMAN_ID + "/fish"));

    /** Fisherman secondary junk table */
    public static final ResourceKey<LootTable> FISHING_JUNK = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(FISHERMAN_ID + "/junk"));

    /** Fisherman secondary treasure table */
    public static final ResourceKey<LootTable> FISHING_TREASURE = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(FISHERMAN_ID + "/treasure"));

    /** Ids for the fisherman bonus loot tables */
    public static final Map<Integer, ResourceKey<LootTable>> FISHERMAN_BONUS = createFishermanBonusMap();

    private static Map<Integer, ResourceKey<LootTable>> createFishermanBonusMap()
    {
        final Map<Integer, ResourceKey<LootTable>> map = new HashMap<>();
        for (int level = 1; level <= MAX_BUILDING_LEVEL; ++level)
        {
            map.put(level, ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(FISHERMAN_ID + "/bonus" + level)));
        }
        return Collections.unmodifiableMap(map);
    }
}
