package com.minecolonies.coremod.client.gui.townhall;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.ICitizen;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.citizen.CitizenWindowUtils;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.coremod.network.messages.server.colony.citizen.RecallSingleCitizenMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_TOWNHALL_CITIZEN_UNEMPLOYED;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * BOWindow for the town hall.
 */
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
        citizens.addAll(building.getColony().getCitizens().values());
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
        String jobKey = view.getJob().trim().isEmpty() ? COM_MINECOLONIES_COREMOD_GUI_TOWNHALL_CITIZEN_UNEMPLOYED : view.getJob();
        findPaneOfTypeByID(JOB_LABEL, Text.class).setText(Component.translatable(jobKey).withStyle(ChatFormatting.BOLD));
        findPaneOfTypeByID(HIDDEN_CITIZEN_ID, Text.class).setText(Component.literal(String.valueOf(view.getId())));
    }

    /**
     * Executed when the recall one button has been clicked. Recalls one specific citizen.
     *
     * @param button the clicked button.
     */
    private void recallOneClicked(final Button button)
    {
        final int citizenid = Integer.parseInt(button.getParent().findPaneOfTypeByID(HIDDEN_CITIZEN_ID, Text.class).getTextAsString());
        Network.getNetwork().sendToServer(new RecallSingleCitizenMessage(building, citizenid));
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
                rowPane.findPaneOfTypeByID(NAME_LABEL, ButtonImage.class).setText(Component.literal(citizen.getName()));
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
