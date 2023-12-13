package com.minecolonies.coremod.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import com.minecolonies.api.colony.buildings.modules.ISettingsModule;
import com.minecolonies.api.colony.buildings.modules.settings.ISetting;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.buildings.modules.settings.SettingKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Module containing all settings.
 */
public class SettingsModule extends AbstractBuildingModule implements IPersistentModule, ISettingsModule
{
    /**
     * Map of setting id (string) to generic setting.
     */
    final Map<ISettingKey<?>, ISetting<?>> settings = new LinkedHashMap<>();

    @Override
    public <T extends ISetting<?>> T getSetting(final ISettingKey<T> key)
    {
        return (T) settings.getOrDefault(key, null);
    }

    @Override
    @NotNull
    public <T extends ISetting<?>> Optional<T> getOptionalSetting(final ISettingKey<T> key)
    {
        final T setting = getSetting(key);
        return setting == null || !setting.isActive(this) ? Optional.empty() : Optional.of(setting);
    }

    @Override
    public ISettingsModule with(final ISettingKey<?> key, final ISetting<?> setting)
    {
        settings.put(key, setting);
        return this;
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        final CompoundTag settingsCompound = compound.contains("settings") ? compound.getCompound("settings") : compound;
        final ListTag list = settingsCompound.getList("settingslist", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++)
        {
            final CompoundTag entryCompound = list.getCompound(i);
            final ResourceLocation key = new ResourceLocation(entryCompound.getString("key"));
            try
            {
                final ISetting setting = StandardFactoryController.getInstance().deserialize(entryCompound.getCompound("value"));
                final ISettingKey<?> settingsKey = new SettingKey<>(setting.getClass(), key);
                if (settings.containsKey(settingsKey))
                {
                    setting.updateSetting(settings.get(settingsKey));
                    settings.put(settingsKey, setting);
                }
            }
            catch (final IllegalArgumentException ex)
            {
                Log.getLogger().warn("Detected Removed Setting");
            }
        }
    }

    @Override
    public void serializeNBT(final CompoundTag compound)
    {
        final ListTag list = new ListTag();
        for (final Map.Entry<ISettingKey<?>, ISetting<?>> setting : settings.entrySet())
        {
            final CompoundTag entryCompound = new CompoundTag();
            entryCompound.putString("key", setting.getKey().getUniqueId().toString());
            entryCompound.put("value", StandardFactoryController.getInstance().serialize(setting.getValue()));
            list.add(entryCompound);
        }
        compound.put("settingslist", list);
    }

    @Override
    public void serializeToView(final FriendlyByteBuf buf)
    {
        buf.writeInt(settings.size());
        for (final Map.Entry<ISettingKey<?>, ISetting<?>> setting : settings.entrySet())
        {
            buf.writeResourceLocation(setting.getKey().getUniqueId());
            StandardFactoryController.getInstance().serialize(buf, setting.getValue());
        }
    }

    @Override
    public void updateSetting(final ISettingKey<?> settingKey, final ISetting<?> value, final ServerPlayer sender)
    {
        if (settings.containsKey(settingKey))
        {
            settings.put(settingKey, value);
            value.onUpdate(building, sender);
        }
    }

    @Override
    public <S, T extends ISetting<S>> S getSettingValueOrDefault(final ISettingKey<T> key, final S def)
    {
        final T setting = getSetting(key);
        return setting == null ? def : setting.getValue();
    }
}
