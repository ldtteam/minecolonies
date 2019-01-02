package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingComposter;
import net.minecraft.item.ItemStack;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

    /**
     * Get the list of blocks which should be added.
     *
     * @param filterPredicate the predicate to filter all blocks for.
     * @return an immutable list of blocks.
     */
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
