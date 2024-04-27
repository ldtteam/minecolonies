package com.minecolonies.core.colony.events.raid.barbarianEvent;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.colonyEvents.EventStatus;
import com.minecolonies.api.entity.mobs.AbstractEntityRaiderMob;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.events.raid.HordeRaidEvent;
import com.minecolonies.core.entity.mobs.barbarians.EntityArcherBarbarian;
import com.minecolonies.core.entity.mobs.barbarians.EntityBarbarian;
import com.minecolonies.core.entity.mobs.barbarians.EntityChiefBarbarian;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import static com.minecolonies.api.entity.ModEntities.*;
import static com.minecolonies.api.util.constant.TranslationConstants.RAID_BARBARIAN;

/**
 * Barbarian raid event for the colony, triggers a horde of barbarians which spawn and attack the colony.
 */
public class BarbarianRaidEvent extends HordeRaidEvent
{
    /**
     * This raids event id, registry entries use res locations as ids.
     */
    public static final ResourceLocation BARBARIAN_RAID_EVENT_TYPE_ID = new ResourceLocation(Constants.MOD_ID, "barbarian_raid");

    public BarbarianRaidEvent(IColony colony)
    {
        super(colony);
    }

    @Override
    public ResourceLocation getEventTypeID()
    {
        return BARBARIAN_RAID_EVENT_TYPE_ID;
    }

    @Override
    public void registerEntity(final Entity entity)
    {
        if (!(entity instanceof AbstractEntityRaiderMob) || !entity.isAlive())
        {
            entity.remove(Entity.RemovalReason.DISCARDED);
            return;
        }

        if (entity instanceof EntityChiefBarbarian && boss.keySet().size() < horde.numberOfBosses)
        {
            boss.put(entity, entity.getUUID());
            return;
        }

        if (entity instanceof EntityArcherBarbarian && archers.keySet().size() < horde.numberOfArchers)
        {
            archers.put(entity, entity.getUUID());
            return;
        }

        if (entity instanceof EntityBarbarian && normal.keySet().size() < horde.numberOfRaiders)
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
        if (!(entity instanceof AbstractEntityRaiderMob))
        {
            return;
        }

        if (entity instanceof EntityChiefBarbarian)
        {
            boss.remove(entity);
            horde.numberOfBosses--;
        }

        if (entity instanceof EntityArcherBarbarian)
        {
            archers.remove(entity);
            horde.numberOfArchers--;
        }

        if (entity instanceof EntityBarbarian)
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
    public static BarbarianRaidEvent loadFromNBT(final IColony colony, final CompoundTag compound)
    {
        BarbarianRaidEvent event = new BarbarianRaidEvent(colony);
        event.deserializeNBT(compound);
        return event;
    }

    @Override
    public EntityType<?> getNormalRaiderType()
    {
        return BARBARIAN;
    }

    @Override
    public EntityType<?> getArcherRaiderType()
    {
        return ARCHERBARBARIAN;
    }

    @Override
    public EntityType<?> getBossRaiderType()
    {
        return CHIEFBARBARIAN;
    }

    @Override
    protected MutableComponent getDisplayName()
    {
        return Component.translatable(RAID_BARBARIAN);
    }
}
