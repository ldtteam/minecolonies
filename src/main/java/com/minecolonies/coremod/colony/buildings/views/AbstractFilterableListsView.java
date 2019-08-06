package com.minecolonies.coremod.colony.buildings.views;

import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.network.messages.AssignFilterableItemMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client side representation of the filterable list window GUI.
 */
public abstract class AbstractFilterableListsView extends AbstractBuildingWorker.View
{
    /**
     * The list of items.
     */
    private final Map<String, List<ItemStorage>> listsOfItems = new HashMap<>();

    /**
     * Creates the view representation of the building.
     *
     * @param c the colony.
     * @param l the location.
     */
    public AbstractFilterableListsView(final IColonyView c, @NotNull final BlockPos l)
    {
        super(c, l);
    }

    /**
     * Add item to the view and notify the server side.
     * @param item the item to add.
     * @param id the id.
     */
    public void addItem(final String id, final ItemStorage item)
    {
        Network.getNetwork().sendToServer(new AssignFilterableItemMessage(this, id, item, true));
        if(listsOfItems.containsKey(id))
        {
            if (!listsOfItems.get(id).contains(item))
            {
                final List<ItemStorage> list = listsOfItems.get(id);
                list.add(item);
                listsOfItems.put(id, list);
            }
        }
        else
        {
            final List<ItemStorage> list = new ArrayList<>();
            list.add(item);
            listsOfItems.put(id, list);
        }
    }

    /**
     * Check if an item is in the list of allowed items.
     * @param item the item to check.
     * @param id the id.
     * @return true if so.
     */
    public boolean isAllowedItem(final String id, final ItemStorage item)
    {
        return listsOfItems.containsKey(id) && listsOfItems.get(id).contains(item);
    }

    /**
     * Remove an item from the view and notify the server side.
     * @param id the id.
     * @param item the item to remove.
     */
    public void removeItem(final String id, final ItemStorage item)
    {
        Network.getNetwork().sendToServer(new AssignFilterableItemMessage(this, id, item, false));
        if(listsOfItems.containsKey(id) && listsOfItems.get(id).contains(item))
        {
            final List<ItemStorage> list = listsOfItems.get(id);
            list.remove(item);
            listsOfItems.put(id, list);
        }
    }

    @Override
    public void deserialize(@NotNull final PacketBuffer buf)
    {
        super.deserialize(buf);

        final int ids = buf.readInt();
        for(int i = 0; i < ids; i++)
        {
            final String id = buf.readString();
            final int size = buf.readInt();
            final List<ItemStorage> list = new ArrayList<>();
            for (int j = 0; j < size; j++)
            {
                list.add(new ItemStorage(buf.readItemStack()));
            }
            listsOfItems.put(id,list);
        }
    }
}
