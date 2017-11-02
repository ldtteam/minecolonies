package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.ColorConstants;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.Color;
import com.minecolonies.blockout.Pane;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.ButtonHandler;
import com.minecolonies.blockout.controls.Label;
import com.minecolonies.blockout.views.Box;
import com.minecolonies.blockout.views.ScrollingList;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.CitizenDataView;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.network.messages.HireFireMessage;
import com.minecolonies.coremod.proxy.ClientProxy;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Window for the request detail.
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
     * Black color.
     */
    private static final int BLACK = Color.getByName("black", 0);

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
     * Id of the request detail box.
     */
    private static final String BOX_ID_REQUEST = "requestDetail";

    /**
     * The citizen of the request.
     */
    private final CitizenDataView citizen;

    /**
     * The request itself.
     */
    private final IRequest request;

    /**
     * Constructor for the window when the player wants to hire a worker for a certain job.
     *  @param c          the colony view.
     * @param request the building position.
     */
    public WindowRequestDetail(final CitizenDataView c, final IRequest request)
    {
        super(Constants.MOD_ID + BUILDING_NAME_RESOURCE_SUFFIX);
        this.citizen = c;
        this.request = request;
    }

    /**
     * Called when the GUI has been opened.
     * Will fill the fields and lists.
     */
    @Override
    public void onOpened()
    {
        final String displayString = request.getDisplayString().getFormattedText();
        final String[] labels = displayString.split("§r");
            final Box box = findPaneOfTypeByID(BOX_ID_REQUEST, Box.class);
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
            box.setSize(box.getWidth(), y);
    }

    /**
     * Called when any button has been clicked.
     *
     * @param button the clicked button.
     */
    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        if (citizen != null)
        {
            MineColonies.proxy.showCitizenWindow(citizen);
        }
    }
}
