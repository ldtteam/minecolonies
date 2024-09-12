package com.minecolonies.core.client.gui.townhall;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.views.Box;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.blockui.views.ScrollingList.DataProvider;
import com.ldtteam.blockui.views.View;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.expeditions.ExpeditionStatus;
import com.minecolonies.api.colony.expeditions.MobKill;
import com.minecolonies.api.colony.managers.interfaces.expeditions.ColonyExpedition;
import com.minecolonies.api.colony.managers.interfaces.expeditions.FinishedExpedition;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.AbstractWindowSkeleton;
import com.minecolonies.core.colony.ColonyView;
import com.minecolonies.core.colony.expeditions.ExpeditionStage;
import com.minecolonies.core.colony.expeditions.colony.types.ColonyExpeditionType;
import com.minecolonies.core.colony.expeditions.colony.types.ColonyExpeditionTypeDifficulty;
import com.minecolonies.core.colony.expeditions.colony.types.ColonyExpeditionTypeManager;
import com.minecolonies.core.colony.expeditions.encounters.ExpeditionEncounter;
import com.minecolonies.core.colony.expeditions.encounters.ExpeditionEncounterManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.minecolonies.api.util.constant.ExpeditionConstants.*;

/**
 * Gui for viewing past expeditions.
 */
public class WindowTownHallExpeditions extends AbstractWindowSkeleton implements ButtonHandler
{
    /**
     * The xml file for this gui
     */
    private static final String RESOURCE_SUFFIX = ":gui/townhall/windowtownhallexpeditions.xml";

    /**
     * ID constants.
     */
    private static final String LIST_ACTIVE_EXPEDITIONS                   = "active_expeditions";
    private static final String LIST_FINISHED_EXPEDITIONS                 = "finished_expeditions";
    private static final String LABEL_EXPEDITION_NAME                     = "expedition_name";
    private static final String IMAGE_EXPEDITION_DIFFICULTY               = "expedition_difficulty";
    private static final String LABEL_EXPEDITION_STATUS                   = "expedition_status";
    private static final String BUTTON_EXPEDITION_OPEN                    = "expedition_open";
    private static final String LABEL_EMPTY                               = "empty_text";
    private static final String VIEW_EXPEDITION_DETAILS                   = "expedition_details";
    private static final String LIST_EXPEDITION_ITEMS                     = "expedition_items";
    private static final String STATUS_EXPEDITION_RESULTS                 = "expedition_results";
    private static final String LIST_EXPEDITION_RESULTS                   = "expedition_results_list";
    private static final String LIST_EXPEDITION_RESULTS_CHILD_HEADER      = "child_header";
    private static final String LIST_EXPEDITION_RESULTS_CHILD_HEADER_TEXT = "header";
    private static final String LIST_EXPEDITION_RESULTS_CHILD_REWARDS     = "child_rewards";
    private static final String LIST_EXPEDITION_RESULTS_CHILD_KILLS       = "child_kills";
    private static final String PARTIAL_ITEM_PREFIX                       = "item_";

    /**
     * The amount of item icons showing on a single list row.
     */
    private static final int ITEMS_PER_ROW = 9;

    /**
     * The client side colony data
     */
    private final IColonyView colony;

    /**
     * The list for the active expeditions.
     */
    private final ScrollingList activeExpeditionsList;

    /**
     * The list for the finished expeditions.
     */
    private final ScrollingList finishedExpeditionsList;

    @Nullable
    private ColonyExpedition openedExpedition;

    /**
     * Constructor for a town hall rename entry window.
     *
     * @param colony {@link ColonyView}
     */
    public WindowTownHallExpeditions(final IColonyView colony)
    {
        super(Constants.MOD_ID + RESOURCE_SUFFIX);
        this.colony = colony;

        this.activeExpeditionsList = findPaneOfTypeByID(LIST_ACTIVE_EXPEDITIONS, ScrollingList.class);
        setupExpeditionList(activeExpeditionsList, colony.getExpeditionManager()::getActiveExpeditions);
        this.finishedExpeditionsList = findPaneOfTypeByID(LIST_FINISHED_EXPEDITIONS, ScrollingList.class);
        setupExpeditionList(finishedExpeditionsList, colony.getExpeditionManager()::getFinishedExpeditions);

        registerButton(BUTTON_EXPEDITION_OPEN, this::openExpedition);
        updateOpenedExpedition();
    }

