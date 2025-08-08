package com.minecolonies.core.debug.gui;

import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Text;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.AbstractWindowSkeleton;
import com.minecolonies.core.debug.messages.DebugEnablePathfindingMessage;
import com.minecolonies.core.debug.messages.QueryCitizenAIHistoryMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Debug window for citizens
 */
public class DebugWindowCitizen extends AbstractWindowSkeleton
{
    /**
     * Static data holder for responses for now, TODO: rework with citizen modules
     */
    public static MutableComponent outputMessage = Component.empty();

    /**
     * Assigned citizen.
     */
    private final ICitizenDataView citizen;

    /**
     * Whether pathfinding tracking is enabled(not synced!)
     */
    private static boolean trackingDebug = false;

    public DebugWindowCitizen(final ICitizenDataView citizen)
    {
        super(Constants.MOD_ID + ":gui/citizen/debug.xml");
        if (outputMessage == Component.empty())
        {
            outputMessage = Component.literal("Enabled Citizen AI History!");
        }

        this.citizen = citizen;

        findPaneOfTypeByID("citizenid", Text.class).setText(Component.literal("Citizen ID:" + citizen.getId()));
        findPaneOfTypeByID("colonyid", Text.class).setText(Component.literal("Colony ID:" + citizen.getColonyId()));
        findPaneOfTypeByID("aihistory", Button.class).setHandler(b -> Network.getNetwork().sendToServer(new QueryCitizenAIHistoryMessage(citizen)));
        findPaneOfTypeByID("pathfinding", Button.class).setHandler(b -> {
            trackingDebug = !trackingDebug;
            if (trackingDebug)
            {
                outputMessage = Component.literal("Receiving pathfinding data");
            }

            Network.getNetwork().sendToServer(new DebugEnablePathfindingMessage(citizen, trackingDebug));
            findPaneOfTypeByID("pathfinding", Button.class).setText(Component.literal((trackingDebug ? "disable Pathfinding tracking" : "enable Pathfinding tracking")));
        });
        findPaneOfTypeByID("pathfinding", Button.class).setText(Component.literal((trackingDebug ? "disable Pathfinding tracking" : "enable Pathfinding tracking")));
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        findPaneOfTypeByID("output", Text.class).setText(outputMessage);
    }
}
