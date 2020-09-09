package com.minecolonies.coremod.colony.colonyEvents.buildingEvents;

import static com.minecolonies.api.colony.colonyEvents.descriptions.NBTTags.*;

import com.minecolonies.api.colony.colonyEvents.descriptions.IBuildingEventDescription;
import com.minecolonies.api.util.BlockPosUtil;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

/**
 * The abstract event handling building/upgrading huts.
 */
public abstract class AbstractBuildingEvent implements IBuildingEventDescription
{

    private BlockPos eventPos;
    private String building;
    private int level;

    @Override
    public BlockPos getEventPos()
    {
        return eventPos;
    }

    @Override
    public void setEventPos(BlockPos pos)
    {
        eventPos = pos;
    }

    @Override
    public CompoundNBT writeToNBT(CompoundNBT compound)
    {
        BlockPosUtil.write(compound, TAG_EVENT_POS, eventPos);
        compound.putString(TAG_BUILDING_NAME, building);
        compound.putInt(TAG_BUILDING_LEVEL, level);
        return compound;
    }

    @Override
    public void readFromNBT(CompoundNBT compound)
    {
        eventPos = BlockPosUtil.read(compound, TAG_EVENT_POS);
        building = compound.getString(TAG_BUILDING_NAME);
        level = compound.getInt(TAG_BUILDING_LEVEL);
    }

    @Override
    public void serialize(PacketBuffer buf)
    {
        buf.writeBlockPos(eventPos);
        buf.writeString(building);
        buf.writeInt(level);
    }

    @Override
    public void deserialize(PacketBuffer buf)
    {
        eventPos = buf.readBlockPos();
        building = buf.readString();
        level = buf.readInt();
    }

    @Override
    public String getBuilding()
    {
        return building;
    }

    @Override
    public void setBuilding(String building)
    {
        this.building = building;
    }

    @Override
    public int getLevel()
    {
        return level;
    }

    @Override
    public void setLevel(int lvl)
    {
        level = lvl;
    }

    @Override
    public String toString()
    {
        return toDisplayString();
    }
}
