package com.minecolonies.coremod.colony.management.requestsystem.api;

import net.minecraft.nbt.NBTTagInt;

import java.util.ArrayList;

/**
 * Enum used to describe the state of a Request.
 */
public enum RequestState {
    CREATED,
    REPORTED,
    RESOLVED,
    COMPLETED,
    FAILED;

    /**
     * Index list used to read and write from NBT
     */
    static ArrayList<RequestState> indexList = new ArrayList<>();

    static {
        /**
         * This should never be changed! It is used to read and write from NBT so it has to
         * persist between mod versions.
         */
        indexList.add(CREATED);
        indexList.add(REPORTED);
        indexList.add(RESOLVED);
        indexList.add(COMPLETED);
        indexList.add(FAILED);
    }

    RequestState() {
    }

    /**
     * Method used to serialize a state to NBT.
     * @return The NBT representation of the state.
     */
    public NBTTagInt serializeNBT() {
        return new NBTTagInt(indexList.indexOf(this));
    }

    /**
     * Method used to deserialize a RequestState from NBT
     * @param nbt The nbt to deserialize from.
     * @return The RequestState that is stored in the given NBT.
     */
    public static RequestState deserializeNBT(NBTTagInt nbt) {
        return indexList.get(nbt.getInt());
    }


}
