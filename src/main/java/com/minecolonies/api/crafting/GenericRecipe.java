package com.minecolonies.api.crafting;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.OptionalPredicate;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ldtteam.structurize.items.ModItems.buildTool;

/** Standard implementation of IGenericRecipe.*/
public class GenericRecipe implements IGenericRecipe
{
    /** Generic recipe builder */
    public static class Builder
    {
        @Nullable private ResourceLocation id;
        private List<ItemStack> mainOutputs = List.of();
        private List<ItemStack> additionalOutputs = List.of();
        private List<List<ItemStack>> inputs = List.of();
        private int gridSize = 1;
        private Block intermediate = Blocks.AIR;
        private ResourceLocation   lootTable = null;
        private EquipmentTypeEntry requiredTool = ModEquipmentTypes.none.get();
        private EntityType<?>      requiredEntity = null;
        private Supplier<List<Component>> restrictions = List::of;
        private int levelSort = -1;

        /** Default constructor */
        public Builder()
        {
        }

        /** Construct from an existing recipe */
        public Builder(@NotNull final IGenericRecipe recipe)
        {
            this.id = recipe.getRecipeId();
            this.mainOutputs = recipe.getAllMultiOutputs();
            this.additionalOutputs = recipe.getAdditionalOutputs();
            this.inputs = recipe.getInputs();
            this.gridSize = recipe.getGridSize();
            this.intermediate = recipe.getIntermediate();
            this.lootTable = recipe.getLootTable();
            this.requiredTool = recipe.getRequiredTool();
            this.requiredEntity = recipe.getRequiredEntity();
            this.restrictions = recipe.getRestrictions();
            this.levelSort = recipe.getLevelSort();
        }

        /**
         * Set (or clear) the original source recipe id.
         * @param id the recipe id.
         * @return this
         */
        public Builder withRecipeId(@Nullable final ResourceLocation id)
        {
            this.id = id == null || id.getPath().isEmpty() ? null : id;
            return this;
        }

        /**
         * Set the main output result of this recipe.
         * @param output the recipe output.
         * @return this
         */
        public Builder withOutput(@NotNull final ItemStack output)
        {
            this.mainOutputs = List.of(output);
            return this;
        }

        /**
         * Set the main output result of this recipe.
         * @param output the recipe output.
         * @return this
         */
        public Builder withOutput(@NotNull final ItemLike output)
        {
            return withOutput(new ItemStack(output));
        }

        /**
         * Set the main output result of this recipe.
         * @param output the recipe output.
         * @param count the count.
         * @return this
         */
        public Builder withOutput(@NotNull final ItemLike output, final int count)
        {
            return withOutput(new ItemStack(output, count));
        }

        /**
         * Set all possible main outputs (one of these will be generated).
         * @param multiOutputs the possible recipe outputs.
         * @return this
         */
        public Builder withOutputs(@NotNull final List<ItemStack> multiOutputs)
        {
            this.mainOutputs = Collections.unmodifiableList(multiOutputs);
            return this;
        }

        /**
         * Set all possible main outputs (one of these will be generated).
         * @param firstOutput one of the possible recipe outputs.
         * @param otherOutputs the other possible recipe outputs.
         * @return this
         */
        public Builder withOutputs(@NotNull final ItemStack firstOutput, @NotNull final List<ItemStack> otherOutputs)
        {
            return withOutputs(Stream.concat(Stream.of(firstOutput),
                    otherOutputs.stream()).filter(ItemStackUtils::isNotEmpty).toList());
        }

        /**
         * Set outputs generated in addition to the main outputs (e.g. containers, byproducts).
         * @param additionalOutputs the additional recipe outputs.
         * @return this
         */
        public Builder withAdditionalOutputs(@NotNull final List<ItemStack> additionalOutputs)
        {
            this.additionalOutputs = Collections.unmodifiableList(additionalOutputs);
            return this;
        }

        /**
         * Set recipe inputs.
         * @param inputs the recipe inputs, as a list of slots each containing a list of acceptable variants.
         * @return this
         */
        public Builder withInputs(@NotNull final List<List<ItemStack>> inputs)
        {
            this.inputs = Collections.unmodifiableList(inputs);
            return this;
        }

