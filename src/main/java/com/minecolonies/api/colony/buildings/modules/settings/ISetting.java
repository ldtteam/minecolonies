package com.minecolonies.api.colony.buildings.modules.settings;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.ISettingsModule;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Generic ISetting that represents all possible setting objects (string, numbers, boolean, etc).
 */
public interface ISetting<S>
{
    /**
     * Get the resource location of the view you want to use for this setting.
     *
     * @return the resource location indicating which view to use.
     */
    ResourceLocation getLayoutItem();

    /**
     * Add the handling of the specific setting to the box in the UI.
     *
     * @param key                the key of the setting.
     * @param rowPane            the pane of it.
     * @param settingsModuleView the module view that holds the setting.
     * @param building           the building.
     * @param window             the calling window.
     */
    void setupHandler(
      final ISettingKey<?> key,
      final Pane rowPane,
      final ISettingsModuleView settingsModuleView,
      final IBuildingView building,
      final BOWindow window);

    /**
     * Update the handling (e.g update settings text).
     *
     * @param key                the key of the setting.
     * @param rowPane            the pane of it.
     * @param settingsModuleView the module view that holds the setting.
     * @param building           the building.
     * @param window             the calling window.
     */
    void render(
      final ISettingKey<?> key,
      final Pane rowPane,
      final ISettingsModuleView settingsModuleView,
      final IBuildingView building,
      final BOWindow window);

    /**
     * Trigger a setting.
     */
    default void trigger() {}

    /**
     * Check if this setting is active (provided {@link ISettingsModule#getOptionalSetting(ISettingKey)} is used).
     *
     * @return true if the setting is active;
     */
    default boolean isActive(final ISettingsModule module)
    {
        return true;
    }

    /**
     * Configures if this setting is inactive, whether it should show in the settings at all.
     * This would default to false in most cases as we want to prevent not showing settings, but certain settings related to
     * addon mods may want to be hidden if another mod is not loaded.
     *
     * @return true if the inactive setting should be hidden.
     */
    default boolean shouldHideWhenInactive()
    {
        return false;
    }

    /**
     * Called when updated.
     *
     * @param building the building its updated for.
     * @param sender   the player triggering the update.
     */
    default void onUpdate(final IBuilding building, final ServerPlayer sender) {}

    /**
     * Allow updating a setting with new data.
     *
     * @param setting the setting with new data
     */
    default void updateSetting(final ISetting<S> setting) {}

    /**
     * Copy value from another instance.
     *
     * @param setting the setting to copy from
     */
    void copyValue(final ISetting<S> setting);

    /**
     * Generates the hover pane for inactive settings.
     *
     * @param component          the component to put the hover pane on.
     * @param settingsModuleView the module view that holds the setting.
     */
    default void setInActiveHoverPane(final Pane component, final ISettingsModuleView settingsModuleView)
    {
        final Component inActiveReason = getInactiveReason();

        final boolean hasTooltip = !tooltip.getString().equals(tooltipKey);
        final boolean isActive = isActive(settingsModuleView);

        if (isActive && hasTooltip)
        {
            PaneBuilders.tooltipBuilder()
              .append(inActiveReason)
              .hoverPane(component)
              .build();
        }
        else if (!isActive && (hasTooltip || inActiveReason != null))
        {
            component.setHoverPane(null);
        }
        else
        {
            component.setHoverPane(null);
        }
    }

    /**
     * Get the reason why this setting is inactive.
     *
     * @return a component stating why this is inactive, or null if no reason.
     */
    @Nullable
    default Component getInactiveReason()
    {
        return null;
    }

    /**
     * Check if this setting is active (provided {@link ISettingsModule#getOptionalSetting(ISettingKey)} is used).
     *
     * @return a component containing a message why this setting is not active, return null if the setting is supposed to be active.
     */
    default boolean isActive(final ISettingsModuleView module)
    {
        return true;
    }

    /**
     * Get the setting value.
     * @return the value.
     */
    S getValue();
}
