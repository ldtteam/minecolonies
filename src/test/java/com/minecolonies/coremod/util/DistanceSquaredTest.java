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
     * String decribing test.
     */
    private static final String DIM_STRING = "%d Dim distance between %s and %s";

    @Test
    public void testDistance2DTwoPositions()
    {
        for (int i = -400; i < 400; i += 20)
        {
            final BlockPos posA = new BlockPos(i * i, 0, i * 2);
            final BlockPos posB = new BlockPos(i, 0, i * i * i);

            assertThat(String.format(DIM_STRING, 2, posA.toString(), posB.toString()),
                    BlockPosUtil.getDistanceSquared2D(posA, posB), greaterThanOrEqualTo(0L));
        }
    }

    @Test
    public void testDistance2DTwoPositionsMaxValues()
    {
        for (int i = 0; i < 100; i++)
        {
            final BlockPos posA = new BlockPos(MAX_POSITION - i, 0, MAX_POSITION - i);
            final BlockPos posB = new BlockPos(-MAX_POSITION + i, 0, -MAX_POSITION + i);

            assertThat(String.format(DIM_STRING, 2, posA.toString(), posB.toString()),
                    BlockPosUtil.getDistanceSquared2D(posA, posB), greaterThanOrEqualTo(0L));
        }
    }

    @Test
    public void testDistance3DTwoPositions()
    {
        for (int i = -400; i < 400; i += 20)
        {
            final BlockPos posA = new BlockPos(i * i, i % 200, i * 2);
            final BlockPos posB = new BlockPos(i, (i + 100) % 200, i * i * i);

            assertThat(String.format(DIM_STRING, 3, posA.toString(), posB.toString()),
                    BlockPosUtil.getDistanceSquared(posA, posB), greaterThanOrEqualTo(0L));
        }
    }

    @Test
    public void testDistance3DTwoPositionsMaxValues()
    {
        for (int i = 0; i < 100; i++)
        {
            final BlockPos posA = new BlockPos(MAX_POSITION - i, 0, MAX_POSITION - i);
            final BlockPos posB = new BlockPos(-MAX_POSITION + i, i, -MAX_POSITION + i);

            assertThat(String.format(DIM_STRING, 3, posA.toString(), posB.toString()),
                    BlockPosUtil.getDistanceSquared(posA, posB), greaterThanOrEqualTo(0L));
        }
    }
}