package com.minecolonies.coremod.colony.colonyEvents.citizenEvents;

import static com.minecolonies.api.colony.colonyEvents.descriptions.NBTTags.*;

import com.minecolonies.api.colony.colonyEvents.descriptions.ICitizenEventDescription;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.constant.Constants;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;

/**
 * The event for handling a citizen death.
 */
public class CitizenDiedEvent implements ICitizenEventDescription
{

	/**
	 * This events id, registry entries use res locations as ids.
	 */
	public static final ResourceLocation CITIZEN_DIED_EVENT_ID = new ResourceLocation(Constants.MOD_ID, "citizen_died");

	private static final String TAG_DEATH_CAUSE = "deathCause";

	private BlockPos eventPos;

	private String citizenName;

	private String deathCause;

	/**
	 * Creates a new event.
	 */
	public CitizenDiedEvent()
	{
	}

	@Override
	public ResourceLocation getEventTypeId()
	{
		return CITIZEN_DIED_EVENT_ID;
	}

	@Override
	public String getName()
	{
		return "Citizen Died";
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
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
        BlockPosUtil.write(compound, TAG_EVENT_POS, eventPos);
        compound.putString(TAG_CITIZEN_NAME, citizenName);
        compound.putString(TAG_DEATH_CAUSE, deathCause);
		return compound;
	}

	@Override
	public void readFromNBT(CompoundNBT compound)
	{
		eventPos = BlockPosUtil.read(compound, TAG_EVENT_POS);
		citizenName = compound.getString(TAG_CITIZEN_NAME);
		deathCause = compound.getString(TAG_DEATH_CAUSE);
	}

	@Override
	public void serialize(PacketBuffer buf)
	{
		buf.writeBlockPos(eventPos);
		buf.writeString(citizenName);
		buf.writeString(deathCause);
	}

	@Override
	public void deserialize(PacketBuffer buf)
	{
		eventPos = buf.readBlockPos();
		citizenName = buf.readString();
		deathCause = buf.readString();
	}

	@Override
	public String getCitizenName()
	{
		return citizenName;
	}

	@Override
	public void setCitizenName(String name)
	{
		citizenName = name;
	}

	/**
	 * Gets the cause of the citizen death.
	 * 
	 * @return the cause of the citizen death.
	 */
	public String getDeathCause()
	{
		return deathCause;
	}

	/**
	 * Sets the cause of the citizen death.
	 * 
	 * @param deathCause the cause of the citizen death.
	 */
	public void setDeathCause(String deathCause)
	{
		this.deathCause = deathCause;
	}

	@Override
	public String toString()
	{
		return toDisplayString();
	}

	/**
     * Loads the citizen died event from the given nbt.
     *
     * @param compound the NBT compound
     * @return the colony to load.
     */
    public static CitizenDiedEvent loadFromNBT(@NotNull final CompoundNBT compound)
    {
        final CitizenDiedEvent deathEvent = new CitizenDiedEvent();
        deathEvent.readFromNBT(compound);
        return deathEvent;
    }

    /**
     * Loads the citizen died event from the given packet buffer.
     *
     * @param compound the packet buffer.
     * @return the colony to load.
     */
    public static CitizenDiedEvent loadFromPacketBuffer(@NotNull final PacketBuffer buf)
    {
    	final CitizenDiedEvent deathEvent = new CitizenDiedEvent();
    	deathEvent.deserialize(buf);
    	return deathEvent;
    }
}
