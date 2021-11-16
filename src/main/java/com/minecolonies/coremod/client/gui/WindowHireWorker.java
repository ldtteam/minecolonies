package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneBuilders;
import com.ldtteam.blockout.controls.AbstractTextBuilder.TextBuilder;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.colony.buildings.IBuildingCanBeHiredFrom;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.CitizenDataView;
import com.minecolonies.coremod.colony.buildings.moduleviews.WorkerBuildingModuleView;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.network.messages.server.colony.building.HireFireMessage;
import com.minecolonies.coremod.network.messages.server.colony.citizen.PauseCitizenMessage;
import com.minecolonies.coremod.network.messages.server.colony.citizen.RestartCitizenMessage;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_HIRE_PAUSE;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_HIRE_UNPAUSE;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Window for the hiring or firing of a worker.
 */
public class WindowHireWorker extends AbstractWindowSkeleton
{
    /**
     * The view of the current building.
     */
    protected final AbstractBuildingView building;

    /**
     * The colony.
     */
    protected final IColonyView colony;

    /**
     * Contains all the citizens.
     */
    protected List<ICitizenDataView> citizens = new ArrayList<>();

    /**
     * Holder of a list element
     */
    protected final ScrollingList citizenList;

    /**
     * Holder of a list element
     */
    protected final ScrollingList jobList;

    /**
     * The different job module views.
     */
    protected final List<WorkerBuildingModuleView> moduleViews = new ArrayList<>();

    /**
     * The selected module.
     */
    protected WorkerBuildingModuleView selectedModule;

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
        building = (AbstractBuildingView) colony.getBuilding(buildingId);

        citizenList = findPaneOfTypeByID(CITIZEN_LIST_UNEMP, ScrollingList.class);
        jobList = findPaneOfTypeByID(JOB_LIST, ScrollingList.class);

        super.registerButton(BUTTON_CANCEL, this::cancelClicked);
        super.registerButton(BUTTON_DONE, this::doneClicked);
        super.registerButton(BUTTON_FIRE, this::fireClicked);
        super.registerButton(BUTTON_PAUSE, this::pauseClicked);
        super.registerButton(BUTTON_RESTART, this::restartClicked);
        super.registerButton(BUTTON_MODE, this::modeClicked);
        super.registerButton(BUTTON_JOB, this::jobClicked);
        moduleViews.addAll(building.getModuleViews(WorkerBuildingModuleView.class));
        selectedModule = moduleViews.get(0);

