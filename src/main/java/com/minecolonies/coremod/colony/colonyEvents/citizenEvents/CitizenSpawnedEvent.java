package com.minecolonies.coremod.colony.colonyEvents.citizenEvents;

import org.jetbrains.annotations.NotNull;

import com.minecolonies.api.util.constant.Constants;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

/**
 * The event handling a newly spawned(not born) citizen.
 */
public class CitizenSpawnedEvent extends AbstractCitizenEvent
{

    /**
     * This events id, registry entries use res locations as ids.
     */
    public static final ResourceLocation CITIZEN_SPAWNED_EVENT_ID = new ResourceLocation(Constants.MOD_ID, "citizen_spawn");

    @Override
    public ResourceLocation getEventTypeId()
    {
        return CITIZEN_SPAWNED_EVENT_ID;
    }

    @Override
    public String getName()
    {
        return "Citizen Spawned";
    }

    /**
     * Loads the citizen born event from the given nbt.
     *
     * @param compound the NBT compound
     * @return the colony to load.
     */
    public static CitizenSpawnedEvent loadFromNBT(@NotNull final CompoundNBT compound)
    {
        final CitizenSpawnedEvent spawnEvent = new CitizenSpawnedEvent();
        spawnEvent.readFromNBT(compound);
        return spawnEvent;
    }

    /**
     * Loads the citizen born event from the given packet buffer.
     *
     * @param compound the packet buffer.
     * @return the colony to load.
     */
    public static CitizenSpawnedEvent loadFromPacketBuffer(@NotNull final PacketBuffer buf)
    {
        final CitizenSpawnedEvent spawnEvent = new CitizenSpawnedEvent();
        spawnEvent.deserialize(buf);
        return spawnEvent;
    }
}
