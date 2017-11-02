package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.Color;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.ButtonHandler;
import com.minecolonies.blockout.controls.Label;
import com.minecolonies.blockout.views.Box;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.CitizenDataView;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Window for the request detail.
 */
public class WindowRequestDetail extends Window implements ButtonHandler
{
    /**
     * Black color.
     */
    private static final int WHITE = Color.getByName("white", 0);

    /**
     * Link to the xml file of the window.
     */
    private static final String BUILDING_NAME_RESOURCE_SUFFIX = ":gui/windowrequestdetail.xml";

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
     *
     * @param c       the colony view.
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
        for (final String s : labels)
        {
            final Label descriptionLabel = new Label();
            descriptionLabel.setColor(WHITE, WHITE);
            descriptionLabel.setLabelText(s);
            box.addChild(descriptionLabel);
            descriptionLabel.setPosition(20, y);
            y += 10;
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