        setupSettings(findPaneOfTypeByID(BUTTON_MODE, Button.class));
    }

    /**
     * Canceled clicked to exit the GUI.
     *
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
     *
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
        int index = selectedModule.getHiringMode().ordinal() + 1;

        if (index >= HiringMode.values().length)
        {
            index = 0;
        }

        selectedModule.setHiringMode(HiringMode.values()[index]);
        setupSettings(settingsButton);
    }

    /**
     * Setup the settings.
     *
     * @param settingsButton the buttons to setup.
     */
    private void setupSettings(final Button settingsButton)
    {
        settingsButton.setText(LanguageHandler.format("com.minecolonies.coremod.gui.hiringmode." + selectedModule.getHiringMode().name().toLowerCase(Locale.ENGLISH)));
    }

    /**
     * Restart citizen clicked to restart its AI.
     *
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
     *
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
     *
     * @param button the clicked button.
     */
    private void fireClicked(@NotNull final Button button)
    {
        final int row = citizenList.getListElementIndexByPane(button);
        final int id = citizens.toArray(new CitizenDataView[0])[row].getId();
        @NotNull final ICitizenDataView citizen = citizens.get(row);

        Network.getNetwork().sendToServer(new HireFireMessage(this.building, false, id, selectedModule.getJobEntry()));
        selectedModule.removeWorkerId(id);
        citizen.setWorkBuilding(null);
        onOpened();
    }

    /**
     * Hire clicked to persist the changes.
     *
     * @param button the clicked button.
     */
    private void doneClicked(@NotNull final Button button)
    {
        final int row = citizenList.getListElementIndexByPane(button);
        final int id = citizens.get(row).getId();
        @NotNull final ICitizenDataView citizen = citizens.get(row);

        selectedModule.addWorkerId(id);
        Network.getNetwork().sendToServer(new HireFireMessage(this.building, true, id, selectedModule.getJobEntry()));
        citizen.setWorkBuilding(building.getPosition());
        citizen.setJobView(selectedModule.getJobEntry().getJobViewProducer().get().apply(colony, citizen));
        citizen.getJobView().setEntry(selectedModule.getJobEntry());
        onOpened();
    }

    /**
     * Fire citizen clicked to fire a citizen.
     *
     * @param button the clicked button.
     */
    private void jobClicked(@NotNull final Button button)
    {
        final int row = jobList.getListElementIndexByPane(button);
        selectedModule = moduleViews.get(row);
        setupSettings(findPaneOfTypeByID(BUTTON_MODE, Button.class));
        updateCitizens();
        citizenList.refreshElementPanes();
        jobList.refreshElementPanes();
    }

    /**
     * Clears and resets/updates all citizens.
     */
    protected void updateCitizens()
    {
        citizens.clear();

        //Removes all citizens which already have a job.
        citizens = colony.getCitizens().values().stream()
                     .filter(citizen -> selectedModule.canAssign(citizen))
                     .filter(citizen ->  citizen.getWorkBuilding() == null
                                           || (citizen.getJobView() != null && citizen.getJobView().getEntry().equals(selectedModule.getJobEntry()))
                                           || colony.getBuilding(citizen.getWorkBuilding()) instanceof IBuildingCanBeHiredFrom)
                     .sorted(Comparator.comparing(ICitizenDataView::getName))
                     .collect(Collectors.toList());

        citizens.sort(
                (c1, c2) -> {
                    int i1 = building.getPosition().equals(c1.getWorkBuilding()) ? -1 : 0;
                    int i2 = building.getPosition().equals(c2.getWorkBuilding()) ? -1 : 0;
                    return Integer.compare(i1, i2);
            }
        );
    }

    /**
     * Called when the GUI has been opened. Will fill the fields and lists.
     */
    @Override
    public void onOpened()
    {
        updateCitizens();
        findPaneOfTypeByID(AUTO_HIRE_WARN, Text.class).off();

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
                @NotNull final ICitizenDataView citizen = citizens.get(index);
                final Skill primary = selectedModule.getPrimarySkill();
                final Skill secondary = selectedModule.getSecondarySkill();

                final Button isPaused = rowPane.findPaneOfTypeByID(BUTTON_PAUSE, Button.class);

                if ((citizen.getWorkBuilding() == null || colony.getBuilding(citizen.getWorkBuilding()) instanceof IBuildingCanBeHiredFrom) && selectedModule.canAssign(citizen) && (!selectedModule.isFull()) && !selectedModule.getWorkerIdList().contains(citizen.getId()))
                {
                    rowPane.findPaneOfTypeByID(BUTTON_FIRE, Button.class).off();
                    rowPane.findPaneOfTypeByID(BUTTON_DONE, Button.class).on();
                    isPaused.off();
                    rowPane.findPaneOfTypeByID(BUTTON_RESTART, Button.class).off();
                }
                else if ((selectedModule.isFull()) && !selectedModule.getWorkerIdList().contains(citizen.getId()))
                {
                    rowPane.findPaneOfTypeByID(BUTTON_FIRE, Button.class).off();
                    rowPane.findPaneOfTypeByID(BUTTON_DONE, Button.class).off();
                    isPaused.off();
                    rowPane.findPaneOfTypeByID(BUTTON_RESTART, Button.class).off();
                }
                else
                {
                    rowPane.findPaneOfTypeByID(BUTTON_DONE, Button.class).off();
                    rowPane.findPaneOfTypeByID(BUTTON_FIRE, Button.class).on();

                    if ((!selectedModule.getColony().isManualHiring() && selectedModule.getHiringMode() == HiringMode.DEFAULT) || (selectedModule.getHiringMode() == HiringMode.AUTO))
                    {
                        rowPane.findPaneOfTypeByID(BUTTON_FIRE, Button.class).disable();
                        findPaneOfTypeByID(AUTO_HIRE_WARN, Text.class).on();
                    }

                    isPaused.on();
                    isPaused.setText(LanguageHandler.format(citizen.isPaused() ? COM_MINECOLONIES_COREMOD_GUI_HIRE_UNPAUSE : COM_MINECOLONIES_COREMOD_GUI_HIRE_PAUSE));
                }

                if (citizen.isPaused())
                {
                    rowPane.findPaneOfTypeByID(BUTTON_RESTART, Button.class).on();
                }
                else
                {
                    rowPane.findPaneOfTypeByID(BUTTON_RESTART, Button.class).off();
                }

                final StringTextComponent intermString = new StringTextComponent(" ");
                final TextBuilder textBuilder = PaneBuilders.textBuilder();
                textBuilder.append(new StringTextComponent(""));
                int skillCount = citizen.getCitizenSkillHandler().getSkills().entrySet().size();

                for (final Map.Entry<Skill, Tuple<Integer, Double>> entry : citizen.getCitizenSkillHandler().getSkills().entrySet())
                {
                    final String skillName = entry.getKey().name().toLowerCase(Locale.US);
                    final int skillLevel = entry.getValue().getA();
                    final Style skillStyle = createColor(primary, secondary, entry.getKey());

                    textBuilder.append(new TranslationTextComponent("com.minecolonies.coremod.gui.citizen.skills." + skillName).setStyle(skillStyle));
                    textBuilder.append(new StringTextComponent(": " + skillLevel).setStyle(skillStyle));
                    if (--skillCount > 0)
                    {
                        textBuilder.append(intermString);
                    }
                }
                textBuilder.newLine(); // finish the current line

                rowPane.findPaneOfTypeByID(CITIZEN_LABEL, Text.class)
                  .setText((citizen.getJob().isEmpty() ? "" : LanguageHandler.format(citizen.getJob()) + ": ") + citizen.getName());
                rowPane.findPaneOfTypeByID(ATTRIBUTES_LABEL, Text.class).setText(textBuilder.getText());
            }
        });

        jobList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return moduleViews.size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final JobEntry entry = moduleViews.get(index).getJobEntry();
                final Button button = rowPane.findPaneOfTypeByID(BUTTON_JOB, Button.class);
                button.setText(new TranslationTextComponent(entry.getTranslationKey()));
                if (entry.equals(selectedModule.getJobEntry()))
                {
                    button.disable();
                }
                else
                {
                    button.enable();
                }
            }
        });
    }

    /**
     * Create the color scheme.
     *
     * @param primary   the primary skill.
     * @param secondary the secondary skill.
     * @param current   the current skill to compare.
     * @return the modifier string.
     */
    protected Style createColor(final Skill primary, final Skill secondary, final Skill current)
    {
        if (primary == current)
        {
            return Style.EMPTY.applyFormat(TextFormatting.GREEN).applyFormat(TextFormatting.BOLD);
        }
        if (secondary == current)
        {
            return Style.EMPTY.applyFormat(TextFormatting.YELLOW).applyFormat(TextFormatting.ITALIC);
        }
        return Style.EMPTY;
    }
}
