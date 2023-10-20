package com.minecolonies.coremod.proxy;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.crafting.CountedIngredient;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.apiimp.CommonMinecoloniesAPIImpl;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.coremod.recipes.FoodIngredient;
import com.minecolonies.coremod.recipes.PlantIngredient;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;

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
    private static final Map<String, CompoundTag> playerPropertiesData = new HashMap<>();

    /**
     * The special townhall recipe.
     */
    public static RecipeSerializer<?> SPECIAL_REC;

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
    public static void storeEntityData(final String name, final CompoundTag compound)
    {
        playerPropertiesData.put(name, compound);
    }

    /**
     * Removes the compound from the map and returns the NBT tag stored for name or null if none exists.
     *
     * @param name player UUID + Properties name, HashMap key.
     * @return CompoundTag PlayerProperties NBT compound.
     */
    public static CompoundTag getEntityData(final String name)
    {
        return playerPropertiesData.remove(name);
    }

    @SubscribeEvent
    public static void registerNewRegistries(final NewRegistryEvent event)
    {
        apiImpl.onRegistryNewRegistry(event);
    }

    @Override
    public void setupApi()
    {
        MinecoloniesAPIProxy.getInstance().setApiInstance(apiImpl);
    }

    @SubscribeEvent
    public static void registerRecipeSerializers(final RegisterEvent event)
    {
        if (event.getRegistryKey().equals(ForgeRegistries.Keys.RECIPE_SERIALIZERS))
        {
            CraftingHelper.register(CountedIngredient.ID, CountedIngredient.Serializer.getInstance());
            CraftingHelper.register(FoodIngredient.ID, FoodIngredient.Serializer.getInstance());
            CraftingHelper.register(PlantIngredient.ID, PlantIngredient.Serializer.getInstance());
        }
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
    public void openSuggestionWindow(@NotNull BlockPos pos, @NotNull BlockState state, @NotNull final ItemStack stack)
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
    public void openResourceScrollWindow(@NotNull final BuildingBuilder.View buildingView, @NotNull final Map<String, Integer> warehouseSnapshot)
    {
        /*
         * Intentionally left empty.
         */
    }

    @NotNull
    @Override
    public RecipeBook getRecipeBookFromPlayer(@NotNull final Player player)
    {
        return ((ServerPlayer) player).getRecipeBook();
    }

    @Override
    public void openDecorationControllerWindow(@NotNull final BlockPos pos)
    {
        /*
         * Intentionally left empty.
         */
    }
}
