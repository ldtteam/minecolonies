package com.minecolonies.coremod.client.gui;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.crafting.ItemStorage;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.ldtteam.blockout.views.View;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingStoneSmeltery;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_REQUESTS_BURNABLE;

/**
 * Stone smelter window class. Specifies the extras the stone smelter has for its list.
 */
public class WindowHutStoneSmelter extends AbstractHutFilterableLists
{
    /**
     * View containing the list.
     */
    private static final String PAGE_ITEMS_VIEW = "fuel";

    /**
     * The resource string.
     */
    private static final String RESOURCE_STRING = ":gui/windowhutstonesmelter.xml";

    /**
     * Constructor for the window of the worker building.
     *
     * @param building class extending
     */
    public WindowHutStoneSmelter(final BuildingStoneSmeltery.View building)
    {
        super(building, Constants.MOD_ID + RESOURCE_STRING);
        final ViewFilterableList window = new ViewFilterableList(findPaneOfTypeByID(PAGE_ITEMS_VIEW, View.class),
          this,
          building,
          LanguageHandler.format(COM_MINECOLONIES_REQUESTS_BURNABLE),
          PAGE_ITEMS_VIEW,
          false);
        views.put(PAGE_ITEMS_VIEW, window);
    }

    /**
     * The classic block list.
     * @param filterPredicate the predicate filter.
     * @param id the id of the specific predicate.
     * @return the list of itemStorages.
     */
    public List<? extends ItemStorage> getBlockList(final Predicate<ItemStack> filterPredicate, final String id)
    {
        return ImmutableList.copyOf(ColonyManager.getCompatibilityManager().getFuel().stream().filter(item -> filterPredicate.test(item.getItemStack())).collect(Collectors.toList()));
    }

    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerHuts.stonesmelter";
    }
}
