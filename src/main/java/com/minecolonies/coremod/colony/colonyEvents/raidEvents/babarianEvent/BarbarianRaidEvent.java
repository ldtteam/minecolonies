package com.minecolonies.coremod.colony.colonyEvents.raidEvents.babarianEvent;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.colonyEvents.EventStatus;
import com.minecolonies.api.colony.colonyEvents.IColonyEvent;
import com.minecolonies.api.colony.colonyEvents.IColonyRaidEvent;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.entity.mobs.RaiderMobUtils;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.ColonyState;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.pirateEvent.PirateEventUtils;
import com.minecolonies.coremod.entity.mobs.barbarians.EntityArcherBarbarian;
import com.minecolonies.coremod.entity.mobs.barbarians.EntityBarbarian;
import com.minecolonies.coremod.entity.mobs.barbarians.EntityChiefBarbarian;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.colony.colonyEvents.NBTTags.*;
import static com.minecolonies.api.entity.ModEntities.*;
import static com.minecolonies.api.util.constant.ColonyConstants.SMALL_HORDE_SIZE;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.coremod.colony.colonyEvents.raidEvents.pirateEvent.PirateRaidEvent.TAG_DAYS_LEFT;
import static com.minecolonies.coremod.colony.colonyEvents.raidEvents.pirateEvent.PirateRaidEvent.TAG_KILLED;

/**
 * Barbarian raid event for the colony, triggers a horde of barbarians which spawn and attack the colony.
 */
public class BarbarianRaidEvent implements IColonyRaidEvent
{
    /**
     * This raids event id, registry entries use res locations as ids.
     */
    public static final ResourceLocation BABARIAN_RAID_EVENT_TYPE_ID = new ResourceLocation(Constants.MOD_ID, "babarian_raid");

    /**
     * The max distance a babarian is allowed to spawn from the original spawn position
     */
    public static int MAX_SPAWN_DEVIATION = 300;

    /**
     * The max distance to search for a loaded blockpos on a respawn try
     */
    public static int MAX_RESPAWN_DEVIATION = 5 * 16;

    /**
     * The minimum distance to the colony center where mobs are allowed to spawn
     */
    public static int MIN_CENTER_DISTANCE = 100;

    /**
     * The amount of babarians overall
     */
    private BarbarianHorde horde;

    /**
     * The references to living raiders left
     */
    private Map<Entity, UUID> barbarians = new WeakHashMap<>();
    private Map<Entity, UUID> archers    = new WeakHashMap<>();
    private Map<Entity, UUID> chiefs     = new WeakHashMap<>();

    /**
     * List of respawns to do
     */
    private List<Tuple<EntityType, BlockPos>> respawns = new ArrayList<>();

    /**
     * The related colony
     */
    private IColony colony;

    /**
     * The events id
     */
    private int id;

    /**
     * The events starting spawnpoint
     */
    private BlockPos spawnPoint;

    /**
     * Status of the event
     */
    private EventStatus status = EventStatus.STARTING;

    /**
     * If a citizen was killed during the raid.
     */
    private boolean killedCitizenInRaid = false;

    /**
     * Days the event can last, to make sure it eventually despawns.
     */
    private int daysToGo = 3;

    public BarbarianRaidEvent(IColony colony)
    {
        this.colony = colony;
        id = colony.getEventManager().getAndTakeNextEventID();
    }

    @Override
    public void setSpawnPoint(final BlockPos spawnPoint)
    {
        this.spawnPoint = spawnPoint;
    }

    @Override
    public BlockPos getSpawnPos()
    {
        return spawnPoint;
    }

    @Override
    public List<Entity> getEntities()
    {
        List<Entity> entities = new ArrayList<>();
        entities.addAll(archers.keySet());
        entities.addAll(chiefs.keySet());
        entities.addAll(barbarians.keySet());
        return entities;
    }

    @Override
    public void setKilledCitizenInRaid()
    {
        killedCitizenInRaid = true;
    }

    @Override
    public EventStatus getStatus()
    {
        return status;
    }

    @Override
    public void setStatus(final EventStatus status)
    {
        this.status = status;
    }

