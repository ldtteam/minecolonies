package com.minecolonies.api.util.constant;

import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.jobs.ModJobs;
import net.minecraft.resources.Identifier;
import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * Constants for the block/item tags defined by Minecolonies
 */
public final class TagConstants
{
    public static final Identifier DECORATION_ITEMS          = Identifier.fromNamespaceAndPath(MOD_ID, "decoblocks");
    public static final Identifier CONCRETE_POWDER           = Identifier.fromNamespaceAndPath(MOD_ID, "concrete_powder");
    public static final Identifier CONCRETE_BLOCK            = Identifier.fromNamespaceAndPath(MOD_ID, "concrete");
    public static final Identifier PATHING_BLOCKS            = Identifier.fromNamespaceAndPath(MOD_ID, "pathblocks");
    public static final Identifier DANGEROUS_BLOCKS          = Identifier.fromNamespaceAndPath(MOD_ID, "dangerousblocks");
    public static final Identifier FREE_CLIMB_BLOCKS         = Identifier.fromNamespaceAndPath(MOD_ID, "freeclimbblocks");
    public static final Identifier TIER0_BLOCKS              = Identifier.fromNamespaceAndPath(MOD_ID, "tier0blocks");
    public static final Identifier TIER1_BLOCKS              = Identifier.fromNamespaceAndPath(MOD_ID, "tier1blocks");
    public static final Identifier TIER2_BLOCKS              = Identifier.fromNamespaceAndPath(MOD_ID, "tier2blocks");
    public static final Identifier TIER3_BLOCKS              = Identifier.fromNamespaceAndPath(MOD_ID, "tier3blocks");
    public static final Identifier TIER4_BLOCKS              = Identifier.fromNamespaceAndPath(MOD_ID, "tier4blocks");
    public static final Identifier TIER5_BLOCKS              = Identifier.fromNamespaceAndPath(MOD_ID, "tier5blocks");
    public static final Identifier TIER6_BLOCKS              = Identifier.fromNamespaceAndPath(MOD_ID, "tier6blocks");
    public static final Identifier MANGROVE_TREE_BLOCKS      = Identifier.fromNamespaceAndPath(MOD_ID, "mangrove_tree");
    public static final Identifier EXTRA_TREE_BLOCKS         = Identifier.fromNamespaceAndPath(MOD_ID, "extra_tree");
    public static final Identifier TREE_BLOCKS               = Identifier.fromNamespaceAndPath(MOD_ID, "tree");
    public static final Identifier FLORIST_FLOWERS           = Identifier.fromNamespaceAndPath(MOD_ID, "florist_flowers");
    public static final Identifier EXCLUDED_FOOD             = Identifier.fromNamespaceAndPath(MOD_ID, "excluded_food");
    public static final Identifier ORECHANCEBLOCKS           = Identifier.fromNamespaceAndPath(MOD_ID, "orechanceblocks");
    public static final Identifier VALIDSPAWNBLOCKS          = Identifier.fromNamespaceAndPath(MOD_ID, "validspawnblocks");
    public static final Identifier COLONYPROTECTIONEXCEPTION = Identifier.fromNamespaceAndPath(MOD_ID, "protectionexception");
    public static final Identifier MUSHROOMS                 = Identifier.fromNamespaceAndPath(MOD_ID, "mushrooms");
    public static final Identifier MUSHROOMS_HUGE            = Identifier.fromNamespaceAndPath(MOD_ID, "mushrooms_huge");
    public static final Identifier FUNGI                     = Identifier.fromNamespaceAndPath(MOD_ID, "fungi");
    public static final Identifier COMPOSTABLES              = Identifier.fromNamespaceAndPath(MOD_ID, "compostables");
    public static final Identifier COMPOSTABLES_POOR         = Identifier.fromNamespaceAndPath(MOD_ID, "compostables_poor");
    public static final Identifier COMPOSTABLES_RICH         = Identifier.fromNamespaceAndPath(MOD_ID, "compostables_rich");
    public static final Identifier INDESTRUCTIBLE            = Identifier.fromNamespaceAndPath(MOD_ID, "indestructible");
    public static final Identifier MESHES                    = Identifier.fromNamespaceAndPath(MOD_ID, "meshes");
    public static final Identifier HOSTILE                   = Identifier.fromNamespaceAndPath(MOD_ID, "hostile");
    public static final Identifier BREAKABLE_ORE             = Identifier.fromNamespaceAndPath(MOD_ID, "breakable_ore");
    public static final Identifier RAW_ORE                   = Identifier.fromNamespaceAndPath(MOD_ID, "raw_ore");
    public static final Identifier MOB_ATTACK_BLACKLIST      = Identifier.fromNamespaceAndPath(MOD_ID, "mob_attack_blacklist");
    public static final Identifier RAIDERS                   = Identifier.fromNamespaceAndPath(MOD_ID, "raiders");
    public static final Identifier IGNORE_NBT                = Identifier.fromNamespaceAndPath(MOD_ID, "ignore_nbt");
    public static final Identifier ALLOW_INTERACT            = Identifier.fromNamespaceAndPath(MOD_ID, "allowinteract");
    public static final Identifier COLD_BIOMES               = Identifier.fromNamespaceAndPath(MOD_ID, "coldbiomes");
    public static final Identifier TEMPERATE_BIOMES          = Identifier.fromNamespaceAndPath(MOD_ID, "temperatebiomes");
    public static final Identifier HUMID_BIOMES              = Identifier.fromNamespaceAndPath(MOD_ID, "humidbiomes");
    public static final Identifier DRY_BIOMES                = Identifier.fromNamespaceAndPath(MOD_ID, "drybiomes");
    public static final Identifier POISONOUS_FOOD            = Identifier.fromNamespaceAndPath(MOD_ID, "poisonousfood");
    public static final Identifier RAW_MEAT                  = Identifier.fromNamespaceAndPath(MOD_ID, "rawmeat");
    public static final Identifier FEED                      = Identifier.fromNamespaceAndPath(MOD_ID, "feed");
    public static final Identifier LEATHER                   = Identifier.fromNamespaceAndPath(MOD_ID, "leather");

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
