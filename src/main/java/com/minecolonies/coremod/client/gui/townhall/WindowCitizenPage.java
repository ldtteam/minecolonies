package com.minecolonies.coremod.client.gui.townhall;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.ICitizen;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.constant.CitizenConstants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.moduleviews.CombinedHiringLimitModuleView;
import com.minecolonies.coremod.colony.buildings.moduleviews.WorkerBuildingModuleView;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingGuardTower;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.coremod.network.messages.server.colony.citizen.RecallSingleCitizenMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

import static com.minecolonies.api.research.util.ResearchConstants.CITIZEN_CAP;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_TOWNHALL_POPULATION_CHILDS;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * BOWindow for the town hall.
 */
public class WindowCitizenPage extends AbstractWindowTownHall
{
    /**
     * Citizen name comparator.
     */
    private static final Comparator<ICitizenDataView> COMPARE_BY_NAME = Comparator.comparing(ICitizen::getName);

    /**
     * List of citizens.
     */
    @NotNull
    private final List<ICitizenDataView> citizens = new ArrayList<>();

    /**
     * The selected citizen.
     */
    private ICitizenDataView selectedCitizen;

    /**
     * The filter for the resource list.
     */
    private String filter = "";

    /**
     * Constructor for the town hall window.
     *
     * @param townHall {@link BuildingTownHall.View}.
     */
    public WindowCitizenPage(final BuildingTownHall.View townHall)
    {
        super(townHall, "layoutcitizens.xml");

        updateCitizens();
        fillCitizensList();
        if (!citizens.isEmpty())
        {
            selectedCitizen = citizens.get(0);
        }

        registerButton(NAME_LABEL, this::citizenSelected);
        registerButton(RECALL_ONE, this::recallOneClicked);
        fillCitizenInfo();
        createAndSetStatistics();

        window.findPaneOfTypeByID(SEARCH_INPUT, TextField.class).setHandler(input -> {
            final String newFilter = input.getText();
            if (!newFilter.equals(filter))
            {
                filter = newFilter;
                updateCitizens();
            }
        });
    }

