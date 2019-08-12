package com.minecolonies.coremod.client.gui;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.crafting.ItemStorage;
import com.ldtteam.structurize.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.ldtteam.blockout.views.View;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingSmeltery;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_REQUESTS_BURNABLE;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_REQUESTS_SMELTABLE_ORE;

/**
 * Smelter window class. Specifies the extras the smelter has for its list.
 */
public class WindowHutSmelter extends AbstractHutFilterableLists
{
    /**
     * View containing the list.
     */
    private static final String PAGE_ITEMS_VIEW = "fuel";

    /**
     * View containing the list.
     */
    private static final String PAGE_ITEMS_VIEW2 = "ores";

    /**
     * The resource string.
     */
    private static final String RESOURCE_STRING = ":gui/windowhutsmelter.xml";

    /**
     * Constructor for the window of the worker building.
     *
     * @param building class extending
     */
    public WindowHutSmelter(final BuildingSmeltery.View building)
    {
        super(building, Constants.MOD_ID + RESOURCE_STRING);
        final ViewFilterableList window = new ViewFilterableList(findPaneOfTypeByID(PAGE_ITEMS_VIEW, View.class),
          this,
          building,
          LanguageHandler.format(COM_MINECOLONIES_REQUESTS_BURNABLE),
          PAGE_ITEMS_VIEW,
          false);
        views.put(PAGE_ITEMS_VIEW, window);

        final ViewFilterableList window2 = new ViewFilterableList(findPaneOfTypeByID(PAGE_ITEMS_VIEW2, View.class),
          this,
          building,
          LanguageHandler.format(COM_MINECOLONIES_REQUESTS_SMELTABLE_ORE),
          PAGE_ITEMS_VIEW2,
          true);
        views.put(PAGE_ITEMS_VIEW2, window2);
    }

    /**
     * The classic block list.
     * @param filterPredicate the predicate filter.
     * @param id the id of the specific predicate.
     * @return the list of itemStorages.
     */
    public List<? extends ItemStorage> getBlockList(final Predicate<ItemStack> filterPredicate, final String id)
    {
        if (id.equals(PAGE_ITEMS_VIEW))
        {
            return ImmutableList.copyOf(IColonyManager.getInstance().getCompatibilityManager().getFuel().stream().filter(item -> filterPredicate.test(item.getItemStack())).collect(Collectors.toList()));
        }
        else if (id.equals(PAGE_ITEMS_VIEW2))
        {
            return ImmutableList.copyOf(IColonyManager.getInstance().getCompatibilityManager().getSmeltableOres().stream().filter(item -> filterPredicate.test(item.getItemStack())).collect(Collectors.toList()));

        }
        else
        {
            return Collections.emptyList();
        }
    }

    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerHuts.smelter";
    }
}
