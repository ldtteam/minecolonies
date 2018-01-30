package com.minecolonies.api.util;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
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

    public static Collector<NBTTagCompound, NBTTagList, NBTTagList> toNBTTagList()
    {
        return new Collector<NBTTagCompound, NBTTagList, NBTTagList>()
        {
            @Override
            public Supplier<NBTTagList> supplier()
            {
                return NBTTagList::new;
            }

            @Override
            public BiConsumer<NBTTagList, NBTTagCompound> accumulator()
            {
                return NBTTagList::appendTag;
            }

            @Override
            public BinaryOperator<NBTTagList> combiner()
            {
                return (list1, list2) -> {
                    final NBTTagList result = supplier().get();

                    streamBase(list1).forEach(result::appendTag);
                    streamBase(list2).forEach(result::appendTag);

                    return result;
                };
            }

            @Override
            public Function<NBTTagList, NBTTagList> finisher()
            {
                return (nbtTagList -> nbtTagList);
            }

            @Override
            public Set<Characteristics> characteristics()
            {
                return Collections.unmodifiableSet(EnumSet.of(Characteristics.UNORDERED, Characteristics.IDENTITY_FINISH));
            }
        };
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
