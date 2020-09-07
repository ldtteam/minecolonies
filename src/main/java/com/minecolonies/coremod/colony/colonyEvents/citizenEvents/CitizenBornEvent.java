package com.minecolonies.coremod.colony.colonyEvents.citizenEvents;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.managers.EventManager;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

/**
 * The event for handling a newly born citizen.
 */
public class CitizenBornEvent extends AbstractCitizenSpawnEvent
{

	/**
     * This events id, registry entries use res locations as ids.
     */
	public static final ResourceLocation CITIZEN_BORN_EVENT_ID = new ResourceLocation(Constants.MOD_ID, "citizen_born");

	static
	{
		EventManager.registerEventDeserializer(CITIZEN_BORN_EVENT_ID, (colony, buf) -> {
			CitizenBornEvent event = new CitizenBornEvent(colony, null);
			event.deserialize(buf);
			return event;
		});
	}

	/**
	 * Creates a new event.
	 * 
	 * @param colony the colony in which the citizen spawned.
	 * @param spawnPos the position in which the citizen spawned.
	 */
	public CitizenBornEvent(IColony colony, BlockPos spawnPos)
	{
		super(colony, spawnPos);
	}

	@Override
	public ResourceLocation getEventTypeID()
	{
		return CITIZEN_BORN_EVENT_ID;
	}

	@Override
	public String getName()
	{
		return "Citizen Born";
	}

}
