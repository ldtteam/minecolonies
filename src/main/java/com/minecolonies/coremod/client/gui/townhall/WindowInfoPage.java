package com.minecolonies.coremod.client.gui.townhall;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.colonyEvents.descriptions.IBuildingEventDescription;
import com.minecolonies.api.colony.colonyEvents.descriptions.ICitizenEventDescription;
import com.minecolonies.api.colony.colonyEvents.descriptions.IColonyEventDescription;
import com.minecolonies.api.colony.permissions.*;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.moduleviews.WorkerBuildingModuleView;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.coremod.colony.colonyEvents.citizenEvents.CitizenDiedEvent;
import com.minecolonies.coremod.network.messages.PermissionsMessage;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

import static com.minecolonies.api.research.util.ResearchConstants.CITIZEN_CAP;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * BOWindow for the town hall.
 */
public class WindowInfoPage extends AbstractWindowTownHall
{
    /**
     * The ScrollingList of the events.
     */
    private ScrollingList eventList;

    /**
     * Whether the event list should display permission events, or colony events.
     */
    private boolean permissionEvents;

    /**
     * Constructor for the town hall window.
     *
     * @param building {@link BuildingTownHall.View}.
     */
    public WindowInfoPage(final BuildingTownHall.View building)
    {
        super(building, "layoutinfo.xml");

        registerButton(BUTTON_PERMISSION_EVENTS, this::permissionEventsClicked);
        registerButton(BUTTON_ADD_PLAYER_OR_FAKEPLAYER, this::addPlayerToColonyClicked);
    }

    /**
     * Executed when <code>WindowTownHall</code> is opened. Does tasks like setting buttons.
     */
    @Override
    public void onOpened()
    {
        super.onOpened();
        createAndSetStatistics();
        fillEventsList();
    }

