package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.AbstractTextBuilder.TextBuilder;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.colony.buildings.modules.IAssignmentModuleView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.CitizenDataView;
import com.minecolonies.coremod.colony.buildings.moduleviews.PupilBuildingModuleView;
import com.minecolonies.coremod.colony.buildings.moduleviews.WorkerBuildingModuleView;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.network.messages.server.colony.citizen.PauseCitizenMessage;
import com.minecolonies.coremod.network.messages.server.colony.citizen.RestartCitizenMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_HIRE_PAUSE;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_HIRE_UNPAUSE;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * BOWindow for the hiring or firing of a worker.
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
    protected final List<IAssignmentModuleView> moduleViews = new ArrayList<>();

    /**
     * The selected module.
     */
    protected IAssignmentModuleView selectedModule;

    /**
     * Whether or not to show citizens who are employed
     */
    protected boolean showEmployed;

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
        super.registerButton(TOGGLE_SHOW_EMPLOYED, this::showEmployedToggled);

        moduleViews.addAll(building.getModuleViews(IAssignmentModuleView.class));
        selectedModule = moduleViews.get(0);

        setupSettings(findPaneOfTypeByID(BUTTON_MODE, Button.class));
        setupShowEmployed();
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
        if (index == HiringMode.LOCKED.ordinal()) { ++index; }  // only homes can be locked, not workplaces

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
        settingsButton.setText(Component.translatable(selectedModule.getHiringMode().getTranslationKey()));
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
        @NotNull final ICitizenDataView citizen = citizens.get(row);

        selectedModule.removeCitizen(citizen);
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
        @NotNull final ICitizenDataView citizen = citizens.get(row);

        // Fire citizen if they already have a job
        if (citizen.getWorkBuilding() != null && selectedModule instanceof WorkerBuildingModuleView)
        {
            IBuildingView oldJob =  colony.getBuilding(citizen.getWorkBuilding());
            oldJob.getModuleViewMatching(IAssignmentModuleView.class,
                                         m -> m.getJobEntry() == citizen.getJobView().getEntry())
                    .removeCitizen(citizen);
        }


        selectedModule.addCitizen(citizen);
        onOpened();
    }

    /**
     * Value to sort citizens in a WindowHireWorker
     * current employees -> no job -> library/training -> other job
     *
     * @param citizen the citizen to sort
     */
    protected int getCitizenPriority(ICitizenDataView citizen)
    {
        if (building.getPosition().equals(citizen.getWorkBuilding()))
            return 0;
        else if (citizen.getWorkBuilding() == null)
            return 1;
        else if (selectedModule.canAssign(citizen))
            return 2;
        else
            return 3;
    }


    /**
     * Job clicked to select a job a citizen.
     *
     * @param button the clicked button.
     */
    private void jobClicked(@NotNull final Button button)
    {
        final int row = jobList.getListElementIndexByPane(button);
        selectedModule = moduleViews.get(row);
        setupShowEmployed();
        setupSettings(findPaneOfTypeByID(BUTTON_MODE, Button.class));
        updateCitizens();
        citizenList.refreshElementPanes();
        jobList.refreshElementPanes();
    }

    /**
     * Show Employed button clicked to show/hide employed citizens
     * @param button the clicked button
     */
    protected void showEmployedToggled(@NotNull final Button button)
    {

        button.setText(Component.literal(showEmployed ? "N" : "Y"));
        showEmployed = !showEmployed;

        onOpened();
    }

    /**
     * Set up the showEmployed button.
     * Disable button if not a "normal" building, like a warehouse or quarry
     * Also disable for pupils
     */
    private void setupShowEmployed()
    {
        Button button = findPaneOfTypeByID(TOGGLE_SHOW_EMPLOYED, Button.class);
        button.setEnabled(selectedModule instanceof WorkerBuildingModuleView
                && !(selectedModule instanceof PupilBuildingModuleView));
        button.setText(Component.literal("N"));
        showEmployed = false;
    }

    /**
     * Helper function to show _all_ citizens that aren't children if viable.
     * @param citizen the citizen to check
     * @return whether the citizen can be assigned to the module
     */
    private boolean canAssign(ICitizenDataView citizen)
    {
        return (showEmployed && !citizen.isChild()) || selectedModule.canAssign(citizen);
    }

    /**
     * Clears and resets/updates all citizens.
     */
    protected void updateCitizens()
    {
        citizens.clear();

        citizens = colony.getCitizens().values().stream()
                .filter(this::canAssign)
                .sorted(Comparator.comparing(this::getCitizenPriority)
                        .thenComparing(ICitizenDataView::getName))
                .collect(Collectors.toList());

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
                final Button isPaused = rowPane.findPaneOfTypeByID(BUTTON_PAUSE, Button.class);

                if (canAssign(citizen)
                      && !selectedModule.isFull()
                      && !selectedModule.getAssignedCitizens().contains(citizen.getId()))
                {
                    rowPane.findPaneOfTypeByID(BUTTON_FIRE, Button.class).off();
                    rowPane.findPaneOfTypeByID(BUTTON_DONE, Button.class).on();
                    isPaused.off();
                    rowPane.findPaneOfTypeByID(BUTTON_RESTART, Button.class).off();
                }
                else if ((selectedModule.isFull()) && !selectedModule.getAssignedCitizens().contains(citizen.getId()))
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
                    isPaused.setText(Component.translatable(citizen.isPaused() ? COM_MINECOLONIES_COREMOD_GUI_HIRE_UNPAUSE : COM_MINECOLONIES_COREMOD_GUI_HIRE_PAUSE));
                }

                if (citizen.isPaused())
                {
                    rowPane.findPaneOfTypeByID(BUTTON_RESTART, Button.class).on();
                }
                else
                {
                    rowPane.findPaneOfTypeByID(BUTTON_RESTART, Button.class).off();
                }

                final MutableComponent intermString = Component.literal(" ");
                final TextBuilder textBuilder = PaneBuilders.textBuilder();
                textBuilder.append(Component.literal(""));
                int skillCount = citizen.getCitizenSkillHandler().getSkills().entrySet().size();

                final Skill primary = selectedModule instanceof  WorkerBuildingModuleView ? ((WorkerBuildingModuleView) selectedModule).getPrimarySkill() : null;
                final Skill secondary = selectedModule instanceof  WorkerBuildingModuleView ? ((WorkerBuildingModuleView) selectedModule).getSecondarySkill() : null;

                for (final Map.Entry<Skill, Tuple<Integer, Double>> entry : citizen.getCitizenSkillHandler().getSkills().entrySet())
                {
                    final String skillName = entry.getKey().name().toLowerCase(Locale.US);
                    final int skillLevel = entry.getValue().getA();
                    final Style skillStyle = createColor(primary, secondary, entry.getKey());

                    textBuilder.append(Component.translatable("com.minecolonies.coremod.gui.citizen.skills." + skillName).setStyle(skillStyle));
                    textBuilder.append(Component.literal(": " + skillLevel).setStyle(skillStyle));
                    if (--skillCount > 0)
                    {
                        textBuilder.append(intermString);
                    }
                }
                textBuilder.newLine(); // finish the current line

                Component citizenLabelComponent =
                  citizen.getJob().isEmpty() ? Component.literal(citizen.getName()) : Component.translatable(citizen.getJob()).append(": ").append(citizen.getName());
                rowPane.findPaneOfTypeByID(CITIZEN_LABEL, Text.class).setText(citizenLabelComponent);
                rowPane.findPaneOfTypeByID(ATTRIBUTES_LABEL, Text.class).setText(textBuilder.getText());
                final JobEntry entry = selectedModule.getJobEntry();
                PaneBuilders.tooltipBuilder()
                  .hoverPane(rowPane.findPaneOfTypeByID(ATTRIBUTES_LABEL, Text.class))
                  .build()
                  .setText(Component.translatable(entry.getKey().toString() + ".skills.desc"));
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
                button.setText(Component.translatable(entry.getTranslationKey()));
                if (entry.equals(selectedModule.getJobEntry()))
                {
                    button.disable();
                }
                else
                {
                    button.enable();
                }
                PaneBuilders.tooltipBuilder().hoverPane(button).build().setText(Component.translatable(entry.getKey().toString() + ".job.desc"));
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
            return Style.EMPTY.applyFormat(ChatFormatting.GREEN).applyFormat(ChatFormatting.BOLD);
        }
        if (secondary == current)
        {
            return Style.EMPTY.applyFormat(ChatFormatting.YELLOW).applyFormat(ChatFormatting.ITALIC);
        }
        return Style.EMPTY;
    }
}
