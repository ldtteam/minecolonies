package com.minecolonies.core.colony.buildings.modules;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.*;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.MinimumStack;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.crafting.RecipeStorage;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.WorldUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.minecolonies.api.research.util.ResearchConstants.MIN_ORDER;

/**
 * Minimum stock module.
 */
public class RestaurantMenuModule extends AbstractBuildingModule implements IPersistentModule, ITickingModule, IAltersRequiredItems
{
    /**
     * Minimum stock it can hold per level.
     */
    public static final int STOCK_PER_LEVEL = 5;

    /**
     * The minimum stock tag.
     */
    private static final String TAG_MENU = "menu";

    /**
     * The minimum stock.
     */
    protected final Set<ItemStorage> menu = new HashSet<>();

    /**
     * Whether the worker here can cook.
     */
    private final boolean                      canCook;

    /**
     * Get max stock calculation.
     */
    private final Function<IBuilding, Integer> expectedStock;

    /**
     * Get the restaurant menu.
     * @return the menu.
     */
    public Set<ItemStorage> getMenu()
    {
        return menu;
    }

    /**
     * Create a restaurant menu module.
     * @param canCook whether the worker here can cook.
     */
    public RestaurantMenuModule(final boolean canCook, final Function<IBuilding, Integer> expectedStock )
    {
        this.canCook = canCook;
        this.expectedStock = expectedStock;
    }

    /**
     * Add a new menu item.
     * @param itemStack the menu item to add.
     */
    public void addMenuItem(final ItemStack itemStack)
    {
        if (menu.size() >= building.getBuildingLevel() * STOCK_PER_LEVEL)
        {
            return;
        }

        menu.add(new ItemStorage(itemStack));
        markDirty();
    }

    /**
     * Remove a menu item.
     * @param itemStack the menu item to remove.
     */
    public void removeMenuItem(final ItemStack itemStack)
    {
        menu.remove(new ItemStorage(itemStack));

        final Collection<IToken<?>> list = building.getOpenRequestsByRequestableType().getOrDefault(TypeToken.of(Stack.class), new ArrayList<>());
        final IToken<?> token = getMatchingRequest(itemStack, list);
        if (token != null)
        {
            building.getColony().getRequestManager().updateRequestState(token, RequestState.CANCELLED);
        }
        markDirty();
    }

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        if (WorldUtil.isBlockLoaded(colony.getWorld(), building.getPosition()))
        {
            final Collection<IToken<?>> list = building.getOpenRequestsByRequestableType().getOrDefault(TypeToken.of(MinimumStack.class), new ArrayList<>());

            for (final ItemStorage menuItem : menu)
            {
                ItemStack itemStack = menuItem.getItemStack().copy();
                if (itemStack.isEmpty())
                {
                    continue;
                }

                if (canCook && MinecoloniesAPIProxy.getInstance().getFurnaceRecipes().getFirstSmeltingRecipeByResult(menuItem) instanceof RecipeStorage recipeStorage)
                {
                    // Smelting Recipes only got 1 input. Request the input if this is a smeltable.

                    itemStack = recipeStorage.getInput().get(0).getItemStack();
                }

                final int target = itemStack.getMaxStackSize() * getExpectedStock();
                final int count = InventoryUtils.hasBuildingEnoughElseCount(this.building, new ItemStorage(itemStack, true), target);
                final int delta = target - count;
                final IToken<?> request = getMatchingRequest(itemStack, list);
                if (delta > (building.getColony().getResearchManager().getResearchEffects().getEffectStrength(MIN_ORDER) > 0 ? target / 4 : 0))
                {
                    if (request == null)
                    {
                        itemStack.setCount(Math.min(16, Math.min(itemStack.getMaxStackSize(), delta)));
                        final MinimumStack stack = new MinimumStack(itemStack, false);
                        stack.setCanBeResolvedByBuilding(false);
                        building.createRequest(stack, false);
                    }
                }
                else if (request != null && delta <= 0)
                {
                    building.getColony().getRequestManager().updateRequestState(request, RequestState.CANCELLED);
                }
            }
        }
    }

    /**
     * Get the request from the list that matches this stack.
     *
     * @param stack the stack to search for in the requests.
     * @param list  the list of requests.
     * @return the token of the matching request or null.
     */
    private IToken<?> getMatchingRequest(final ItemStack stack, final Collection<IToken<?>> list)
    {
        for (final IToken<?> token : list)
        {
            final IRequest<?> iRequest = building.getColony().getRequestManager().getRequestForToken(token);
            if (iRequest != null && iRequest.getRequest() instanceof Stack && ItemStackUtils.compareItemStacksIgnoreStackSize(((Stack) iRequest.getRequest()).getStack(), stack))
            {
                return token;
            }
        }
        return null;
    }

    /**
     * Get the max stock in stacks per menu item.
     * @return the max stock.
     */
    public int getExpectedStock()
    {
        return expectedStock.apply(building);
    }

    @Override
    public void alterItemsToBeKept(final TriConsumer<Predicate<ItemStack>, Integer, Boolean> consumer)
    {
        for (final ItemStorage menuItem : menu)
        {
            consumer.accept(stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, menuItem.getItemStack(), false, true), menuItem.getItemStack().getMaxStackSize() * getExpectedStock(), false);
        }
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        menu.clear();
        final ListTag minimumStockTagList = compound.getList(TAG_MENU, Tag.TAG_COMPOUND);
        for (int i = 0; i < minimumStockTagList.size(); i++)
        {
            menu.add(new ItemStorage(ItemStack.of(minimumStockTagList.getCompound(i))));
        }
    }

    @Override
    public void serializeNBT(final CompoundTag compound)
    {
        @NotNull final ListTag minimumStockTagList = new ListTag();
        for (final ItemStorage menuItem : menu)
        {
            minimumStockTagList.add(menuItem.getItemStack().save(new CompoundTag()));
        }
        compound.put(TAG_MENU, minimumStockTagList);
    }

    @Override
    public void serializeToView(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(menu.size());
        for (final ItemStorage menuItem : menu)
        {
            buf.writeItem(menuItem.getItemStack());
        }
    }
}
