package com.minecolonies.api.loot;

import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.minecolonies.api.colony.jobs.ModJobs.FISHERMAN_ID;
import static com.minecolonies.api.util.constant.Constants.MAX_BUILDING_LEVEL;

/** List of custom loot tables used by the mod (other than those used in recipes) */
public final class ModLootTables
{
    /** Ids for the fisherman bonus loot tables */
    public static final Map<Integer, ResourceLocation> FISHERMAN_BONUS = createFishermanBonusMap();

    private static Map<Integer, ResourceLocation> createFishermanBonusMap()
    {
        final Map<Integer, ResourceLocation> map = new HashMap<>();
        for (int level = 1; level <= MAX_BUILDING_LEVEL; ++level)
        {
            map.put(level, new ResourceLocation(FISHERMAN_ID + Integer.toString(level)));
        }
        return Collections.unmodifiableMap(map);
    }
}
