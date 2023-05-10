package com.minecolonies.api.colony.buildings.modules.settings;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.ISettingsModule;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import net.minecraft.server.level.ServerPlayer;

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
    void setupHandler(
      final ISettingKey<?> key,
      final Pane rowPane,
      final ISettingsModuleView settingsModuleView,
      final IBuildingView building, final BOWindow window);

    /**
     * Update the handling (e.g update settings text).
     * @param key the key of the setting.
     * @param rowPane the pane of it.
     * @param settingsModuleView the module view that holds the setting.
     * @param building the building.
     * @param window the calling window.
     */
    void render(
      final ISettingKey<?> key,
      final Pane rowPane,
      final ISettingsModuleView settingsModuleView,
      final IBuildingView building, final BOWindow window);

    /**
     * Trigger a setting.
     */
    void trigger();

    /**
     * Check if this setting is effective (provided {@link ISettingsModule#getOptionalSetting(ISettingKey)} is used).
     * @return true by default
     */
    default boolean isActive(ISettingsModule module)
    {
        return true;
    }

    /**
     * Check if this setting is visible in the GUI.
     * @return true by default.
     */
    default boolean isActive(ISettingsModuleView module)
    {
        return true;
    }

    /**
     * Called when updated.
     * @param building the building its updated for.
     * @param sender the player triggering the update.
     */
    default void onUpdate(IBuilding building, final ServerPlayer sender) { };

    /**
     * Allow updating a setting with new data.
     * @param iSetting the setting with new data
     */
    default void updateSetting(final ISetting iSetting)
    {
        
    }

    /**
     * Copy value from another instance.
     * @param iSetting the setting to copy from
     */
    void copyValue(final ISetting iSetting);
}
