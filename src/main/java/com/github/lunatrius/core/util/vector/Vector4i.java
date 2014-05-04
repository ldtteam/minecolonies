package com.github.lunatrius.core.util.vector;

public class Vector4i extends Vector3i {
	public int w;

	public Vector4i() {
		this(0, 0, 0, 0);
	}

	public Vector4i(Vector4i vec) {
		this(vec.x, vec.y, vec.z, vec.w);
	}

	public Vector4i(int num) {
		this(num, num, num, num);
	}

	public Vector4i(int x, int y, int z, int w) {
		super(x, y, z);
		this.w = w;
	}

	public final int getW() {
		return this.w;
	}

	public final void setW(int w) {
		this.w = w;
	}

	public Vector4i set(Vector4i vec) {
		return set(vec.x, vec.y, vec.z, vec.w);
	}

	public Vector4i set(int x, int y, int z, int w) {
		super.set(x, y, z);
		this.w = w;
		return this;
	}

	@Override
	public int lengthSquared() {
		return super.lengthSquared() + this.w * this.w;
	}

	@Override
	public Vector4i negate() {
		super.negate();
		this.w = -this.w;
		return this;
	}

	public double dot(Vector4i vec) {
		return super.dot(vec) + this.w * vec.w;
	}

	@Override
	public Vector4i scale(double scale) {
		super.scale(scale);
		this.w *= scale;
		return this;
	}

	public Vector4i add(Vector4i vec) {
		super.add(vec);
		this.w += vec.w;
		return this;
	}

	public Vector4i add(int x, int y, int z, int w) {
		super.add(x, y, z);
		this.w += w;
		return this;
	}

	public Vector4i sub(Vector4i vec) {
		super.sub(vec);
		this.w -= vec.w;
		return this;
	}

	public Vector4i sub(int x, int y, int z, int w) {
		super.sub(x, y, z);
		this.w -= w;
		return this;
	}

	public Vector4f toVector4f() {
		return new Vector4f(this.x, this.y, this.z, this.w);
	}

	@Override
	public Vector4i clone() {
		return new Vector4i(this);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Vector4i && equals((Vector4i) obj);
	}

	public boolean equals(Vector4i vec) {
		return super.equals(vec) && this.w == vec.w;
	}

	@Override
	public String toString() {
		return String.format("[%s, %s, %s, %s]", this.x, this.y, this.z, this.w);
	}
}
