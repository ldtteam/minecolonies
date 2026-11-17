package com.minecolonies.core.client.gui.modules.building;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.CheckBox;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.AbstractModuleWindow;
import com.minecolonies.core.colony.buildings.moduleviews.StablePatrolTargetsModuleView;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingStable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Window for selecting a Stable's automatic cavalry patrol targets.
 */
public class StablePatrolTargetsModuleWindow extends AbstractModuleWindow<StablePatrolTargetsModuleView>
{
    /**
     * Window elements
     */
    private static final String TARGET_LIST = "patrolTargets";
    private static final String TARGET_NAME = "targetName";
    private static final String TARGET_SELECTED = "targetSelected";
    private static final String TARGET_COUNT = "targetCount";
    private static final String ACTIVE_PATROL = "activePatrol";

    private List<IBuildingView> eligibleBuildings = new ArrayList<>();
    private List<BlockPos> selectedTargets = new ArrayList<>();
    private ScrollingList targetList;

    private BlockPos displayedActiveTarget = null;

    /**
     * Constructs the patrol target selection window.
     *
     * @param moduleView the patrol target module view.
     */
    public StablePatrolTargetsModuleWindow(final StablePatrolTargetsModuleView moduleView)
    {
        super(moduleView, new ResourceLocation(Constants.MOD_ID, "gui/layouthuts/layoutstablepatroltargets.xml"));
        registerButton(TARGET_SELECTED, this::selectionClicked);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUpdate()
    {
        super.onUpdate();

        if (!Objects.equals(displayedActiveTarget, moduleView.getActivePatrolTarget()))
        {
            displayedActiveTarget = moduleView.getActivePatrolTarget();
            targetList.refreshElementPanes();
        }
    }

    /**
     * Populates the eligible target list when the window opens.
     */
    @Override
    public void onOpened()
    {
        super.onOpened();

        selectedTargets = new ArrayList<>(moduleView.getPatrolTargets());
        eligibleBuildings = new ArrayList<>();
        
        for (final IBuildingView building : buildingView.getColony().getClientBuildingManager().getBuildings().values())
        {
            if (BuildingStable.isCavalryPatrolTarget(building))
            {
                eligibleBuildings.add(building);
            }
        }

        targetList = findPaneOfTypeByID(TARGET_LIST, ScrollingList.class);

        targetList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * Gets the number of eligible patrol buildings.
             *
             * @return the number of displayed rows.
             */
            @Override
            public int getElementCount()
            {
                return eligibleBuildings.size();
            }

            /**
             * Updates one eligible patrol-building row.
             *
             * @param index   the eligible building index.
             * @param rowPane the row pane to update.
             */
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final IBuildingView targetBuilding = eligibleBuildings.get(index);
                final Text namePane = rowPane.findPaneOfTypeByID(TARGET_NAME, Text.class);

                if (targetBuilding.getCustomName().isEmpty())
                {
                    namePane.setText(Component.translatable(targetBuilding.getBuildingType().getTranslationKey()));
                }
                else
                {
                    namePane.setText(Component.literal(targetBuilding.getCustomName()));
                }

                final BlockPos targetPosition = targetBuilding.getPosition();
                final int distance = (int) BlockPosUtil.getDistance2D(buildingView.getPosition(), targetPosition);

                namePane.setHoverPane(null);
                PaneBuilders.tooltipBuilder()
                    .append(Component.translatable("com.minecolonies.gui.workerhuts.stable.patroltargets.coordinates",
                        targetPosition.getX(),
                        targetPosition.getY(),
                        targetPosition.getZ()))
                    .appendNL(Component.translatable("com.minecolonies.gui.workerhuts.stable.patroltargets.distance", distance))
                    .hoverPane(namePane)
                    .build();

                final Pane activePatrol = rowPane.findPaneByID(ACTIVE_PATROL);
                final BlockPos activePatrolTarget = moduleView.getActivePatrolTarget();

                if (targetPosition.equals(activePatrolTarget))
                {
                    displayedActiveTarget = activePatrolTarget;
                    activePatrol.show();
                }
                else
                {
                    activePatrol.hide();
                }

                activePatrol.setHoverPane(null);
                PaneBuilders.tooltipBuilder()
                    .append(Component.translatable("com.minecolonies.gui.workerhuts.stable.patroltargets.activepatrol"))
                    .hoverPane(activePatrol)
                    .build();

                final CheckBox checkBox = rowPane.findPaneOfTypeByID(TARGET_SELECTED, CheckBox.class);
                final boolean selected = selectedTargets.contains(targetPosition);
                checkBox.setChecked(selected);

                if ((!selected && selectedTargets.size() >= moduleView.getTargetCapacity()) ||
                    (selected && selectedTargets.size() <= 1))
                {
                    checkBox.disable();
                }
                else
                {
                    checkBox.enable();
                }
            }
        });

        updateTargetCount();
    }

    /**
     * Handles a patrol target checkbox click.
     *
     * @param button the clicked checkbox.
     */
    private void selectionClicked(@NotNull final Button button)
    {
        final int row = targetList.getListElementIndexByPane(button);
        if (row < 0 || row >= eligibleBuildings.size())
        {
            return;
        }

        final BlockPos target = eligibleBuildings.get(row).getPosition();
        final boolean selected = selectedTargets.contains(target);

        if (selected)
        {
            if (selectedTargets.size() <= 1)
            {
                return;
            }
            selectedTargets.remove(target);
        }
        else
        {
            if (selectedTargets.size() >= moduleView.getTargetCapacity())
            {
                return;
            }
            selectedTargets.add(target);
        }

        moduleView.setPatrolTargetSelected(target, !selected);
        updateTargetCount();
        targetList.refreshElementPanes();
    }

    /**
     * Updates the selected target count label.
     */
    private void updateTargetCount()
    {
        findPaneOfTypeByID(TARGET_COUNT, Text.class)
            .setText(Component.translatable("com.minecolonies.gui.workerhuts.stable.patroltargets.count",
                selectedTargets.size(),
                moduleView.getTargetCapacity()));
    }
}
