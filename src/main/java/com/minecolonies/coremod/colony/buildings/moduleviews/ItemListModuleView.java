package com.minecolonies.coremod.colony.buildings.moduleviews;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.buildings.modules.*;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.modules.ItemListModuleWindow;
import com.minecolonies.coremod.network.messages.server.colony.building.AssignFilterableItemMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.ResetFilterableItemMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Client side version of the abstract class for all buildings which require a filterable list of allowed items.
 */
public class ItemListModuleView extends AbstractBuildingModuleView implements IItemListModuleView
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

    @Override
    public void addItem(final ItemStorage item)
    {
        Network.getNetwork().sendToServer(new AssignFilterableItemMessage(this.buildingView, id, item, true));
        listsOfItems.add(item);
    }

    @Override
    public boolean isAllowedItem(final ItemStorage item)
    {
        return listsOfItems.contains(item);
    }

    @Override
    public int getSize()
    {
        return listsOfItems.size();
    }

    @Override
    public void removeItem(final ItemStorage item)
    {
        Network.getNetwork().sendToServer(new AssignFilterableItemMessage(this.buildingView, id, item, false));
        listsOfItems.remove(item);
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public Function<IBuildingView, Set<ItemStorage>> getAllItems()
    {
        return allItems;
    }

    @Override
    public boolean isInverted()
    {
        return inverted;
    }

    @Override
    public void clearItems()
    {
        Network.getNetwork().sendToServer(new ResetFilterableItemMessage(this.buildingView, id));
        listsOfItems.clear();
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
            listsOfItems.add(new ItemStorage(buf.readItem()));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
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
