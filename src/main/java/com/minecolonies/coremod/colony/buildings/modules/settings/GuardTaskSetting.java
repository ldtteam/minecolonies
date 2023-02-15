package com.minecolonies.coremod.colony.buildings.modules.settings;

import com.ldtteam.blockui.Loader;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.View;
import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingsModuleView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.buildings.moduleviews.ToolModuleView;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

/**
 * Stores a gurd task setting.
 */
public class GuardTaskSetting extends StringSettingWithDesc
{
    /**
     * Different setting possibilities.
     */
    public static final String PATROL =  "com.minecolonies.core.guard.setting.patrol";
    public static final String GUARD =  "com.minecolonies.core.guard.setting.guard";
    public static final String FOLLOW =  "com.minecolonies.core.guard.setting.follow";
    public static final String PATROL_MINE =  "com.minecolonies.core.guard.setting.patrol_mine";

    /**
     * Create a new guard task list setting.
     */
    public GuardTaskSetting()
    {
        super(PATROL, GUARD, FOLLOW, PATROL_MINE);
    }

    /**
     * Create a new guard task list setting.
     */
    public GuardTaskSetting(final String...list)
    {
        super(list);
    }

    /**
     * Create a new string list setting.
     * @param settings the overall list of settings.
     * @param currentIndex the current selected index.
     */
    public GuardTaskSetting(final List<String> settings, final int currentIndex)
    {
        super(settings, currentIndex);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void setupHandler(
      final ISettingKey<?> key,
      final Pane pane,
      final ISettingsModuleView settingsModuleView,
      final IBuildingView building, final BOWindow window)
    {
        Loader.createFromXMLFile(new ResourceLocation("minecolonies:gui/layouthuts/layoutguardtasksetting.xml"), (View) pane);
        pane.findPaneOfTypeByID("id", Text.class).setText(Component.literal(key.getUniqueId().toString()));
        pane.findPaneOfTypeByID("desc", Text.class).setText(Component.translatable("com.minecolonies.coremod.setting." + key.getUniqueId().toString()));
        pane.findPaneOfTypeByID("trigger", ButtonImage.class).setHandler(button -> settingsModuleView.trigger(key));
    }

    @Override
    public void render(final ISettingKey<?> key, final Pane pane, final ISettingsModuleView settingsModuleView, final IBuildingView building, final BOWindow window)
    {
        final String setting = getSettings().get(getCurrentIndex());
        final ButtonImage targetButton = pane.findPaneOfTypeByID("setTarget", ButtonImage.class);
        final Text mineLabel = pane.findPaneOfTypeByID("minePos", Text.class);
        if (setting.equals(PATROL_MINE) && building instanceof final AbstractBuildingGuards.View buildingGuards)
        {
            mineLabel.setVisible(true);
            if (buildingGuards.getMinePos() != null)
            {
                mineLabel.setText(Component.translatable("com.minecolonies.coremod.gui.worherhuts.patrollingmine", buildingGuards.getMinePos().toShortString()));
            }
            else
            {
                mineLabel.setText(Component.translatable("com.minecolonies.coremod.job.guard.assignmine"));
            }
            targetButton.setVisible(false);
        }
        else if (!setting.equals(FOLLOW))
        {
            mineLabel.setVisible(false);
            targetButton.setVisible(true);
            if (setting.equals(PATROL))
            {
                if (!settingsModuleView.getSetting(AbstractBuildingGuards.PATROL_MODE).getValue().equals(PatrolModeSetting.MANUAL))
                {
                    targetButton.setVisible(false);
                }
                else
                {
                    targetButton.setText(Component.translatable("com.minecolonies.coremod.gui.workerhuts.targetpatrol"));
                }
            }
            else
            {
                targetButton.setText(Component.translatable("com.minecolonies.coremod.gui.workerhuts.targetguard"));
            }

            targetButton.setHandler(button -> building.getModuleView(ToolModuleView.class).getWindow().open());
        }
        else
        {
            mineLabel.setVisible(false);
            targetButton.setVisible(false);
        }
        pane.findPaneOfTypeByID("trigger", ButtonImage.class).setText(Component.translatable(setting));
    }

    @Override
    public void onUpdate(final IBuilding building, final ServerPlayer sender)
    {
        if (building instanceof final AbstractBuildingGuards buildingGuards && getValue().equals(FOLLOW))
        {
            buildingGuards.setPlayerToFollow(sender);
        }
    }
}
