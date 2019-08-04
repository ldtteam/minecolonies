package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.blockout.Log;
import com.minecolonies.coremod.colony.Colony;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;

/**
 * Abstract class for all buildings which require a filterable list of allowed items.
 */
public abstract class AbstractFilterableListBuilding extends AbstractBuildingWorker
{
    /**
     * Tag to store the item list.
     */
    private static final String TAG_ITEMLIST = "itemList";

    /**
     * List of allowed items.
     */
    private final Map<String, List<ItemStorage>> itemsAllowed = new HashMap<>();

    /**
     * The constructor of the building.
     *
     * @param c the colony
     * @param l the position
     */
    public AbstractFilterableListBuilding(@NotNull final Colony c, final BlockPos l)
    {
        super(c, l);
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);
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
                    items.add(new ItemStorage(new ItemStack(filterableItems.getCompound(j))));
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
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();
        @NotNull final ListNBT filterableListCompound = new ListNBT();
        for(@NotNull final Map.Entry<String, List<ItemStorage>> entry : itemsAllowed.entrySet())
        {
            @NotNull final CompoundNBT listCompound = new CompoundNBT();
            listCompound.putString(TAG_ID, entry.getKey());
            @NotNull final ListNBT filteredItems = new ListNBT();
            for(@NotNull final ItemStorage item : entry.getValue())
            {
                @NotNull final CompoundNBT itemCompound = new CompoundNBT();
                item.getItemStack().writeToNBT(itemCompound);
                filteredItems.add(itemCompound);
            }
            listCompound.put(TAG_ITEMLIST, filteredItems);
            filterableListCompound.add(listCompound);
        }
        compound.put(TAG_ITEMLIST, filterableListCompound);
        return compound;
    }

    /**
     * Add a compostable item to the list.
     * @param item the item to add.
     */
    public void addItem(final String id, final ItemStorage item)
    {
        if(itemsAllowed.containsKey(id))
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

    /**
     * Check if the item is an allowed item.
     * @param item the item to check.
     * @return true if so.
     */
    public boolean isAllowedItem(final String id, final ItemStorage item)
    {
        return itemsAllowed.containsKey(id) && itemsAllowed.get(id).contains(item);
    }

    /**
     * Remove a compostable item from the list.
     * @param item the item to remove.
     */
    public void removeItem(final String id, final ItemStorage item)
    {
        if(itemsAllowed.containsKey(id) && itemsAllowed.get(id).contains(item))
        {
            final List<ItemStorage> list = itemsAllowed.get(id);
            list.remove(item);
            itemsAllowed.put(id, list);
        }
        markDirty();
    }

    /**
     * Getter of copy of all allowed items.
     * @return a copy.
     */
    public Map<String, List<ItemStorage>> getCopyOfAllowedItems()
    {
        return new HashMap<>(itemsAllowed);
    }

    @Override
    public void serializeToView(@NotNull final ByteBuf buf)
    {
        super.serializeToView(buf);
        buf.writeInt(itemsAllowed.size());
        for (final Map.Entry<String, List<ItemStorage>> entry : itemsAllowed.entrySet())
        {
            ByteBufUtils.writeUTF8String(buf, entry.getKey());
            buf.writeInt(entry.getValue().size());
            for (final ItemStorage item : entry.getValue())
            {
                ByteBufUtils.writeItemStack(buf, item.getItemStack());
            }
        }
    }
}
