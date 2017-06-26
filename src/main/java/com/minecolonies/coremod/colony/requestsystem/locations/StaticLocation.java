package com.minecolonies.coremod.colony.requestsystem.locations;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.location.ILocationFactory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Location described by an immutable blockpos and a dimension.
 */
public class StaticLocation implements ILocation
{

    @NotNull
    private final BlockPos pos;

    private final int      dimension;

    StaticLocation(@NotNull BlockPos pos, int dimension)
    {
        this.pos = pos;
        this.dimension = dimension;
    }

    /**
     * Method to get the location in the dimension
     *
     * @return The location.
     */
    @NotNull
    @Override
    public BlockPos getInDimensionLocation()
    {
        return pos;
    }

    /**
     * Method to get the dimension of the location.
     *
     * @return The dimension of the location.
     */
    @Override
    public int getDimension()
    {
        return dimension;
    }

    /**
     * Method to check if this location is reachable from the other.
     *
     * @param location The check if it is reachable from here.
     * @return True when reachable, false when not.
     */
    @Override
    public boolean isReachableFromLocation(@NotNull ILocation location)
    {
        return location.getDimension() == getDimension();
    }

    /**
     * Internal factory class.
     */
    public static class Factory implements ILocationFactory<BlockPos, StaticLocation>
    {

        ////// --------------------------- NBTConstants --------------------------- \\\\\\
        private static final String NBT_POS = "Pos";
        private static final String NBT_DIM = "Dim";
        ////// --------------------------- NBTConstants --------------------------- \\\\\\

        /**
         * Method to serialize a given constructable.
         *
         * @param controller The controller that can be used to serialize complicated types.
         * @param request    The request to serialize.
         * @return The serialized data of the given requets.
         */
        @NotNull
        @Override
        public NBTTagCompound serialize(@NotNull IFactoryController controller, @NotNull StaticLocation request)
        {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setLong(NBT_POS, request.getInDimensionLocation().toLong());
            compound.setInteger(NBT_DIM, request.getDimension());
            return compound;
        }

        /**
         * Method to deserialize a given constructable.
         *
         * @param controller The controller that can be used to deserialize complicated types.
         * @param nbt        The data of the request that should be deserialized.
         * @return The request that corresponds with the given data in the nbt
         */
        @NotNull
        @Override
        public StaticLocation deserialize(@NotNull IFactoryController controller, @NotNull NBTTagCompound nbt)
        {
            BlockPos pos = BlockPos.fromLong(nbt.getLong(NBT_POS));
            Integer dim = nbt.getInteger(NBT_DIM);
            return new StaticLocation(pos, dim);
        }

        /**
         * Method to get a new instance of a location given the input.
         *
         *
         * Method not used in this factory.
         *
         *
         *
         * @param input The input to build a new location for.
         * @return The new output instance for a given input.
         */
        @NotNull
        @Override
        public StaticLocation getNewInstance(@NotNull BlockPos input)
        {
            return new StaticLocation(input, 0);
        }

        @NotNull
        @Override
        public TypeToken<StaticLocation> getFactoryOutputType()
        {
            return new TypeToken<StaticLocation>() {};
        }

        @NotNull
        @Override
        public TypeToken<BlockPos> getFactoryInputType()
        {
            return new TypeToken<BlockPos>() {};
        }

        @NotNull
        @Override
        public StaticLocation getNewInstance(@NotNull final BlockPos blockPos, @NotNull final Object... context) throws IllegalArgumentException
        {
            if (context.length != 1)
            {
                throw new IllegalArgumentException("Unsupported context - Not the correct amount available. Needed is 1!");
            }

            if (!(context[0] instanceof Integer))
            {
                throw new IllegalArgumentException("Unsupported context - First context object is not a Integer. Provide an Integer as Dimension.");
            }

            return new StaticLocation(blockPos, (Integer) context[0]);
        }
    }
}
