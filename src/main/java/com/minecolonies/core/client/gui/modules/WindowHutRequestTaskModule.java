package com.minecolonies.core.client.gui.modules;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.Image;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IStackBasedTask;
import com.minecolonies.api.colony.requestsystem.requestable.deliveryman.IDeliverymanRequestable;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.core.client.gui.AbstractModuleWindow;
import com.minecolonies.core.colony.buildings.moduleviews.RequestTaskModuleView;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_ENTITY_DELIVERYMAN_PRIORITY;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Task list module.
 */
public class WindowHutRequestTaskModule extends AbstractModuleWindow
{
    /**
     * Id of the the task list inside the GUI.
     */
    private static final String LIST_TASKS = "tasks";

    /**
     * The constructor of the window.
     * @param view the building view.
     * @param name the layout file.
     */
    public WindowHutRequestTaskModule(final IBuildingView view, final String name)
    {
        super(view, name);
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        final List<IToken<?>> tasks =  buildingView.getModuleViewByType(RequestTaskModuleView.class).getTasks();
        findPaneOfTypeByID(LIST_TASKS, ScrollingList.class).setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                tasks.removeIf(token -> buildingView.getColony().getRequestManager().getRequestForToken(token) == null);
                return tasks.size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final IRequest<?> request = buildingView.getColony().getRequestManager().getRequestForToken(tasks.get(index));

                IRequest<?> parent = buildingView.getColony().getRequestManager().getRequestForToken(request.getParent());

                while (parent != null && parent.getRequester().getLocation().equals(request.getRequester().getLocation()))
                {
                    final IRequest<?> tempParent = buildingView.getColony().getRequestManager().getRequestForToken(parent.getParent());
                    if (tempParent != null)
                    {
                        parent = tempParent;
                    }
                    else
                    {
                        break;
                    }
                }

                if (parent == null)
                {
                    rowPane.findPaneOfTypeByID(REQUESTER, Text.class).setText(request.getRequester().getRequesterDisplayName(buildingView.getColony().getRequestManager(), request));
                }
                else
                {
                    rowPane.findPaneOfTypeByID(REQUESTER, Text.class)
                      .setText(Component.literal(request.getRequester().getRequesterDisplayName(buildingView.getColony().getRequestManager(), request).getString() + " -> " + parent.getRequester().getRequesterDisplayName(buildingView.getColony().getRequestManager(), parent).getString()));
                    PaneBuilders.tooltipBuilder().hoverPane(rowPane.findPaneOfTypeByID(REQUESTER, Text.class))
                      .build().setText(Component.literal(request.getRequester().getLocation().getInDimensionLocation().toShortString() + " -> " + parent.getRequester().getLocation().getInDimensionLocation().toShortString()));

                }

                // Add an extra thing with an Interface about having a stack to display, if we have this, then also add a method for a shorter string.
                if (request instanceof IStackBasedTask)
                {
                    final ItemIcon icon = rowPane.findPaneOfTypeByID("detailIcon", ItemIcon.class);
                    final ItemStack copyStack = ((IStackBasedTask) request).getTaskStack().copy();
                    copyStack.setCount(((IStackBasedTask) request).getDisplayCount());
                    icon.setItem(copyStack);
                    icon.setVisible(true);
                    rowPane.findPaneOfTypeByID(REQUEST_SHORT_DETAIL, Text.class).setText(((IStackBasedTask) request).getDisplayPrefix().withStyle(request.getState() == RequestState.IN_PROGRESS ? ChatFormatting.DARK_GREEN : ChatFormatting.BLACK));
                }
                else
                {
                    rowPane.findPaneOfTypeByID("detailIcon", ItemIcon.class).setVisible(false);
                    rowPane.findPaneOfTypeByID(REQUEST_SHORT_DETAIL, Text.class).setText(Component.literal(request.getShortDisplayString().getString().replace("§f", "")).withStyle(request.getState() == RequestState.IN_PROGRESS ? ChatFormatting.DARK_GREEN : ChatFormatting.BLACK));
                }

                if (request.getRequest() instanceof IDeliverymanRequestable)
                {
                    rowPane.findPaneOfTypeByID(REQUEST_PRIORITY, Text.class).setText(Component.translatable(COM_MINECOLONIES_COREMOD_ENTITY_DELIVERYMAN_PRIORITY)
                                                                                       .append(String.valueOf(((IDeliverymanRequestable) request.getRequest()).getPriority())));
                }

                final Image logo = rowPane.findPaneOfTypeByID(DELIVERY_IMAGE, Image.class);
                logo.setImage(request.getDisplayIcon(), false);
            }
        });
    }
}
