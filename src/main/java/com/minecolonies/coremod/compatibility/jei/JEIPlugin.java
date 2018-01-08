package com.minecolonies.coremod.compatibility.jei;

import com.minecolonies.coremod.compatibility.jei.transer.PrivateCraftingTeachingTransferHandler;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;

@mezz.jei.api.JEIPlugin
public class JEIPlugin implements IModPlugin
{
    @Override
    public void registerItemSubtypes(final ISubtypeRegistry iSubtypeRegistry)
    {

    }

    @Override
    public void registerIngredients(final IModIngredientRegistration iModIngredientRegistration)
    {

    }

    @Override
    public void registerCategories(final IRecipeCategoryRegistration iRecipeCategoryRegistration)
    {

    }

    @Override
    public void register(final IModRegistry iModRegistry)
    {
        final IRecipeTransferRegistry recipeTranferRegistry = iModRegistry.getRecipeTransferRegistry();
        recipeTranferRegistry.addRecipeTransferHandler(new PrivateCraftingTeachingTransferHandler(iModRegistry.getJeiHelpers().recipeTransferHandlerHelper()), VanillaRecipeCategoryUid.CRAFTING);
    }

    @Override
    public void onRuntimeAvailable(final IJeiRuntime iJeiRuntime)
    {

    }
}
