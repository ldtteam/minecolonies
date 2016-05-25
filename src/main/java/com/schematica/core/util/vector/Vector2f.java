package com.schematica.core.util.vector;

public class Vector2f {
    public static final float FLOAT_EPSILON = 10e-6f;
    public float x;
    public float y;

    public Vector2f() {
        this(0, 0);
    }

    public Vector2f(final Vector2f vec) {
        this(vec.x, vec.y);
    }

    public Vector2f(final float num) {
        this(num, num);
    }

    public Vector2f(final float x, final float y) {
        this.x = x;
        this.y = y;
    }

    public final float getX() {
        return this.x;
    }

    public final float getY() {
        return this.y;
    }

    public final void setX(final float x) {
        this.x = x;
    }

    public final void setY(final float y) {
        this.y = y;
    }

    public Vector2f set(final Vector2f vec) {
        return set(vec.x, vec.y);
    }

    public Vector2f set(final float x, final float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public final double length() {
        return Math.sqrt(lengthSquared());
    }

    public float lengthSquared() {
        return this.x * this.x + this.y * this.y;
    }

    public final double lengthTo(final Vector2f vec) {
        return Math.sqrt(lengthSquaredTo(vec));
    }

    public float lengthSquaredTo(final Vector2f vec) {
        return pow2(this.x - vec.x) + pow2(this.y - vec.y);
    }

    protected final float pow2(final float num) {
        return num * num;
    }

    public final Vector2f normalize() {
        final double len = length();
        if (len != 0.0) {
            return scale(1.0 / len);
        }

        return this;
    }

    public Vector2f negate() {
        this.x = -this.x;
        this.y = -this.y;
        return this;
    }

    public float dot(final Vector2f vec) {
        return this.x * vec.x + this.y * vec.y;
    }

    public Vector2f scale(final double scale) {
        this.x *= scale;
        this.y *= scale;
        return this;
    }

    public Vector2f add(final Vector2f vec) {
        this.x += vec.x;
        this.y += vec.y;
        return this;
    }

    public Vector2f add(final float x, final float y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public Vector2f sub(final Vector2f vec) {
        this.x -= vec.x;
        this.y -= vec.y;
        return this;
    }

    public Vector2f sub(final float x, final float y) {
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

    public Vector2d toVector2d() {
        return new Vector2d(this.x, this.y);
    }

    public Vector2d toVector2d(final Vector2d vec) {
        return vec.set(this.x, this.y);
    }

    @Override
    public Vector2f clone() {
        return new Vector2f(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof Vector2f && equals((Vector2f) obj);
    }

    public boolean equals(final Vector2f vec) {
        return equals(vec, FLOAT_EPSILON);
    }

    public boolean equals(final Vector2f vec, final float epsilon) {
        return Math.abs(this.x - vec.x) < epsilon && Math.abs(this.y - vec.y) < epsilon;
    }

    @Override
    public String toString() {
        return String.format("[%s, %s]", this.x, this.y);
    }
}
