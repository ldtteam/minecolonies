package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.views.View;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingFlorist;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Florist window class. Specifies the extras the florist has for its list.
 */
public class WindowHutFlorist extends AbstractHutFilterableLists
{
    /**
     * View containing the list.
     */
    private static final String PAGE_ITEMS_VIEW = "flowers";

    /**
     * The resource string.
     */
    private static final String RESOURCE_STRING = ":gui/windowhutflorist.xml";

    /**
     * The building of the florist (Client side representation).
     */
    private final BuildingFlorist.View ownBuilding;

    /**
     * Constructor for the window of the worker building.
     *
     * @param building class extending
     */
    public WindowHutFlorist(final BuildingFlorist.View building)
    {
        super(building, Constants.MOD_ID + RESOURCE_STRING);

        final ViewFilterableList window = new ViewFilterableList(findPaneOfTypeByID(PAGE_ITEMS_VIEW, View.class),
          this,
          building,
          LanguageHandler.format("com.minecolonies.gui.workerHuts.florist.flowers"),
          PAGE_ITEMS_VIEW,
          false);
        views.put(PAGE_ITEMS_VIEW, window);
        this.ownBuilding = building;
    }

    @Override
    public List<? extends ItemStorage> getBlockList(final Predicate<ItemStack> filterPredicate, final String id)
    {
        return IColonyManager.getInstance().getCompatibilityManager().getCopyOfPlantables().stream().filter(storage -> filterPredicate.test(storage.getItemStack())).collect(Collectors.toList());
    }

    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerHuts.florist";
    }
}
