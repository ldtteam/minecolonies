package com.minecolonies.api.compatibility.dynmap.area;

import net.minecraft.world.level.ChunkPos;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Collection;

public class AreaGenerator
{
    private AreaGenerator()
    {
    }

    public static ColonyArea generateAreaFromChunks(Collection<ChunkPos> chunks)
    {
        var holes = new ArrayList<ColonyArea>();
        ColonyArea colonyArea = null;

        var area = createArea(chunks);
        var iterator = area.getPathIterator(null);
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

        for (var hole : holes)
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