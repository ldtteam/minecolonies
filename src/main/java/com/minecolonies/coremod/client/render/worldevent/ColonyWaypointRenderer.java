package com.minecolonies.coremod.client.render.worldevent;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.client.StructureClientHandler;
import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.minecolonies.api.util.constant.CitizenConstants.WAYPOINT_STRING;

public class ColonyWaypointRenderer
{
    /**
     * Cached wayPointBlueprint.
     */
    private static Blueprint wayPointTemplate;

    /**
     * Pending template to be loaded.
     */
    private static Future<Blueprint> pendingTemplate;

    /**
     * Renders waypoints of current colony.
     * 
     * @param ctx rendering context
     */
    static void render(final WorldEventContext ctx)
    {
        final Blueprint structure = RenderingCache.getOrCreateBlueprintPreviewData("blueprint").getBlueprint();
        if (structure != null && structure.getFilePath().toString().contains(WAYPOINT_STRING) && ctx.nearestColony != null)
        {
            if (wayPointTemplate == null && pendingTemplate == null)
            {
                pendingTemplate = StructurePacks.getBlueprintFuture("Default", "infrastructure/roads/waypoint.blueprint");
            }

            if (pendingTemplate != null)
            {
                if (pendingTemplate.isDone())
                {
                    try
                    {
                        wayPointTemplate = pendingTemplate.get();
                        pendingTemplate = null;
                    }
                    catch (InterruptedException | ExecutionException e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    return;
                }
            }

            if (wayPointTemplate == null)
            {
                return;
            }

            StructureClientHandler.renderStructureAtPosList(
              RenderingCache.getOrCreateBlueprintPreviewData("blueprint").getBlueprint().hashCode() == wayPointTemplate.hashCode() ? RenderingCache.getOrCreateBlueprintPreviewData("blueprint").getBlueprint()
                    : wayPointTemplate,
                ctx.partialTicks,
                new ArrayList<>(ctx.nearestColony.getWayPoints().keySet()),
                ctx.poseStack);
        }
    }
}
