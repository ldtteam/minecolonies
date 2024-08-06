package com.minecolonies.core.colony.eventhooks.citizenEvents;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * The event handling a newly spawned(not born) citizen.
 */
public class CitizenSpawnedEvent extends AbstractCitizenEvent
{

    /**
     * This events id, registry entries use res locations as ids.
     */
    public static final ResourceLocation CITIZEN_SPAWNED_EVENT_ID = new ResourceLocation(Constants.MOD_ID, "citizen_spawn");

    /**
     * Creates a new citizen spawned event.
     */
    public CitizenSpawnedEvent()
    {
        super();
    }

    /**
     * Creates a new citizen spawned event.
     * 
     * @param eventPos    the position of the hut block of the building.
     * @param citizenName the name of the building.
     */
    public CitizenSpawnedEvent(BlockPos eventPos, String citizenName)
    {
        super(eventPos, citizenName);
    }

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
     * Loads the citizen spawned event from the given nbt.
     *
     * @param compound the NBT compound
     * @return the colony to load.
     */
    public static CitizenSpawnedEvent loadFromNBT(@NotNull final HolderLookup.Provider provider, @NotNull final CompoundTag compound)
    {
        final CitizenSpawnedEvent spawnEvent = new CitizenSpawnedEvent();
        spawnEvent.deserializeNBT(provider, compound);
        return spawnEvent;
    }

    /**
     * Loads the citizen spawned event from the given packet buffer.
     *
     * @param buf the packet buffer.
     * @return the colony to load.
     */
    public static CitizenSpawnedEvent loadFromFriendlyByteBuf(@NotNull final RegistryFriendlyByteBuf buf)
    {
        final CitizenSpawnedEvent spawnEvent = new CitizenSpawnedEvent();
        spawnEvent.deserialize(buf);
        return spawnEvent;
    }
}
