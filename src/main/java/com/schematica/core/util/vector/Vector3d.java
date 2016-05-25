package com.schematica.core.util.vector;

public class Vector3d extends Vector2d {
    public double z;

    public Vector3d() {
        this(0, 0, 0);
    }

    public Vector3d(final Vector3d vec) {
        this(vec.x, vec.y, vec.z);
    }

    public Vector3d(final double x, final double y, final double z) {
        super(x, y);
        this.z = z;
    }

    public final double getZ() {
        return this.z;
    }

    public final void setZ(final double z) {
        this.z = z;
    }

    public Vector3d set(final Vector3d vec) {
        return set(vec.x, vec.y, vec.z);
    }

    public Vector3d set(final double x, final double y, final double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    @Override
    public double lengthSquared() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    @Override
    public Vector3d scale(final double scale) {
        this.x *= scale;
        this.y *= scale;
        this.z *= scale;
        return this;
    }

    public Vector3d add(final Vector3d vec) {
        this.x += vec.x;
        this.y += vec.y;
        this.z += vec.z;
        return this;
    }

    public Vector3d add(final double x, final double y, final double z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Vector3d sub(final double x, final double y, final double z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    @Override
    public Vector3d clone() {
        return new Vector3d(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof Vector3d && equals((Vector3d) obj);
    }

    public boolean equals(final Vector3d vec) {
        return equals(vec, DOUBLE_EPSILON);
    }

    public boolean equals(final Vector3d vec, final double epsilon) {
        return Math.abs(this.x - vec.x) < epsilon && Math.abs(this.y - vec.y) < epsilon && Math.abs(this.z - vec.z) < epsilon;
    }

    @Override
    public String toString() {
        return String.format("[%s, %s, %s]", this.x, this.y, this.z);
    }
}
