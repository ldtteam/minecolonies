package com.schematica.core.util;

import com.google.common.collect.AbstractIterator;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class BlockPosHelper
{
    @Nullable
    public static Iterable<BlockPos> getAllInBox(final int fromX, final int fromY, final int fromZ, final int toX, final int toY, final int toZ)
    {
        @NotNull final BlockPos posMin = new BlockPos(Math.min(fromX, toX), Math.min(fromY, toY), Math.min(fromZ, toZ));
        @NotNull final BlockPos posMax = new BlockPos(Math.max(fromX, toX), Math.max(fromY, toY), Math.max(fromZ, toZ));
        return new Iterable<BlockPos>()
        {
            @Nullable
            @Override
            public Iterator<BlockPos> iterator()
            {
                return new AbstractIterator<BlockPos>()
                {
                    @Nullable
                    private BlockPos pos = null;
                    private int x;
                    private int y;
                    private int z;

                    @Nullable
                    @Override
                    protected BlockPos computeNext()
                    {
                        if (this.pos == null)
                        {
                            this.x = posMin.getX();
                            this.y = posMin.getY();
                            this.z = posMin.getZ();
                            this.pos = new BlockPos(this.x, this.y, this.z);
                            return this.pos;
                        }

                        if (this.pos.equals(posMax))
                        {
                            return this.endOfData();
                        }

                        if (this.x < posMax.getX())
                        {
                            this.x++;
                        }
                        else if (this.y < posMax.getY())
                        {
                            this.x = posMin.getX();
                            this.y++;
                        }
                        else if (this.z < posMax.getZ())
                        {
                            this.x = posMin.getX();
                            this.y = posMin.getY();
                            this.z++;
                        }

                        this.pos = new BlockPos(this.x, this.y, this.z);
                        return this.pos;
                    }
                };
            }
        };
    }
}
