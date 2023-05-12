package com.minecolonies.coremod.client.gui.huts;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.AbstractWindowModuleBuilding;
import com.minecolonies.coremod.client.gui.WindowAssignCitizen;
import com.minecolonies.coremod.colony.buildings.views.LivingBuildingView;
import com.minecolonies.coremod.network.messages.server.colony.building.RecallCitizenHutMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_LEVEL_0;
import static com.minecolonies.api.util.constant.TranslationConstants.LABEL_HOUSE_ASSIGNED_CITIZENS;
import static com.minecolonies.api.util.constant.WindowConstants.BUTTON_RECALL;

/**
 * BOWindow for the tavern
 */
public class WindowHutLiving extends AbstractWindowModuleBuilding<LivingBuildingView>
{
    /**
     * Id of the hire/fire button in the GUI.
     */
    private static final String BUTTON_ASSIGN = "assign";

    /**
     * Label showing the assigned.
     */
    private static final String ASSIGNED_LABEL = "assignedlabel";

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
    private final LivingBuildingView home;

    /**
     * The list of citizen assigned to this hut.
     */
    private ScrollingList citizen;

    /**
     * Creates the BOWindow object.
     *
     * @param building View of the home building.
     */
    public WindowHutLiving(final LivingBuildingView building)
    {
        super(building, Constants.MOD_ID + HOME_BUILDING_RESOURCE_SUFFIX);

        super.registerButton(BUTTON_ASSIGN, this::assignClicked);
        super.registerButton(BUTTON_RECALL, this::recallClicked);

        this.home = building;
    }

    /**
     * On recall clicked.
     */
    private void recallClicked()
    {
        Network.getNetwork().sendToServer(new RecallCitizenHutMessage(building));
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
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
                final ICitizenDataView citizenDataView = home.getColony().getCitizen(home.getResidents().get(index));
                if (citizenDataView != null)
                {
                    rowPane.findPaneOfTypeByID("name", Text.class).setText(Component.literal((citizenDataView.getJob().isEmpty() ? "" : (Component.translatable(citizenDataView.getJob()).getString() + ": ")) + citizenDataView.getName()));
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
        findPaneOfTypeByID(ASSIGNED_LABEL, Text.class).setText(Component.translatable(LABEL_HOUSE_ASSIGNED_CITIZENS, building.getResidents().size(), building.getMax()));
        citizen.refreshElementPanes();
    }

    /**
     * Action when an assign button is clicked.
     */
    private void assignClicked()
    {
        if (building.getBuildingLevel() == 0)
        {
            MessageUtils.format(COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_LEVEL_0).sendTo(Minecraft.getInstance().player);
            return;
        }

        @NotNull final WindowAssignCitizen window = new WindowAssignCitizen(building.getColony(), building);
        window.open();
    }
}
