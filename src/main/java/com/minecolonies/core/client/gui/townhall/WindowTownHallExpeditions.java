package com.minecolonies.core.client.gui.townhall;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.views.Box;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.blockui.views.ScrollingList.DataProvider;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.AbstractWindowSkeleton;
import com.minecolonies.core.colony.ColonyView;
import com.minecolonies.core.colony.expeditions.colony.ColonyExpedition;
import com.minecolonies.core.colony.expeditions.colony.ColonyExpeditionType;
import com.minecolonies.core.colony.expeditions.colony.ColonyExpeditionTypeManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

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
    private static final String LIST_ACTIVE_EXPEDITIONS        = "active_expeditions";
    private static final String LIST_FINISHED_EXPEDITIONS      = "finished_expeditions";
    private static final String LABEL_EXPEDITION_NAME          = "expedition_name";
    private static final String LABEL_EXPEDITION_STATUS        = "expedition_status";
    private static final String BUTTON_EXPEDITION_OPEN         = "expedition_open";
    private static final String BUTTON_EXPEDITION_OPEN_OVERLAY = "expedition_open_overlay";

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
        setupExpeditionList(activeExpeditionsList, () -> colony.getExpeditionManager().getActiveExpeditions(), true);
        this.finishedExpeditionsList = findPaneOfTypeByID(LIST_FINISHED_EXPEDITIONS, ScrollingList.class);
        setupExpeditionList(finishedExpeditionsList, () -> colony.getExpeditionManager().getFinishedExpeditions(), false);

        registerButton(BUTTON_EXPEDITION_OPEN, this::openExpedition);
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

                final boolean isOpenActive = openedExpedition == null || openedExpedition.isActive == isActive && openedExpedition.listIndex == index;
                pane.findPaneOfTypeByID(BUTTON_EXPEDITION_OPEN, ButtonImage.class).setEnabled(isOpenActive);
                pane.findPaneOfTypeByID(BUTTON_EXPEDITION_OPEN_OVERLAY, Image.class).setEnabled(isOpenActive);
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
}
