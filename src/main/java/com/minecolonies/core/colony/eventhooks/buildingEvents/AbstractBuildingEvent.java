package com.minecolonies.core.colony.eventhooks.buildingEvents;

import com.minecolonies.api.colony.colonyEvents.descriptions.IBuildingEventDescription;
import com.minecolonies.api.util.BlockPosUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_BUILDING_LEVEL;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_BUILDING_NAME;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_EVENT_POS;

/**
 * The abstract event handling building/upgrading huts.
 */
public abstract class AbstractBuildingEvent implements IBuildingEventDescription
{

    private BlockPos eventPos;
    private String buildingName;
    private int level;

    /**
     * Creates a new building event.
     */
    public AbstractBuildingEvent()
    {
    }

    /**
     * Creates a new building event.
     * 
     * @param eventPos      the position of the hut block of the building.
     * @param buildingName  the name of the building.
     * @param buildingLevel the level of the building after this event.
     */
    public AbstractBuildingEvent(BlockPos eventPos, String buildingName, int buildingLevel)
    {
        this.eventPos = eventPos;
        this.buildingName = buildingName;
        level = buildingLevel;
    }

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
    public CompoundTag serializeNBT()
    {
        CompoundTag compound = new CompoundTag();
        BlockPosUtil.write(compound, TAG_EVENT_POS, eventPos);
        compound.putString(TAG_BUILDING_NAME, buildingName);
        compound.putInt(TAG_BUILDING_LEVEL, level);
        return compound;
    }

    @Override
    public void deserializeNBT(CompoundTag compound)
    {
        eventPos = BlockPosUtil.read(compound, TAG_EVENT_POS);
        buildingName = compound.getString(TAG_BUILDING_NAME);
        level = compound.getInt(TAG_BUILDING_LEVEL);
    }

    @Override
    public void serialize(FriendlyByteBuf buf)
    {
        buf.writeBlockPos(eventPos);
        buf.writeUtf(buildingName);
        buf.writeInt(level);
    }

    @Override
    public void deserialize(FriendlyByteBuf buf)
    {
        eventPos = buf.readBlockPos();
        buildingName = buf.readUtf();
        level = buf.readInt();
    }

    @Override
    public String getBuildingName()
    {
        return buildingName;
    }

    @Override
    public void setBuildingName(String buildingName)
    {
        this.buildingName = buildingName;
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
