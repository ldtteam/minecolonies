package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.crafting.ModCraftingTypes;
import com.minecolonies.api.crafting.RecipeCraftingType;
import com.minecolonies.api.crafting.registry.CraftingType;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.recipes.ArchitectsCutterCraftingType;
import com.minecolonies.core.recipes.BrewingCraftingType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCraftingTypesInitializer
{
    public final static DeferredRegister<CraftingType>
       DEFERRED_REGISTER = DeferredRegister.create(new ResourceLocation(Constants.MOD_ID, "craftingtypes"), Constants.MOD_ID);

    private ModCraftingTypesInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModCraftingTypesInitializer but this is a Utility class.");
    }

    static
    {
            ModCraftingTypes.SMALL_CRAFTING = DEFERRED_REGISTER.register(ModCraftingTypes.SMALL_CRAFTING_ID.getPath(), () -> new RecipeCraftingType<>(ModCraftingTypes.SMALL_CRAFTING_ID,
              RecipeType.CRAFTING, r -> r.canCraftInDimensions(2, 2)));

            ModCraftingTypes.LARGE_CRAFTING = DEFERRED_REGISTER.register(ModCraftingTypes.LARGE_CRAFTING_ID.getPath(), () -> new RecipeCraftingType<>(ModCraftingTypes.LARGE_CRAFTING_ID,
              RecipeType.CRAFTING, r -> r.canCraftInDimensions(3, 3)
                                          && !r.canCraftInDimensions(2, 2)));

            ModCraftingTypes.SMELTING = DEFERRED_REGISTER.register(ModCraftingTypes.SMELTING_ID.getPath(), () -> new RecipeCraftingType<>(ModCraftingTypes.SMELTING_ID,
              RecipeType.SMELTING, null));

            ModCraftingTypes.BREWING = DEFERRED_REGISTER.register(ModCraftingTypes.BREWING_ID.getPath(), BrewingCraftingType::new);

            ModCraftingTypes.ARCHITECTS_CUTTER = DEFERRED_REGISTER.register(ModCraftingTypes.ARCHITECTS_CUTTER_ID.getPath(), () -> new ArchitectsCutterCraftingType());
    }
}
