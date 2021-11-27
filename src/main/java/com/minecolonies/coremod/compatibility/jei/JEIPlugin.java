package com.minecolonies.coremod.compatibility.jei;

import com.google.common.collect.ImmutableMap;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.buildings.modules.IBuildingModule;
import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.crafting.CompostRecipe;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.buildings.modules.AnimalHerdingModule;
import com.minecolonies.coremod.colony.crafting.CustomRecipesReloadedEvent;
import com.minecolonies.coremod.compatibility.jei.transfer.CraftingGuiHandler;
import com.minecolonies.coremod.compatibility.jei.transfer.FurnaceCraftingGuiHandler;
import com.minecolonies.coremod.compatibility.jei.transfer.PrivateCraftingTeachingTransferHandler;
import com.minecolonies.coremod.compatibility.jei.transfer.PrivateSmeltingTeachingTransferHandler;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@mezz.jei.api.JeiPlugin
public class JEIPlugin implements IModPlugin
{
    public JEIPlugin()
    {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, CustomRecipesReloadedEvent.class, this::onCustomRecipesReloaded);
    }

    @NotNull
    @Override
    public ResourceLocation getPluginUid()
    {
        return new ResourceLocation(Constants.MOD_ID);
    }

    private final List<JobBasedRecipeCategory<?>> categories = new ArrayList<>();
    private boolean recipesLoaded;
    private WeakReference<IJeiRuntime> weakRuntime;

    @Override
    public void registerCategories(@NotNull final IRecipeCategoryRegistration registration)
    {
        final IJeiHelpers jeiHelpers = registration.getJeiHelpers();
        final IGuiHelper guiHelper = jeiHelpers.getGuiHelper();

        registration.addRecipeCategories(new CompostRecipeCategory(guiHelper));
        registration.addRecipeCategories(new FishermanRecipeCategory(guiHelper));

        categories.clear();
        for (final BuildingEntry building : IMinecoloniesAPI.getInstance().getBuildingRegistry())
        {
            for (final Supplier<IBuildingModule> producer : building.getModuleProducers())
            {
                final IBuildingModule module = producer.get();

                if (module instanceof ICraftingBuildingModule)
                {
                    final ICraftingBuildingModule crafting = (ICraftingBuildingModule) module;
                    final IJob<?> job = crafting.getCraftingJob();
                    if (job != null)
                    {
                        registerCategory(registration, new GenericRecipeCategory(building, job, crafting, guiHelper));
                    }
                }

                if (module instanceof AnimalHerdingModule)
                {
                    final AnimalHerdingModule herding = (AnimalHerdingModule) module;
                    registerCategory(registration, new HerderRecipeCategory(building, herding.getHerdingJob(), herding, guiHelper));
                }
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
        registration.addIngredientInfo(new ItemStack(ModBlocks.blockHutComposter.asItem()), VanillaTypes.ITEM,
                new TranslationTextComponent(TranslationConstants.COM_MINECOLONIES_JEI_PREFIX + ModJobs.COMPOSTER_ID.getPath()));

        if (!recipesLoaded && !Minecraft.getInstance().isLocalServer())
        {
            // if we're not on an integrated server, we're on a dedicated server, and that
            // means that the CustomRecipes are not loaded yet, so we need to wait until
            // later before we can populate the recipes -- unless we have received that event already.
            //
            // TODO this whole drama could probably go away if we loaded the CustomRecipes into
            //      the vanilla RecipeManager instead (and then they'd get automatically synced
            //      too) -- but that will probably have to wait for 1.17 since it would break all
            //      the datapacks.
            return;
        }

        populateRecipes(buildVanillaRecipesMap(), registration::addRecipes);
        recipesLoaded = true;
    }

    @Override
    public void registerRecipeCatalysts(@NotNull final IRecipeCatalystRegistration registration)
    {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.blockBarrel), CompostRecipe.ID);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.blockHutComposter), CompostRecipe.ID);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.blockHutFisherman), ModJobs.FISHERMAN_ID);

        for (final JobBasedRecipeCategory<?> category : this.categories)
        {
            registration.addRecipeCatalyst(category.getCatalyst(), category.getUid());
        }
    }

    @Override
    public void registerRecipeTransferHandlers(@NotNull final IRecipeTransferRegistration registration)
    {
        registration.addRecipeTransferHandler(new PrivateCraftingTeachingTransferHandler(registration.getTransferHelper()), VanillaRecipeCategoryUid.CRAFTING);
        registration.addRecipeTransferHandler(new PrivateSmeltingTeachingTransferHandler(registration.getTransferHelper()), VanillaRecipeCategoryUid.FURNACE);
    }

    @Override
    public void registerGuiHandlers(@NotNull final IGuiHandlerRegistration registration)
    {
        new CraftingGuiHandler(this.categories).register(registration);
        new FurnaceCraftingGuiHandler(this.categories).register(registration);
    }

    @Override
    public void onRuntimeAvailable(@NotNull final IJeiRuntime jeiRuntime)
    {
        this.weakRuntime = new WeakReference<>(jeiRuntime);
    }

    private Map<IRecipeType<?>, List<IGenericRecipe>> buildVanillaRecipesMap()
    {
        final RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        final List<IGenericRecipe> craftingRecipes = new ArrayList<>();
        for (final IRecipe<CraftingInventory> recipe : recipeManager.byType(IRecipeType.CRAFTING).values())
        {
            if (!recipe.canCraftInDimensions(3, 3)) continue;

            tryAddingVanillaRecipe(craftingRecipes, recipe);
        }

        final List<IGenericRecipe> smeltingRecipes = new ArrayList<>();
        for (final IRecipe<IInventory> recipe : recipeManager.byType(IRecipeType.SMELTING).values())
        {
            tryAddingVanillaRecipe(smeltingRecipes, recipe);
        }

        return new ImmutableMap.Builder<IRecipeType<?>, List<IGenericRecipe>>()
                .put(IRecipeType.CRAFTING, craftingRecipes)
                .put(IRecipeType.SMELTING, smeltingRecipes)
                .build();
    }

    private void tryAddingVanillaRecipe(@NotNull final List<IGenericRecipe> recipes,
                                        @NotNull final IRecipe<?> recipe)
    {
        if (recipe.getResultItem().isEmpty()) return;     // invalid or special recipes

        try
        {
            final IGenericRecipe genericRecipe = GenericRecipeUtils.create(recipe);
            if (genericRecipe.getInputs().isEmpty()) return;

            recipes.add(genericRecipe);
        }
        catch (final Exception ex)
        {
            Log.getLogger().warn("Error evaluating recipe " + recipe.getId() + "; ignoring.", ex);
        }
    }

    private void populateRecipes(@NotNull final Map<IRecipeType<?>, List<IGenericRecipe>> vanilla,
                                 @NotNull final BiConsumer<Collection<?>, ResourceLocation> registrar)
    {
        registrar.accept(CompostRecipeCategory.findRecipes(), CompostRecipe.ID);
        registrar.accept(FishermanRecipeCategory.findRecipes(), ModJobs.FISHERMAN_ID);

        for (final JobBasedRecipeCategory<?> category : this.categories)
        {
            try
            {
                registrar.accept(category.findRecipes(vanilla), category.getUid());
            }
            catch (Exception e)
            {
                Log.getLogger().error("Failed to process recipes for " + category.getTitle(), e);
            }
        }
    }

    private void onCustomRecipesReloaded(@NotNull final CustomRecipesReloadedEvent event)
    {
        // if this happens after JEI has loaded, it means we're on a dedicated server and
        // we couldn't register the recipes above since they weren't loaded yet, so we
        // need to load them now.  this uses a deprecated API in JEI but it seems like the
        // only way to get things to actually work.  just to make life more difficult,
        // though, some mods (such as JER) can change the order things happen, so this
        // can actually happen first (but that's ok, it means we can load the recipes in
        // the usual place once we know that happened).
        if (weakRuntime != null && !recipesLoaded && !categories.isEmpty())
        {
            final IJeiRuntime runtime = weakRuntime.get();
            if (runtime != null)
            {
                final IRecipeManager jeiManager = runtime.getRecipeManager();
                populateRecipes(buildVanillaRecipesMap(), (list, uid) ->
                {
                    for (final Object recipe : list)
                    {
                        //noinspection deprecation
                        jeiManager.addRecipe(recipe, uid);
                    }
                });
            }
        }
        recipesLoaded = true;
    }
}
