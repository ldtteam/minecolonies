package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.views.View;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingEnchanter;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingFlorist;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Enchanter window class.
 */
public class WindowHutEnchanter extends AbstractHutFilterableLists
{
    /**
     * View containing the list.
     */
    private static final String PAGE_ITEMS_VIEW = "enchantments";

    /**
     * The resource string.
     */
    private static final String RESOURCE_STRING       = ":gui/windowhutenchanter.xml";

    /**
     * The building of the florist (Client side representation).
     */
    private final BuildingEnchanter.View ownBuilding;

    /**
     * Constructor for the window of the worker building.
     *
     * @param building class extending
     */
    public WindowHutEnchanter(final BuildingEnchanter.View building)
    {
        super(building, Constants.MOD_ID + RESOURCE_STRING);

        final ViewFilterableList window = new ViewFilterableList(findPaneOfTypeByID(PAGE_ITEMS_VIEW, View.class),
          this,
          building,
          LanguageHandler.format(FLORIST_FLOWER_DESC),
          PAGE_ITEMS_VIEW,
          false);
        views.put(PAGE_ITEMS_VIEW, window);
        this.ownBuilding = building;
    }

    @Override
    public List<? extends ItemStorage> getBlockList(final Predicate<ItemStack> filterPredicate, final String id)
    {
        return BuildingFlorist.getPlantablesForBuildingLevel(building.getBuildingLevel());
    }

    @Override
    public String getBuildingName()
    {
        return FLORIST_BUILDING_NAME;
    }
}
