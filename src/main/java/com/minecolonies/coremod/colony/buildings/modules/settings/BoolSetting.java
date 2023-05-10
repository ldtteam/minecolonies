package com.minecolonies.coremod.colony.buildings.modules.settings;

import com.ldtteam.blockui.Loader;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.View;
import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.modules.settings.ISetting;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingsModuleView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
    public void setupHandler(
      final ISettingKey<?> key,
      final Pane pane,
      final ISettingsModuleView settingsModuleView,
      final IBuildingView building, final BOWindow window)
    {
        Loader.createFromXMLFile(new ResourceLocation("minecolonies:gui/layouthuts/layoutboolsetting.xml"), (View) pane);
        pane.findPaneOfTypeByID("id", Text.class).setText(Component.literal(key.getUniqueId().toString()));
        pane.findPaneOfTypeByID("desc", Text.class).setText(Component.translatable("com.minecolonies.coremod.setting." + key.getUniqueId().toString()));
        pane.findPaneOfTypeByID("trigger", ButtonImage.class).setHandler(button -> settingsModuleView.trigger(key));
    }

    @Override
    public void render(final ISettingKey<?> key, final Pane pane, final ISettingsModuleView settingsModuleView, final IBuildingView building, final BOWindow window)
    {
        pane.findPaneOfTypeByID("trigger", ButtonImage.class).setText(Component.translatable(value ? ON : OFF));
    }

    @Override
    public void trigger()
    {
        this.value = !this.value;
    }

    @Override
    public void copyValue(final ISetting iSetting)
    {
        if (iSetting instanceof final BoolSetting other)
        {
            this.value = other.value;
        }
    }
}
