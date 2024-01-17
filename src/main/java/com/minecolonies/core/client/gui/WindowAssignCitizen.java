package com.minecolonies.core.client.gui;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ButtonHandler;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.Network;
import com.minecolonies.core.colony.buildings.modules.BuildingModules;
import com.minecolonies.core.colony.buildings.views.LivingBuildingView;
import com.minecolonies.core.network.messages.server.colony.building.home.AssignUnassignMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_TOWNHALL_CITIZEN_UNEMPLOYED;
import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.core.client.gui.townhall.AbstractWindowTownHall.*;

/**
 * BOWindow for the hiring or firing of a worker.
 */
public class WindowAssignCitizen extends AbstractWindowSkeleton implements ButtonHandler
{
    /**
     * Threshold that defines when the living quarters are too far away.
     */
    private static final double FAR_DISTANCE_THRESHOLD = 250;

    /**
     * The view of the current building.
     */
    private final LivingBuildingView building;

    /**
     * List of citizens which can be assigned.
     */
    private final ScrollingList unassignedCitizenList;

    /**
     * List of citizens which are currently assigned.
     */
    private final ScrollingList assignedCitizenList;

    /**
     * The colony.
     */
    private final IColonyView colony;

    /**
     * Contains all the unassigned citizens.
     */
    private List<ICitizenDataView> unassignedCitizens = new ArrayList<>();

    /**
     * Contains all the already assigned citizens.
     */
    private List<ICitizenDataView> assignedCitizens = new ArrayList<>();

    /**
     * Constructor for the window when the player wants to assign a worker for a certain home building.
     *
     * @param c          the colony view.
     * @param building the building.
     */
    public WindowAssignCitizen(final IColonyView c, final LivingBuildingView building)
    {
        super(Constants.MOD_ID + ASSIGN_CITIZEN_RESOURCE_SUFFIX);
        this.colony = c;
        this.building = building;
        unassignedCitizenList = findPaneOfTypeByID(UNASSIGNED_CITIZEN_LIST, ScrollingList.class);
        assignedCitizenList = findPaneOfTypeByID(ASSIGNED_CITIZEN_LIST, ScrollingList.class);

        super.registerButton(BUTTON_CANCEL, this::cancelClicked);
        super.registerButton(BUTTON_MODE, this::modeClicked);

        super.registerButton(BUTTON_HIRE, this::hireClicked);
        super.registerButton(BUTTON_FIRE, this::fireClicked);

        updateCitizens();

        setupSettings(findPaneOfTypeByID(BUTTON_MODE, Button.class));
    }

    /**
     * When hire was clicked.
     * @param button the clicked button.
     */
    private void hireClicked(@NotNull final Button button)
    {
        final int row = unassignedCitizenList.getListElementIndexByPane(button);
        final ICitizenDataView data = unassignedCitizens.get(row);

        if (building.getResidents().size() >= building.getMax())
        {
            return;
        }
        building.addResident(data.getId());
        data.setHomeBuilding(building.getPosition());

        Network.getNetwork().sendToServer(new AssignUnassignMessage(this.building, true, data.getId(), null));

        updateCitizens();
        unassignedCitizenList.refreshElementPanes();
        assignedCitizenList.refreshElementPanes();
    }

