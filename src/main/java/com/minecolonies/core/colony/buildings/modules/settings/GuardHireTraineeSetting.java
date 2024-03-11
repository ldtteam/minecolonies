package com.minecolonies.core.colony.buildings.modules.settings;

import com.minecolonies.api.colony.buildings.modules.ISettingsModule;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingsModuleView;
import com.minecolonies.core.colony.buildings.modules.GuardBuildingModule;
import com.minecolonies.core.colony.buildings.moduleviews.CombinedHiringLimitModuleView;
import com.minecolonies.core.util.BuildingUtils;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Stores the hire trainee setting.
 */
public class GuardHireTraineeSetting extends BoolSetting
{
    /**
     * Reason display constants.
     */
    public static final String HIRING_MODE_REASON = "com.minecolonies.coremod.settings.reason.hiringmode";

    /**
     * Create a new hire trainee setting.
     *
     * @param init the initial value.
     */
    public GuardHireTraineeSetting(final boolean init)
    {
        super(init);
    }

    /**
     * Create a new hire trainee setting.
     *
     * @param value the value.
     * @param def   the default value.
     */
    public GuardHireTraineeSetting(final boolean value, final boolean def)
    {
        super(value, def);
    }

    @Override
    public boolean isActive(final ISettingsModule module)
    {
        final List<GuardBuildingModule> guardModules = module.getBuilding().getModulesByType(GuardBuildingModule.class);
        for (final GuardBuildingModule guardModule : guardModules)
        {
            if (BuildingUtils.canAutoHire(module.getBuilding(), guardModule.getHiringMode(), guardModule.getJobEntry()))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    @Nullable
    public Component getInactiveReason()
    {
        return Component.translatable(HIRING_MODE_REASON);
    }

    @Override
    public boolean isActive(final ISettingsModuleView module)
    {
        final List<CombinedHiringLimitModuleView> guardModules = module.getBuildingView().getModuleViews(CombinedHiringLimitModuleView.class);
        for (final CombinedHiringLimitModuleView guardModule : guardModules)
        {
            if (BuildingUtils.canAutoHire(module.getBuildingView(), guardModule.getHiringMode(), guardModule.getJobEntry()))
            {
                return true;
            }
        }
        return false;
    }
}
