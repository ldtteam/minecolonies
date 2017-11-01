package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.ColorConstants;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.Pane;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.ButtonHandler;
import com.minecolonies.blockout.controls.Label;
import com.minecolonies.blockout.views.ScrollingList;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.CitizenDataView;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.network.messages.HireFireMessage;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Window for the hiring or firing of a worker.
 */
public class WindowRequestDetail extends Window implements ButtonHandler
{
    /**
     * Id of the done button in the GUI.
     */
    private static final String BUTTON_DONE = "done";

    /**
     * Id of the cancel button in the GUI.
     */
    private static final String BUTTON_CANCEL = "cancel";

    /**
     * Id of the citizen name in the GUI.
     */
    private static final String CITIZEN_LABEL = "citizen";

    /**
     * Id of the id label in the GUI.
     */
    private static final String ID_LABEL = "id";

    /**
     * Id of the citizen list in the GUI.
     */
    private static final String CITIZEN_LIST = "unemployed";

    /**
     * Id of the attributes label in the GUI.
     */
    private static final String ATTRIBUTES_LABEL = "attributes";

    /**
     * Link to the xml file of the window.
     */
    private static final String BUILDING_NAME_RESOURCE_SUFFIX = ":gui/windowrequestdetail.xml";

    /**
     * Position of the id label of each citizen in the list.
     */
    private static final int CITIZEN_ID_LABEL_POSITION = 4;

    /**
     * Id of the fire button
     */
    private static final String BUTTON_FIRE = "fire";

    /**
     * The view of the current building.
     */
    private final AbstractBuildingWorker.View building;

    /**
     * The colony.
     */
    private final ColonyView            colony;


    /**
     * Constructor for the window when the player wants to hire a worker for a certain job.
     *
     * @param c          the colony view.
     * @param buildingId the building position.
     */
    public WindowRequestDetail(final ColonyView c, final BlockPos buildingId)
    {
        super(Constants.MOD_ID + BUILDING_NAME_RESOURCE_SUFFIX);
        this.colony = c;
        building = (AbstractBuildingWorker.View) colony.getBuilding(buildingId);
    }

    /**
     * Called when the GUI has been opened.
     * Will fill the fields and lists.
     */
    @Override
    public void onOpened()
    {

        //final String displayString = request.getDisplayString().getFormattedText();
        //final String[] labels = displayString.split("§r");
            /*final Box box = rowPane.findPaneOfTypeByID(BOX_ID_REQUEST, Box.class);
            int y = 10;
            for(final String s: labels)
            {
                final Label descriptionLabel = new Label();
                descriptionLabel.setColor(BLACK, BLACK);
                descriptionLabel.setLabelText(s);
                box.addChild(descriptionLabel);
                descriptionLabel.setPosition(20, y);
                y+=10;
            }

            final Label targetLabel = rowPane.findPaneOfTypeByID(LIST_ELEMENT_ID_REQUEST_LOCATION, Label.class);
            targetLabel.setLabelText(getNicePositionString(request.getRequester().getDeliveryLocation().getInDimensionLocation()));
            targetLabel.setPosition(1, y);

            box.setSize(box.getWidth(), y + 10);*/


    }

    /**
     * Called when any button has been clicked.
     *
     * @param button the clicked button.
     */
    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        if (button.getID().equals(BUTTON_DONE))
        {
            @NotNull final Label idLabel = (Label) button.getParent().getChildren().get(CITIZEN_ID_LABEL_POSITION);
            final int id = Integer.parseInt(idLabel.getLabelText());
            building.addWorkerId(id);
            MineColonies.getNetwork().sendToServer(new HireFireMessage(this.building, true, id));
        }
        else if (button.getID().equals(BUTTON_FIRE))
        {
            @NotNull final Label idLabel = (Label) button.getParent().getChildren().get(CITIZEN_ID_LABEL_POSITION);
            final int id = Integer.parseInt(idLabel.getLabelText());

            MineColonies.getNetwork().sendToServer(new HireFireMessage(building, false, id));
            building.removeWorkerId(id);
        }
        else if (!button.getID().equals(BUTTON_CANCEL))
        {
            return;
        }

        if (colony.getTownHall() != null)
        {
            building.openGui();
        }
    }
}
