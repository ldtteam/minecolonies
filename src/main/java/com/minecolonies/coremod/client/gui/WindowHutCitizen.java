package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.Label;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingHome;
import com.minecolonies.coremod.network.messages.server.colony.building.home.AssignUnassignMessage;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_HOME_ASSIGN;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_LEVEL_0;

/**
 * Window for the home building.
 */
public class WindowHutCitizen extends AbstractWindowBuilding<BuildingHome.View>
{
    /**
     * Id of the hire/fire button in the GUI.
     */
    private static final String BUTTON_ASSIGN = "assign";

    /**
     * Id of the hire/fire button in the GUI.
     */
    private static final String BUTTON_REMOVE = "remove";

    /**
     * Suffix describing the window xml.
     */
    private static final String HOME_BUILDING_RESOURCE_SUFFIX = ":gui/windowhuthome.xml";

    /**
     * Id to identify the list of the citizen in the view.
     */
    private static final String LIST_CITIZEN = "assignedCitizen";
    /**
     * The building the view is relates to.
     */
    private final BuildingHome.View home;
    /**
     * The list of citizen assigned to this hut.
     */
    private ScrollingList citizen;

    /**
     * Creates the Window object.
     *
     * @param building View of the home building.
     */
    public WindowHutCitizen(final BuildingHome.View building)
    {
        super(building, Constants.MOD_ID + HOME_BUILDING_RESOURCE_SUFFIX);

        super.registerButton(BUTTON_ASSIGN, this::assignClicked);
        super.registerButton(BUTTON_REMOVE, this::removeClicked);
        this.home = building;
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        final boolean isManualHousing = building.getColony().isManualHousing();
        citizen = findPaneOfTypeByID(LIST_CITIZEN, ScrollingList.class);
        citizen.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return home.getResidents().size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final ICitizenDataView citizenDataView = home.getColony().getCitizen((home.getResidents().get(index)));
                if (citizenDataView != null)
                {
                    rowPane.findPaneOfTypeByID("name", Label.class).setLabelText(citizenDataView.getName());
                    rowPane.findPaneOfTypeByID(BUTTON_REMOVE, Button.class).setEnabled(isManualHousing);
                }
            }
        });

        refreshView();
    }

    /**
     * Refresh the view.
     */
    private void refreshView()
    {
        final Button buttonAssign = findPaneOfTypeByID(BUTTON_ASSIGN, Button.class);

        final int sparePlaces = building.getBuildingLevel() - building.getResidents().size();
        buttonAssign.setLabel(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HOME_ASSIGN, sparePlaces));
        buttonAssign.setEnabled(sparePlaces > 0 && building.getColony().isManualHousing());

        citizen.refreshElementPanes();
    }

    /**
     * Action when an assign button is clicked.
     */
    private void assignClicked()
    {
        if (building.getColony().isManualHousing())
        {
            if (building.getBuildingLevel() == 0)
            {
                LanguageHandler.sendPlayerMessage(Minecraft.getInstance().player, COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_LEVEL_0);
                return;
            }

            if (building.getResidents().size() < building.getBuildingLevel())
            {
                @NotNull final WindowAssignCitizen window = new WindowAssignCitizen(building.getColony(), building.getPosition());
                window.open();
            }
        }
    }

    /**
     * Action when the remove button is clicked.
     *
     * @param button the clicked button.
     */
    private void removeClicked(@NotNull final Button button)
    {
        if (building.getColony().isManualHousing())
        {
            final int row = citizen.getListElementIndexByPane(button);
            final int citizenid = home.getResidents().get(row);
            home.removeResident(row);
            Network.getNetwork().sendToServer(new AssignUnassignMessage(building, false, citizenid));
            refreshView();
        }
    }

    /**
     * Returns the name of a building.
     *
     * @return Name of a building.
     */
    @NotNull
    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerhuts.homehut";
    }
}
