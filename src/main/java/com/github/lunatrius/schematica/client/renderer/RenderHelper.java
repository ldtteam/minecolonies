package com.github.lunatrius.schematica.client.renderer;

import com.github.lunatrius.schematica.lib.Reference;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;

public class RenderHelper {
	public static final int QUAD_DOWN = 0x01;
	public static final int QUAD_UP = 0x02;
	public static final int QUAD_NORTH = 0x04;
	public static final int QUAD_SOUTH = 0x08;
	public static final int QUAD_WEST = 0x10;
	public static final int QUAD_EAST = 0x20;
	public static final int QUAD_ALL = QUAD_DOWN | QUAD_UP | QUAD_NORTH | QUAD_SOUTH | QUAD_WEST | QUAD_EAST;

	public static final int LINE_DOWN_WEST = 0x11;
	public static final int LINE_UP_WEST = 0x12;
	public static final int LINE_DOWN_EAST = 0x21;
	public static final int LINE_UP_EAST = 0x22;
	public static final int LINE_DOWN_NORTH = 0x05;
	public static final int LINE_UP_NORTH = 0x06;
	public static final int LINE_DOWN_SOUTH = 0x09;
	public static final int LINE_UP_SOUTH = 0x0A;
	public static final int LINE_NORTH_WEST = 0x14;
	public static final int LINE_NORTH_EAST = 0x24;
	public static final int LINE_SOUTH_WEST = 0x18;
	public static final int LINE_SOUTH_EAST = 0x28;
	public static final int LINE_ALL = LINE_DOWN_WEST | LINE_UP_WEST | LINE_DOWN_EAST | LINE_UP_EAST | LINE_DOWN_NORTH | LINE_UP_NORTH | LINE_DOWN_SOUTH | LINE_UP_SOUTH | LINE_NORTH_WEST | LINE_NORTH_EAST | LINE_SOUTH_WEST | LINE_SOUTH_EAST;

	public static final Vector3f VEC_ZERO = new Vector3f(0, 0, 0);

	private static int quadSize = 0;
	private static float[] quadVertexBuffer = null;
	private static float[] quadColorBuffer = null;
	private static int quadVertexIndex = 0;
	private static int quadColorIndex = 0;
	private static int quadCount = 0;

	private static int lineSize = 0;
	private static float[] lineVertexBuffer = null;
	private static float[] lineColorBuffer = null;
	private static int lineVertexIndex = 0;
	private static int lineColorIndex = 0;
	private static int lineCount = 0;

	private static final Vector3f vecZero = new Vector3f();
	private static final Vector3f vecSize = new Vector3f();

	public static void createBuffers() {
		quadSize = 240;
		quadVertexBuffer = new float[quadSize * 3];
		quadColorBuffer = new float[quadSize * 4];

		lineSize = 240;
		lineVertexBuffer = new float[lineSize * 3];
		lineColorBuffer = new float[lineSize * 4];

		initBuffers();
	}

	public static void initBuffers() {
		quadVertexIndex = 0;
		quadColorIndex = 0;
		quadCount = 0;

		lineVertexIndex = 0;
		lineColorIndex = 0;
		lineCount = 0;
	}

	public static void destroyBuffers() {
		quadSize = 0;
		quadVertexBuffer = null;
		quadColorBuffer = null;

		lineSize = 0;
		lineVertexBuffer = null;
		lineColorBuffer = null;
	}

	public static FloatBuffer getQuadVertexBuffer() {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(quadVertexBuffer.length).put(quadVertexBuffer);
		buffer.flip();
		return buffer;
	}

	public static FloatBuffer getQuadColorBuffer() {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(quadColorBuffer.length).put(quadColorBuffer);
		buffer.flip();
		return buffer;
	}

	public static int getQuadCount() {
		return quadCount;
	}

	public static FloatBuffer getLineVertexBuffer() {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(lineVertexBuffer.length).put(lineVertexBuffer);
		buffer.flip();
		return buffer;
	}

