package com.minecolonies.api.util;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class NBTUtils
{
    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_BOUNDINGBOX_MINCORNER = "minCorner";
    private static final String NBT_BOUNDINGBOX_MAXCORNER = "maxCorner";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    public static Stream<CompoundNBT> streamCompound(final ListNBT list)
    {
        return streamBase(list).filter(b -> b instanceof CompoundNBT).map(b -> (CompoundNBT) b);
    }

    public static Stream<INBT> streamBase(final ListNBT list)
    {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new TagListIterator(list), Spliterator.ORDERED), false);
    }

    public static Collector<CompoundNBT, ?, ListNBT> toListNBT()
    {
        return Collectors.collectingAndThen(
          Collectors.toList(),
          list -> {
              final ListNBT tagList = new ListNBT();
              list.forEach(tagList::add);

              return tagList;
          });
    }

    public static CompoundNBT writeBoundingBox(final AxisAlignedBB boundingBox)
    {
        final CompoundNBT nbt = new CompoundNBT();
        BlockPosUtil.write(nbt, NBT_BOUNDINGBOX_MINCORNER, new BlockPos(boundingBox.minX, boundingBox.minY, boundingBox.minZ));
        BlockPosUtil.write(nbt, NBT_BOUNDINGBOX_MAXCORNER, new BlockPos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ));
        return nbt;
    }

    public static AxisAlignedBB readBoundingBox(final CompoundNBT nbt)
    {
        return new AxisAlignedBB(BlockPosUtil.read(nbt, NBT_BOUNDINGBOX_MINCORNER), BlockPosUtil.read(nbt, NBT_BOUNDINGBOX_MAXCORNER));
    }

    private static class TagListIterator implements Iterator<INBT>
    {

        private final ListNBT list;
        private       int     currentIndex = 0;

        private TagListIterator(final ListNBT list) {this.list = list;}

        @Override
        public boolean hasNext()
        {
            return currentIndex < list.size();
        }

        @Override
        public INBT next()
        {
            return list.getCompound(currentIndex++);
        }
    }
}
