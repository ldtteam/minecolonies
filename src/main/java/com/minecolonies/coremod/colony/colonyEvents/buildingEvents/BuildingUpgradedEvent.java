package com.minecolonies.coremod.colony.colonyEvents.buildingEvents;

import org.jetbrains.annotations.NotNull;

import com.minecolonies.api.util.constant.Constants;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

/**
 * Event handling a building being upgraded.
 */
public class BuildingUpgradedEvent extends AbstractBuildingEvent
{

    /**
     * This events id, registry entries use res locations as ids.
     */
    public static final ResourceLocation BUILDING_UPGRADED_EVENT_ID = new ResourceLocation(Constants.MOD_ID, "building_upgraded");

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
    public static BuildingUpgradedEvent loadFromNBT(@NotNull final CompoundNBT compound)
    {
        final BuildingUpgradedEvent upgradeEvent = new BuildingUpgradedEvent();
        upgradeEvent.readFromNBT(compound);
        return upgradeEvent;
    }

    /**
     * Loads the citizen born event from the given packet buffer.
     *
     * @param compound the packet buffer.
     * @return the colony to load.
     */
    public static BuildingUpgradedEvent loadFromPacketBuffer(@NotNull final PacketBuffer buf)
    {
        final BuildingUpgradedEvent upgradeEvent = new BuildingUpgradedEvent();
        upgradeEvent.deserialize(buf);
        return upgradeEvent;
    }
}
