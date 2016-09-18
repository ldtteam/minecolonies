package com.minecolonies.util;

import com.minecolonies.colony.ColonyManager;
import net.minecraft.util.BlockPos;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;

@PrepareForTest({ColonyManager.class, LanguageHandler.class})
@RunWith(PowerMockRunner.class)
public class DistanceSquaredTest
{
    /**
     * Random seed.
     */
    private static final Random random = new Random(1839045834);

    /**
     * Nunber of iterations in testCase.
     */
    private static final int NOTestCases = 10000;

    @Test
    public void test2DTwoPositions()
    {
        for (int i = 0; i < NOTestCases; i++)
        {
            BlockPos posA = new BlockPos(random.nextInt(), 0, random.nextInt());
            BlockPos posB = new BlockPos(random.nextInt(), 0, random.nextInt());

            long distance = BlockPosUtil.getDistanceSquared2D(posA, posB);

            assertThat("2Dim distance between " + posA + " and " + posB, distance, greaterThanOrEqualTo(0L));
        }
    }

    @Test
    public void test3DTwoPositions()
    {
        for (int i = 0; i < NOTestCases; i++)
        {
            BlockPos posA = new BlockPos(random.nextInt(), random.nextInt(), random.nextInt());
            BlockPos posB = new BlockPos(random.nextInt(), random.nextInt(), random.nextInt());

            long distance = BlockPosUtil.getDistanceSquared(posA, posB);

            assertThat("3Dim distance between " + posA + " and " + posB, distance, greaterThanOrEqualTo(0L));
        }
    }
}