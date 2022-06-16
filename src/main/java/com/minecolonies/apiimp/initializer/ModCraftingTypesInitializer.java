package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.crafting.ModCraftingTypes;
import com.minecolonies.api.crafting.RecipeCraftingType;
import com.minecolonies.api.crafting.registry.CraftingType;
import com.minecolonies.coremod.recipes.ArchitectsCutterCraftingType;
import com.minecolonies.coremod.recipes.BrewingCraftingType;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;

public final class ModCraftingTypesInitializer
{
    private ModCraftingTypesInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModCraftingTypesInitializer but this is a Utility class.");
    }

    public static void init(final RegisterEvent event)
    {
        final IForgeRegistry<CraftingType> reg = event.getForgeRegistry();

        ModCraftingTypes.SMALL_CRAFTING = new RecipeCraftingType<>(ModCraftingTypes.SMALL_CRAFTING_ID,
                RecipeType.CRAFTING, r -> r.canCraftInDimensions(2, 2));
        reg.register(ModCraftingTypes.SMALL_CRAFTING_ID, ModCraftingTypes.SMALL_CRAFTING);

        ModCraftingTypes.LARGE_CRAFTING = new RecipeCraftingType<>(ModCraftingTypes.LARGE_CRAFTING_ID,
                RecipeType.CRAFTING, r -> r.canCraftInDimensions(3, 3)
                    && !r.canCraftInDimensions(2, 2));
        reg.register(ModCraftingTypes.LARGE_CRAFTING_ID, ModCraftingTypes.LARGE_CRAFTING);

        ModCraftingTypes.SMELTING = new RecipeCraftingType<>(ModCraftingTypes.SMELTING_ID,
                RecipeType.SMELTING, null);
        reg.register(ModCraftingTypes.SMELTING_ID, ModCraftingTypes.SMELTING);

        ModCraftingTypes.BREWING = new BrewingCraftingType();
        reg.register(ModCraftingTypes.BREWING_ID, ModCraftingTypes.BREWING);

        ModCraftingTypes.ARCHITECTS_CUTTER = new ArchitectsCutterCraftingType();
        reg.register(ModCraftingTypes.ARCHITECTS_CUTTER_ID, ModCraftingTypes.ARCHITECTS_CUTTER);
    }
}
