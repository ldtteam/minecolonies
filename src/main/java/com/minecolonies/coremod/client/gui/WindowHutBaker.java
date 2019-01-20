package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBaker;
import com.minecolonies.coremod.entity.ai.citizen.baker.BakerRecipes;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_BAKER;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Baker window class. Specifies the extras the baker has for its list.
 */
public class WindowHutBaker extends WindowFilterableList<BuildingBaker.View>
{
    /**
     * Constructor for the window of the baker.
     *
     * @param building {@link BuildingBaker.View}.
     */
    public WindowHutBaker(final BuildingBaker.View building)
    {
        super(building, stack -> true, LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.baker.recipes"));
    }

    @Override
    public Collection<? extends ItemStorage> getBlockList(final Predicate<ItemStack> filterPredicate)
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