	public static FloatBuffer getLineColorBuffer() {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(lineColorBuffer.length).put(lineColorBuffer);
		buffer.flip();
		return buffer;
	}

	public static int getLineCount() {
		return lineCount;
	}

	private static float[] createAndCopyBuffer(int newSize, float[] oldBuffer) {
		float[] tempBuffer = new float[newSize];
		System.arraycopy(oldBuffer, 0, tempBuffer, 0, oldBuffer.length);
		return tempBuffer;
	}

	public static void drawCuboidSurface(Vector3f zero, Vector3f size, int sides, float red, float green, float blue, float alpha) {
		vecZero.set(zero.x - Reference.config.blockDelta, zero.y - Reference.config.blockDelta, zero.z - Reference.config.blockDelta);
		vecSize.set(size.x + Reference.config.blockDelta, size.y + Reference.config.blockDelta, size.z + Reference.config.blockDelta);

		if (quadCount + 24 >= quadSize) {
			quadSize *= 2;

			quadVertexBuffer = createAndCopyBuffer(quadSize * 3, quadVertexBuffer);
			quadColorBuffer = createAndCopyBuffer(quadSize * 4, quadColorBuffer);
		}

		int total = 0;

		if ((sides & QUAD_DOWN) != 0) {
			quadVertexBuffer[quadVertexIndex++] = vecSize.x;
			quadVertexBuffer[quadVertexIndex++] = vecZero.y;
			quadVertexBuffer[quadVertexIndex++] = vecZero.z;
			quadCount++;

			quadVertexBuffer[quadVertexIndex++] = vecSize.x;
			quadVertexBuffer[quadVertexIndex++] = vecZero.y;
			quadVertexBuffer[quadVertexIndex++] = vecSize.z;
			quadCount++;

			quadVertexBuffer[quadVertexIndex++] = vecZero.x;
			quadVertexBuffer[quadVertexIndex++] = vecZero.y;
			quadVertexBuffer[quadVertexIndex++] = vecSize.z;
			quadCount++;

			quadVertexBuffer[quadVertexIndex++] = vecZero.x;
			quadVertexBuffer[quadVertexIndex++] = vecZero.y;
			quadVertexBuffer[quadVertexIndex++] = vecZero.z;
			quadCount++;

			total += 4;
		}

		if ((sides & QUAD_UP) != 0) {
			quadVertexBuffer[quadVertexIndex++] = vecSize.x;
			quadVertexBuffer[quadVertexIndex++] = vecSize.y;
			quadVertexBuffer[quadVertexIndex++] = vecZero.z;
			quadCount++;

			quadVertexBuffer[quadVertexIndex++] = vecZero.x;
			quadVertexBuffer[quadVertexIndex++] = vecSize.y;
			quadVertexBuffer[quadVertexIndex++] = vecZero.z;
			quadCount++;

			quadVertexBuffer[quadVertexIndex++] = vecZero.x;
			quadVertexBuffer[quadVertexIndex++] = vecSize.y;
			quadVertexBuffer[quadVertexIndex++] = vecSize.z;
			quadCount++;

			quadVertexBuffer[quadVertexIndex++] = vecSize.x;
			quadVertexBuffer[quadVertexIndex++] = vecSize.y;
			quadVertexBuffer[quadVertexIndex++] = vecSize.z;
			quadCount++;

			total += 4;
		}

		if ((sides & QUAD_NORTH) != 0) {
			quadVertexBuffer[quadVertexIndex++] = vecSize.x;
			quadVertexBuffer[quadVertexIndex++] = vecZero.y;
			quadVertexBuffer[quadVertexIndex++] = vecZero.z;
			quadCount++;

			quadVertexBuffer[quadVertexIndex++] = vecZero.x;
			quadVertexBuffer[quadVertexIndex++] = vecZero.y;
			quadVertexBuffer[quadVertexIndex++] = vecZero.z;
			quadCount++;

			quadVertexBuffer[quadVertexIndex++] = vecZero.x;
			quadVertexBuffer[quadVertexIndex++] = vecSize.y;
			quadVertexBuffer[quadVertexIndex++] = vecZero.z;
			quadCount++;

			quadVertexBuffer[quadVertexIndex++] = vecSize.x;
			quadVertexBuffer[quadVertexIndex++] = vecSize.y;
			quadVertexBuffer[quadVertexIndex++] = vecZero.z;
			quadCount++;

			total += 4;
		}

		if ((sides & QUAD_SOUTH) != 0) {
			quadVertexBuffer[quadVertexIndex++] = vecZero.x;
			quadVertexBuffer[quadVertexIndex++] = vecZero.y;
			quadVertexBuffer[quadVertexIndex++] = vecSize.z;
			quadCount++;

			quadVertexBuffer[quadVertexIndex++] = vecSize.x;
			quadVertexBuffer[quadVertexIndex++] = vecZero.y;
			quadVertexBuffer[quadVertexIndex++] = vecSize.z;
			quadCount++;

			quadVertexBuffer[quadVertexIndex++] = vecSize.x;
			quadVertexBuffer[quadVertexIndex++] = vecSize.y;
			quadVertexBuffer[quadVertexIndex++] = vecSize.z;
			quadCount++;

			quadVertexBuffer[quadVertexIndex++] = vecZero.x;
			quadVertexBuffer[quadVertexIndex++] = vecSize.y;
			quadVertexBuffer[quadVertexIndex++] = vecSize.z;
			quadCount++;

			total += 4;
		}

		if ((sides & QUAD_WEST) != 0) {
			quadVertexBuffer[quadVertexIndex++] = vecZero.x;
			quadVertexBuffer[quadVertexIndex++] = vecZero.y;
			quadVertexBuffer[quadVertexIndex++] = vecZero.z;
			quadCount++;

			quadVertexBuffer[quadVertexIndex++] = vecZero.x;
			quadVertexBuffer[quadVertexIndex++] = vecZero.y;
			quadVertexBuffer[quadVertexIndex++] = vecSize.z;
			quadCount++;

			quadVertexBuffer[quadVertexIndex++] = vecZero.x;
			quadVertexBuffer[quadVertexIndex++] = vecSize.y;
			quadVertexBuffer[quadVertexIndex++] = vecSize.z;
			quadCount++;

			quadVertexBuffer[quadVertexIndex++] = vecZero.x;
			quadVertexBuffer[quadVertexIndex++] = vecSize.y;
			quadVertexBuffer[quadVertexIndex++] = vecZero.z;
			quadCount++;

			total += 4;
		}

		if ((sides & QUAD_EAST) != 0) {
			quadVertexBuffer[quadVertexIndex++] = vecSize.x;
			quadVertexBuffer[quadVertexIndex++] = vecZero.y;
			quadVertexBuffer[quadVertexIndex++] = vecSize.z;
			quadCount++;

			quadVertexBuffer[quadVertexIndex++] = vecSize.x;
			quadVertexBuffer[quadVertexIndex++] = vecZero.y;
			quadVertexBuffer[quadVertexIndex++] = vecZero.z;
			quadCount++;

			quadVertexBuffer[quadVertexIndex++] = vecSize.x;
			quadVertexBuffer[quadVertexIndex++] = vecSize.y;
			quadVertexBuffer[quadVertexIndex++] = vecZero.z;
			quadCount++;

			quadVertexBuffer[quadVertexIndex++] = vecSize.x;
			quadVertexBuffer[quadVertexIndex++] = vecSize.y;
			quadVertexBuffer[quadVertexIndex++] = vecSize.z;
			quadCount++;

			total += 4;
		}

		for (int i = 0; i < total; i++) {
			quadColorBuffer[quadColorIndex++] = red;
			quadColorBuffer[quadColorIndex++] = green;
			quadColorBuffer[quadColorIndex++] = blue;
			quadColorBuffer[quadColorIndex++] = alpha;
		}
	}

