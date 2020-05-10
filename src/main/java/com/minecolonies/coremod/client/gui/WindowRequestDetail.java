package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Color;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.*;
import com.ldtteam.blockout.views.Box;
import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.Suppression.EXCEPTION_HANDLERS_SHOULD_PRESERVE_THE_ORIGINAL_EXCEPTIONS;
import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.REQUEST_SHORT_DETAIL;

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
     * The previous window.
     */
    private final Window prevWindow;

    /**
     * Player inventory
     */
    private final PlayerInventory inventory  = this.mc.player.inventory;
    /**
     * Is the player in creative or not.
     */
    private final boolean         isCreative = this.mc.player.isCreative();

    /**
     * Open the request detail.
     *
     * @param prevWindow the window we're coming from.
     * @param request    the request.
     * @param colonyId   the colony id.
     */
    public WindowRequestDetail(@Nullable final Window prevWindow, final IRequest request, final int colonyId)
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
        final String[] labels = new String[] {request.getLongDisplayString().getFormattedText()};
        final Box box = findPaneOfTypeByID(BOX_ID_REQUEST, Box.class);
        int y = Y_OFFSET_EACH_TEXTFIELD;
        final int availableLabelWidth = box.getInteriorWidth() - 1 - box.getX();

        //Checks if fulfill button should be displayed
        Pane fulfillButton = this.window.getChildren().stream().filter(pane -> pane.getID().equals(REQUEST_FULLFIL)).findFirst().get();
        if (this.prevWindow instanceof WindowCitizen && !((WindowCitizen) prevWindow).fulfillable(request))
        {
            fulfillButton.hide();
        }

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

        final IColonyView view = IColonyManager.getInstance().getColonyView(colonyId, Minecraft.getInstance().world.getDimension().getType().getId());
        findPaneOfTypeByID(REQUESTER, Label.class).setLabelText(request.getRequester().getRequesterDisplayName(view.getRequestManager(), request).getFormattedText());
        final Label targetLabel = findPaneOfTypeByID(LIST_ELEMENT_ID_REQUEST_LOCATION, Label.class);
        targetLabel.setLabelText(request.getRequester().getLocation().toString());


        final IColonyView colony = IColonyManager.getInstance().getColonyView(colonyId, Minecraft.getInstance().world.getDimension().getType().getId());
        if (colony == null)
        {
            Log.getLogger().warn("---Colony Null in WindowRequestDetail---");
            return;
        }

        try
        {
            final IRequestResolver resolver = colony.getRequestManager().getResolverForRequest(request.getId());
            if (resolver == null)
            {
                Log.getLogger().warn("---IRequestResolver Null in WindowRequestDetail---");
                return;
            }

            findPaneOfTypeByID(RESOLVER, Label.class).setLabelText("Resolver: " + resolver.getRequesterDisplayName(view.getRequestManager(), request).getFormattedText());
        }
        catch (@SuppressWarnings(EXCEPTION_HANDLERS_SHOULD_PRESERVE_THE_ORIGINAL_EXCEPTIONS) final IllegalArgumentException e)
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
        if (button.getID().equals(REQUEST_FULLFIL))
        {
            if (this.prevWindow instanceof WindowCitizen)
            {
                ((WindowCitizen) this.prevWindow).fulfill(request);
            }
            this.window.close();
        }
        else if (button.getID().equals(REQUEST_CANCEL))
        {
            if (this.prevWindow instanceof WindowCitizen)
            {
                ((WindowCitizen) this.prevWindow).cancel(request);
            }
            this.window.close();
        }
        else
        {
            prevWindow.open();
        }
    }
}
