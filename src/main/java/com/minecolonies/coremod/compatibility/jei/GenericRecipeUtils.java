package com.minecolonies.coremod.compatibility.jei;

import com.minecolonies.api.crafting.GenericRecipe;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.research.IGlobalResearch;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.crafting.CustomRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static com.ldtteam.structurize.items.ModItems.buildTool;
import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;

/**
 * Provides some utility methods to help build more complex {@link GenericRecipe}s.
 */
public final class GenericRecipeUtils
{
    private GenericRecipeUtils() { }

    @NotNull
    public static List<ITextComponent> calculateRestrictions(@NotNull final CustomRecipe customRecipe)
    {
        final List<ITextComponent> restrictions = new ArrayList<>();
        if (customRecipe.getMinBuildingLevel() == customRecipe.getMaxBuildingLevel())
        {
            restrictions.add(new TranslationTextComponent(TranslationConstants.COM_MINECOLONIES_JEI_PREFIX + "onelevelrestriction",
                    customRecipe.getMinBuildingLevel()));
        }
        else if (customRecipe.getMinBuildingLevel() > 1 || customRecipe.getMaxBuildingLevel() < CONST_DEFAULT_MAX_BUILDING_LEVEL)
        {
            restrictions.add(new TranslationTextComponent(TranslationConstants.COM_MINECOLONIES_JEI_PREFIX + "levelrestriction",
                    customRecipe.getMinBuildingLevel(), customRecipe.getMaxBuildingLevel()));
        }
        if (customRecipe.getRequiredResearchId() != null)
        {
            final ITextComponent researchName = getResearchDisplayName(customRecipe.getRequiredResearchId());
            restrictions.add(new TranslationTextComponent(TranslationConstants.COM_MINECOLONIES_JEI_PREFIX + "minresearch",
                    researchName));
        }
        if (customRecipe.getExcludedResearchId() != null)
        {
            final ITextComponent researchName = getResearchDisplayName(customRecipe.getExcludedResearchId());
            restrictions.add(new TranslationTextComponent(TranslationConstants.COM_MINECOLONIES_JEI_PREFIX + "maxresearch",
                    researchName));
        }
        return restrictions;
    }

    @NotNull
    public static IGenericRecipe create(@NotNull final CustomRecipe customRecipe, @NotNull final IRecipeStorage storage)
    {
        final List<ITextComponent> restrictions = calculateRestrictions(customRecipe);
        return Objects.requireNonNull(GenericRecipe.of(storage, restrictions, customRecipe.getMinBuildingLevel()));
    }

    @NotNull
    public static IGenericRecipe create(@NotNull final IRecipe<?> recipe)
    {
        final IGenericRecipe original = Objects.requireNonNull(GenericRecipe.of(recipe));
        final List<List<ItemStack>> inputs = compact(recipe.getIngredients());
        return new GenericRecipe(original.getPrimaryOutput(), original.getAdditionalOutputs(), inputs,
                original.getGridSize(), original.getIntermediate(), original.getLootTable(), new ArrayList<>(), -1);
    }

    @NotNull
    private static ITextComponent getResearchDisplayName(@NotNull final ResourceLocation researchId)
    {
        final IGlobalResearchTree researchTree = IGlobalResearchTree.getInstance();

        // first, try to see if this is a research id
        final IGlobalResearch research = researchTree.getResearch(researchId);
        if (research != null)
        {
            return research.getName();
        }

        // next, see if it's an effect id
        final Set<IGlobalResearch> researches = researchTree.getResearchForEffect(researchId);
        if (researches != null && !researches.isEmpty())
        {
            // there might be more than one, but this should be sufficient for now
            return researches.iterator().next().getName();
        }

        // otherwise it may be an effect with no research (perhaps disabled via datapack)
        return new StringTextComponent("???");
    }

    private static List<List<ItemStack>> compact(final NonNullList<Ingredient> inputs)
    {
        // FYI, this largely does the same job as RecipeStorage.calculateCleanedInput(), but we can't re-use
        // that implementation as we need to operate on Ingredients, which can be a list of stacks.
        final Map<IngredientStacks, IngredientStacks> ingredients = new HashMap<>();

        for (final Ingredient ingredient : inputs)
        {
            if (ingredient == Ingredient.EMPTY) continue;

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

        public IngredientStacks(final Ingredient ingredient)
        {
            this.stacks = Collections.unmodifiableList(Arrays.stream(ingredient.getItems())
                    .filter(stack -> !stack.isEmpty())
                    .map(ItemStack::copy)
                    .collect(Collectors.toList()));

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
