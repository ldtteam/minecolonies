package com.minecolonies.util;

import com.minecolonies.colony.ColonyManager;
import net.minecraft.util.BlockPos;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

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
    public void testDistance2DTwoPositions()
    {
        for (int i = -400; i < 400; i+=20)
        {
            BlockPos posA = new BlockPos(i^2, 0, i*2);
            BlockPos posB = new BlockPos(i, 0, i^3);

            long distance = BlockPosUtil.getDistanceSquared2D(posA, posB);

            assertThat("2Dim distance between " + posA + " and " + posB, distance, greaterThanOrEqualTo(0L));
        }

        for(int i = 0; i < 100 ; i++)
        {
            BlockPos posA = new BlockPos(MAX_POSITION-i, 0, MAX_POSITION-i);
            BlockPos posB = new BlockPos(-MAX_POSITION+i, 0, -MAX_POSITION+i);

            long distance = BlockPosUtil.getDistanceSquared(posA, posB);

            assertThat("2Dim distance between " + posA + " and " + posB, distance, greaterThanOrEqualTo(0L));
        }
    }

    @Test
    public void testDistance3DTwoPositions()
    {
        for (int i = -400; i < 400; i+=20)
        {
            BlockPos posA = new BlockPos(i^2, i*3, i*2);
            BlockPos posB = new BlockPos(i, i*4, i^3);

            long distance = BlockPosUtil.getDistanceSquared(posA, posB);

            assertThat("2Dim distance between " + posA + " and " + posB, distance, greaterThanOrEqualTo(0L));
        }

        for(int i = 0; i < 100 ; i++)
        {
            BlockPos posA = new BlockPos(MAX_POSITION-i, i, MAX_POSITION-i);
            BlockPos posB = new BlockPos(-MAX_POSITION+i, 255-i, -MAX_POSITION+i);

            long distance = BlockPosUtil.getDistanceSquared(posA, posB);

            assertThat("2Dim distance between " + posA + " and " + posB, distance, greaterThanOrEqualTo(0L));
        }

    }
}