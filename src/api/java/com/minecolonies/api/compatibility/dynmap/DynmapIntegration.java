package com.minecolonies.api.compatibility.dynmap;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.compatibility.dynmap.area.AreaGenerator;
import com.minecolonies.api.compatibility.dynmap.area.ColonyArea;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.storage.ServerLevelData;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static com.minecolonies.api.compatibility.dynmap.DynmapConstants.*;

/**
 * The core integration class for Dynmap, contains all the logic from generating and modifying markers.
 */
public class DynmapIntegration
{
    private final MarkerSet colonySet;

    /**
     * Constructor containing the {@link MarkerAPI} which is received from the {@link DynmapApiListener}.
     *
     * @param markerApi The {@link MarkerAPI} instance.
     */
    public DynmapIntegration(MarkerAPI markerApi)
    {
        MarkerSet colonySetInternal = markerApi.getMarkerSet(DYNMAP_COLONY_MARKER_SET_ID);
        if (colonySetInternal == null)
        {
            colonySetInternal = markerApi.createMarkerSet(DYNMAP_COLONY_MARKER_SET_ID, DYNMAP_COLONY_MARKER_SET_NAME, null, false);
        }
        this.colonySet = colonySetInternal;

        List<IColony> colonies = IMinecoloniesAPI.getInstance().getColonyManager().getAllColonies();
        colonies.forEach(this::createColony);
    }

    /**
     * Creates a colony marker on the map, based on the provided colony.
     *
     * @param colony The colony.
     */
    public void createColony(final IColony colony)
    {
        String colonyId = getColonyId(colony);
        ServerLevelData levelData = (ServerLevelData) colony.getWorld().getLevelData();

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

    /**
     * Updates the description popover for a colony on the map.
     * Includes changes to the name, owner and citizen count.
     *
     * @param colony The colony.
     */
    public void updateDescription(final IColony colony)
    {
        String colonyId = getColonyId(colony);
        AreaMarker colonyMarker = colonySet.findAreaMarker(colonyId);

        if (colonyMarker != null)
        {
            colonyMarker.setDescription(DYNMAP_COLONY_DESCRIPTION_FORMAT.formatted(
              colony.getName(),
              colony.getPermissions().getOwnerName(),
              colony.getCitizenManager().getCurrentCitizenCount()
            ));
        }
    }

    /**
     * Updates the team color for a given colony.
     * Changes the line and background style for the colony marker.
     *
     * @param colony The colony.
     */
    public void updateTeamColor(final IColony colony)
    {
        String colonyId = getColonyId(colony);
        AreaMarker colonyMarker = colonySet.findAreaMarker(colonyId);

        if (colonyMarker != null)
        {
            Integer color = colony.getTeam().getColor().getColor();
            if (color == null)
            {
                color = Objects.requireNonNull(ChatFormatting.WHITE.getColor());
            }
            colonyMarker.setLineStyle(colonyMarker.getLineWeight(), colonyMarker.getLineOpacity(), getBorderColor(color));
            colonyMarker.setFillStyle(colonyMarker.getFillOpacity(), color);
        }
    }

    /**
     * Updates the borders for the given colony. This will recalculate the borders by using the {@link AreaGenerator}.
     *
     * @param colony The colony.
     */
    public void updateBorders(final IColony colony)
    {
        String colonyId = getColonyId(colony);
        AreaMarker colonyMarker = colonySet.findAreaMarker(colonyId);

        if (colonyMarker != null)
        {
            Collection<ChunkPos> claimedChunks = ColonyChunkClaimCalculator.getAllClaimedChunks(colony);
            ColonyArea area = AreaGenerator.generateAreaFromChunks(claimedChunks);
            colonyMarker.setCornerLocations(area.toXArray(), area.toZArray());
        }
    }

    private int getBorderColor(int color)
    {
        // We cannot directly call `Color.getRGB()` because this includes the alpha bytes, which Dynmap it's line style does not expect.
        // Thus, we manually have to extract the RGB bits.
        Color darkerColor = new Color(color).darker();
        return (darkerColor.getRed() & 0xff) << 16 | (darkerColor.getGreen() & 0xff) << 8 | darkerColor.getBlue() & 0xff;
    }

    public void updateName(final IColony colony)
    {
        AreaMarker colonyMarker = colonySet.findAreaMarker(getColonyId(colony));

        if (colonyMarker != null)
        {
            colonyMarker.setLabel(colony.getName());
            updateDescription(colony);
        }
    }

    public void deleteColony(final IColony colony)
    {
        String colonyId = getColonyId(colony);
        AreaMarker colonyMarker = colonySet.findAreaMarker(colonyId);

        if (colonyMarker != null)
        {
            colonyMarker.deleteMarker();
        }
    }
}