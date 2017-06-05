package com.minecolonies.api.colony.workorder;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

/**
 * ----------------------- Not Documented Object ---------------------
 */
public interface IWorkOrder
{
    void readFromNBT(@NotNull NBTTagCompound compound);

    int getPriority();

    void setPriority(int priority);

    boolean hasChanged();

    void resetChange();

    int getID();

    void setID(int id);

    boolean isClaimed();

    boolean isClaimedBy(@NotNull ICitizenData citizen);

    int getClaimedBy();

    void clearClaimedBy();

    void writeToNBT(@NotNull NBTTagCompound compound);

    boolean isValid(IColony colony);

    /**
     * Attempt to fulfill the Work Order.
     * Override this with an implementation for the Work Order to find a Citizen to perform the job
     *
     * @param colony The colony that owns the Work Order
     */
    void attemptToFulfill(IColony colony);

    void serializeViewNetworkData(@NotNull ByteBuf buf);
}
