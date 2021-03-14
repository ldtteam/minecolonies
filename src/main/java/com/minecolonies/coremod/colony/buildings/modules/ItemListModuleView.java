package com.minecolonies.coremod.colony.buildings.modules;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.buildings.modules.*;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.ItemListModuleWindow;
import com.minecolonies.coremod.network.messages.server.colony.building.AssignFilterableItemMessage;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Client side version of the abstract class for all buildings which require a filterable list of allowed items.
 */
public class ItemListModuleView extends AbstractBuildingModuleView implements IBuildingModuleView
{
    /**
     * The list of items.
     */
    private final List<ItemStorage> listsOfItems = new ArrayList<>();

    /**
     * Unique string id of the module.
     */
    private final String id;

    /**
     * Supplier for the list of all items (not only the disabled/enabled ones).
     */
    private final Function<IBuildingView, Set<ItemStorage>> allItems;

    /**
     * if the list is inverted (so list encludes the disabled ones).
     */
    private final boolean inverted;

    /**
     * Lang string for description.
     */
    private final String desc;

    /**
     * Create a nw grouped item list view for the client side.
     * @param id the id.
     * @param desc desc lang string.
     * @param inverted enabling or disabling.
     * @param allItems a supplier for all the items.
     */
    public ItemListModuleView(final String id, final String desc, final boolean inverted, final Function<IBuildingView, Set<ItemStorage>> allItems)
    {
        super();
        this.id = id;
        this.desc = desc;
        this.inverted = inverted;
        this.allItems = allItems;
    }

    /**
     * Add item to the view and notify the server side.
     *
     * @param item the item to add.
     */
    public void addItem(final ItemStorage item)
    {
        Network.getNetwork().sendToServer(new AssignFilterableItemMessage(this.buildingView, id, item, true));
        listsOfItems.add(item);
    }

    /**
     * Check if an item is in the list of allowed items.
     *
     * @param item the item to check.
     * @return true if so.
     */
    public boolean isAllowedItem(final ItemStorage item)
    {
        return listsOfItems.contains(item);
    }

    /**
     * Get the size of allowed items.
     *
     * @return the size.
     */
    public int getSize()
    {
        return listsOfItems.size();
    }

    /**
     * Remove an item from the view and notify the server side.
     *
     * @param item the item to remove.
     */
    public void removeItem(final ItemStorage item)
    {
        Network.getNetwork().sendToServer(new AssignFilterableItemMessage(this.buildingView, id, item, false));
        listsOfItems.remove(item);
    }

    /**
     * Get the unique id of this group (used to sync with server side).
     * @return the id.
     */
    public String getId()
    {
        return id;
    }

    /**
     * Get the supplier of the list of all items to display.
     * @return the list.
     */
    public Function<IBuildingView, Set<ItemStorage>> getAllItems()
    {
        return allItems;
    }

    /**
     * Check if the list is enabling or disabling.
     * @return true if enabling.
     */
    public boolean isInverted()
    {
        return inverted;
    }

    @Override
    public String getDesc()
    {
        return desc;
    }

    @Override
    public void deserialize(@NotNull final PacketBuffer buf)
    {
        listsOfItems.clear();
        final int size = buf.readInt();

        for (int j = 0; j < size; j++)
        {
            listsOfItems.add(new ItemStorage(buf.readItemStack()));
        }
    }

    @Override
    public Window getWindow()
    {
        return new ItemListModuleWindow(Constants.MOD_ID + ":gui/layouthuts/layoutfilterablelist.xml", buildingView, this);
    }

    @Override
    public String getIcon()
    {
        return this.getId();
    }
}
