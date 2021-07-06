package com.minecolonies.coremod.colony.buildings.modules.settings;

import com.ldtteam.blockout.Loader;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.ButtonImage;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.View;
import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingsModuleView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.buildings.moduleviews.ToolModuleView;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
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
      final IBuildingView building, final Window window)
    {
        Loader.createFromXMLFile("minecolonies:gui/layouthuts/layoutguardtasksetting.xml", (View) pane);
        pane.findPaneOfTypeByID("id", Text.class).setText(key.getUniqueId().toString());
        pane.findPaneOfTypeByID("desc", Text.class).setText(new TranslationTextComponent("com.minecolonies.coremod.setting." + key.getUniqueId().toString()));
        pane.findPaneOfTypeByID("trigger", ButtonImage.class).setHandler(button -> settingsModuleView.trigger(key));
    }

    @Override
    public void render(final ISettingKey<?> key, final Pane pane, final ISettingsModuleView settingsModuleView, final IBuildingView building, final Window window)
    {
        final String setting = getSettings().get(getCurrentIndex());
        final ButtonImage targetButton = pane.findPaneOfTypeByID("setTarget", ButtonImage.class);
        final Text mineLabel = pane.findPaneOfTypeByID("minePos", Text.class);
        if (setting.equals(PATROL_MINE) && building instanceof AbstractBuildingGuards.View )
        {
            mineLabel.setVisible(true);
            if (((AbstractBuildingGuards.View) building).getMinePos() != null)
            {
                mineLabel.setText(new TranslationTextComponent("com.minecolonies.coremod.gui.worherhuts.patrollingmine", ((AbstractBuildingGuards.View) building).getMinePos().toShortString()));
            }
            else
            {
                mineLabel.setText(new TranslationTextComponent("com.minecolonies.coremod.job.guard.assignmine"));
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
                    targetButton.setText(new TranslationTextComponent("com.minecolonies.coremod.gui.workerhuts.targetpatrol"));
                }
            }
            else
            {
                targetButton.setText(new TranslationTextComponent("com.minecolonies.coremod.gui.workerhuts.targetguard"));
            }

            targetButton.setHandler(button -> building.getModuleView(ToolModuleView.class).getWindow().open());
        }
        else
        {
            mineLabel.setVisible(false);
            targetButton.setVisible(false);
        }
        pane.findPaneOfTypeByID("trigger", ButtonImage.class).setText(new TranslationTextComponent(setting));
    }

    @Override
    public void onUpdate(final IBuilding building, final ServerPlayerEntity sender)
    {
        if (building instanceof AbstractBuildingGuards && getValue().equals(FOLLOW))
        {
            ((AbstractBuildingGuards) building).setPlayerToFollow(sender);
        }
    }
}
