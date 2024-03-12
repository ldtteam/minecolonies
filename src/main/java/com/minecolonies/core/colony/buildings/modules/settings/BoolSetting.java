package com.minecolonies.core.colony.buildings.modules.settings;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.modules.settings.ISetting;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingsModuleView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.WindowConstants.OFF;
import static com.minecolonies.api.util.constant.WindowConstants.ON;

/**
 * Stores a boolean setting.
 */
public class BoolSetting implements ISetting<Boolean>
{
    /**
     * The value of the setting.
     */
    private final String tooltipKey;

    /**
     * Default value of the setting.
     */
    private final boolean defaultValue;

    /**
     * The value of the setting.
     */
    private boolean value;

    /**
     * Create a new boolean setting.
     *
     * @param init the initial value.
     */
    public BoolSetting(final boolean init)
    {
        this(init, "");
    }

    /**
     * Create a new boolean setting.
     *
     * @param init       the initial value.
     * @param tooltipKey the translation key for a tooltip to display for this setting.
     */
    public BoolSetting(final boolean init, final @NotNull String tooltipKey)
    {
        this(init, init, tooltipKey);
    }

    /**
     * Create a new boolean setting.
     *
     * @param value      the value.
     * @param def        the default value.
     * @param tooltipKey the translation key for a tooltip to display for this setting.
     */
    public BoolSetting(final boolean value, final boolean def, final @NotNull String tooltipKey)
    {
        this.value = value;
        this.defaultValue = def;
        this.tooltipKey = tooltipKey;
    }

    /**
     * Get the default value.
     *
     * @return the default value.
     */
    public boolean getDefault()
    {
        return defaultValue;
    }

    @Override
    public ResourceLocation getLayoutItem()
    {
        return new ResourceLocation("minecolonies:gui/layouthuts/layoutboolsetting.xml");
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void setupHandler(
      final ISettingKey<?> key,
      final Pane pane,
      final ISettingsModuleView settingsModuleView,
      final IBuildingView building, final BOWindow window)
    {
        pane.findPaneOfTypeByID("trigger", ButtonImage.class).setHandler(btn -> settingsModuleView.trigger(key));
    }

    @Override
    public void render(
      final ISettingKey<?> key,
      final Pane pane,
      final ISettingsModuleView settingsModuleView,
      final IBuildingView building,
      final BOWindow window)
    {
        ButtonImage triggerButton = pane.findPaneOfTypeByID("trigger", ButtonImage.class);
        triggerButton.setEnabled(isActive(settingsModuleView));
        triggerButton.setText(Component.translatable(value ? ON : OFF));
        setHoverPane(key, triggerButton, settingsModuleView);
    }

    @Override
    public void trigger()
    {
        this.value = !this.value;
    }

    @Override
    public void copyValue(final ISetting<?> setting)
    {
        if (setting instanceof final BoolSetting other)
        {
            this.value = other.value;
        }
    }

    @Override
    public Boolean getValue()
    {
        return value;
    }

    /**
     * Get the translation key for the tooltip.
     *
     * @return the translation key.
     */
    @NotNull
    public String getTooltipKey()
    {
        return tooltipKey;
    }
}
