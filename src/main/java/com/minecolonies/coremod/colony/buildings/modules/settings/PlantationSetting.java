package com.minecolonies.coremod.colony.buildings.modules.settings;

import com.ldtteam.blockui.Loader;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.View;
import com.ldtteam.blockui.views.Window;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingsModuleView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

import static com.minecolonies.api.research.util.ResearchConstants.PLANT_2;

/**
 * Stores the string-list setting (Like enum, but easily serializable).
 */
public class PlantationSetting extends StringSetting
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
        if (hasResearch)
        {
            pane.findPaneOfTypeByID("trigger", ButtonImage.class).setText(getCombinedSetting());
        }
        else
        {
            pane.findPaneOfTypeByID("trigger", ButtonImage.class).setText(new TranslatableComponent(getSettings().get(getCurrentIndex())));
        }
    }

    /**
     * Get the setting when the research exists.
     *
     * @return the combined setting.
     */
    private MutableComponent getCombinedSetting()
    {
        final MutableComponent component = new TranslatableComponent("");
        for (int i = 0; i < getSettings().size(); i++)
        {
            if (i != getCurrentIndex())
            {
                if (!component.getSiblings().isEmpty())
                {
                    component.append(new TextComponent(" & "));
                }
                component.append(new TranslatableComponent(getSettings().get(i)));
            }
        }

        return component;
    }
}
