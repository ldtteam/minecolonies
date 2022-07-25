package com.minecolonies.api.crafting;

import com.minecolonies.api.crafting.registry.CraftingType;
import net.minecraft.util.ResourceLocation;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

public class ModCraftingTypes
{
    public static final ResourceLocation SMALL_CRAFTING_ID = new ResourceLocation(MOD_ID, "smallcrafting");
    public static final ResourceLocation LARGE_CRAFTING_ID = new ResourceLocation(MOD_ID, "largecrafting");
    public static final ResourceLocation SMELTING_ID = new ResourceLocation(MOD_ID, "smelting");
    public static final ResourceLocation BREWING_ID = new ResourceLocation(MOD_ID, "brewing");

    public static CraftingType SMALL_CRAFTING;
    public static CraftingType LARGE_CRAFTING;
    public static CraftingType SMELTING;
    public static CraftingType BREWING;

    private ModCraftingTypes()
    {
        throw new IllegalStateException("Tried to initialize: ModCraftingTypes but this is a Utility class.");
    }
}
