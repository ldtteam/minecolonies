package com.minecolonies.api.crafting;

import com.minecolonies.api.crafting.registry.CraftingType;
import com.minecolonies.core.recipes.ArchitectsCutterCraftingType;
import com.minecolonies.core.recipes.BrewingCraftingType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.neoforged.neoforge.registries.DeferredHolder;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

public class ModCraftingTypes
{
    public static final Identifier SMALL_CRAFTING_ID = Identifier.fromNamespaceAndPath(MOD_ID, "smallcrafting");
    public static final Identifier LARGE_CRAFTING_ID = Identifier.fromNamespaceAndPath(MOD_ID, "largecrafting");
    public static final Identifier SMELTING_ID = Identifier.fromNamespaceAndPath(MOD_ID, "smelting");
    public static final Identifier BREWING_ID = Identifier.fromNamespaceAndPath(MOD_ID, "brewing");
    public static final Identifier ARCHITECTS_CUTTER_ID = Identifier.fromNamespaceAndPath("domum_ornamentum", "architects_cutter");

    public static DeferredHolder<CraftingType, RecipeCraftingType<CraftingInput, CraftingRecipe>>     SMALL_CRAFTING;
    public static DeferredHolder<CraftingType, RecipeCraftingType<CraftingInput, CraftingRecipe>>     LARGE_CRAFTING;
    public static DeferredHolder<CraftingType, RecipeCraftingType<SingleRecipeInput, SmeltingRecipe>> SMELTING;
    public static DeferredHolder<CraftingType, BrewingCraftingType>                                   BREWING;
    public static DeferredHolder<CraftingType, ArchitectsCutterCraftingType> ARCHITECTS_CUTTER;

    private ModCraftingTypes()
    {
        throw new IllegalStateException("Tried to initialize: ModCraftingTypes but this is a Utility class.");
    }
}
