package com.minecolonies.coremod.colony.buildings.modules.settings;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingsModuleView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.buildings.moduleviews.ToolModuleView;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

import static com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards.PATROL_MODE;
import static com.minecolonies.coremod.colony.buildings.modules.settings.GuardPatrolModeSetting.MANUAL;

/**
 * Stores a guard task setting.
 */
public class GuardTaskSetting extends StringSettingWithDesc
{
    /**
     * Different setting possibilities.
     */
    public static final String PATROL      = "com.minecolonies.core.guard.setting.patrol";
    public static final String GUARD       = "com.minecolonies.core.guard.setting.guard";
    public static final String FOLLOW      = "com.minecolonies.core.guard.setting.follow";
    public static final String PATROL_MINE = "com.minecolonies.core.guard.setting.patrol_mine";

    /**
     * Different trigger button widths.
     */
    private static final int SET_POS_BUTTON_WIDTH = 60;
    private static final int HELP_BUTTON_WIDTH    = 125;

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

    @Override
    public ResourceLocation getLayoutItem()
    {
        return new ResourceLocation("minecolonies:gui/layouthuts/layoutguardtasksetting.xml");
    }

    @Override
    public void onUpdate(final IBuilding building, final ServerPlayer sender)
    {
        if (building instanceof AbstractBuildingGuards guardBuilding && getValue().equals(FOLLOW))
        {
            guardBuilding.setPlayerToFollow(sender);
        }
    }

    @Override
    public void setupHandler(final ISettingKey<?> key, final Pane pane, final ISettingsModuleView settingsModuleView, final IBuildingView building, final BOWindow window)
    {
        super.setupHandler(key, pane, settingsModuleView, building, window);

        final ButtonImage setPositionsButton = pane.findPaneOfTypeByID("setPositions", ButtonImage.class);
        setPositionsButton.setHandler(button -> building.getModuleView(ToolModuleView.class).getWindow().open());
    }

    @Override
    public void render(final ISettingKey<?> key, final Pane pane, final ISettingsModuleView settingsModuleView, final IBuildingView building, final BOWindow window)
    {
        super.render(key, pane, settingsModuleView, building, window);

        final ButtonImage setPositionsButton = pane.findPaneOfTypeByID("setPositions", ButtonImage.class);
        final ButtonImage helpButton = pane.findPaneOfTypeByID("helpButton", ButtonImage.class);

        switch (getValue())
        {
            case PATROL ->
            {
                final String patrolMode = settingsModuleView.getSetting(PATROL_MODE).getValue();
                setPositionsButton.setVisible(patrolMode.equals(MANUAL));
                helpButton.setVisible(false);
            }
            case GUARD -> setPositionsButton.setVisible(true);
            case PATROL_MINE ->
            {
                setPositionsButton.setVisible(false);
                helpButton.setVisible(true);
                setPatrolMineHelpLabel(helpButton, (AbstractBuildingGuards.View) building);
            }
            default ->
            {
                setPositionsButton.setVisible(false);
                helpButton.setVisible(false);
            }
        }
    }

    @Override
    protected int getButtonWidth(final ISettingsModuleView settingsModuleView)
    {
        return switch (getValue())
        {
            case PATROL ->
            {
                final String patrolMode = settingsModuleView.getSetting(PATROL_MODE).getValue();
                yield patrolMode.equals(MANUAL) ? SET_POS_BUTTON_WIDTH : MAX_BUTTON_WIDTH;
            }
            case GUARD -> SET_POS_BUTTON_WIDTH;
            case PATROL_MINE -> HELP_BUTTON_WIDTH;
            default -> MAX_BUTTON_WIDTH;
        };
    }

    /**
     * Set the correct text on the patrol mine help button.
     *
     * @param button   the button instance.
     * @param building the building.
     */
    private void setPatrolMineHelpLabel(final ButtonImage button, final AbstractBuildingGuards.View building)
    {
        Component component;
        if (building.getMinePos() != null)
        {
            component = Component.translatable("com.minecolonies.coremod.gui.worherhuts.patrollingmine", building.getMinePos().toShortString());
        }
        else
        {
            component = Component.translatable("com.minecolonies.coremod.job.guard.assignmine");
        }
        PaneBuilders.tooltipBuilder()
          .append(component)
          .hoverPane(button)
          .build();
    }
}
