package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingComposter;
import net.minecraft.item.ItemStack;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Composter window class. Specifies the extras the composter has for its list.
 */
public class WindowHutComposter extends WindowFilterableList<BuildingComposter.View>
{
    /**
     * Constructor for the window of the worker building.
     *
     * @param building class extending
     */
    public WindowHutComposter(final BuildingComposter.View building)
    {
        super(building, stack -> true, LanguageHandler.format("com.minecolonies.gui.workerHuts.composter.compostables"));
    }

    @Override
    public Collection<? extends ItemStorage> getBlockList(final Predicate<ItemStack> filterPredicate)
    {
        return ColonyManager.getCompatibilityManager().getCopyOfCompostableItems().stream().filter(storage -> filterPredicate.test(storage.getItemStack())).collect(Collectors.toList());
    }

    @Override
    public String getBuildingName()
    {
        return "Composter Hut";
    }
}
