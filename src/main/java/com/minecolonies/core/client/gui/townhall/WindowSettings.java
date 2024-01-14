package com.minecolonies.core.client.gui.townhall;

import com.minecolonies.core.colony.buildings.modules.BuildingModules;
import com.minecolonies.core.colony.buildings.moduleviews.SettingsModuleView;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingTownHall;

import static com.minecolonies.api.util.constant.WindowConstants.BUTTON_SETTINGS;

/**
 * BOWindow for the town hall.
 */
public class WindowSettings extends AbstractWindowTownHall
{
    /**
     * Module
     */
    private final SettingsModuleView moduleView;

    /**
     * Constructor for the town hall window.
     *
     * @param townHall {@link BuildingTownHall.View}.
     */
    public WindowSettings(final BuildingTownHall.View townHall)
    {
        super(townHall, "layoutsettings.xml");

        this.moduleView = building.getModuleView(BuildingModules.TOWNHALL_SETTINGS);

        moduleView.getSetting(BuildingTownHall.AUTO_HIRING_MODE)
          .setupHandler(BuildingTownHall.AUTO_HIRING_MODE, window.findPaneByID("job"), moduleView, moduleView.getBuildingView(), WindowSettings.this);
        moduleView.getSetting(BuildingTownHall.MOVE_IN)
          .setupHandler(BuildingTownHall.MOVE_IN, window.findPaneByID("movein"), moduleView, moduleView.getBuildingView(), WindowSettings.this);
        moduleView.getSetting(BuildingTownHall.AUTO_HOUSING_MODE)
          .setupHandler(BuildingTownHall.AUTO_HOUSING_MODE, window.findPaneByID("housing"), moduleView, moduleView.getBuildingView(), WindowSettings.this);
        moduleView.getSetting(BuildingTownHall.ENTER_LEAVE_MESSAGES)
          .setupHandler(BuildingTownHall.ENTER_LEAVE_MESSAGES, window.findPaneByID("entermessages"), moduleView, moduleView.getBuildingView(), WindowSettings.this);
        moduleView.getSetting(BuildingTownHall.CONSTRUCTION_TAPE)
          .setupHandler(BuildingTownHall.CONSTRUCTION_TAPE, window.findPaneByID("tape"), moduleView, moduleView.getBuildingView(), WindowSettings.this);
    }

    /**
     * Executed when <code>WindowTownHall</code> is opened. Does tasks like setting buttons.
     */
    @Override
    public void onOpened()
    {
        super.onOpened();
    }

    @Override
    public void onUpdate()
    {
        moduleView.getSetting(BuildingTownHall.AUTO_HIRING_MODE)
          .render(BuildingTownHall.AUTO_HIRING_MODE, window.findPaneByID("job"), moduleView, moduleView.getBuildingView(), WindowSettings.this);
        moduleView.getSetting(BuildingTownHall.MOVE_IN)
          .render(BuildingTownHall.MOVE_IN, window.findPaneByID("movein"), moduleView, moduleView.getBuildingView(), WindowSettings.this);
        moduleView.getSetting(BuildingTownHall.AUTO_HOUSING_MODE)
          .render(BuildingTownHall.AUTO_HOUSING_MODE, window.findPaneByID("housing"), moduleView, moduleView.getBuildingView(), WindowSettings.this);
        moduleView.getSetting(BuildingTownHall.ENTER_LEAVE_MESSAGES)
          .render(BuildingTownHall.ENTER_LEAVE_MESSAGES, window.findPaneByID("entermessages"), moduleView, moduleView.getBuildingView(), WindowSettings.this);
        moduleView.getSetting(BuildingTownHall.CONSTRUCTION_TAPE)
          .render(BuildingTownHall.CONSTRUCTION_TAPE, window.findPaneByID("tape"), moduleView, moduleView.getBuildingView(), WindowSettings.this);
    }

    @Override
    protected String getWindowId()
    {
        return BUTTON_SETTINGS;
    }
}
