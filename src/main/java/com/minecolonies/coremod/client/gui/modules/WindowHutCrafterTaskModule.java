package com.minecolonies.coremod.client.gui.modules;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Image;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.deliveryman.IDeliverymanRequestable;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.coremod.client.gui.AbstractModuleWindow;
import com.minecolonies.coremod.colony.buildings.moduleviews.WorkerBuildingModuleView;
import com.minecolonies.coremod.colony.jobs.views.CrafterJobView;
import com.minecolonies.coremod.colony.jobs.views.DmanJobView;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_ENTITY_DELIVERYMAN_PRIORITY;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Task list module.
 */
public class WindowHutCrafterTaskModule extends AbstractModuleWindow
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
    public WindowHutCrafterTaskModule(final IBuildingView view, final String name)
    {
        super(view, name);
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        final List<IToken<?>> tasks = new ArrayList<>();


        for (final WorkerBuildingModuleView moduleView : buildingView.getModuleViews(WorkerBuildingModuleView.class))
        {
            for (final int citizenId : moduleView.getAssignedCitizens())
            {
                ICitizenDataView citizen = buildingView.getColony().getCitizen(citizenId);
                if (citizen != null)
                {
                    if (citizen.getJobView() instanceof CrafterJobView)
                    {
                        tasks.addAll(((CrafterJobView) citizen.getJobView()).getDataStore().getQueue());
                    }
                    else if (citizen.getJobView() instanceof DmanJobView)
                    {
                        tasks.addAll(((DmanJobView) citizen.getJobView()).getDataStore().getQueue());
                    }
                }
            }
        }

        final ScrollingList deliveryList = findPaneOfTypeByID(LIST_TASKS, ScrollingList.class);
        deliveryList.setDataProvider(new ScrollingList.DataProvider()
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

                final IRequest<?> parent = buildingView.getColony().getRequestManager().getRequestForToken(request.getParent());

                if (parent != null)
                {
                    rowPane.findPaneOfTypeByID(REQUESTER, Text.class)
                      .setText(request.getRequester().getRequesterDisplayName(buildingView.getColony().getRequestManager(), request).getString() + " ->");
                    rowPane.findPaneOfTypeByID(PARENT, Text.class)
                      .setText(parent.getRequester().getRequesterDisplayName(buildingView.getColony().getRequestManager(), parent));
                }
                else
                {
                    rowPane.findPaneOfTypeByID(REQUESTER, Text.class)
                      .setText(request.getRequester().getRequesterDisplayName(buildingView.getColony().getRequestManager(), request));
                    rowPane.findPaneOfTypeByID(PARENT, Text.class).clearText();
                }

                rowPane.findPaneOfTypeByID(REQUEST_SHORT_DETAIL, Text.class)
                  .setText(request.getShortDisplayString().getString().replace("§f", ""));

                if (request.getRequest() instanceof IDeliverymanRequestable)
                {
                    rowPane.findPaneOfTypeByID(REQUEST_PRIORITY, Text.class)
                      .setText(
                        LanguageHandler.format(COM_MINECOLONIES_COREMOD_ENTITY_DELIVERYMAN_PRIORITY) + ((IDeliverymanRequestable) (request.getRequest())).getPriority());
                }

                final Image logo = rowPane.findPaneOfTypeByID(DELIVERY_IMAGE, Image.class);
                logo.setImage(request.getDisplayIcon());
            }
        });
    }
}
