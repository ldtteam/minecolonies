package com.minecolonies.core.client.gui.townhall;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.colonyEvents.descriptions.IBuildingEventDescription;
import com.minecolonies.api.colony.colonyEvents.descriptions.ICitizenEventDescription;
import com.minecolonies.api.colony.colonyEvents.descriptions.IColonyEventDescription;
import com.minecolonies.api.colony.workorders.IWorkOrderView;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.core.colony.buildings.views.AbstractBuildingBuilderView;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.core.colony.eventhooks.citizenEvents.CitizenDiedEvent;
import com.minecolonies.core.network.messages.server.colony.WorkOrderChangeMessage;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * BOWindow for the town hall.
 */
public class WindowInfoPage extends AbstractWindowTownHall
{
    /**
     * List of workOrders.
     */
    private final List<IWorkOrderView> workOrders = new ArrayList<>();

    /**
     * The ScrollingList of the events.
     */
    private ScrollingList eventList;

    /**
     * Constructor for the town hall window.
     *
     * @param building {@link BuildingTownHall.View}.
     */
    public WindowInfoPage(final BuildingTownHall.View building)
    {
        super(building, "layoutinfo.xml");
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
        fillEventsList();
    }


    private void fillEventsList()
    {
        eventList = findPaneOfTypeByID(EVENTS_LIST, ScrollingList.class);
        eventList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return building.getColonyEvents().size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final Text nameLabel = rowPane.findPaneOfTypeByID(NAME_LABEL, Text.class);
                final Text actionLabel = rowPane.findPaneOfTypeByID(ACTION_LABEL, Text.class);

                final IColonyEventDescription event = building.getColonyEvents().get(index);
                if (event instanceof CitizenDiedEvent)
                {
                    actionLabel.setText(Component.literal(((CitizenDiedEvent) event).getDeathCause()));
                }
                else
                {
                    actionLabel.setText(Component.literal(event.getName()));
                }
                if (event instanceof ICitizenEventDescription)
                {
                    nameLabel.setText(Component.literal(((ICitizenEventDescription) event).getCitizenName()));
                }
                else if (event instanceof IBuildingEventDescription)
                {
                    IBuildingEventDescription buildEvent = (IBuildingEventDescription) event;
                    nameLabel.setText(MessageUtils.format(buildEvent.getBuildingName()).append(Component.literal(" " + buildEvent.getLevel())).create());
                    PaneBuilders.tooltipBuilder().append(nameLabel.getText()).hoverPane(nameLabel).build();
                }
                rowPane.findPaneOfTypeByID(POS_LABEL, Text.class)
                  .setText(Component.literal(event.getEventPos().getX() + " " + event.getEventPos().getY() + " " + event.getEventPos().getZ()));
                rowPane.findPaneOfTypeByID(BUTTON_ADD_PLAYER_OR_FAKEPLAYER, Button.class).hide();
            }
        });
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
                    new WorkOrderChangeMessage(this.building, id, false, workOrder.getPriority()).sendToServer();
                }
                else if (buttonLabel.equals(BUTTON_DOWN) && i <= workOrders.size())
                {
                    workOrder.setPriority(workOrders.get(i + 1).getPriority() - 1);
                    new WorkOrderChangeMessage(this.building, id, false, workOrder.getPriority()).sendToServer();
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
        new WorkOrderChangeMessage(this.building, id, true, 0).sendToServer();
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
                    rowPane.findPaneOfTypeByID(BUTTON_DOWN, Button.class).setVisible(numElements != 1);
                    rowPane.findPaneOfTypeByID(BUTTON_UP, Button.class).hide();
                }
                else if (index == numElements - 1)
                {
                    rowPane.findPaneOfTypeByID(BUTTON_DOWN, Button.class).hide();
                }
                else
                {
                    rowPane.findPaneOfTypeByID(BUTTON_DOWN, Button.class).show();
                    rowPane.findPaneOfTypeByID(BUTTON_UP, Button.class).show();
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
                workOrderTextPanel.setText(Component.literal(workOrder.getDisplayName().getString().replace("\n", ": ")));
                rowPane.findPaneOfTypeByID(ASSIGNEE_LABEL, Text.class).setText(Component.literal(claimingCitizen));
                rowPane.findPaneOfTypeByID(HIDDEN_WORKORDER_ID, Text.class).setText(Component.literal(Integer.toString(workOrder.getId())));
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
        return BUTTON_INFOPAGE;
    }
}