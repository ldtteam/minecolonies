package com.minecolonies.coremod.client.gui.modules;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.ScrollingList;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.buildings.modules.IAssignmentModuleView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.AbstractModuleWindow;
import com.minecolonies.coremod.client.gui.WindowHireWorker;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.network.messages.server.colony.building.worker.RecallCitizenMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_LEVEL_0;
import static com.minecolonies.api.util.constant.WindowConstants.BUTTON_RECALL;

/**
 * Assignment module for workers to a building.
 * This is specifically for the assignment of workers that got their own hut and are assigned additionally to this building (e.g., warehouse, quarry).
 */
public class SpecialAssignmentModuleWindow extends AbstractModuleWindow
{
    /**
     * Id of the hire/fire button in the GUI.
     */
    private static final String BUTTON_HIRE = "hire";

    /**
     * Id of the scroll view
     */
    private static final String LIST_WORKERS = "workers";

    /**
     * Id of the name label in the GUI.
     */
    private static final String LABEL_WORKERNAME = "workerName";

    /**
     * Constructor for the window of the worker building.
     *
     * @param building class extending {@link AbstractBuildingView}.
     * @param resource Resource of the window.
     */
    public SpecialAssignmentModuleWindow(final IBuildingView building, final String resource)
    {
        super(building, resource);
        super.registerButton(BUTTON_HIRE, this::hireClicked);
        super.registerButton(BUTTON_RECALL, this::recallClicked);
    }

    /**
     * On recall clicked.
     */
    private void recallClicked()
    {
        Network.getNetwork().sendToServer(new RecallCitizenMessage(buildingView));
    }

    /**
     * Action when a hire button is clicked. If there is no worker (worker.Id == 0) then Contract someone. Else then Fire the current worker.
     *
     * @param button the clicked button.
     */
    protected void hireClicked(@NotNull final Button button)
    {
        if (buildingView.getBuildingLevel() == 0)
        {
            MessageUtils.format(COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_LEVEL_0).sendTo(Minecraft.getInstance().player);
            return;
        }

        @NotNull final WindowHireWorker window = new WindowHireWorker(buildingView.getColony(), buildingView.getPosition());
        window.open();
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        final List<Tuple<String, Integer>> workers = new ArrayList<>();

        for (final IAssignmentModuleView module : buildingView.getModuleViews(IAssignmentModuleView.class))
        {
            for (final int worker : module.getAssignedCitizens())
            {
                workers.add(new Tuple<>(new TranslationTextComponent(module.getJobEntry().getTranslationKey()).getString(), worker));
            }
        }

        if (findPaneByID(LIST_WORKERS) != null)
        {
            ScrollingList workerList = findPaneOfTypeByID(LIST_WORKERS, ScrollingList.class);
            workerList.setDataProvider(new ScrollingList.DataProvider()
            {
                @Override
                public int getElementCount()
                {
                    return workers.size();
                }

                @Override
                public void updateElement(final int index, @NotNull final Pane rowPane)
                {

                    final ICitizenDataView worker = buildingView.getColony().getCitizen(workers.get(index).getB());
                    if (worker != null)
                    {
                        rowPane.findPaneOfTypeByID(LABEL_WORKERNAME, Text.class)
                          .setText(new TranslationTextComponent(workers.get(index).getA()).getString() + ": " + worker.getName());
                    }
                }
            });
        }
    }
}
