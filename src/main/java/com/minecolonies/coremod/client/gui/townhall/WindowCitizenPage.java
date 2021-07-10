package com.minecolonies.coremod.client.gui.townhall;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.*;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizen;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.citizen.CitizenWindowUtils;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.coremod.network.messages.server.colony.citizen.RecallSingleCitizenMessage;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Window for the town hall.
 */
@SuppressWarnings("PMD.ExcessiveClassLength")
public class WindowCitizenPage extends AbstractWindowTownHall
{
    /**
     * Citizen name comparator.
     */
    private static final Comparator<ICitizenDataView> COMPARE_BY_NAME = Comparator.comparing(ICitizen::getName);

    /**
     * List of citizens.
     */
    @NotNull
    private final List<ICitizenDataView> citizens = new ArrayList<>();

    /**
     * Constructor for the town hall window.
     *
     * @param townHall {@link BuildingTownHall.View}.
     */
    public WindowCitizenPage(final BuildingTownHall.View townHall)
    {
        super(townHall, "layoutcitizens.xml");

        updateCitizens();
        fillCitizensList();

        registerButton(NAME_LABEL, this::fillCitizenInfo);
        registerButton(RECALL_ONE, this::recallOneClicked);
    }

    /**
     * Clears and resets all citizens.
     */
    private void updateCitizens()
    {
        citizens.clear();
        citizens.addAll(townHall.getColony().getCitizens().values());
        citizens.sort(COMPARE_BY_NAME);
    }

    /**
     * Executed when fill citizen is clicked.
     *
     * @param button the clicked button.
     */
    private void fillCitizenInfo(final Button button)
    {
        final ScrollingList citizenList = findPaneOfTypeByID(LIST_CITIZENS, ScrollingList.class);
        for (final Pane pane : citizenList.getContainer().getChildren())
        {
            pane.findPaneOfTypeByID(NAME_LABEL, ButtonImage.class).enable();
        }
        final int row = citizenList.getListElementIndexByPane(button);
        findPaneByID(CITIZEN_INFO).show();
        button.disable();
        final ICitizenDataView view = citizens.get(row);
        CitizenWindowUtils.createHappinessBar(view, this);
        CitizenWindowUtils.createSkillContent(view, this);
        findPaneOfTypeByID(JOB_LABEL, Text.class).setText(
          "§l" + LanguageHandler.format(view.getJob().trim().isEmpty() ? COM_MINECOLONIES_COREMOD_GUI_TOWNHALL_CITIZEN_UNEMPLOYED : view.getJob()));
        findPaneOfTypeByID(HIDDEN_CITIZEN_ID, Text.class).setText(String.valueOf(view.getId()));
    }

    /**
     * Executed when the recall one button has been clicked. Recalls one specific citizen.
     *
     * @param button the clicked button.
     */
    private void recallOneClicked(final Button button)
    {
        final int citizenid = Integer.parseInt(button.getParent().findPaneOfTypeByID(HIDDEN_CITIZEN_ID, Text.class).getTextAsString());
        Network.getNetwork().sendToServer(new RecallSingleCitizenMessage(townHall, citizenid));
    }

    /**
     * Fills the citizens list in the GUI.
     */
    private void fillCitizensList()
    {
        final ScrollingList citizenList = findPaneOfTypeByID(LIST_CITIZENS, ScrollingList.class);
        citizenList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return citizens.size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final ICitizenDataView citizen = citizens.get(index);
                rowPane.findPaneOfTypeByID(NAME_LABEL, ButtonImage.class).setText(citizen.getName());
            }
        });
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        updateCitizens();
    }

    @Override
    protected String getWindowId()
    {
        return BUTTON_CITIZENS;
    }
}
