package com.minecolonies.api.compatibility.dynmap.area;

import javax.annotation.Nonnull;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class ColonyArea
{
    private final List<Point2D.Double> points;

    private Point2D.Double secondLast;
    private Point2D.Double last;

    public ColonyArea()
    {
        this.points = new ArrayList<>();
    }

    public void addPoint(double x, double z)
    {
        var newPoint = new Point2D.Double(x, z);

        // If the current X or Z values match at least 2 items back, we remove the last (middle of the comparison) item
        // from the deque for simplification.
        if ((last != null && secondLast != null) && ((last.x == x && secondLast.x == x) || (last.y == z && secondLast.y == z)))
        {
            this.points.remove(this.points.size() - 1);
        }

        this.points.add(newPoint);

        if (last != null)
        {
            secondLast = new Point2D.Double(last.x, last.y);
        }
        last = newPoint;
    }

    public void addHole(@Nonnull final ColonyArea hole)
    {
        // Find the closest distance between any point of the current area and the hole.
        var minimumDistance = -1d;
        var selectedAreaPointIndex = -1;
        var selectedHolePointIndex = -1;

        var areaPointIndex = 0;
        var holePointIndex = 0;
        for (var point : points)
        {
            holePointIndex = 0;
            for (var holePoint : hole.points)
            {
                var distance = point.distance(holePoint);
                if (distance < minimumDistance || minimumDistance == -1)
                {
                    minimumDistance = distance;
                    selectedAreaPointIndex = areaPointIndex;
                    selectedHolePointIndex = holePointIndex;
                }
                holePointIndex++;
            }
            areaPointIndex++;
        }

        if (selectedAreaPointIndex >= 0 && selectedHolePointIndex >= 0)
        {
            var newPoints = new ArrayList<Point2D.Double>();

            // We need to intersect the area with the hole at the selected points.
            var currentPosition = selectedHolePointIndex;
            var round = false;
            while (!round)
            {
                var point = hole.points.get(currentPosition);
                newPoints.add(new Point2D.Double(point.x, point.y));

                currentPosition++;
                if (currentPosition >= hole.points.size())
                {
                    currentPosition = 0;
                }

                if (currentPosition == selectedHolePointIndex)
                {
                    var initialPoint = hole.points.get(currentPosition);
                    newPoints.add(new Point2D.Double(initialPoint.x, initialPoint.y));
                    round = true;
                }
            }

            // Add the selected area point at the end of the list again in order to return the line back to the original area
            var areaPoint = this.points.get(selectedAreaPointIndex);
            newPoints.add(new Point2D.Double(areaPoint.x, areaPoint.y));

            this.points.addAll(selectedAreaPointIndex + 1, newPoints);
        }
    }

    public void close()
    {
        this.points.add(new Point2D.Double(this.points.get(0).x, this.points.get(0).y));
    }

    public void reset()
    {
        this.points.clear();
        this.last = null;
        this.secondLast = null;
    }

    public double[] toXArray()
    {
        return points.stream().mapToDouble(i -> i.x).toArray();
    }

    public double[] toZArray()
    {
        return points.stream().mapToDouble(i -> i.y).toArray();
    }
}