    /**
     * Creates several statistics and sets them in the building GUI.
     */
    private void createAndSetStatistics()
    {
        final DecimalFormat df = new DecimalFormat("#.#");
        df.setRoundingMode(RoundingMode.CEILING);
        final String roundedHappiness = df.format(building.getColony().getOverallHappiness());

        findPaneOfTypeByID(HAPPINESS_LABEL, Text.class).setText(roundedHappiness);
        final int citizensSize = building.getColony().getCitizens().size();
        final int citizensCap;

        if(MinecoloniesAPIProxy.getInstance().getGlobalResearchTree().hasResearchEffect(CITIZEN_CAP))
        {
            citizensCap = (int) (Math.min(MineColonies.getConfig().getServer().maxCitizenPerColony.get(),
              25 + this.building.getColony().getResearchManager().getResearchEffects().getEffectStrength(CITIZEN_CAP)));
        }
        else
        {
              citizensCap = MineColonies.getConfig().getServer().maxCitizenPerColony.get();
        }

        final Text totalCitizenLabel = findPaneOfTypeByID(TOTAL_CITIZENS_LABEL, Text.class);
        totalCitizenLabel.setText(new TranslatableComponent(COM_MINECOLONIES_COREMOD_GUI_TOWNHALL_POPULATION_TOTALCITIZENS_COUNT,
            citizensSize,
            Math.max(citizensSize, building.getColony().getCitizenCountLimit())));
        List<MutableComponent> hoverText = new ArrayList<>();
        if(citizensSize < (citizensCap * 0.9) && citizensSize < (building.getColony().getCitizenCountLimit() * 0.9))
        {
            totalCitizenLabel.setColors(DARKGREEN);
        }
        else if(citizensSize < citizensCap)
        {
            hoverText.add(new TranslatableComponent("com.minecolonies.coremod.gui.townhall.population.totalcitizens.houselimited", this.building.getColony().getName()));
            totalCitizenLabel.setColors(ORANGE);
        }
        else
        {
            if(citizensCap < MineColonies.getConfig().getServer().maxCitizenPerColony.get())
            {
                hoverText.add(new TranslatableComponent("com.minecolonies.coremod.gui.townhall.population.totalcitizens.researchlimited", this.building.getColony().getName()));
            }
            else
            {
                hoverText.add(new TranslatableComponent( "com.minecolonies.coremod.gui.townhall.population.totalcitizens.configlimited", this.building.getColony().getName()));
            }
            totalCitizenLabel.setText(
                new TranslatableComponent(COM_MINECOLONIES_COREMOD_GUI_TOWNHALL_POPULATION_TOTALCITIZENS_COUNT, citizensSize, citizensCap));
            totalCitizenLabel.setColors(RED);
        }
        PaneBuilders.tooltipBuilder().hoverPane(totalCitizenLabel).build().setText(hoverText);

        int children = 0;
        final Map<String, Tuple<Integer, Integer>> jobMaxCountMap = new HashMap<>();
        for (@NotNull final IBuildingView building : building.getColony().getBuildings())
        {
            if (building instanceof AbstractBuildingView)
            {
                for (final WorkerBuildingModuleView module : building.getModuleViews(WorkerBuildingModuleView.class))
                {
                    int max = module.getMaxInhabitants();
                    int workers = module.getWorkerIdList().size();

                    final String jobName = module.getJobDisplayName().toLowerCase(Locale.ENGLISH);

                    final Tuple<Integer, Integer> tuple = jobMaxCountMap.getOrDefault(jobName, new Tuple<>(0, 0));
                    jobMaxCountMap.put(jobName, new Tuple<>(tuple.getA() + workers, tuple.getB() + max));
                }
            }
        }


        //calculate number of children
        int unemployed = 0;
        for (ICitizenDataView iCitizenDataView : building.getColony().getCitizens().values())
        {
            if (iCitizenDataView.isChild())
            {
                children++;
            }
            else if (iCitizenDataView.getJobView() == null)
            {
                unemployed++;
            }
        }

        final int childCount = children;
        final int unemployedCount = unemployed;

        final ScrollingList list = findPaneOfTypeByID("citizen-stats", ScrollingList.class);
        if (list == null)
        {
            return;
        }

        final int maxJobs = jobMaxCountMap.size();
        final List<Map.Entry<String, Tuple<Integer, Integer>>> theList = new ArrayList<>(jobMaxCountMap.entrySet());

        list.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return maxJobs + 2;
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final Text label = rowPane.findPaneOfTypeByID(CITIZENS_AMOUNT_LABEL, Text.class);
                // preJobsHeaders = number of all unemployed citizens

                if (index < theList.size())
                {
                    final Map.Entry<String, Tuple<Integer, Integer>> entry = theList.get(index);
                    final String job = new TranslatableComponent(entry.getKey()).getString();
                    label.setText(new TranslatableComponent(COM_MINECOLONIES_COREMOD_GUI_TOWNHALL_POPULATION_EACH, job, entry.getValue().getA(), entry.getValue().getB()));
                }
                else
                {
                    if (index == maxJobs + 1)
                    {
                        label.setText(new TranslatableComponent(COM_MINECOLONIES_COREMOD_GUI_TOWNHALL_POPULATION_UNEMPLOYED, unemployedCount));
                    }
                    else
                    {
                        label.setText(new TranslatableComponent(COM_MINECOLONIES_COREMOD_GUI_TOWNHALL_POPULATION_CHILDS, childCount));
                    }
                }
            }
        });
    }

    private void fillEventsList()
    {
        eventList = findPaneOfTypeByID(EVENTS_LIST, ScrollingList.class);
        eventList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return permissionEvents ? building.getPermissionEvents().size() : building.getColonyEvents().size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final Text nameLabel = rowPane.findPaneOfTypeByID(NAME_LABEL, Text.class);
                final Text actionLabel = rowPane.findPaneOfTypeByID(ACTION_LABEL, Text.class);
                if (permissionEvents)
                {
                    final PermissionEvent event = building.getPermissionEvents().get(index);

                    nameLabel.setText(event.getName() + (event.getId() == null ? " <fake>" : ""));
                    rowPane.findPaneOfTypeByID(POS_LABEL, Text.class).setText(event.getPosition().getX() + " " + event.getPosition().getY() + " " + event.getPosition().getZ());

                    if (event.getId() == null)
                    {
                        rowPane.findPaneOfTypeByID(BUTTON_ADD_PLAYER_OR_FAKEPLAYER, Button.class).hide();
                    }

                    final String name = new TranslatableComponent(KEY_TO_PERMISSIONS + event.getAction().toString().toLowerCase(Locale.US)).getString();

                    if (name.contains(KEY_TO_PERMISSIONS))
                    {
                        Log.getLogger().warn("Didn't work for:" + name);
                        return;
                    }
                    actionLabel.setText(name);
                }
                else
                {
                    final IColonyEventDescription event = building.getColonyEvents().get(index);
                    if (event instanceof CitizenDiedEvent)
                    {
                        actionLabel.setText(((CitizenDiedEvent) event).getDeathCause());
                    }
                    else
                    {
                        actionLabel.setText(event.getName());
                    }
                    if (event instanceof ICitizenEventDescription)
                    {
                        nameLabel.setText(((ICitizenEventDescription) event).getCitizenName());
                    }
                    else if (event instanceof IBuildingEventDescription)
                    {
                        IBuildingEventDescription buildEvent = (IBuildingEventDescription) event;
                        nameLabel.setText(buildEvent.getBuildingName() + " " + buildEvent.getLevel());
                    }
                    rowPane.findPaneOfTypeByID(POS_LABEL, Text.class).setText(event.getEventPos().getX() + " " + event.getEventPos().getY() + " " + event.getEventPos().getZ());
                    rowPane.findPaneOfTypeByID(BUTTON_ADD_PLAYER_OR_FAKEPLAYER, Button.class).hide();
                }
            }
        });
    }

    /**
     * Action performed when remove player button is clicked.
     *
     * @param button Button that holds the user clicked on.
     */
    private void addPlayerToColonyClicked(@NotNull final Button button)
    {
        final int row = eventList.getListElementIndexByPane(button);
        if (row >= 0 && row < building.getPermissionEvents().size())
        {
            final PermissionEvent user = building.getPermissionEvents().get(row);
            Network.getNetwork().sendToServer(new PermissionsMessage.AddPlayerOrFakePlayer(building.getColony(), user.getName(), user.getId()));
        }
    }

    /**
     * Switching between permission and colony events.
     * 
     * @param button the clicked button.
     */
    public void permissionEventsClicked(@NotNull final Button button)
    {
        permissionEvents = !permissionEvents;
        button.setText(new TranslatableComponent(permissionEvents ? TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_TOWNHALL_COLONYEVENTS : TranslationConstants.COM_MINECOLONIES_CIREMOD_GUI_TOWNHALL_PERMISSIONEVENTS));
    }

    @Override
    protected String getWindowId()
    {
        return BUTTON_INFOPAGE;
    }
}
