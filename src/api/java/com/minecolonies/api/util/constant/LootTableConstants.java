package com.minecolonies.api.util.constant;

import net.minecraft.util.ResourceLocation;

public final class LootTableConstants
{

    /**
     * Loot table of the entity.
     */
    public static final ResourceLocation ARCHER_BARBARIAN_DROPS = new ResourceLocation(Constants.MOD_ID, "EntityArcherBarbarianDrops");
    /**
     * Loot table of the entity.
     */
    public static final ResourceLocation CHIEF_BARBARIAN_DROPS  = new ResourceLocation(Constants.MOD_ID, "EntityChiefBarbarianDrops");
    /**
     * Loot table of the entity.
     */
    public static final ResourceLocation MELEE_BARBARIAN_DROPS  = new ResourceLocation(Constants.MOD_ID, "EntityBarbarianDrops");
    /**
     * Loot table of the entity.
     */
    public static final ResourceLocation ARCHER_PIRATE_DROPS    = new ResourceLocation(Constants.MOD_ID, "entityarcherpiratedrops"); //TODO: Check if this is correct.
    /**
     * Loot table of the entity.
     */
    public static final ResourceLocation CHIEF_PIRATE_DROPS     = new ResourceLocation(Constants.MOD_ID, "entitychiefpiratedrops");
    /**
     * Loot table of the entity.
     */
    public static final ResourceLocation MELEE_PIRATE_DROPS     = new ResourceLocation(Constants.MOD_ID, "entitypiratedrops");

    private LootTableConstants()
    {
        throw new IllegalStateException("Tried to initialize: LootTableConstants but this is a Utility class.");
    }
}
