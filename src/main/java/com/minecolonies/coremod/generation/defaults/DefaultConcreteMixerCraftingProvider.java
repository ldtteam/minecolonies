package com.minecolonies.coremod.generation.defaults;

import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.crafting.ItemStackHandling;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.coremod.generation.CustomRecipeProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import com.minecolonies.coremod.generation.CustomRecipeProvider.CustomRecipeBuilder;

/** Datagen for concrete mixer crafterrecipes */
public class DefaultConcreteMixerCraftingProvider extends CustomRecipeProvider
{
    public DefaultConcreteMixerCraftingProvider(@NotNull final DataGenerator generatorIn)
    {
        super(generatorIn);
    }

    @Override
    protected void registerRecipes(@NotNull final Consumer<IFinishedRecipe> consumer)
    {
        final List<ItemStorage> input = new ArrayList<>();
        input.add(new ItemStackHandling(new ItemStack(Items.SAND, 4)));
        input.add(new ItemStackHandling(new ItemStack(Items.GRAVEL, 4)));

        for (final DyeColor color : DyeColor.values())
        {
            final String prefix = color.name().toLowerCase(Locale.ROOT);
            final Item powder = ForgeRegistries.ITEMS.getValue(new ResourceLocation(prefix + "_concrete_powder"));
            final Item concrete = ForgeRegistries.ITEMS.getValue(new ResourceLocation(prefix + "_concrete"));
            final Item dye = ForgeRegistries.ITEMS.getValue(new ResourceLocation(prefix + "_dye"));

            if (powder == null || concrete == null || dye == null)
            {
                throw new IllegalStateException("Missing items for " + color.getSerializedName());
            }

            final List<ItemStorage> customInput = new ArrayList<>(input);
            customInput.add(new ItemStackHandling(new ItemStack(dye)));

            CustomRecipeBuilder.create(ModJobs.CONCRETE_ID.getPath() + "_custom", powder.getRegistryName().getPath())
                    .inputs(customInput)
                    .result(new ItemStack(powder, 8))
                    .build(consumer);

            CustomRecipeBuilder.create(ModJobs.CONCRETE_ID.getPath() + "_custom", concrete.getRegistryName().getPath())
                    .inputs(Collections.singletonList(new ItemStackHandling(new ItemStack(powder))))
                    .result(new ItemStack(concrete))
                    //.intermediate(Blocks.WATER)
                    .build(consumer);
            // TODO: it makes sense for this to have WATER as an intermediate, but the RS logic
            //       and JEI rendering don't currently support that.  Previous versions just used
            //       air, so we'll do the same for now.
        }
    }
}
