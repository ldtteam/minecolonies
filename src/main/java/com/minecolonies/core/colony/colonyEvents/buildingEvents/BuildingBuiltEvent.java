package com.minecolonies.core.colony.colonyEvents.buildingEvents;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Event handling a new building being built.
 */
public class BuildingBuiltEvent extends AbstractBuildingEvent
{

    /**
     * This events id, registry entries use res locations as ids.
     */
    public static final ResourceLocation BUILDING_BUILT_EVENT_ID = new ResourceLocation(Constants.MOD_ID, "building_built");

    /**
     * Creates a new building built event.
     */
    public BuildingBuiltEvent()
    {
        super();
    }

    /**
     * Creates a new building built event.
     * 
     * @param eventPos      the position of the hut block of the building.
     * @param buildingName  the name of the building.
     */
    public BuildingBuiltEvent(BlockPos eventPos, String buildingName)
    {
        super(eventPos, buildingName, 1);
    }

    @Override
    public ResourceLocation getEventTypeId()
    {
        return BUILDING_BUILT_EVENT_ID;
    }

    @Override
    public String getName()
    {
        return "Building Built";
    }

    /**
     * Loads the citizen born event from the given nbt.
     *
     * @param compound the NBT compound
     * @return the colony to load.
     */
    public static BuildingBuiltEvent loadFromNBT(@NotNull final CompoundTag compound)
    {
        final BuildingBuiltEvent buildEvent = new BuildingBuiltEvent();
        buildEvent.deserializeNBT(compound);
        return buildEvent;
    }

    /**
     * Loads the citizen born event from the given packet buffer.
     *
     * @param buf the packet buffer.
     * @return the colony to load.
     */
    public static BuildingBuiltEvent loadFromFriendlyByteBuf(@NotNull final FriendlyByteBuf buf)
    {
        final BuildingBuiltEvent buildEvent = new BuildingBuiltEvent();
        buildEvent.deserialize(buf);
        return buildEvent;
    }
}
