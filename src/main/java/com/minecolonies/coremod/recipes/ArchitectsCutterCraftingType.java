package com.minecolonies.coremod.recipes;

import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlockComponent;
import com.ldtteam.domumornamentum.recipe.ModRecipeTypes;
import com.ldtteam.domumornamentum.recipe.architectscutter.ArchitectsCutterRecipe;
import com.minecolonies.api.crafting.GenericRecipe;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.crafting.ModCraftingTypes;
import com.minecolonies.api.crafting.RecipeCraftingType;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ArchitectsCutterCraftingType extends RecipeCraftingType<Container, ArchitectsCutterRecipe>
{
    public ArchitectsCutterCraftingType()
    {
        super(ModCraftingTypes.ARCHITECTS_CUTTER_ID, ModRecipeTypes.ARCHITECTS_CUTTER, null);
    }

    @Override
    public @NotNull List<IGenericRecipe> findRecipes(@NotNull RecipeManager recipeManager, @Nullable Level world)
    {
        final Random rnd = world == null ? new Random() : world.getRandom();
        final List<IGenericRecipe> recipes = new ArrayList<>();
        for (final ArchitectsCutterRecipe recipe : recipeManager.getAllRecipesFor(ModRecipeTypes.ARCHITECTS_CUTTER))
        {
            // cutter recipes don't implement getIngredients(), so we have to work around it
            final Block generatedBlock = ForgeRegistries.BLOCKS.getValue(recipe.getBlockName());

            if (!(generatedBlock instanceof final IMateriallyTexturedBlock materiallyTexturedBlock))
                continue;

            final List<List<ItemStack>> inputs = new ArrayList<>();
            for (final IMateriallyTexturedBlockComponent component : materiallyTexturedBlock.getComponents())
            {

                final List<Block> blocks = StreamSupport.stream(Registry.BLOCK.getTagOrEmpty(component.getValidSkins()).spliterator(), false).map(Holder::value).toList();
                Collections.shuffle(blocks, rnd);
                inputs.add(blocks.stream().map(ItemStack::new).collect(Collectors.toList()));
            }

            final ItemStack output = recipe.getResultItem().copy();
            output.setCount(Math.max(recipe.getCount(), inputs.size()));

            // resultItem usually doesn't have textureData, but we need it to properly match the creative tab
            if (!output.getOrCreateTag().contains("textureData"))
            {
                assert output.getTag() != null;
                output.getTag().put("textureData", new CompoundTag());
            }

            recipes.add(new GenericRecipe(recipe.getId(), output, new ArrayList<>(),
                    inputs, 3, Blocks.AIR, null, new ArrayList<>(), -1));
        }

        return recipes;
    }
}
