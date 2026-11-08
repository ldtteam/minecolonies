package com.minecolonies.core.compatibility.jei;

import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.crafting.ModCraftingTypes;
import com.minecolonies.api.crafting.registry.CraftingType;
import com.minecolonies.api.util.ItemStackUtils;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IModIdHelper;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JEI recipe category showing restaurant cooking.
 */
public class CookRecipeCategory extends GenericRecipeCategory
{
    /**
     * Constructor
     */
    public CookRecipeCategory(@NotNull final IGuiHelper guiHelper,
        @NotNull final IModIdHelper modIdHelper)
    {
        super(ModBuildings.cook.get(), ModJobs.cook.get().produceJob(null), guiHelper, modIdHelper);
    }

    @Override
    public @NotNull List<IGenericRecipe> findRecipes(
        final @NotNull Map<CraftingType, List<IGenericRecipe>> vanilla,
        final @NotNull List<Animal> animals,
        final @NotNull Level world)
    {
        final List<IGenericRecipe> recipes = new ArrayList<>();

        for (final IGenericRecipe recipe : vanilla.getOrDefault(ModCraftingTypes.SMELTING.get(), List.of()))
        {
            if (ItemStackUtils.ISCOOKABLE.test(recipe.getInputs().get(0).get(0)))
            {
                recipes.add(recipe);
            }
        }
        
        return recipes;
    }
}
