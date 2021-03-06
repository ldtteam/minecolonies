package com.minecolonies.coremod.colony.buildings.modules.itemlist.ore;

import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.coremod.colony.buildings.modules.itemlist.ItemListModuleView;

import java.util.Set;
import java.util.function.Function;

public class OreItemListModuleView extends ItemListModuleView
{
    /**
     * Create a nw grouped item list view for the client side.
     *
     * @param desc     desc lang string.
     * @param inverted enabling or disabling.
     * @param allItems a supplier for all the items.
     */
    public OreItemListModuleView(
      final String desc,
      final boolean inverted,
      final Function<IBuildingView, Set<ItemStorage>> allItems)
    {
        super(desc, inverted, allItems);
    }
}