    /**
     * When fire was clicked.
     * @param button the clicked button.
     */
    private void fireClicked(@NotNull final Button button)
    {
        final int row = assignedCitizenList.getListElementIndexByPane(button);
        final ICitizenDataView data = assignedCitizens.get(row);

        building.removeResident(data.getId());
        data.setHomeBuilding(null);

        Network.getNetwork().sendToServer(new AssignUnassignMessage(this.building, false, data.getId(), null));

        updateCitizens();
        unassignedCitizenList.refreshElementPanes();
        assignedCitizenList.refreshElementPanes();
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
        int index = building.getHiringMode().ordinal() + 1;

        if (index >= HiringMode.values().length)
        {
            index = 0;
        }

        building.setHiringMode(HiringMode.values()[index]);
        setupSettings(settingsButton);
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
     * Setup the settings.
     *
     * @param settingsButton the buttons to setup.
     */
    private void setupSettings(final Button settingsButton)
    {
        settingsButton.setText(Component.translatable("com.minecolonies.coremod.gui.hiringmode." + building.getHiringMode().name().toLowerCase(Locale.ENGLISH)));
    }

    /**
     * Clears and resets/updates all citizens.
     */
    private void updateCitizens()
    {
        //Removes citizens that work from home and remove citizens already living here.
        unassignedCitizens = colony.getCitizens().values().stream()
                     .filter(cit -> !Objects.equals(cit.getHomeBuilding(), cit.getWorkBuilding()) && !building.getPosition().equals(cit.getHomeBuilding()))
                     .sorted(Comparator.comparing((ICitizenDataView cit) -> cit.getHomeBuilding() == null ? 0 : 1)
                               .thenComparingLong(cit -> {
                                   if (cit.getWorkBuilding() == null)
                                   {
                                       if (cit.getHomeBuilding() == null)
                                       {
                                           return 0;
                                       }
                                       return Integer.MAX_VALUE;
                                   }

                                   return (int) BlockPosUtil.getDistance(cit.getWorkBuilding(), building.getPosition());
                               })).toList();

        assignedCitizens.clear();
        for (final int id : building.getModuleView(BuildingModules.LIVING).getAssignedCitizens())
        {
            assignedCitizens.add(colony.getCitizen(id));
        }
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        updateCitizens();

        unassignedCitizenList.enable();
        unassignedCitizenList.show();
        //Creates a dataProvider for the homeless citizenList.
        unassignedCitizenList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return unassignedCitizens.size();
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                @NotNull final ICitizenDataView citizen = unassignedCitizens.get(index);
                final Button hireButton = rowPane.findPaneOfTypeByID(BUTTON_HIRE, Button.class);
                final BlockPos home = citizen.getHomeBuilding();
                final BlockPos work = citizen.getWorkBuilding();

                final Text citizenLabel = rowPane.findPaneOfTypeByID(CITIZEN_LABEL, Text.class);
                citizenLabel.setText(Component.literal(citizen.getName()));

                MutableComponent workString = Component.empty();
                double newDistance = 0;
                if (work != null)
                {
                    newDistance = BlockPosUtil.getDistance(work, building.getPosition());
                    workString = Component.translatable("com.minecolonies.coremod.gui.home.new", newDistance);
                }

                MutableComponent homeString = Component.translatable("com.minecolonies.coremod.gui.home.homeless");
                boolean better = false;
                if (home != null)
                {
                    if (work != null)
                    {
                        final int oldDistance = (int) BlockPosUtil.getDistance(work, home);
                        homeString = Component.translatable("com.minecolonies.coremod.gui.home.currently", oldDistance);
                        better = newDistance < oldDistance;
                        if (oldDistance > FAR_DISTANCE_THRESHOLD)
                        {
                            homeString = homeString.withStyle(ChatFormatting.RED);
                        }
                    }
                    else
                    {
                        homeString = Component.empty();
                    }
                }

                if (better)
                {
                    workString = workString.withStyle(ChatFormatting.DARK_GREEN);
                }


                final Text newLivingLabel = rowPane.findPaneOfTypeByID(CITIZEN_JOB, Text.class);
                if (citizen.getJobView() != null)
                {
                    newLivingLabel.setText(
                      Component.empty().append(Component.translatable(citizen.getJobView().getEntry().getTranslationKey())).append(": ").append(workString).append(" ").append(homeString));
                    newLivingLabel.setTextWrap(true);
                }
                else
                {
                    newLivingLabel.setText(Component.translatable(COM_MINECOLONIES_COREMOD_GUI_TOWNHALL_CITIZEN_UNEMPLOYED));
                }

                if (((colony.isManualHousing() && building.getHiringMode() == HiringMode.DEFAULT) || (building.getHiringMode() == HiringMode.MANUAL)))
                {
                    if (building.getResidents().size() < building.getMax())
                    {
                        hireButton.enable();
                    }
                    PaneBuilders.tooltipBuilder().hoverPane(hireButton).build().setText(Component.empty());
                }
                else
                {
                    hireButton.disable();
                    PaneBuilders.tooltipBuilder().hoverPane(hireButton).build().setText(Component.translatable("com.minecolonies.coremod.gui.home.hire.warning"));
                }
            }
        });

        assignedCitizenList.enable();
        assignedCitizenList.show();
        //Creates a dataProvider for the homeless citizenList.
        assignedCitizenList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return assignedCitizens.size();
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                @NotNull final ICitizenDataView citizen = assignedCitizens.get(index);
                final Button fireButton = rowPane.findPaneOfTypeByID(BUTTON_FIRE, Button.class);
                final BlockPos work = citizen.getWorkBuilding();
                fireButton.setText(Component.translatable("com.minecolonies.coremod.gui.hiring.buttonunassign"));


                final Text citizenLabel = rowPane.findPaneOfTypeByID(CITIZEN_LABEL, Text.class);
                citizenLabel.setText(Component.literal(citizen.getName()));

                MutableComponent workString = Component.empty();
                int newDistance;
                if (work != null)
                {
                    newDistance = (int) BlockPosUtil.getDistance(work, building.getPosition());
                    workString = Component.translatable("com.minecolonies.coremod.gui.home.new", newDistance);
                }

                final Text newLivingLabel = rowPane.findPaneOfTypeByID(CITIZEN_JOB, Text.class);
                newLivingLabel.setTextWrap(true);
                if (citizen.getJobView() != null)
                {
                    if (work != null)
                    {
                        final int distance = (int) BlockPosUtil.getDistance(work, building.getPosition());
                        if (distance > FAR_DISTANCE_THRESHOLD)
                        {
                            workString = workString.withStyle(ChatFormatting.RED);
                        }
                    }
                    newLivingLabel.setText(Component.empty().append(Component.translatable(citizen.getJobView().getEntry().getTranslationKey())).append(Component.literal(": ")).append(workString));
                }
                else
                {
                    newLivingLabel.setText(Component.translatable(COM_MINECOLONIES_COREMOD_GUI_TOWNHALL_CITIZEN_UNEMPLOYED));
                }

                if (((colony.isManualHousing() && building.getHiringMode() == HiringMode.DEFAULT) || (building.getHiringMode() == HiringMode.MANUAL)))
                {
                    fireButton.enable();
                    PaneBuilders.tooltipBuilder().hoverPane(fireButton).build().setText(Component.empty());
                }
                else
                {
                    fireButton.disable();
                    PaneBuilders.tooltipBuilder().hoverPane(fireButton).build().setText(Component.translatable("com.minecolonies.coremod.gui.home.hire.warning"));
                }
            }
        });
    }
}
