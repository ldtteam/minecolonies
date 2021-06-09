package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.crafting.ClassicRecipe;
import com.minecolonies.api.crafting.ModRecipeTypes;
import com.minecolonies.api.crafting.MultiOutputRecipe;
import com.minecolonies.api.crafting.registry.RecipeTypeEntry;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;

public final class ModRecipeTypesInitializer
{

    private ModRecipeTypesInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModRecipeTypesInitializer but this is a Utility class.");
    }

    public static void init(final RegistryEvent.Register<RecipeTypeEntry> event)
    {
        final IForgeRegistry<RecipeTypeEntry> reg = event.getRegistry();

        ModRecipeTypes.Classic = new RecipeTypeEntry.Builder()
                                .setRecipeTypeProducer(ClassicRecipe::new)
                                .setRegistryName(ModRecipeTypes.CLASSIC_ID)
                                .createRecipeTypeEntry();

        ModRecipeTypes.MultiOutput = new RecipeTypeEntry.Builder()
                                .setRecipeTypeProducer(MultiOutputRecipe::new)
                                .setRegistryName(ModRecipeTypes.MULTI_OUTPUT_ID)
                                .createRecipeTypeEntry();

        reg.register(ModRecipeTypes.Classic);
        reg.register(ModRecipeTypes.MultiOutput);
    }
}