    @Override
    public int getID()
    {
        return id;
    }

    @Override
    public ResourceLocation getEventTypeID()
    {
        return BABARIAN_RAID_EVENT_TYPE_ID;
    }

    @Override
    public void setColony(@NotNull final IColony colony)
    {
        this.colony = colony;
    }

    @Override
    public void registerEntity(final Entity entity)
    {
        if (!(entity instanceof AbstractEntityMinecoloniesMob) || !entity.isAlive())
        {
            entity.remove();
            return;
        }

        if (entity instanceof EntityChiefBarbarian && chiefs.keySet().size() < horde.numberOfBosses)
        {
            chiefs.put(entity, entity.getUniqueID());
            return;
        }

        if (entity instanceof EntityArcherBarbarian && archers.keySet().size() < horde.numberOfArchers)
        {
            archers.put(entity, entity.getUniqueID());
            return;
        }

        if (entity instanceof EntityBarbarian && barbarians.keySet().size() < horde.numberOfRaiders)
        {
            barbarians.put(entity, entity.getUniqueID());
            return;
        }

        entity.remove();
    }

    /**
     * Called when an entity is removed
     *
     * @param entity the entity to unregister.
     */
    @Override
    public void unregisterEntity(final Entity entity)
    {
        if (!(archers.containsKey(entity) || chiefs.containsKey(entity) || barbarians.containsKey(entity)) || status != EventStatus.PROGRESSING
              || colony.getState() != ColonyState.ACTIVE)
        {
            return;
        }

        archers.remove(entity);
        chiefs.remove(entity);
        barbarians.remove(entity);

        // Respawn as a new entity in a loaded chunk, if not too close.
        respawns.add(new Tuple<>(entity.getType(), entity.getPosition()));
    }

