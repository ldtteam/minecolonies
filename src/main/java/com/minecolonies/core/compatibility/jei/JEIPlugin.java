package com.minecolonies.core.compatibility.jei;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.crafting.registry.CraftingType;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.core.colony.buildings.modules.AnimalHerdingModule;
import com.minecolonies.core.colony.crafting.RecipeAnalyzer;
import com.minecolonies.core.compatibility.jei.transfer.*;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;

@mezz.jei.api.JeiPlugin
public class JEIPlugin implements IModPlugin
{
    @NotNull
    @Override
    public ResourceLocation getPluginUid()
    {
        return new ResourceLocation(Constants.MOD_ID);
    }

    private final List<JobBasedRecipeCategory<?>> categories = new ArrayList<>();

    @Override
    public void registerCategories(@NotNull final IRecipeCategoryRegistration registration)
    {
        final IJeiHelpers jeiHelpers = registration.getJeiHelpers();
        final IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
        final IModIdHelper modIdHelper = jeiHelpers.getModIdHelper();

        registration.addRecipeCategories(new ToolRecipeCategory(guiHelper));
        registration.addRecipeCategories(new CropRecipeCategory(guiHelper));
        registration.addRecipeCategories(new CompostRecipeCategory(guiHelper));
        registration.addRecipeCategories(new FishermanRecipeCategory(guiHelper));
        registration.addRecipeCategories(new FloristRecipeCategory(guiHelper));

        categories.clear();
        for (final BuildingEntry building : IMinecoloniesAPI.getInstance().getBuildingRegistry())
        {
            final Map<JobEntry, GenericRecipeCategory> craftingCategories = new HashMap<>();

            for (final BuildingEntry.ModuleProducer producer : building.getModuleProducers())
            {
                if (!producer.hasServerModule())
                {
                    continue;
                }

                final var module = BuildingEntry.produceModuleWithoutBuilding(producer.key);

                if (module == null)
                {
                    continue;
                }

                if (module instanceof final ICraftingBuildingModule crafting)
                {
                    final IJob<?> job = crafting.getCraftingJob();
                    if (job != null)
                    {
                        GenericRecipeCategory category = craftingCategories.get(job.getJobRegistryEntry());
                        if (category == null)
                        {
                            category = new GenericRecipeCategory(building, job, guiHelper, modIdHelper);
                            craftingCategories.put(job.getJobRegistryEntry(), category);
                        }
                        category.addModule(crafting);
                    }
                }
                else if (module instanceof final AnimalHerdingModule herding)
                {
                    final IJob<?> job = herding.getHerdingJob();
                    GenericRecipeCategory category = craftingCategories.get(job.getJobRegistryEntry());
                    if (category == null)
                    {
                        category = new GenericRecipeCategory(building, job, guiHelper, modIdHelper);
                        craftingCategories.put(job.getJobRegistryEntry(), category);
                    }
                    category.addModule(herding);
                }
            }

            for (final GenericRecipeCategory category : craftingCategories.values())
            {
                registerCategory(registration, category);
            }
        }
    }

    private void registerCategory(@NotNull final IRecipeCategoryRegistration registration,
                                  @NotNull final JobBasedRecipeCategory<?> category)
    {
        categories.add(category);
        registration.addRecipeCategories(category);
    }

    @Override
    public void registerRecipes(@NotNull final IRecipeRegistration registration)
    {
        registration.addIngredientInfo(new ItemStack(ModBlocks.blockHutComposter.asItem()), VanillaTypes.ITEM_STACK,
                Component.translatable(TranslationConstants.PARTIAL_JEI_INFO + ModJobs.COMPOSTER_ID.getPath()));

        registration.addRecipes(ModRecipeTypes.TOOLS, ToolRecipeCategory.findRecipes());
        registration.addRecipes(ModRecipeTypes.CROPS, CropRecipeCategory.findRecipes());
        registration.addRecipes(ModRecipeTypes.COMPOSTING, CompostRecipeCategory.findRecipes());
        registration.addRecipes(ModRecipeTypes.FISHING, FishermanRecipeCategory.findRecipes());
        registration.addRecipes(ModRecipeTypes.FLOWERS, FloristRecipeCategory.findRecipes());

        final ClientLevel level = Objects.requireNonNull(Minecraft.getInstance().level);
        final Map<CraftingType, List<IGenericRecipe>> vanilla = RecipeAnalyzer.buildVanillaRecipesMap(level.getRecipeManager(), level);
        final List<Animal> animals = RecipeAnalyzer.createAnimals(level);

        for (final JobBasedRecipeCategory<?> category : this.categories)
        {
            addJobBasedRecipes(vanilla, animals, category, registration::addRecipes, level);
        }
    }

    private <R> void addJobBasedRecipes(@NotNull final Map<CraftingType, List<IGenericRecipe>> vanilla,
                                        @NotNull final List<Animal> animals,
                                        @NotNull final JobBasedRecipeCategory<R> category,
                                        @NotNull final BiConsumer<RecipeType<R>, List<R>> registrar,
                                        @NotNull final Level world)
    {
        try
        {
            registrar.accept(category.getRecipeType(), category.findRecipes(vanilla, animals, world));
        }
        catch (Exception e)
        {
            Log.getLogger().error("Failed to process recipes for " + category.getTitle(), e);
        }
    }

    @Override
    public void registerRecipeCatalysts(@NotNull final IRecipeCatalystRegistration registration)
    {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.blockBarrel), ModRecipeTypes.COMPOSTING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.blockHutComposter), ModRecipeTypes.COMPOSTING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.blockHutFisherman), ModRecipeTypes.FISHING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.blockHutFlorist), ModRecipeTypes.FLOWERS);

        for (final JobBasedRecipeCategory<?> category : this.categories)
        {
            registration.addRecipeCatalyst(category.getCatalyst(), category.getRecipeType());
        }
    }

    @Override
    public void registerRecipeTransferHandlers(@NotNull final IRecipeTransferRegistration registration)
    {
        registration.addRecipeTransferHandler(new PrivateCraftingTeachingTransferHandler(registration.getTransferHelper()), RecipeTypes.CRAFTING);
        registration.addRecipeTransferHandler(new PrivateSmeltingTeachingTransferHandler(registration.getTransferHelper()), RecipeTypes.SMELTING);
        registration.addRecipeTransferHandler(new PrivateBrewingTeachingTransferHandler(registration.getTransferHelper()), RecipeTypes.BREWING);
    }

    @Override
    public void registerGuiHandlers(@NotNull final IGuiHandlerRegistration registration)
    {
        new CraftingGuiHandler(this.categories).register(registration);
        new FurnaceCraftingGuiHandler(this.categories).register(registration);
        new BrewingCraftingGuiHandler(this.categories).register(registration);
    }
}
