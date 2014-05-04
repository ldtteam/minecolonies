package com.github.lunatrius.core.util.vector;

public class Vector2i {
	public int x;
	public int y;

	public Vector2i() {
		this(0, 0);
	}

	public Vector2i(Vector2i vec) {
		this(vec.x, vec.y);
	}

	public Vector2i(int num) {
		this(num, num);
	}

	public Vector2i(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public final int getX() {
		return this.x;
	}

	public final int getY() {
		return this.y;
	}

	public final void setX(int x) {
		this.x = x;
	}

	public final void setY(int y) {
		this.y = y;
	}

	public Vector2i set(Vector2i vec) {
		return set(vec.x, vec.y);
	}

	public Vector2i set(int x, int y) {
		this.x = x;
		this.y = y;
		return this;
	}

	public final double length() {
		return Math.sqrt(lengthSquared());
	}

	public int lengthSquared() {
		return this.x * this.x + this.y * this.y;
	}

	public final Vector2i normalize() {
		double len = length();
		if (len != 0.0) {
			return scale(1.0 / len);
		}

		return this;
	}

	public Vector2i negate() {
		this.x = -this.x;
		this.y = -this.y;
		return this;
	}

	public double dot(Vector2i vec) {
		return this.x * vec.x + this.y * vec.y;
	}

	public Vector2i scale(double scale) {
		this.x *= scale;
		this.y *= scale;
		return this;
	}

	public Vector2i add(Vector2i vec) {
		this.x += vec.x;
		this.y += vec.y;
		return this;
	}

	public Vector2i add(int x, int y) {
		this.x += x;
		this.y += y;
		return this;
	}

	public Vector2i sub(Vector2i vec) {
		this.x -= vec.x;
		this.y -= vec.y;
		return this;
	}

	public Vector2i sub(int x, int y) {
		this.x -= x;
		this.y -= y;
		return this;
	}

	public Vector2f toVector2f() {
		return new Vector2f(this.x, this.y);
	}

	@Override
	public Vector2i clone() {
		return new Vector2i(this);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Vector2i && equals((Vector2i) obj);
	}

	public boolean equals(Vector2i vec) {
		return this.x == vec.x && this.y == vec.y;
	}

	@Override
	public String toString() {
		return String.format("[%s, %s]", this.x, this.y);
	}
}
