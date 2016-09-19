package com.minecolonies.util;

import com.minecolonies.colony.ColonyManager;
import net.minecraft.util.BlockPos;
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
     * Max x/z position in vanilla minecraft.
     */
    private static final int MAX_POSITION =  30_000_000;

    @Test
    public void test2DTwoPositions()
    {
        for (int i = -400; i < 400; i+=20)
        {
            BlockPos posA = new BlockPos(i*10, 0, i*2);
            BlockPos posB = new BlockPos(i, 0, i*5);

            long distance = BlockPosUtil.getDistanceSquared2D(posA, posB);

            assertThat("2Dim distance between " + posA + " and " + posB, distance, greaterThanOrEqualTo(0L));
        }

        for (int i = -20_000_000; i < 20_000_000; i+=1230)
        {
            BlockPos posA = new BlockPos(i/10, 0, i);
            BlockPos posB = new BlockPos(i, 0, i/5);

            long distance = BlockPosUtil.getDistanceSquared2D(posA, posB);

            assertThat("2Dim distance between " + posA + " and " + posB, distance, greaterThanOrEqualTo(0L));
        }
    }

    @Test
    public void test3DTwoPositions()
    {
        for (int i = -400; i < 400; i+=20)
        {
            BlockPos posA = new BlockPos(i*10, i*3, i*2);
            BlockPos posB = new BlockPos(i, i*4, i*5);

            long distance = BlockPosUtil.getDistanceSquared2D(posA, posB);

            assertThat("2Dim distance between " + posA + " and " + posB, distance, greaterThanOrEqualTo(0L));
        }

        for (int i = -20_000_000; i < 20_000_000; i+=1230)
        {
            BlockPos posA = new BlockPos(i/10, i/2, i);
            BlockPos posB = new BlockPos(i, i/3, i/5);

            long distance = BlockPosUtil.getDistanceSquared2D(posA, posB);

            assertThat("2Dim distance between " + posA + " and " + posB, distance, greaterThanOrEqualTo(0L));
        }
    }
}