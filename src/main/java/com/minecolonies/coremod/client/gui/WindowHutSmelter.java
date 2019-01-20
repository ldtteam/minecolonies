package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingSmeltery;
import net.minecraft.tileentity.TileEntityFurnace;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_REQUESTS_BURNABLE;

/**
 * Smelter window class. Specifies the extras the smelter has for its list.
 */
public class WindowHutSmelter extends WindowFilterableList<BuildingSmeltery.View>
{
    /**
     * Constructor for the window of the worker building.
     *
     * @param building class extending
     */
    public WindowHutSmelter(final BuildingSmeltery.View building)
    {
        super(building, TileEntityFurnace::isItemFuel, LanguageHandler.format(COM_MINECOLONIES_REQUESTS_BURNABLE));
    }

    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerHuts.smelter";
    }
}
