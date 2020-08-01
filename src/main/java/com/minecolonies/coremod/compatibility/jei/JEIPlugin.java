package com.minecolonies.coremod.compatibility.jei;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.compatibility.jei.transer.PrivateCraftingTeachingTransferHandler;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@mezz.jei.api.JeiPlugin
public class JEIPlugin implements IModPlugin
{
    @NotNull
    @Override
    public ResourceLocation getPluginUid()
    {
        return new ResourceLocation(Constants.MOD_ID);
    }

    @Override
    public void registerItemSubtypes(final ISubtypeRegistration iSubtypeRegistry)
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
    public void registerRecipeTransferHandlers(final IRecipeTransferRegistration registration)
    {
        registration.addRecipeTransferHandler(new PrivateCraftingTeachingTransferHandler(registration.getTransferHelper()), VanillaRecipeCategoryUid.CRAFTING);
    }

    @Override
    public void onRuntimeAvailable(final IJeiRuntime iJeiRuntime)
    {

    }
}
