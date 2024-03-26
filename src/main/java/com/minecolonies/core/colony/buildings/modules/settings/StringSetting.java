package com.minecolonies.core.colony.buildings.modules.settings;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.modules.settings.ISetting;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingsModuleView;
import com.minecolonies.api.colony.buildings.modules.settings.IStringSetting;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.Log;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Stores a string-list setting (Like enum, but easily serializable).
 */
public class StringSetting implements IStringSetting<String>
{
    /**
     * The maximum possible width of the trigger button.
     */
    public static final int MAX_BUTTON_WIDTH = 145;

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
        this.settings = new ArrayList<>(Arrays.asList(settings));
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
        this.settings = new ArrayList<>(settings);
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

    @Override
    public ResourceLocation getLayoutItem()
    {
        return new ResourceLocation("minecolonies:gui/layouthuts/layoutstringsetting.xml");
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void setupHandler(
      final ISettingKey<?> key,
      final Pane pane,
      final ISettingsModuleView settingsModuleView,
      final IBuildingView building, final BOWindow window)
    {
        pane.findPaneOfTypeByID("trigger", ButtonImage.class).setHandler(button -> settingsModuleView.trigger(key));
    }

    @Override
    public void render(
      final ISettingKey<?> key,
      final Pane pane,
      final ISettingsModuleView settingsModuleView,
      final IBuildingView building,
      final BOWindow window)
    {
        int buttonWidth = Mth.clamp(getButtonWidth(settingsModuleView), 0, MAX_BUTTON_WIDTH);
        ButtonImage triggerButton = pane.findPaneOfTypeByID("trigger", ButtonImage.class);
        triggerButton.setSize(buttonWidth, triggerButton.getHeight());
        triggerButton.setEnabled(isActive(settingsModuleView));
        triggerButton.setText(getDisplayText());
        setHoverPane(key, triggerButton, settingsModuleView);
    }

    /**
     * Get the text to render on the button, defaults to the stored index value.
     *
     * @return the component to render on the button.
     */
    protected Component getDisplayText()
    {
        return Component.translatable(settings.get(currentIndex));
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
    public void updateSetting(final ISetting<?> setting)
    {
        if (setting instanceof StringSetting stringSetting)
        {
            this.settings.clear();
            this.settings.addAll(stringSetting.settings);
            if (currentIndex >= this.settings.size())
            {
                currentIndex = this.settings.size() - 1;
            }
        }
    }

    @Override
    public void copyValue(final ISetting<?> setting)
    {
        if (setting instanceof final StringSetting other)
        {
            set(other.getValue());
        }
    }

    @Override
    public void set(final String value)
    {
        currentIndex = getSettings().indexOf(value);
    }

    /**
     * Get the width to render the button at. This can be at most 145, to leave adequate spacing.
     *
     * @param settingsModuleView the module view that holds the setting.
     * @return the width.
     */
    protected int getButtonWidth(final ISettingsModuleView settingsModuleView)
    {
        return MAX_BUTTON_WIDTH;
    }
}
