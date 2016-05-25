package com.schematica.core.util.vector;

public class Vector2d {
    public static final double DOUBLE_EPSILON = 10e-6f;
    public double x;
    public double y;

    public Vector2d() {
        this(0, 0);
    }

    public Vector2d(final Vector2d vec) {
        this(vec.x, vec.y);
    }

    public Vector2d(final double x, final double y) {
        this.x = x;
        this.y = y;
    }

    public final double getX() {
        return this.x;
    }

    public final double getY() {
        return this.y;
    }

    public final void setX(final double x) {
        this.x = x;
    }

    public final void setY(final double y) {
        this.y = y;
    }

    public Vector2d set(final Vector2d vec) {
        return set(vec.x, vec.y);
    }

    public Vector2d set(final double x, final double y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public final double length() {
        return Math.sqrt(lengthSquared());
    }

    public double lengthSquared() {
        return this.x * this.x + this.y * this.y;
    }

    public Vector2d scale(final double scale) {
        this.x *= scale;
        this.y *= scale;
        return this;
    }

    public Vector2d add(final Vector2d vec) {
        this.x += vec.x;
        this.y += vec.y;
        return this;
    }

    public Vector2d add(final double x, final double y) {
        this.x += x;
        this.y += y;
        return this;
    }

    @Override
    public Vector2d clone() {
        return new Vector2d(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof Vector2d && equals((Vector2d) obj);
    }

    public boolean equals(final Vector2d vec) {
        return equals(vec, DOUBLE_EPSILON);
    }

    public boolean equals(final Vector2d vec, final double epsilon) {
        return Math.abs(this.x - vec.x) < epsilon && Math.abs(this.y - vec.y) < epsilon;
    }

    @Override
    public String toString() {
        return String.format("[%s, %s]", this.x, this.y);
    }
}
