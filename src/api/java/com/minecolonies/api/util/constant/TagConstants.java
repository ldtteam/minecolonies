package com.minecolonies.api.util.constant;

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
    public static final ResourceLocation INDESTRUCTIBLE = new ResourceLocation(MOD_ID, "indestructible");
    public static final ResourceLocation MESHES = new ResourceLocation(MOD_ID, "meshes");
    public static final ResourceLocation HOSTILE = new ResourceLocation(MOD_ID, "hostile");
    public static final ResourceLocation BREAKABLE_ORE = new ResourceLocation(MOD_ID,"breakable_ore");
    public static final ResourceLocation RAW_ORE = new ResourceLocation(MOD_ID, "raw_ore");
    public static final ResourceLocation ARCHEOLOGIST_VISITABLE = new ResourceLocation(MOD_ID, "archeologist_visitable");

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
