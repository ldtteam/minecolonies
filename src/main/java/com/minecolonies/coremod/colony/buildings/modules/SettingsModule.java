package com.minecolonies.coremod.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import com.minecolonies.api.colony.buildings.modules.ISettingsModule;
import com.minecolonies.api.colony.buildings.modules.settings.ISetting;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.buildings.modules.settings.SettingKey;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Module containing all settings.
 */
public class SettingsModule extends AbstractBuildingModule implements IPersistentModule, ISettingsModule
{
    /**
     * Map of setting id (string) to generic setting.
     */
    final Map<ISettingKey<?>, ISetting> settings = new LinkedHashMap<>();

    @Override
    public <T extends ISetting> T getSetting(final ISettingKey<T> key)
    {
        return (T) settings.getOrDefault(key, null);
    }

    @Override
    public ISettingsModule with(final ISettingKey<?> key, final ISetting setting)
    {
        settings.put(key, setting);
        return this;
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        final CompoundNBT settingsCompound = compound.getCompound("settings");
        final ListNBT list = settingsCompound.getList("settingslist", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++)
        {
            final CompoundNBT entryCompound = list.getCompound(i);
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
    public void serializeNBT(final CompoundNBT compound)
    {
        final CompoundNBT settingsCompound = new CompoundNBT();

        final ListNBT list = new ListNBT();
        for (final Map.Entry<ISettingKey<?>, ISetting> setting : settings.entrySet())
        {
            final CompoundNBT entryCompound = new CompoundNBT();
            entryCompound.putString("key", setting.getKey().getUniqueId().toString());
            entryCompound.put("value", StandardFactoryController.getInstance().serialize(setting.getValue()));
            list.add(entryCompound);
        }
        settingsCompound.put("settingslist", list);

        compound.put("settings", settingsCompound);
    }

    @Override
    public void serializeToView(final PacketBuffer buf)
    {
        buf.writeInt(settings.size());
        for (final Map.Entry<ISettingKey<?>, ISetting> setting : settings.entrySet())
        {
            buf.writeResourceLocation(setting.getKey().getUniqueId());
            StandardFactoryController.getInstance().serialize(buf, setting.getValue());
        }
    }

    @Override
    public void updateSetting(final ISettingKey<?> settingKey, final ISetting value, final ServerPlayerEntity sender)
    {
        if (settings.containsKey(settingKey))
        {
            settings.put(settingKey, value);
            value.onUpdate(building, sender);
        }
    }
}
