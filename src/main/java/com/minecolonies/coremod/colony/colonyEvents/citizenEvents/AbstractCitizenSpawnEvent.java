package com.minecolonies.coremod.colony.colonyEvents.citizenEvents;

import com.minecolonies.api.colony.colonyEvents.descriptions.ICitizenEventDescription;
import com.minecolonies.api.util.BlockPosUtil;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import static com.minecolonies.api.colony.colonyEvents.descriptions.NBTTags.*;

/**
 * Event for spawning a new citizen.
 */
public abstract class AbstractCitizenSpawnEvent implements ICitizenEventDescription
{

    private BlockPos eventPos;
    private String citizenName;

    @Override
    public BlockPos getEventPos()
    {
        return eventPos;
    }

    @Override
    public void setEventPos(BlockPos eventPos)
    {
        this.eventPos = eventPos;
    }

    @Override
    public String getCitizenName()
    {
        return citizenName;
    }

    @Override
    public void setCitizenName(String citizenName)
    {
        this.citizenName = citizenName;
    }

    @Override
    public CompoundNBT writeToNBT(CompoundNBT compound)
    {
        BlockPosUtil.write(compound, TAG_EVENT_POS, eventPos);
        compound.putString(TAG_CITIZEN_NAME, citizenName);
        return compound;
    }

    @Override
    public void readFromNBT(CompoundNBT compound)
    {
        eventPos = BlockPosUtil.read(compound, TAG_EVENT_POS);
        citizenName = compound.getString(TAG_CITIZEN_NAME);
    }

    @Override
    public void serialize(PacketBuffer buf)
    {
        buf.writeBlockPos(eventPos);
        buf.writeString(citizenName);
    }

    @Override
    public void deserialize(PacketBuffer buf)
    {
        eventPos = buf.readBlockPos();
        citizenName = buf.readString();
    }

    @Override
    public String toString()
    {
        return toDisplayString();
    }
}
