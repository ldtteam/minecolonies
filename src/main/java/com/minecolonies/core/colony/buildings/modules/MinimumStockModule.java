package com.minecolonies.core.colony.buildings.modules;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.modules.*;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.MinimumStack;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.WorldUtil;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;

import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

import static com.minecolonies.api.research.util.ResearchConstants.MINIMUM_STOCK;
import static com.minecolonies.api.research.util.ResearchConstants.MIN_ORDER;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_QUANTITY;

/**
 * Minimum stock module.
 */
public class MinimumStockModule extends AbstractBuildingModule implements IMinimumStockModule, IPersistentModule, ITickingModule, IAltersRequiredItems
{
    /**
     * Minimum stock it can hold per level.
     */
    private static final int STOCK_PER_LEVEL = 5;

    /**
     * The minimum stock tag.
     */
    private static final String TAG_MINIMUM_STOCK = "minstock";

    /**
     * The minimum stock.
     */
    protected final Map<ItemStorage, Integer> minimumStock = new HashMap<>();

    /**
     * Calculate the minimum stock size.
     *
     * @return the size.
     */
    private int minimumStockSize()
    {
        final double increase = 1 + building.getColony().getResearchManager().getResearchEffects().getEffectStrength(MINIMUM_STOCK);

        return (int) (building.getBuildingLevel() * STOCK_PER_LEVEL * increase);
    }

    @Override
    public void addMinimumStock(final ItemStack itemStack, final int quantity)
    {
        if (minimumStock.containsKey(new ItemStorage(itemStack)) || minimumStock.size() < minimumStockSize())
        {
            minimumStock.put(new ItemStorage(itemStack), quantity);
            markDirty();
        }
    }

    /**
     * Get the request from the list that matches this stack.
     * @param stack the stack to search for in the requests.
     * @param list the list of requests.
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

    @Override
    public void removeMinimumStock(final ItemStack itemStack)
    {
        minimumStock.remove(new ItemStorage(itemStack));

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

            for (final Map.Entry<ItemStorage, Integer> entry : minimumStock.entrySet())
            {
                final ItemStack itemStack = entry.getKey().getItemStack().copy();

                if (itemStack.isEmpty())
                {
                    continue;
                }

                final int target = entry.getValue() * itemStack.getMaxStackSize();
                final int count = InventoryUtils.hasBuildingEnoughElseCount(this.building, new ItemStorage(itemStack, true), target);
                final int delta = target - count;
                final IToken<?> request = getMatchingRequest(itemStack, list);
                if (delta > (building.getColony().getResearchManager().getResearchEffects().getEffectStrength(MIN_ORDER) > 0 ? target / 4 : 0))
                {
                    if (request == null)
                    {
                        itemStack.setCount(Math.min(itemStack.getMaxStackSize(), delta));
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

    @Override
    public boolean isStocked(final ItemStack stack)
    {
        return minimumStock.containsKey(new ItemStorage(stack));
    }

    @Override
    public void alterItemsToBeKept(final TriConsumer<Predicate<ItemStack>, Integer, Boolean> consumer)
    {
        if(!minimumStock.isEmpty())
        {
            for(ItemStorage item:minimumStock.keySet())
            {
                consumer.accept(stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, item.getItemStack(), false, true), minimumStock.get(item).intValue() * item.getItemStack().getMaxStackSize(), false);
            }
        }
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        minimumStock.clear();
        final ListTag minimumStockTagList = compound.getList(TAG_MINIMUM_STOCK, Tag.TAG_COMPOUND);
        for (int i = 0; i < minimumStockTagList.size(); i++)
        {
            final CompoundTag compoundNBT = minimumStockTagList.getCompound(i);
            minimumStock.put(new ItemStorage(ItemStack.of(compoundNBT)), compoundNBT.getInt(TAG_QUANTITY));
        }
    }

    @Override
    public void serializeNBT(final CompoundTag compound)
    {
        @NotNull final ListTag minimumStockTagList = new ListTag();
        for (@NotNull final Map.Entry<ItemStorage, Integer> entry : minimumStock.entrySet())
        {
            final CompoundTag compoundNBT = new CompoundTag();
            entry.getKey().getItemStack().save(compoundNBT);
            compoundNBT.putInt(TAG_QUANTITY, entry.getValue());
            minimumStockTagList.add(compoundNBT);
        }
        compound.put(TAG_MINIMUM_STOCK, minimumStockTagList);
    }

    @Override
    public void serializeToView(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(minimumStock.size());
        for (final Map.Entry<ItemStorage, Integer> entry : minimumStock.entrySet())
        {
            buf.writeItem(entry.getKey().getItemStack());
            buf.writeInt(entry.getValue());
        }
        buf.writeBoolean(minimumStock.size() >= minimumStockSize());
    }
}
