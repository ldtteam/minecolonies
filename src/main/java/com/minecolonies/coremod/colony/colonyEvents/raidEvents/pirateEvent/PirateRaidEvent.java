package com.minecolonies.coremod.colony.colonyEvents.raidEvents.pirateEvent;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.colonyEvents.IColonyEvent;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.AbstractShipRaidEvent;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * The Pirate raid event, spawns a ship with pirate spawners onboard.
 */
public class PirateRaidEvent extends AbstractShipRaidEvent
{
    /**
     * This raids event id, registry entries use res locations as ids.
     */
    public static final ResourceLocation PIRATE_RAID_EVENT_TYPE_ID = new ResourceLocation(Constants.MOD_ID, "pirate_raid");

    /**
     * Create a new Pirate raid event.
     * @param colony the colony.
     */
    public PirateRaidEvent(@NotNull final IColony colony)
    {
        super(colony);
    }

    @Override
    public String getShipDesc()
    {
        return "pirate_ship";
    }

    @Override
    public ResourceLocation getEventTypeID()
    {
        return PIRATE_RAID_EVENT_TYPE_ID;
    }

    /**
     * Loads the raid event from the given nbt.
     *
     * @param colony   the events colony
     * @param compound the NBT compound
     * @return the colony to load.
     */
    public static IColonyEvent loadFromNBT(@NotNull final IColony colony, @NotNull final CompoundNBT compound)
    {
        final PirateRaidEvent raidEvent = new PirateRaidEvent(colony);
        raidEvent.readFromNBT(compound);
        return raidEvent;
    }

    @Override
    public EntityType<?> getNormalRaiderType()
    {
        return ModEntities.PIRATE;
    }

    @Override
    public EntityType<?> getArcherRaiderType()
    {
        return ModEntities.ARCHERPIRATE;
    }

    @Override
    public EntityType<?> getBossRaiderType()
    {
        return ModEntities.CHIEFPIRATE;
    }
}
