package com.minecolonies.coremod.client.gui.townhall;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneBuilders;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.ScrollingList;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.workorders.IWorkOrderView;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingBuilderView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.coremod.network.messages.server.colony.WorkOrderChangeMessage;
import net.minecraft.util.text.StringTextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Window for the town hall.
 */
public class WindowWorkOrderPage extends AbstractWindowTownHall
{
    /**
     * List of workOrders.
     */
    private final List<IWorkOrderView> workOrders = new ArrayList<>();

    /**
     * Constructor for the town hall window.
     *
     * @param townHall {@link BuildingTownHall.View}.
     */
    public WindowWorkOrderPage(final BuildingTownHall.View townHall)
    {
        super(townHall, "layoutworkorder.xml");
        updateWorkOrders();

        registerButton(BUTTON_UP, this::updatePriority);
        registerButton(BUTTON_DOWN, this::updatePriority);
        registerButton(BUTTON_DELETE, this::deleteWorkOrder);
    }

    /**
     * Executed when <code>WindowTownHall</code> is opened. Does tasks like setting buttons.
     */
    @Override
    public void onOpened()
    {
        super.onOpened();
        fillWorkOrderList();
    }

    /**
     * Clears and resets all work orders.
     */
    private void updateWorkOrders()
    {
        workOrders.clear();
        workOrders.addAll(building.getColony().getWorkOrders().stream().filter(wo -> wo.shouldShowIn(building)).collect(Collectors.toList()));
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
     * On Button click update the priority.
     *
     * @param button the clicked button.
     */
    private void updatePriority(@NotNull final Button button)
    {
        final int id = Integer.parseInt(button.getParent().findPaneOfTypeByID("hiddenId", Text.class).getTextAsString());
        final String buttonLabel = button.getID();

        for (int i = 0; i < workOrders.size(); i++)
        {
            final IWorkOrderView workOrder = workOrders.get(i);
            if (workOrder.getId() == id)
            {
                if (buttonLabel.equals(BUTTON_UP) && i > 0)
                {
                    workOrder.setPriority(workOrders.get(i - 1).getPriority() + 1);
                    Network.getNetwork().sendToServer(new WorkOrderChangeMessage(this.building, id, false, workOrder.getPriority()));
                }
                else if (buttonLabel.equals(BUTTON_DOWN) && i <= workOrders.size())
                {
                    workOrder.setPriority(workOrders.get(i + 1).getPriority() - 1);
                    Network.getNetwork().sendToServer(new WorkOrderChangeMessage(this.building, id, false, workOrder.getPriority()));
                }

                sortWorkOrders();
                window.findPaneOfTypeByID(LIST_WORKORDER, ScrollingList.class).refreshElementPanes();
                return;
            }
        }
    }

    /**
     * On Button click remove the workOrder.
     *
     * @param button the clicked button.
     */
    private void deleteWorkOrder(@NotNull final Button button)
    {
        final int id = Integer.parseInt(button.getParent().findPaneOfTypeByID("hiddenId", Text.class).getTextAsString());
        for (int i = 0; i < workOrders.size(); i++)
        {
            if (workOrders.get(i).getId() == id)
            {
                workOrders.remove(i);
                break;
            }
        }
        Network.getNetwork().sendToServer(new WorkOrderChangeMessage(this.building, id, true, 0));
        window.findPaneOfTypeByID(LIST_WORKORDER, ScrollingList.class).refreshElementPanes();
    }

    /**
     * Fills the workOrder list inside the townhall GUI.
     */
    private void fillWorkOrderList()
    {
        final ScrollingList workOrderList = findPaneOfTypeByID(LIST_WORKORDER, ScrollingList.class);
        workOrderList.enable();
        workOrderList.show();

        //Creates a dataProvider for the unemployed citizenList.
        workOrderList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return workOrders.size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final IWorkOrderView workOrder = workOrders.get(index);
                String claimingCitizen = "";

                final int numElements = getElementCount();

                if (index == 0)
                {
                    if (numElements == 1)
                    {
                        rowPane.findPaneOfTypeByID(BUTTON_DOWN, Button.class).hide();
                    }
                    else
                    {
                        rowPane.findPaneOfTypeByID(BUTTON_DOWN, Button.class).show();
                    }
                    rowPane.findPaneOfTypeByID(BUTTON_UP, Button.class).hide();
                }
                else if (index == numElements - 1)
                {
                    rowPane.findPaneOfTypeByID(BUTTON_DOWN, Button.class).hide();
                }

                //Searches citizen of id x
                for (@NotNull final IBuildingView buildingView : building.getColony().getBuildings())
                {
                    if (buildingView.getPosition().equals(workOrder.getClaimedBy()) && buildingView instanceof AbstractBuildingBuilderView)
                    {
                        claimingCitizen = ((AbstractBuildingBuilderView) buildingView).getWorkerName();
                        break;
                    }
                }

                Text workOrderTextPanel = rowPane.findPaneOfTypeByID(WORK_LABEL, Text.class);
                PaneBuilders.tooltipBuilder().append(workOrder.getDisplayName()).hoverPane(workOrderTextPanel).build();
                workOrderTextPanel.setText(workOrder.getDisplayName());
                rowPane.findPaneOfTypeByID(ASSIGNEE_LABEL, Text.class).setText(new StringTextComponent(claimingCitizen));
                rowPane.findPaneOfTypeByID(HIDDEN_WORKORDER_ID, Text.class).setText(new StringTextComponent(Integer.toString(workOrder.getId())));
            }
        });
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        updateWorkOrders();
    }

    @Override
    protected String getWindowId()
    {
        return BUTTON_WORKORDER;
    }
}
