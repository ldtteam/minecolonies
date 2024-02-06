package com.minecolonies.api.crafting;

import com.minecolonies.api.crafting.registry.CraftingType;
import com.minecolonies.core.recipes.ArchitectsCutterCraftingType;
import com.minecolonies.core.recipes.BrewingCraftingType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.neoforged.neoforge.registries.DeferredHolder;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

public class ModCraftingTypes
{
    public static final ResourceLocation SMALL_CRAFTING_ID = new ResourceLocation(MOD_ID, "smallcrafting");
    public static final ResourceLocation LARGE_CRAFTING_ID = new ResourceLocation(MOD_ID, "largecrafting");
    public static final ResourceLocation SMELTING_ID = new ResourceLocation(MOD_ID, "smelting");
    public static final ResourceLocation BREWING_ID = new ResourceLocation(MOD_ID, "brewing");
    public static final ResourceLocation ARCHITECTS_CUTTER_ID = new ResourceLocation("domum_ornamentum", "architects_cutter");

    public static DeferredHolder<CraftingType, RecipeCraftingType<CraftingContainer, CraftingRecipe>> SMALL_CRAFTING;
    public static DeferredHolder<CraftingType, RecipeCraftingType<CraftingContainer, CraftingRecipe>> LARGE_CRAFTING;
    public static DeferredHolder<CraftingType, RecipeCraftingType<Container, SmeltingRecipe>> SMELTING;
    public static DeferredHolder<CraftingType, BrewingCraftingType> BREWING;
    public static DeferredHolder<CraftingType, ArchitectsCutterCraftingType> ARCHITECTS_CUTTER;

    private ModCraftingTypes()
    {
        throw new IllegalStateException("Tried to initialize: ModCraftingTypes but this is a Utility class.");
    }
}
