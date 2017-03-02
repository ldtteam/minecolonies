package com.minecolonies.coremod.colony;

import com.google.common.annotations.VisibleForTesting;
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
 *
 * @param <T> Type of IColony (Colony or ColonyView)
 */
public final class ColonyList<T extends IColony> implements Iterable<T>
{
    @VisibleForTesting
    static final int INITIAL_SIZE = 16;

    private IColony[] list = new IColony[INITIAL_SIZE];

    private final List<Integer> nullIndices = new ArrayList<>();

    private int topID = 0;

    private int size = 0;

    /**
     * Get the Colony with the provided colony id.
     * @param index colony id.
     * @return The Colony associated with the provided id.
     */
    @Nullable
    // no way to remove this, java does it too
    @SuppressWarnings("unchecked")
    public T get(int index)
    {
        if (index < 1 || index > list.length)
        {
            return null;
        }

        return (T) list[index];
    }

    /**
     * Creates a new Colony, adds it to the list, and returns it.
     * @param world The world for the Colony.
     * @param position The position for the Colony center.
     * @return The newly created Colony.
     */
    @NotNull
    public Colony create(World world, BlockPos position)
    {
        final int colonyID = getNextColonyID();
        if (colonyID >= list.length)
        {
            expandList();
        }

        final Colony colony = new Colony(colonyID, world, position);
        size++;
        list[colony.getID()] = colony;
        return colony;
    }

    /**
     * Add a new Colony to the List.
     * @param colony colony to add to the list.
     */
    public void add(T colony)
    {
        final T existingColony = get(colony.getID());
        if (existingColony != null && existingColony != colony)
        {
            throw new IllegalArgumentException(
                    String.format("Already a colony registered to id=%d, colony=%s, not changing to colony=%s",
                            colony.getID(),
                            existingColony.getName(),
                            colony.getName()));
        }

        while (colony.getID() >= list.length)
        {
            expandList();
        }

        size++;

        list[colony.getID()] = colony;
    }

    /**
     * Remove the Colony from the list.
     * @param colony the Colony to remove.
     */
    public void remove(T colony)
    {
        remove(colony.getID());
    }

    /**
     * Remove the colony with the provided id from the list.
     * @param id colony id to remove.
     */
    public void remove(int id)
    {
        if (list[id] == null)
        {
            Log.getLogger().warn("Tried to remove colony with id=%d, but it didn't exist.", id);
        }

        size--;
        list[id] = null;

        if (!nullIndices.contains(id))
        {
            nullIndices.add(id);
        }
    }

    private int getNextColonyID()
    {
        if (nullIndices.isEmpty())
        {

            return ++topID;
        }

        return nullIndices.remove(0);
    }

    private void expandList()
    {
        final IColony[] newList = new IColony[list.length * 2];
        System.arraycopy(list, 0, newList, 0, list.length);
        list = newList;
    }

    /**
     * Empty the list.
     */
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

    /**
     * Return the number of Colonies in the list.
     * @return number of Colonies in the list.
     */
    public int size()
    {
        return size;
    }

    /**
     * Checks if there are Colonies in the list.
     * @return true if there are no Colonies.
     */
    public boolean isEmpty()
    {
        return size == 0;
    }

    /**
     * Copy all of the colonies to a List. Because this does a copy, it should only
     * be used when really needed.
     * @return List of Colonies.
     */
    @NotNull
    public List<T> getCopyAsList()
    {
        final List<T> copyList = new ArrayList<>();
        for (final T colony : this)
        {
            copyList.add(colony);
        }

        return copyList;
    }

    /**
     * Makes an iterator for the list.
     * @return an iterator for the colonies.
     */
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
                if (index >= list.length)
                {
                    throw new NoSuchElementException();
                }
                return get(index);
            }
        };
    }

    private int getNextIndex(int startingIndex)
    {
        int index = startingIndex + 1;
        while (index < list.length)
        {
            if (list[index] != null)
            {
                return index;
            }

            index++;
        }

        return index;
    }

    /**
     * Create a Stream of Colonies.
     * @return a Colony Stream.
     */
    public Stream<T> stream()
    {
        return StreamSupport.stream(spliterator(), false);
    }
}
