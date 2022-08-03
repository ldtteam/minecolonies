package com.minecolonies.coremod.colony.buildings.modules.settings;

import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.placement.StructureIterators;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.ISettingsModule;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingsModuleView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
        super(new ArrayList<>(StructureIterators.getKeySet()), 0);
        set(Structurize.getConfig().getServer().iteratorType.get());
    }

    /**
     * Create the builder mode setting.
     * @param value the list of possible settings.
     * @param curr the current setting.
     */
    public BuilderModeSetting(final List<String> value, final int curr)
    {
        super(new ArrayList<>(StructureIterators.getKeySet()), 0);
        set(value.get(curr));
    }

    @Override
    public boolean isActive(final ISettingsModule module)
    {
        return module.getBuilding().getColony().getResearchManager().getResearchEffects().getEffectStrength(BUILDER_MODE) > 0;
    }

    @Override
    public boolean isActive(final ISettingsModuleView module)
    {
        return module.getColony().getResearchManager().getResearchEffects().getEffectStrength(BUILDER_MODE) > 0;
    }

    @NotNull
    public static String getActualValue(@NotNull final IBuilding building)
    {
        return building.getOptionalSetting(BuildingBuilder.BUILDING_MODE)
                .map(StringSetting::getValue)
                .orElse(Structurize.getConfig().getServer().iteratorType.get());
    }
}
