package com.minecolonies.api.colony;

import com.minecolonies.api.IMinecoloniesAPI;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

/**
 * Manages access to citizen data types.
 */
public interface ICitizenDataManager
{

    static ICitizenDataManager getInstance()
    {
        return IMinecoloniesAPI.getInstance().getCitizenDataManager();
    }

    /**
     * Creates a citizen data instance from the stored nbt.
     *
     * @param compound The nbt data to create an instance from.
     * @param colony The colony to create an instance in.
     * @return The citizen data, loaded from the nbt into the colony.
     */
    ICitizenData createFromNBT(@NotNull CompoundNBT compound, IColony colony);

    /**
     * Creates a citizen data view from a given network buffer, containing the views data.
     *
     * @param id the id of the citizen.
     * @param networkBuffer The network buffer to read from.
     * @param colonyView the colony the citizen belongs to.
     * @return The citizen data view.
     */
    ICitizenDataView createFromNetworkData(@NotNull final int id, @NotNull final PacketBuffer networkBuffer, final IColonyView colonyView);
}
