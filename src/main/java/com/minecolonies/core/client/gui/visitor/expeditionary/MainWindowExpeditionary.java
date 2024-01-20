package com.minecolonies.core.client.gui.visitor.expeditionary;

import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.Image;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.View;
import com.minecolonies.api.colony.IVisitorViewData;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.AbstractWindowSkeleton;
import com.minecolonies.core.colony.expeditions.colony.ColonyExpeditionType;
import com.minecolonies.core.colony.expeditions.colony.ColonyExpeditionType.Difficulty;
import com.minecolonies.core.entity.visitor.ExpeditionaryVisitorType;
import com.minecolonies.core.network.messages.server.colony.OpenInventoryMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static com.minecolonies.api.util.constant.WindowConstants.EXPEDITIONARY_MAIN_RESOURCE_SUFFIX;
import static com.minecolonies.api.util.constant.translation.GuiTranslationConstants.EXPEDITIONARY_DIFFICULTY;
import static com.minecolonies.api.util.constant.translation.GuiTranslationConstants.EXPEDITIONARY_DIFFICULTY_PREFIX;

/**
 * Main window for the expeditionary their GUI.
 */
public class MainWindowExpeditionary extends AbstractWindowSkeleton
{
    /**
     * Window constants.
     */
    private static final String LABEL_EXPEDITION_NAME      = "expedition_name";
    private static final String VIEW_EXPEDITION_DIFFICULTY = "expedition_difficulty";

    /**
     * The visitor data.
     */
    @NotNull
    private final IVisitorViewData visitorData;

    /**
     * The current expedition type.
     */
    private final ColonyExpeditionType expeditionType;

    /**
     * Default constructor.
     */
    public MainWindowExpeditionary(final @NotNull IVisitorViewData visitorData)
    {
        super(Constants.MOD_ID + EXPEDITIONARY_MAIN_RESOURCE_SUFFIX);
        this.visitorData = visitorData;
        this.expeditionType = visitorData.getExtraDataValue(ExpeditionaryVisitorType.EXTRA_DATA_EXPEDITION_TYPE).orElseThrow();

        findPaneOfTypeByID(LABEL_EXPEDITION_NAME, Text.class).setText(expeditionType.getName());

        updateDifficulty();

        registerButton(LABEL_EXPEDITION_NAME, this::startExpedition);
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
    }

    /**
     * Triggers starting the expedition.
     */
    private void startExpedition()
    {
        Network.getNetwork().sendToServer(new OpenInventoryMessage(visitorData.getColony(), visitorData.getName(), visitorData.getId()));
    }

    /**
     * Update the difficulty icons.
     */
    private void updateDifficulty()
    {
        final int maxDifficulty = Arrays.stream(Difficulty.values())
                                    .filter(m -> m.equals(expeditionType.getDifficulty()) || !m.isHidden())
                                    .mapToInt(Difficulty::getLevel)
                                    .max()
                                    .orElse(0);
        final Difficulty currentDifficulty = expeditionType.getDifficulty();

        for (int i = currentDifficulty.getLevel(); i <= maxDifficulty; i++)
        {
            findPaneOfTypeByID("diff_" + i, Image.class).setVisible(true);
        }

        for (int i = 1; i <= currentDifficulty.getLevel(); i++)
        {
            final Image iconPane = findPaneOfTypeByID("diff_" + i, Image.class);
            iconPane.setVisible(true);
            iconPane.setImage(new ResourceLocation("textures/item/" + currentDifficulty.getIcon().toString() + ".png"), false);
        }

        PaneBuilders.tooltipBuilder()
          .append(Component.translatable(EXPEDITIONARY_DIFFICULTY, Component.translatable(EXPEDITIONARY_DIFFICULTY_PREFIX + currentDifficulty.getKey()))
                    .withStyle(currentDifficulty.getStyle()))
          .hoverPane(findPaneOfTypeByID(VIEW_EXPEDITION_DIFFICULTY, View.class))
          .build();
    }
}