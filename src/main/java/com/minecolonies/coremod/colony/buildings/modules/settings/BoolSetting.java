package com.minecolonies.coremod.colony.buildings.modules.settings;

import com.ldtteam.blockout.Color;
import com.ldtteam.blockout.controls.ButtonImage;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.Box;
import com.minecolonies.api.colony.buildings.modules.settings.ISetting;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingsModuleView;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static com.minecolonies.api.util.constant.WindowConstants.OFF;
import static com.minecolonies.api.util.constant.WindowConstants.ON;

/**
 * Stores a boolean setting.
 */
public class BoolSetting implements ISetting
{
    /**
     * The value of the setting.
     */
    private boolean value;

    /**
     * Default value of the setting.
     */
    private boolean defaultValue;

    /**
     * Create a new boolean setting.
     * @param init the initial value.
     */
    public BoolSetting(final boolean init)
    {
        this.value = init;
        this.defaultValue = init;
    }

    /**
     * Create a new boolean setting.
     * @param value the value.
     * @param def the default value.
     */
    public BoolSetting(final boolean value, final boolean def)
    {
        this.value = value;
        this.defaultValue = def;
    }

    /**
     * Get the setting value.
     * @return the set value.
     */
    public boolean getValue()
    {
        return value;
    }

    /**
     * Get the default value.
     * @return the default value.
     */
    public boolean getDefault()
    {
        return defaultValue;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addHandlersToBox(final String key, final Box box, final ISettingsModuleView settingsModuleView)
    {
        final ButtonImage image;
        if (box.findPaneByID("trigger") == null)
        {
            final Text text = new Text();
            text.setText(new TranslationTextComponent("com.minecolonies.coremod.setting." + key));
            text.setSize(120, 15);
            text.setPosition(2, 10);
            text.setID("desc");
            text.setTextColor(Color.getByName("black", 0));

            image = new ButtonImage();
            image.setImage("minecolonies:textures/gui/builderhut/builder_button_very_small.png");
            image.setPosition(120, 10);
            image.setHandler(button -> settingsModuleView.trigger(key));
            image.setID("trigger");
            image.setTextColor(Color.getByName("black", 0));
            image.setSize(29, 15);
            image.setTextRenderBox(29, 15);

            box.addChild(text);
            box.addChild(image);
        }
        else
        {
            image = box.findPaneOfTypeByID("trigger", ButtonImage.class);
        }

        image.setText(new TranslationTextComponent(value ? ON : OFF));
    }

    @Override
    public void trigger()
    {
        this.value = true;
    }
}
