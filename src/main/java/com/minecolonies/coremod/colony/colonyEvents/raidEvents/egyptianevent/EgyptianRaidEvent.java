package com.minecolonies.coremod.colony.colonyEvents.raidEvents.egyptianevent;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.colonyEvents.EventStatus;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.sounds.RaidSounds;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.HordeRaidEvent;
import com.minecolonies.coremod.entity.mobs.egyptians.EntityArcherMummy;
import com.minecolonies.coremod.entity.mobs.egyptians.EntityMummy;
import com.minecolonies.coremod.entity.mobs.egyptians.EntityPharao;
import com.minecolonies.coremod.network.messages.client.PlayAudioMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import static com.minecolonies.api.entity.ModEntities.*;
import static com.minecolonies.api.util.constant.TranslationConstants.RAID_EGYPTIAN;

/**
 * Egyptian raid event for the colony, triggers a horde of egyptians that spawn and attack the colony.
 */
public class EgyptianRaidEvent extends HordeRaidEvent
{
    /**
     * This raids event id, registry entries use res locations as ids.
     */
    public static final ResourceLocation EGYPTIAN_RAID_EVENT_TYPE_ID = new ResourceLocation(Constants.MOD_ID, "egyptian_raid");

    /**
     * Cooldown for the music, to not play it too much/not overlap with itself
     */
    private int musicCooldown = 0;

    public EgyptianRaidEvent(IColony colony)
    {
        super(colony);
    }

    @Override
    public ResourceLocation getEventTypeID()
    {
        return EGYPTIAN_RAID_EVENT_TYPE_ID;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        PlayAudioMessage.sendToAll(getColony(), true, false, new PlayAudioMessage(RaidSounds.DESERT_RAID_WARNING));
    }

    @Override
    protected void updateRaidBar()
    {
        super.updateRaidBar();
        raidBar.setDarkenScreen(true);
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        if (--musicCooldown <= 0)
        {
            PlayAudioMessage.sendToAll(getColony(), true, true, new PlayAudioMessage(RaidSounds.DESERT_RAID));
            musicCooldown = 12;
        }
    }

    @Override
    public void registerEntity(final Entity entity)
    {
        if (!(entity instanceof AbstractEntityMinecoloniesMob) || !entity.isAlive())
        {
            entity.remove(Entity.RemovalReason.DISCARDED);
            return;
        }

        if (entity instanceof EntityPharao && boss.keySet().size() < horde.numberOfBosses)
        {
            boss.put(entity, entity.getUUID());
            return;
        }

        if (entity instanceof EntityArcherMummy && archers.keySet().size() < horde.numberOfArchers)
        {
            archers.put(entity, entity.getUUID());
            return;
        }

        if (entity instanceof EntityMummy && normal.keySet().size() < horde.numberOfRaiders)
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

        if (entity instanceof EntityPharao)
        {
            boss.remove(entity);
            horde.numberOfBosses--;
        }

        if (entity instanceof EntityArcherMummy)
        {
            archers.remove(entity);
            horde.numberOfArchers--;
        }

        if (entity instanceof EntityMummy)
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
    public static EgyptianRaidEvent loadFromNBT(final IColony colony, final CompoundTag compound)
    {
        EgyptianRaidEvent event = new EgyptianRaidEvent(colony);
        event.deserializeNBT(compound);
        return event;
    }

    @Override
    public EntityType<?> getNormalRaiderType()
    {
        return MUMMY;
    }

    @Override
    public EntityType<?> getArcherRaiderType()
    {
        return ARCHERMUMMY;
    }

    @Override
    public EntityType<?> getBossRaiderType()
    {
        return PHARAO;
    }

    @Override
    protected MutableComponent getDisplayName()
    {
        return Component.translatable(RAID_EGYPTIAN);
    }
}
