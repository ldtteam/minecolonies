package com.minecolonies.core.colony.buildings.modules.settings;

import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.placement.StructureIterators;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.ISettingsModule;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingsModuleView;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingBuilder;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.minecolonies.api.research.util.ResearchConstants.BUILDER_MODE;

/**
 * Stores the builder mode setting.
 */
public class BuilderModeSetting extends StringSetting
{
    /**
     * Reason display constants.
     */
    public static final String NEEDS_RESEARCH_REASON  = "com.minecolonies.coremod.settings.reason.needsresearch";
    public static final String BUILDER_MODES_RESEARCH = "com.minecolonies.research.technology.buildermodes.name";

    /**
     * Create the builder mode setting.
     */
    public BuilderModeSetting()
    {
        super(StructureIterators.getKeySet().stream().sorted(String::compareToIgnoreCase).toList(), 0);
        set(Structurize.getConfig().getServer().iteratorType.get());
    }

    /**
     * Create the builder mode setting.
     *
     * @param value the list of possible settings.
     * @param curr  the current setting.
     */
    public BuilderModeSetting(final List<String> value, final int curr)
    {
        super(StructureIterators.getKeySet().stream().sorted(String::compareToIgnoreCase).toList(), 0);
        set(value.get(curr));
    }

    @NotNull
    public static String getActualValue(@NotNull final IBuilding building)
    {
        return building.getSettingValueOrDefault(BuildingBuilder.BUILDING_MODE, Structurize.getConfig().getServer().iteratorType.get());
    }

    @Override
    protected Component getDisplayText()
    {
        return Component.translatableEscape("com.ldtteam.structurize.iterators." + getSettings().get(getCurrentIndex()));
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

    @Override
    public @Nullable Component getInactiveReason()
    {
        return Component.translatableEscape(NEEDS_RESEARCH_REASON, Component.translatableEscape(BUILDER_MODES_RESEARCH));
    }
}
