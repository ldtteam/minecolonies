package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.views.View;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBaker;
import com.minecolonies.coremod.entity.ai.citizen.baker.BakerRecipes;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_BAKER;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Baker window class. Specifies the extras the baker has for its list.
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
     * Constructor for the window of the baker.
     *
     * @param building {@link BuildingBaker.View}.
     */
    public WindowHutBaker(final BuildingBaker.View building)
    {
        super(building, Constants.MOD_ID + RESOURCE_STRING);
        final ViewFilterableList window = new ViewFilterableList(findPaneOfTypeByID(PAGE_ITEMS_VIEW, View.class),
          this,
          building,

          LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.baker.recipes"),
          PAGE_ITEMS_VIEW,
          false);
        views.put(PAGE_ITEMS_VIEW, window);
    }

    @Override
    public List<? extends ItemStorage> getBlockList(final Predicate<ItemStack> filterPredicate, final String id)
    {
        return BakerRecipes.getRecipes().stream().map(recipe -> new ItemStorage(recipe.getPrimaryOutput())).filter(storage -> filterPredicate.test(storage.getItemStack())).collect(Collectors.toList());
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

