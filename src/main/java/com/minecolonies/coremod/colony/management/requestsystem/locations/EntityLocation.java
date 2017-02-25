package com.minecolonies.coremod.colony.management.requestsystem.locations;

import com.minecolonies.coremod.colony.management.requestsystem.api.IRequestManager;
import com.minecolonies.coremod.colony.management.requestsystem.api.location.ILocation;
import com.minecolonies.coremod.colony.management.requestsystem.api.location.ILocationFactory;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Created by marcf on 2/25/2017.
 */
public class EntityLocation implements ILocation {

    @NotNull
    private final UUID uuid;

    @Nullable
    private Entity entity;

    public EntityLocation(@NotNull UUID uuid) {
        this.uuid = uuid;
        checkEntity();
    }


    /**
     * Method to get the location in the dimension
     *
     * @return The location.
     */
    @NotNull
    @Override
    public BlockPos getLocation() {
        checkEntity();
        if (entity == null)
            return BlockPos.ORIGIN;

        return entity.getPosition();
    }

    /**
     * Method to get the dimension of the location.
     *
     * @return The dimension of the location.
     */
    @NotNull
    @Override
    public int getDimension() {
        checkEntity();
        if (entity == null)
            return 0;

        return entity.dimension;
    }

    /**
     * Method to check if this location is reachable from the other.
     *
     * @param location The check if it is reachable from here.
     * @return True when reachable, false when not.
     */
    @Override
    public boolean isReachableFromLocation(@NotNull ILocation location) {
        checkEntity();
        if (entity == null)
            return false;

        return location.getDimension() == getDimension();
    }

    private void checkEntity() {
        if (entity != null)
            return;

        entity = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityFromUuid(uuid);
    }

    public static class Factory implements ILocationFactory<EntityLocation, NBTTagCompound> {

        ////// --------------------------- NBTConstants --------------------------- \\\\\\
        private static final String NBT_MSB = "Id_MSB";
        private static final String NBT_LSB = "Id_LSB";
        ////// --------------------------- NBTConstants --------------------------- \\\\\\

        /**
         * Method to get the location type this factory can produce.
         *
         * @return The type of location this factory can produce.
         */
        @NotNull
        @Override
        public Class<? extends EntityLocation> getFactoryProductionType() {
            return EntityLocation.class;
        }

        /**
         * Method to serialize a given Request.
         *
         * @param manager  The manager that requested the serialization.
         * @param location The location to serialize.
         * @return The serialized data of the given location.
         */
        @NotNull
        @Override
        public NBTTagCompound serializeLocation(@NotNull IRequestManager manager, @NotNull EntityLocation location) {
            NBTTagCompound compound = new NBTTagCompound();

            compound.setLong(NBT_LSB, location.uuid.getLeastSignificantBits());
            compound.setLong(NBT_MSB, location.uuid.getMostSignificantBits());

            return compound;
        }

        /**
         * Method to deserialize a given Request.
         *
         * @param manager The manager requesting
         * @param nbt     The data of the location that should be deserialized.
         * @return The location that corresponds with the given data in the nbt
         */
        @NotNull
        @Override
        public EntityLocation deserializeLocation(@NotNull IRequestManager manager, @NotNull NBTTagCompound nbt) {
            UUID uuid = new UUID(nbt.getLong(NBT_MSB), nbt.getLong(NBT_LSB));

            return new EntityLocation(uuid);
        }


    }
}
