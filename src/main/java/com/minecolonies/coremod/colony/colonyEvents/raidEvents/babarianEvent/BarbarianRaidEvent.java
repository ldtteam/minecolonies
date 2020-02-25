package com.minecolonies.coremod.colony.colonyEvents.raidEvents.babarianEvent;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.colonyEvents.EventStatus;
import com.minecolonies.api.colony.colonyEvents.IColonyEvent;
import com.minecolonies.api.colony.colonyEvents.IRaidEvent;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.entity.mobs.RaiderMobUtils;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.ColonyState;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.pirateEvent.PirateEventUtils;
import com.minecolonies.coremod.entity.mobs.barbarians.EntityArcherBarbarian;
import com.minecolonies.coremod.entity.mobs.barbarians.EntityBarbarian;
import com.minecolonies.coremod.entity.mobs.barbarians.EntityChiefBarbarian;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.colony.colonyEvents.NBTTags.*;
import static com.minecolonies.api.util.constant.ColonyConstants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.coremod.colony.colonyEvents.raidEvents.pirateEvent.PirateRaidEvent.TAG_DAYS_LEFT;

/**
 * Barbarian raid event for the colony, triggers a horde of barbarians which spawn and attack the colony.
 */
public class BarbarianRaidEvent implements IColonyEvent, IRaidEvent
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
    private List<Tuple<ResourceLocation, BlockPos>> respawns = new ArrayList<>();

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
    public BlockPos getStartPos()
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
        if (!(entity instanceof AbstractEntityMinecoloniesMob) || !entity.isEntityAlive())
        {
            entity.setDead();
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

        entity.setDead();
    }

    /**
     * Called when an entity is removed
     *
     * @param entity
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
        final ResourceLocation entityID = net.minecraftforge.fml.common.registry.EntityRegistry.getEntry(entity.getClass()).getRegistryName();
        respawns.add(new Tuple<>(entityID, entity.getPosition()));
    }

    @Override
    public void onEntityDeath(final EntityLiving entity)
    {
        if (!(entity instanceof AbstractEntityMinecoloniesMob))
        {
            return;
        }

        if (entity.isDead)
        {
            Log.getLogger().warn("THROWS TANTRUM!");
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
        RaiderMobUtils.spawn(CHIEF, horde.numberOfBosses, spawnPos, colony.getWorld(), colony, id);
        RaiderMobUtils.spawn(ARCHER, horde.numberOfArchers, spawnPos, colony.getWorld(), colony, id);

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
            for (final Tuple<ResourceLocation, BlockPos> entry : respawns)
            {
                final BlockPos spawnPos = PirateEventUtils.getLoadedPositionTowardsCenter(entry.getSecond(), colony, MAX_RESPAWN_DEVIATION, spawnPoint, MIN_CENTER_DISTANCE, 10);
                if (spawnPos != null)
                {
                    RaiderMobUtils.spawn(entry.getFirst(), 1, spawnPos, colony.getWorld(), colony, id);
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
                RaiderMobUtils.spawn(CHIEF, horde.numberOfBosses - chiefs.size(), spawnPos, colony.getWorld(), colony, id);
                RaiderMobUtils.spawn(ARCHER, horde.numberOfArchers - archers.size(), spawnPos, colony.getWorld(), colony, id);
                RaiderMobUtils.spawn(BARBARIAN, horde.numberOfRaiders - barbarians.size(), spawnPos, colony.getWorld(), colony, id);
            }
        }

        if (colony.getRaiderManager().areSpiesEnabled())
        {
            for (final Entity entity : getEntities())
            {
                ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(MobEffects.GLOWING, 550));
            }
        }
    }

    @Override
    public void onFinish()
    {
        for (final Entity entity : getEntities())
        {
            entity.setDead();
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
        }
        else if (total > 0 && total <= SMALL_HORDE_SIZE)
        {
            LanguageHandler.sendPlayersMessage(colony.getMessageEntityPlayers(), ONLY_X_BARBARIANS_LEFT_MESSAGE, total);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound compound)
    {
        compound.setInteger(TAG_EVENT_ID, id);
        BlockPosUtil.writeToNBT(compound, TAG_SPAWN_POS, spawnPoint);
        compound.setInteger(TAG_EVENT_STATUS, status.ordinal());
        compound.setInteger(TAG_DAYS_LEFT, daysToGo);
        horde.writeToNbt(compound);
        return compound;
    }

    @Override
    public void readFromNBT(final NBTTagCompound compound)
    {
        id = compound.getInteger(TAG_EVENT_ID);
        setHorde(BarbarianHorde.loadFromNbt(compound));
        spawnPoint = BlockPosUtil.readFromNBT(compound, TAG_SPAWN_POS);
        status = EventStatus.values()[compound.getInteger(TAG_EVENT_STATUS)];
        daysToGo = compound.getInteger(TAG_DAYS_LEFT);
    }

    /**
     * Loads the event from the nbt compound.
     *
     * @param colony   colony to load into
     * @param compound NBTcompound with saved values
     * @return
     */
    public static BarbarianRaidEvent loadFromNBT(final IColony colony, final NBTTagCompound compound)
    {
        BarbarianRaidEvent event = new BarbarianRaidEvent(colony);
        event.readFromNBT(compound);
        return event;
    }
}
