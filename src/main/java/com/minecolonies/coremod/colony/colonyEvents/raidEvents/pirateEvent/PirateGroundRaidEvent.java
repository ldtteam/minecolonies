package com.minecolonies.coremod.colony.colonyEvents.raidEvents.pirateEvent;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.colonyEvents.EventStatus;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.HordeRaidEvent;
import com.minecolonies.coremod.entity.mobs.pirates.EntityArcherPirate;
import com.minecolonies.coremod.entity.mobs.pirates.EntityCaptainPirate;
import com.minecolonies.coremod.entity.mobs.pirates.EntityPirate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;

import static com.minecolonies.api.entity.ModEntities.*;
import static com.minecolonies.api.util.constant.TranslationConstants.RAID_PIRATE;

/**
 * The Pirate raid event, spawns the worst pirates you've ever heard of.
 */
public class PirateGroundRaidEvent extends HordeRaidEvent
{
    /**
     * This raids event id, registry entries use res locations as ids.
     */
    public static final ResourceLocation PIRATE_GROUND_RAID_EVENT_TYPE_ID = new ResourceLocation(Constants.MOD_ID, "pirate_ground_raid");

    public PirateGroundRaidEvent(IColony colony)
    {
        super(colony);
    }

    @Override
    public ResourceLocation getEventTypeID()
    {
        return PIRATE_GROUND_RAID_EVENT_TYPE_ID;
    }

    @Override
    public void onStart()
    {
        super.onStart();
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
    }

    @Override
    public void onFinish()
    {
        super.onFinish();
    }

    @Override
    public void registerEntity(final Entity entity)
    {
        if (!(entity instanceof AbstractEntityMinecoloniesMob) || !entity.isAlive())
        {
            entity.remove();
            return;
        }

        if (entity instanceof EntityCaptainPirate && boss.keySet().size() < horde.numberOfBosses)
        {
            boss.put(entity, entity.getUUID());
            return;
        }

        if (entity instanceof EntityArcherPirate && archers.keySet().size() < horde.numberOfArchers)
        {
            archers.put(entity, entity.getUUID());
            return;
        }

        if (entity instanceof EntityPirate && normal.keySet().size() < horde.numberOfRaiders)
        {
            normal.put(entity, entity.getUUID());
            return;
        }

        entity.remove();
    }

    @Override
    public void onEntityDeath(final LivingEntity entity)
    {
        if (!(entity instanceof AbstractEntityMinecoloniesMob))
        {
            return;
        }

        if (entity instanceof EntityCaptainPirate)
        {
            boss.remove(entity);
            horde.numberOfBosses--;
        }

        if (entity instanceof EntityArcherPirate)
        {
            archers.remove(entity);
            horde.numberOfArchers--;
        }

        if (entity instanceof EntityPirate)
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
    public static PirateGroundRaidEvent loadFromNBT(final IColony colony, final CompoundNBT compound)
    {
        PirateGroundRaidEvent event = new PirateGroundRaidEvent(colony);
        event.deserializeNBT(compound);
        return event;
    }

    @Override
    public EntityType<?> getNormalRaiderType()
    {
        return PIRATE;
    }

    @Override
    public EntityType<?> getArcherRaiderType()
    {
        return ARCHERPIRATE;
    }

    @Override
    public EntityType<?> getBossRaiderType()
    {
        return CHIEFPIRATE;
    }

    @Override
    protected IFormattableTextComponent getDisplayName()
    {
        return new StringTextComponent(LanguageHandler.format(RAID_PIRATE));
    }
}
