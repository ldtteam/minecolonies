package com.minecolonies.api.util;

import com.minecolonies.api.crafting.ItemStorage;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.IItemHandler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.ldtteam.structurize.api.util.ItemStackUtils.getItemHandlersFromProvider;

public class InvDebugUtil
{
    /**
     * Sums up all items in the given provider/handlers
     *
     * @param provider inventory provider
     * @param handlers inventory handlers
     * @return Map of IdentityItemstorage
     */
    public static Map<ItemStorage, ItemStorage> getAllItemsForProviders(final ICapabilityProvider provider, final IItemHandler... handlers)
    {
        final Set<IItemHandler> providerHandlers = getItemHandlersFromProvider(provider);
        if (handlers != null)
        {
            providerHandlers.addAll(Arrays.asList(handlers));
        }

        return getAllItemsForProviders(providerHandlers);
    }

    /**
     * Sums up all items in the given handlers
     *
     * @param handlerList inventory handlers
     * @return Map of IdentityItemstorage
     */
    public static Map<ItemStorage, ItemStorage> getAllItemsForProviders(Set<IItemHandler> handlerList)
    {
        final Map<ItemStorage, ItemStorage> storageMap = new HashMap<>();
        for (final IItemHandler handler : handlerList)
        {
            for (int i = 0; i < handler.getSlots(); i++)
            {
                final ItemStack containedStack = handler.getStackInSlot(i);
                if (!ItemStackUtils.isEmpty(containedStack))
                {
                    final ItemStorage storage = new ItemStorage(containedStack.copy(), false, false);

                    if (storageMap.containsKey(storage))
                    {
                        final ItemStorage existing = storageMap.get(storage);
                        existing.setAmount(existing.getAmount() + storage.getAmount());
                    }
                    else
                    {
                        storageMap.put(storage, storage);
                    }
                }
            }
        }

        return storageMap;
    }

    /**
     * Returns whether two sets of itemstorage has the same content
     *
     * @param first     First set of item storages
     * @param second    Second set of item storages
     * @param showTrace whther to print a stacktrace on false
     * @return true if matching
     */
    public static boolean doStorageSetsMatch(Map<ItemStorage, ItemStorage> first, Map<ItemStorage, ItemStorage> second, boolean showTrace)
    {
        for (final ItemStorage storage : first.keySet())
        {
            final ItemStorage compareStorage = second.get(storage);

            if (compareStorage == null || storage.getAmount() != compareStorage.getAmount())
            {
                if (showTrace)
                {
                    Log.getLogger().warn("Possible inventory issue, not matching:", new Exception());
                }
                return false;
            }
        }

        for (final ItemStorage storage : second.keySet())
        {
            final ItemStorage compareStorage = first.get(storage);

            if (compareStorage == null || storage.getAmount() != compareStorage.getAmount())
            {
                if (showTrace)
                {
                    Log.getLogger().warn("Possible inventory issue, not matching:", new Exception());
                }
                return false;
            }
        }

        return true;
    }
}
