package com.minecolonies.coremod.client.render.worldevent;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.client.StructureClientHandler;
import com.ldtteam.structurize.helpers.OldSettings;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.LoadOnlyStructureHandler;
import net.minecraft.core.BlockPos;
import java.util.ArrayList;
import static com.minecolonies.api.util.constant.CitizenConstants.WAYPOINT_STRING;

public class ColonyWaypointRenderer
{
    /**
     * Cached wayPointBlueprint.
     */
    private static Blueprint wayPointTemplate;

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
            if (wayPointTemplate == null)
            {
                wayPointTemplate = new LoadOnlyStructureHandler(ctx.clientLevel,
                    BlockPos.ZERO,
                    "schematics/infrastructure/waypoint",
                  new PlacementSettings(),
                    true).getBluePrint();
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
