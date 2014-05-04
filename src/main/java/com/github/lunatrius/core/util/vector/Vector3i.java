package com.github.lunatrius.core.util.vector;

public class Vector3i extends Vector2i {
	public int z;

	public Vector3i() {
		this(0, 0, 0);
	}

	public Vector3i(Vector3i vec) {
		this(vec.x, vec.y, vec.z);
	}

	public Vector3i(int num) {
		this(num, num, num);
	}

	public Vector3i(int x, int y, int z) {
		super(x, y);
		this.z = z;
	}

	public final int getZ() {
		return this.z;
	}

	public final void setZ(int z) {
		this.z = z;
	}

	public Vector3i set(Vector3i vec) {
		return set(vec.x, vec.y, vec.z);
	}

	public Vector3i set(int x, int y, int z) {
		super.set(x, y);
		this.z = z;
		return this;
	}

	@Override
	public int lengthSquared() {
		return super.lengthSquared() + this.z * this.z;
	}

	@Override
	public Vector3i negate() {
		super.negate();
		this.z = -this.z;
		return this;
	}

	public double dot(Vector3i vec) {
		return super.dot(vec) + this.z * vec.z;
	}

	@Override
	public Vector3i scale(double scale) {
		super.scale(scale);
		this.z *= scale;
		return this;
	}

	public Vector3i add(Vector3i vec) {
		super.add(vec);
		this.z += vec.z;
		return this;
	}

	public Vector3i add(int x, int y, int z) {
		super.add(x, y);
		this.z += z;
		return this;
	}

	public Vector3i sub(Vector3i vec) {
		super.sub(vec);
		this.z -= vec.z;
		return this;
	}

	public Vector3i sub(int x, int y, int z) {
		super.sub(x, y);
		this.z -= z;
		return this;
	}

	public Vector3f toVector3f() {
		return new Vector3f(this.x, this.y, this.z);
	}

	@Override
	public Vector3i clone() {
		return new Vector3i(this);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Vector3i && equals((Vector3i) obj);
	}

	public boolean equals(Vector3i vec) {
		return super.equals(vec) && this.z == vec.z;
	}

	@Override
	public String toString() {
		return String.format("[%s, %s, %s]", this.x, this.y, this.z);
	}
}
