package com.minecolonies.api.util;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class NBTUtils
{

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

    private static class TagListIterator implements Iterator<INBT>
    {

        private final ListNBT list;
        private int currentIndex = 0;
        private TagListIterator(final ListNBT list) {this.list = list;}

        @Override
        public boolean hasNext()
        {
            return currentIndex < list.tagCount();
        }

        @Override
        public INBT next()
        {
            return list.getCompoundTagAt(currentIndex++);
        }
    }
}
