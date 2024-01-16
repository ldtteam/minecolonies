package com.minecolonies.core.colony.colonyEvents.citizenEvents;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * The event handling a newly spawned visitor.
 */
public class VisitorSpawnedEvent extends AbstractCitizenEvent
{

    /**
     * This events id, registry entries use res locations as ids.
     */
    public static final ResourceLocation VISITOR_SPAWNED_EVENT_ID = new ResourceLocation(Constants.MOD_ID, "visitor_spawn");

    /**
     * Creates a new visitor spawned event.
     */
    public VisitorSpawnedEvent()
    {
        super();
    }

    /**
     * Creates a new visitor spawned event.
     * 
     * @param eventPos    the position of the hut block of the building.
     * @param citizenName the name of the building.
     */
    public VisitorSpawnedEvent(BlockPos eventPos, String citizenName)
    {
        super(eventPos, citizenName);
    }

    @Override
    public ResourceLocation getEventTypeId()
    {
        return VISITOR_SPAWNED_EVENT_ID;
    }

    @Override
    public String getName()
    {
        return "Visitor Spawned";
    }

    /**
     * Loads the visitor spawned event from the given nbt.
     *
     * @param compound the NBT compound
     * @return the colony to load.
     */
    public static VisitorSpawnedEvent loadFromNBT(@NotNull final CompoundTag compound)
    {
        final VisitorSpawnedEvent spawnEvent = new VisitorSpawnedEvent();
        spawnEvent.deserializeNBT(compound);
        return spawnEvent;
    }

    /**
     * Loads the visitor spawned event from the given packet buffer.
     *
     * @param buf the packet buffer.
     * @return the colony to load.
     */
    public static VisitorSpawnedEvent loadFromFriendlyByteBuf(@NotNull final FriendlyByteBuf buf)
    {
        final VisitorSpawnedEvent spawnEvent = new VisitorSpawnedEvent();
        spawnEvent.deserialize(buf);
        return spawnEvent;
    }
}
