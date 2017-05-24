package com.minecolonies.api.colony;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

/**
 * ----------------------- Not Documented Object ---------------------
 * TODO: Document Object
 */
public interface IColonyList<T extends IColony> extends Iterable<T>
{
    @NotNull
    IColony create(World world, BlockPos position);

    void add(T colony);

    @Nullable
    // no way to remove this, java does it too
    @SuppressWarnings("unchecked")
    T get(int index);

    void remove(T colony);

    void remove(int id);

    void clear();

    int size();

    boolean isEmpty();

    @NotNull
    List<T> getCopyAsList();

    Stream<T> stream();
}
