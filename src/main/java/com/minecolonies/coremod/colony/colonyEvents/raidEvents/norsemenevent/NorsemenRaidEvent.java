package com.minecolonies.coremod.colony.colonyEvents.raidEvents.norsemenevent;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.colonyEvents.EventStatus;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.HordeRaidEvent;
import com.minecolonies.coremod.entity.mobs.norsemen.EntityNorsemenArcher;
import com.minecolonies.coremod.entity.mobs.norsemen.EntityNorsemenChief;
import com.minecolonies.coremod.entity.mobs.norsemen.EntityShieldmaiden;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import static com.minecolonies.api.entity.ModEntities.*;
import static com.minecolonies.api.util.constant.TranslationConstants.RAID_NORSEMEN;

/**
 * Norsemen raid event for the colony, triggers a horde of vikings which spawn and attack the colony.
 */
public class NorsemenRaidEvent extends HordeRaidEvent
{
    /**
     * This raids event id, registry entries use res locations as ids.
     */
    public static final ResourceLocation NORSEMEN_RAID_EVENT_TYPE_ID = new ResourceLocation(Constants.MOD_ID, "norsemen_raid");

    public NorsemenRaidEvent(IColony colony)
    {
        super(colony);
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

    @Override
    public void registerEntity(final Entity entity)
    {
        if (!(entity instanceof AbstractEntityMinecoloniesMob) || !entity.isAlive())
        {
            entity.remove(Entity.RemovalReason.DISCARDED);
            return;
        }

        if (entity instanceof EntityNorsemenChief && boss.keySet().size() < horde.numberOfBosses)
        {
            boss.put(entity, entity.getUUID());
            return;
        }

        if (entity instanceof EntityNorsemenArcher && archers.keySet().size() < horde.numberOfArchers)
        {
            archers.put(entity, entity.getUUID());
            return;
        }

        if (entity instanceof EntityShieldmaiden && normal.keySet().size() < horde.numberOfRaiders)
        {
            normal.put(entity, entity.getUUID());
            return;
        }

        entity.remove(Entity.RemovalReason.DISCARDED);
    }

    @Override
    public void onEntityDeath(final LivingEntity entity)
    {
        super.onEntityDeath(entity);
        if (!(entity instanceof AbstractEntityMinecoloniesMob))
        {
            return;
        }

        if (entity instanceof EntityNorsemenChief)
        {
            boss.remove(entity);
            horde.numberOfBosses--;
        }

        if (entity instanceof EntityNorsemenArcher)
        {
            archers.remove(entity);
            horde.numberOfArchers--;
        }

        if (entity instanceof EntityShieldmaiden)
        {
            normal.remove(entity);
            horde.numberOfRaiders--;
        }

        horde.hordeSize--;

        if (horde.hordeSize == 0)
        {
            status = EventStatus.DONE;
        }

        sendHordeMessage();
    }

    /**
     * Loads the event from the nbt compound.
     *
     * @param colony   colony to load into
     * @param compound NBTcompound with saved values
     * @return the raid event.
     */
    public static NorsemenRaidEvent loadFromNBT(final IColony colony, final CompoundTag compound)
    {
        NorsemenRaidEvent
          event = new NorsemenRaidEvent(colony);
        event.deserializeNBT(compound);
        return event;
    }

    @Override
    public EntityType<?> getNormalRaiderType()
    {
        return SHIELDMAIDEN;
    }

    @Override
    public EntityType<?> getArcherRaiderType()
    {
        return NORSEMEN_ARCHER;
    }

    @Override
    public EntityType<?> getBossRaiderType()
    {
        return NORSEMEN_CHIEF;
    }

    @Override
    protected MutableComponent getDisplayName()
    {
        return Component.translatable(RAID_NORSEMEN);
    }
}
