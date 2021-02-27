package com.minecolonies.coremod.colony.buildings.modules;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IGroupedItemListModule;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.Log;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;

/**
 * Abstract class for all buildings which require a filterable list of allowed items.
 */
public class GroupedItemListModule extends AbstractBuildingModule implements IGroupedItemListModule, IPersistentModule
{
    /**
     * Tag to store the item list.
     */
    private static final String TAG_ITEMLIST = "itemList";

    /**
     * List of allowed items.
     */
    private final Map<String, List<ItemStorage>> itemsAllowed = new HashMap<>();

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        final ListNBT filterableList = compound.getList(TAG_ITEMLIST, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < filterableList.size(); ++i)
        {
            try
            {
                final CompoundNBT listItem = filterableList.getCompound(i);
                final String id = listItem.getString(TAG_ID);
                final ListNBT filterableItems = listItem.getList(TAG_ITEMLIST, Constants.NBT.TAG_COMPOUND);
                final List<ItemStorage> items = new ArrayList<>();
                for (int j = 0; j < filterableItems.size(); ++j)
                {
                    items.add(new ItemStorage(ItemStack.read(filterableItems.getCompound(j))));
                }
                if (!items.isEmpty())
                {
                    itemsAllowed.put(id, items);
                }
            }
            catch (Exception e)
            {
                Log.getLogger().info("Removing incompatible stack");
            }
        }
    }

    @Override
    public void serializeNBT(final CompoundNBT compound)
    {
        @NotNull final ListNBT filterableListCompound = new ListNBT();
        for (@NotNull final Map.Entry<String, List<ItemStorage>> entry : itemsAllowed.entrySet())
        {
            @NotNull final CompoundNBT listCompound = new CompoundNBT();
            listCompound.putString(TAG_ID, entry.getKey());
            @NotNull final ListNBT filteredItems = new ListNBT();
            for (@NotNull final ItemStorage item : entry.getValue())
            {
                @NotNull final CompoundNBT itemCompound = new CompoundNBT();
                item.getItemStack().write(itemCompound);
                filteredItems.add(itemCompound);
            }
            listCompound.put(TAG_ITEMLIST, filteredItems);
            filterableListCompound.add(listCompound);
        }
        compound.put(TAG_ITEMLIST, filterableListCompound);
    }

    @Override
    public void addItem(final String id, final ItemStorage item)
    {
        if (itemsAllowed.containsKey(id))
        {
            if (!itemsAllowed.get(id).contains(item))
            {
                final List<ItemStorage> list = itemsAllowed.get(id);
                list.add(item);
                itemsAllowed.put(id, list);
            }
        }
        else
        {
            final List<ItemStorage> list = new ArrayList<>();
            list.add(item);
            itemsAllowed.put(id, list);
        }
        markDirty();
    }

    @Override
    public boolean isItemInList(final String id, final ItemStorage item)
    {
        return itemsAllowed.containsKey(id) && itemsAllowed.get(id).contains(item);
    }

    @Override
    public void removeItem(final String id, final ItemStorage item)
    {
        if (itemsAllowed.containsKey(id) && itemsAllowed.get(id).contains(item))
        {
            final List<ItemStorage> list = itemsAllowed.get(id);
            list.remove(item);
            itemsAllowed.put(id, list);
        }
        markDirty();
    }

    @Override
    public ImmutableList<ItemStorage> getList(final String id)
    {
        return ImmutableList.copyOf(itemsAllowed.getOrDefault(id, Collections.emptyList()));
    }

    @Override
    public void serializeToView(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(itemsAllowed.size());
        for (final Map.Entry<String, List<ItemStorage>> entry : itemsAllowed.entrySet())
        {
            buf.writeString(entry.getKey());
            buf.writeInt(entry.getValue().size());
            for (final ItemStorage item : entry.getValue())
            {
                buf.writeItemStack(item.getItemStack());
            }
        }
    }
}
