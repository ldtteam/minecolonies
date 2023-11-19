package com.minecolonies.coremod.client.gui.townhall;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.blockui.views.View;
import com.minecolonies.api.colony.ICitizen;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.coremod.network.messages.server.colony.citizen.RecallSingleCitizenMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import org.jetbrains.annotations.NotNull;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.coremod.client.gui.modules.WindowBuilderResModule.BLACK;

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
     * The selected citizen.
     */
    private Entity selectedEntity;

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
        updateHappiness();

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
     * Update the display for the happiness.
     */
    private void updateHappiness()
    {
        final Map<String, Double> happinessMap = new HashMap<>();

        for (final ICitizenDataView data : building.getColony().getCitizens().values())
        {
            for (final String modifier : data.getHappinessHandler().getModifiers())
            {
                happinessMap.put(modifier, happinessMap.getOrDefault(modifier, 0.0) + data.getHappinessHandler().getModifier(modifier).getFactor(null));
            }
        }

        final DecimalFormat df = new DecimalFormat("#.#");
        df.setRoundingMode(RoundingMode.CEILING);

        final String roundedHappiness = df.format(building.getColony().getOverallHappiness());

        final View pane = findPaneOfTypeByID("happinesspage", View.class);
        final Text titleLabel = new Text();
        titleLabel.setSize(136, 11);
        titleLabel.setPosition(25, 42);
        titleLabel.setColors(BLACK);
        titleLabel.setText(Component.translatable("com.minecolonies.coremod.gui.townhall.currenthappiness", roundedHappiness));
        pane.addChild(titleLabel);

        int yPos = 60;
        for (final Map.Entry<String, Double> entry : happinessMap.entrySet())
        {
            final double value = entry.getValue() / building.getColony().getCitizenCount();
            final Image image = new Image();
            image.setSize(11, 11);
            image.setPosition(0, yPos);

            final Text label = new Text();
            label.setSize(136, 11);
            label.setPosition(25, yPos);
            label.setColors(BLACK);
            label.setText(Component.translatable(PARTIAL_HAPPINESS_MODIFIER_NAME + entry.getKey()));

            if (value > 1.0)
            {
                image.setImage(new ResourceLocation(HAPPY_ICON), false);
            }
            else if (value == 1)
            {
                image.setImage(new ResourceLocation(SATISFIED_ICON), false);
            }
            else if (value > 0.75)
            {
                image.setImage(new ResourceLocation(UNSATISFIED_ICON), false);
            }
            else
            {
                image.setImage(new ResourceLocation(UNHAPPY_ICON), false);
            }
            pane.addChild(image);
            pane.addChild(label);
            PaneBuilders.tooltipBuilder().hoverPane(label).append(Component.translatable("com.minecolonies.coremod.gui.townhall.happiness.desc." + entry.getKey())).build();

            yPos += 12;
        }
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

        selectedEntity = Minecraft.getInstance().level.getEntity(selectedCitizen.getEntityId());
        if (selectedEntity != null && selectedEntity.getPose() == Pose.SLEEPING)
        {
            final EntityIcon entityIcon = findPaneOfTypeByID(ENTITY_ICON, EntityIcon.class);
            entityIcon.setEntity(selectedEntity);
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
        final EntityIcon entityIcon = findPaneOfTypeByID(ENTITY_ICON, EntityIcon.class);
        if (selectedEntity != null && selectedEntity.getPose() == Pose.SLEEPING)
        {
            entityIcon.hide();
        }
        else
        {
            entityIcon.show();
        }
    }

    @Override
    protected String getWindowId()
    {
        return BUTTON_CITIZENS;
    }
}
