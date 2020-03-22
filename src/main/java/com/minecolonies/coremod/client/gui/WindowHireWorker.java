package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.constant.Constants;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ButtonHandler;
import com.ldtteam.blockout.controls.Label;
import com.ldtteam.blockout.views.ScrollingList;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.CitizenDataView;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.network.messages.HireFireMessage;
import com.minecolonies.coremod.network.messages.PauseCitizenMessage;
import com.minecolonies.coremod.network.messages.RestartCitizenMessage;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Window for the hiring or firing of a worker.
 */
public class WindowHireWorker extends AbstractWindowSkeleton implements ButtonHandler
{
    /**
     * The view of the current building.
     */
    private final AbstractBuildingWorker.View building;

    /**
     * The colony.
     */
    private final IColonyView colony;

    /**
     * Contains all the citizens.
     */
    private List<ICitizenDataView> citizens = new ArrayList<>();

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
    public WindowHireWorker(final IColonyView c, final BlockPos buildingId)
    {
        super(Constants.MOD_ID + HIRE_WORKER_SUFFIX);
        this.colony = c;
        building = (AbstractBuildingWorker.View) colony.getBuilding(buildingId);

        citizenList = findPaneOfTypeByID(CITIZEN_LIST_UNEMP, ScrollingList.class);

        super.registerButton(BUTTON_CANCEL, this::cancelClicked);
        super.registerButton(BUTTON_DONE, this::doneClicked);
        super.registerButton(BUTTON_FIRE, this::fireClicked);
        super.registerButton(BUTTON_PAUSE, this::pauseClicked);
        super.registerButton(BUTTON_RESTART, this::restartClicked);
        super.registerButton(BUTTON_MODE, this::modeClicked);
        setupSettings(findPaneOfTypeByID(BUTTON_MODE, Button.class));
    }

    /**
     * Canceled clicked to exit the GUI.
     * @param button the clicked button.
     */
    private void cancelClicked(@NotNull final Button button)
    {
        if (button.getID().equals(BUTTON_CANCEL) && colony.getTownHall() != null)
        {
            building.openGui(false);
        }
    }

    /**
     * Hiring mode switch clicked.
     * @param button the clicked button.
     */
    private void modeClicked(@NotNull final Button button)
    {
        switchHiringMode(button);
    }

    /**
     * Switch the mode after clicking the button.
     *
     * @param settingsButton the clicked button.
     */
    private void switchHiringMode(final Button settingsButton)
    {
        int index = building.getHiringMode().ordinal() + 1;

        if (index >= HiringMode.values().length)
        {
            index = 0;
        }

        building.setHiringMode(HiringMode.values()[index]);
        setupSettings(settingsButton);
    }

    /**
     * Setup the settings.
     *
     * @param settingsButton the buttons to setup.
     */
    private void setupSettings(final Button settingsButton)
    {
        settingsButton.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.hiringmode." + building.getHiringMode().name().toLowerCase(Locale.ENGLISH)));
    }

    /**
     * Restart citizen clicked to restart its AI.
     * @param button the clicked button.
     */
    private void restartClicked(@NotNull final Button button)
    {
        final int row = citizenList.getListElementIndexByPane(button);
        final int id = citizens.toArray(new CitizenDataView[0])[row].getId();

        Network.getNetwork().sendToServer(new RestartCitizenMessage(this.building, id));
        this.close();
    }

    /**
     * Pause citizen clicked to pause the citizen.
     * @param button the clicked button.
     */
    private void pauseClicked(@NotNull final Button button)
    {
        final int row = citizenList.getListElementIndexByPane(button);
        final int id = citizens.toArray(new CitizenDataView[0])[row].getId();
        @NotNull final ICitizenDataView citizen = citizens.get(row);

        Network.getNetwork().sendToServer(new PauseCitizenMessage(this.building, id));
        citizen.setPaused(!citizen.isPaused());
    }

