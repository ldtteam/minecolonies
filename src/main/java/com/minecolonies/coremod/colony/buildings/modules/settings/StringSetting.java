package com.minecolonies.coremod.colony.buildings.modules.settings;

import com.ldtteam.blockout.Loader;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.ButtonImage;
import com.ldtteam.blockout.views.Box;
import com.ldtteam.blockout.views.View;
import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingsModuleView;
import com.minecolonies.api.colony.buildings.modules.settings.IStringSetting;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Stores a string-list setting (Like enum, but easily serializable).
 */
public class StringSetting implements IStringSetting
{
    /**
     * The value of the setting.
     */
    private final List<String> settings;

    /**
     * Current index of the setting.
     */
    private int currentIndex;

    /**
     * Create a new string list setting.
     * @param settings the overall list of settings.
     */
    public StringSetting(final String...settings)
    {
        this.settings = Arrays.asList(settings);
        this.currentIndex = 0;
    }

    /**
     * Create a new string list setting.
     * @param settings the overall list of settings.
     * @param currentIndex the current selected index.
     */
    public StringSetting(final List<String> settings, final int currentIndex)
    {
        this.settings = settings;
        this.currentIndex = currentIndex;
    }

    @Override
    public String getValue()
    {
        return settings.get(currentIndex);
    }

    @Override
    public String getDefault()
    {
        return settings.get(0);
    }

    @Override
    public int getCurrentIndex()
    {
        return currentIndex;
    }

    @Override
    public List<String> getSettings()
    {
        return new ArrayList<>(settings);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addHandlersToBox(
      final ISettingKey<?> key,
      final Pane pane,
      final ISettingsModuleView settingsModuleView,
      final IBuildingView building, final Window window)
    {
        if (pane.findPaneOfTypeByID("box", Box.class).getChildren().isEmpty())
        {
            Loader.createFromXMLFile("minecolonies:gui/layouthuts/layoutstringsetting.xml", (View) pane);
            pane.findPaneOfTypeByID("trigger", ButtonImage.class).setHandler(button -> settingsModuleView.trigger(key));
        }
        pane.findPaneOfTypeByID("trigger", ButtonImage.class).setText(new TranslationTextComponent(settings.get(currentIndex)));
    }

    @Override
    public void trigger()
    {
        this.currentIndex++;
        if (currentIndex >= settings.size())
        {
            currentIndex = 0;
        }
    }
}
