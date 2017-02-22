package com.minecolonies.coremod.colony.management.requestsystem.api;

import net.minecraft.nbt.NBTTagInt;

import java.util.ArrayList;

/**
 * Created by marcf on 2/22/2017.
 */
public enum RequestState {
    CREATED,
    REPORTED,
    RESOLVED,
    CHAINED,
    ONROUTE,
    DELIVERED,
    FAILED;

    static ArrayList<RequestState> indexList = new ArrayList<>();

    static {
        indexList.add(CREATED);
        indexList.add(REPORTED);
        indexList.add(RESOLVED);
        indexList.add(CHAINED);
        indexList.add(ONROUTE);
        indexList.add(DELIVERED);
        indexList.add(FAILED);
    }

    RequestState() {
    }

    public NBTTagInt serializeNBT() {
        return new NBTTagInt(indexList.indexOf(this));
    }

    public static RequestState deserializeNBT(NBTTagInt nbt) {
        return indexList.get(nbt.getInt());
    }


}
