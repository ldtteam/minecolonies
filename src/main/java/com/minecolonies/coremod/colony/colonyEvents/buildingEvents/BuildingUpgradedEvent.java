package com.minecolonies.coremod.colony.colonyEvents.buildingEvents;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Event handling a building being upgraded.
 */
public class BuildingUpgradedEvent extends AbstractBuildingEvent
{

    /**
     * This events id, registry entries use res locations as ids.
     */
    public static final ResourceLocation BUILDING_UPGRADED_EVENT_ID = new ResourceLocation(Constants.MOD_ID, "building_upgraded");

    /**
     * Creates a new building upgraded event.
     */
    public BuildingUpgradedEvent()
    {
        super();
    }

    /**
     * Creates a new building upgraded event.
     * 
     * @param eventPos      the position of the hut block of the building.
     * @param buildingName  the name of the building.
     * @param buildingLevel the level of the building after this event.
     */
    public BuildingUpgradedEvent(BlockPos eventPos, String buildingName, int buildingLevel)
    {
        super(eventPos, buildingName, buildingLevel);
    }

    @Override
    public ResourceLocation getEventTypeId()
    {
        return BUILDING_UPGRADED_EVENT_ID;
    }

    @Override
    public String getName()
    {
        return "Building Upgraded";
    }

    /**
     * Loads the citizen born event from the given nbt.
     *
     * @param compound the NBT compound
     * @return the colony to load.
     */
    public static BuildingUpgradedEvent loadFromNBT(@NotNull final CompoundTag compound)
    {
        final BuildingUpgradedEvent upgradeEvent = new BuildingUpgradedEvent();
        upgradeEvent.deserializeNBT(compound);
        return upgradeEvent;
    }

    /**
     * Loads the citizen born event from the given packet buffer.
     *
     * @param buf the packet buffer.
     * @return the colony to load.
     */
    public static BuildingUpgradedEvent loadFromFriendlyByteBuf(@NotNull final FriendlyByteBuf buf)
    {
        final BuildingUpgradedEvent upgradeEvent = new BuildingUpgradedEvent();
        upgradeEvent.deserialize(buf);
        return upgradeEvent;
    }
}
