package com.minecolonies.coremod.colony.buildings.views;

import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.IColonyView;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.IBuildingWorker;
import com.minecolonies.coremod.network.messages.AssignFilterableItemMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Client side representation of the filterable list window GUI.
 */
public abstract class FilterableListView extends AbstractBuildingWorker.View
{
    /**
     * The list of items.
     */
    private final List<ItemStorage> listOfItems = new ArrayList<>();

    /**
     * Creates the view representation of the building.
     *
     * @param c the colony.
     * @param l the location.
     */
    public FilterableListView(final IColonyView c, @NotNull final BlockPos l)
    {
        super(c, l);
    }

    /**
     * Add item to the view and notify the server side.
     * @param item the item to add.
     */
    public void addItem(final ItemStorage item)
    {
        MineColonies.getNetwork().sendToServer(new AssignFilterableItemMessage(this, item, true));
        if(!listOfItems.contains(item))
        {
            listOfItems.add(item);
        }
    }

    /**
     * Check if an item is in the list of allowed items.
     * @param item the item to check.
     * @return true if so.
     */
    public boolean isAllowedItem(final ItemStorage item)
    {
        return listOfItems.contains(item);
    }

    /**
     * Remove an item from the view and notify the server side.
     * @param item the item to remove.
     */
    public void removeItem(final ItemStorage item)
    {
        MineColonies.getNetwork().sendToServer(new AssignFilterableItemMessage(this, item, false));
        listOfItems.remove(item);
    }

    @Override
    public void deserialize(@NotNull final ByteBuf buf)
    {
        super.deserialize(buf);

        final int size = buf.readInt();
        for(int i = 0; i < size; i++)
        {
            listOfItems.add(new ItemStorage(ByteBufUtils.readItemStack(buf)));
        }
    }
}
