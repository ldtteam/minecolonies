package com.minecolonies.core.colony.eventhooks;

import com.minecolonies.api.colony.colonyEvents.descriptions.IColonyEventDescription;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * The abstract event handling building/upgrading huts.
 */
public abstract class AbstractEvent implements IColonyEventDescription
{
    private boolean includeInSummary;
    private int day;

    /**
     * Creates a new building event.
     */
    public AbstractEvent()
    {
    }

    /**
     * Creates a new abstract event.
     * @param includeInSummary
     */
    public AbstractEvent(final boolean includeInSummary)
    {
        this.includeInSummary = includeInSummary;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag compound = new CompoundTag();
        compound.putInt(TAG_DAY, day);
        compound.putBoolean(TAG_SUMMARIZE, includeInSummary);
        return compound;
    }

    @Override
    public void deserializeNBT(CompoundTag compound)
    {
        day = compound.getInt(TAG_DAY);
        includeInSummary = compound.getBoolean(TAG_SUMMARIZE);
    }

    @Override
    public void serialize(FriendlyByteBuf buf)
    {
        buf.writeInt(day);
        buf.writeBoolean(includeInSummary);
    }

    @Override
    public void deserialize(FriendlyByteBuf buf)
    {
        day = buf.readInt();
        includeInSummary = buf.readBoolean();
    }

    @Override
    public boolean includeInSummary()
    {
        return includeInSummary;
    }

    @Override
    public int getDay()
    {
        return day;
    }

    @Override
    public void setDay(final int day)
    {
        this.day = day;
    }

    @Override
    public String toString()
    {
        return toDisplayString();
    }
}
