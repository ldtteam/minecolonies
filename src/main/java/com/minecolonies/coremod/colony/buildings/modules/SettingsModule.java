package com.minecolonies.coremod.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import com.minecolonies.api.colony.buildings.modules.ISettingsModule;
import com.minecolonies.api.colony.buildings.modules.settings.ISetting;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Module containing all settings.
 */
public class SettingsModule extends AbstractBuildingModule implements IPersistentModule, ISettingsModule
{
    /**
     * Map of setting id (string) to generic setting.
     */
    final Map<ISettingKey<ISetting>, ISetting> settings = new HashMap<>();

    @Override
    public <T extends ISetting> T getSetting(final ISettingKey<T> key)
    {
        final ISetting setting = settings.getOrDefault(key, null);
        if (!key.getType().isInstance(setting))
        {
            return null;
        }
        return (T) setting;
    }

    @Override
    public IBuildingModule with(final ISettingKey<ISetting> key, final ISetting setting)
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
            final String key = entryCompound.getString("key");
            final ISetting setting = StandardFactoryController.getInstance().deserialize(entryCompound.getCompound("value"));
            settings.put(key, setting);
        }
    }

    @Override
    public void serializeNBT(final CompoundNBT compound)
    {
        final CompoundNBT settingsCompound = new CompoundNBT();

        final ListNBT list = new ListNBT();
        for (final Map.Entry<String, ISetting> setting : settings.entrySet())
        {
            final CompoundNBT entryCompound = new CompoundNBT();
            entryCompound.putString("key", setting.getKey());
            entryCompound.put("value", StandardFactoryController.getInstance().serialize(setting.getValue()));
        }
        settingsCompound.put("settingslist", list);

        compound.put("settings", settingsCompound);
    }

    @Override
    public void serializeToView(final PacketBuffer buf)
    {
        buf.writeInt(settings.size());
        for (final Map.Entry<String, ISetting> setting : settings.entrySet())
        {
            buf.writeString(setting.getKey(), 32767);
            StandardFactoryController.getInstance().serialize(buf, setting.getValue());
        }
    }
}
