package com.minecolonies.coremod.generation.defaults;

import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.coremod.generation.CustomRecipeProvider;
import net.minecraft.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.function.Consumer;

import com.minecolonies.coremod.generation.CustomRecipeProvider.CustomRecipeBuilder;

/** Datagen for glassblower crafterrecipes */
public class DefaultGlassblowerCraftingProvider extends CustomRecipeProvider
{
    public DefaultGlassblowerCraftingProvider(@NotNull final  DataGenerator generatorIn)
    {
        super(generatorIn);
    }

    @Override
    protected void registerRecipes(@NotNull final Consumer<IFinishedRecipe> consumer)
    {
        register(consumer, Items.SAND, Items.GLASS);
        register(consumer, Items.RED_SAND, Items.GLASS);
    }

    private void register(@NotNull final Consumer<IFinishedRecipe> consumer,
                          @NotNull final Item input,
                          @NotNull final Item output)
    {
        CustomRecipeBuilder.create(ModJobs.GLASSBLOWER_ID.getPath(), input.getRegistryName().getPath())
                .inputs(Collections.singletonList(new ItemStorage(new ItemStack(input))))
                .result(new ItemStack(output))
                .intermediate(Blocks.FURNACE)
                .build(consumer);
    }
}
