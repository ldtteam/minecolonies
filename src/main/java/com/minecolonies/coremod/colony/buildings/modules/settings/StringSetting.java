package com.minecolonies.coremod.colony.buildings.modules.settings;

import com.ldtteam.blockout.Loader;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.ButtonImage;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.View;
import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.buildings.modules.settings.ISetting;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingsModuleView;
import com.minecolonies.api.colony.buildings.modules.settings.IStringSetting;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.Log;
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
    protected int currentIndex;

    /**
     * Create a new string list setting.
     *
     * @param settings the overall list of settings.
     */
    public StringSetting(final String... settings)
    {
        this.settings = Arrays.asList(settings);
        this.currentIndex = 0;
    }

    /**
     * Create a new string list setting.
     *
     * @param settings     the overall list of settings.
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
    public void setupHandler(
      final ISettingKey<?> key,
      final Pane pane,
      final ISettingsModuleView settingsModuleView,
      final IBuildingView building, final Window window)
    {
        Loader.createFromXMLFile("minecolonies:gui/layouthuts/layoutstringsetting.xml", (View) pane);
        pane.findPaneOfTypeByID("id", Text.class).setText(key.getUniqueId().toString());
        pane.findPaneOfTypeByID("trigger", ButtonImage.class).setHandler(button -> settingsModuleView.trigger(key));
    }

    @Override
    public void render(final ISettingKey<?> key, final Pane pane, final ISettingsModuleView settingsModuleView, final IBuildingView building, final Window window)
    {
        pane.findPaneOfTypeByID("trigger", ButtonImage.class).setText(new TranslationTextComponent(settings.get(currentIndex)));
    }

    @Override
    public void trigger()
    {
        boolean hasAllowedValue = false;
        for (int i = 0; i < settings.size(); i++)
        {
            currentIndex++;
            if (currentIndex >= settings.size())
            {
                currentIndex = 0;
            }

            if (isIndexAllowed(currentIndex))
            {
                hasAllowedValue = true;
                break;
            }
        }

        if (!hasAllowedValue)
        {
            Log.getLogger().warn(this.getClass().getName() + " could not select any allowed value, currentIndex is reset to 0, please report this to the developers.");
            currentIndex = 0;
        }
    }

    public boolean isIndexAllowed(int index)
    {
        return index >= 0 && index < settings.size();
    }

    @Override
    public void updateSetting(final ISetting iSetting)
    {
        if (iSetting instanceof StringSetting)
        {
            this.settings.clear();
            this.settings.addAll(((StringSetting) iSetting).settings);
            if (currentIndex >= this.settings.size())
            {
                currentIndex = this.settings.size() - 1;
            }
        }
    }

    @Override
    public void set(final String value)
    {
        currentIndex = getSettings().indexOf(value);
    }
}
