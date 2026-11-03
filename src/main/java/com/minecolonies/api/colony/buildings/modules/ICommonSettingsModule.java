package com.minecolonies.api.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.modules.settings.ISetting;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Common (sideless) Settings module interface.
 */
public interface ICommonSettingsModule
{
    /**
     * Get a specific setting.
     * @param key the key of the setting.
     * @param <T> the type of setting.
     * @return the setting.
     */
    @Nullable
    <T extends ISetting<?>> T getSetting(final ISettingKey<T> key);

    /**
     * Trigger a setting of a specific key.
     * @param key the settings key.
     */
    default void trigger(final ISettingKey<?> key)
    {
        // Noop on server side.
    }

    /**
     * Update a given settings value.
     * @param settingKey the given key.
     * @param value the value.
     * @param sender the player that updated the setting.
     */
    void updateSetting(ISettingKey<?> settingKey, ISetting<?> value, final ServerPlayer sender);
}
