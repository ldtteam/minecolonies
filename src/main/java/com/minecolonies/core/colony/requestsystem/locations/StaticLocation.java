package com.minecolonies.core.colony.requestsystem.locations;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.location.ILocationFactory;
import com.minecolonies.api.util.constant.SerializationIdentifierConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Location described by an immutable blockpos and a dimension.
 */
public class StaticLocation implements ILocation
{

    private static final int NUMBER_OR_CONTEXTS = 1;

    @NotNull
    private final BlockPos pos;

    private final ResourceKey<Level> dimension;

    public StaticLocation(@NotNull final BlockPos pos, final ResourceKey<Level> dimension)
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
    @NotNull
    @Override
    public ResourceKey<Level> getDimension()
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
    public boolean isReachableFromLocation(@NotNull final ILocation location)
    {
        return location.getDimension() == getDimension();
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof StaticLocation))
        {
            return false;
        }

        final StaticLocation that = (StaticLocation) o;

        if (getDimension() != that.getDimension())
        {
            return false;
        }
        return pos.equals(that.pos);
    }

    @Override
    public int hashCode()
    {
        int result = pos.hashCode();
        result = 31 * result + getDimension().location().toString().hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "Dim: " + dimension.location() + " " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " ";
    }

    /**
     * Internal factory class.
     */
    @SuppressWarnings("squid:S2972")
    /**
     * We have this class the way it is for a reason.
     */
    public static class Factory implements ILocationFactory<BlockPos, StaticLocation>
    {

        ////// --------------------------- NBTConstants --------------------------- \\\\\\
        private static final String NBT_POS = "Pos";
        private static final String NBT_DIM = "Dim";
        ////// --------------------------- NBTConstants --------------------------- \\\\\\

        @NotNull
        @Override
        @SuppressWarnings("squid:LeftCurlyBraceStartLineCheck")
        /**
         * Moving the curly braces really makes the code hard to read.
         */
        public TypeToken<StaticLocation> getFactoryOutputType()
        {
            return TypeToken.of(StaticLocation.class);
        }

        @NotNull
        @Override
        @SuppressWarnings("squid:LeftCurlyBraceStartLineCheck")
        /**
         * Moving the curly braces really makes the code hard to read.
         */
        public TypeToken<BlockPos> getFactoryInputType()
        {
            return TypeToken.of(BlockPos.class);
        }

        /**
         * Method to serialize a given constructable.
         *
         * @param controller The controller that can be used to serialize complicated types.
         * @param request    The request to serialize.
         * @return The serialized data of the given requets.
         */
        @NotNull
        @Override
        public CompoundTag serialize(@NotNull final HolderLookup.Provider provider, @NotNull final IFactoryController controller, @NotNull final StaticLocation request)
        {
            final CompoundTag compound = new CompoundTag();
            compound.putLong(NBT_POS, request.getInDimensionLocation().asLong());
            compound.putString(NBT_DIM, request.getDimension().location().toString());
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
        public StaticLocation deserialize(@NotNull final HolderLookup.Provider provider, @NotNull final IFactoryController controller, @NotNull final CompoundTag nbt)
        {
            final BlockPos pos = BlockPos.of(nbt.getLong(NBT_POS));
            final String dim = nbt.getString(NBT_DIM);
            return new StaticLocation(pos, ResourceKey.create(Registries.DIMENSION, new ResourceLocation(dim)));
        }

        @NotNull
        @Override
        public StaticLocation getNewInstance(@NotNull final IFactoryController factoryController, @NotNull final BlockPos blockPos, @NotNull final Object... context)
        {
            if (context.length != NUMBER_OR_CONTEXTS)
            {
                throw new IllegalArgumentException("Unsupported context - Not the correct amount available. Needed is 1!");
            }

            if (!(context[0] instanceof ResourceKey))
            {
                throw new IllegalArgumentException("Unsupported context - First context object is not a ResourceLocation. Provide an ResourceLocation as Dimension.");
            }

            return new StaticLocation(blockPos, (ResourceKey<Level>) context[0]);
        }

        /**
         * Method to get a new instance of a location given the input. Method not used in this factory.
         *
         * @param input The input to build a new location for.
         * @return The new output instance for a given input.
         */
        @NotNull
        @Override
        public StaticLocation getNewInstance(@NotNull final IFactoryController factoryController, @NotNull final BlockPos input)
        {
            return new StaticLocation(input, Level.OVERWORLD);
        }

        @Override
        public void serialize(@NotNull IFactoryController controller, @NotNull StaticLocation input, RegistryFriendlyByteBuf packetBuffer)
        {
            StaticLocation.serialize(packetBuffer, input);
        }

        @NotNull
        @Override
        public StaticLocation deserialize(@NotNull IFactoryController controller, @NotNull RegistryFriendlyByteBuf buffer) throws Throwable
        {
            return StaticLocation.deserialize(buffer);
        }

        @Override
        public short getSerializationId()
        {
            return SerializationIdentifierConstants.STATIC_LOCATION_ID;
        }
    }

    /**
     * Serialize this location to the given {@link FriendlyByteBuf}.
     *
     * @param buffer the buffer to serialize this location to.
     */
    public static void serialize(FriendlyByteBuf buffer, StaticLocation location)
    {
        buffer.writeBlockPos(location.pos);
        buffer.writeUtf(location.dimension.location().toString());
    }

    /**
     * Deserialize the location from the given {@link FriendlyByteBuf}
     *
     * @param buffer the buffer to read.
     * @return the deserialized location.
     */
    public static StaticLocation deserialize(FriendlyByteBuf buffer)
    {
        final BlockPos pos = buffer.readBlockPos();
        final ResourceLocation dimension = new ResourceLocation(buffer.readUtf(32767));

        return new StaticLocation(pos, ResourceKey.create(Registries.DIMENSION, dimension));
    }
}
