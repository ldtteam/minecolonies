package com.minecolonies.coremod.colony.colonyEvents.buildingEvents;

import org.jetbrains.annotations.NotNull;

import com.minecolonies.api.util.constant.Constants;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

/**
 * The event handling a building being deconstructed.
 */
public class BuildingDeconstructedEvent extends AbstractBuildingEvent
{

    /**
     * This events id, registry entries use res locations as ids.
     */
    public static final ResourceLocation BUILDING_DECONSTRUCTED_EVENT_ID = new ResourceLocation(Constants.MOD_ID, "building_deconstructed");

    @Override
    public ResourceLocation getEventTypeId()
    {
        return BUILDING_DECONSTRUCTED_EVENT_ID;
    }

    @Override
    public String getName()
    {
        return "Building Deconstructed";
    }

    /**
     * Loads the citizen born event from the given nbt.
     *
     * @param compound the NBT compound
     * @return the colony to load.
     */
    public static BuildingDeconstructedEvent loadFromNBT(@NotNull final CompoundNBT compound)
    {
        final BuildingDeconstructedEvent deconstructionEvent = new BuildingDeconstructedEvent();
        deconstructionEvent.readFromNBT(compound);
        return deconstructionEvent;
    }

    /**
     * Loads the citizen born event from the given packet buffer.
     *
     * @param compound the packet buffer.
     * @return the colony to load.
     */
    public static BuildingDeconstructedEvent loadFromPacketBuffer(@NotNull final PacketBuffer buf)
    {
        final BuildingDeconstructedEvent deconstructionEvent = new BuildingDeconstructedEvent();
        deconstructionEvent.deserialize(buf);
        return deconstructionEvent;
    }
}