    /**
     * Set up an expedition list.
     *
     * @param list             the scrolling list element.
     * @param expeditionGetter the getter for the list of expeditions.
     */
    private void setupExpeditionList(final ScrollingList list, final Supplier<List<ColonyExpedition>> expeditionGetter)
    {
        list.setDataProvider(new DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return expeditionGetter.get().size();
            }

            @Override
            public void updateElement(final int index, final Pane pane)
            {
                final ColonyExpedition expedition = expeditionGetter.get().get(index);

                final ColonyExpeditionType expeditionType = ColonyExpeditionTypeManager.getInstance().getExpeditionType(expedition.getExpeditionTypeId());
                if (expeditionType == null)
                {
                    return;
                }

                pane.findPaneOfTypeByID(LABEL_EXPEDITION_NAME, Text.class).setText(expeditionType.name());
                renderDifficultyImage(pane, expeditionType);

                final ExpeditionStatus expeditionStatus = colony.getExpeditionManager().getExpeditionStatus(expedition.getId());
                if (expeditionStatus.equals(ExpeditionStatus.FINISHED))
                {
                    final FinishedExpedition finishedExpedition = colony.getExpeditionManager().getFinishedExpedition(expedition.getId());
                    if (finishedExpedition != null)
                    {
                        final MutableComponent statusComponent = Component.translatable(EXPEDITION_TOWNHALL_LIST_STATUS + finishedExpedition.status().name())
                                                                   .withStyle(finishedExpedition.status().getStatusType().getDisplayColor());
                        pane.findPaneOfTypeByID(LABEL_EXPEDITION_STATUS, Text.class).setText(statusComponent);
                    }
                }
                else if (expeditionStatus.equals(ExpeditionStatus.ONGOING))
                {
                    final MutableComponent statusComponent = Component.translatable(EXPEDITION_TOWNHALL_LIST_STATUS + ExpeditionStatus.ONGOING.name());
                    pane.findPaneOfTypeByID(LABEL_EXPEDITION_STATUS, Text.class).setText(statusComponent);
                }

                final boolean isOpenActive = openedExpedition == null || openedExpedition.getId() != expedition.getId();
                pane.findPaneOfTypeByID(BUTTON_EXPEDITION_OPEN, ButtonImage.class).setEnabled(isOpenActive);
            }
        });
    }

    /**
     * Called when a button is clicked to open expedition details.
     *
     * @param button the button that was clicked.
     */
    private void openExpedition(final Button button)
    {
        final Box container = (Box) button.getParent();
        final int activeListIndex = activeExpeditionsList.getListElementIndexByPane(container);
        if (activeListIndex != -1)
        {
            this.openedExpedition = colony.getExpeditionManager().getActiveExpeditions().get(activeListIndex);
        }

        final int finishedListIndex = finishedExpeditionsList.getListElementIndexByPane(container);
        if (finishedListIndex != -1)
        {
            this.openedExpedition = colony.getExpeditionManager().getFinishedExpeditions().get(finishedListIndex);
        }

        updateOpenedExpedition();
    }

    /**
     * Updates the pane on the right to view the opened expedition.
     */
    private void updateOpenedExpedition()
    {
        findPaneOfTypeByID(LABEL_EMPTY, Text.class).setEnabled(openedExpedition == null);
        findPaneOfTypeByID(LABEL_EMPTY, Text.class).setVisible(openedExpedition == null);

        final View detailsContainer = findPaneOfTypeByID(VIEW_EXPEDITION_DETAILS, View.class);
        detailsContainer.setEnabled(openedExpedition != null);
        detailsContainer.setVisible(openedExpedition != null);

        if (openedExpedition != null)
        {
            final ColonyExpeditionType expeditionType = ColonyExpeditionTypeManager.getInstance().getExpeditionType(openedExpedition.getExpeditionTypeId());
            if (expeditionType == null)
            {
                return;
            }

            detailsContainer.findPaneOfTypeByID(LABEL_EXPEDITION_NAME, Text.class).setText(expeditionType.name());
            renderDifficultyImage(detailsContainer, expeditionType);

            final ExpeditionStatus expeditionStatus = colony.getExpeditionManager().getExpeditionStatus(openedExpedition.getId());
            if (expeditionStatus.equals(ExpeditionStatus.FINISHED))
            {
                final FinishedExpedition finishedExpedition = colony.getExpeditionManager().getFinishedExpedition(openedExpedition.getId());
                if (finishedExpedition != null)
                {
                    final MutableComponent statusComponent = Component.translatable(EXPEDITION_TOWNHALL_LIST_STATUS + finishedExpedition.status().name())
                                                               .withStyle(finishedExpedition.status().getStatusType().getDisplayColor());
                    detailsContainer.findPaneOfTypeByID(LABEL_EXPEDITION_STATUS, Text.class).setText(statusComponent);

                    findPaneOfTypeByID(STATUS_EXPEDITION_RESULTS, View.class).on();

                    final List<OpenedExpeditionResultData> rows = getResultRowData(openedExpedition);
                    detailsContainer.findPaneOfTypeByID(LIST_EXPEDITION_RESULTS, ScrollingList.class).setDataProvider(new DataProvider()
                    {
                        @Override
                        public int getElementCount()
                        {
                            return rows.size();
                        }

                        @Override
                        public boolean shouldUpdate()
                        {
                            return false;
                        }

                        @Override
                        public void updateElement(final int i, final Pane pane)
                        {
                            final OpenedExpeditionResultData rowData = rows.get(i);
                            final ExpeditionStage currentStage = openedExpedition.getResults().get(rowData.stageIndex);

                            final View childHeader = pane.findPaneOfTypeByID(LIST_EXPEDITION_RESULTS_CHILD_HEADER, View.class);
                            final View childRewards = pane.findPaneOfTypeByID(LIST_EXPEDITION_RESULTS_CHILD_REWARDS, View.class);
                            final View childKills = pane.findPaneOfTypeByID(LIST_EXPEDITION_RESULTS_CHILD_KILLS, View.class);

                            childHeader.off();
                            childRewards.off();
                            childKills.off();

                            switch (rowData.type)
                            {
                                case HEADER ->
                                {
                                    childHeader.on();
                                    childHeader.findPaneOfTypeByID(LIST_EXPEDITION_RESULTS_CHILD_HEADER_TEXT, Text.class).setText(currentStage.getHeader());
                                }
                                case REWARDS ->
                                {
                                    childRewards.on();

                                    for (int colIndex = 0; colIndex < ITEMS_PER_ROW; colIndex++)
                                    {
                                        final int itemIndex = colIndex + rowData.listOffsetIndex;
                                        if (currentStage.getRewards().size() > itemIndex)
                                        {
                                            final ItemStack item = currentStage.getRewards().get(itemIndex);
                                            childRewards.findPaneOfTypeByID(PARTIAL_ITEM_PREFIX + colIndex, ItemIcon.class).setItem(item);
                                        }
                                        else
                                        {
                                            childRewards.findPaneOfTypeByID(PARTIAL_ITEM_PREFIX + colIndex, ItemIcon.class).setItem(Items.AIR.getDefaultInstance());
                                        }
                                    }
                                }
                                case KILLS ->
                                {
                                    childKills.on();

                                    for (int colIndex = 0; colIndex < ITEMS_PER_ROW; colIndex++)
                                    {
                                        final int itemIndex = colIndex + rowData.listOffsetIndex;
                                        if (currentStage.getKills().size() > itemIndex)
                                        {
                                            final MobKill item = currentStage.getKills().get(itemIndex);
                                            final EntityIcon entityIcon = childKills.findPaneOfTypeByID(PARTIAL_ITEM_PREFIX + colIndex, EntityIcon.class);

                                            final ExpeditionEncounter encounter = ExpeditionEncounterManager.getInstance().getEncounter(item.encounterId());
                                            if (encounter != null)
                                            {
                                                entityIcon.setEntity(encounter.getEntityType());
                                                entityIcon.setCount(item.count());
                                            }
                                            else
                                            {
                                                entityIcon.resetEntity();
                                                entityIcon.setCount(0);
                                            }
                                        }
                                        else
                                        {
                                            final EntityIcon entityIcon = childKills.findPaneOfTypeByID(PARTIAL_ITEM_PREFIX + colIndex, EntityIcon.class);
                                            entityIcon.resetEntity();
                                            entityIcon.setCount(0);
                                        }
                                    }
                                }
                            }
                        }
                    });
                }
                else
                {
                    findPaneOfTypeByID(STATUS_EXPEDITION_RESULTS, View.class).off();
                    detailsContainer.findPaneOfTypeByID(LIST_EXPEDITION_RESULTS, ScrollingList.class).setDataProvider(null);
                }
            }
            else
            {
                final MutableComponent statusComponent = Component.translatable(EXPEDITION_TOWNHALL_LIST_STATUS + ExpeditionStatus.ONGOING.name());
                detailsContainer.findPaneOfTypeByID(LABEL_EXPEDITION_STATUS, Text.class).setText(statusComponent);

                findPaneOfTypeByID(STATUS_EXPEDITION_RESULTS, View.class).off();
                detailsContainer.findPaneOfTypeByID(LIST_EXPEDITION_RESULTS, ScrollingList.class).setDataProvider(null);
            }

            final ScrollingList itemsList = detailsContainer.findPaneOfTypeByID(LIST_EXPEDITION_ITEMS, ScrollingList.class);
            itemsList.setDataProvider(new DataProvider()
            {
                @Override
                public int getElementCount()
                {
                    return (int) Math.ceil(openedExpedition.getEquipment().size() / (double) ITEMS_PER_ROW);
                }

                @Override
                public void updateElement(final int i, final Pane pane)
                {
                    for (int colIndex = 0; colIndex < ITEMS_PER_ROW; colIndex++)
                    {
                        final int itemIndex = colIndex * (i + 1);
                        if (openedExpedition.getEquipment().size() <= itemIndex)
                        {
                            break;
                        }

                        final ItemStack item = openedExpedition.getEquipment().get(itemIndex);
                        pane.findPaneOfTypeByID(PARTIAL_ITEM_PREFIX + colIndex, ItemIcon.class).setItem(item);
                    }
                }
            });
        }
    }

    /**
     * Render the difficulty image.
     *
     * @param container      the container to find the image in.
     * @param expeditionType the expedition type.
     */
    private void renderDifficultyImage(final Pane container, final ColonyExpeditionType expeditionType)
    {
        final ColonyExpeditionTypeDifficulty difficulty = expeditionType.difficulty();
        container.findPaneOfTypeByID(WindowTownHallExpeditions.IMAGE_EXPEDITION_DIFFICULTY, Image.class)
          .setImage(new ResourceLocation("textures/item/" + difficulty.getIcon().toString() + ".png"), true);

        PaneBuilders.tooltipBuilder()
          .append(Component.translatable(EXPEDITIONARY_DIFFICULTY, Component.translatable(EXPEDITIONARY_DIFFICULTY_PREFIX + difficulty.getKey()))
                    .withStyle(difficulty.getStyle()))
          .hoverPane(container.findPaneOfTypeByID(WindowTownHallExpeditions.IMAGE_EXPEDITION_DIFFICULTY, Image.class))
          .build();
    }

    /**
     * Extract the results data from an expedition instance.
     *
     * @param colonyExpedition the expedition instance.
     * @return the list of data.
     */
    private List<OpenedExpeditionResultData> getResultRowData(@Nullable final ColonyExpedition colonyExpedition)
    {
        if (colonyExpedition == null)
        {
            return new ArrayList<>();
        }

        final List<OpenedExpeditionResultData> results = new ArrayList<>();

        final List<ExpeditionStage> instanceResults = colonyExpedition.getResults();
        for (int stageIndex = 0; stageIndex < instanceResults.size(); stageIndex++)
        {
            final ExpeditionStage stage = instanceResults.get(stageIndex);
            // 1 row for the stage header
            results.add(new OpenedExpeditionResultData(OpenedExpeditionResultType.HEADER, stageIndex, 0));

            // X rows if there's any rewards, amount of rows divided by ITEMS_PER_ROW
            if (!stage.getRewards().isEmpty())
            {
                final int rewardRows = (int) Math.ceil(stage.getRewards().size() / (double) ITEMS_PER_ROW);
                for (int offsetIndex = 0; offsetIndex < rewardRows; offsetIndex++)
                {
                    results.add(new OpenedExpeditionResultData(OpenedExpeditionResultType.REWARDS, stageIndex, offsetIndex));
                }
            }

            // X rows if there's any kills, amount of rows divided by ITEMS_PER_ROW
            if (!stage.getKills().isEmpty())
            {
                final int killsRows = (int) Math.ceil(stage.getKills().size() / (double) ITEMS_PER_ROW);
                for (int offsetIndex = 0; offsetIndex < killsRows; offsetIndex++)
                {
                    results.add(new OpenedExpeditionResultData(OpenedExpeditionResultType.KILLS, stageIndex, offsetIndex));
                }
            }
        }
        return results;
    }

    /**
     * The type of result row.
     */
    private enum OpenedExpeditionResultType
    {
        HEADER,
        REWARDS,
        KILLS
    }

    /**
     * Container class for the opened expedition.
     */
    private record OpenedExpeditionResultData(OpenedExpeditionResultType type, int stageIndex, int listOffsetIndex)
    {
    }
}
