package com.minecolonies.coremod.compatibility.jei;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.crafting.CompostRecipe;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.compatibility.jei.transfer.PrivateCraftingTeachingTransferHandler;
import com.minecolonies.coremod.compatibility.jei.transfer.PrivateSmeltingTeachingTransferHandler;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.registration.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@mezz.jei.api.JeiPlugin
public class JEIPlugin implements IModPlugin
{
    @NotNull
    @Override
    public ResourceLocation getPluginUid()
    {
        return new ResourceLocation(Constants.MOD_ID);
    }
    private final List<GenericRecipeCategory> categories = new ArrayList<>();

    @Override
    public void registerCategories(@NotNull final IRecipeCategoryRegistration registration)
    {
        final IJeiHelpers jeiHelpers = registration.getJeiHelpers();
        final IGuiHelper guiHelper = jeiHelpers.getGuiHelper();

        registration.addRecipeCategories(new CompostRecipeCategory(guiHelper));

        categories.clear();
        for (final BuildingEntry building : IMinecoloniesAPI.getInstance().getBuildingRegistry())
        {
            building.getModuleProducers().stream()
                    .map(Supplier::get)
                    .filter(m -> m instanceof ICraftingBuildingModule)
                    .map(m -> (ICraftingBuildingModule) m)
                    .forEach(crafting ->
                    {
                        final IJob<?> job = crafting.getCraftingJob();
                        if (job != null)
                        {
                            final GenericRecipeCategory category = new GenericRecipeCategory(building, job, crafting, guiHelper);
                            categories.add(category);
                            registration.addRecipeCategories(category);
                        }
                    });
        }
    }

    @Override
    public void registerRecipes(@NotNull final IRecipeRegistration registration)
    {
        for (final GenericRecipeCategory category : this.categories)
        {
            try
            {
                registration.addRecipes(category.findRecipes(), category.getUid());
            }
            catch (Exception e)
            {
                Log.getLogger().error("Failed to process recipes for " + category.getTitle(), e);
            }
        }

        registration.addRecipes(CompostRecipeCategory.findRecipes(), CompostRecipe.ID);
        registration.addIngredientInfo(new ItemStack(ModBlocks.blockHutComposter.asItem()), VanillaTypes.ITEM, TranslationConstants.COM_MINECOLONIES_JEI_PREFIX + ModJobs.COMPOSTER_ID.getPath());
    }

    @Override
    public void registerRecipeCatalysts(@NotNull final IRecipeCatalystRegistration registration)
    {
        for (final GenericRecipeCategory category : this.categories)
        {
            registration.addRecipeCatalyst(category.getCatalyst(), category.getUid());
        }

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
