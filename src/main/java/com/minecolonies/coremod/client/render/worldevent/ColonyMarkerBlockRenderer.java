package com.minecolonies.coremod.client.render.worldevent;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.client.StructureClientHandler;
import com.ldtteam.structurize.helpers.Settings;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.LoadOnlyStructureHandler;
import net.minecraft.core.BlockPos;
import java.util.ArrayList;
import java.util.Objects;

import static com.minecolonies.api.util.constant.CitizenConstants.INFRASTRUCTURE_DIRECTORY;

public class ColonyMarkerBlockRenderer
{
    /**
     * Cached wayPointBlueprint.
     */
    private static Blueprint wayPointTemplate;

    /**
     * Cached gatewayMarkerBlueprint.
     */
    private static Blueprint gatewayMarkerTemplate;

    /**
     * Renders waypoints of current colony.
     * 
     * @param ctx rendering context
     */
    static void render(final WorldEventContext ctx)
    {
        final Blueprint structure = Settings.instance.getActiveStructure();
        if (structure != null && ctx.hasNearestColony() && Settings.instance.getStructureName() != null
            && Settings.instance.getStructureName().contains(INFRASTRUCTURE_DIRECTORY))
        {
            if (wayPointTemplate == null)
            {
                final PlacementSettings settings = new PlacementSettings(Settings.instance.getMirror(),
                    BlockPosUtil.getRotationFromRotations(Settings.instance.getRotation()));

                wayPointTemplate = new LoadOnlyStructureHandler(ctx.clientLevel,
                    BlockPos.ZERO,
                    "schematics/infrastructure/waypoint",
                    settings,
                    true).getBluePrint();
            }

            if (gatewayMarkerTemplate == null)
            {
                final PlacementSettings settings = new PlacementSettings(Settings.instance.getMirror(),
                  BlockPosUtil.getRotationFromRotations(Settings.instance.getRotation()));

                gatewayMarkerTemplate = new LoadOnlyStructureHandler(ctx.clientLevel,
                  BlockPos.ZERO,
                  "schematics/infrastructure/gatewayMarker",
                  settings,
                  true).getBluePrint();
            }

            StructureClientHandler.renderStructureAtPosList(
                Settings.instance.getActiveStructure().hashCode() == wayPointTemplate.hashCode() ? Settings.instance.getActiveStructure()
                    : wayPointTemplate,
                ctx.partialTicks,
                new ArrayList<>(Objects.requireNonNull(ctx.nearestColony).getWayPoints().keySet()),
                ctx.poseStack);

            StructureClientHandler.renderStructureAtPosList(
              Settings.instance.getActiveStructure().hashCode() == gatewayMarkerTemplate.hashCode() ? Settings.instance.getActiveStructure()
                : gatewayMarkerTemplate,
              ctx.partialTicks,
              new ArrayList<>(ctx.nearestColony.getGateMarkers().keySet()),
              ctx.poseStack);
        }
    }

}
