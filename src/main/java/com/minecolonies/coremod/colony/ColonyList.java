package com.minecolonies.coremod.colony;

import com.minecolonies.coremod.util.Log;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

//TODO extend list/collection
/**
 * Data structure for storing colonies, optimized for performance.
 *
 * @author Colton
 */
public final class ColonyList<T extends IColony> implements Iterable<T>
{
    private static final int INITIAL_SIZE = 16;

    private Object[] list = new Object[INITIAL_SIZE];

    private final List<Integer> nullIndices = new ArrayList<>();

    private int topID = 0;

    private int size = 0;

    @Nullable
    @SuppressWarnings("unchecked")
    public T get(int index)
    {
        if (index < 1 || index > list.length) {
            return null;
        }

        return (T) list[index];
    }

    @NotNull
    public Colony create(World world, BlockPos position)
    {
        Colony colony = new Colony(getNextColonyID(), world, position);
        list[colony.getID()] = colony;
        size++;
        return colony;
    }


    public void add(T colony)
    {
        T existingColony = get(colony.getID());
        if (existingColony != null && existingColony != colony) {
            throw new IllegalArgumentException(
                    String.format("Already a colony registered to id=%d, colony=%s, not changing to colony=%s",
                            colony.getID(),
                            existingColony.getName(),
                            colony.getName()));
        }

        list[colony.getID()] = colony;
    }

    public void remove(Colony colony)
    {
        remove(colony.getID());
    }

    public void remove(int id)
    {
        if (list[id] == null) {
            Log.getLogger().warn("Tried to remove colony with id=%d, but it didn't exist.", id);
        }

        size--;
        list[id] = null;

        if (!nullIndices.contains(id)) {
            nullIndices.add(id);
        }
    }

    private int getNextColonyID()
    {
        if (nullIndices.isEmpty()) {
            topID++;
            if (topID >= list.length) {
                // Expand list
                Object[] newList = new Object[list.length * 2];
                System.arraycopy(list, 0, newList, 0, list.length);
                list = newList;
            }

            return topID;
        }

        return nullIndices.remove(0);
    }

    public void clear()
    {
        for (int i = 0; i < list.length; i++)
        {
            list[i] = null;
        }

        nullIndices.clear();

        topID = 0;
        size = 0;
    }

    public int size()
    {
        return size;
    }

    public boolean isEmpty()
    {
        return size == 0;
    }

    @NotNull
    public List<T> getCopyAsList()
    {
        final List<T> copyList = new ArrayList<>();
        for (final T colony : this) {
            copyList.add(colony);
        }

        return copyList;
    }

    @Override
    public Iterator<T> iterator()
    {
        return new Iterator<T>()
        {

            private int nextIndex = getNextIndex(0);

            @Override
            public boolean hasNext()
            {
                return nextIndex < list.length;
            }

            @Override
            public T next()
            {
                final int index = nextIndex;
                nextIndex = getNextIndex(nextIndex);
                if (index >= list.length) {
                    throw new NoSuchElementException();
                }
                return get(index);
            }

            private int getNextIndex(int startingIndex) {
                int index = startingIndex + 1;
                while (index < list.length) {
                    if (list[index] != null) {
                        return index;
                    }

                    index++;
                }

                return index;
            }
        };
    }

    public Stream<T> stream()
    {
        return StreamSupport.stream(spliterator(), false);
    }
}
