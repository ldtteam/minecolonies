package com.minecolonies.api.colony.buildings.modules.settings;

import net.minecraft.resources.ResourceLocation;

/**
 * Key type for settings.
 */
public interface ISettingKey<T>
{
    /**
     * Get the class type of the key.
     * @return the type.
     */
    Class<? extends T> getType();

    /**
     * Get the unique id of the setting.
     * @return the res location.
     */
    ResourceLocation getUniqueId();
}
