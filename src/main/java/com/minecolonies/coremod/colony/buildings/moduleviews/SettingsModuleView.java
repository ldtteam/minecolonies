package com.minecolonies.coremod.colony.buildings.moduleviews;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.buildings.modules.*;
import com.minecolonies.api.colony.buildings.modules.settings.ISetting;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingsModuleView;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.modules.SettingsModuleWindow;
import com.minecolonies.coremod.network.messages.server.colony.building.TriggerSettingMessage;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Module containing all settings (client side)
 */
public class SettingsModuleView extends AbstractBuildingModuleView implements ISettingsModuleView
{
    /**
     * Map of setting id (string) to generic setting.
     */
    final HashMap<String, ISetting> settings = new HashMap<>();

    @Override
    public void deserialize(@NotNull final PacketBuffer buf)
    {
        settings.clear();

        final int size = buf.readInt();
        for (int i = 0; i < size; i++)
        {
            final String key = buf.readString(32767);
            final ISetting setting = StandardFactoryController.getInstance().deserialize(buf);
            settings.put(key, setting);
        }
    }

    /**
     * Get the full settings map.
     * @return the map of string key and ISetting value.
     */
    public Map<String, ISetting> getSettings()
    {
        return settings;
    }

    @Override
    public Window getWindow()
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
    public void trigger(final String key)
    {
        final ISetting setting = settings.get(key);
        setting.trigger();
        Network.getNetwork().sendToServer(new TriggerSettingMessage(buildingView, key, setting));
    }
}
