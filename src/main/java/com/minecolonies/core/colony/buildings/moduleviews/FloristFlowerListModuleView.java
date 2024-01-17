package com.minecolonies.core.colony.buildings.moduleviews;

import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingFlorist;
import net.minecraft.client.Minecraft;

import static com.minecolonies.api.util.constant.BuildingConstants.BUILDING_FLOWER_LIST;
import static com.minecolonies.api.util.constant.Constants.MAX_BUILDING_LEVEL;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Specific florist item list module view.
 */
public class FloristFlowerListModuleView extends ItemListModuleView
{
    /**
     * The max level the building doesn't let filtering yet.
     */
    private static final int MAX_LEVEL_BEFORE_SORTING = 3;

    /**
     * Create a new florist specific list module view.
     */
    public FloristFlowerListModuleView()
    {
        super(BUILDING_FLOWER_LIST, FLORIST_FLOWER_DESC, true, (buildingView) -> BuildingFlorist.getPlantablesForBuildingLevel(buildingView.getBuildingLevel()));
    }

    @Override
    public void removeItem(final ItemStorage item)
    {
        if (buildingView.getBuildingLevel() <= 1)
        {
            MessageUtils.format(TOO_LOW_LEVEL_TO_FILTER_FLORIST).sendTo(Minecraft.getInstance().player);
            return;
        }

        super.removeItem(item);
    }

    @Override
    public void addItem(final ItemStorage item)
    {
        if (buildingView.getBuildingLevel() <= 1)
        {
            MessageUtils.format(TOO_LOW_LEVEL_TO_FILTER_FLORIST).sendTo(Minecraft.getInstance().player);
            return;
        }

        final int size = getSize();
        if (buildingView.getBuildingLevel() <= MAX_LEVEL_BEFORE_SORTING && size >= 1)
        {
            MessageUtils.format(TOO_MANY_FILTERED_BELOW_LVL4_FLORIST).sendTo(Minecraft.getInstance().player);
            return;
        }

        if (buildingView.getBuildingLevel() < MAX_BUILDING_LEVEL && size >= MAX_BUILDING_LEVEL)
        {
            MessageUtils.format(TOO_MANY_FILTERED_FLORIST).sendTo(Minecraft.getInstance().player);
            return;
        }

        super.addItem(item);
    }
}
