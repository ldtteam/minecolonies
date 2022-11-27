package com.minecolonies.api.compatibility.dynmap;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.compatibility.dynmap.area.AreaGenerator;
import net.minecraft.world.level.storage.ServerLevelData;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import java.awt.*;

import static com.minecolonies.api.compatibility.dynmap.Constants.*;

public class DynmapIntegration
{
    private final MarkerSet colonySet;

    public DynmapIntegration(MarkerAPI markerApi)
    {
        var colonySetInternal = markerApi.getMarkerSet(DYNMAP_COLONY_MARKER_SET_ID);
        if (colonySetInternal == null)
        {
            colonySetInternal = markerApi.createMarkerSet(DYNMAP_COLONY_MARKER_SET_ID, DYNMAP_COLONY_MARKER_SET_NAME, null, false);
        }
        this.colonySet = colonySetInternal;

        var colonies = IMinecoloniesAPI.getInstance().getColonyManager().getAllColonies();
        colonies.forEach(this::createColony);
    }

    public void createColony(final IColony colony)
    {
        var colonyId = getColonyId(colony);
        var levelData = (ServerLevelData) colony.getWorld().getLevelData();

        colonySet.createAreaMarker(
          colonyId,
          colony.getName(),
          false,
          levelData.getLevelName(),
          new double[0],
          new double[0],
          false
        );

        updateDescription(colony);
        updateTeamColor(colony);
        updateBorders(colony);
    }

    private String getColonyId(final IColony colony)
    {
        return String.format(DYNMAP_COLONY_MARKER_FORMAT, colony.getID());
    }

    public void updateDescription(final IColony colony)
    {
        var colonyId = getColonyId(colony);
        var colonyMarker = colonySet.findAreaMarker(colonyId);

        if (colonyMarker != null)
        {
            colonyMarker.setDescription(DYNMAP_COLONY_DESCRIPTION_FORMAT.formatted(
              colony.getName(),
              colony.getPermissions().getOwnerName(),
              colony.getCitizenManager().getCurrentCitizenCount()
            ));
        }
    }

    public void updateTeamColor(final IColony colony)
    {
        var colonyId = getColonyId(colony);
        var colonyMarker = colonySet.findAreaMarker(colonyId);

        if (colonyMarker != null)
        {
            var color = colony.getTeam().getColor().getColor();
            if (color == null)
            {
                color = 16777215;
            }
            colonyMarker.setLineStyle(colonyMarker.getLineWeight(), colonyMarker.getLineOpacity(), getBorderColor(color));
            colonyMarker.setFillStyle(colonyMarker.getFillOpacity(), color);
        }
    }

    public void updateBorders(final IColony colony)
    {
        var colonyId = getColonyId(colony);
        var colonyMarker = colonySet.findAreaMarker(colonyId);

        if (colonyMarker != null)
        {
            var claimedChunks = ColonyChunkClaimCalculator.getAllClaimedChunks(colony);
            var area = AreaGenerator.generateAreaFromChunks(claimedChunks);
            colonyMarker.setCornerLocations(area.toXArray(), area.toZArray());
        }
    }

    private int getBorderColor(int color)
    {
        // We cannot directly call `Color.getRGB()` because this includes the alpha bytes, which Dynmap it's line style does not expect.
        // Thus, we manually have to extract the RGB bits.
        var darkerColor = new Color(color).darker();
        return (darkerColor.getRed() & 0xff) << 16 | (darkerColor.getGreen() & 0xff) << 8 | darkerColor.getBlue() & 0xff;
    }

    public void deleteColony(final IColony colony)
    {
        var colonyId = getColonyId(colony);
        var colonyMarker = colonySet.findAreaMarker(colonyId);

        if (colonyMarker != null)
        {
            colonyMarker.deleteMarker();
        }
    }

    public void updateName(final IColony colony)
    {
        var colonyMarker = colonySet.findAreaMarker(getColonyId(colony));

        if (colonyMarker != null)
        {
            colonyMarker.setLabel(colony.getName());
            updateDescription(colony);
        }
    }
}