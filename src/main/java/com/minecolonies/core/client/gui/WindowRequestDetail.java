package com.minecolonies.core.client.gui;

import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IStackBasedTask;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.modules.RequestTreeWindowModule;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.core.colony.requestsystem.requests.AbstractRequest.MISSING;

/**
 * BOWindow for the request detail.
 */
public class WindowRequestDetail extends AbstractWindowSkeleton implements ButtonHandler
{
    public static final ResourceLocation WINDOW_ID = new ResourceLocation(Constants.MOD_ID, "gui/windowrequestdetail.xml");

    /**
     * ID of the requester label.
     */
    private static final String REQUESTER = "requester";

    /**
     * Requests stack id.
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
    private final int colonyId;

    /**
     * The request tree window module.
     */
    private final RequestTreeWindowModule requestTreeWindowModule;

    /**
     * Life count.
     */
    private int lifeCount = 0;

    /**
     * Open the request detail.
     *
     * @param parent                  the window we're coming from.
     * @param request                 the request.
     * @param colonyId                the colony id.
     * @param requestTreeWindowModule the request tree module.
     */
    public WindowRequestDetail(@Nullable final BOWindow parent, final IRequest<?> request, final int colonyId, final RequestTreeWindowModule requestTreeWindowModule)
    {
        super(parent, WINDOW_ID);
        this.request = request;
        this.colonyId = colonyId;
        this.requestTreeWindowModule = requestTreeWindowModule;
    }

    /**
     * Called when the GUI has been opened. Will fill the fields and lists.
     */
    @Override
    public void onOpened()
    {
        if (request instanceof IStackBasedTask)
        {
            final ItemIcon icon = findPaneOfTypeByID("detailIcon", ItemIcon.class);
            final ItemStack copyStack = ((IStackBasedTask) request).getTaskStack().copy();
            copyStack.setCount(((IStackBasedTask) request).getDisplayCount());
            icon.setItem(copyStack);
            icon.setVisible(true);
            findPaneOfTypeByID(REQUEST_SHORT_DETAIL, Text.class).setText(((IStackBasedTask) request).getDisplayPrefix().withStyle(ChatFormatting.BLACK));
        }
        else
        {
            findPaneOfTypeByID("detailIcon", ItemIcon.class).setVisible(false);
            findPaneOfTypeByID(REQUEST_SHORT_DETAIL, Text.class).setText(Component.literal(request.getLongDisplayString().getString().replace("§f", ""))
                .withStyle(ChatFormatting.BLACK));
        }

        final Image logo = findPaneOfTypeByID(DELIVERY_IMAGE, Image.class);

        final ItemIcon exampleStackDisplay = findPaneOfTypeByID(LIST_ELEMENT_ID_REQUEST_STACK, ItemIcon.class);
        final List<ItemStack> displayStacks = request.getDisplayStacks();
        final IColonyView colony = IColonyManager.getInstance().getColonyView(colonyId, Minecraft.getInstance().level.dimension());

        if (!displayStacks.isEmpty())
        {
            exampleStackDisplay.setItem(displayStacks.get((lifeCount / LIFE_COUNT_DIVIDER) % displayStacks.size()));
        }
        else if (!request.getDisplayIcon().equals(MISSING))
        {
            logo.setVisible(true);
            logo.setImage(request.getDisplayIcon(), false);
            PaneBuilders.tooltipBuilder().hoverPane(logo).build().setText(request.getResolverToolTip(colony));
        }

        findPaneOfTypeByID(REQUESTER, Text.class).setText(request.getRequester().getRequesterDisplayName(colony.getRequestManager(), request));
        findPaneOfTypeByID(LIST_ELEMENT_ID_REQUEST_LOCATION, Text.class).setText(Component.literal(request.getRequester().getLocation().toString()));

        try
        {
            final IRequestResolver<?> resolver = colony.getRequestManager().getResolverForRequest(request.getId());
            if (resolver == null)
            {
                Log.getLogger().warn("---IRequestResolver Null in WindowRequestDetail---");
                return;
            }

            findPaneOfTypeByID(RESOLVER, Text.class).setText(Component.literal("Resolver: " + resolver.getRequesterDisplayName(colony.getRequestManager(), request).getString()));
        }
        catch (final IllegalArgumentException e)
        {
            Log.getLogger().warn("---IRequestResolver Null in WindowRequestDetail---", e);
        }

        findPaneOfTypeByID(REQUEST_FULFILL, ButtonImage.class).setEnabled(requestTreeWindowModule.isFulfillable(request));
        findPaneOfTypeByID(REQUEST_CANCEL, ButtonImage.class).setEnabled(requestTreeWindowModule.isCancellable(request));
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
     * Called when any button has been clicked.
     *
     * @param button the clicked button.
     */
    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        if (button.getID().equals(REQUEST_FULFILL))
        {
            if (requestTreeWindowModule instanceof final RequestTreeWindowModule.IRequestTreeSupportsFulfill requestTreeSupportsFulfill)
            {
                requestTreeSupportsFulfill.onFulfill(request);
            }
            this.window.close();
        }
        else if (button.getID().equals(REQUEST_CANCEL))
        {
            requestTreeWindowModule.cancel(request);
            this.window.close();
        }
    }
}
