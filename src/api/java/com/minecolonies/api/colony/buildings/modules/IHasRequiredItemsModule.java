package com.minecolonies.api.colony.buildings.modules;

import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.crafting.ItemStorage;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Predicate;

/**
 * Module defining items to be left behind and not used otherwise.
 */
public interface IHasRequiredItemsModule extends IBuildingModule
{
    /**
     * Override this method if you want to keep an amount of items in inventory. When the inventory is full, everything get's dumped into the building chest. But you can use this
     * method to hold some stacks back.
     *
     * @return a list of objects which should be kept.
     */
    Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> getRequiredItemsAndAmount();

    /**
     * Calculate the number of reserved stacks the resolver can't touch.
     * @return a list of itemstorages.
     */
    default Map<ItemStorage, Integer> reservedStacks()
    {
        return reservedStacksExcluding(null);
    }

    /**
     * Calculate the number of reserved stacks the resolver can't touch.
     * @param excluded ignore reservations that would exist for the current request.
     * @return a list of itemstorages.
     */
    Map<ItemStorage, Integer> reservedStacksExcluding(@Nullable final IRequest<? extends IDeliverable> excluded);
}
