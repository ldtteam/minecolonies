package com.minecolonies.api.compatibility.dynmap.area;

import net.minecraft.world.level.ChunkPos;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Collection;

/**
 * The Dynmap area generator, responsible for turning a collection of chunks into an
 * array of X and Z points in order for Dynmap to generate an area marker.
 */
public class AreaGenerator
{
    private AreaGenerator() {}

    /**
     * Generate an area based on a collection of chunks by using an {@link Area} from AWT geometry classes.
     *
     * @param chunks A collection of chunks.
     * @return A {@link ColonyArea} instance containing an array of X and Z coordinates.
     */
    public static ColonyArea generateAreaFromChunks(Collection<ChunkPos> chunks)
    {
        ArrayList<ColonyArea> holes = new ArrayList<>();
        ColonyArea colonyArea = null;

        Area area = createArea(chunks);
        PathIterator iterator = area.getPathIterator(null);
        final double[] points = new double[6];
        while (!iterator.isDone())
        {
            final int type = iterator.currentSegment(points);
            if (type == PathIterator.SEG_MOVETO)
            {
                if (colonyArea != null)
                {
                    holes.add(colonyArea);
                }
                colonyArea = new ColonyArea();
            }

            if (colonyArea != null && (type == PathIterator.SEG_MOVETO || type == PathIterator.SEG_LINETO))
            {
                colonyArea.addPoint(points[0], points[1]);
            }

            iterator.next();
        }

        if (colonyArea == null)
        {
            throw new IllegalArgumentException("Generator could not create an area, did you pass an empty list of chunks?");
        }

        for (ColonyArea hole : holes)
        {
            colonyArea.addHole(hole);
        }

        colonyArea.close();

        return colonyArea;
    }

    private static Area createArea(Collection<ChunkPos> chunks)
    {
        final Area area = new Area();
        for (final ChunkPos chunkPos : chunks)
        {
            area.add(new Area(new Rectangle(chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(), 16, 16)));
        }
        return area;
    }
}