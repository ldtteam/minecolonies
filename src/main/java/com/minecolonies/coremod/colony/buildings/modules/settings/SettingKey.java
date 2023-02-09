package com.minecolonies.coremod.colony.buildings.modules.settings;

import com.minecolonies.api.colony.buildings.modules.settings.ISetting;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/**
 * Specific Settingskey implementation.
 */
public class SettingKey<T extends ISetting> implements ISettingKey<T>
{
    /**
     * Specific settings type.
     */
    private final Class<T> type;

    /**
     * Unique id.
     */
    private final ResourceLocation id;

    /**
     * Create a new settings key.
     * @param type the specific ISetting class.
     * @param id the unique id.
     */
    public SettingKey(final Class<T> type, final ResourceLocation id)
    {
        this.type = type;
        this.id = id;
    }

    @Override
    public Class<T> getType()
    {
        return type;
    }

    @Override
    public ResourceLocation getUniqueId()
    {
        return id;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null)
        {
            return false;
        }
        return o instanceof final SettingKey<?> other && Objects.equals(type, other.type) && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(type, id);
    }
}
