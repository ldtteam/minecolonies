package com.minecolonies.core.colony.eventhooks.buildingEvents;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Event handling a new building being built.
 */
public class BuildingRepairedEvent extends AbstractBuildingEvent
{

    /**
     * This events id, registry entries use res locations as ids.
     */
    public static final ResourceLocation BUILDING_REPAIRED_EVENT_ID = new ResourceLocation(Constants.MOD_ID, "building_repaired");

    /**
     * Creates a new building repaired event.
     */
    public BuildingRepairedEvent()
    {
        super();
    }

    /**
     * Creates a new building repaired event.
     *
     * @param eventPos      the position of the hut block of the building.
     * @param buildingName  the name of the building.
     * @param level         the level of the repaired building
     */
    public BuildingRepairedEvent(BlockPos eventPos, String buildingName, int level)
    {
        super(eventPos, buildingName, level);
    }

    @Override
    public ResourceLocation getEventTypeId()
    {
        return BUILDING_REPAIRED_EVENT_ID;
    }

    @Override
    public String getName()
    {
        return "Building Repaired";
    }

    /**
     * Loads the citizen born event from the given nbt.
     *
     * @param compound the NBT compound
     * @return the colony to load.
     */
    public static BuildingRepairedEvent loadFromNBT(@NotNull final CompoundTag compound)
    {
        final BuildingRepairedEvent buildEvent = new BuildingRepairedEvent();
        buildEvent.deserializeNBT(compound);
        return buildEvent;
    }

    /**
     * Loads the citizen born event from the given packet buffer.
     *
     * @param buf the packet buffer.
     * @return the colony to load.
     */
    public static BuildingRepairedEvent loadFromFriendlyByteBuf(@NotNull final RegistryFriendlyByteBuf buf)
    {
        final BuildingRepairedEvent buildEvent = new BuildingRepairedEvent();
        buildEvent.deserialize(buf);
        return buildEvent;
    }
}
