package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingStoneSmeltery;
import net.minecraft.tileentity.TileEntityFurnace;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_REQUESTS_BURNABLE;

/**
 * Stone smelter window class. Specifies the extras the stone smelter has for its list.
 */
public class WindowHutStoneSmelter extends WindowFilterableList<BuildingStoneSmeltery.View>
{
    /**
     * Constructor for the window of the worker building.
     *
     * @param building class extending
     */
    public WindowHutStoneSmelter(final BuildingStoneSmeltery.View building)
    {
        super(building, TileEntityFurnace::isItemFuel, LanguageHandler.format(COM_MINECOLONIES_REQUESTS_BURNABLE));
    }

    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerHuts.stonesmelter";
    }
}
