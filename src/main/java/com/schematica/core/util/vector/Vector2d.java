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

    public Vector2d(final double num) {
        this(num, num);
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

    public final double lengthTo(final Vector2d vec) {
        return Math.sqrt(lengthSquaredTo(vec));
    }

    public double lengthSquaredTo(final Vector2d vec) {
        return pow2(this.x - vec.x) + pow2(this.y - vec.y);
    }

    protected final double pow2(final double num) {
        return num * num;
    }

    public final Vector2d normalize() {
        final double len = length();
        if (len != 0.0) {
            return scale(1.0 / len);
        }

        return this;
    }

    public Vector2d negate() {
        this.x = -this.x;
        this.y = -this.y;
        return this;
    }

    public double dot(final Vector2d vec) {
        return this.x * vec.x + this.y * vec.y;
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

    public Vector2d sub(final Vector2d vec) {
        this.x -= vec.x;
        this.y -= vec.y;
        return this;
    }

    public Vector2d sub(final double x, final double y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    public Vector2i toVector2i() {
        return new Vector2i((int) Math.floor(this.x), (int) Math.floor(this.y));
    }

    public Vector2i toVector2i(final Vector2i vec) {
        return vec.set((int) Math.floor(this.x), (int) Math.floor(this.y));
    }

    public Vector2f toVector2f() {
        return new Vector2f((float) Math.floor(this.x), (float) Math.floor(this.y));
    }

    public Vector2f toVector2f(final Vector2f vec) {
        return vec.set((float) Math.floor(this.x), (float) Math.floor(this.y));
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
