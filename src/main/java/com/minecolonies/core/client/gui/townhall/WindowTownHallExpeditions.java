package com.minecolonies.core.client.gui.townhall;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.ButtonHandler;
import com.ldtteam.blockui.controls.Text;
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
    private static final String LIST_ACTIVE_EXPEDITIONS = "active_expeditions";
    private static final String LABEL_EXPEDITION_NAME   = "expedition_name";
    private static final String LABEL_EXPEDITION_STATUS = "expedition_status";
    private static final String BUTTON_EXPEDITION_OPEN  = "expedition_open";

    /**
     * The client side colony data
     */
    private final IColonyView colony;

    /**
     * Constructor for a town hall rename entry window.
     *
     * @param colony {@link ColonyView}
     */
    public WindowTownHallExpeditions(final IColonyView colony)
    {
        super(Constants.MOD_ID + RESOURCE_SUFFIX);
        this.colony = colony;

        setupActiveExpeditionsList();
    }

    /**
     * Set up the list for the active expeditions.
     */
    private void setupActiveExpeditionsList()
    {
        final ScrollingList expeditionsList = findPaneOfTypeByID(LIST_ACTIVE_EXPEDITIONS, ScrollingList.class);
        expeditionsList.setDataProvider(new DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return colony.getExpeditionManager().getActiveExpeditions().size();
            }

            @Override
            public void updateElement(final int index, final Pane pane)
            {
                final ColonyExpedition expedition = colony.getExpeditionManager().getActiveExpeditions().get(index);

                final ColonyExpeditionType expeditionType = ColonyExpeditionTypeManager.getInstance().getExpeditionType(expedition.getExpeditionTypeId());
                if (expeditionType == null)
                {
                    return;
                }

                pane.findPaneOfTypeByID(LABEL_EXPEDITION_NAME, Text.class).setText(expeditionType.getName());

                final MutableComponent statusComponent =
                  Component.translatable(EXPEDITION_TOWNHALL_LIST_STATUS + expedition.getStatus().name()).withStyle(expedition.getStatus().getStatusType().getDisplayColor());
                pane.findPaneOfTypeByID(LABEL_EXPEDITION_STATUS, Text.class).setText(statusComponent);
            }
        });
    }
}
