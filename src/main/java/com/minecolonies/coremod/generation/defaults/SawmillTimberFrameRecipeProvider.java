package com.minecolonies.coremod.generation.defaults;

import com.minecolonies.coremod.generation.CustomRecipeProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.function.Consumer;

import static com.ldtteam.structurize.api.util.constant.Constants.MOD_ID;


public class SawmillTimberFrameRecipeProvider extends CustomRecipeProvider
{
    public SawmillTimberFrameRecipeProvider(final DataGenerator generatorIn)
    {
        super(generatorIn);
    }

    @Override
    protected void registerRecipes(final Consumer<FinishedRecipe> consumer)
    {
       //todo 1.17
    }

    private static ItemStack idToStructurize(final String name, final int count)
    {
        return idToStack(new ResourceLocation(MOD_ID, name), count);
    }

    private static ItemStack idToStack(final ResourceLocation id, final int count)
    {
        return new ItemStack(ForgeRegistries.ITEMS.getValue(id), count);
    }
}
