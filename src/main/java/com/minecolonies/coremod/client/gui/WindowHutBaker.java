package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.views.View;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBaker;
import com.minecolonies.coremod.entity.ai.citizen.baker.BakerRecipes;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_BAKER;

/**
 * Baker window class. Specifies the extras the bakery has for its list.
 */
public class WindowHutBaker extends AbstractHutFilterableLists
{
    /**
     * The resource string.
     */
    private static final String RESOURCE_STRING = ":gui/windowhutbaker.xml";

    /**
     * View containing the list.
     */
    private static final String PAGE_ITEMS_VIEW = "recipes";

    /**
     * Constructor for the window of the bakery.
     *
     * @param building {@link BuildingBaker.View}.
     */
    public WindowHutBaker(final BuildingBaker.View building)
    {
        super(building, Constants.MOD_ID + RESOURCE_STRING);
        final ViewFilterableList window = new ViewFilterableList(findPaneOfTypeByID(PAGE_ITEMS_VIEW, View.class),
          this,
          building,

          LanguageHandler.format("com.minecolonies.coremod.gui.workerhuts.bakery.recipes"),
          PAGE_ITEMS_VIEW,
          false);
        views.put(PAGE_ITEMS_VIEW, window);
    }

    @Override
    public List<? extends ItemStorage> getBlockList(final Predicate<ItemStack> filterPredicate, final String id)
    {
        final List<ItemStorage> list = new ArrayList<>();

        for (final IRecipeStorage recipe : BakerRecipes.getRecipes())
        {
            if (filterPredicate.test(recipe.getPrimaryOutput()))
            {
                list.add(new ItemStorage(recipe.getPrimaryOutput()));
            }
        }

        for (final IRecipeStorage recipe : building.getRecipes())
        {
            if (filterPredicate.test(recipe.getPrimaryOutput()))
            {
                list.add(new ItemStorage(recipe.getPrimaryOutput()));
            }
        }

        return list;
    }

    /**
     * Returns the name of a building.
     *
     * @return Name of a building.
     */
    @NotNull
    @Override
    public String getBuildingName()
    {
        return COM_MINECOLONIES_COREMOD_GUI_BAKER;
    }
}

