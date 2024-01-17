package com.minecolonies.core.client.gui.modules;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.workorders.IWorkOrderView;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.AbstractModuleWindow;
import com.minecolonies.core.colony.buildings.moduleviews.SettingsModuleView;
import com.minecolonies.core.colony.buildings.moduleviews.WorkOrderListModuleView;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.core.network.messages.server.colony.WorkOrderChangeMessage;
import com.minecolonies.core.network.messages.server.colony.building.builder.BuilderSelectWorkOrderMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * BOWindow for the builder hut workorder list.
 */
public class WorkOrderModuleWindow extends AbstractModuleWindow
{
    /**
     * List of workOrders.
     */
    private final List<IWorkOrderView> workOrders = new ArrayList<>();

    /**
     * List of workorders.
     */
    private ScrollingList workOrdersList;

    /**
     * If the building is set to manual builder mode.
     */
    private boolean manualMode;

    /**
     * The tick check.
     */
    private int tick = 0;

    /**
     * @param res
     * @param building
     * @param moduleView
     */
    public WorkOrderModuleWindow(final String res, final IBuildingView building, final WorkOrderListModuleView moduleView)
    {
        super(building, res);

        window.findPaneOfTypeByID(DESC_LABEL, Text.class).setText(Component.translatable(moduleView.getDesc().toLowerCase(Locale.US)));
        registerButton(WORK_ORDER_SELECT, this::selectWorkOrder);
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        manualMode = buildingView.getModuleViewByType(SettingsModuleView.class).getSetting(BuildingBuilder.MODE).getValue().equals(BuildingBuilder.MANUAL_SETTING);

        workOrdersList = findPaneOfTypeByID(LIST_WORK_ORDERS, ScrollingList.class);
        workOrdersList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return workOrders.size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                updateAvailableWorkOrders(index, rowPane);
            }
        });

        updateWorkOrders();
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        if (tick++ == 20)
        {
            tick = 0;
            updateWorkOrders();
        }
    }

    /**
     * Clears and resets all work orders.
     */
    private void updateWorkOrders()
    {
        workOrders.clear();
        workOrders.addAll(buildingView.getColony().getWorkOrders().stream()
          .filter(wo -> wo.shouldShowIn(buildingView))
          .collect(Collectors.toList()));

        if (manualMode)
        {
            workOrders.removeIf(order -> !order.getClaimedBy().equals(buildingView.getPosition()) && !order.getClaimedBy().equals(BlockPos.ZERO));
        }
        else
        {
            workOrders.removeIf(order -> !order.getClaimedBy().equals(buildingView.getPosition()));
        }

        workOrders.removeIf(order -> !order.canBuildIgnoringDistance(buildingView.getPosition(), buildingView.getBuildingLevel()));

        sortWorkOrders();
    }

    /**
     * Re-sorts the WorkOrders list according to the priorities inside the list.
     */
    private void sortWorkOrders()
    {
        workOrders.sort(Comparator.comparing(IWorkOrderView::getPriority, Comparator.reverseOrder()));
    }

    /**
     * Updates the available work orders page.
     *
     * @param index   index in the list of resources.
     * @param rowPane The Pane to use to display the information.
     */
    private void updateAvailableWorkOrders(final int index, @NotNull final Pane rowPane)
    {
        final IWorkOrderView order = workOrders.get(index);

        Text workOrderTextPanel = rowPane.findPaneOfTypeByID(WORK_ORDER_NAME, Text.class);
        PaneBuilders.tooltipBuilder()
          .append(order.getDisplayName())
          .hoverPane(workOrderTextPanel)
          .build();
        workOrderTextPanel.setText(order.getDisplayName());
        rowPane.findPaneOfTypeByID(WORK_ORDER_POS, Text.class)
          .setText(Component.translatable("com.minecolonies.coremod.gui.blocks.distance", BlockPosUtil.getDistance2D(order.getLocation(), buildingView.getPosition())));

        if (order.getClaimedBy().equals(buildingView.getPosition()))
        {
            rowPane.findPaneOfTypeByID(WORK_ORDER_SELECT, ButtonImage.class).setText(Component.translatable("com.minecolonies.coremod.gui.builder.cancel"));
        }
        else if (manualMode)
        {
            rowPane.findPaneOfTypeByID(WORK_ORDER_SELECT, ButtonImage.class).setText(Component.translatable("com.minecolonies.coremod.gui.builder.select"));
        }
    }

    /**
     * On click select the clicked work order.
     *
     * @param button the clicked button.
     */
    private void selectWorkOrder(@NotNull final Button button)
    {
        final int row = workOrdersList.getListElementIndexByPane(button);
        final IWorkOrderView view = workOrders.get(row);

        if (view.getClaimedBy().equals(buildingView.getPosition()))
        {
            view.setClaimedBy(buildingView.getPosition());
            Network.getNetwork().sendToServer(new WorkOrderChangeMessage(buildingView, view.getId(), true, 0));
        }
        else
        {
            Network.getNetwork().sendToServer(new BuilderSelectWorkOrderMessage(buildingView, view.getId()));
        }
    }
}
