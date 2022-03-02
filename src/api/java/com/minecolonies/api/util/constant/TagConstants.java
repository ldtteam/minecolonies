package com.minecolonies.api.util.constant;

import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.jobs.ModJobs;
import net.minecraft.resources.ResourceLocation;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/** Constants for the block/item tags defined by Minecolonies */
public final class TagConstants
{
    public static final ResourceLocation DECORATION_ITEMS = new ResourceLocation(MOD_ID, "decoblocks");
    public static final ResourceLocation CONCRETE_POWDER = new ResourceLocation(MOD_ID, "concrete_powder");
    public static final ResourceLocation CONCRETE_BLOCK  = new ResourceLocation(MOD_ID, "concrete");
    public static final ResourceLocation PATHING_BLOCKS = new ResourceLocation(MOD_ID, "pathblocks");
    public static final ResourceLocation FLORIST_FLOWERS = new ResourceLocation(MOD_ID, "florist_flowers");
    public static final ResourceLocation EXCLUDED_FOOD = new ResourceLocation(MOD_ID, "excluded_food");
    public static final ResourceLocation ORECHANCEBLOCKS = new ResourceLocation(MOD_ID, "orechanceblocks");
    public static final ResourceLocation COLONYPROTECTIONEXCEPTION = new ResourceLocation(MOD_ID, "protectionexception");
    public static final ResourceLocation FUNGI = new ResourceLocation(MOD_ID, "fungi");
    public static final ResourceLocation COMPOSTABLES = new ResourceLocation(MOD_ID, "compostables");
    public static final ResourceLocation COMPOSTABLES_POOR = new ResourceLocation(MOD_ID, "compostables_poor");
    public static final ResourceLocation COMPOSTABLES_RICH = new ResourceLocation(MOD_ID, "compostables_rich");
    public static final ResourceLocation INDESTRUCTIBLE = new ResourceLocation(MOD_ID, "indestructible");
    public static final ResourceLocation MESHES = new ResourceLocation(MOD_ID, "meshes");
    public static final ResourceLocation HOSTILE = new ResourceLocation(MOD_ID, "hostile");
    public static final ResourceLocation BREAKABLE_ORE = new ResourceLocation(MOD_ID, "breakable_ore");
    public static final ResourceLocation RAW_ORE = new ResourceLocation(MOD_ID, "raw_ore");
    public static final ResourceLocation MOB_ATTACK_BLACKLIST = new ResourceLocation(MOD_ID, "mob_attack_blacklist");

    public static final String CRAFTING_BAKER = ModJobs.BAKER_ID.getPath();
    public static final String CRAFTING_BLACKSMITH = ModJobs.BLACKSMITH_ID.getPath();
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
