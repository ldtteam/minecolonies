package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.crafting.ClassicRecipe;
import com.minecolonies.api.crafting.ModRecipeTypes;
import com.minecolonies.api.crafting.MultiOutputRecipe;
import com.minecolonies.api.crafting.registry.RecipeTypeEntry;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;

public final class ModRecipeTypesInitializer
{
    public final static DeferredRegister<RecipeTypeEntry> DEFERRED_REGISTER = DeferredRegister.create(new ResourceLocation(Constants.MOD_ID, "recipetypeentries"), Constants.MOD_ID);

    private ModRecipeTypesInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModRecipeTypesInitializer but this is a Utility class.");
    }

    static
    {
        ModRecipeTypes.Classic = DEFERRED_REGISTER.register(ModRecipeTypes.CLASSIC_ID.getPath(), () -> new RecipeTypeEntry.Builder()
                                .setRecipeTypeProducer(ClassicRecipe::new)
                                .createRecipeTypeEntry());

        ModRecipeTypes.MultiOutput = DEFERRED_REGISTER.register(ModRecipeTypes.MULTI_OUTPUT_ID.getPath(), () -> new RecipeTypeEntry.Builder()
                                .setRecipeTypeProducer(MultiOutputRecipe::new)
                                .createRecipeTypeEntry());
    }
}
