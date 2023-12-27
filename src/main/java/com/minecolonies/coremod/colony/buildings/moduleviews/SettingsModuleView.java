package com.minecolonies.coremod.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.api.colony.buildings.modules.settings.ISetting;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingsModuleView;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.modules.SettingsModuleWindow;
import com.minecolonies.coremod.colony.buildings.modules.settings.SettingKey;
import com.minecolonies.coremod.network.messages.server.colony.building.TriggerSettingMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Module containing all settings (client side).
 */
public class SettingsModuleView extends AbstractBuildingModuleView implements ISettingsModuleView
{
    /**
     * Map of setting id (string) to generic setting.
     */
    final Map<ISettingKey<? extends ISetting>, ISetting> settings = new LinkedHashMap<>();

    @Override
    public void deserialize(@NotNull final FriendlyByteBuf buf)
    {
        final Map<ISettingKey<?>, ISetting> tempSettings = new LinkedHashMap<>();
        final int size = buf.readInt();
        for (int i = 0; i < size; i++)
        {
            final ResourceLocation key = buf.readResourceLocation();
            final ISetting setting = StandardFactoryController.getInstance().deserialize(buf);
            if (setting != null)
            {
                final SettingKey<?> settingsKey = new SettingKey<>(setting.getClass(), key);
                tempSettings.put(settingsKey, setting);
                settings.putIfAbsent(settingsKey, setting);
            }
        }

        for (final Map.Entry<ISettingKey<? extends ISetting>, ISetting> entry : new ArrayList<>(settings.entrySet()))
        {
            final ISetting syncSetting = tempSettings.get(entry.getKey());
            if (syncSetting == null)
            {
                settings.remove(entry.getKey());
            }
            else if (entry.getValue() != syncSetting)
            {
                entry.getValue().updateSetting(syncSetting);
                entry.getValue().copyValue(syncSetting);
            }
        }
    }

    /**
     * Get the full settings map.
     *
     * @return the list of string key and ISetting value.
     */
    public List<ISettingKey<? extends ISetting>> getSettingsToShow()
    {
        List<ISettingKey<? extends ISetting>> filteredSettings = new ArrayList<>();
        for (Map.Entry<ISettingKey<? extends ISetting>, ISetting> setting : settings.entrySet())
        {
            if (setting.getValue().isActive(this) || !setting.getValue().shouldHideWhenInactive())
            {
                filteredSettings.add(setting.getKey());
            }
        }
        return filteredSettings;
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends ISetting> T getSetting(final ISettingKey<T> key)
    {
        return (T) settings.getOrDefault(key, null);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public BOWindow getWindow()
    {
        return new SettingsModuleWindow(Constants.MOD_ID + ":gui/layouthuts/layoutsettings.xml", buildingView, this);
    }

    @Override
    public String getIcon()
    {
        return "settings";
    }

    @Override
    public String getDesc()
    {
        return "com.minecolonies.coremod.gui.workerhuts.settings";
    }

    @Override
    public void trigger(final ISettingKey<?> key)
    {
        final ISetting setting = settings.get(key);
        if (setting.isActive(this))
        {
            setting.trigger();
            Network.getNetwork().sendToServer(new TriggerSettingMessage(buildingView, key, setting, getProducer().getRuntimeID()));
        }
    }
}
