package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.Color;
import com.minecolonies.blockout.controls.*;
import com.minecolonies.blockout.views.Box;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.CitizenDataView;
import com.minecolonies.coremod.colony.ColonyManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Window for the request detail.
 */
public class WindowRequestDetail extends Window implements ButtonHandler
{
    /**
     * Black color.
     */
    private static final int BLACK = Color.getByName("black", 0);

    /**
     * Link to the xml file of the window.
     */
    private static final String BUILDING_NAME_RESOURCE_SUFFIX = ":gui/windowrequestdetail.xml";

    /**
     * Id of the request detail box.
     */
    private static final String BOX_ID_REQUEST = "requestDetail";

    /**
     * Id of the requester label.
     */
    private static final String REQUESTER = "requester";

    /**
     * Requestst stack id.
     */
    private static final String LIST_ELEMENT_ID_REQUEST_STACK = "requestStack";

    /**
     * The divider for the life count.
     */
    private static final int LIFE_COUNT_DIVIDER               = 30;

    /**
     * Location string.
     */
    private static final String LIST_ELEMENT_ID_REQUEST_LOCATION = "targetLocation";

    /**
     * Resolver string.
     */
    private static final String RESOLVER                            = "resolver";

    /**
     * Resolver string.
     */
    private static final String DELIVERY_IMAGE                            = "deliveryImage";

    /**
     * Life count.
     */
    private int lifeCount = 0;

    /**
     * The citizen of the request.
     */
    private final CitizenDataView citizen;

    /**
     * The request itself.
     */
    private final IRequest request;

    /**
     * The colony id.
     */
    private final int colonyId;

    /**
     * Constructor for the window when the player wants to hire a worker for a certain job.
     *
     * @param c       the colony view.
     * @param request the building position.
     */
    public WindowRequestDetail(@Nullable final CitizenDataView c, final IRequest request, final int colonyId)
    {
        super(Constants.MOD_ID + BUILDING_NAME_RESOURCE_SUFFIX);
        this.citizen = c;
        this.request = request;
        this.colonyId = colonyId;
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        if (!GuiScreen.isShiftKeyDown())
        {
            lifeCount++;
        }
    }

    /**
     * Called when the GUI has been opened.
     * Will fill the fields and lists.
     */
    @Override
    public void onOpened()
    {
        String[] labels = request.getLongDisplayString().getFormattedText()
                .replace(":",":\n")
                .replace("§r"," ")
                .split("(?<=\n)");

        final StringBuilder finalLabel = new StringBuilder();

        for(final String s: labels)
        {
            finalLabel.append(WordUtils.wrap(s, 30, "\n", true));
        }

        labels = finalLabel.toString().split("\n");
        final Box box = findPaneOfTypeByID(BOX_ID_REQUEST, Box.class);
        int y = 10;
        for (final String s : labels)
        {
            final Label descriptionLabel = new Label();
            descriptionLabel.setColor(BLACK, BLACK);
            descriptionLabel.setLabelText(s);
            box.addChild(descriptionLabel);
            descriptionLabel.setPosition(1, y);
            y += 10;
        }

        final ItemIcon exampleStackDisplay = findPaneOfTypeByID(LIST_ELEMENT_ID_REQUEST_STACK, ItemIcon.class);
        final List<ItemStack> displayStacks = request.getDisplayStacks();

        if (!displayStacks.isEmpty())
        {
            exampleStackDisplay.setItem(displayStacks.get((lifeCount / LIFE_COUNT_DIVIDER) % displayStacks.size()));
        }
        else
        {
            findPaneOfTypeByID(DELIVERY_IMAGE, Image.class).setVisible(true);
        }

        findPaneOfTypeByID(REQUESTER, Label.class).setLabelText(request.getRequester().getDisplayName(request.getToken()).getFormattedText());
        final Label targetLabel = findPaneOfTypeByID(LIST_ELEMENT_ID_REQUEST_LOCATION, Label.class);
        targetLabel.setLabelText(request.getRequester().getDeliveryLocation().toString());

        ColonyManager.getColony(colonyId).getRequestManager().

        findPaneOfTypeByID(RESOLVER, Label.class).setLabelText(.getResolverFromRequest(request.getToken));

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
        else
        {
            MineColonies.proxy.openClipBoardWindow(colonyId);

        }
    }
}
