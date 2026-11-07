package com.minecolonies.core.entity.ai.workers.util;

import com.ldtteam.structurize.placement.AbstractBlueprintIterator;
import com.ldtteam.structurize.placement.structure.IStructureHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BlueprintFrameIterator extends AbstractBlueprintIterator
{
    /**
     * List of frame positions.
     */
    public List<BlockPos> frameList = new ArrayList<>();

    /**
     * Current progress index.
     */
    public int index = 0;

    /**
     * Initialize the blueprint iterator with the structure handler.
     * @param structureHandler the structure handler.
     */
    public BlueprintFrameIterator(final IStructureHandler structureHandler, final List<BlockPos> hframePos, final List<BlockPos> vframePos)
    {
        super(structureHandler, new BlockPos(structureHandler.getBluePrint().getSizeX(), structureHandler.getBluePrint().getSizeY(), structureHandler.getBluePrint().getSizeZ()));
        for (BlockPos pos : vframePos)
        {
            // First, until bottom of blueprint, below frame-pos.
            for (int i = 0; i <= pos.getY(); i++)
            {
                frameList.add(new BlockPos(pos.getX(), i, pos.getZ()));
            }
        }

        // Second for each pair, direct line.
        for (BlockPos pos : hframePos)
        {
            for (BlockPos pos2 : hframePos)
            {
                if (pos.getY() == pos2.getY())
                {
                    if (pos.getX() == pos2.getX())
                    {
                        for (int z = Math.min(pos.getZ(), pos2.getZ()); z <= Math.max(pos.getZ(), pos2.getZ()); z++)
                        {
                            final BlockPos newPos = new BlockPos(pos.getX(), pos.getY(), z);
                            if (!frameList.contains(newPos))
                            {
                                frameList.add(newPos);
                            }
                        }
                    }
                    if (pos.getZ() == pos2.getZ())
                    {
                        for (int x = Math.min(pos.getX(), pos2.getX()); x <= Math.max(pos.getX(), pos2.getX()); x++)
                        {
                            final BlockPos newPos = new BlockPos(x, pos.getY(), pos.getZ());
                            if (!frameList.contains(newPos))
                            {
                                frameList.add(newPos);
                            }
                        }
                    }
                }
            }
        }

        frameList.sort(Comparator.comparingInt(Vec3i::getY));
    }

    private Result iterate(boolean up)
    {
        if (index + 1 >= frameList.size())
        {
            return Result.AT_END;
        }

        index++;
        return Result.NEW_BLOCK;
    }

    @Override
    public BlockPos getProgressPos()
    {
        return frameList.get(index);
    }

    @Override
    public void setProgressPos(final BlockPos localPosition)
    {
        super.setProgressPos(localPosition);
        index = frameList.indexOf(localPosition);
    }

    @Override
    public BlockPos getPrevProgressPos()
    {
        if (index == 0)
        {
            return NULL_POS;
        }
        return frameList.get(index - 1);
    }

    @Override
    public void reset()
    {
        super.reset();
        index = 0;
    }

    @Override
    public Result increment()
    {
        return iterate(true);
    }

    @Override
    public Result decrement()
    {
       return iterate(false);
    }
}
