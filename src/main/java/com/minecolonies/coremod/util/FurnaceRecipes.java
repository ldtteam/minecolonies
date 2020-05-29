package com.minecolonies.coremod.util;

import com.minecolonies.api.colony.requestsystem.token.StandardToken;
import com.minecolonies.api.compatibility.IFurnaceRecipes;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.crafting.RecipeStorage;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.network.messages.client.UpdateClientWithRecipesMessage;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.NonNullList;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.registries.ObjectHolder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ObjectHolder(Constants.MOD_ID)
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FurnaceRecipes implements IFurnaceRecipes
{
    /**
     * Furnace recipes.
     */
    private Map<ItemStorage, RecipeStorage> recipes = new HashMap<>();

    /**
     * Instance of the furnace recipes.
     */
    public static FurnaceRecipes instance;

    /**
     * Load all the recipes in the recipe storage.
     * @param server the server obj to load.
     */
    private void loadRecipes(final MinecraftServer server)
    {
        server.getRecipeManager().getRecipes(IRecipeType.SMELTING).values().forEach(recipe -> {
            final NonNullList<Ingredient> list = recipe.getIngredients();
            if (list.size() == 1)
            {
                for (final ItemStack stack : list.get(0).getMatchingStacks())
                {
                    recipes.put(new ItemStorage(stack), new RecipeStorage(new StandardToken(), Collections.singletonList(stack), 1, recipe.getRecipeOutput(), Blocks.FURNACE));
                }
            }
        });
    }

    @SubscribeEvent
    public static void onServerStarting(final FMLServerStartedEvent event)
    {
        instance.loadRecipes(event.getServer());
        loadUtilityPredicates();
    }

    /**
     * Load all the utility predicates.
     */
    private static void loadUtilityPredicates()
    {
        ItemStackUtils.ISFOOD = itemStack -> !ItemStackUtils.isEmpty(itemStack) && itemStack.getItem().isFood();
        ItemStackUtils.IS_SMELTABLE = itemStack -> !ItemStackUtils.isEmpty(instance.getSmeltingResult(itemStack));
        ItemStackUtils.CAN_EAT = itemStack -> !ItemStackUtils.isEmpty(itemStack) && itemStack.getItem().isFood() && !ItemStackUtils.ISFOOD.test(instance.getSmeltingResult(itemStack));
        ItemStackUtils.ISCOOKABLE = itemStack -> ItemStackUtils.ISFOOD.test(instance.getSmeltingResult(itemStack));
    }

    @SubscribeEvent
    public static void onServerStarting(final FMLServerAboutToStartEvent event)
    {
        instance = new FurnaceRecipes();
        loadUtilityPredicates();
    }

    @SubscribeEvent
    public static void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent event)
    {
        if (!event.getPlayer().world.isRemote)
        {
            Network.getNetwork().sendToPlayer(new UpdateClientWithRecipesMessage(instance.recipes), (ServerPlayerEntity) event.getPlayer());
        }
    }

    /**
     * Set the map.
     * This is called from the client side message.
     * @param map the map to set.
     */
    public void setMap(final Map<ItemStorage, RecipeStorage> map)
    {
        this.recipes = map;
        if (ItemStackUtils.ISFOOD == null)
        {
            loadUtilityPredicates();
        }
    }

    /**
     * Get the smelting result for a certain itemStack.
     * @param itemStack the itemStack to test.
     * @return the result or empty if not existent.
     */
    public ItemStack getSmeltingResult(final ItemStack itemStack)
    {
        final RecipeStorage storage = recipes.getOrDefault(new ItemStorage(itemStack), null);
        if (storage != null)
        {
            return storage.getPrimaryOutput();
        }
        return ItemStack.EMPTY;
    }

    /**
     * Get the instance of the class.
     * @return the instance.
     */
    public static FurnaceRecipes getInstance()
    {
        if (instance == null)
        {
            instance = new FurnaceRecipes();
        }
        return instance;
    }

    /**
     * Method to check if the furnace recipes are loaded already.
     * @return true if so.
     */
    public boolean loaded()
    {
        return !recipes.isEmpty();
    }
}