        /**
         * Set input grid size (e.g. 3 for 3x3 grid).
         * @param gridSize the grid size.
         * @return this
         */
        public Builder withGridSize(final int gridSize)
        {
            this.gridSize = gridSize;
            return this;
        }

        /**
         * Set required intermediate crafting block.
         * @param intermediate the intermediate block.
         * @return this
         */
        public Builder withIntermediate(@NotNull final Block intermediate)
        {
            this.intermediate = intermediate;
            return this;
        }

        /**
         * Set the loot table produced by this recipe.
         * @param lootTable the loot table id.
         * @return this
         */
        public Builder withLootTable(@Nullable ResourceLocation lootTable)
        {
            this.lootTable = lootTable;
            return this;
        }

        /**
         * Set the tool required to craft this recipe.
         * @param requiredTool the tool entry.
         * @return this
         */
        public Builder withRequiredTool(@NotNull EquipmentTypeEntry requiredTool)
        {
            this.requiredTool = requiredTool;
            return this;
        }

        /**
         * Set the entity required to craft this recipe.
         * @param requiredEntity the entity type.
         * @return this
         */
        public Builder withRequiredEntity(@Nullable EntityType<?> requiredEntity)
        {
            this.requiredEntity = requiredEntity;
            return this;
        }

        /**
         * Set the restrictions on crafting this recipe.
         * @param restrictions the restrictions.
         * @return this
         * @apiNote Each restriction is expected to be a translation key for the main JEI display, with
         *          an optional extra key with .tip suffix for a tooltip.  Both take the same parameters.
         */
        public Builder withRestrictions(@NotNull List<Component> restrictions)
        {
            final List<Component> fixedRestrictions = Collections.unmodifiableList(restrictions);
            return withRestrictions(() -> fixedRestrictions);
        }

        /**
         * Set the restrictions on crafting this recipe.
         * @param restrictions the restrictions.
         * @return this
         * @apiNote Each restriction is expected to be a translation key for the main JEI display, with
         *          an optional extra key with .tip suffix for a tooltip.  Both take the same parameters.
         */
        public Builder withRestrictions(@NotNull Supplier<List<Component>> restrictions)
        {
            this.restrictions = restrictions;
            return this;
        }

        /**
         * Sets a value that helps sort this recipe relative to others.
         * @param levelSort the sorting value.
         * @return this
         */
        public Builder withLevelSort(final int levelSort)
        {
            this.levelSort = levelSort;
            return this;
        }

        /**
         * Builds a recipe from the current builder state.
         * @return the recipe.
         */
        @NotNull
        public IGenericRecipe build()
        {
            return new GenericRecipe(this);
        }
    }

    /**
     * Start building a generic recipe.
     * @return a builder.
     */
    @NotNull
    public static Builder builder()
    {
        return new Builder();
    }

    /**
     * Start building a generic recipe.
     * @param recipe initialise from this recipe.
     * @return a builder.
     */
    @NotNull
    public static Builder builder(@NotNull final IGenericRecipe recipe)
    {
        return new Builder(recipe);
    }

    /**
     * Start building a generic recipe.
     * @param storage initialise from this recipe.
     * @return a builder.
     */
    @NotNull
    public static Builder builder(@NotNull final IRecipeStorage storage)
    {
        final List<List<ItemStack>> inputs = storage.getCleanedInput().stream()
                .map(input -> Collections.singletonList(toItemStack(input)))
                .toList();

        return builder()
                .withRecipeId(storage.getRecipeSource())
                .withOutputs(storage.getPrimaryOutput(), storage.getAlternateOutputs())
                .withAdditionalOutputs(storage.getCraftingToolsAndSecondaryOutputs())
                .withInputs(inputs)
                .withGridSize(storage.getGridSize())
                .withIntermediate(storage.getIntermediate())
                .withLootTable(storage.getLootTable())
                .withRequiredTool(storage.getRequiredTool());
    }

    /**
     * Construct from builder.
     * @param builder the builder.
     */
    private GenericRecipe(@NotNull final Builder builder)
    {
        this.id = builder.id;
        this.mainOutputs = builder.mainOutputs;
        this.additionalOutputs = builder.additionalOutputs;
        this.inputs = builder.inputs;
        this.gridSize = builder.gridSize;
        this.intermediate = builder.intermediate;
        this.lootTable = builder.lootTable;
        this.requiredTool = builder.requiredTool;
        this.requiredEntity = builder.requiredEntity;
        this.restrictions = builder.restrictions;
        this.levelSort = builder.levelSort;
    }

