package com.minecolonies.coremod.compatibility.jei;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.crafting.CompostRecipe;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.compatibility.jei.transfer.PrivateCraftingTeachingTransferHandler;
import com.minecolonies.coremod.compatibility.jei.transfer.PrivateSmeltingTeachingTransferHandler;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
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
    public void registerCategories(@NotNull final IRecipeCategoryRegistration registration)
    {
        final IJeiHelpers jeiHelpers = registration.getJeiHelpers();
        final IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
        registration.addRecipeCategories(new CompostRecipeCategory(guiHelper));
	}

    @Override
    public void registerRecipes(@NotNull final IRecipeRegistration registration)
    {
        // on initial world load, this is entirely redundant.  but sadly it's required for
        // supporting /reload or /datapack on multiplayer, as there we have separate
        // IColonyManager instances and only the server reloads via datapack event.
        IColonyManager.getInstance().getCompatibilityManager().invalidateRecipes(Minecraft.getInstance().world.getRecipeManager());

        registration.addRecipes(CompostRecipeCategory.findRecipes(), CompostRecipe.ID);
        registration.addIngredientInfo(new ItemStack(ModBlocks.blockHutComposter.asItem()), VanillaTypes.ITEM,
                TranslationConstants.COM_MINECOLONIES_JEI_PREFIX + ModJobs.COMPOSTER_ID.getPath());
    }

    @Override
    public void registerRecipeCatalysts(@NotNull final IRecipeCatalystRegistration registration)
    {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.blockBarrel), CompostRecipe.ID);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.blockHutComposter), CompostRecipe.ID);
    }

    @Override
    public void registerRecipeTransferHandlers(@NotNull final IRecipeTransferRegistration registration)
    {
        registration.addRecipeTransferHandler(new PrivateCraftingTeachingTransferHandler(registration.getTransferHelper()), VanillaRecipeCategoryUid.CRAFTING);
        registration.addRecipeTransferHandler(new PrivateSmeltingTeachingTransferHandler(registration.getTransferHelper()), VanillaRecipeCategoryUid.FURNACE);
    }
}
