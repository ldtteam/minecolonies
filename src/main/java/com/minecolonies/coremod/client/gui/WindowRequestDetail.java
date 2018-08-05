package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.Color;
import com.minecolonies.blockout.Log;
import com.minecolonies.blockout.controls.*;
import com.minecolonies.blockout.views.Box;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.CitizenDataView;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.ColonyView;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.minecolonies.api.util.constant.Suppression.EXCEPTION_HANDLERS_SHOULD_PRESERVE_THE_ORIGINAL_EXCEPTIONS;

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
    private static final int LIFE_COUNT_DIVIDER = 30;

    /**
     * Location string.
     */
    private static final String LIST_ELEMENT_ID_REQUEST_LOCATION = "targetLocation";

    /**
     * Y offset each text line.
     */
    private static final int Y_OFFSET_EACH_TEXTFIELD = 10;

    /**
     * Wrap after x amount of symbols the text.
     */
    //private static final int WRAP_AFTER_X = 30;

    /**
     * Resolver string.
     */
    private static final String RESOLVER = "resolver";

    /**
     * Resolver string.
     */
    private static final String DELIVERY_IMAGE = "deliveryImage";
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
     * Life count.
     */
    private int lifeCount = 0;

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

        final ItemIcon exampleStackDisplay = findPaneOfTypeByID(LIST_ELEMENT_ID_REQUEST_STACK, ItemIcon.class);
        final List<ItemStack> displayStacks = request.getDisplayStacks();

        if (!displayStacks.isEmpty())
        {
            exampleStackDisplay.setItem(displayStacks.get((lifeCount / LIFE_COUNT_DIVIDER) % displayStacks.size()));
        }
        else
        {
            exampleStackDisplay.setItem(ItemStackUtils.EMPTY);
        }
    }

    /**
     * Called when the GUI has been opened.
     * Will fill the fields and lists.
     */
    @Override
    public void onOpened()
    {
        final String[] labels = new String[] {request.getLongDisplayString().getFormattedText()};
        final Box box = findPaneOfTypeByID(BOX_ID_REQUEST, Box.class);
        int y = Y_OFFSET_EACH_TEXTFIELD;
        final int availableLabelWidth = box.getInteriorWidth() - 1 - box.getX();
        for (final String s : labels)
        {
            final String labelText = "§r§0" + s;
            // Temporary workaround until Labels support multi-line rendering
            final List<String> multilineLabelStrings = mc.fontRenderer.listFormattedStringToWidth(labelText, availableLabelWidth);
            for (final String splitLabelText : multilineLabelStrings)
            {
                final Label descriptionLabel = new Label();
                descriptionLabel.setColor(BLACK, BLACK);
                descriptionLabel.setLabelText(splitLabelText);
                box.addChild(descriptionLabel);
                descriptionLabel.setPosition(1, y);
                y += Y_OFFSET_EACH_TEXTFIELD;
            }
        }

        final ItemIcon exampleStackDisplay = findPaneOfTypeByID(LIST_ELEMENT_ID_REQUEST_STACK, ItemIcon.class);
        final List<ItemStack> displayStacks = request.getDisplayStacks();

        if (!displayStacks.isEmpty())
        {
            exampleStackDisplay.setItem(displayStacks.get((lifeCount / LIFE_COUNT_DIVIDER) % displayStacks.size()));
        }
        else
        {
            final Image logo = findPaneOfTypeByID(DELIVERY_IMAGE, Image.class);
            logo.setVisible(true);
            logo.setImage(request.getDisplayIcon());
        }

        final ColonyView view = ColonyManager.getColonyView(colonyId);
        findPaneOfTypeByID(REQUESTER, Label.class).setLabelText(request.getRequester().getDisplayName(view.getRequestManager(), request.getToken()).getFormattedText());
        final Label targetLabel = findPaneOfTypeByID(LIST_ELEMENT_ID_REQUEST_LOCATION, Label.class);
        targetLabel.setLabelText(request.getRequester().getDeliveryLocation().toString());


        final ColonyView colony = ColonyManager.getColonyView(colonyId);
        if(colony == null)
        {
            Log.getLogger().warn("---Colony Null in WindowRequestDetail---");
            return;
        }

        try
        {
            final IRequestResolver resolver = colony.getRequestManager().getResolverForRequest(request.getToken());
            if(resolver == null)
            {
                Log.getLogger().warn("---IRequestResolver Null in WindowRequestDetail---");
                return;
            }

            findPaneOfTypeByID(RESOLVER, Label.class).setLabelText("Resolver: " + resolver.getDisplayName(view.getRequestManager(), request.getToken()).getFormattedText());
        }
        catch(@SuppressWarnings(EXCEPTION_HANDLERS_SHOULD_PRESERVE_THE_ORIGINAL_EXCEPTIONS) final IllegalArgumentException e)
        {
            /**
             * Do nothing we just need to know if it has a resolver or not.
             */
            Log.getLogger().warn("---IRequestResolver Null in WindowRequestDetail---", e);
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
        else
        {
            MineColonies.proxy.openClipBoardWindow(colonyId);
        }
    }
}
