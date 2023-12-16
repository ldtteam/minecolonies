package com.minecolonies.coremod.colony.buildings.modules.settings;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingsModuleView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBeekeeper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

import static com.minecolonies.api.research.util.ResearchConstants.BEEKEEP_2;

/**
 * Stores the beekeeper collection setting.
 */
public class BeekeeperCollectionSetting extends StringSetting
{
    /**
     * Cached research.
     */
    private boolean hasResearch;

    /**
     * Create a new string list setting.
     *
     * @param settings the overall list of settings.
     */
    public BeekeeperCollectionSetting(final String... settings)
    {
        super(settings);
    }

    /**
     * Create a new string list setting.
     *
     * @param settings     the overall list of settings.
     * @param currentIndex the current selected index.
     */
    public BeekeeperCollectionSetting(final List<String> settings, final int currentIndex)
    {
        super(settings, currentIndex);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void setupHandler(
      final ISettingKey<?> key,
      final Pane pane,
      final ISettingsModuleView settingsModuleView,
      final IBuildingView building,
      final BOWindow window)
    {
        hasResearch = building.getColony().getResearchManager().getResearchEffects().getEffectStrength(BEEKEEP_2) > 0;
        pane.findPaneOfTypeByID("trigger", ButtonImage.class).setHandler(button -> settingsModuleView.trigger(key));
    }

    @Override
    public boolean isIndexAllowed(final int index)
    {
        return super.isIndexAllowed(index) && (hasResearch || !getSettings().get(index).equals(BuildingBeekeeper.BOTH));
    }
}
