package com.minecolonies.core.colony.requestsystem.locations;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.location.ILocationFactory;
import com.minecolonies.api.util.constant.SerializationIdentifierConstants;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.UUID;

/**
 * Location described by an Entity.
 */
public class EntityLocation implements ILocation
{

    @NotNull
    private final UUID uuid;

    @NotNull
    private WeakReference<Entity> entity = new WeakReference<>(null);

    public EntityLocation(@NotNull final UUID uuid)
    {
        this.uuid = uuid;
        checkEntity();
    }

    private void checkEntity()
    {
        if (entity.get() != null)
        {
            return;
        }

        for (final ServerLevel world : ServerLifecycleHooks.getCurrentServer().levels.values())
        {
            try
            {
                final Entity ent = world.getEntity(uuid);
                if (ent != null)
                {
                    entity = new WeakReference<>(ent);
                    return;
                }
            }
            catch (final NullPointerException ex)
            {

            }
        }
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
        checkEntity();
        final Entity entityRef = entity.get();
        if (entityRef == null)
        {
            return BlockPos.ZERO;
        }
        else
        {
            return entityRef.blockPosition();
        }
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
        checkEntity();
        final Entity entityRef = entity.get();
        if (entityRef == null)
        {
            return Level.OVERWORLD;
        }
        else
        {
            return entityRef.getCommandSenderWorld().dimension();
        }
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
        checkEntity();
        return !(entity == null || entity.get() == null) && location.getDimension() == getDimension();
    }

    /**
     * Returns the *player* entity the location is tracking.
     *
     * @return player entity being tracked, or null if the tracked entity is not a player.
     */
    public Player getPlayerEntity()
    {
        checkEntity();
        final Entity entityRef = entity.get();
        return entityRef instanceof Player ? (Player) entityRef : null;
    }

    @SuppressWarnings("squid:S2972")
    /**
     * We have this class the way it is for a reason.
     */
    public static class Factory implements ILocationFactory<Entity, EntityLocation>
    {
        ////// --------------------------- NBTConstants --------------------------- \\\\\\
        private static final String NBT_MSB = "Id_MSB";
        private static final String NBT_LSB = "Id_LSB";
        ////// --------------------------- NBTConstants --------------------------- \\\\\\

        @NotNull
        @Override
        @SuppressWarnings("squid:LeftCurlyBraceStartLineCheck")
        /**
         * Moving the curly braces really makes the code hard to read.
         */
        public TypeToken<EntityLocation> getFactoryOutputType()
        {
            return TypeToken.of(EntityLocation.class);
        }

        @NotNull
        @Override
        @SuppressWarnings("squid:LeftCurlyBraceStartLineCheck")
        /**
         * Moving the curly braces really makes the code hard to read.
         */
        public TypeToken<Entity> getFactoryInputType()
        {
            return TypeToken.of(Entity.class);
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
        public CompoundTag serialize(@NotNull final IFactoryController controller, @NotNull final EntityLocation request)
        {
            final CompoundTag compound = new CompoundTag();

            compound.putLong(NBT_LSB, request.uuid.getLeastSignificantBits());
            compound.putLong(NBT_MSB, request.uuid.getMostSignificantBits());

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
        public EntityLocation deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundTag nbt)
        {
            final UUID uuid = new UUID(nbt.getLong(NBT_MSB), nbt.getLong(NBT_LSB));

            return new EntityLocation(uuid);
        }

        /**
         * Method to get a new instance of a location given the input.
         *
         * @param factoryController The {@link IFactoryController} that called this method.
         * @param input             The input to build a new location for.
         * @return The new output instance for a given input.
         */
        @NotNull
        @Override
        public EntityLocation getNewInstance(@NotNull final IFactoryController factoryController, @NotNull final Entity input)
        {
            return new EntityLocation(input.getUUID());
        }

        @Override
        public void serialize(IFactoryController controller, EntityLocation input, FriendlyByteBuf packetBuffer)
        {
            EntityLocation.serialize(packetBuffer, input);
        }

        @Override
        public EntityLocation deserialize(IFactoryController controller, FriendlyByteBuf buffer) throws Throwable
        {
            return EntityLocation.deserialize(buffer);
        }

        @Override
        public short getSerializationId()
        {
            return SerializationIdentifierConstants.ENTITY_LOCATION_ID;
        }
    }

    /**
     * Serialize this location to the given {@link FriendlyByteBuf}.
     *
     * @param buffer the buffer to serialize this location to.
     */
    public static void serialize(FriendlyByteBuf buffer, EntityLocation location)
    {
        buffer.writeUUID(location.uuid);
    }

    /**
     * Deserialize the location from the given {@link FriendlyByteBuf}
     *
     * @param buffer the buffer to read.
     * @return the deserialized location.
     */
    public static EntityLocation deserialize(FriendlyByteBuf buffer)
    {
        final UUID uuid = buffer.readUUID();

        return new EntityLocation(uuid);
    }
}
