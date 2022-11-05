package com.minecolonies.coremod.client.gui.modules;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Image;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.gui.AbstractModuleWindow;
import com.minecolonies.coremod.colony.requestsystem.requests.StandardRequests;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.coremod.colony.requestsystem.requests.AbstractRequest.MISSING;

/**
 * Window that shows existing unmatched requests compatible with the given predicate
 */
public class WindowSelectRequest extends AbstractModuleWindow
{
    private static final String RESOURCE_STRING = ":gui/layouthuts/layoutselectrequest.xml";

    private final List<IToken<?>> requestIds;
    private final Consumer<IToken<?>> reopenWithRequest;

    private final ScrollingList requestsList;
    private int lifeCount = 0;

    /**
     * Construct window.
     * @param building the building to check for requests
     * @param requestIds the ids of requests to be displayed
     * @param reopenWithRequest called after clicking select or cancel, with the request or null respectively.
     *                          not called if the player hits ESC or clicks a different tab
     */
    public WindowSelectRequest(final IBuildingView building,
                               final List<IToken<?>> requestIds,
                               final Consumer<@Nullable IToken<?>> reopenWithRequest)
    {
        super(building, Constants.MOD_ID + RESOURCE_STRING);
        this.requestIds = requestIds;
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

        if (row >= 0 && row < requestIds.size())
        {
            this.reopenWithRequest.accept(requestIds.get(row));
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
                if (requests == null)
                {
                    requests = new ArrayList<>();
                    for (final IToken<?> requestId : requestIds)
                    {
                        requests.add(buildingView.getColony().getRequestManager().getRequestForToken(requestId));
                    }
                }
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