    @Override
    public void onEntityDeath(final LivingEntity entity)
    {
        if (!(entity instanceof AbstractEntityMinecoloniesMob))
        {
            return;
        }

        if (entity instanceof EntityChiefBarbarian)
        {
            chiefs.remove(entity);
            horde.numberOfBosses--;
        }

        if (entity instanceof EntityArcherBarbarian)
        {
            archers.remove(entity);
            horde.numberOfArchers--;
        }

        if (entity instanceof EntityBarbarian)
        {
            barbarians.remove(entity);
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
    public void onStart()
    {
        final BlockPos spawnPos = PirateEventUtils.getLoadedPositionTowardsCenter(spawnPoint, colony, MAX_SPAWN_DEVIATION, spawnPoint, MIN_CENTER_DISTANCE, 10);
        if (spawnPos == null)
        {
            status = EventStatus.CANCELED;
            return;
        }

        status = EventStatus.PROGRESSING;
        RaiderMobUtils.spawn(BARBARIAN, horde.numberOfRaiders, spawnPos, colony.getWorld(), colony, id);
        RaiderMobUtils.spawn(CHIEFBARBARIAN, horde.numberOfBosses, spawnPos, colony.getWorld(), colony, id);
        RaiderMobUtils.spawn(ARCHERBARBARIAN, horde.numberOfArchers, spawnPos, colony.getWorld(), colony, id);

        LanguageHandler.sendPlayersMessage(
          colony.getImportantMessageEntityPlayers(),
          RAID_EVENT_MESSAGE + horde.getMessageID(), colony.getName(), BlockPosUtil.calcDirection(colony.getCenter(), spawnPoint));
    }

    @Override
    public void onUpdate()
    {
        if (horde.hordeSize == 0)
        {
            status = EventStatus.DONE;
        }

        if (!respawns.isEmpty())
        {
            for (final Tuple<EntityType, BlockPos> entry : respawns)
            {
                final BlockPos spawnPos = PirateEventUtils.getLoadedPositionTowardsCenter(entry.getB(), colony, MAX_RESPAWN_DEVIATION, spawnPoint, MIN_CENTER_DISTANCE, 10);
                if (spawnPos != null)
                {
                    RaiderMobUtils.spawn(entry.getA(), 1, spawnPos, colony.getWorld(), colony, id);
                }
            }
            respawns.clear();
            return;
        }

        if (chiefs.size() + archers.size() + barbarians.size() < horde.numberOfBosses + horde.numberOfRaiders + horde.numberOfArchers)
        {
            final BlockPos spawnPos = PirateEventUtils.getLoadedPositionTowardsCenter(spawnPoint, colony, MAX_RESPAWN_DEVIATION, spawnPoint, MIN_CENTER_DISTANCE, 10);
            if (spawnPos != null)
            {
                RaiderMobUtils.spawn(CHIEFBARBARIAN, horde.numberOfBosses - chiefs.size(), spawnPos, colony.getWorld(), colony, id);
                RaiderMobUtils.spawn(ARCHERBARBARIAN, horde.numberOfArchers - archers.size(), spawnPos, colony.getWorld(), colony, id);
                RaiderMobUtils.spawn(BARBARIAN, horde.numberOfRaiders - barbarians.size(), spawnPos, colony.getWorld(), colony, id);
            }
        }

        if (colony.getRaiderManager().areSpiesEnabled())
        {
            for (final Entity entity : getEntities())
            {
                ((LivingEntity) entity).addPotionEffect(new EffectInstance(Effects.GLOWING, 550));
            }
        }
    }

    @Override
    public void onFinish()
    {
        for (final Entity entity : getEntities())
        {
            entity.remove();
        }
    }

    @Override
    public void onNightFall()
    {
        daysToGo--;
        if (daysToGo < 0)
        {
            status = EventStatus.DONE;
        }
    }

    public void setHorde(final BarbarianHorde horde)
    {
        this.horde = horde;
    }

    /**
     * Sends the right horde message.
     */
    private void sendHordeMessage()
    {
        int total = 0;

        for (IColonyEvent event : colony.getEventManager().getEvents().values())
        {
            if (event instanceof BarbarianRaidEvent)
            {
                total += ((BarbarianRaidEvent) event).horde.hordeSize;
            }
        }

        if (total == 0)
        {
            LanguageHandler.sendPlayersMessage(colony.getImportantMessageEntityPlayers(), ALL_BARBARIANS_KILLED_MESSAGE);
            if (!this.killedCitizenInRaid)
            {
                colony.getCitizenManager().updateModifier("raidwithoutdeath");
            }
        }
        else if (total > 0 && total <= SMALL_HORDE_SIZE)
        {
            LanguageHandler.sendPlayersMessage(colony.getMessagePlayerEntities(), ONLY_X_BARBARIANS_LEFT_MESSAGE, total);
        }
    }

    @Override
    public CompoundNBT writeToNBT(final CompoundNBT compound)
    {
        compound.putInt(TAG_EVENT_ID, id);
        BlockPosUtil.write(compound, TAG_SPAWN_POS, spawnPoint);
        compound.putInt(TAG_EVENT_STATUS, status.ordinal());
        compound.putInt(TAG_DAYS_LEFT, daysToGo);
        horde.writeToNbt(compound);
        compound.putBoolean(TAG_KILLED, killedCitizenInRaid);
        return compound;
    }

    @Override
    public void readFromNBT(final CompoundNBT compound)
    {
        id = compound.getInt(TAG_EVENT_ID);
        setHorde(BarbarianHorde.loadFromNbt(compound));
        spawnPoint = BlockPosUtil.read(compound, TAG_SPAWN_POS);
        status = EventStatus.values()[compound.getInt(TAG_EVENT_STATUS)];
        daysToGo = compound.getInt(TAG_DAYS_LEFT);
        killedCitizenInRaid = compound.getBoolean(TAG_KILLED);
    }

    /**
     * Loads the event from the nbt compound.
     *
     * @param colony   colony to load into
     * @param compound NBTcompound with saved values
     * @return the raid event.
     */
    public static BarbarianRaidEvent loadFromNBT(final IColony colony, final CompoundNBT compound)
    {
        BarbarianRaidEvent event = new BarbarianRaidEvent(colony);
        event.readFromNBT(compound);
        return event;
    }
}
