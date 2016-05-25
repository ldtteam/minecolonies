package com.schematica.core.util.vector;

public class Vector3f extends Vector2f {
    public float z;

    public Vector3f() {
        this(0, 0, 0);
    }

    public Vector3f(final Vector3f vec) {
        this(vec.x, vec.y, vec.z);
    }

    public Vector3f(final float num) {
        this(num, num, num);
    }

    public Vector3f(final float x, final float y, final float z) {
        super(x, y);
        this.z = z;
    }

    public final float getZ() {
        return this.z;
    }

    public final void setZ(final float z) {
        this.z = z;
    }

    public Vector3f set(final Vector3f vec) {
        return set(vec.x, vec.y, vec.z);
    }

    public Vector3f set(final float x, final float y, final float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    @Override
    public float lengthSquared() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public final double lengthTo(final Vector3f vec) {
        return Math.sqrt(lengthSquaredTo(vec));
    }

    public float lengthSquaredTo(final Vector3f vec) {
        return pow2(this.x - vec.x) + pow2(this.y - vec.y) + pow2(this.z - vec.z);
    }

    @Override
    public Vector3f negate() {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
        return this;
    }

    public float dot(final Vector3f vec) {
        return this.x * vec.x + this.y * vec.y + this.z * vec.z;
    }

    @Override
    public Vector3f scale(final double scale) {
        this.x *= scale;
        this.y *= scale;
        this.z *= scale;
        return this;
    }

    public Vector3f add(final Vector3f vec) {
        this.x += vec.x;
        this.y += vec.y;
        this.z += vec.z;
        return this;
    }

    public Vector3f add(final float x, final float y, final float z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Vector3f sub(final Vector3f vec) {
        this.x -= vec.x;
        this.y -= vec.y;
        this.z -= vec.z;
        return this;
    }

    public Vector3f sub(final float x, final float y, final float z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    public Vector3i toVector3i() {
        return new Vector3i((int) Math.floor(this.x), (int) Math.floor(this.y), (int) Math.floor(this.z));
    }

    public Vector3i toVector3i(final Vector3i vec) {
        return vec.set((int) Math.floor(this.x), (int) Math.floor(this.y), (int) Math.floor(this.z));
    }

    public Vector3d toVector3d() {
        return new Vector3d(this.x, this.y, this.z);
    }

    public Vector3d toVector3d(final Vector3d vec) {
        return vec.set(this.x, this.y, this.z);
    }

    @Override
    public Vector3f clone() {
        return new Vector3f(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof Vector3f && equals((Vector3f) obj);
    }

    public boolean equals(final Vector3f vec) {
        return equals(vec, FLOAT_EPSILON);
    }

    public boolean equals(final Vector3f vec, final float epsilon) {
        return Math.abs(this.x - vec.x) < epsilon && Math.abs(this.y - vec.y) < epsilon && Math.abs(this.z - vec.z) < epsilon;
    }

    @Override
    public String toString() {
        return String.format("[%s, %s, %s]", this.x, this.y, this.z);
    }
}
