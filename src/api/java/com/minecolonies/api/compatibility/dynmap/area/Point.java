package com.minecolonies.api.compatibility.dynmap.area;

/**
 * Point class for border generation in Dynmap
 *
 * @param x the x coordinate
 * @param z the z coordinate
 */
public record Point(double x, double z)
{
    /**
     * Obtain the square of the distance between this point and another point.
     *
     * @param other the other point.
     * @return the square of the distance between the two points.
     */
    public double distanceSq(Point other)
    {
        double px = other.x() - this.x();
        double py = other.z() - this.z();
        return px * px + py * py;
    }
}