    /**
     * //todo dont forget to adjust health etc display to allow 3 digit!
     * Creates several statistics and sets them in the building GUI.
     */
    private void createAndSetStatistics()
    {
        final int citizensSize = building.getColony().getCitizens().size();
        final int citizensCap;

        if (MinecoloniesAPIProxy.getInstance().getGlobalResearchTree().hasResearchEffect(CITIZEN_CAP))
        {
            final int max = Math.max(CitizenConstants.CITIZEN_LIMIT_DEFAULT, (int) this.building.getColony().getResearchManager().getResearchEffects().getEffectStrength(CITIZEN_CAP));
            citizensCap = Math.min(max, MineColonies.getConfig().getServer().maxCitizenPerColony.get());
        }
        else
        {
            citizensCap = MineColonies.getConfig().getServer().maxCitizenPerColony.get();
        }

        final Text totalCitizenLabel = findPaneOfTypeByID(TOTAL_CITIZENS_LABEL, Text.class);
        totalCitizenLabel.setText(Component.translatable(COM_MINECOLONIES_COREMOD_GUI_TOWNHALL_POPULATION_TOTALCITIZENS_COUNT,
          citizensSize,
          Math.max(citizensSize, building.getColony().getCitizenCountLimit())));
        List<MutableComponent> hoverText = new ArrayList<>();
        if(citizensSize < (citizensCap * 0.9) && citizensSize < (building.getColony().getCitizenCountLimit() * 0.9))
        {
            totalCitizenLabel.setColors(DARKGREEN);
        }
        else if(citizensSize < citizensCap)
        {
            hoverText.add(Component.translatable(WARNING_POPULATION_NEEDS_HOUSING, this.building.getColony().getName()));
            totalCitizenLabel.setColors(ORANGE);
        }
        else
        {
            if(citizensCap < MineColonies.getConfig().getServer().maxCitizenPerColony.get())
            {
                hoverText.add(Component.translatable(WARNING_POPULATION_RESEARCH_LIMITED, this.building.getColony().getName()));
            }
            else
            {
                hoverText.add(Component.translatable( WARNING_POPULATION_CONFIG_LIMITED, this.building.getColony().getName()));
            }
            totalCitizenLabel.setText(Component.translatable(COM_MINECOLONIES_COREMOD_GUI_TOWNHALL_POPULATION_TOTALCITIZENS_COUNT, citizensSize, citizensCap));
            totalCitizenLabel.setColors(RED);
        }
        PaneBuilders.tooltipBuilder().hoverPane(totalCitizenLabel).build().setText(hoverText);

        int children = 0;
        final Map<String, com.minecolonies.api.util.Tuple<Integer, Integer>> jobMaxCountMap = new HashMap<>();
        for (@NotNull final IBuildingView building : building.getColony().getBuildings())
        {
            if (building instanceof AbstractBuildingView)
            {
                for (final WorkerBuildingModuleView module : building.getModuleViews(WorkerBuildingModuleView.class))
                {
                    int alreadyAssigned = 0;
                    if (module instanceof CombinedHiringLimitModuleView)
                    {
                        for (final WorkerBuildingModuleView combinedModule : building.getModuleViews(WorkerBuildingModuleView.class))
                        {
                            alreadyAssigned += combinedModule.getAssignedCitizens().size();
                        }
                    }
                    int max = module.getMaxInhabitants() - alreadyAssigned + module.getAssignedCitizens().size();
                    int workers = module.getAssignedCitizens().size();

                    final String jobName = module.getJobDisplayName().toLowerCase(Locale.ENGLISH);

                    final com.minecolonies.api.util.Tuple<Integer, Integer> tuple = jobMaxCountMap.getOrDefault(jobName, new com.minecolonies.api.util.Tuple<>(0, 0));
                    jobMaxCountMap.put(jobName, new com.minecolonies.api.util.Tuple<>(tuple.getA() + workers, tuple.getB() + max));
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
        final List<Map.Entry<String, com.minecolonies.api.util.Tuple<Integer, Integer>>> theList = new ArrayList<>(jobMaxCountMap.entrySet());
        theList.sort(Map.Entry.comparingByKey());

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
                    final Map.Entry<String, com.minecolonies.api.util.Tuple<Integer, Integer>> entry = theList.get(index);
                    final String jobString = Component.translatable(entry.getKey()).getString();
                    final String formattedJobString = jobString.substring(0, 1).toUpperCase(Locale.US) + jobString.substring(1);

                    final Component numberOfWorkers = Component.translatable(COM_MINECOLONIES_COREMOD_GUI_TOWNHALL_POPULATION_EACH, formattedJobString, entry.getValue().getA(), entry.getValue().getB());
                    label.setText(numberOfWorkers);
                }
                else
                {
                    if (index == maxJobs + 1)
                    {
                        label.setText(Component.translatable(COM_MINECOLONIES_COREMOD_GUI_TOWNHALL_POPULATION_UNEMPLOYED, unemployedCount));
                    }
                    else
                    {
                        label.setText(Component.translatable(COM_MINECOLONIES_COREMOD_GUI_TOWNHALL_POPULATION_CHILDS, childCount));
                    }
                }
            }
        });
    }


    /**
     * Clears and resets all citizens.
     */
    private void updateCitizens()
    {
        citizens.clear();
        if (filter.isEmpty())
        {
            citizens.addAll(building.getColony().getCitizens().values());
        }
        else
        {
            citizens.addAll(building.getColony().getCitizens().values().stream().filter(cit -> cit.getName().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US))
                                                                                                 || cit.getJobComponent().getString().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US))).toList());
        }
        citizens.sort(COMPARE_BY_NAME);
    }

    /**
     * On clicking a citizen name in the list.
     * @param button the clicked button.
     */
    private void citizenSelected(final Button button)
    {
        final ScrollingList citizenList = findPaneOfTypeByID(LIST_CITIZENS, ScrollingList.class);
        selectedCitizen = citizens.get(citizenList.getListElementIndexByPane(button));

        fillCitizenInfo();
    }

    /**
     * Setup citizen info in UI.
     */
    private void fillCitizenInfo()
    {
        if (selectedCitizen == null)
        {
            return;
        }

        if (selectedCitizen.isFemale())
        {
            findPaneOfTypeByID(WINDOW_ID_GENDER, Image.class).setImage(new ResourceLocation(FEMALE_SOURCE), false);
        }
        else
        {
            findPaneOfTypeByID(WINDOW_ID_GENDER, Image.class).setImage(new ResourceLocation(MALE_SOURCE), false);
        }

        findPaneOfTypeByID(JOB_LABEL, Text.class).setText(selectedCitizen.getJobComponent().withStyle(ChatFormatting.BOLD));

        findPaneOfTypeByID(HEALTH_SHORT_LABEL, Text.class).setText(Component.literal((int)selectedCitizen.getHealth() + "/" + (int) selectedCitizen.getMaxHealth()));
        findPaneOfTypeByID(HAPPINESS_SHORT_LABEL, Text.class).setText(Component.literal((int) selectedCitizen.getHappiness() + "/" + 10));
        findPaneOfTypeByID(SATURATION_SHORT_LABEL, Text.class).setText(Component.literal((int)selectedCitizen.getSaturation() + "/" + 20));

        final Entity entity = Minecraft.getInstance().level.getEntity(selectedCitizen.getEntityId());
        if (entity != null)
        {
            final EntityIcon entityIcon = findPaneOfTypeByID(ENTITY_ICON, EntityIcon.class);
            entityIcon.setEntity(entity);
            entityIcon.show();
        }
    }

    /**
     * Executed when the recall one button has been clicked. Recalls one specific citizen.
     *
     * @param button the clicked button.
     */
    private void recallOneClicked(final Button button)
    {
        if (selectedCitizen == null)
        {
            return;
        }
        Network.getNetwork().sendToServer(new RecallSingleCitizenMessage(building, selectedCitizen.getId()));
    }

    /**
     * Fills the citizens list in the GUI.
     */
    private void fillCitizensList()
    {
        final ScrollingList citizenList = findPaneOfTypeByID(LIST_CITIZENS, ScrollingList.class);
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
                final ICitizenDataView citizen = citizens.get(index);
                final Button button = rowPane.findPaneOfTypeByID(NAME_LABEL, ButtonImage.class);
                button.setText(Component.literal(citizen.getName()));

                final AbstractTextBuilder.TextBuilder textBuilder = PaneBuilders.textBuilder();
                for (final Map.Entry<Skill, Tuple<Integer, Double>> entry : citizen.getCitizenSkillHandler().getSkills().entrySet())
                {
                    final String skillName = entry.getKey().name().toLowerCase(Locale.US);
                    final int skillLevel = entry.getValue().getA();

                    textBuilder.append(Component.translatable("com.minecolonies.coremod.gui.citizen.skills." + skillName));
                    textBuilder.append(Component.literal(": " + skillLevel + " "));
                }
                textBuilder.newLine();

                PaneBuilders.tooltipBuilder().hoverPane(button).build().setText(textBuilder.build().getText());
                if (selectedCitizen == citizen)
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

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        updateCitizens();
    }

    @Override
    protected String getWindowId()
    {
        return BUTTON_CITIZENS;
    }
}
