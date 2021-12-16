package com.minecolonies.coremod.compatibility.journeymap;

import journeymap.client.api.model.MapPolygon;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Helper utility class to simplify making map polygons for JourneyMap
 */
public class PolygonUtils
{
    private PolygonUtils()
    {
    }

    /**
     * A slightly more user-friendly container class than just throwing these in a Tuple...
     */
    public static class PolygonWithHoles
    {
        @NotNull public final MapPolygon hull;
        @NotNull public final List<MapPolygon> holes;

        /**
         * Creates a PolygonWithHoles.
         *
         * @param hull The possibly-concave outer hull of the polygon.
         * @param holes Intersecting polygons representing holes in the hull.
         */
        public PolygonWithHoles(@NotNull final MapPolygon hull,
                                @NotNull final List<MapPolygon> holes)
        {
            this.hull = hull;
            this.holes = holes;
        }
    }

    /**
     * Given a collection of chunks, creates an Area that covers them.
     *
     * @param chunks The set of chunks.
     * @return An Area of the corresponding block coordinates.
     */
    public static @NotNull Area createChunksArea(@NotNull final Collection<ChunkPos> chunks)
    {
        final Area area = new Area();
        for (final ChunkPos chunkPos : chunks)
        {
            area.add(new Area(new Rectangle(chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(), 16, 16)));
        }
        return area;
    }

    /**
     * Converts a {@link MapPolygon} into an {@link Area} (keeping XZ coords only).
     *
     * @param polygon The polygon.
     * @return The corresponding area.
     */
    public static @NotNull Area toArea(@NotNull final MapPolygon polygon)
    {
        final List<BlockPos> points = polygon.getPoints();
        final int[] xPoints = new int[points.size()];
        final int[] yPoints = new int[points.size()];

        for (int i = 0; i < points.size(); ++i)
        {
            xPoints[i] = points.get(i).getX();
            yPoints[i] = points.get(i).getZ();
        }

        return new Area(new Polygon(xPoints, yPoints, points.size()));
    }

    /**
     * Creates a set of PolygonWithHoles from the given Area (XZ block coords) and Y coord.
     *
     * Note that this includes some point-simplification that currently only works if the area is
     * only made up of rectangular subregions -- i.e. all lines are perfectly horizontal or vertical.
     * If you do have diagonal lines this is mostly harmless; just might make more triangles in the end.
     *
     * @param area The area to cover.
     * @param y The y-coordinate.
     * @return The polygons.
     */
    public static @NotNull List<PolygonWithHoles> createPolygonFromArea(@NotNull final Area area, final int y)
    {
        final List<MapPolygon> polygons = new ArrayList<>();
        List<BlockPos> poly = new ArrayList<>();
        final PathIterator iterator = area.getPathIterator(null);
        final float[] points = new float[6];
        while (!iterator.isDone())
        {
            final int type = iterator.currentSegment(points);
            switch (type)
            {
                case PathIterator.SEG_MOVETO:
                    if (!poly.isEmpty())
                    {
                        poly = simplify(poly);
                        polygons.add(new MapPolygon(poly));
                        poly = new ArrayList<>();
                    }
                    poly.add(new BlockPos(Math.round(points[0]), y, Math.round(points[1])));
                    break;
                case PathIterator.SEG_LINETO:
                    poly.add(new BlockPos(Math.round(points[0]), y, Math.round(points[1])));
                    break;
            }
            iterator.next();
        }
        if (!poly.isEmpty())
        {
            polygons.add(new MapPolygon(poly));
        }

        return classifyAndGroup(polygons);
    }

    /**
     * Given an arbitrary list of polygons, determine which are hulls and holes and which holes are
     * associated with which hulls.
     *
     * Assumes that hulls use CCW point winding and holes use CW point winding, which seems to be
     * consistent with {@link #createPolygonFromArea}.
     *
     * @param polygons The input list of {@link MapPolygon}s.
     * @return The resulting list of {@link PolygonWithHoles}.
     */
    public static @NotNull List<PolygonWithHoles> classifyAndGroup(@NotNull final List<MapPolygon> polygons)
    {
        final List<MapPolygon> hulls = new ArrayList<>();
        final List<MapPolygon> holes = new ArrayList<>();

        for (final MapPolygon polygon : polygons)
        {
            if (isHole(polygon))
            {
                holes.add(polygon);
            }
            else
            {
                hulls.add(polygon);
            }
        }

        final List<Tuple<MapPolygon, Area>> holeAreas = holes.stream()
                .map(hole -> new Tuple<>(hole, toArea(hole)))
                .collect(Collectors.toList());

        final List<PolygonWithHoles> result = new ArrayList<>();
        for (final MapPolygon hull : hulls)
        {
            final Area hullArea = toArea(hull);
            final List<MapPolygon> hullHoles = new ArrayList<>();

            for (final Iterator<Tuple<MapPolygon, Area>> iterator = holeAreas.iterator(); iterator.hasNext(); )
            {
                final Tuple<MapPolygon, Area> holeArea = iterator.next();
                final Area intersection = new Area(hullArea);
                intersection.intersect(holeArea.getB());
                if (!intersection.isEmpty())
                {
                    hullHoles.add(holeArea.getA());
                    iterator.remove();
                }
            }

            result.add(new PolygonWithHoles(hull, hullHoles));
        }

        return result;
    }

