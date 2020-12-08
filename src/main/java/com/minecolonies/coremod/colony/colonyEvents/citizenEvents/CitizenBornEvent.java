package com.minecolonies.coremod.colony.colonyEvents.citizenEvents;

import org.jetbrains.annotations.NotNull;

import com.minecolonies.api.util.constant.Constants;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

/**
 * The event for handling a newly born citizen.
 */
public class CitizenBornEvent extends AbstractCitizenEvent
{

    /**
     * This events id, registry entries use res locations as ids.
     */
    public static final ResourceLocation CITIZEN_BORN_EVENT_ID = new ResourceLocation(Constants.MOD_ID, "citizen_born");

    /**
     * Creates a new citizen born event.
     */
    public CitizenBornEvent()
    {
        super();
    }

    /**
     * Creates a new citizen born event.
     * 
     * @param eventPos    the position of the hut block of the building.
     * @param citizenName the name of the building.
     */
    public CitizenBornEvent(BlockPos eventPos, String citizenName)
    {
        super(eventPos, citizenName);
    }

    @Override
    public ResourceLocation getEventTypeId()
    {
        return CITIZEN_BORN_EVENT_ID;
    }

    @Override
    public String getName()
    {
        return "Citizen Born";
    }

    /**
     * Loads the citizen born event from the given nbt.
     *
     * @param compound the NBT compound
     * @return the colony to load.
     */
    public static CitizenBornEvent loadFromNBT(@NotNull final CompoundNBT compound)
    {
        final CitizenBornEvent birthEvent = new CitizenBornEvent();
        birthEvent.deserializeNBT(compound);
        return birthEvent;
    }

    /**
     * Loads the citizen born event from the given packet buffer.
     *
     * @param buf the packet buffer.
     * @return the colony to load.
     */
    public static CitizenBornEvent loadFromPacketBuffer(@NotNull final PacketBuffer buf)
    {
        final CitizenBornEvent birthEvent = new CitizenBornEvent();
        birthEvent.deserialize(buf);
        return birthEvent;
    }
}
