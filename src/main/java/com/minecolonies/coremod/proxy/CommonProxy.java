package com.minecolonies.coremod.proxy;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.colonyEvents.registry.ColonyEventDescriptionTypeRegistryEntry;
import com.minecolonies.api.colony.colonyEvents.registry.ColonyEventTypeRegistryEntry;
import com.minecolonies.api.colony.guardtype.GuardType;
import com.minecolonies.api.colony.interactionhandling.registry.InteractionResponseHandlerEntry;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.crafting.CompostRecipe;
import com.minecolonies.api.crafting.CountedIngredient;
import com.minecolonies.api.crafting.registry.RecipeTypeEntry;
import com.minecolonies.api.research.effects.registry.ResearchEffectEntry;
import com.minecolonies.api.research.registry.ResearchRequirementEntry;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.apiimp.CommonMinecoloniesAPIImpl;
import com.minecolonies.apiimp.initializer.*;
import com.minecolonies.coremod.recipes.FoodIngredient;
import com.minecolonies.coremod.recipes.PlantIngredient;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.RecipeBook;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * CommonProxy of the minecolonies mod (Server and Client).
 */
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public abstract class CommonProxy implements IProxy
{
    /**
     * API instance.
     */
    protected static CommonMinecoloniesAPIImpl apiImpl;

    /**
     * Used to store IExtendedEntityProperties data temporarily between player death and respawn.
     */
    private static final Map<String, CompoundNBT> playerPropertiesData = new HashMap<>();

    /**
     * The special townhall recipe.
     */
    public static IRecipeSerializer<?> SPECIAL_REC;

    /**
     * Creates instance of proxy.
     */
    public CommonProxy()
    {
        apiImpl = new CommonMinecoloniesAPIImpl();
    }

    /**
     * Adds an entity's custom data to the map for temporary storage.
     *
     * @param name     player UUID + Properties name, HashMap key.
     * @param compound An NBT Tag Compound that stores the IExtendedEntityProperties data only.
     */
    public static void storeEntityData(final String name, final CompoundNBT compound)
    {
        playerPropertiesData.put(name, compound);
    }

    /**
     * Removes the compound from the map and returns the NBT tag stored for name or null if none exists.
     *
     * @param name player UUID + Properties name, HashMap key.
     * @return CompoundNBT PlayerProperties NBT compound.
     */
    public static CompoundNBT getEntityData(final String name)
    {
        return playerPropertiesData.remove(name);
    }

    @SubscribeEvent
    public static void registerGuardTypes(final RegistryEvent.Register<GuardType> event)
    {
        ModGuardTypesInitializer.init(event);
    }

    @SubscribeEvent
    public static void registerNewRegistries(final RegistryEvent.NewRegistry event)
    {
        apiImpl.onRegistryNewRegistry(event);
    }

    @Override
    public void setupApi()
    {
        MinecoloniesAPIProxy.getInstance().setApiInstance(apiImpl);
    }

    @SubscribeEvent
    public static void registerBuildingTypes(@NotNull final RegistryEvent.Register<BuildingEntry> event)
    {
        ModBuildingsInitializer.init(event);
    }

    @SubscribeEvent
    public static void registerResearchRequirementTypes(@NotNull final RegistryEvent.Register<ResearchRequirementEntry> event)
    {
        ModResearchRequirementInitializer.init(event);
    }

    @SubscribeEvent
    public static void registerResearchEffectTypes(@NotNull final RegistryEvent.Register<ResearchEffectEntry> event)
    {
        ModResearchEffectInitializer.init(event);
    }

    @SubscribeEvent
    public static void registerInteractionTypes(@NotNull final RegistryEvent.Register<InteractionResponseHandlerEntry> event)
    {
        ModInteractionsInitializer.init(event);
    }

    @SubscribeEvent
    public static void registerColonyEventTypes(@NotNull final RegistryEvent.Register<ColonyEventTypeRegistryEntry> event)
    {
        ModColonyEventTypeInitializer.init(event);
    }

    @SubscribeEvent
    public static void registerColonyEventDescriptionTypes(@NotNull final RegistryEvent.Register<ColonyEventDescriptionTypeRegistryEntry> event)
    {
        ModColonyEventDescriptionTypeInitializer.init(event);
    }

    @SubscribeEvent
    public static void registerJobTypes(final RegistryEvent.Register<JobEntry> event)
    {
        ModJobsInitializer.init(event);
    }

    @SubscribeEvent
    public static void registerRecipeTypes(final RegistryEvent.Register<RecipeTypeEntry> event)
    {
        ModRecipeTypesInitializer.init(event);
    }

    @SubscribeEvent
    public static void registerRecipeSerializers(final RegistryEvent.Register<IRecipeSerializer<?>> event)
    {
        final IForgeRegistry<IRecipeSerializer<?>> r = event.getRegistry();
        r.register(CompostRecipe.Serializer.getInstance().setRegistryName(CompostRecipe.ID));

        CraftingHelper.register(CountedIngredient.ID, CountedIngredient.Serializer.getInstance());
        CraftingHelper.register(FoodIngredient.ID, FoodIngredient.Serializer.getInstance());
        CraftingHelper.register(PlantIngredient.ID, PlantIngredient.Serializer.getInstance());
    }

    @Override
    public boolean isClient()
    {
        return false;
    }

    @Override
    public void showCitizenWindow(final ICitizenDataView citizen)
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public void openBuildToolWindow(final BlockPos pos)
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public void openShapeToolWindow(final BlockPos pos)
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public void openSuggestionWindow(@NotNull BlockPos pos, @NotNull BlockState state, @NotNull final ItemStack stack)
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public void openBuildToolWindow(final BlockPos pos, final String structureName, final int rotation, final int groundstyle)
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public void openBannerRallyGuardsWindow(final ItemStack banner)
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public void openClipboardWindow(final IColonyView colonyView)
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public void openResourceScrollWindow(final int colonyId, final BlockPos pos)
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public File getSchematicsFolder()
    {
        return null;
    }

    @NotNull
    @Override
    public RecipeBook getRecipeBookFromPlayer(@NotNull final PlayerEntity player)
    {
        return ((ServerPlayerEntity) player).getRecipeBook();
    }

    @Override
    public void openDecorationControllerWindow(@NotNull final BlockPos pos)
    {
        /*
         * Intentionally left empty.
         */
    }
}
