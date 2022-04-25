package com.minecolonies.coremod.colony.crafting;

import com.minecolonies.api.crafting.GenericRecipe;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.research.IGlobalResearch;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.util.OptionalPredicate;
import com.minecolonies.api.util.constant.TranslationConstants;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;

/**
 * Provides some utility methods to help build more complex {@link GenericRecipe}s.
 */
public final class GenericRecipeUtils
{
    private GenericRecipeUtils() { }

    @NotNull
    public static List<Component> calculateRestrictions(@NotNull final CustomRecipe customRecipe)
    {
        final List<Component> restrictions = new ArrayList<>();
        if (customRecipe.getMinBuildingLevel() == customRecipe.getMaxBuildingLevel())
        {
            restrictions.add(new TranslatableComponent(TranslationConstants.COM_MINECOLONIES_JEI_PREFIX + "onelevelrestriction",
                    customRecipe.getMinBuildingLevel()));
        }
        else if (customRecipe.getMinBuildingLevel() > 1 || customRecipe.getMaxBuildingLevel() < CONST_DEFAULT_MAX_BUILDING_LEVEL)
        {
            restrictions.add(new TranslatableComponent(TranslationConstants.COM_MINECOLONIES_JEI_PREFIX + "levelrestriction",
                    customRecipe.getMinBuildingLevel(), customRecipe.getMaxBuildingLevel()));
        }
        if (customRecipe.getRequiredResearchId() != null)
        {
            final Component researchName = getResearchDisplayName(customRecipe.getRequiredResearchId());
            restrictions.add(new TranslatableComponent(TranslationConstants.COM_MINECOLONIES_JEI_PREFIX + "minresearch",
                    researchName));
        }
        if (customRecipe.getExcludedResearchId() != null)
        {
            final Component researchName = getResearchDisplayName(customRecipe.getExcludedResearchId());
            restrictions.add(new TranslatableComponent(TranslationConstants.COM_MINECOLONIES_JEI_PREFIX + "maxresearch",
                    researchName));
        }
        return restrictions;
    }

    @NotNull
    public static IGenericRecipe create(@NotNull final CustomRecipe customRecipe, @NotNull final IRecipeStorage storage)
    {
        final List<Component> restrictions = calculateRestrictions(customRecipe);
        return Objects.requireNonNull(GenericRecipe.of(storage, restrictions, customRecipe.getMinBuildingLevel()));
    }

    /**
     * Exclude input ingredients that fail the supplied predicate.
     *
     * @param recipe the original recipe
     * @param predicate an ingredient predicate that returns false to reject an ingredient
     * @return the original recipe, if there were no problems.
     *         or a modified recipe, if some ingredients were banned but alternates were acceptable.
     *         some slots might still contain banned ingredients if there were no valid alternatives.
     */
    @NotNull
    public static IGenericRecipe filterInputs(@NotNull IGenericRecipe recipe,
                                              @NotNull OptionalPredicate<ItemStack> predicate)
    {
        final List<List<ItemStack>> newInputs = new ArrayList<>();
        boolean modified = false;

        for (final List<ItemStack> slot : recipe.getInputs())
        {
            final List<ItemStack> newSlot = slot.stream()
                    .filter(stack -> predicate.test(stack).orElse(true))
                    .collect(Collectors.toList());

            if (newSlot.isEmpty() && !slot.isEmpty())
            {
                // don't reduce the slot to nothing; it's possible despite all excluded ingredients the overall
                // recipe will still be allowed in the end due to another rule
                newInputs.add(slot);
                continue;
            }
            modified |= newSlot.size() != slot.size();

            newInputs.add(newSlot);
        }

        if (!modified)
        {
            return recipe;
        }

        return new GenericRecipe(recipe.getRecipeId(),
                recipe.getPrimaryOutput(),
                recipe.getAdditionalOutputs(),
                newInputs,
                recipe.getGridSize(),
                recipe.getIntermediate(),
                recipe.getLootTable(),
                recipe.getRestrictions(),
                recipe.getLevelSort());
    }

    @NotNull
    private static Component getResearchDisplayName(@NotNull final ResourceLocation researchId)
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
        return new TextComponent("???");
    }
}