	public static void drawCuboidOutline(Vector3f zero, Vector3f size, int sides, float red, float green, float blue, float alpha) {
		vecZero.set(zero.x - Reference.config.blockDelta, zero.y - Reference.config.blockDelta, zero.z - Reference.config.blockDelta);
		vecSize.set(size.x + Reference.config.blockDelta, size.y + Reference.config.blockDelta, size.z + Reference.config.blockDelta);

		if (lineCount + 24 >= lineSize) {
			lineSize *= 2;

			lineVertexBuffer = createAndCopyBuffer(lineSize * 3, lineVertexBuffer);
			lineColorBuffer = createAndCopyBuffer(lineSize * 4, lineColorBuffer);
		}

		int total = 0;

		if ((sides & LINE_DOWN_WEST) != 0) {
			lineVertexBuffer[lineVertexIndex++] = vecZero.x;
			lineVertexBuffer[lineVertexIndex++] = vecZero.y;
			lineVertexBuffer[lineVertexIndex++] = vecZero.z;
			lineCount++;

			lineVertexBuffer[lineVertexIndex++] = vecZero.x;
			lineVertexBuffer[lineVertexIndex++] = vecZero.y;
			lineVertexBuffer[lineVertexIndex++] = vecSize.z;
			lineCount++;

			total += 2;
		}

		if ((sides & LINE_UP_WEST) != 0) {
			lineVertexBuffer[lineVertexIndex++] = vecZero.x;
			lineVertexBuffer[lineVertexIndex++] = vecSize.y;
			lineVertexBuffer[lineVertexIndex++] = vecZero.z;
			lineCount++;

			lineVertexBuffer[lineVertexIndex++] = vecZero.x;
			lineVertexBuffer[lineVertexIndex++] = vecSize.y;
			lineVertexBuffer[lineVertexIndex++] = vecSize.z;
			lineCount++;

			total += 2;
		}

		if ((sides & LINE_DOWN_EAST) != 0) {
			lineVertexBuffer[lineVertexIndex++] = vecSize.x;
			lineVertexBuffer[lineVertexIndex++] = vecZero.y;
			lineVertexBuffer[lineVertexIndex++] = vecZero.z;
			lineCount++;

			lineVertexBuffer[lineVertexIndex++] = vecSize.x;
			lineVertexBuffer[lineVertexIndex++] = vecZero.y;
			lineVertexBuffer[lineVertexIndex++] = vecSize.z;
			lineCount++;

			total += 2;
		}

		if ((sides & LINE_UP_EAST) != 0) {
			lineVertexBuffer[lineVertexIndex++] = vecSize.x;
			lineVertexBuffer[lineVertexIndex++] = vecSize.y;
			lineVertexBuffer[lineVertexIndex++] = vecZero.z;
			lineCount++;

			lineVertexBuffer[lineVertexIndex++] = vecSize.x;
			lineVertexBuffer[lineVertexIndex++] = vecSize.y;
			lineVertexBuffer[lineVertexIndex++] = vecSize.z;
			lineCount++;

			total += 2;
		}


		if ((sides & LINE_DOWN_NORTH) != 0) {
			lineVertexBuffer[lineVertexIndex++] = vecZero.x;
			lineVertexBuffer[lineVertexIndex++] = vecZero.y;
			lineVertexBuffer[lineVertexIndex++] = vecZero.z;
			lineCount++;

			lineVertexBuffer[lineVertexIndex++] = vecSize.x;
			lineVertexBuffer[lineVertexIndex++] = vecZero.y;
			lineVertexBuffer[lineVertexIndex++] = vecZero.z;
			lineCount++;

			total += 2;
		}

		if ((sides & LINE_UP_NORTH) != 0) {
			lineVertexBuffer[lineVertexIndex++] = vecZero.x;
			lineVertexBuffer[lineVertexIndex++] = vecSize.y;
			lineVertexBuffer[lineVertexIndex++] = vecZero.z;
			lineCount++;

			lineVertexBuffer[lineVertexIndex++] = vecSize.x;
			lineVertexBuffer[lineVertexIndex++] = vecSize.y;
			lineVertexBuffer[lineVertexIndex++] = vecZero.z;
			lineCount++;

			total += 2;
		}

		if ((sides & LINE_DOWN_SOUTH) != 0) {
			lineVertexBuffer[lineVertexIndex++] = vecZero.x;
			lineVertexBuffer[lineVertexIndex++] = vecZero.y;
			lineVertexBuffer[lineVertexIndex++] = vecSize.z;
			lineCount++;

			lineVertexBuffer[lineVertexIndex++] = vecSize.x;
			lineVertexBuffer[lineVertexIndex++] = vecZero.y;
			lineVertexBuffer[lineVertexIndex++] = vecSize.z;
			lineCount++;

			total += 2;
		}

		if ((sides & LINE_UP_SOUTH) != 0) {
			lineVertexBuffer[lineVertexIndex++] = vecZero.x;
			lineVertexBuffer[lineVertexIndex++] = vecSize.y;
			lineVertexBuffer[lineVertexIndex++] = vecSize.z;
			lineCount++;

			lineVertexBuffer[lineVertexIndex++] = vecSize.x;
			lineVertexBuffer[lineVertexIndex++] = vecSize.y;
			lineVertexBuffer[lineVertexIndex++] = vecSize.z;
			lineCount++;

			total += 2;
		}

		if ((sides & LINE_NORTH_WEST) != 0) {
			lineVertexBuffer[lineVertexIndex++] = vecZero.x;
			lineVertexBuffer[lineVertexIndex++] = vecZero.y;
			lineVertexBuffer[lineVertexIndex++] = vecZero.z;
			lineCount++;

			lineVertexBuffer[lineVertexIndex++] = vecZero.x;
			lineVertexBuffer[lineVertexIndex++] = vecSize.y;
			lineVertexBuffer[lineVertexIndex++] = vecZero.z;
			lineCount++;

			total += 2;
		}

		if ((sides & LINE_NORTH_EAST) != 0) {
			lineVertexBuffer[lineVertexIndex++] = vecSize.x;
			lineVertexBuffer[lineVertexIndex++] = vecZero.y;
			lineVertexBuffer[lineVertexIndex++] = vecZero.z;
			lineCount++;

			lineVertexBuffer[lineVertexIndex++] = vecSize.x;
			lineVertexBuffer[lineVertexIndex++] = vecSize.y;
			lineVertexBuffer[lineVertexIndex++] = vecZero.z;
			lineCount++;

			total += 2;
		}

		if ((sides & LINE_SOUTH_WEST) != 0) {
			lineVertexBuffer[lineVertexIndex++] = vecZero.x;
			lineVertexBuffer[lineVertexIndex++] = vecZero.y;
			lineVertexBuffer[lineVertexIndex++] = vecSize.z;
			lineCount++;

			lineVertexBuffer[lineVertexIndex++] = vecZero.x;
			lineVertexBuffer[lineVertexIndex++] = vecSize.y;
			lineVertexBuffer[lineVertexIndex++] = vecSize.z;
			lineCount++;

			total += 2;
		}

		if ((sides & LINE_SOUTH_EAST) != 0) {
			lineVertexBuffer[lineVertexIndex++] = vecSize.x;
			lineVertexBuffer[lineVertexIndex++] = vecZero.y;
			lineVertexBuffer[lineVertexIndex++] = vecSize.z;
			lineCount++;

			lineVertexBuffer[lineVertexIndex++] = vecSize.x;
			lineVertexBuffer[lineVertexIndex++] = vecSize.y;
			lineVertexBuffer[lineVertexIndex++] = vecSize.z;
			lineCount++;

			total += 2;
		}

		for (int i = 0; i < total; i++) {
			lineColorBuffer[lineColorIndex++] = red;
			lineColorBuffer[lineColorIndex++] = green;
			lineColorBuffer[lineColorIndex++] = blue;
			lineColorBuffer[lineColorIndex++] = alpha;
		}
	}
}
