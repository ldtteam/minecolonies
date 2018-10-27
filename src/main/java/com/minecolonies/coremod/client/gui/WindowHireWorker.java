package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.ColorConstants;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.Pane;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.ButtonHandler;
import com.minecolonies.blockout.controls.Label;
import com.minecolonies.blockout.views.ScrollingList;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.CitizenDataView;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.network.messages.HireFireMessage;
import com.minecolonies.coremod.network.messages.PauseCitizenMessage;
import com.minecolonies.coremod.network.messages.RestartCitizenMessage;

import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Window for the hiring or firing of a worker.
 */
public class WindowHireWorker extends Window implements ButtonHandler
{
    /**
     * Id of the done button in the GUI.
     */
    private static final String BUTTON_DONE = "done";

    /**
     * Id of the cancel button in the GUI.
     */
    private static final String BUTTON_CANCEL = "cancel";

    /**
     * Id of the citizen name in the GUI.
     */
    private static final String CITIZEN_LABEL = "citizen";

    /**
     * Id of the citizen list in the GUI.
     */
    private static final String CITIZEN_LIST = "unemployed";

    /**
     * Id of the attributes label in the GUI.
     */
    private static final String ATTRIBUTES_LABEL = "attributes";

    /**
     * Link to the xml file of the window.
     */
    private static final String BUILDING_NAME_RESOURCE_SUFFIX = ":gui/windowhireworker.xml";

    /**
     * Id of the fire button
     */
    private static final String BUTTON_FIRE = "fire";

    /**
     * Id of the automatic hiring warning
     */
    private static final String AUTO_HIRE_WARN = "autoHireWarn";

    /**
     * Id of the pause button
     */
    private static final String BUTTON_PAUSE = "pause";

    /**
     * Id of the pause button
     */
    private static final String BUTTON_RESTART = "restart";

    /**
     * The view of the current building.
     */
    private final AbstractBuildingWorker.View building;

    /**
     * The colony.
     */
    private final ColonyView colony;

    /**
     * Contains all the citizens.
     */
    private List<CitizenDataView> citizens = new ArrayList<>();

    /**
     * Holder of a list element
     */
    private final ScrollingList citizenList;

    /**
     * Constructor for the window when the player wants to hire a worker for a certain job.
     *
     * @param c          the colony view.
     * @param buildingId the building position.
     */
    public WindowHireWorker(final ColonyView c, final BlockPos buildingId)
    {
        super(Constants.MOD_ID + BUILDING_NAME_RESOURCE_SUFFIX);
        this.colony = c;
        building = (AbstractBuildingWorker.View) colony.getBuilding(buildingId);

        citizenList = findPaneOfTypeByID(CITIZEN_LIST, ScrollingList.class);
    }

    /**
     * Clears and resets/updates all citizens.
     */
    private void updateCitizens()
    {
        citizens.clear();
        citizens.addAll(colony.getCitizens().values());

        //Removes all citizens which already have a job.
        citizens = colony.getCitizens().values().stream()
                     .filter(citizen -> (citizen.getWorkBuilding() == null && !building.hasEnoughWorkers())
                                          || building.getLocation().equals(citizen.getWorkBuilding())).sorted(Comparator.comparing(CitizenDataView::getName))
                     .collect(Collectors.toList());
    }