    /**
     * Given a polygon (consisting of a possibly-concave hull and holes), convert into
     * multiple ordinary convex polygons (which can actually be drawn).  This uses a
     * triangulation algorithm to do that.
     *
     * @param polygon The input {@link PolygonWithHoles}.
     * @return The output triangle polygons that cover the input.
     */
    public static @NotNull List<MapPolygon> triangulate(@NotNull final PolygonWithHoles polygon)
    {
        final List<BlockPos> blockPoints = Stream.concat(polygon.hull.getPoints().stream(),
                polygon.holes.stream()
                        .flatMap(hole -> hole.getPoints().stream()))
                .collect(Collectors.toList());

        final double[] points = new double[blockPoints.size() * 2];
        for (int index = 0; index < blockPoints.size(); ++index)
        {
            points[index * 2] = blockPoints.get(index).getX();
            points[index * 2 + 1] = blockPoints.get(index).getZ();
        }

        final int[] holes = new int[polygon.holes.size()];
        int holeIndex = polygon.hull.getPoints().size();
        for (int index = 0; index < polygon.holes.size(); ++index)
        {
            holes[index] = holeIndex;
            holeIndex += polygon.holes.get(index).getPoints().size();
        }

        final List<Integer> triangles = Earcut.earcut(points, holes, 2);

        final List<MapPolygon> trianglePolys = new ArrayList<>();
        for (int index = 0; index < triangles.size(); index += 3)
        {
            final List<BlockPos> trianglePoints = new ArrayList<>();
            trianglePoints.add(blockPoints.get(triangles.get(index + 2)));
            trianglePoints.add(blockPoints.get(triangles.get(index + 1)));
            trianglePoints.add(blockPoints.get(triangles.get(index)));
            trianglePolys.add(new MapPolygon(trianglePoints));
        }
        return trianglePolys;
    }

    /**
     * The input tends to have points for each chunk, even along a straight line.
     * Remove the unneeded intermediate points.
     *
     * @param points The input points
     * @return The filtered points
     */
    private static @NotNull List<BlockPos> simplify(@NotNull final List<BlockPos> points)
    {
        final List<BlockPos> result = new ArrayList<>();
        BlockPos prev2 = points.get(0);
        BlockPos prev1 = points.get(1);
        result.add(prev2);
        for (int index = 2; index < points.size(); ++index)
        {
            final BlockPos next = points.get(index);
            if (prev2.getX() == prev1.getX() && prev1.getX() == next.getX())
            {
                // merge horizontal line by skipping the middle point
                prev1 = next;
            }
            else if (prev2.getZ() == prev1.getZ() && prev1.getZ() == next.getZ())
            {
                // merge vertical line by skipping the middle point
                prev1 = next;
            }
            else
            {
                // corner; keep the point
                result.add(prev1);
                prev2 = prev1;
                prev1 = next;
            }
        }
        result.add(prev1);
        return result;
    }

    /**
     * Determine if the given polygon is a "hole".  Holes have CW point winding.
     * Assumes that +X is "right" and +Z is "down".
     *
     * @param polygon The polygon.
     * @return True if it's a hole.
     */
    private static boolean isHole(@NotNull final MapPolygon polygon)
    {
        // from https://stackoverflow.com/a/18472899/43534
        long sum = 0;
        final List<BlockPos> points = polygon.getPoints();
        BlockPos a = points.get(points.size() - 1);
        for (final BlockPos b : points)
        {
            sum += (long) (b.getX() - a.getX()) * (b.getZ() + a.getZ());
            a = b;
        }
        return sum < 0;
    }
}
