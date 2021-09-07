package com.minecolonies.coremod.colony.buildings.modules.settings;

import com.minecolonies.api.colony.buildings.modules.settings.ISettingsModuleView;
import com.minecolonies.coremod.MineColonies;
import net.minecraftforge.fml.ModList;

public class DynamicTreesSetting extends IntSetting
{
    /**
     *
     */
    public DynamicTreesSetting()
    {
        super(MineColonies.getConfig().getServer().dynamicTreeHarvestSize.get());
    }

    public DynamicTreesSetting(int value, int defaultValue)
    {
        super(value, defaultValue);
    }

    @Override
    public boolean isActive(ISettingsModuleView module)
    {
        return ModList.get().isLoaded("dynamictrees");
    }
}
