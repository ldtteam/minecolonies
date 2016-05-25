package com.schematica.core.util;

import com.google.common.collect.AbstractIterator;
import net.minecraft.util.BlockPos;

import java.util.Iterator;

public class BlockPosHelper {
    public static Iterable<MBlockPos> getAllInBox(final BlockPos from, final BlockPos to) {
        return getAllInBox(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ());
    }

    public static Iterable<MBlockPos> getAllInBox(final int fromX, final int fromY, final int fromZ, final int toX, final int toY, final int toZ) {
        final BlockPos posMin = new BlockPos(Math.min(fromX, toX), Math.min(fromY, toY), Math.min(fromZ, toZ));
        final BlockPos posMax = new BlockPos(Math.max(fromX, toX), Math.max(fromY, toY), Math.max(fromZ, toZ));
        return new Iterable<MBlockPos>() {
            @Override
            public Iterator<MBlockPos> iterator() {
                return new AbstractIterator<MBlockPos>() {
                    private MBlockPos pos = null;
                    private int x;
                    private int y;
                    private int z;

                    @Override
                    protected MBlockPos computeNext() {
                        if (this.pos == null) {
                            this.x = posMin.getX();
                            this.y = posMin.getY();
                            this.z = posMin.getZ();
                            this.pos = new MBlockPos(this.x, this.y, this.z);
                            return this.pos;
                        }

                        if (this.pos.equals(posMax)) {
                            return this.endOfData();
                        }

                        if (this.x < posMax.getX()) {
                            this.x++;
                        } else if (this.y < posMax.getY()) {
                            this.x = posMin.getX();
                            this.y++;
                        } else if (this.z < posMax.getZ()) {
                            this.x = posMin.getX();
                            this.y = posMin.getY();
                            this.z++;
                        }

                        this.pos.x = this.x;
                        this.pos.y = this.y;
                        this.pos.z = this.z;
                        return this.pos;
                    }
                };
            }
        };
    }
}
