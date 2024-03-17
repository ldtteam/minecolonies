package com.minecolonies.core.colony.buildings.modules.settings;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.TextField;
import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.modules.settings.ISetting;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingsModuleView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Stores an integer setting.
 */
public class IntSetting implements ISetting<Integer>
{
    /**
     * Default value of the setting.
     */
    private final int defaultValue;

    /**
     * The value of the setting.
     */
    private int value;

    /**
     * Create a new boolean setting.
     * @param init the initial value.
     */
    public IntSetting(final int init)
    {
        this.value = init;
        this.defaultValue = init;
    }

    /**
     * Create a new int setting.
     * @param value the value.
     * @param def the default value.
     */
    public IntSetting(final int value, final int def)
    {
        this.value = value;
        this.defaultValue = def;
    }

    /**
     * Get the setting value.
     * @return the set value.
     */
    public Integer getValue()
    {
        return value;
    }

    /**
     * Get the default value.
     * @return the default value.
     */
    public int getDefault()
    {
        return defaultValue;
    }

    @Override
    public ResourceLocation getLayoutItem()
    {
        return new ResourceLocation("minecolonies:gui/layouthuts/layoutintsetting.xml");
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void setupHandler(
      final ISettingKey<?> key,
      final Pane pane,
      final ISettingsModuleView settingsModuleView,
      final IBuildingView building, final BOWindow window)
    {
        pane.findPaneOfTypeByID("trigger", TextField.class).setHandler(input -> {
            try
            {
                if (input.getText().isEmpty())
                {
                    this.value = 0;
                }
                else
                {
                    this.value = Integer.parseInt(input.getText());
                    settingsModuleView.trigger(key);
                }
            }
            catch (final NumberFormatException ex)
            {
                //Noop
            }
        });
    }

    @Override
    public void render(
      final ISettingKey<?> key,
      final Pane pane,
      final ISettingsModuleView settingsModuleView,
      final IBuildingView building,
      final BOWindow window)
    {
        final TextField field = pane.findPaneOfTypeByID("trigger", TextField.class);
        field.setEnabled(isActive(settingsModuleView));
        setHoverPane(key, field, settingsModuleView);
        if (!field.getText().equals(String.valueOf(this.value)))
        {
            field.setText(String.valueOf(value));
        }
    }

    @Override
    public void copyValue(final ISetting<?> setting)
    {
        if (setting instanceof final IntSetting other)
        {
            setValue(other.getValue());
        }
    }

    /**
     * Set a new int value.
     * @param value the int to set.
     */
    public void setValue(final int value)
    {
        this.value = value;
    }
}
