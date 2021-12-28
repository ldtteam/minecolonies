package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ButtonHandler;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.blockout.views.Window;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.buildings.views.LivingBuildingView;
import com.minecolonies.coremod.network.messages.server.colony.building.home.AssignUnassignMessage;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.coremod.client.gui.townhall.AbstractWindowTownHall.DARKGREEN;
import static com.minecolonies.coremod.client.gui.townhall.AbstractWindowTownHall.RED;

/**
 * Window for the hiring or firing of a worker.
 */
public class WindowAssignCitizen extends Window implements ButtonHandler
{
    /**
     * Threshold that defines when the living quarters are too far away.
     */
    private static final double FAR_DISTANCE_THRESHOLD = 250;

    /**
     * The view of the current building.
     */
    private final IBuildingView building;

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
     * @param buildingId the building position.
     */
    public WindowAssignCitizen(final IColonyView c, final BlockPos buildingId)
    {
        super(Constants.MOD_ID + ASSIGN_CITIZEN_RESOURCE_SUFFIX);
        this.colony = c;
        building = colony.getBuilding(buildingId);
        citizenList = findPaneOfTypeByID(CITIZEN_LIST, ScrollingList.class);
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
                     .filter(cit -> cit.getHomeBuilding() == null
                                      || !(colony.getBuilding(cit.getHomeBuilding()) instanceof AbstractBuildingGuards.View)
                                           && !cit.getHomeBuilding().equals(building.getID()))
                     .sorted(Comparator.comparing(cit -> ((ICitizenDataView) cit).getHomeBuilding() == null ? 0 : 1)
                               .thenComparingLong(cit -> {
                                   if (((ICitizenDataView) cit).getWorkBuilding() == null)
                                   {
                                       return 0;
                                   }

                                   return BlockPosUtil.getDistance2D(((ICitizenDataView) cit).getWorkBuilding(), building.getPosition());
                               })).collect(Collectors.toList());
    }

    /**
     * Called when the GUI has been opened. Will fill the fields and lists.
     */
    @Override
    public void onOpened()
    {
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

                if (building instanceof LivingBuildingView)
                {
                    rowPane.findPaneOfTypeByID(CITIZEN_LABEL, Text.class).setText(citizen.getName());

                    final BlockPos work = citizen.getWorkBuilding();
                    String workString = "";
                    double newDistance = 0;
                    if (work != null)
                    {
                        newDistance = BlockPosUtil.getDistance2D(work, building.getPosition());;
                        workString = " " + newDistance + " blocks";
                    }

                    final BlockPos home = citizen.getHomeBuilding();
                    String homeString = "";
                    boolean better = false;
                    boolean badCurrentLiving = false;
                    if (home != null)
                    {
                        if (work != null)
                        {
                            final double oldDistance = BlockPosUtil.getDistance2D(work, home);
                            homeString = LanguageHandler.format("com.minecolonies.coremod.gui.homehut.currently", oldDistance);
                            better = newDistance < oldDistance;
                            if (oldDistance >= FAR_DISTANCE_THRESHOLD)
                            {
                                badCurrentLiving = true;
                            }
                        }
                        else
                        {
                            homeString = LanguageHandler.format("com.minecolonies.coremod.gui.homehut.current", home.getX(), home.getY(), home.getZ());
                        }
                    }

                    final Text newLivingLabel = rowPane.findPaneOfTypeByID(CITIZEN_JOB, Text.class);
                    newLivingLabel.setText(LanguageHandler.format(citizen.getJob()) + workString);
                    if (better)
                    {
                        newLivingLabel.setColors(DARKGREEN);
                    }

                    final Text currentLivingLabel = rowPane.findPaneOfTypeByID(CITIZEN_LIVING, Text.class);
                    currentLivingLabel.setText(homeString);
                    if (badCurrentLiving)
                    {
                        currentLivingLabel.setColors(RED);
                    }

                    final Button done = rowPane.findPaneOfTypeByID(CITIZEN_DONE, Button.class);
                    if (colony.isManualHousing())
                    {
                        done.enable();
                    }
                    else
                    {
                        done.disable();
                    }
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
            final int row = citizenList.getListElementIndexByPane(button);
            final ICitizenDataView data = citizens.get(row);
            if (building instanceof LivingBuildingView)
            {
                ((LivingBuildingView) building).addResident(data.getId());
            }
            Network.getNetwork().sendToServer(new AssignUnassignMessage(this.building, true, data.getId(), null));
        }
        else if (!button.getID().equals(BUTTON_CANCEL))
        {
            return;
        }

        if (colony.getTownHall() != null)
        {
            building.openGui(false);
        }
    }
}
