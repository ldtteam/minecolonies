package com.minecolonies.api.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class NBTUtils
{

    public static Stream<CompoundTag> streamCompound(final ListTag list)
    {
        return streamBase(list).filter(b -> b instanceof CompoundTag).map(b -> (CompoundTag) b);
    }

    public static Stream<Tag> streamBase(final ListTag list)
    {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new TagListIterator(list), Spliterator.ORDERED), false);
    }

    public static Collector<CompoundTag, ?, ListTag> toListNBT()
    {
        return Collectors.collectingAndThen(
          Collectors.toList(),
          list -> {
              final ListTag tagList = new ListTag();
              tagList.addAll(list);

              return tagList;
          });
    }

    public static BlockPos readBlockPos(@NotNull final CompoundTag compound, @NotNull final String name)
    {
        int[] aint = compound.getIntArray(name);
        return aint.length == 3 ? new BlockPos(aint[0], aint[1], aint[2]) : null;
    }

    public static BlockPos readBlockPos(@NotNull final Tag compound)
    {
        int[] aint = ((IntArrayTag) compound).getAsIntArray();
        return aint.length == 3 ? new BlockPos(aint[0], aint[1], aint[2]) : null;
    }

    public static Tag writeBlockPos(@NotNull final BlockPos pos)
    {
        return new IntArrayTag(new int[]{pos.getX(), pos.getY(), pos.getZ()});
    }

    private static class TagListIterator implements Iterator<Tag>
    {

        private final ListTag list;
        private       int     currentIndex = 0;

        private TagListIterator(final ListTag list) {this.list = list;}

        @Override
        public boolean hasNext()
        {
            return currentIndex < list.size();
        }

        @Override
        public Tag next()
        {
            return list.getCompound(currentIndex++);
        }
    }
}
