package com.minecolonies.api.colony.buildings.modules.settings;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.buildings.views.IBuildingView;

/**
 * Generic ISetting that represents all possible setting objects (string, numbers, boolean, etc).
 */
public interface ISetting
{
    /**
     * Add the handling of the specific setting to the box in the UI.
     * @param key the key of the setting.
     * @param rowPane the pane of it.
     * @param settingsModuleView the module view that holds the setting.
     * @param building the building.
     * @param window the calling window.
     */
    void addHandlersToBox(
      final ISettingKey<?> key,
      final Pane rowPane,
      final ISettingsModuleView settingsModuleView,
      final IBuildingView building, final Window window);

    /**
     * Trigger a setting.
     */
    void trigger();
}
