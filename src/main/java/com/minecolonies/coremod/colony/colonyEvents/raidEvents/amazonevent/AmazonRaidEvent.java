package com.minecolonies.coremod.colony.colonyEvents.raidEvents.amazonevent;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.colonyEvents.EventStatus;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.sounds.RaidSounds;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.HordeRaidEvent;
import com.minecolonies.coremod.entity.mobs.amazons.EntityAmazonChief;
import com.minecolonies.coremod.entity.mobs.amazons.EntityAmazonSpearman;
import com.minecolonies.coremod.entity.mobs.amazons.EntityArcherAmazon;
import com.minecolonies.coremod.network.messages.client.PlayAudioMessage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import static com.minecolonies.api.entity.ModEntities.*;
import static com.minecolonies.api.util.constant.TranslationConstants.RAID_AMAZON;

/**
 * Amazon raid event for the colony, triggers a horde of amazons that spawn and attack the colony.
 */
public class AmazonRaidEvent extends HordeRaidEvent
{
    /**
     * This raids event id, registry entries use res locations as ids.
     */
    public static final ResourceLocation AMAZON_RAID_EVENT_TYPE_ID = new ResourceLocation(Constants.MOD_ID, "amazon_raid");

    /**
     * Cooldown for the music, to not play it too much/not overlap with itself
     */
    private int musicCooldown = 0;

    public AmazonRaidEvent(IColony colony)
    {
        super(colony);
    }

    @Override
    public ResourceLocation getEventTypeID()
    {
        return AMAZON_RAID_EVENT_TYPE_ID;
    }

    @Override
    public void registerEntity(final Entity entity)
    {
        if (!(entity instanceof AbstractEntityMinecoloniesMob) || !entity.isAlive())
        {
            entity.remove();
            return;
        }

        if (entity instanceof EntityAmazonChief && boss.keySet().size() < horde.numberOfBosses)
        {
            boss.put(entity, entity.getUUID());
            return;
        }

        if (entity instanceof EntityArcherAmazon && archers.keySet().size() < horde.numberOfArchers)
        {
            archers.put(entity, entity.getUUID());
            return;
        }

        if (entity instanceof EntityAmazonSpearman && normal.keySet().size() < horde.numberOfRaiders)
        {
            normal.put(entity, entity.getUUID());
            return;
        }

        entity.remove();
    }

    @Override
    protected void updateRaidBar()
    {
        super.updateRaidBar();
        raidBar.setCreateWorldFog(true);
    }

    @Override
    protected IFormattableTextComponent getDisplayName()
    {
        return new TranslationTextComponent(RAID_AMAZON);
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        if (--musicCooldown <= 0)
        {
            PlayAudioMessage.sendToAll(getColony(), true, true, new PlayAudioMessage(RaidSounds.AMAZON_RAID));
            musicCooldown = 20;
        }
    }

    @Override
    public void onEntityDeath(final LivingEntity entity)
    {
        if (!(entity instanceof AbstractEntityMinecoloniesMob))
        {
            return;
        }

        if (entity instanceof EntityAmazonChief)
        {
            boss.remove(entity);
            horde.numberOfBosses--;
        }

        if (entity instanceof EntityArcherAmazon)
        {
            archers.remove(entity);
            horde.numberOfArchers--;
        }

        if (entity instanceof EntityAmazonSpearman)
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
    public static AmazonRaidEvent loadFromNBT(final IColony colony, final CompoundNBT compound)
    {
        AmazonRaidEvent event = new AmazonRaidEvent(colony);
        event.deserializeNBT(compound);
        return event;
    }

    @Override
    public EntityType<?> getNormalRaiderType()
    {
        return AMAZONSPEARMAN;
    }

    @Override
    public EntityType<?> getArcherRaiderType()
    {
        return AMAZON;
    }

    @Override
    public EntityType<?> getBossRaiderType()
    {
        return AMAZONCHIEF;
    }
}
