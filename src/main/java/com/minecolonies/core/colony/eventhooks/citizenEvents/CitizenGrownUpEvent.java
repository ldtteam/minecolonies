package com.minecolonies.core.colony.eventhooks.citizenEvents;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * The event for handling a citizen growing up.
 */
public class CitizenGrownUpEvent extends AbstractCitizenEvent
{

    /**
     * This events id, registry entries use res locations as ids.
     */
    public static final ResourceLocation CITIZEN_GROWN_UP_EVENT_ID = new ResourceLocation(Constants.MOD_ID, "citizen_grown_up");

    /**
     * Creates a new citizen grown up event.
     */
    public CitizenGrownUpEvent()
    {
        super();
    }

    /**
     * Creates a new citizen grown up event.
     * 
     * @param eventPos    the position of the hut block of the building.
     * @param citizenName the name of the building.
     */
    public CitizenGrownUpEvent(BlockPos eventPos, String citizenName)
    {
        super(eventPos, citizenName);
    }

    @Override
    public ResourceLocation getEventTypeId()
    {
        return CITIZEN_GROWN_UP_EVENT_ID;
    }

    @Override
    public String getName()
    {
        return "Citizen Grew Up";
    }

    /**
     * Loads the citizen grown up event from the given nbt.
     *
     * @param compound the NBT compound
     * @return the colony to load.
     */
    public static CitizenGrownUpEvent loadFromNBT(@NotNull final CompoundTag compound, @NotNull final HolderLookup.Provider provider)
    {
        final CitizenGrownUpEvent growUpEvent = new CitizenGrownUpEvent();
        growUpEvent.deserializeNBT(provider, compound);
        return growUpEvent;
    }

    /**
     * Loads the citizen grown up event from the given packet buffer.
     *
     * @param buf the packet buffer.
     * @return the colony to load.
     */
    public static CitizenGrownUpEvent loadFromFriendlyByteBuf(@NotNull final RegistryFriendlyByteBuf buf)
    {
        final CitizenGrownUpEvent growUpEvent = new CitizenGrownUpEvent();
        growUpEvent.deserialize(buf);
        return growUpEvent;
    }
}
