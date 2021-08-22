package com.minecolonies.coremod.colony.buildings.modules.settings;

import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.placement.StructureIterators;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingsModuleView;

import java.util.List;

import static com.minecolonies.api.research.util.ResearchConstants.BUILDER_MODE;

/**
 * Stores the builder mode setting.
 */
public class BuilderModeSetting extends StringSetting
{
    /**
     * Create the builder mode setting.
     */
    public BuilderModeSetting()
    {
        super(StructureIterators.getKeySet(), 0);
        set(Structurize.getConfig().getServer().iteratorType.get());
    }

    /**
     * Create the builder mode setting.
     * @param value the list of possible settings.
     * @param curr the current setting.
     */
    public BuilderModeSetting(final List<String> value, final int curr)
    {
        super(StructureIterators.getKeySet(), 0);
        set(value.get(curr));
    }

    @Override
    public boolean isActive(final ISettingsModuleView module)
    {
        return module.getColony().getResearchManager().getResearchEffects().getEffectStrength(BUILDER_MODE) > 0;
    }
}
