package com.minecolonies.api.util.constant;

import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.jobs.ModJobs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * Constants for the block/item tags defined by Minecolonies
 */
public final class TagConstants
{
    public static final ResourceLocation DECORATION_ITEMS          = new ResourceLocation(MOD_ID, "decoblocks");
    public static final ResourceLocation CONCRETE_POWDER           = new ResourceLocation(MOD_ID, "concrete_powder");
    public static final ResourceLocation CONCRETE_BLOCK            = new ResourceLocation(MOD_ID, "concrete");
    public static final ResourceLocation PATHING_BLOCKS            = new ResourceLocation(MOD_ID, "pathblocks");
    public static final ResourceLocation DANGEROUS_BLOCKS          = new ResourceLocation(MOD_ID, "dangerousblocks");
    public static final ResourceLocation FREE_CLIMB_BLOCKS         = new ResourceLocation(MOD_ID, "freeclimbblocks");
    public static final ResourceLocation TIER0_BLOCKS              = new ResourceLocation(MOD_ID, "tier0blocks");
    public static final ResourceLocation TIER1_BLOCKS              = new ResourceLocation(MOD_ID, "tier1blocks");
    public static final ResourceLocation TIER2_BLOCKS              = new ResourceLocation(MOD_ID, "tier2blocks");
    public static final ResourceLocation TIER3_BLOCKS              = new ResourceLocation(MOD_ID, "tier3blocks");
    public static final ResourceLocation TIER4_BLOCKS              = new ResourceLocation(MOD_ID, "tier4blocks");
    public static final ResourceLocation TIER5_BLOCKS              = new ResourceLocation(MOD_ID, "tier5blocks");
    public static final ResourceLocation TIER6_BLOCKS              = new ResourceLocation(MOD_ID, "tier6blocks");
    public static final ResourceLocation MANGROVE_TREE_BLOCKS      = new ResourceLocation(MOD_ID, "mangrove_tree");
    public static final ResourceLocation TREE_BLOCKS               = new ResourceLocation(MOD_ID, "tree");
    public static final ResourceLocation FLORIST_FLOWERS           = new ResourceLocation(MOD_ID, "florist_flowers");
    public static final ResourceLocation EXCLUDED_FOOD             = new ResourceLocation(MOD_ID, "excluded_food");
    public static final ResourceLocation ORECHANCEBLOCKS           = new ResourceLocation(MOD_ID, "orechanceblocks");
    public static final ResourceLocation VALIDSPAWNBLOCKS          = new ResourceLocation(MOD_ID, "validspawnblocks");
    public static final ResourceLocation COLONYPROTECTIONEXCEPTION = new ResourceLocation(MOD_ID, "protectionexception");
    public static final ResourceLocation MUSHROOMS                 = new ResourceLocation(MOD_ID, "mushrooms");
    public static final ResourceLocation MUSHROOMS_HUGE            = new ResourceLocation(MOD_ID, "mushrooms_huge");
    public static final ResourceLocation FUNGI                     = new ResourceLocation(MOD_ID, "fungi");
    public static final ResourceLocation COMPOSTABLES              = new ResourceLocation(MOD_ID, "compostables");
    public static final ResourceLocation COMPOSTABLES_POOR         = new ResourceLocation(MOD_ID, "compostables_poor");
    public static final ResourceLocation COMPOSTABLES_RICH         = new ResourceLocation(MOD_ID, "compostables_rich");
    public static final ResourceLocation INDESTRUCTIBLE            = new ResourceLocation(MOD_ID, "indestructible");
    public static final ResourceLocation MESHES                    = new ResourceLocation(MOD_ID, "meshes");
    public static final ResourceLocation HOSTILE                   = new ResourceLocation(MOD_ID, "hostile");
    public static final ResourceLocation BREAKABLE_ORE             = new ResourceLocation(MOD_ID, "breakable_ore");
    public static final ResourceLocation RAW_ORE                   = new ResourceLocation(MOD_ID, "raw_ore");
    public static final ResourceLocation MOB_ATTACK_BLACKLIST      = new ResourceLocation(MOD_ID, "mob_attack_blacklist");
    public static final ResourceLocation RAIDERS                   = new ResourceLocation(MOD_ID, "raiders");
    public static final ResourceLocation IGNORE_NBT                = new ResourceLocation(MOD_ID, "ignore_nbt");
    public static final ResourceLocation ALLOW_INTERACT            = new ResourceLocation(MOD_ID, "allowinteract");
    public static final ResourceLocation COLD_BIOMES               = new ResourceLocation(MOD_ID, "coldbiomes");
    public static final ResourceLocation TEMPERATE_BIOMES          = new ResourceLocation(MOD_ID, "temperatebiomes");
    public static final ResourceLocation HUMID_BIOMES              = new ResourceLocation(MOD_ID, "humidbiomes");
    public static final ResourceLocation DRY_BIOMES                = new ResourceLocation(MOD_ID, "drybiomes");
    public static final ResourceLocation POISONOUS_FOOD            = new ResourceLocation(MOD_ID, "poisonousfood");

    public static final String CRAFTING_BAKER                = ModJobs.BAKER_ID.getPath();
    public static final String CRAFTING_BLACKSMITH           = ModJobs.BLACKSMITH_ID.getPath();
    public static final String CRAFTING_COOK = ModJobs.COOK_ID.getPath();
    public static final String CRAFTING_DYER = ModJobs.DYER_ID.getPath();
    public static final String CRAFTING_DYER_SMELTING = ModJobs.DYER_ID.getPath() + "_smelting";
    public static final String CRAFTING_FARMER = ModJobs.FARMER_ID.getPath();
    public static final String CRAFTING_FLETCHER = ModJobs.FLETCHER_ID.getPath();
    public static final String CRAFTING_GLASSBLOWER = ModJobs.GLASSBLOWER_ID.getPath();
    public static final String CRAFTING_GLASSBLOWER_SMELTING = ModJobs.GLASSBLOWER_ID.getPath() + "_smelting";
    public static final String CRAFTING_MECHANIC = ModJobs.MECHANIC_ID.getPath();
    public static final String CRAFTING_PLANTATION = ModBuildings.PLANTATION_ID;
    public static final String CRAFTING_SAWMILL = ModJobs.SAWMILL_ID.getPath();
    public static final String CRAFTING_STONEMASON = ModJobs.STONEMASON_ID.getPath();
    public static final String CRAFTING_STONE_SMELTERY = ModJobs.STONE_SMELTERY_ID.getPath();
    public static final String CRAFTING_REDUCEABLE = "reduceable";      // recipe improvement

    /**
     * Private constructor to hide implicit public one.
     */
    private TagConstants()
    {
        /*
         * Intentionally left empty.
         */
    }
}
