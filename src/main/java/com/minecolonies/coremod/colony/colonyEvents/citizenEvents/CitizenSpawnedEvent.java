package com.minecolonies.coremod.colony.colonyEvents.citizenEvents;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.managers.EventManager;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

/**
 * The event handling a newly spawned(not born) citizen.
 */
public class CitizenSpawnedEvent extends AbstractCitizenSpawnEvent
{

	/**
     * This events id, registry entries use res locations as ids.
     */
	public static final ResourceLocation CITIZEN_SPAWNED_EVENT_ID = new ResourceLocation(Constants.MOD_ID, "citizen_spawn");

	static
	{
		EventManager.registerEventDeserializer(CITIZEN_SPAWNED_EVENT_ID, (colony, buf) -> {
			CitizenSpawnedEvent event = new CitizenSpawnedEvent(colony, null);
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
	public CitizenSpawnedEvent(IColony colony, BlockPos spawnPos)
	{
		super(colony, spawnPos);
	}

	@Override
	public ResourceLocation getEventTypeID()
	{
		return CITIZEN_SPAWNED_EVENT_ID;
	}

	@Override
	public String getName()
	{
		return "Citizen Spawned";
	}

}
