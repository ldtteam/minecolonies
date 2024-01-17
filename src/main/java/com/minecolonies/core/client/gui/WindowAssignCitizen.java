package com.minecolonies.core.client.gui;

import com.ldtteam.blockui.Pane;
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
import com.minecolonies.core.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.core.colony.buildings.views.LivingBuildingView;
import com.minecolonies.core.network.messages.server.colony.building.home.AssignUnassignMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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
    private final ScrollingList citizenList;

    /**
     * The colony.
     */
    private final IColonyView colony;

    /**
     * Contains all the citizens.
     */
    private List<ICitizenDataView> citizens = new ArrayList<>();

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
        citizenList = findPaneOfTypeByID(CITIZEN_LIST, ScrollingList.class);
        super.registerButton(BUTTON_CANCEL, this::cancelClicked);
        super.registerButton(BUTTON_MODE, this::modeClicked);
        super.registerButton(BUTTON_DONE, this::hireClicked);

        updateCitizens();

        setupSettings(findPaneOfTypeByID(BUTTON_MODE, Button.class));
    }

    /**
     * When assignment was clicked.
     * @param button the clicked button.
     */
    private void hireClicked(@NotNull final Button button)
    {
        final int row = citizenList.getListElementIndexByPane(button);
        final ICitizenDataView data = citizens.get(row);

        final boolean isAssign = button.getText().getString().equals(Component.translatable("com.minecolonies.coremod.gui.hiring.buttonassign").getString());
        if (isAssign)
        {
            if (building.getResidents().size() >= building.getMax())
            {
                return;
            }
            building.addResident(data.getId());
            data.setHomeBuilding(building.getPosition());
        }
        else
        {
            building.removeResident(data.getId());
            data.setHomeBuilding(null);
        }

        Network.getNetwork().sendToServer(new AssignUnassignMessage(this.building, isAssign, data.getId(), null));
        updateCitizens();
        citizenList.refreshElementPanes();
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
        citizens.clear();
        citizens.addAll(colony.getCitizens().values());

        //Removes all citizens which already have a job.
        citizens = colony.getCitizens().values().stream()
                     .filter(cit -> !(colony.getBuilding(cit.getHomeBuilding()) instanceof AbstractBuildingGuards.View))
                     .sorted(Comparator.comparing((ICitizenDataView cit) -> (cit.getHomeBuilding() != null && cit.getHomeBuilding().equals(building.getPosition())) ? 0 : 1)
                       .thenComparing(cit -> cit.getHomeBuilding() == null ? 0 : 1)
                               .thenComparingLong(cit -> {
                                   if (cit.getWorkBuilding() == null)
                                   {
                                       return 0;
                                   }

                                   return BlockPosUtil.getDistance2D(cit.getWorkBuilding(), building.getPosition());
                               })).collect(Collectors.toList());
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        updateCitizens();
        citizenList.enable();
        citizenList.show();
        //Creates a dataProvider for the homeless citizenList.
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


                final Button done = rowPane.findPaneOfTypeByID(CITIZEN_DONE, Button.class);
                final BlockPos home = citizen.getHomeBuilding();
                final BlockPos work = citizen.getWorkBuilding();

                boolean assign = false;
                if (home != null && home.equals(building.getPosition()))
                {
                    done.setText(Component.translatable("com.minecolonies.coremod.gui.hiring.buttonunassign"));
                }
                else
                {
                    assign = true;
                    done.setText(Component.translatable("com.minecolonies.coremod.gui.hiring.buttonassign"));
                }

                final Text citizenLabel = rowPane.findPaneOfTypeByID(CITIZEN_LABEL, Text.class);
                citizenLabel.setText(Component.literal(citizen.getName()));
                if (assign)
                {
                    citizenLabel.setColors(YELLOW);
                }
                else
                {
                    citizenLabel.setColors(DARKGREEN);
                }

                String workString = "";
                double newDistance = 0;
                if (work != null)
                {
                    newDistance = BlockPosUtil.getDistance2D(work, building.getPosition());
                    workString = " " + Component.translatable("com.minecolonies.coremod.gui.home.new", newDistance).getString();
                }

                String homeString = Component.translatable("com.minecolonies.coremod.gui.home.homeless").getString();
                boolean better = false;
                boolean badCurrentLiving = true;
                if (home != null)
                {
                    if (work != null)
                    {
                        final double oldDistance = BlockPosUtil.getDistance2D(work, home);
                        homeString = Component.translatable("com.minecolonies.coremod.gui.home.currently", oldDistance).getString();
                        better = newDistance < oldDistance;
                        if (oldDistance < FAR_DISTANCE_THRESHOLD)
                        {
                            badCurrentLiving = false;
                        }
                    }
                    else
                    {
                        homeString = Component.translatable("com.minecolonies.coremod.gui.home.currently", home.getX(), home.getY(), home.getZ()).getString();
                    }
                }

                final Text newLivingLabel = rowPane.findPaneOfTypeByID(CITIZEN_JOB, Text.class);
                if (citizen.getJobView() != null)
                {
                    newLivingLabel.setText(Component.literal(Component.translatable(citizen.getJobView().getEntry().getTranslationKey()).getString() + ":" + workString));
                    if (better)
                    {
                        newLivingLabel.setColors(DARKGREEN);
                    }
                }

                final Text currentLivingLabel = rowPane.findPaneOfTypeByID(CITIZEN_LIVING, Text.class);
                if (assign)
                {
                    currentLivingLabel.setText(Component.literal(homeString));
                    if (badCurrentLiving)
                    {
                        currentLivingLabel.setColors(RED);
                    }
                }
                else
                {
                    currentLivingLabel.setText(Component.translatable("com.minecolonies.coremod.gui.home.liveshere"));
                }

                if ((colony.isManualHousing() || building.getHiringMode() != HiringMode.DEFAULT)
                      && !(building.getHiringMode() == HiringMode.AUTO)
                      && (!assign || building.getResidents().size() < building.getMax()))
                {
                    done.enable();
                }
                else
                {
                    done.disable();
                }
            }
        });
    }
}
