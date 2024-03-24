package com.minecolonies.core.colony.buildings.modules;

import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.crafting.ModCraftingTypes;
import com.minecolonies.api.crafting.registry.CraftingType;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.OptionalPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/**
 * An abstract crafting module for Domum Ornamentum cutter recipes
 */
public abstract class AbstractDOCraftingBuildingModule extends AbstractCraftingBuildingModule.Custom
{
    protected AbstractDOCraftingBuildingModule(@NotNull final JobEntry jobEntry)
    {
        super(jobEntry);
    }

    // ideally this should override getId() and return something DO-ish and not "custom" (which would also let a
    // more appropriate icon be shown in the UI), but changing that now would break the existing saved recipes
    // without a special upgrade...

    @Override
    public Set<CraftingType> getSupportedCraftingTypes()
    {
        return Set.of(ModCraftingTypes.ARCHITECTS_CUTTER.get());
    }

    @Override
    public boolean isRecipeCompatible(final @NotNull IGenericRecipe recipe)
    {
        final OptionalPredicate<ItemStack> validator = getIngredientValidator();
        final ItemStack stack = recipe.getPrimaryOutput();
        if (BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals("domum_ornamentum"))
        {
            for (final List<ItemStack> slot : recipe.getInputs())
            {
                // when teaching there should only be one stack in each slot; for JEI there may be more.
                // any one compatible ingredient in any slot makes the whole recipe acceptable.
                for (final ItemStack ingredientStack : slot)
                {
                    if (!ItemStackUtils.isEmpty(stack) && validator.test(ingredientStack).orElse(false))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // override getIngredientValidator() to limit compatible ingredients
}
