package com.github.lunatrius.core.world.chunk;

import java.util.Random;

public class ChunkHelper {
	public static boolean isSlimeChunk(long seed, int x, int z) {
		return new Random(seed + (x * x * 4987142) + (x * 5947611) + (z * z * 4392871) + (z * 389711) ^ 987234911).nextInt(10) == 0;
	}
}
