package com.minecolonies.core.colony.eventhooks.buildingEvents;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * The event handling a building being deconstructed.
 */
public class BuildingDeconstructedEvent extends AbstractBuildingEvent
{

    /**
     * This events id, registry entries use res locations as ids.
     */
    public static final ResourceLocation BUILDING_DECONSTRUCTED_EVENT_ID = new ResourceLocation(Constants.MOD_ID, "building_deconstructed");

    /**
     * Creates a new building deconstructed event.
     */
    public BuildingDeconstructedEvent()
    {
        super();
    }

    /**
     * Creates a new building deconstructed event.
     * 
     * @param eventPos      the position of the hut block of the building.
     * @param buildingName  the name of the building.
     * @param buildingLevel the level of the building before this event.
     */
    public BuildingDeconstructedEvent(BlockPos eventPos, String buildingName, int buildingLevel)
    {
        super(eventPos, buildingName, buildingLevel);
    }

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
    public static BuildingDeconstructedEvent loadFromNBT(@NotNull final CompoundTag compound)
    {
        final BuildingDeconstructedEvent deconstructionEvent = new BuildingDeconstructedEvent();
        deconstructionEvent.deserializeNBT(compound);
        return deconstructionEvent;
    }

    /**
     * Loads the citizen born event from the given packet buffer.
     *
     * @param buf the packet buffer.
     * @return the colony to load.
     */
    public static BuildingDeconstructedEvent loadFromFriendlyByteBuf(@NotNull final FriendlyByteBuf buf)
    {
        final BuildingDeconstructedEvent deconstructionEvent = new BuildingDeconstructedEvent();
        deconstructionEvent.deserialize(buf);
        return deconstructionEvent;
    }
}
