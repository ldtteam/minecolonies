package com.minecolonies.core.generation.defaults.workers;

import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.core.generation.CustomRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.minecolonies.api.util.constant.BuildingConstants.MODULE_CUSTOM;

/** Datagen for concrete mixer crafterrecipes */
public class DefaultConcreteMixerCraftingProvider extends CustomRecipeProvider
{
    public DefaultConcreteMixerCraftingProvider(@NotNull final PackOutput packOutput, final CompletableFuture<HolderLookup.Provider> lookupProvider)
    {
        super(packOutput, lookupProvider);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "DefaultConcreteMixerCraftingProvider";
    }

    @Override
    protected void registerRecipes(@NotNull final Consumer<CustomRecipeBuilder> consumer)
    {
        final List<ItemStorage> input = new ArrayList<>();
        input.add(new ItemStorage(new ItemStack(Items.SAND, 4)));
        input.add(new ItemStorage(new ItemStack(Items.GRAVEL, 4)));

        for (final DyeColor color : DyeColor.values())
        {
            final String prefix = color.name().toLowerCase(Locale.US);
            final Item powder = BuiltInRegistries.ITEM.get(ResourceLocation.parse(prefix + "_concrete_powder"));
            final Item concrete = BuiltInRegistries.ITEM.get(ResourceLocation.parse(prefix + "_concrete"));
            final Item dye = BuiltInRegistries.ITEM.get(ResourceLocation.parse(prefix + "_dye"));

            if (powder == null || concrete == null || dye == null)
            {
                throw new IllegalStateException("Missing items for " + color.getSerializedName());
            }

            final List<ItemStorage> customInput = new ArrayList<>(input);
            customInput.add(new ItemStorage(new ItemStack(dye)));

            recipe(ModJobs.CONCRETE_ID.getPath(),  MODULE_CUSTOM, BuiltInRegistries.ITEM.getKey(powder).getPath())
                    .inputs(customInput)
                    .result(new ItemStack(powder, 8))
                    .build(consumer);

            recipe(ModJobs.CONCRETE_ID.getPath(), MODULE_CUSTOM, BuiltInRegistries.ITEM.getKey(concrete).getPath())
                    .inputs(Collections.singletonList(new ItemStorage(new ItemStack(powder))))
                    .result(new ItemStack(concrete))
                    //.intermediate(Blocks.WATER)
                    .build(consumer);
            // TODO: it makes sense for this to have WATER as an intermediate, but the RS logic
            //       and JEI rendering don't currently support that.  Previous versions just used
            //       air, so we'll do the same for now.
        }
    }
}
