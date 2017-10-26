package com.minecolonies.api.util;

import com.google.common.collect.Iterators;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class NBTUtils
{

    public static Stream<NBTBase> streamBase(NBTTagList list)
    {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new TagListIterator(list), Spliterator.ORDERED), false);
    }

    public static Stream<NBTTagCompound> streamCompound(NBTTagList list)
    {
        return streamBase(list).filter(b -> b instanceof NBTTagCompound).map(b -> (NBTTagCompound) b);
    }


    private static class TagListIterator implements Iterator<NBTBase>{

        private int currentIndex = 0;
        private final NBTTagList list;

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
