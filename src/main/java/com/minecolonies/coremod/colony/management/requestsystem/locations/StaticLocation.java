package com.minecolonies.coremod.colony.management.requestsystem.locations;

import com.minecolonies.coremod.colony.management.requestsystem.api.IRequestManager;
import com.minecolonies.coremod.colony.management.requestsystem.api.location.ILocation;
import com.minecolonies.coremod.colony.management.requestsystem.api.location.ILocationFactory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Created by marcf on 2/25/2017.
 */
public class StaticLocation implements ILocation {

    @NotNull
    private final BlockPos pos;
    @NotNull
    private final int dimension;

    public StaticLocation(@NotNull BlockPos pos, @NotNull int dimension) {
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
    public BlockPos getLocation() {
        return pos;
    }

    /**
     * Method to get the dimension of the location.
     *
     * @return The dimension of the location.
     */
    @NotNull
    @Override
    public int getDimension() {
        return dimension;
    }

    /**
     * Method to check if this location is reachable from the other.
     *
     * @param location The check if it is reachable from here.
     * @return True when reachable, false when not.
     */
    @Override
    public boolean isReachableFromLocation(@NotNull ILocation location) {
        return location.getDimension() == getDimension();
    }

    public static class Factory implements ILocationFactory<StaticLocation, NBTTagCompound> {

        ////// --------------------------- NBTConstants --------------------------- \\\\\\
        private static final String NBT_POS = "Pos";
        private static final String NBT_DIM = "Dim";
        ////// --------------------------- NBTConstants --------------------------- \\\\\\

        /**
         * Method to get the location type this factory can produce.
         *
         * @return The type of location this factory can produce.
         */
        @NotNull
        @Override
        public Class<? extends StaticLocation> getFactoryProductionType() {
            return StaticLocation.class;
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
        public NBTTagCompound serializeLocation(@NotNull IRequestManager manager, @NotNull StaticLocation location) {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setLong(NBT_POS, location.getLocation().toLong());
            compound.setInteger(NBT_DIM, location.getDimension());
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
        public StaticLocation deserializeLocation(@NotNull IRequestManager manager, @NotNull NBTTagCompound nbt) {
            BlockPos pos = BlockPos.fromLong(nbt.getLong(NBT_POS));
            Integer dim = nbt.getInteger(NBT_DIM);
            return new StaticLocation(pos, dim);
        }
    }
}
