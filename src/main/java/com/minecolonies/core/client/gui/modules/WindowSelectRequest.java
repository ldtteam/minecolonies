package com.minecolonies.core.client.gui.modules;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Image;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.resolver.player.IPlayerRequestResolver;
import com.minecolonies.api.colony.requestsystem.resolver.retrying.IRetryingRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.AbstractModuleWindow;
import com.minecolonies.core.colony.requestsystem.requests.StandardRequests;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.core.colony.requestsystem.requests.AbstractRequest.MISSING;

/**
 * Window that shows existing unmatched requests compatible with the given predicate
 */
public class WindowSelectRequest extends AbstractModuleWindow
{
    private static final String RESOURCE_STRING = ":gui/layouthuts/layoutselectrequest.xml";

    private final Predicate<IRequest<?>> predicate;
    private final Consumer<IRequest<?>> reopenWithRequest;

    private final ScrollingList requestsList;
    private int lifeCount = 0;

    /**
     * Construct window.
     * @param building the building to check for requests
     * @param predicate predicate returning true if this is a selectable request
     * @param reopenWithRequest called after clicking select or cancel, with the request or null respectively.
     *                          not called if the player hits ESC or clicks a different tab
     */
    public WindowSelectRequest(final IBuildingView building,
                               final Predicate<IRequest<?>> predicate,
                               final Consumer<@Nullable IRequest<?>> reopenWithRequest)
    {
        super(building, Constants.MOD_ID + RESOURCE_STRING);
        this.predicate = predicate;
        this.reopenWithRequest = reopenWithRequest;

        this.requestsList = findPaneOfTypeByID("requests", ScrollingList.class);
        registerButton(BUTTON_SELECT, this::select);
        registerButton(BUTTON_CANCEL, this::cancel);
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        if (!Screen.hasShiftDown())
        {
            lifeCount++;
        }
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        updateRequests();
    }

    private List<IRequest<?>> getOpenRequests()
    {
        final List<IRequest<?>> requests = new ArrayList<>();

        final IRequestManager requestManager = buildingView.getColony().getRequestManager();
        final IPlayerRequestResolver resolver = requestManager.getPlayerResolver();
        final IRetryingRequestResolver retryingRequestResolver = requestManager.getRetryingRequestResolver();

        final Set<IToken<?>> requestTokens = new HashSet<>();
        requestTokens.addAll(resolver.getAllAssignedRequests());
        requestTokens.addAll(retryingRequestResolver.getAllAssignedRequests());

        for (final IToken<?> token : requestTokens)
        {
            IRequest<?> request = requestManager.getRequestForToken(token);

            while (request != null)
            {
                if (requests.contains(request))
                {
                    break;
                }

                if (predicate.test(request))
                {
                    requests.add(request);
                }

                //noinspection ConstantConditions
                request = request.hasParent() ? requestManager.getRequestForToken(request.getParent()) : null;
            }
        }

        return requests;
    }

    private void cancel()
    {
        this.reopenWithRequest.accept(null);
    }

    /**
     * When clicking the select button in the request list
     * @param button the button clicked
     */
    private void select(@NotNull final Button button)
    {
        final int row = requestsList.getListElementIndexByPane(button);
        final List<IRequest<?>> requests = getOpenRequests();

        if (row >= 0 && row < requests.size())
        {
            this.reopenWithRequest.accept(requests.get(row));
        }
    }

    /**
     * Updates request list.
     */
    private void updateRequests()
    {
        requestsList.setDataProvider(new ScrollingList.DataProvider()
        {
            private List<IRequest<?>> requests = null;

            @Override
            public int getElementCount()
            {
                requests = getOpenRequests();
                return requests.size();
            }

            @Override
            public void updateElement(final int index, final Pane rowPane)
            {
                if (index < 0 || index >= requests.size())
                {
                    return;
                }

                final IRequest<?> request = requests.get(index);
                if (request == null)
                {
                    return;
                }

                final ItemIcon exampleStackDisplay = rowPane.findPaneOfTypeByID(LIST_ELEMENT_ID_REQUEST_STACK, ItemIcon.class);
                final List<ItemStack> displayStacks = request.getDisplayStacks();
                final Image logo = rowPane.findPaneOfTypeByID(DELIVERY_IMAGE, Image.class);

                if (!displayStacks.isEmpty())
                {
                    logo.setVisible(false);
                    exampleStackDisplay.setVisible(true);
                    exampleStackDisplay.setItem(displayStacks.get((lifeCount / LIFE_COUNT_DIVIDER) % displayStacks.size()));
                    rowPane.findPaneOfTypeByID(REQUESTER, Text.class).setText(request.getRequester().getRequesterDisplayName(buildingView.getColony().getRequestManager(), request));
                }
                else
                {
                    exampleStackDisplay.setVisible(false);
                    if (!request.getDisplayIcon().equals(MISSING))
                    {
                        logo.setVisible(true);
                        logo.setImage(request.getDisplayIcon(), false);
                        PaneBuilders.tooltipBuilder().hoverPane(logo).build().setText(request.getResolverToolTip(buildingView.getColony()));
                    }
                }

                if (request instanceof StandardRequests.ItemTagRequest)
                {
                    if (!displayStacks.isEmpty())
                    {
                        rowPane.findPaneOfTypeByID(REQUEST_SHORT_DETAIL, Text.class).setText(
                                request.getDisplayStacks().get((lifeCount / LIFE_COUNT_DIVIDER) % displayStacks.size()).getHoverName());
                    }
                }
                else
                {
                    rowPane.findPaneOfTypeByID(REQUEST_SHORT_DETAIL, Text.class).setText(Component.literal(request.getShortDisplayString().getString().replace("§f", "")));
                }
            }
        });
    }
}
