package com.minecolonies.api.crafting;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.OptionalPredicate;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ldtteam.structurize.items.ModItems.buildTool;

/** Standard implementation of IGenericRecipe.*/
public class GenericRecipe implements IGenericRecipe
{
    @Nullable
    public static IGenericRecipe of(@Nullable final IRecipe<?> recipe, @Nullable final World world)
    {
        if (recipe == null) return null;
        final List<List<ItemStack>> inputs = compactInputs(recipe.getIngredients().stream()
                .map(ingredient -> Arrays.asList(ingredient.getItems()))
                .collect(Collectors.toList()));
        final int size;
        final Block intermediate;
        if (recipe instanceof FurnaceRecipe)
        {
            size = 1;
            intermediate = Blocks.FURNACE;
        }
        else
        {
            size = recipe.canCraftInDimensions(2, 2) ? 2 : 3;
            intermediate = Blocks.AIR;
        }
        return new GenericRecipe(recipe.getId(), recipe.getResultItem(), calculateSecondaryOutputs(recipe, world), inputs,
                size, intermediate, null, new ArrayList<>(), -1);
    }

    @Nullable
    public static IGenericRecipe of(@Nullable final IRecipeStorage storage, @NotNull final List<ITextComponent> restrictions, final int levelSort)
    {
        if (storage == null) return null;
        final List<List<ItemStack>> inputs = storage.getCleanedInput().stream()
                .map(input -> Collections.singletonList(toItemStack(input)))
                .collect(Collectors.toList());
        return new GenericRecipe(storage.getRecipeSource(), storage.getPrimaryOutput(), storage.getAlternateOutputs(),
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

    @Nullable private final ResourceLocation id;
    private final ItemStack output;
    private final List<ItemStack> allMultiOutputs;
    private final List<ItemStack> additionalOutputs;
    private final List<List<ItemStack>> inputs;
    private final int gridSize;
    private final Block intermediate;
    private final ResourceLocation lootTable;
    private final List<ITextComponent> restrictions;
    private final int levelSort;

    public GenericRecipe(@Nullable final ResourceLocation id,
                         @NotNull final ItemStack output,
                         @NotNull final List<ItemStack> additionalOutputs,
                         @NotNull final List<List<ItemStack>> inputs,
                         final int gridSize, @NotNull final Block intermediate,
                         @Nullable final ResourceLocation lootTable,
                         @NotNull final List<ITextComponent> restrictions,
                         final int levelSort)
    {
        this.id = id == null || id.getPath().isEmpty() ? null : id;
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

    public GenericRecipe(@Nullable final ResourceLocation id,
                         @NotNull final ItemStack output,
                         @NotNull final List<ItemStack> altOutputs,
                         @NotNull final List<ItemStack> additionalOutputs,
                         @NotNull final List<List<ItemStack>> inputs,
                         final int gridSize, @NotNull final Block intermediate,
                         @Nullable final ResourceLocation lootTable,
                         @NotNull final List<ITextComponent> restrictions,
                         final int levelSort)
    {
        this.id = id;
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
    @Nullable
    public ResourceLocation getRecipeId()
    {
        return this.id;
    }

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
    public List<ITextComponent> getRestrictions()
    {
        return this.restrictions;
    }

    @Override
    public int getLevelSort()
    {
        return this.levelSort;
    }

    @Override
    public Optional<Boolean> matchesOutput(@NotNull OptionalPredicate<ItemStack> predicate)
    {
        return predicate.test(this.output);
    }

    @Override
    public Optional<Boolean> matchesInput(@NotNull OptionalPredicate<ItemStack> predicate)
    {
        for (final List<ItemStack> slot : this.inputs)
        {
            for (final ItemStack stack : slot)
            {
                final Optional<Boolean> result = predicate.test(stack);
                if (result.isPresent())
                {
                    return result;
                }
            }
        }
        return Optional.empty();
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
    private static List<ItemStack> calculateSecondaryOutputs(@NotNull final IRecipe<?> recipe,
                                                             @Nullable final World world)
    {
        if (recipe instanceof ICraftingRecipe)
        {
            final List<Ingredient> inputs = recipe.getIngredients();
            final CraftingInventory inv = new CraftingInventory(new Container(ContainerType.CRAFTING, 0)
            {
                @Override
                public boolean stillValid(@NotNull final PlayerEntity playerIn)
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
            if (((ICraftingRecipe) recipe).matches(inv, world))
            {
                return ((ICraftingRecipe) recipe).getRemainingItems(inv).stream()
                        .filter(ItemStackUtils::isNotEmpty)
                        .filter(stack -> stack.getItem() != buildTool.get())  // this is filtered out of the inputs too
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    private static List<List<ItemStack>> compactInputs(final List<List<ItemStack>> inputs)
    {
        // FYI, this largely does the same job as RecipeStorage.calculateCleanedInput(), but we can't re-use
        // that implementation as we need to operate on Ingredients, which can be a list of stacks.
        final Map<IngredientStacks, IngredientStacks> ingredients = new HashMap<>();

        for (final List<ItemStack> ingredient : inputs)
        {
            final IngredientStacks newIngredient = new IngredientStacks(ingredient);
            // also ignore the build tool as an ingredient, since colony crafters don't require it.
            //   (see RecipeStorage.calculateCleanedInput() for why)
            if (!newIngredient.getStacks().isEmpty() && newIngredient.getStacks().get(0).getItem() == buildTool.get()) continue;

            final IngredientStacks existing = ingredients.get(newIngredient);
            if (existing == null)
            {
                ingredients.put(newIngredient, newIngredient);
            }
            else
            {
                existing.merge(newIngredient);
            }
        }

        return ingredients.values().stream()
                .sorted(Comparator.reverseOrder())
                .map(IngredientStacks::getStacks)
                .collect(Collectors.toCollection(NonNullList::create));
    }

    private static class IngredientStacks implements Comparable<IngredientStacks>
    {
        private final List<ItemStack> stacks;
        private final Set<Item> items;

        public IngredientStacks(final List<ItemStack> ingredient)
        {
            this.stacks = ingredient.stream()
                    .filter(stack -> !stack.isEmpty())
                    .map(ItemStack::copy)
                    .collect(Collectors.toList());

            this.items = this.stacks.stream()
                    .map(ItemStack::getItem)
                    .collect(Collectors.toSet());
        }

        @NotNull
        public List<ItemStack> getStacks() { return this.stacks; }

        public int getCount() { return this.stacks.isEmpty() ? 0 : this.stacks.get(0).getCount(); }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final IngredientStacks that = (IngredientStacks) o;
            return this.items.equals(that.items);
            // note that this does not compare the counts to maintain key-stability
        }

        @Override
        public int hashCode()
        {
            return this.items.hashCode();
        }

        @Override
        public int compareTo(@NotNull IngredientStacks o)
        {
            int diff = this.getCount() - o.getCount();
            if (diff != 0) return diff;

            diff = this.stacks.size() - o.stacks.size();
            if (diff != 0) return diff;

            return this.hashCode() - o.hashCode();
        }

        public void merge(@NotNull final IngredientStacks other)
        {
            // assumes equals(other)
            for (int i = 0; i < this.stacks.size(); i++)
            {
                this.stacks.get(i).grow(other.stacks.get(i).getCount());
            }
        }

        @Override
        public String toString()
        {
            return "IngredientStacks{" +
                    "stacks=" + stacks +
                    ", items=" + items +
                    '}';
        }
    }
}
