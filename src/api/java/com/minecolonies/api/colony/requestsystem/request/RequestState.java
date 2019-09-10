package com.minecolonies.api.colony.requestsystem.request;

import net.minecraft.nbt.NBTTagInt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Enum used to describe the state of a Request.
 */
public enum RequestState
{
    /**
     * Default state for a not registered request.
     */
    CREATED,

    /**
     * State for a request that has been registered, yet not resolved.
     */
    REPORTED,

    /**
     * State for a request that is being assigned.
     */
    ASSIGNING,

    /**
     * State for a request that has been assigned, yet it has not been started.
     */
    ASSIGNED,

    /**
     * State for a request on which is being worked.
     */
    IN_PROGRESS,

    /**
     * States for a request who's primary processing has been completed.
     * Followup processing still needs to be determined.
     */
    RESOLVED,

    /**
     * State for a requests, who's resolving has been completed, but who's followups are still in processing stages.
     */
    FOLLOWUP_IN_PROGRESS,

    /**
     * State for a request that has been completed.
     */
    COMPLETED,

    /**
     * State for a request when it has been forcefully fullfilled by a player
     */
    OVERRULED,

    /**
     * State for a request that has been cancelled.
     */
    CANCELLED,

    /**
     * State used to indicate that this request has been received by the requester.
     */
    RECEIVED,

    /**
     * State used to indicate that a request is cancelled overruled, yet processing is still continuing to ensure proper cleanup.
     */
    FINALIZING;

    /**
     * Index list used to read and write from NBT
     */
    static final List<RequestState> indexList = new ArrayList<>();
    static
    {
        /*
         * This should never be changed! It is used to read and write from NBT so it has to
         * persist between mod versions.
         */
        Collections.addAll(indexList, RequestState.values());
    }
    /**
     * This is an empty constructor, i don't know why, Orion probably does =D.
     */
    RequestState()
    {
    }

    /**
     * Method used to deserialize a RequestState from NBT
     *
     * @param nbt The nbt to deserialize from.
     * @return The RequestState that is stored in the given NBT.
     */
    public static RequestState deserializeNBT(final NBTTagInt nbt)
    {
        return indexList.get(nbt.getInt());
    }

    /**
     * Method used to serialize a state to NBT.
     *
     * @return The NBT representation of the state.
     */
    public NBTTagInt serializeNBT()
    {
        return new NBTTagInt(indexList.indexOf(this));
    }


}
