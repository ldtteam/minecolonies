package com.minecolonies.coremod.client.render.worldevent;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.client.StructureClientHandler;
import com.ldtteam.structurize.helpers.Settings;
import com.ldtteam.structurize.helpers.WallExtents;
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
        final Blueprint structure = Settings.instance.getActiveStructure();
        if (structure != null && ctx.hasNearestColony() && Settings.instance.getStructureName() != null
            && Settings.instance.getStructureName().contains(WAYPOINT_STRING))
        {
            if (wayPointTemplate == null)
            {
                final PlacementSettings settings = new PlacementSettings(Settings.instance.getMirror(),
                    BlockPosUtil.getRotationFromRotations(Settings.instance.getRotation()), new WallExtents());

                wayPointTemplate = new LoadOnlyStructureHandler(ctx.clientLevel,
                    BlockPos.ZERO,
                    "schematics/infrastructure/waypoint",
                    settings,
                    true).getBluePrint();
            }

            StructureClientHandler.renderStructureAtPosList(
                Settings.instance.getActiveStructure().hashCode() == wayPointTemplate.hashCode() ? Settings.instance.getActiveStructure()
                    : wayPointTemplate,
                ctx.partialTicks,
                new ArrayList<>(ctx.nearestColony.getWayPoints().keySet()),
                ctx.poseStack);
        }
    }
}
