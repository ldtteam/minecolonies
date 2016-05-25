package com.schematica.core.util.vector;

public class Vector4d extends Vector3d {
    public double w;

    public Vector4d() {
        this(0, 0, 0, 0);
    }

    public Vector4d(final Vector4d vec) {
        this(vec.x, vec.y, vec.z, vec.w);
    }

    public Vector4d(final double num) {
        this(num, num, num, num);
    }

    public Vector4d(final double x, final double y, final double z, final double w) {
        super(x, y, z);
        this.w = w;
    }

    public final double getW() {
        return this.w;
    }

    public final void setW(final double w) {
        this.w = w;
    }

    public Vector4d set(final Vector4d vec) {
        return set(vec.x, vec.y, vec.z, vec.w);
    }

    public Vector4d set(final double x, final double y, final double z, final double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    @Override
    public double lengthSquared() {
        return this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w;
    }

    public final double lengthTo(final Vector4d vec) {
        return Math.sqrt(lengthSquaredTo(vec));
    }

    public double lengthSquaredTo(final Vector4d vec) {
        return pow2(this.x - vec.x) + pow2(this.y - vec.y) + pow2(this.z - vec.z) + pow2(this.w - vec.w);
    }

    @Override
    public Vector4d negate() {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
        this.w = -this.w;
        return this;
    }

    public double dot(final Vector4d vec) {
        return this.x * vec.x + this.y * vec.y + this.z * vec.z + this.w * vec.w;
    }

    @Override
    public Vector4d scale(final double scale) {
        this.x *= scale;
        this.y *= scale;
        this.z *= scale;
        this.w *= scale;
        return this;
    }

    public Vector4d add(final Vector4d vec) {
        this.x += vec.x;
        this.y += vec.y;
        this.z += vec.z;
        this.w += vec.w;
        return this;
    }

    public Vector4d add(final double x, final double y, final double z, final double w) {
        this.x += x;
        this.y += y;
        this.z += z;
        this.w += w;
        return this;
    }

    public Vector4d sub(final Vector4d vec) {
        this.x -= vec.x;
        this.y -= vec.y;
        this.z -= vec.z;
        this.w -= vec.w;
        return this;
    }

    public Vector4d sub(final double x, final double y, final double z, final double w) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        this.w -= w;
        return this;
    }

    public Vector4i toVector4i() {
        return new Vector4i((int) Math.floor(this.x), (int) Math.floor(this.y), (int) Math.floor(this.z), (int) Math.floor(this.w));
    }

    public Vector4i toVector4i(final Vector4i vec) {
        return vec.set((int) Math.floor(this.x), (int) Math.floor(this.y), (int) Math.floor(this.z), (int) Math.floor(this.w));
    }

    public Vector4f toVector4f() {
        return new Vector4f((float) Math.floor(this.x), (float) Math.floor(this.y), (float) Math.floor(this.z), (float) Math.floor(this.w));
    }

    public Vector4f toVector4f(final Vector4f vec) {
        return vec.set((float) Math.floor(this.x), (float) Math.floor(this.y), (float) Math.floor(this.z), (float) Math.floor(this.w));
    }

    @Override
    public Vector4d clone() {
        return new Vector4d(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof Vector4d && equals((Vector4d) obj);
    }

    public boolean equals(final Vector4d vec) {
        return equals(vec, DOUBLE_EPSILON);
    }

    public boolean equals(final Vector4d vec, final double epsilon) {
        return Math.abs(this.x - vec.x) < epsilon && Math.abs(this.y - vec.y) < epsilon && Math.abs(this.z - vec.z) < epsilon && Math.abs(this.w - vec.w) < epsilon;
    }

    @Override
    public String toString() {
        return String.format("[%s, %s, %s, %s]", this.x, this.y, this.z, this.w);
    }
}
