package com.minecolonies.api.crafting;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ldtteam.structurize.items.ModItems.buildTool;

/** Standard implementation of IGenericRecipe.*/
public class GenericRecipe implements IGenericRecipe
{
    @Nullable
    public static IGenericRecipe of(@Nullable final Recipe<?> recipe)
    {
        if (recipe == null) return null;
        final List<List<ItemStack>> inputs = recipe.getIngredients().stream()
                .map(ingredient -> Arrays.asList(ingredient.getItems()))
                .collect(Collectors.toList());
        final int size;
        final Block intermediate;
        if (recipe instanceof SmeltingRecipe)
        {
            size = 1;
            intermediate = Blocks.FURNACE;
        }
        else
        {
            size = recipe.canCraftInDimensions(2, 2) ? 2 : 3;
            intermediate = Blocks.AIR;
        }
        return new GenericRecipe(recipe.getResultItem(), calculateSecondaryOutputs(recipe), inputs,
                size, intermediate, null, new ArrayList<>(), -1);
    }

    @Nullable
    public static IGenericRecipe of(@Nullable final IRecipeStorage storage, @NotNull final List<Component> restrictions, final int levelSort)
    {
        if (storage == null) return null;
        final List<List<ItemStack>> inputs = storage.getCleanedInput().stream()
                .map(input -> Collections.singletonList(toItemStack(input)))
                .collect(Collectors.toList());
        return new GenericRecipe(storage.getPrimaryOutput(), storage.getAlternateOutputs(),
                storage.getSecondaryOutputs(), inputs, storage.getGridSize(),
                storage.getIntermediate(), storage.getLootTable(), restrictions, levelSort);
    }

    @Nullable
    public static IGenericRecipe of(@Nullable final IRecipeStorage storage)
    {
        return of(storage, new ArrayList<>(), -1);
    }

    @Nullable
    public static IGenericRecipe of(@Nullable final IToken<?> recipeToken)
    {
        if (recipeToken == null) return null;
        return of(IColonyManager.getInstance().getRecipeManager().getRecipes().get(recipeToken));
    }

    private final ItemStack output;
    private final List<ItemStack> allMultiOutputs;
    private final List<ItemStack> additionalOutputs;
    private final List<List<ItemStack>> inputs;
    private final int gridSize;
    private final Block intermediate;
    private final ResourceLocation lootTable;
    private final List<Component> restrictions;
    private final int levelSort;

    public GenericRecipe(@NotNull final ItemStack output,
                         @NotNull final List<ItemStack> additionalOutputs,
                         @NotNull final List<List<ItemStack>> inputs,
                         final int gridSize, @NotNull final Block intermediate,
                         @Nullable final ResourceLocation lootTable,
                         @NotNull final List<Component> restrictions,
                         final int levelSort)
    {
        this.output = output;
        this.allMultiOutputs = Collections.singletonList(output);
        this.additionalOutputs = Collections.unmodifiableList(additionalOutputs);
        this.inputs = Collections.unmodifiableList(inputs);
        this.gridSize = gridSize;
        this.intermediate = intermediate;
        this.lootTable = lootTable;
        this.restrictions = Collections.unmodifiableList(restrictions);
        this.levelSort = levelSort;
    }

    public GenericRecipe(@NotNull final ItemStack output,
                         @NotNull final List<ItemStack> altOutputs,
                         @NotNull final List<ItemStack> additionalOutputs,
                         @NotNull final List<List<ItemStack>> inputs,
                         final int gridSize, @NotNull final Block intermediate,
                         @Nullable final ResourceLocation lootTable,
                         @NotNull final List<Component> restrictions,
                         final int levelSort)
    {
        this.output = output;
        this.allMultiOutputs = Collections.unmodifiableList(
                Stream.concat(Stream.of(output),
                    altOutputs.stream()).collect(Collectors.toList()));
        this.additionalOutputs = Collections.unmodifiableList(additionalOutputs);
        this.inputs = Collections.unmodifiableList(inputs);
        this.gridSize = gridSize;
        this.intermediate = intermediate;
        this.lootTable = lootTable;
        this.restrictions = Collections.unmodifiableList(restrictions);
        this.levelSort = levelSort;
    }

    @Override
    public int getGridSize() { return this.gridSize; }

    @Override
    @NotNull
    public ItemStack getPrimaryOutput()
    {
        return this.output;
    }

    @NotNull
    public List<ItemStack> getAllMultiOutputs()
    {
        return this.allMultiOutputs;
    }

    @NotNull
    @Override
    public List<ItemStack> getAdditionalOutputs()
    {
        return this.additionalOutputs;
    }

    @Override
    @NotNull
    public List<List<ItemStack>> getInputs()
    {
        return this.inputs;
    }

    @NotNull
    @Override
    public List<Component> getRestrictions()
    {
        return this.restrictions;
    }

    @Override
    public int getLevelSort()
    {
        return this.levelSort;
    }

    @Override
    public boolean matchesOutput(@NotNull final Predicate<ItemStack> predicate)
    {
        return predicate.test(this.output);
    }

    @Override
    public boolean matchesInput(@NotNull final Predicate<ItemStack> predicate)
    {
        for (final List<ItemStack> slot : this.inputs)
        {
            for (final ItemStack stack : slot)
            {
                if (predicate.test(stack))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @NotNull
    @Override
    public Block getIntermediate()
    {
        return this.intermediate;
    }

    @Nullable
    @Override
    public ResourceLocation getLootTable() { return this.lootTable; }

    @Override
    public String toString()
    {
        return "GenericRecipe{output=" + output +'}';
    }

    @NotNull
    private static ItemStack toItemStack(@NotNull final ItemStorage input)
    {
        final ItemStack result = input.getItemStack().copy();
        result.setCount(input.getAmount());
        return result;
    }

    @NotNull
    private static List<ItemStack> calculateSecondaryOutputs(@NotNull final Recipe<?> recipe)
    {
        if (recipe instanceof CraftingRecipe)
        {
            final List<Ingredient> inputs = recipe.getIngredients();
            final CraftingContainer inv = new CraftingContainer(new AbstractContainerMenu(MenuType.CRAFTING, 0)
            {
                @Override
                public boolean stillValid(@NotNull final Player playerIn)
                {
                    return false;
                }
            }, 3, 3);
            for (int slot = 0; slot < inputs.size(); ++slot)
            {
                final ItemStack[] stacks = inputs.get(slot).getItems();
                if (stacks.length > 0)
                {
                    inv.setItem(slot, stacks[0]);
                }
            }
            if (((CraftingRecipe) recipe).matches(inv, null))
            {
                return ((CraftingRecipe) recipe).getRemainingItems(inv).stream()
                        .filter(ItemStackUtils::isNotEmpty)
                        .filter(stack -> stack.getItem() != buildTool.get())  // this is filtered out of the inputs too
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }
}
