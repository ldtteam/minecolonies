package com.minecolonies.core.client.gui.townhall;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.views.Box;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.blockui.views.ScrollingList.DataProvider;
import com.ldtteam.blockui.views.View;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.expeditions.ExpeditionStatusType;
import com.minecolonies.api.colony.expeditions.MobKill;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.AbstractWindowSkeleton;
import com.minecolonies.core.colony.ColonyView;
import com.minecolonies.core.colony.expeditions.ExpeditionStage;
import com.minecolonies.core.colony.expeditions.colony.ColonyExpedition;
import com.minecolonies.core.colony.expeditions.colony.ColonyExpeditionType;
import com.minecolonies.core.colony.expeditions.colony.ColonyExpeditionTypeManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.minecolonies.api.util.constant.ExpeditionConstants.EXPEDITION_TOWNHALL_LIST_STATUS;

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
    private static final String LIST_ACTIVE_EXPEDITIONS               = "active_expeditions";
    private static final String LIST_FINISHED_EXPEDITIONS             = "finished_expeditions";
    private static final String LABEL_EXPEDITION_NAME                 = "expedition_name";
    private static final String LABEL_EXPEDITION_STATUS               = "expedition_status";
    private static final String BUTTON_EXPEDITION_OPEN                = "expedition_open";
    private static final String LABEL_EMPTY                           = "empty_text";
    private static final String VIEW_EXPEDITION_DETAILS               = "expedition_details";
    private static final String LIST_EXPEDITION_ITEMS                 = "expedition_items";
    private static final String STATUS_EXPEDITION_RESULTS             = "expedition_results";
    private static final String LIST_EXPEDITION_RESULTS               = "expedition_results_list";
    private static final String LIST_EXPEDITION_RESULTS_CHILD_HEADER  = "child_header";
    private static final String LIST_EXPEDITION_RESULTS_CHILD_REWARDS = "child_rewards";
    private static final String LIST_EXPEDITION_RESULTS_CHILD_KILLS   = "child_kills";
    private static final String PARTIAL_ITEM_PREFIX                   = "item_";

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

    /**
     * The current opened expedition data.
     */
    @Nullable
    private OpenedExpeditionInfo openedExpedition;

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
        setupExpeditionList(activeExpeditionsList, colony.getExpeditionManager()::getActiveExpeditions, true);
        this.finishedExpeditionsList = findPaneOfTypeByID(LIST_FINISHED_EXPEDITIONS, ScrollingList.class);
        setupExpeditionList(finishedExpeditionsList, colony.getExpeditionManager()::getFinishedExpeditions, false);

        registerButton(BUTTON_EXPEDITION_OPEN, this::openExpedition);
        updateOpenedExpedition();
    }

    /**
     * Set up an expedition list.
     *
     * @param list             the scrolling list element.
     * @param expeditionGetter the getter for the list of expeditions.
     * @param isActive         whether this list is the active or finished list.
     */
    private void setupExpeditionList(final ScrollingList list, final Supplier<List<ColonyExpedition>> expeditionGetter, final boolean isActive)
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

                pane.findPaneOfTypeByID(LABEL_EXPEDITION_NAME, Text.class).setText(expeditionType.getName());

                final MutableComponent statusComponent =
                  Component.translatable(EXPEDITION_TOWNHALL_LIST_STATUS + expedition.getStatus().name()).withStyle(expedition.getStatus().getStatusType().getDisplayColor());
                pane.findPaneOfTypeByID(LABEL_EXPEDITION_STATUS, Text.class).setText(statusComponent);

                final boolean isOpenActive = openedExpedition == null || openedExpedition.isActive != isActive || openedExpedition.listIndex != index;
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
            openedExpedition = new OpenedExpeditionInfo(colony.getExpeditionManager().getActiveExpeditions().get(activeListIndex), activeListIndex, true);
            return;
        }

        final int finishedListIndex = finishedExpeditionsList.getListElementIndexByPane(container);
        if (finishedListIndex != -1)
        {
            openedExpedition = new OpenedExpeditionInfo(colony.getExpeditionManager().getFinishedExpeditions().get(finishedListIndex), finishedListIndex, false);
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
            final ColonyExpeditionType expeditionType = ColonyExpeditionTypeManager.getInstance().getExpeditionType(openedExpedition.instance.getExpeditionTypeId());
            if (expeditionType == null)
            {
                return;
            }

            detailsContainer.findPaneOfTypeByID(LABEL_EXPEDITION_NAME, Text.class).setText(expeditionType.getName());

            final MutableComponent statusComponent =
              Component.translatable(EXPEDITION_TOWNHALL_LIST_STATUS + openedExpedition.instance.getStatus().name())
                .withStyle(openedExpedition.instance.getStatus().getStatusType().getDisplayColor());
            detailsContainer.findPaneOfTypeByID(LABEL_EXPEDITION_STATUS, Text.class).setText(statusComponent);

            final ScrollingList itemsList = detailsContainer.findPaneOfTypeByID(LIST_EXPEDITION_ITEMS, ScrollingList.class);
            itemsList.setDataProvider(new DataProvider()
            {
                @Override
                public int getElementCount()
                {
                    return (int) Math.ceil(openedExpedition.instance.getEquipment().size() / (double) ITEMS_PER_ROW);
                }

                @Override
                public void updateElement(final int i, final Pane pane)
                {
                    for (int colIndex = 0; colIndex < ITEMS_PER_ROW; colIndex++)
                    {
                        final int itemIndex = colIndex * (i + 1);
                        if (openedExpedition.instance.getEquipment().size() <= itemIndex)
                        {
                            break;
                        }

                        final ItemStack item = openedExpedition.instance.getEquipment().get(itemIndex);
                        pane.findPaneOfTypeByID(PARTIAL_ITEM_PREFIX + colIndex, ItemIcon.class).setItem(item);
                    }
                }
            });

            if (openedExpedition.instance.getStatus().getStatusType().equals(ExpeditionStatusType.SUCCESSFUL))
            {
                findPaneOfTypeByID(STATUS_EXPEDITION_RESULTS, View.class).on();

                final List<OpenedExpeditionResultData> rows = getResultRowData();

                final ScrollingList resultsList = detailsContainer.findPaneOfTypeByID(LIST_EXPEDITION_RESULTS, ScrollingList.class);
                resultsList.setDataProvider(new DataProvider()
                {
                    @Override
                    public int getElementCount()
                    {
                        return rows.size();
                    }

                    @Override
                    public void updateElement(final int i, final Pane pane)
                    {
                        final OpenedExpeditionResultData rowData = rows.get(i);
                        final ExpeditionStage currentStage = openedExpedition.instance.getResults().get(rowData.stageIndex);

                        final Text childHeader = pane.findPaneOfTypeByID(LIST_EXPEDITION_RESULTS_CHILD_HEADER, Text.class);
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
                                childHeader.setText(Component.empty());
                            }
                            case REWARDS ->
                            {
                                childRewards.on();

                                for (int colIndex = 0; colIndex < ITEMS_PER_ROW; colIndex++)
                                {
                                    final int itemIndex = colIndex + rowData.listOffsetIndex;
                                    if (currentStage.getRewards().size() <= itemIndex)
                                    {
                                        break;
                                    }

                                    final ItemStack item = currentStage.getRewards().get(itemIndex);
                                    pane.findPaneOfTypeByID(PARTIAL_ITEM_PREFIX + colIndex, ItemIcon.class).setItem(item);
                                }
                            }
                            case KILLS ->
                            {
                                childKills.on();

                                for (int colIndex = 0; colIndex < ITEMS_PER_ROW; colIndex++)
                                {
                                    final int itemIndex = colIndex + rowData.listOffsetIndex;
                                    if (currentStage.getKills().size() <= itemIndex)
                                    {
                                        break;
                                    }

                                    final MobKill item = currentStage.getKills().get(itemIndex);
                                    final EntityIcon entityIcon = pane.findPaneOfTypeByID(PARTIAL_ITEM_PREFIX + colIndex, EntityIcon.class);
                                    entityIcon.setEntity(item.entity());
                                    entityIcon.setCount(item.count());
                                }
                            }
                        }
                    }
                });
            }
            else
            {
                findPaneOfTypeByID(STATUS_EXPEDITION_RESULTS, View.class).off();
            }
        }
    }

    private List<OpenedExpeditionResultData> getResultRowData()
    {
        if (openedExpedition == null)
        {
            return new ArrayList<>();
        }

        final List<OpenedExpeditionResultData> results = new ArrayList<>();

        final List<ExpeditionStage> instanceResults = openedExpedition.instance.getResults();
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

    private enum OpenedExpeditionResultType
    {
        HEADER,
        REWARDS,
        KILLS
    }

    /**
     * Container class to hold a reference to the selected expedition, mostly used for the different lists to know whether to disable their open buttons or not.
     *
     * @param instance  the expedition instance.
     * @param listIndex the index in the list.
     * @param isActive  whether this expedition is in the active or finished list.
     */
    private record OpenedExpeditionInfo(ColonyExpedition instance, int listIndex, boolean isActive)
    {
    }

    private record OpenedExpeditionResultData(OpenedExpeditionResultType type, int stageIndex, int listOffsetIndex)
    {

    }
}
