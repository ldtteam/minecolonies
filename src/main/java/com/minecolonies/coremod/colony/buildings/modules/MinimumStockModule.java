package com.minecolonies.coremod.colony.buildings.modules;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.modules.*;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.ItemStackHandling;
import com.minecolonies.api.crafting.ItemStackStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.WorldUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

import static com.minecolonies.api.research.util.ResearchConstants.MINIMUM_STOCK;
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
        if (minimumStock.containsKey(new ItemStackStorage(itemStack)) || minimumStock.size() < minimumStockSize())
        {
            minimumStock.put(new ItemStackStorage(itemStack), quantity);
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
            if (iRequest != null && iRequest.getRequest() instanceof Stack && ((Stack) iRequest.getRequest()).getStack().sameItem(stack))
            {
                return token;
            }
        }
        return null;
    }

    @Override
    public void removeMinimumStock(final ItemStack itemStack)
    {
        minimumStock.remove(new ItemStackStorage(itemStack));

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
            final Collection<IToken<?>> list = building.getOpenRequestsByRequestableType().getOrDefault(TypeToken.of(Stack.class), new ArrayList<>());

            for (final Map.Entry<ItemStorage, Integer> entry : minimumStock.entrySet())
            {
                final ItemStack itemStack = entry.getKey().getItemStack().copy();

                if (itemStack.isEmpty())
                {
                    continue;
                }

                final int count = InventoryUtils.hasBuildingEnoughElseCount(this.building, new ItemStackStorage(itemStack), entry.getValue() * itemStack.getMaxStackSize());
                final int delta = (entry.getValue() * itemStack.getMaxStackSize()) - count;
                final IToken<?> request = getMatchingRequest(itemStack, list);
                if (delta > 0)
                {
                    if (request == null)
                    {
                        itemStack.setCount(Math.min(itemStack.getMaxStackSize(), delta));
                        final Stack stack = new Stack(itemStack, false);
                        building.createRequest(stack, false);
                    }
                }
                else if (request != null)
                {
                    building.getColony().getRequestManager().updateRequestState(request, RequestState.CANCELLED);
                }
            }
        }
    }

    @Override
    public boolean isMinimumStockRequest(final IRequest<? extends IDeliverable> request)
    {
        for (final Map.Entry<ItemStorage, Integer> entry : minimumStock.entrySet())
        {
            if (request.getRequest() instanceof com.minecolonies.api.colony.requestsystem.requestable.Stack
                  && ((Stack) request.getRequest()).getStack().sameItem(entry.getKey().getItemStack()))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isStocked(final ItemStack stack)
    {
        return minimumStock.containsKey(new ItemStackHandling(stack));
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
    public void deserializeNBT(final CompoundNBT compound)
    {
        minimumStock.clear();
        final ListNBT minimumStockTagList = compound.getList(TAG_MINIMUM_STOCK, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < minimumStockTagList.size(); i++)
        {
            final CompoundNBT compoundNBT = minimumStockTagList.getCompound(i);
            minimumStock.put(new ItemStackStorage(ItemStack.of(compoundNBT)), compoundNBT.getInt(TAG_QUANTITY));
        }
    }

    @Override
    public void serializeNBT(final CompoundNBT compound)
    {
        @NotNull final ListNBT minimumStockTagList = new ListNBT();
        for (@NotNull final Map.Entry<ItemStorage, Integer> entry : minimumStock.entrySet())
        {
            final CompoundNBT compoundNBT = new CompoundNBT();
            entry.getKey().getItemStack().save(compoundNBT);
            compoundNBT.putInt(TAG_QUANTITY, entry.getValue());
            minimumStockTagList.add(compoundNBT);
        }
        compound.put(TAG_MINIMUM_STOCK, minimumStockTagList);
    }

    @Override
    public void serializeToView(@NotNull final PacketBuffer buf)
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
