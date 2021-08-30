package com.minecolonies.coremod.colony.buildings.modules.settings;

import com.minecolonies.api.colony.buildings.modules.settings.ISettingsModuleView;
import java.util.List;

import static com.minecolonies.api.research.util.ResearchConstants.RECIPE_MODE;

/**
 * Stores the recipe setting for crafters.
 */
public class CrafterRecipeSetting extends StringSettingWithDesc
{
    /**
     * Different setting possibilities.
     */
    public static final String PRIORITY =  "com.minecolonies.core.crafting.setting.priority";
    public static final String MAX_STOCK =  "com.minecolonies.core.crafting.setting.maxstock";

    /**
     * Create a new guard task list setting.
     */
    public CrafterRecipeSetting()
    {
        super(PRIORITY, MAX_STOCK);
    }

    /**
     * Create a new string list setting.
     * @param settings the overall list of settings.
     * @param currentIndex the current selected index.
     */
    public CrafterRecipeSetting(final List<String> settings, final int currentIndex)
    {
        super(settings, currentIndex);
    }

    @Override
    public boolean isActive(final ISettingsModuleView module)
    {
        return module.getColony().getResearchManager().getResearchEffects().getEffectStrength(RECIPE_MODE) > 0;
    }
}
