package com.minecolonies.core.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.modules.FoodItemListModuleWindow;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Set;
import java.util.function.Function;

/**
 * Client side version of the abstract class for all buildings which require a filterable list of allowed items.
 */
public class FoodItemListModuleView extends ItemListModuleView
{
    /**
     * Create a nw grouped item list view for the client side.
     * @param id the id.
     * @param desc desc lang string.
     * @param inverted enabling or disabling.
     * @param allItems a supplier for all the items.
     */
    public FoodItemListModuleView(final String id, final String desc, final boolean inverted, final Function<IBuildingView, Set<ItemStorage>> allItems)
    {
        super(id, desc, inverted, allItems);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BOWindow getWindow()
    {
        return new FoodItemListModuleWindow(Constants.MOD_ID + ":gui/foodlist.xml", buildingView, this);
    }

    @Override
    public String getIcon()
    {
        return this.getId();
    }
}