    /**
     * Construct from vanilla recipe.
     * @param recipe the vanilla recipe.
     * @param world the world.
     * @return the recipe, or null.
     */
    @Nullable
    public static IGenericRecipe of(@Nullable final Recipe<?> recipe, @NotNull final Level world)
    {
        if (recipe == null) return null;

        final List<List<ItemStack>> inputs = compactInputs(recipe.getIngredients().stream()
                .map(ingredient -> Arrays.asList(ingredient.getItems()))
                .toList());

        final Builder builder = builder()
                .withRecipeId(recipe.getId())
                .withOutput(recipe.getResultItem(world.registryAccess()))
                .withAdditionalOutputs(calculateSecondaryOutputs(recipe, world))
                .withInputs(inputs);

        if (recipe instanceof SmeltingRecipe)
        {
            builder.withGridSize(1).withIntermediate(Blocks.FURNACE);
        }
        else
        {
            builder.withGridSize(recipe.canCraftInDimensions(2, 2) ? 2 : 3);
        }

        return builder.build();
    }

    @Nullable
    public static IGenericRecipe of(@Nullable final IToken<?> recipeToken)
    {
        if (recipeToken == null) return null;
        final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(recipeToken);
        return storage == null ? null : builder(storage).build();
    }

    @Nullable private final ResourceLocation id;
    private final List<ItemStack> mainOutputs;
    private final List<ItemStack> additionalOutputs;
    private final List<List<ItemStack>> inputs;
    private final int gridSize;
    private final Block intermediate;
    @Nullable private final ResourceLocation   lootTable;
    private final EquipmentTypeEntry requiredTool;
    @Nullable private final EntityType<?>      requiredEntity;
    private final Supplier<List<Component>> restrictions;
    private final int levelSort;

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
        return this.mainOutputs.isEmpty() ? ItemStack.EMPTY : this.mainOutputs.get(0);
    }

    @NotNull
    public List<ItemStack> getAllMultiOutputs()
    {
        return this.mainOutputs;
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
    public Supplier<List<Component>> getRestrictions()
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
        return predicate.test(getPrimaryOutput());
    }

    @Override
    public Optional<Boolean> matchesInput(@NotNull OptionalPredicate<ItemStack> predicate)
    {
        Optional<Boolean> result = Optional.empty();

        for (final List<ItemStack> slot : this.inputs)
        {
            for (final ItemStack stack : slot)
            {
                final Optional<Boolean> itemResult = predicate.test(stack);
                if (itemResult.isPresent())
                {
                    if (!itemResult.get())
                    {
                        // immediately fail on any predicate failure
                        return itemResult;
                    }
                    // otherwise remember a pass but keep checking
                    result = itemResult;
                }
            }
        }
        return result;
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

    @NotNull
    @Override
    public EquipmentTypeEntry getRequiredTool()
    {
        return this.requiredTool;
    }

    @Nullable
    @Override
    public EntityType<?> getRequiredEntity()
    {
        return this.requiredEntity;
    }

    @Override
    public String toString()
    {
        return "GenericRecipe{output=" + getPrimaryOutput() +'}';
    }

    @NotNull
    private static ItemStack toItemStack(@NotNull final ItemStorage input)
    {
        final ItemStack result = input.getItemStack().copy();
        result.setCount(input.getAmount());
        return result;
    }

    @NotNull
    private static List<ItemStack> calculateSecondaryOutputs(@NotNull final Recipe<?> recipe,
                                                             @Nullable final Level world)
    {
        if (recipe instanceof final CraftingRecipe craftingRecipe)
        {
            final List<Ingredient> inputs = recipe.getIngredients();
            final CraftingContainer inv = new TransientCraftingContainer(new AbstractContainerMenu(MenuType.CRAFTING, 0)
            {
                @Override
                public @NotNull ItemStack quickMoveStack(final @NotNull Player player, final int slot)
                {
                    return ItemStack.EMPTY;
                }

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
                    inv.setItem(slot, stacks[0].copy());
                }
            }
            if (craftingRecipe.matches(inv, world))
            {
                return craftingRecipe.getRemainingItems(inv).stream()
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
                .collect(Collectors.toList());
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
