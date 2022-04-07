package com.minecolonies.coremod.colony.buildings.modules.settings;

import com.ldtteam.blockout.Loader;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.ButtonImage;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.View;
import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingsModuleView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBeekeeper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

import static com.minecolonies.api.research.util.ResearchConstants.BEEKEEP_2;

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
      final IBuildingView building, final Window window)
    {
        hasResearch = building.getColony().getResearchManager().getResearchEffects().getEffectStrength(BEEKEEP_2) > 0;
        Loader.createFromXMLFile("minecolonies:gui/layouthuts/layoutstringsetting.xml", (View) pane);
        pane.findPaneOfTypeByID("id", Text.class).setText(new StringTextComponent(key.getUniqueId().toString()));
        pane.findPaneOfTypeByID("trigger", ButtonImage.class).setHandler(button -> settingsModuleView.trigger(key));
    }

    @Override
    public boolean isIndexAllowed(final int index)
    {
        return super.isIndexAllowed(index) && (hasResearch || !getSettings().get(index).equals(BuildingBeekeeper.BOTH));
    }
}
