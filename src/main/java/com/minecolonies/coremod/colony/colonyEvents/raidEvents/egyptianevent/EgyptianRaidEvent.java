package com.minecolonies.coremod.colony.colonyEvents.raidEvents.egyptianevent;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.colonyEvents.EventStatus;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.entity.mobs.RaiderMobUtils;
import com.minecolonies.api.sounds.RaidSounds;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.HordeRaidEvent;
import com.minecolonies.coremod.entity.mobs.egyptians.EntityArcherMummy;
import com.minecolonies.coremod.entity.mobs.egyptians.EntityMummy;
import com.minecolonies.coremod.entity.mobs.egyptians.EntityPharao;
import com.minecolonies.coremod.network.messages.client.StopMusicMessage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;

import static com.minecolonies.api.entity.ModEntities.*;

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
        for (final PlayerEntity player : getColony().getImportantMessageEntityPlayers())
        {
            Network.getNetwork().sendToPlayer(new StopMusicMessage(), (ServerPlayerEntity) player);
            getColony().getWorld().playSound(player, new BlockPos(player.getPosX(), player.getPosY(), player.getPosZ()), RaidSounds.DESERT_RAID_WARNING, SoundCategory.AMBIENT, 0.5f, 1.0f);
        }
        musicCooldown = 2;
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        if (--musicCooldown <= 0)
        {
            for (final PlayerEntity player : getColony().getImportantMessageEntityPlayers())
            {
                Network.getNetwork().sendToPlayer(new StopMusicMessage(), (ServerPlayerEntity) player);
                getColony().getWorld()
                  .playSound(player, player.getPosition(), RaidSounds.DESERT_RAID, SoundCategory.AMBIENT, 2f, 1.0f);
            }
            musicCooldown = 12;
        }
    }

    @Override
    public void onFinish()
    {
        super.onFinish();
        for (final PlayerEntity player : getColony().getImportantMessageEntityPlayers())
        {
            Network.getNetwork().sendToPlayer(new StopMusicMessage(), (ServerPlayerEntity) player);
            getColony().getWorld().playSound(player, new BlockPos(player.getPosX(), player.getPosY(), player.getPosZ()), RaidSounds.DESERT_RAID_VICTORY, SoundCategory.AMBIENT, 0.5f, 1.0f);
        }
    }

    @Override
    public void registerEntity(final Entity entity)
    {
        if (!(entity instanceof AbstractEntityMinecoloniesMob) || !entity.isAlive())
        {
            entity.remove();
            return;
        }

        if (entity instanceof EntityPharao && boss.keySet().size() < horde.numberOfBosses)
        {
            boss.put(entity, entity.getUniqueID());
            return;
        }

        if (entity instanceof EntityArcherMummy && archers.keySet().size() < horde.numberOfArchers)
        {
            archers.put(entity, entity.getUniqueID());
            return;
        }

        if (entity instanceof EntityMummy && normal.keySet().size() < horde.numberOfRaiders)
        {
            normal.put(entity, entity.getUniqueID());
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

    @Override
    protected void spawnHorde(final BlockPos spawnPos, final IColony colony, final int id, final int numberOfBosses, final int numberOfArchers, final int numberOfRaiders)
    {
        RaiderMobUtils.spawn(MUMMY, numberOfRaiders, spawnPos, colony.getWorld(), colony, id);
        RaiderMobUtils.spawn(PHARAO, numberOfBosses, spawnPos, colony.getWorld(), colony, id);
        RaiderMobUtils.spawn(ARCHERMUMMY, numberOfArchers, spawnPos, colony.getWorld(), colony, id);
    }

    /**
     * Loads the event from the nbt compound.
     *
     * @param colony   colony to load into
     * @param compound NBTcompound with saved values
     * @return the raid event.
     */
    public static EgyptianRaidEvent loadFromNBT(final IColony colony, final CompoundNBT compound)
    {
        EgyptianRaidEvent event = new EgyptianRaidEvent(colony);
        event.readFromNBT(compound);
        return event;
    }
}
