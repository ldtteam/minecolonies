package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneBuilders;
import com.ldtteam.blockout.controls.*;
import com.ldtteam.blockout.views.Box;
import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.gui.citizen.RequestWindowCitizen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.minecolonies.api.util.constant.Suppression.EXCEPTION_HANDLERS_SHOULD_PRESERVE_THE_ORIGINAL_EXCEPTIONS;
import static com.minecolonies.api.util.constant.WindowConstants.REQUEST_CANCEL;
import static com.minecolonies.api.util.constant.WindowConstants.REQUEST_FULLFIL;
import static com.minecolonies.coremod.colony.requestsystem.requests.AbstractRequest.MISSING;

/**
 * Window for the request detail.
 */
public class WindowRequestDetail extends Window implements ButtonHandler
{
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
     * Resolver string.
     */
    private static final String RESOLVER = "resolver";

    /**
     * A Resolver string.
     */
    private static final String DELIVERY_IMAGE = "deliveryImage";

    /**
     * The request itself.
     */
    private final IRequest<?> request;
    /**
     * The colony id.
     */
    private final int         colonyId;
    /**
     * Life count.
     */
    private       int         lifeCount = 0;

    /**
     * The previous window.
     */
    private final Window prevWindow;

    /**
     * Open the request detail.
     *
     * @param prevWindow the window we're coming from.
     * @param request    the request.
     * @param colonyId   the colony id.
     */
    public WindowRequestDetail(@Nullable final Window prevWindow, final IRequest<?> request, final int colonyId)
    {
        super(Constants.MOD_ID + BUILDING_NAME_RESOURCE_SUFFIX);
        this.prevWindow = prevWindow;
        this.request = request;
        this.colonyId = colonyId;
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        if (!Screen.hasShiftDown())
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
     * Called when the GUI has been opened. Will fill the fields and lists.
     */
    @Override
    public void onOpened()
    {
        final Box box = findPaneOfTypeByID(BOX_ID_REQUEST, Box.class);
        final Text description = PaneBuilders.textBuilder()
            .style(TextFormatting.fromFormattingCode('r'))
            .style(TextFormatting.fromFormattingCode('0'))
            .append(request.getLongDisplayString())
            .build();
        description.setPosition(1, 1);
        description.setSize(box.getWidth() - 2, AbstractTextElement.SIZE_FOR_UNLIMITED_ELEMENTS);

        box.addChild(description);
        box.setSize(box.getWidth(), description.getRenderedTextHeight() + 2);
        description.setSize(box.getWidth() - 2, box.getHeight());

        final Image logo = findPaneOfTypeByID(DELIVERY_IMAGE, Image.class);

        final ItemIcon exampleStackDisplay = findPaneOfTypeByID(LIST_ELEMENT_ID_REQUEST_STACK, ItemIcon.class);
        final List<ItemStack> displayStacks = request.getDisplayStacks();
        final IColonyView colony = IColonyManager.getInstance().getColonyView(colonyId, Minecraft.getInstance().world.getDimensionKey());

        if (!displayStacks.isEmpty())
        {
            exampleStackDisplay.setItem(displayStacks.get((lifeCount / LIFE_COUNT_DIVIDER) % displayStacks.size()));
        }
        else if (!request.getDisplayIcon().equals(MISSING))
        {
            logo.setVisible(true);
            logo.setImage(request.getDisplayIcon());
            PaneBuilders.tooltipBuilder().hoverPane(logo).build().setText(request.getResolverToolTip(colony));
        }

        findPaneOfTypeByID(REQUESTER, Text.class).setText(request.getRequester().getRequesterDisplayName(colony.getRequestManager(), request));
        findPaneOfTypeByID(LIST_ELEMENT_ID_REQUEST_LOCATION, Text.class).setText(request.getRequester().getLocation().toString());

        if (colony == null)
        {
            Log.getLogger().warn("---Colony Null in WindowRequestDetail---");
            return;
        }

        try
        {
            final IRequestResolver<?> resolver = colony.getRequestManager().getResolverForRequest(request.getId());
            if (resolver == null)
            {
                Log.getLogger().warn("---IRequestResolver Null in WindowRequestDetail---");
                return;
            }

            findPaneOfTypeByID(RESOLVER, Text.class).setText("Resolver: " + resolver.getRequesterDisplayName(colony.getRequestManager(), request).getString());
        }
        catch (@SuppressWarnings(EXCEPTION_HANDLERS_SHOULD_PRESERVE_THE_ORIGINAL_EXCEPTIONS) final IllegalArgumentException e)
        {
            /*
             * Do nothing we just need to know if it has a resolver or not.
             */
            Log.getLogger().warn("---IRequestResolver Null in WindowRequestDetail---", e);
        }

        //Checks if fulfill button should be displayed
        Pane fulfillButton = this.window.getChildren().stream().filter(pane -> pane.getID().equals(REQUEST_FULLFIL)).findFirst().get();
        if ((this.prevWindow instanceof RequestWindowCitizen && !((RequestWindowCitizen) prevWindow).fulfillable(request)) || this.prevWindow instanceof WindowClipBoard)
        {
            fulfillButton.hide();
        }
        //Checks if cancel button should be displayed
        Pane cancelButton = this.window.getChildren().stream().filter(pane -> pane.getID().equals(REQUEST_CANCEL)).findFirst().get();
        if (this.prevWindow instanceof RequestWindowCitizen && !((RequestWindowCitizen) prevWindow).cancellable(request))
        {
            cancelButton.hide();
        }
    }

    /**
     * Called when any button has been clicked.
     *
     * @param button the clicked button.
     */
    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        if (button.getID().equals(REQUEST_FULLFIL))
        {
            if (this.prevWindow instanceof RequestWindowCitizen)
            {
                ((RequestWindowCitizen) this.prevWindow).fulfill(request);
            }
            this.window.close();
        }
        else if (button.getID().equals(REQUEST_CANCEL))
        {
            if (this.prevWindow instanceof RequestWindowCitizen)
            {
                ((RequestWindowCitizen) this.prevWindow).cancel(request);
            }
            this.window.close();
        }
        else
        {
            prevWindow.open();
        }
    }
}
