package com.minecolonies.coremod.colony.buildings.modules.settings;

import com.ldtteam.blockout.Loader;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.controls.TextField;
import com.ldtteam.blockout.views.Box;
import com.ldtteam.blockout.views.View;
import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.buildings.modules.settings.ISetting;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingsModuleView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Stores an integer setting.
 */
public class IntSetting implements ISetting
{
    /**
     * The value of the setting.
     */
    private int value;

    /**
     * Default value of the setting.
     */
    private int defaultValue;

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
    public int getValue()
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
            Loader.createFromXMLFile("minecolonies:gui/layouthuts/layoutintsetting.xml", (View) pane);
            pane.findPaneOfTypeByID("desc", Text.class).setText(new TranslationTextComponent("com.minecolonies.coremod.setting." + key.getUniqueId().toString()));
            pane.findPaneOfTypeByID("trigger", TextField.class).setHandler(input -> {

                try
                {
                    this.value = Integer.parseInt(input.getText());
                    settingsModuleView.trigger(key);
                }
                catch (final NumberFormatException ex)
                {
                    //Noop
                }
            });
        }

        final TextField field = pane.findPaneOfTypeByID("trigger", TextField.class);
        if (!field.getText().equals(String.valueOf(this.value)))
        {
            field.setText(String.valueOf(value));
        }
    }

    @Override
    public void trigger()
    {

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
