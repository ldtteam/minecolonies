package com.minecolonies.coremod.colony.buildings.modules.settings;

import com.ldtteam.blockui.Loader;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.View;
import com.ldtteam.blockui.views.Window;
import com.minecolonies.api.colony.buildings.modules.settings.ISetting;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingsModuleView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.client.gui.WindowSelectRes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Stores a boolean setting.
 */
public class BlockSetting implements ISetting
{
    /**
     * The value of the setting.
     */
    private BlockItem value;

    /**
     * Default value of the setting.
     */
    private BlockItem defaultValue;

    /**
     * Create a new boolean setting.
     *
     * @param init the initial value.
     */
    public BlockSetting(final BlockItem init)
    {
        this.value = init;
        this.defaultValue = init;
    }

    /**
     * Create a new boolean setting.
     *
     * @param value the value.
     * @param def   the default value.
     */
    public BlockSetting(final BlockItem value, final BlockItem def)
    {
        this.value = value;
        this.defaultValue = def;
    }

    /**
     * Get the setting value.
     *
     * @return the set value.
     */
    public BlockItem getValue()
    {
        return value;
    }

    /**
     * Get the default value.
     *
     * @return the default value.
     */
    public BlockItem getDefault()
    {
        return defaultValue;
    }

    /**
     * Set a new block value.
     *
     * @param value the itemblock to set.
     */
    public void setValue(final BlockItem value)
    {
        this.value = value;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void setupHandler(
      final ISettingKey<?> key,
      final Pane pane,
      final ISettingsModuleView settingsModuleView,
      final IBuildingView building, final Window window)
    {

        Loader.createFromXMLFile("minecolonies:gui/layouthuts/layoutblocksetting.xml", (View) pane);
        pane.findPaneOfTypeByID("id", Text.class).setText(key.getUniqueId().toString());
        pane.findPaneOfTypeByID("desc", Text.class).setText(new TranslatableComponent("com.minecolonies.coremod.setting." + key.getUniqueId().toString()));

        pane.findPaneOfTypeByID("trigger", ButtonImage.class).setHandler(button -> new WindowSelectRes(
          window,
          building,
          (stack) -> stack.getItem() instanceof BlockItem, (stack, qty) -> {
            value = (BlockItem) stack.getItem();
            settingsModuleView.trigger(key);
        }, false).open());
    }

    @Override
    public void render(final ISettingKey<?> key, final Pane pane, final ISettingsModuleView settingsModuleView, final IBuildingView building, final Window window)
    {
        pane.findPaneOfTypeByID("icon", ItemIcon.class).setItem(new ItemStack(value));
        pane.findPaneOfTypeByID("trigger", ButtonImage.class).setText(new TranslatableComponent(SWITCH));
    }

    @Override
    public void trigger()
    {

    }
}
