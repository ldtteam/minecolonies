package com.minecolonies.api.crafting.registry;

import com.minecolonies.api.crafting.CompostRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.RegistryObject;

/**
 * Holds ref to the mod recipe serializers and recipe types.
 */
public class ModRecipeSerializer
{
    public static RegistryObject<CompostRecipe.Serializer> CompostRecipeSerializer;
    public static RegistryObject<RecipeType<CompostRecipe>>   CompostRecipeType;
}
