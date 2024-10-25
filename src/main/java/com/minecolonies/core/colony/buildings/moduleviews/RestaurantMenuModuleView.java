package com.minecolonies.core.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.constant.translation.RequestSystemTranslationConstants;
import com.minecolonies.core.client.gui.modules.RestaurantMenuModuleWindow;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.core.colony.buildings.modules.RestaurantMenuModule.STOCK_PER_LEVEL;
import static com.minecolonies.core.colony.buildings.workerbuildings.BuildingCook.FOOD_EXCLUSION_LIST;

/**
 * Client side version of food menu.
 */
public class RestaurantMenuModuleView extends AbstractBuildingModuleView
{
    /**
     * The menu.
     */
    private final List<ItemStorage> menu = new ArrayList<>();

    @Override
    public void deserialize(final @NotNull FriendlyByteBuf buf)
    {
        menu.clear();
        final int size = buf.readInt();
        for (int i = 0; i < size; i++)
        {
            menu.add(new ItemStorage(buf.readItem()));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BOWindow getWindow()
    {
        return new RestaurantMenuModuleWindow(buildingView, this);
    }

    @Override
    public String getIcon()
    {
        return FOOD_EXCLUSION_LIST;
    }

    @Override
    public String getDesc()
    {
        return RequestSystemTranslationConstants.REQUESTS_TYPE_FOOD;
    }

    /**
     * Get the menu for the restaurant.
     * @return the menu.
     */
    public List<ItemStorage> getMenu()
    {
        return menu;
    }

    public boolean hasReachedLimit()
    {
        return menu.size() >= buildingView.getBuildingLevel() * STOCK_PER_LEVEL;
    }
}
