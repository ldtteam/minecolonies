package com.minecolonies.coremod.colony.crafting;

import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import com.minecolonies.api.crafting.GenericRecipe;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.research.IGlobalResearch;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.util.OptionalPredicate;
import com.minecolonies.api.util.constant.TranslationConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
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
            restrictions.add(Component.translatable(TranslationConstants.PARTIAL_JEI_INFO + "onelevelrestriction",
                    customRecipe.getMinBuildingLevel()));
        }
        else if (customRecipe.getMinBuildingLevel() > 1 || customRecipe.getMaxBuildingLevel() < CONST_DEFAULT_MAX_BUILDING_LEVEL)
        {
            restrictions.add(Component.translatable(TranslationConstants.PARTIAL_JEI_INFO + "levelrestriction",
                    customRecipe.getMinBuildingLevel(), customRecipe.getMaxBuildingLevel()));
        }
        for (final ResourceLocation researchId : customRecipe.getRequiredResearchIds())
        {
            final Component researchName = getResearchDisplayName(researchId);
            restrictions.add(Component.translatable(TranslationConstants.PARTIAL_JEI_INFO + "minresearch",
                    researchName));
        }
        for (final ResourceLocation researchId : customRecipe.getExcludedResearchIds())
        {
            final Component researchName = getResearchDisplayName(researchId);
            restrictions.add(Component.translatable(TranslationConstants.PARTIAL_JEI_INFO + "maxresearch",
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
        // for filtering purposes, most recipes want to treat null/don't-care in the ingredient filter as
        // acceptable (don't remove the item from the recipe).  DO recipes, though, have a massive
        // alternate-ingredient stack for the same recipe and we do want to only show in JEI the inputs
        // that explicitly pass the filter, not merely those that don't fail it.
        final boolean fallbackAccept = !isDomumRecipe(recipe);
        final List<List<ItemStack>> newInputs = new ArrayList<>();
        boolean modified = false;

        for (final List<ItemStack> slot : recipe.getInputs())
        {
            final List<ItemStack> newSlot = slot.stream()
                    .filter(stack -> predicate.test(stack).orElse(fallbackAccept))
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
                recipe.getRequiredTool(),
                recipe.getRestrictions(),
                recipe.getLevelSort());
    }

    private static boolean isDomumRecipe(@NotNull final IGenericRecipe recipe)
    {
        final ItemStack output = recipe.getPrimaryOutput();
        if (output.isEmpty()) return false;

        if (output.getItem() instanceof BlockItem blockItem)
        {
            return blockItem.getBlock() instanceof IMateriallyTexturedBlock;
        }
        return false;
    }

    @NotNull
    private static Component getResearchDisplayName(@NotNull final ResourceLocation researchId)
    {
        final IGlobalResearchTree researchTree = IGlobalResearchTree.getInstance();

        // first, try to see if this is a research id
        final IGlobalResearch research = researchTree.getResearch(researchId);
        if (research != null)
        {
            return MutableComponent.create(research.getName());
        }

        // next, see if it's an effect id
        final Set<IGlobalResearch> researches = researchTree.getResearchForEffect(researchId);
        if (researches != null && !researches.isEmpty())
        {
            // there might be more than one, but this should be sufficient for now
            return MutableComponent.create(researches.iterator().next().getName());
        }

        // otherwise it may be an effect with no research (perhaps disabled via datapack)
        return Component.literal("???");
    }
}