    /**
     * Fire citizen clicked to fire a citizen.
     * @param button the clicked button.
     */
    private void fireClicked(@NotNull final Button button)
    {
        final int row = citizenList.getListElementIndexByPane(button);
        final int id = citizens.toArray(new CitizenDataView[0])[row].getId();
        @NotNull final ICitizenDataView citizen = citizens.get(row);

        Network.getNetwork().sendToServer(new HireFireMessage(this.building, false, id));
        building.removeWorkerId(id);
        citizen.setWorkBuilding(null);
        onOpened();
    }

    /**
     * Done clicked to persist the changes.
     * @param button the clicked button.
     */
    private void doneClicked(@NotNull final Button button)
    {
        final int row = citizenList.getListElementIndexByPane(button);
        final int id = citizens.get(row).getId();
        @NotNull final ICitizenDataView citizen = citizens.get(row);

        building.addWorkerId(id);
        Network.getNetwork().sendToServer(new HireFireMessage(this.building, true, id));
        citizen.setWorkBuilding(building.getPosition());
        onOpened();
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
                     .filter(citizen -> !citizen.isChild())
                     .filter(citizen -> (citizen.getWorkBuilding() == null && !building.hasEnoughWorkers())
                                          || building.getPosition().equals(citizen.getWorkBuilding())).sorted(Comparator.comparing(ICitizenDataView::getName))
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
        findPaneOfTypeByID(AUTO_HIRE_WARN, Label.class).off();

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
                @NotNull final ICitizenDataView citizen = citizens.get(index);
                final Skill primary = building.getPrimarySkill();
                final Skill secondary = building.getSecondarySkill();

                final Button isPaused = rowPane.findPaneOfTypeByID(BUTTON_PAUSE, Button.class);

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

                    if ((!building.getColony().isManualHiring() && building.getHiringMode() == HiringMode.DEFAULT) || (building.getHiringMode() == HiringMode.AUTO))
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

                StringBuilder attributes = new StringBuilder();
                final String intermString = " | ";

                final List<Map.Entry<Skill, Tuple<Integer, Double>>> list = new ArrayList<>(citizen.getCitizenSkillHandler().getSkills().entrySet());
                for (int i = 0; i < 5; i++)
                {
                    final Map.Entry<Skill, Tuple<Integer, Double>> entry = list.get(i);
                    @NotNull final String text = createAttributeText(createColor(primary, secondary, entry.getKey()),
                      LanguageHandler.format("com.minecolonies.coremod.gui.citizen.skills." + entry.getKey().name().toLowerCase(Locale.US) , entry.getValue().getA()));
                    attributes.append(text).append(intermString);
                }
                attributes.delete(attributes.length() - intermString.length(), attributes.length());

                StringBuilder attributes2 = new StringBuilder();
                for (int i = 5; i < list.size(); i++)
                {
                    final Map.Entry<Skill, Tuple<Integer, Double>> entry = list.get(i);
                    @NotNull final String text = createAttributeText(createColor(primary, secondary, entry.getKey()),
                      LanguageHandler.format("com.minecolonies.coremod.gui.citizen.skills." + entry.getKey().name().toLowerCase(Locale.US) , entry.getValue().getA()));
                    attributes2.append(text).append(intermString);
                }
                attributes2.delete(attributes2.length() - intermString.length(), attributes2.length());

                rowPane.findPaneOfTypeByID(CITIZEN_LABEL, Label.class).setLabelText(citizen.getName());
                rowPane.findPaneOfTypeByID(ATTRIBUTES_LABEL, Label.class).setLabelText(attributes.toString());
                rowPane.findPaneOfTypeByID(ATTRIBUTES_LABEL2, Label.class).setLabelText(attributes2.toString());
            }
        });
    }

    private static String createAttributeText(final String color, final String text)
    {
        return color + text + TextFormatting.RESET.toString();
    }

    private static String createColor(final Skill primary, final Skill secondary, final Skill current)
    {
        if (primary == current)
        {
            return TextFormatting.GREEN.toString() + TextFormatting.BOLD.toString();
        }
        if (secondary == current)
        {
            return TextFormatting.YELLOW.toString() + TextFormatting.ITALIC.toString();
        }
        return "";
    }
}
