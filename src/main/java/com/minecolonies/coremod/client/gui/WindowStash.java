package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.*;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.network.messages.ChangeDeliveryPriorityMessage;
import com.minecolonies.coremod.network.messages.ChangeDeliveryPriorityStateMessage;
import com.minecolonies.coremod.network.messages.OpenInventoryMessage;
import com.minecolonies.coremod.network.messages.PostBoxRequestMessage;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Window for the replace block GUI.
 */
public class WindowStash extends AbstractWindowSkeleton implements ButtonHandler
{

    /**
     * The building view of this window.
     */
    private final AbstractBuildingView buildingView;

    /**
     * Button to increase delivery prio.
     */
    private static final String BUTTON_DP_UP = "deliveryPrioUp";

    /**
     * Button to decrease delivery prio.
     */
    private static final String BUTTON_DP_DOWN = "deliveryPrioDown";

    /**
     * Button to set delivery prio state
     */
    private static final String BUTTON_DP_STATE = "deliveryPrioState";

    private static final String DP_MODE_STATIC = "com.minecolonies.coremod.gui.workerhuts.deliveryPrio.static";

    private static final String DP_MODE_AUTOMATIC = "com.minecolonies.coremod.gui.workerhuts.deliveryPrio.automatic";

    private int prio;

    private boolean state;

    private String stateString;

    /**
     * Create the postBox GUI.
     */
    public WindowStash(final AbstractBuildingView buildingView)
    {
        super(Constants.MOD_ID + WINDOW_STASH);
        this.buildingView = buildingView;

        registerButton(BUTTON_INVENTORY, this::inventoryClicked);
        registerButton(BUTTON_DP_UP, this::deliveryPrioUp);
        registerButton(BUTTON_DP_DOWN, this::deliveryPrioDown);
        registerButton(BUTTON_DP_STATE, this::changeDPState);

        this.prio = buildingView.getBuildingDmPrio();
        this.state = buildingView.isBuildingDmPrioState();
        this.stateString =  state ? DP_MODE_STATIC : DP_MODE_AUTOMATIC;

    }

    @Override
    public void onOpened() {
        findPaneOfTypeByID(LABEL_BUILDINGTYPE, Label.class).setLabelText(prio + "/10");
        findPaneOfTypeByID(BUTTON_DP_STATE, Button.class).setLabel(LanguageHandler.format(stateString));
        findPaneOfTypeByID(LABEL_BUILDING_NAME, Label.class).setLabelText("Stash");
    }

    private void deliveryPrioUp()
    {
        if (prio != 10)
        {
            prio++;
        }
        Network.getNetwork().sendToServer(new ChangeDeliveryPriorityMessage(buildingView, true));
        findPaneOfTypeByID(LABEL_BUILDINGTYPE, Label.class).setLabelText(prio + "/10");
    }

    private void deliveryPrioDown()
    {
        if (prio != 1)
        {
            prio--;
        }
        Network.getNetwork().sendToServer(new ChangeDeliveryPriorityMessage(buildingView, false));
        findPaneOfTypeByID(LABEL_BUILDINGTYPE, Label.class).setLabelText(prio + "/10");
    }

    private void changeDPState()
    {
        state = !state;
        stateString = state ? DP_MODE_STATIC : DP_MODE_AUTOMATIC;

        Network.getNetwork().sendToServer(new ChangeDeliveryPriorityStateMessage(buildingView));
        findPaneOfTypeByID(BUTTON_DP_STATE, Button.class).setLabel(LanguageHandler.format(stateString));
    }

    /**
     * Action when a button opening an inventory is clicked.
     */
    private void inventoryClicked()
    {
        Network.getNetwork().sendToServer(new OpenInventoryMessage(buildingView.getID()));
    }
}
