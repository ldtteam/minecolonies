package com.minecolonies.core.entity.ai.workers.util;

import com.ldtteam.structurize.placement.AbstractBlueprintIterator;
import com.ldtteam.structurize.placement.AbstractDelegateBlueprintIterator;
import com.ldtteam.structurize.placement.StructureIterators;
import com.ldtteam.structurize.placement.structure.IStructureHandler;
import net.minecraft.core.BlockPos;

import java.util.function.Supplier;

public class LayerBlueprintIterator extends AbstractDelegateBlueprintIterator
{
    private int layer;

    public LayerBlueprintIterator(final AbstractBlueprintIterator delegate)
    {
        super(delegate);
    }

    public LayerBlueprintIterator(final String iteratorId, final IStructureHandler handler)
    {
        super(StructureIterators.getIterator(iteratorId, handler));
    }

    public void setLayer(final int newLevel)
    {
        layer = newLevel;
        delegate.setProgressPos(NULL_POS);
    }

    public int getLayer()
    {
        return layer;
    }

    private Result advance(Supplier<Result> nextPosition)
    {
        final boolean isAtBegin = delegate.getProgressPos().equals(NULL_POS);
        final Result result = nextPosition.get();

        if (result != Result.NEW_BLOCK)
        {
            return result;
        }

        if (isAtBegin)
        {
            // Fix the Y to the current level
            final BlockPos curPos = delegate.getProgressPos();
            delegate.setProgressPos(curPos.atY(layer));
        }
        else if (getProgressPos().getY() != layer)
        {
            // Done with the layer, which means this iterator is done
            return Result.AT_END;
        }
        return Result.NEW_BLOCK;
    }

    @Override
    public Result increment()
    {
        return advance(delegate::increment);
    }

    @Override
    public Result decrement()
    {
        return advance(delegate::decrement);
    }

    @Override
    public void setProgressPos(final BlockPos localPosition)
    {
        if (localPosition != NULL_POS && localPosition.getY() != layer)
        {
            delegate.setProgressPos(localPosition.atY(layer));
        }
        else
        {
            delegate.setProgressPos(localPosition);
        }
    }
}
