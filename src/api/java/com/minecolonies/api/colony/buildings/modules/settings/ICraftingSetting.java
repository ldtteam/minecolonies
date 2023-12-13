package com.minecolonies.api.colony.buildings.modules.settings;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Crafting Setting.
 */
public interface ICraftingSetting extends ISetting<IToken<?>>
{
    /**
     * Get the setting value.
     * @return the current value.
     */
    IRecipeStorage getValue(final IBuilding building);

    /**
     * Get the setting value.
     * @return the current value.
     */
    IRecipeStorage getValue(final IBuildingView building);

    /**
     * Get the list of all settings.
     * @param building server side building.
     * @return the list.
     */
    List<ItemStack> getSettings(final IBuilding building);

    /**
     * Get the list of all settings.
     * @param buildingView client side building.
     * @return a copy of the list.
     */
    List<ItemStack> getSettings(final IBuildingView buildingView);

    /**
     * Set the setting to a specific index.
     * @param value the value to set.
     */
    void set(final IRecipeStorage value);
}
