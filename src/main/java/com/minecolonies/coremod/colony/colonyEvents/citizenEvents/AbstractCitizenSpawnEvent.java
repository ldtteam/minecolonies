package com.minecolonies.coremod.colony.colonyEvents.citizenEvents;

import org.jetbrains.annotations.NotNull;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.colonyEvents.EventStatus;
import com.minecolonies.api.colony.colonyEvents.IColonyEntitySpawnEvent;
import com.minecolonies.api.util.BlockPosUtil;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import static com.minecolonies.api.colony.colonyEvents.NBTTags.*;

/**
 * Event for spawning a new citizen, or recruitable citizen.
 */
public abstract class AbstractCitizenSpawnEvent implements IColonyEntitySpawnEvent
{

	private int id;
	private IColony colony;
	private BlockPos spawnPos;
	private EventStatus status = EventStatus.DONE;

	/**
	 * Creates a new Event.
	 * 
	 * @param colony the colony in which the citizen spawned.
	 * @param spawnPos the position in which the citizen spawned.
	 */
	public AbstractCitizenSpawnEvent(@NotNull final IColony colony, @NotNull final BlockPos spawnPos)
    {
        this.colony = colony;
        id = colony.getEventManager().getAndTakeNextEventID();
        this.spawnPos = spawnPos;
    }

	@Override
	public int getID()
	{
		return id;
	}

	@Override
	public BlockPos getSpawnPos()
	{
		return spawnPos;
	}

	@Override
	public void setSpawnPoint(BlockPos spawnPoint)
	{
		spawnPos = spawnPoint;
	}

	@Override
	public void setColony(IColony colony)
	{
		this.colony = colony;
	}

	@Override
	public EventStatus getStatus()
	{
		return status;
	}

	@Override
	public void setStatus(EventStatus status)
	{
		this.status = status;
	}

	@Override
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		compound.putInt(TAG_EVENT_ID, id);
        BlockPosUtil.write(compound, TAG_SPAWN_POS, spawnPos);
		compound.putInt(TAG_EVENT_STATUS, status.ordinal());
		return compound;
	}

	@Override
	public void readFromNBT(CompoundNBT compound)
	{
		id = compound.getInt(TAG_EVENT_ID);
		spawnPos = BlockPosUtil.read(compound, TAG_SPAWN_POS);
		status = EventStatus.values()[compound.getInt(TAG_EVENT_STATUS)];
	}

	@Override
	public void serialize(PacketBuffer buf) {
		buf.writeInt(id);
		buf.writeBlockPos(spawnPos);
		buf.writeInt(status.ordinal());
	}

	@Override
	public void deserialize(PacketBuffer buf) {
		id = buf.readInt();
		spawnPos = buf.readBlockPos();
		status = EventStatus.values()[buf.readInt()];
	}

}
