package com.minecolonies.coremod.util;

import com.minecolonies.api.util.BlockPosUtil;
import net.minecraft.util.math.BlockPos;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;

//TODO improve this test: check the value is correct, better edge cases
@RunWith(MockitoJUnitRunner.class)
public class DistanceSquaredTest
{
    /**
     * Max x/z position in vanilla minecraft.
     */
    private static final int MAX_POSITION = 30_000_000;

    /**
     * Max y position in vanilla minecraft.
     */
    private static final int MAX_HEIGHT = 500;

    @Test
    public void testDistance2DTwoPositions()
    {
        for (int i = -400; i < 400; i += 20)
        {
            final BlockPos posA = new BlockPos(i * i, 0, i * 2);
            final BlockPos posB = new BlockPos(i, 0, i * i * i);

            testDistance2D(posA, posB);
        }
    }

    /**
     * Tests the distance between two BlockPos and checks if it overflows.
     */
    private void testDistance2D(final BlockPos posA, final BlockPos posB)
    {
        final long distance = BlockPosUtil.getDistanceSquared2D(posA, posB);

        assertThat("2Dim distance between " + posA + " and " + posB, distance, greaterThanOrEqualTo(0L));
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
    public void testDistance3DTwoPositions()
    {
        for (int i = -400; i < 400; i += 20)
        {
            final BlockPos posA = new BlockPos(i * i, i % 200, i * 2);
            final BlockPos posB = new BlockPos(i, (i + 100) % 200, i * i * i);

            testDistance3D(posA, posB);
        }
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
    public void testDistance3DTwoPositionsMaxValues()
    {
        for (int i = 0; i < 100; i++)
        {
            final BlockPos posA = new BlockPos(MAX_POSITION - i, 0, MAX_POSITION - i);
            final BlockPos posB = new BlockPos(-MAX_POSITION + i, i, -MAX_POSITION + i);

            testDistance3D(posA, posB);
        }
    }
}