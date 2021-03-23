package com.minecolonies.api.colony.buildings.modules.settings;

import net.minecraft.util.ResourceLocation;

/**
 * Key type for settings.
 */
public interface ISettingKey<T extends ISetting>
{
    /**
     * Get the class type of the key.
     * @return the type.
     */
    Class<T> getType();

    /**
     * Get the unique id of the setting.
     * @return the res location.
     */
    ResourceLocation getUniqueId();
}
