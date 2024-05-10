package com.minecolonies.core.colony.events.raid.pirateEvent;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.colonyEvents.IColonyEvent;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.events.raid.AbstractShipRaidEvent;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.MutableComponent;

import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.TranslationConstants.RAID_PIRATE;

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
     * Ship description
     */
    public static final String SHIP_NAME = "pirate_ship";

    /**
     * Create a new Pirate raid event.
     *
     * @param colony the colony.
     */
    public PirateRaidEvent(@NotNull final IColony colony)
    {
        super(colony);
    }

    @Override
    public String getShipDesc()
    {
        return SHIP_NAME;
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
    public static IColonyEvent loadFromNBT(@NotNull final IColony colony, @NotNull final CompoundTag compound)
    {
        final PirateRaidEvent raidEvent = new PirateRaidEvent(colony);
        raidEvent.deserializeNBT(compound);
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

    @Override
    protected MutableComponent getDisplayName()
    {
        return Component.translatable(RAID_PIRATE);
    }
}
