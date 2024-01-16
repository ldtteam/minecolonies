package com.minecolonies.core.colony.buildings.modules.settings;

import com.minecolonies.api.colony.buildings.modules.ISettingsModule;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingsModuleView;
import com.minecolonies.core.MineColonies;
import net.minecraftforge.fml.ModList;

public class DynamicTreesSetting extends IntSetting
{
    /**
     * Create a new dynamic trees setting, using the config value as initial value.
     */
    public DynamicTreesSetting()
    {
        super(MineColonies.getConfig().getServer().dynamicTreeHarvestSize.get());
    }

    /**
     * Create a new dynamic tree setting
     *
     * @param value        the current value
     * @param defaultValue the default value
     */
    public DynamicTreesSetting(int value, int defaultValue)
    {
        super(value, defaultValue);
    }

    @Override
    public boolean isActive(final ISettingsModule module)
    {
        return ModList.get().isLoaded("dynamictrees");
    }

    @Override
    public boolean shouldHideWhenInactive()
    {
        return true;
    }

    @Override
    public boolean isActive(ISettingsModuleView module)
    {
        return ModList.get().isLoaded("dynamictrees");
    }
}
