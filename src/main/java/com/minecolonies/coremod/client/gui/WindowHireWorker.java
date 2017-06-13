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
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.network.messages.HireFireMessage;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
    private static final String BUILDING_NAME_RESOURCE_SUFFIX = ":gui/windowHireWorker.xml";

    /**
     * Position of the id label of each citizen in the list.
     */
    private static final int CITIZEN_ID_LABEL_POSITION = 3;

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
                @NotNull final CitizenDataView citizen = citizens.get(index);

                if (building instanceof AbstractBuildingWorker.View)
                {
                    final AbstractBuildingWorker.Skill primary = ((AbstractBuildingWorker.View) building).getPrimarySkill();
                    final AbstractBuildingWorker.Skill secondary = ((AbstractBuildingWorker.View) building).getSecondarySkill();

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
                    @NotNull final String attributes = strength + charisma + dexterity + endurance + intelligence;

                    rowPane.findPaneOfTypeByID(CITIZEN_LABEL, Label.class).setLabelText(citizen.getName());
                    rowPane.findPaneOfTypeByID(ATTRIBUTES_LABEL, Label.class).setLabelText(attributes);
                    //Invisible id textContent.
                    rowPane.findPaneOfTypeByID(ID_LABEL, Label.class).setLabelText(Integer.toString(citizen.getID()));
                }
            }
        });
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

    private static String createAttributeText(String color, String text)
    {
        return color + text + ColorConstants.WHITE;
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
