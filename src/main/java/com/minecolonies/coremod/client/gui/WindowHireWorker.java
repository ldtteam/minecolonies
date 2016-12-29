package com.minecolonies.coremod.client.gui;

import com.minecolonies.blockout.Pane;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.Label;
import com.minecolonies.blockout.views.ScrollingList;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.CitizenDataView;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.lib.Constants;
import com.minecolonies.coremod.network.messages.HireFireMessage;
import com.minecolonies.coremod.util.LanguageHandler;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Window for the hiring or firing of a worker.
 */
public class WindowHireWorker extends Window implements Button.Handler
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
     * Id of the id label in the GUI.
     */
    private static final String ID_LABEL = "id";

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
     * Position of the id label of each citizen in the list.
     */
    private static final int CITIZEN_ID_LABEL_POSITION = 3;

    /**
     * Colors the following String in green.
     */
    private static final String GREEN_STRING = "§2";

    /**
     * Colors the following string in orange.
     */
    private static final String YELLOW_STRING = "§e";

    /**
     * Contains all the citizens.
     */
    private List<CitizenDataView> citizens = new ArrayList<>();

    /**
     * The view of the current building.
     */
    private final AbstractBuilding.View building;

    /**
     * The colony.
     */
    private final ColonyView colony;

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
        building = colony.getBuilding(buildingId);
        updateCitizens();
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
                     .filter(citizen -> citizen.getWorkBuilding() == null)
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
        final ScrollingList citizenList = findPaneOfTypeByID(CITIZEN_LIST, ScrollingList.class);
        citizenList.enable();
        citizenList.show();
        //Creates a dataProvider for the unemployed citizenList.
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
                final CitizenDataView citizen = citizens.get(index);

                if (building instanceof AbstractBuildingWorker.View)
                {
                    final AbstractBuildingWorker.Skill primary = ((AbstractBuildingWorker.View) building).getPrimarySkill();
                    final AbstractBuildingWorker.Skill secondary = ((AbstractBuildingWorker.View) building).getSecondarySkill();

                    @NotNull final String strength = (primary.equals(AbstractBuildingWorker.Skill.STRENGTH) ? GREEN_STRING
                            : (secondary.equals(AbstractBuildingWorker.Skill.STRENGTH) ? YELLOW_STRING : ""))
                            +  LanguageHandler.format("com.minecolonies.coremod.gui.citizen.skills.strength", citizen.getStrength()) + " §f ";

                    @NotNull final String charisma = (primary.equals(AbstractBuildingWorker.Skill.CHARISMA) ? GREEN_STRING
                            : (secondary.equals(AbstractBuildingWorker.Skill.CHARISMA) ? YELLOW_STRING : ""))
                            +  LanguageHandler.format("com.minecolonies.coremod.gui.citizen.skills.charisma", citizen.getCharisma()) + " §f ";

                    @NotNull final String dexterity = (primary.equals(AbstractBuildingWorker.Skill.DEXTERITY) ? GREEN_STRING
                            : (secondary.equals(AbstractBuildingWorker.Skill.DEXTERITY) ? YELLOW_STRING : ""))
                            +  LanguageHandler.format("com.minecolonies.coremod.gui.citizen.skills.dexterity", citizen.getDexterity()) + " §f ";

                    @NotNull final String endurance = (primary.equals(AbstractBuildingWorker.Skill.ENDURANCE) ? GREEN_STRING
                            : (secondary.equals(AbstractBuildingWorker.Skill.ENDURANCE) ? YELLOW_STRING : ""))
                            +  LanguageHandler.format("com.minecolonies.coremod.gui.citizen.skills.endurance", citizen.getEndurance()) + " §f ";

                    @NotNull final String intelligence = (primary.equals(AbstractBuildingWorker.Skill.INTELLIGENCE) ? GREEN_STRING
                            : (secondary.equals(AbstractBuildingWorker.Skill.INTELLIGENCE) ? YELLOW_STRING : ""))
                            +  LanguageHandler.format("com.minecolonies.coremod.gui.citizen.skills.intelligence", citizen.getStrength()) + " §f ";

                    //Creates the list of attributes for each citizen
                    @NotNull final String attributes = strength + charisma + dexterity + endurance + intelligence;

                    rowPane.findPaneOfTypeByID(CITIZEN_LABEL, Label.class).setLabelText(citizen.getName());
                    rowPane.findPaneOfTypeByID(ATTRIBUTES_LABEL, Label.class).setLabelText(attributes);
                    //Invisible id textContent.
                    rowPane.findPaneOfTypeByID(ID_LABEL, Label.class).setLabelText(Integer.toString(citizen.getID()));
                }
            }
        });
    }

    @Override
    public void onUpdate()
    {
        updateCitizens();
        window.findPaneOfTypeByID(CITIZEN_LIST, ScrollingList.class).refreshElementPanes();
    }

    /**
     * Called when any button has been clicked.
     *
     * @param button the clicked button.
     */
    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        if (button.getID().equals(BUTTON_DONE))
        {
            @NotNull final Label idLabel = (Label) button.getParent().getChildren().get(CITIZEN_ID_LABEL_POSITION);
            final int id = Integer.parseInt(idLabel.getLabelText());

            if (building instanceof AbstractBuildingWorker.View)
            {
                ((AbstractBuildingWorker.View) building).setWorkerId(id);
            }
            MineColonies.getNetwork().sendToServer(new HireFireMessage(this.building, true, id));
        }
        else if (!button.getID().equals(BUTTON_CANCEL))
        {
            return;
        }

        if (colony.getTownHall() != null)
        {
            building.openGui();
        }
    }
}
