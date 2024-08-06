package com.minecolonies.core.colony.events.raid.norsemenevent;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.colonyEvents.IColonyEvent;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.events.raid.AbstractShipRaidEvent;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.MutableComponent;

import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.TranslationConstants.RAID_NORSEMEN;

/**
 * The Norsemen raid event, spawns a ship with Norsemen spawners onboard.
 */
public class NorsemenShipRaidEvent extends AbstractShipRaidEvent
{
    /**
     * This raids event id, registry entries use res locations as ids.
     */
    public static final ResourceLocation NORSEMEN_RAID_EVENT_TYPE_ID = new ResourceLocation(Constants.MOD_ID, "norsemen_ship_raid");

    /**
     * Ship description
     */
    public static final String SHIP_NAME = "norsemen_ship";

    /**
     * Create a new Norsemen raid event.
     *
     * @param colony the colony.
     */
    public NorsemenShipRaidEvent(@NotNull final IColony colony)
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
        return NORSEMEN_RAID_EVENT_TYPE_ID;
    }

    @Override
    protected void updateRaidBar()
    {
        super.updateRaidBar();
        raidBar.setCreateWorldFog(true);
    }

    /**
     * Loads the raid event from the given nbt.
     *
     * @param colony   the events colony
     * @param compound the NBT compound
     * @return the colony to load.
     */
    public static IColonyEvent loadFromNBT(@NotNull final IColony colony, @NotNull final CompoundTag compound, @NotNull final HolderLookup.Provider provider)
    {
        final NorsemenShipRaidEvent raidEvent = new NorsemenShipRaidEvent(colony);
        raidEvent.deserializeNBT(provider, compound);
        return raidEvent;
    }

    @Override
    public EntityType<?> getNormalRaiderType()
    {
        return ModEntities.SHIELDMAIDEN;
    }

    @Override
    public EntityType<?> getArcherRaiderType()
    {
        return ModEntities.NORSEMEN_ARCHER;
    }

    @Override
    public EntityType<?> getBossRaiderType()
    {
        return ModEntities.NORSEMEN_CHIEF;
    }

    @Override
    protected MutableComponent getDisplayName()
    {
        return Component.translatableEscape(RAID_NORSEMEN);
    }
}
