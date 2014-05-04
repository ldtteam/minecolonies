package com.github.lunatrius.core.util.vector;

public class Vector2f {
	public static final float FLOAT_EPSILON = 10e-6f;
	public float x;
	public float y;

	public Vector2f() {
		this(0, 0);
	}

	public Vector2f(Vector2f vec) {
		this(vec.x, vec.y);
	}

	public Vector2f(float num) {
		this(num, num);
	}

	public Vector2f(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public final float getX() {
		return this.x;
	}

	public final float getY() {
		return this.y;
	}

	public final void setX(float x) {
		this.x = x;
	}

	public final void setY(float y) {
		this.y = y;
	}

	public Vector2f set(Vector2f vec) {
		return set(vec.x, vec.y);
	}

	public Vector2f set(float x, float y) {
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

	public final Vector2f normalize() {
		double len = length();
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

	public double dot(Vector2i vec) {
		return this.x * vec.x + this.y * vec.y;
	}

	public Vector2f scale(double scale) {
		this.x *= scale;
		this.y *= scale;
		return this;
	}

	public Vector2f add(Vector2f vec) {
		this.x += vec.x;
		this.y += vec.y;
		return this;
	}

	public Vector2f add(float x, float y) {
		this.x += x;
		this.y += y;
		return this;
	}

	public Vector2f sub(Vector2f vec) {
		this.x -= vec.x;
		this.y -= vec.y;
		return this;
	}

	public Vector2f sub(float x, float y) {
		this.x -= x;
		this.y -= y;
		return this;
	}

	public Vector2i toVector2i() {
		return new Vector2i((int) Math.floor(this.x), (int) Math.floor(this.y));
	}

	@Override
	public Vector2f clone() {
		return new Vector2f(this);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Vector2f && equals((Vector2f) obj);
	}

	public boolean equals(Vector2f vec) {
		return equals(vec, FLOAT_EPSILON);
	}

	public boolean equals(Vector2f vec, float epsilon) {
		return Math.abs(this.x - vec.x) < epsilon && Math.abs(this.y - vec.y) < epsilon;
	}

	@Override
	public String toString() {
		return String.format("[%s, %s]", this.x, this.y);
	}
}