    /**
     * Called when the GUI has been opened.
     * Will fill the fields and lists.
     */
    @Override
    public void onOpened()
    {
        updateCitizens();
        citizenList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return citizens.size();
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                @NotNull final CitizenDataView citizen = citizens.get(index);
                final AbstractBuildingWorker.Skill primary = building.getPrimarySkill();
                final AbstractBuildingWorker.Skill secondary = building.getSecondarySkill();

                final Button isPaused = rowPane.findPaneOfTypeByID(BUTTON_PAUSE, Button.class);

                findPaneOfTypeByID(AUTO_HIRE_WARN, Label.class).off();

                if (citizen.getWorkBuilding() == null)
                {
                    rowPane.findPaneOfTypeByID(BUTTON_FIRE, Button.class).off();
                    rowPane.findPaneOfTypeByID(BUTTON_DONE, Button.class).on();
                    isPaused.off();
                }
                else
                {
                    rowPane.findPaneOfTypeByID(BUTTON_DONE, Button.class).off();
                    rowPane.findPaneOfTypeByID(BUTTON_FIRE, Button.class).on();

                    if (!building.getColony().isManualHiring())
                    {
                        rowPane.findPaneOfTypeByID(BUTTON_FIRE, Button.class).disable();
                        findPaneOfTypeByID(AUTO_HIRE_WARN, Label.class).on();
                    }

                    isPaused.on();
                    isPaused.setLabel(LanguageHandler.format(citizen.isPaused() ? COM_MINECOLONIES_COREMOD_GUI_HIRE_UNPAUSE : COM_MINECOLONIES_COREMOD_GUI_HIRE_PAUSE));
                }

                if (citizen.isPaused())
                {
                    rowPane.findPaneOfTypeByID(BUTTON_RESTART, Button.class).on();
                }
                else
                {
                    rowPane.findPaneOfTypeByID(BUTTON_RESTART, Button.class).off();
                }

                @NotNull final String strength = createAttributeText(createColor(primary, secondary, AbstractBuildingWorker.Skill.STRENGTH),
                  LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_CITIZEN_SKILLS_STRENGTH, citizen.getStrength()));
                @NotNull final String charisma = createAttributeText(createColor(primary, secondary, AbstractBuildingWorker.Skill.CHARISMA),
                  LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_CITIZEN_SKILLS_CHARISMA, citizen.getCharisma()));
                @NotNull final String dexterity = createAttributeText(createColor(primary, secondary, AbstractBuildingWorker.Skill.DEXTERITY),
                  LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_CITIZEN_SKILLS_DEXTERITY, citizen.getDexterity()));
                @NotNull final String endurance = createAttributeText(createColor(primary, secondary, AbstractBuildingWorker.Skill.ENDURANCE),
                  LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_CITIZEN_SKILLS_ENDURANCE, citizen.getEndurance()));
                @NotNull final String intelligence = createAttributeText(createColor(primary, secondary, AbstractBuildingWorker.Skill.INTELLIGENCE),
                  LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_CITIZEN_SKILLS_INTELLIGENCE, citizen.getIntelligence()));

                //Creates the list of attributes for each citizen
                @NotNull final String attributes = strength + " | " + charisma + " | " + dexterity + " | " + endurance + " | " + intelligence;

                rowPane.findPaneOfTypeByID(CITIZEN_LABEL, Label.class).setLabelText(citizen.getName());
                rowPane.findPaneOfTypeByID(ATTRIBUTES_LABEL, Label.class).setLabelText(attributes);
            }
        });
    }

    private static String createAttributeText(final String color, final String text)
    {
        return color + text + ColorConstants.WHITE;
    }

    private static String createColor(final AbstractBuildingWorker.Skill primary, final AbstractBuildingWorker.Skill secondary, final AbstractBuildingWorker.Skill current)
    {
        if (primary == current)
        {
            return ColorConstants.GREEN;
        }
        if (secondary == current)
        {
            return ColorConstants.YELLOW;
        }
        return "";
    }

    /**
     * Called when any button has been clicked.
     *
     * @param button the clicked button.
     */
    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        if (button.getID().equals(BUTTON_CANCEL))
        {
            if (colony.getTownHall() != null)
            {
                building.openGui(false);
            }
            return;
        }

        final int row = citizenList.getListElementIndexByPane(button);
        final int id = citizens.toArray(new CitizenDataView[citizens.size()])[row].getId();
        @NotNull final CitizenDataView citizen = citizens.get(row); // TODO: NEW BLOCKOUT -> delete this and also all setters of <code>citizen</code> under this + views

        switch (button.getID())
        {
            case BUTTON_DONE:
                building.addWorkerId(id);
                MineColonies.getNetwork().sendToServer(new HireFireMessage(this.building, true, id));
                citizen.setWorkBuilding(building.getLocation());
                onOpened();
                break;
            case BUTTON_FIRE:
                MineColonies.getNetwork().sendToServer(new HireFireMessage(this.building, false, id));
                building.removeWorkerId(id);
                citizen.setWorkBuilding(null);
                onOpened();
                break;
            case BUTTON_PAUSE:
                MineColonies.getNetwork().sendToServer(new PauseCitizenMessage(this.building, id));
                citizen.setPaused(!citizen.isPaused());
                break;
            case BUTTON_RESTART:
                MineColonies.getNetwork().sendToServer(new RestartCitizenMessage(this.building, id));
                this.close();
                break;
            default:
                break;
        }
    }
}
