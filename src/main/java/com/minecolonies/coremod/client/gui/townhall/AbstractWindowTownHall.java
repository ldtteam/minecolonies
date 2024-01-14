package com.minecolonies.coremod.client.gui.townhall;

import com.ldtteam.blockui.Color;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.controls.Image;
import com.minecolonies.api.colony.buildings.workerbuildings.ITownHallView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.gui.AbstractWindowModuleBuilding;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingTownHall;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * BOWindow for the town hall.
 */
public abstract class AbstractWindowTownHall extends AbstractWindowModuleBuilding<ITownHallView>
{
    /**
     * Color constants for builder list.
     */
    public static final int RED       = Color.getByName("red", 0);
    public static final int DARKGREEN = Color.getByName("darkgreen", 0);
    public static final int ORANGE    = Color.getByName("orange", 0);
    public static final int YELLOW = Color.getByName("yellow", 0);

    /**
     * Constructor for the town hall window.
     *
     * @param townHall {@link BuildingTownHall.View}.
     */
    public AbstractWindowTownHall(final BuildingTownHall.View townHall, final String page)
    {
        super(townHall, Constants.MOD_ID + ":gui/townhall/" + page);

        registerButton(BUTTON_ACTIONS, () -> new WindowMainPage(townHall).open());
        registerButton(BUTTON_INFOPAGE, () -> new WindowInfoPage(townHall).open());
        registerButton(BUTTON_PERMISSIONS, () -> new WindowPermissionsPage(townHall).open());
        registerButton(BUTTON_CITIZENS, () -> new WindowCitizenPage(townHall).open());
        registerButton(BUTTON_STATS, () -> new WindowStatsPage(townHall).open());
        registerButton(BUTTON_SETTINGS, () -> new WindowSettings(townHall).open());
        registerButton(BUTTON_ALLIANCE, () -> new WindowAlliancePage(townHall).open());

        findPaneOfTypeByID(getWindowId() + "0", Image.class).hide();
        findPaneOfTypeByID(getWindowId(), ButtonImage.class).hide();

        findPaneOfTypeByID(getWindowId() + "1", ButtonImage.class).show();
    }

    /**
     * Get the id that identifies the window.
     * @return the string id.
     */
    protected abstract String getWindowId();

    /**
     * Returns the name of a building.
     *
     * @return Name of a building.
     */
    @Override
    public String getBuildingName()
    {
        return building.getColony().getName();
    }
}
