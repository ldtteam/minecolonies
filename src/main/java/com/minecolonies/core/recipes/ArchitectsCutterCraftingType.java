package com.minecolonies.core.recipes;

import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlockComponent;
import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import com.ldtteam.domumornamentum.component.ModDataComponents;
import com.ldtteam.domumornamentum.recipe.ModRecipeTypes;
import com.ldtteam.domumornamentum.recipe.architectscutter.ArchitectsCutterRecipe;
import com.ldtteam.domumornamentum.recipe.architectscutter.ArchitectsCutterRecipeInput;
import com.minecolonies.api.crafting.GenericRecipe;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.crafting.ModCraftingTypes;
import com.minecolonies.api.crafting.RecipeCraftingType;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ArchitectsCutterCraftingType extends RecipeCraftingType<ArchitectsCutterRecipeInput, ArchitectsCutterRecipe>
{
    public ArchitectsCutterCraftingType()
    {
        super(ModCraftingTypes.ARCHITECTS_CUTTER_ID, ModRecipeTypes.ARCHITECTS_CUTTER.get(), null);
    }

    @Override
    public @NotNull List<IGenericRecipe> findRecipes(@NotNull RecipeManager recipeManager, @Nullable Level world)
    {
        final Random rnd = new Random();
        final List<IGenericRecipe> recipes = new ArrayList<>();
        for (final RecipeHolder<ArchitectsCutterRecipe> holder : recipeManager.getAllRecipesFor(ModRecipeTypes.ARCHITECTS_CUTTER.get()))
        {
            final ArchitectsCutterRecipe recipe = holder.value();
            // cutter recipes don't implement getIngredients(), so we have to work around it
            final Block generatedBlock = BuiltInRegistries.BLOCK.get(recipe.getBlockName());

            if (!(generatedBlock instanceof final IMateriallyTexturedBlock materiallyTexturedBlock))
                continue;

            final List<List<ItemStack>> inputs = new ArrayList<>();
            for (final IMateriallyTexturedBlockComponent component : materiallyTexturedBlock.getComponents())
            {
                final Named<Block> tag = BuiltInRegistries.BLOCK.getTag(component.getValidSkins()).orElse(null);
                if (tag != null)
                {
                    final List<Block> blocks = tag.stream().map(Holder::value).collect(Collectors.toList());
                    Collections.shuffle(blocks, rnd);
                    inputs.add(blocks.stream().map(ItemStack::new).collect(Collectors.toList()));
                }
            }

            final ItemStack output = recipe.getResultItem(world.registryAccess()).copy();
            output.setCount(Math.max(recipe.getCount(), inputs.size()));
            MaterialTextureData.EMPTY.writeToItemStack(output);

            recipes.add(new GenericRecipe(holder.id(), output, new ArrayList<>(),
                    inputs, 3, Blocks.AIR, null, ModEquipmentTypes.none.get(), new ArrayList<>(), -1));
        }

        return recipes;
    }
}
