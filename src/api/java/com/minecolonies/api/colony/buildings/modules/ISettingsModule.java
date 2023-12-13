package com.minecolonies.api.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.modules.settings.ISetting;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Settings module interface.
 */
public interface ISettingsModule extends IBuildingModule
{
    /**
     * Register a new setting.
     * @param key the key of the setting.
     * @param setting the setting.
     * @return the instance of the module.
     */
    ISettingsModule with(final ISettingKey<?> key, final ISetting<?> setting);

    /**
     * Get a specific setting.
     * @param key the key of the setting.
     * @param <T> the type of setting.
     * @return the setting.
     */
    <T extends ISetting<?>> T getSetting(final ISettingKey<T> key);

    /**
     * Get a specific setting.
     * @param key the key of the setting.
     * @param <T> the type of setting.
     * @return the setting, if it exists and is active.
     */
    @NotNull
    <T extends ISetting<?>> Optional<T> getOptionalSetting(final ISettingKey<T> key);

    /**
     * Update a given settings value.
     * @param settingKey the given key.
     * @param value the value.
     * @param sender the player that updated the setting.
     */
    void updateSetting(ISettingKey<?> settingKey, ISetting<?> value, final ServerPlayer sender);

    /**
     * Get setting value or default.
     * @param key the key of the setting.
     * @param <T> the type of setting.
     * @param <S> the setting value type.
     * @param def the default.
     * @return the setting value or the default.
     */
    <S, T extends ISetting<S>> S getSettingValueOrDefault(ISettingKey<T> key, S def);
}
