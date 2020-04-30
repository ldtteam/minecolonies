package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Image;
import com.ldtteam.blockout.controls.Label;
import com.ldtteam.blockout.views.ScrollingList;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingDeliveryman;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingFarmer;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Window for the farmer hut.
 */
public class WindowHutDeliveryman extends AbstractWindowWorkerBuilding<BuildingDeliveryman.View>
{
    /**
     * Resource suffix of the GUI.
     */
    private static final String HUT_DMAN_RESOURCE_SUFFIX = ":gui/windowhutdeliveryman.xml";

    /**
     * Id of the the fields list inside the GUI.
     */
    private static final String LIST_DELIVERIES = "deliveries";

    /**
     * Constructor for the window of the farmer.
     *
     * @param building {@link BuildingFarmer.View}.
     */
    public WindowHutDeliveryman(final BuildingDeliveryman.View building)
    {
        super(building, Constants.MOD_ID + HUT_DMAN_RESOURCE_SUFFIX);
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        final ScrollingList deliveryList = findPaneOfTypeByID(LIST_DELIVERIES, ScrollingList.class);
        deliveryList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return building.getTasks().size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final IRequest<?> request = building.getColony().getRequestManager().getRequestForToken(building.getTasks().get(index));

                final IRequest<?> parent = building.getColony().getRequestManager().getRequestForToken(request.getParent());
                rowPane.findPaneOfTypeByID(REQUESTER, Label.class)
                  .setLabelText(request.getRequester().getRequesterDisplayName(building.getColony().getRequestManager(), request).getFormattedText() + " ->");
                rowPane.findPaneOfTypeByID(PARENT, Label.class)
                  .setLabelText(parent.getRequester().getRequesterDisplayName(building.getColony().getRequestManager(), parent).getFormattedText());

                rowPane.findPaneOfTypeByID(REQUEST_SHORT_DETAIL, Label.class)
                  .setLabelText(request.getShortDisplayString().getFormattedText().replace("§f", ""));

                final Image logo = rowPane.findPaneOfTypeByID(DELIVERY_IMAGE, Image.class);
                logo.setImage(request.getDisplayIcon());
            }
        });
    }

    @NotNull
    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerhuts.deliveryman";
    }
}

