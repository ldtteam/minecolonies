package com.minecolonies.coremod.client.gui.huts;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.AbstractWindowModuleBuilding;
import com.minecolonies.coremod.client.gui.WindowAssignCitizen;
import com.minecolonies.coremod.colony.buildings.modules.HomeBuildingModule;
import com.minecolonies.coremod.network.messages.server.colony.building.RecallCitizenHutMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.home.AssignUnassignMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Window for the home building.
 */
public class WindowHutCitizen extends AbstractWindowModuleBuilding<HomeBuildingModule.View>
{
    /**
     * Suffix describing the window xml.
     */
    private static final String HOME_BUILDING_RESOURCE_SUFFIX = ":gui/windowhuthome.xml";

    /**
     * The building the view is relates to.
     */
    private final HomeBuildingModule.View home;
    /**
     * The list of citizen assigned to this hut.
     */
    private       ScrollingList           citizen;

    /**
     * Creates the Window object.
     *
     * @param building View of the home building.
     */
    public WindowHutCitizen(final HomeBuildingModule.View building)
    {
        super(building, Constants.MOD_ID + HOME_BUILDING_RESOURCE_SUFFIX);

        super.registerButton(BUTTON_ASSIGN, this::assignClicked);
        super.registerButton(BUTTON_REMOVE, this::removeClicked);
        super.registerButton(BUTTON_RECALL, this::recallClicked);

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
                    rowPane.findPaneOfTypeByID(LABEL_NAME, Text.class).setText(citizenDataView.getName());
                    rowPane.findPaneOfTypeByID(BUTTON_REMOVE, Button.class).setEnabled(isManualHousing);

                    if (citizenDataView.getWorkBuilding() != null)
                    {
                        final BlockPos work = citizenDataView.getWorkBuilding();
                        final double distance2D = BlockPosUtil.getDistance2D(work, home.getPosition());
                        rowPane.findPaneOfTypeByID(LABEL_DIST, Text.class).setText(LanguageHandler.format(DIST, distance2D));
                    }
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
        buttonAssign.setText(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HOME_ASSIGN, sparePlaces));
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
     * On recall clicked.
     */
    private void recallClicked()
    {
        Network.getNetwork().sendToServer(new RecallCitizenHutMessage(building));
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
