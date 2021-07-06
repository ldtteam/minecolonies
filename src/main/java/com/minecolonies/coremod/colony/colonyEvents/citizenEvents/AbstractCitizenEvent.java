package com.minecolonies.coremod.colony.colonyEvents.citizenEvents;

import com.minecolonies.api.colony.colonyEvents.descriptions.ICitizenEventDescription;
import com.minecolonies.api.util.BlockPosUtil;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Event for something happening to a citizen.
 */
public abstract class AbstractCitizenEvent implements ICitizenEventDescription
{

    private BlockPos eventPos;
    private String citizenName;

    /**
     * Creates a new citizen event.
     */
    public AbstractCitizenEvent()
    {
    }

    /**
     * Creates a new citizen event.
     * 
     * @param eventPos    the position of the hut block of the building.
     * @param citizenName the name of the building.
     */
    public AbstractCitizenEvent(BlockPos eventPos, String citizenName)
    {
        this.eventPos = eventPos;
        this.citizenName = citizenName;
    }

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
    public CompoundNBT serializeNBT()
    {
        CompoundNBT compound = new CompoundNBT();
        BlockPosUtil.write(compound, TAG_EVENT_POS, eventPos);
        compound.putString(TAG_CITIZEN_NAME, citizenName);
        return compound;
    }

    @Override
    public void deserializeNBT(CompoundNBT compound)
    {
        eventPos = BlockPosUtil.read(compound, TAG_EVENT_POS);
        citizenName = compound.getString(TAG_CITIZEN_NAME);
    }

    @Override
    public void serialize(PacketBuffer buf)
    {
        buf.writeBlockPos(eventPos);
        buf.writeUtf(citizenName);
    }

    @Override
    public void deserialize(PacketBuffer buf)
    {
        eventPos = buf.readBlockPos();
        citizenName = buf.readUtf();
    }

    @Override
    public String toString()
    {
        return toDisplayString();
    }
}
