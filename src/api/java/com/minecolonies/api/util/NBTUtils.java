package com.minecolonies.api.util;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class NBTUtils
{

    public static Stream<NBTTagCompound> streamCompound(final NBTTagList list)
    {
        return streamBase(list).filter(b -> b instanceof NBTTagCompound).map(b -> (NBTTagCompound) b);
    }

    public static Stream<NBTBase> streamBase(final NBTTagList list)
    {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new TagListIterator(list), Spliterator.ORDERED), false);
    }

    public static Collector<NBTTagCompound, ?, NBTTagList> toNBTTagList()
    {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    final NBTTagList tagList = new NBTTagList();
                    list.forEach(tagList::appendTag);

                    return tagList;
                });
    }

    private static class TagListIterator implements Iterator<NBTBase>
    {

        private final NBTTagList list;
        private int currentIndex = 0;
        private TagListIterator(final NBTTagList list) {this.list = list;}

        @Override
        public boolean hasNext()
        {
            return currentIndex < list.tagCount();
        }

        @Override
        public NBTBase next()
        {
            return list.getCompoundTagAt(currentIndex++);
        }
    }
}
