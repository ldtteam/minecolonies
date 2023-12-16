package com.minecolonies.coremod.client.gui.modules;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.AbstractModuleWindow;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.buildings.modules.settings.GuardTaskSetting;
import com.minecolonies.coremod.colony.buildings.moduleviews.SettingsModuleView;
import com.minecolonies.coremod.network.messages.server.colony.building.guard.GuardSetMinePosMessage;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BOWindow for the miner hut.
 */
public class WindowMineGuardModule  extends AbstractModuleWindow
{
    private static final String                        LIST_GUARDS               = "guards";
    private static final String                        BUTTON_ASSIGNGUARD        = "assignGuard";
    private static final String                        HUT_MINER_RESOURCE_SUFFIX = ":gui/layouthuts/layoutguardlist.xml";

    private              ScrollingList                 guardsList;
    private              List<ICitizenDataView>        guardsInfo = new ArrayList<>();
    private int assignedGuards;

    /**
     * Constructor for the window of the miner hut.
     *
     * @param building {@link IBuildingView}.
     */
    public WindowMineGuardModule(final IBuildingView building)
    {
        super(building, Constants.MOD_ID + HUT_MINER_RESOURCE_SUFFIX);
        pullGuardsFromHut();

        registerButton(BUTTON_ASSIGNGUARD, this::assignGuardClicked);
    }

    private void assignGuardClicked(final Button button)
    {
        final int guardRow = guardsList.getListElementIndexByPane(button);
        final ICitizenDataView guard = guardsInfo.get(guardRow);
        if (guard != null)
        {
            final AbstractBuildingGuards.View guardbuilding = (AbstractBuildingGuards.View) buildingView.getColony().getBuilding(guard.getWorkBuilding());
            if (guardbuilding.getMinePos() == null)
            {
                if (assignedGuards < getMaxGuards())
                {
                    Network.getNetwork().sendToServer(new GuardSetMinePosMessage(guardbuilding, buildingView.getPosition()));
                    button.setText(Component.translatable("com.minecolonies.coremod.gui.hiring.buttonunassign"));
                    guardbuilding.setMinePos(buildingView.getPosition());
                    assignedGuards++;
                }
            }
            else if (guardbuilding.getMinePos().equals(buildingView.getPosition()))
            {
                Network.getNetwork().sendToServer(new GuardSetMinePosMessage(guardbuilding));
                button.setText(Component.translatable("com.minecolonies.coremod.gui.hiring.buttonassign"));
                guardbuilding.setMinePos(null);
                assignedGuards--;
            }
        }
        pullGuardsFromHut();
    }

    /**
     * Retrieve guards from the building to display in GUI.
     */
    private void pullGuardsFromHut()
    {
        if (buildingView.getColony().getBuilding(buildingView.getID()) != null)
        {
            guardsInfo.clear();
            assignedGuards = 0;
            final List<IBuildingView> buildings = buildingView.getColony().getBuildings().stream().filter(entry -> entry instanceof AbstractBuildingGuards.View && entry.getModuleViewByType(
              SettingsModuleView.class).getSetting(AbstractBuildingGuards.GUARD_TASK).getValue().equals(
              GuardTaskSetting.PATROL_MINE)).collect(Collectors.toList());
            for (final IBuildingView building : buildings)
            {
                final AbstractBuildingGuards.View guardbuilding = (AbstractBuildingGuards.View) building;
                if (guardbuilding.getMinePos() != null && guardbuilding.getMinePos().equals(buildingView.getPosition()))
                {
                    assignedGuards++;
                }
                for (final Integer citizenId : guardbuilding.getGuards())
                {
                    guardsInfo.add(buildingView.getColony().getCitizen(citizenId));
                }
            }

            if (guardsInfo.isEmpty())
            {
                findPaneOfTypeByID("noguardwarning", Text.class).setVisible(true);
            }
            else
            {
                findPaneOfTypeByID("noguardwarning", Text.class).setVisible(false);
            }
        }
    }

    /**
     * Get the maximum of allowed guards for the mine
     * 1 guard for mine level 1 and 2
     * 2 guards for mine level 3 and 4
     * 3 guards for mine level 5
     * @return maximum number of guards
     */
    public int getMaxGuards()
    {
        switch (buildingView.getBuildingLevel())
        {
            case 1:
            case 2:
                return 1;
            case 3:
            case 4:
                return 2;
            case 5:
                return 3;
            default:
                return 0;
        }
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        guardsList = findPaneOfTypeByID(LIST_GUARDS, ScrollingList.class);
        guardsList.setDataProvider(new ScrollingList.DataProvider() {
            @Override
            public int getElementCount()
            {
                return guardsInfo.size();
            }

            @Override
            public void updateElement(final int i, final Pane pane)
            {
                final ICitizenDataView citizen = guardsInfo.get(i);
                if (citizen != null)
                {
                    final IBuildingView building = buildingView.getColony().getBuilding(citizen.getWorkBuilding());
                    if (building instanceof AbstractBuildingGuards.View)
                    {
                        pane.findPaneOfTypeByID("guardName", Text.class).setText(Component.literal(citizen.getName()));
                        final AbstractBuildingGuards.View guardbuilding = (AbstractBuildingGuards.View) building;
                        final Button button = pane.findPaneOfTypeByID("assignGuard", Button.class);
                        if (guardbuilding.getMinePos() == null)
                        {
                            button.setText(Component.translatable("com.minecolonies.coremod.gui.hiring.buttonassign"));
                            if (assignedGuards >= getMaxGuards())
                            {
                                button.setEnabled(false);
                            }
                            else
                            {
                                button.setEnabled(true);
                            }
                        }
                        else if (guardbuilding.getMinePos().equals(buildingView.getPosition()))
                        {
                            button.setText(Component.translatable("com.minecolonies.coremod.gui.hiring.buttonunassign"));
                        }
                        else
                        {
                            button.setText(Component.translatable("com.minecolonies.coremod.gui.hiring.buttonassign"));
                            button.setEnabled(false);
                        }
                    }
                }
                else
                {
                    final Button button = pane.findPaneOfTypeByID("assignGuard", Button.class);
                    button.setEnabled(false);
                }
            }
        });
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        pullGuardsFromHut();
    }
}
