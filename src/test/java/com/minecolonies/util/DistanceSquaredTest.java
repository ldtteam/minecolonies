package com.minecolonies.util;

import com.minecolonies.test.AbstractTest;
import net.minecraft.util.math.BlockPos;
import org.junit.Test;

import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;

public class DistanceSquaredTest extends AbstractTest
{

    /**
     * Max x/z position in vanilla minecraft.
     */
    private static final int MAX_POSITION = 30_000_000;

    /**
     * Max y position in vanilla minecraft.
     */
    private static final int MAX_HEIGHT = 500;

    @Override
    public String getTestName()
    {
        return "DistanceSquaredTest";
    }

    /**
     * Tests the distance between two BlockPos and checks if it overflows.
     */
    private void testDistance2D(final BlockPos posA, final BlockPos posB)
    {
        final long distance = BlockPosUtil.getDistanceSquared2D(posA, posB);

        assertThat("2Dim distance between " + posA + " and " + posB, distance, greaterThanOrEqualTo(0L));
    }

    /**
     * Tests the distance between two BlockPos and checks if it overflows.
     */
    private void testDistance3D(final BlockPos posA, final BlockPos posB)
    {
        final long distance = BlockPosUtil.getDistanceSquared(posA, posB);

        assertThat("3Dim distance between " + posA + " and " + posB, distance, greaterThanOrEqualTo(0L));
    }

    @Test
    public void testDistance2DTwoPositions()
    {
        for (int i = -400; i < 400; i += 20)
        {
            final BlockPos posA = new BlockPos(Math.pow(i, 2), 0, i * 2);
            final BlockPos posB = new BlockPos(i, 0, Math.pow(i, 3));

            testDistance2D(posA, posB);
        }
    }

    @Test
    public void testDistance2DTwoPositionsMaxValues()
    {
        for (int i = 0; i < 100; i++)
        {
            final BlockPos posA = new BlockPos(MAX_POSITION - i, 0, MAX_POSITION - i);
            final BlockPos posB = new BlockPos(-MAX_POSITION + i, 0, -MAX_POSITION + i);

            testDistance2D(posA, posB);
        }
    }

    @Test
    public void testDistance2DTwoPositionsRandomValues()
    {
        final Random random = this.getRandom();
        for (int i = 0; i < 100; i++)
        {
            final BlockPos posA = new BlockPos(random.nextInt(MAX_POSITION), 0, random.nextInt(MAX_POSITION));
            final BlockPos posB = new BlockPos(random.nextInt(MAX_POSITION), 0, random.nextInt(MAX_POSITION));

            testDistance2D(posA, posB);
        }
    }

    @Test
    public void testDistance3DTwoPositions()
    {
        for (int i = -400; i < 400; i += 20)
        {
            final BlockPos posA = new BlockPos(Math.pow(i, 2), i % 200, i * 2);
            final BlockPos posB = new BlockPos(i, (i + 100) % 200, Math.pow(i, 3));

            testDistance3D(posA, posB);
        }
    }

    @Test
    public void testDistance3DTwoPositionsMaxValues()
    {
        for (int i = 0; i < 100; i++)
        {
            final BlockPos posA = new BlockPos(MAX_POSITION - i, 0, MAX_POSITION - i);
            final BlockPos posB = new BlockPos(-MAX_POSITION + i, i, -MAX_POSITION + i);

            testDistance3D(posA, posB);
        }
    }

    @Test
    public void testDistance3DTwoPositionsRandomValues()
    {
        final Random random = this.getRandom();
        for (int i = 0; i < 100; i++)
        {
            final BlockPos posA = new BlockPos(random.nextInt(MAX_POSITION), random.nextInt(MAX_HEIGHT), random.nextInt(MAX_POSITION));
            final BlockPos posB = new BlockPos(random.nextInt(MAX_POSITION), random.nextInt(MAX_HEIGHT), random.nextInt(MAX_POSITION));

            testDistance3D(posA, posB);
        }
    }
}