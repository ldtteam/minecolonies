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
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Arrays;
import java.util.List;

import static com.minecolonies.api.research.util.ResearchConstants.PLANT_2;

/**
 * Stores the string-list setting (Like enum, but easily serializable).
 */
public class PlantationSetting extends StringSetting
{
    public static String SPLIT_TOKEN = "/";

    /**
     * Cached research.
     */
    private boolean hasResearch;

    /**
     * Create a new string list setting.
     *
     * @param settings the overall list of settings.
     */
    public PlantationSetting(final String... settings)
    {
        super(settings);
    }

    /**
     * Create a new string list setting.
     *
     * @param settings     the overall list of settings.
     * @param currentIndex the current selected index.
     */
    public PlantationSetting(final List<String> settings, final int currentIndex)
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
        hasResearch = building.getColony().getResearchManager().getResearchEffects().getEffectStrength(PLANT_2) > 0;
        Loader.createFromXMLFile("minecolonies:gui/layouthuts/layoutstringsetting.xml", (View) pane);
        pane.findPaneOfTypeByID("id", Text.class).setText(key.getUniqueId().toString());
        pane.findPaneOfTypeByID("trigger", ButtonImage.class).setHandler(button -> settingsModuleView.trigger(key));
    }

    @Override
    public void render(final ISettingKey<?> key, final Pane pane, final ISettingsModuleView settingsModuleView, final IBuildingView building, final Window window)
    {
        pane.findPaneOfTypeByID("trigger", ButtonImage.class).setText(getCombinedSetting());
    }

    /**
     * Get the setting when the research exists.
     *
     * @return the combined setting.
     */
    private IFormattableTextComponent getCombinedSetting()
    {
        int index = getCurrentIndex();
        if (!hasResearch)
        {
            index -= (getSettings().size() / 2);
        }

        final String[] inputSplit = getSettings().get(index).split(SPLIT_TOKEN);
        return Arrays.stream(inputSplit)
          .map(TranslationTextComponent::new)
          .reduce(new TranslationTextComponent(""), (current, next) -> {
              current.append(next);
              if (!next.getKey().equals(inputSplit[inputSplit.length - 1]))
              {
                  current.append(" & ");
              }
              return current;
          });
    }
}
