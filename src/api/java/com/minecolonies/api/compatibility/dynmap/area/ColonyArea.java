package com.minecolonies.api.compatibility.dynmap.area;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Class containing the X and Z coordinates for a colony it's generated area for Dynmap.
 */
public class ColonyArea
{
    private final List<Point> points;

    private Point secondLast;
    private Point last;

    /**
     * Default constructor.
     */
    public ColonyArea()
    {
        this.points = new ArrayList<>();
    }

    /**
     * Adds a new X Z coord onto this area.
     * This method automatically ensures that the added point is not in the same line as a previous point,
     * so that we not make an unnecessary amount of waypoints.
     *
     * @param x The X coord.
     * @param z The Z coord.
     */
    public void addPoint(double x, double z)
    {
        Point newPoint = new Point(x, z);

        // If the current X or Z values match at least 2 items back, we remove the last (middle of the comparison) item
        // from the deque for simplification.
        if ((last != null && secondLast != null) && ((last.x() == x && secondLast.x() == x) || (last.z() == z && secondLast.z() == z)))
        {
            this.points.remove(this.points.size() - 1);
        }

        this.points.add(newPoint);

        if (last != null)
        {
            secondLast = new Point(last.x(), last.z());
        }
        last = newPoint;
    }

    /**
     * Add a hole into the area, a hole is another {@link ColonyArea} containing the borders of a set of points which is completely encompassed by the current
     * area.
     * This works by selecting 2 points from the current area, as well as the hole, which have the short distance towards one another.
     * Afterwards it combines the hole it's X Z coords into the current area, creating a line to link up the 2 areas.
     *
     * @param hole The area instance to add as a hole to the current area.
     */
    public void addHole(@Nonnull final ColonyArea hole)
    {
        // Find the closest distance between any point of the current area and the hole.
        double minimumDistance = Double.MAX_VALUE;
        int selectedAreaPointIndex = -1;
        int selectedHolePointIndex = -1;

        int areaPointIndex = 0;
        int holePointIndex = 0;
        for (Point point : points)
        {
            holePointIndex = 0;
            for (Point holePoint : hole.points)
            {
                double distance = point.distanceSq(holePoint);
                if (distance < minimumDistance)
                {
                    minimumDistance = distance;
                    selectedAreaPointIndex = areaPointIndex;
                    selectedHolePointIndex = holePointIndex;
                }
                holePointIndex++;
            }
            areaPointIndex++;
        }

        generateHole(hole, selectedAreaPointIndex, selectedHolePointIndex);
    }

    private void generateHole(@Nonnull final ColonyArea hole, int selectedAreaPointIndex, int selectedHolePointIndex)
    {
        if (selectedAreaPointIndex >= 0 && selectedHolePointIndex >= 0)
        {
            ArrayList<Point> newPoints = new ArrayList<>();

            // We need to intersect the area with the hole at the selected points.
            int currentPosition = selectedHolePointIndex;
            boolean round = false;
            while (!round)
            {
                Point point = hole.points.get(currentPosition);
                newPoints.add(new Point(point.x(), point.z()));

                currentPosition++;
                if (currentPosition >= hole.points.size())
                {
                    currentPosition = 0;
                }

                if (currentPosition == selectedHolePointIndex)
                {
                    Point initialPoint = hole.points.get(currentPosition);
                    newPoints.add(new Point(initialPoint.x(), initialPoint.z()));
                    round = true;
                }
            }

            // Add the selected area point at the end of the list again in order to return the line back to the original area
            Point areaPoint = this.points.get(selectedAreaPointIndex);
            newPoints.add(new Point(areaPoint.x(), areaPoint.z()));

            this.points.addAll(selectedAreaPointIndex + 1, newPoints);
        }
    }

    /**
     * Closes off the area by adding a last point which links up the last point back to the first one.
     */
    public void close()
    {
        this.points.add(new Point(this.points.get(0).x(), this.points.get(0).z()));
    }

    /**
     * Generates a double array of all the X coords (specifically for Dynmap to generate an area marker)
     *
     * @return A double array.
     */
    public double[] toXArray()
    {
        return points.stream().mapToDouble(Point::x).toArray();
    }

    /**
     * Generates a double array of all the Z coords (specifically for Dynmap to generate an area marker)
     *
     * @return A double array.
     */
    public double[] toZArray()
    {
        return points.stream().mapToDouble(Point::z).toArray();
    }
}