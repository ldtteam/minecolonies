package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.crafting.ClassicRecipe;
import com.minecolonies.api.crafting.ModRecipeTypes;
import com.minecolonies.api.crafting.MultiOutputRecipe;
import com.minecolonies.api.crafting.registry.RecipeTypeEntry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;

public final class ModRecipeTypesInitializer
{

    private ModRecipeTypesInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModRecipeTypesInitializer but this is a Utility class.");
    }

    public static void init(final RegisterEvent event)
    {
        final IForgeRegistry<RecipeTypeEntry> reg = event.getForgeRegistry();

        ModRecipeTypes.Classic = new RecipeTypeEntry.Builder()
                                .setRecipeTypeProducer(ClassicRecipe::new)
                                .setRegistryName(ModRecipeTypes.CLASSIC_ID)
                                .createRecipeTypeEntry();

        ModRecipeTypes.MultiOutput = new RecipeTypeEntry.Builder()
                                .setRecipeTypeProducer(MultiOutputRecipe::new)
                                .setRegistryName(ModRecipeTypes.MULTI_OUTPUT_ID)
                                .createRecipeTypeEntry();

        reg.register(ModRecipeTypes.CLASSIC_ID, ModRecipeTypes.Classic);
        reg.register(ModRecipeTypes.MULTI_OUTPUT_ID, ModRecipeTypes.MultiOutput);
    }
}
