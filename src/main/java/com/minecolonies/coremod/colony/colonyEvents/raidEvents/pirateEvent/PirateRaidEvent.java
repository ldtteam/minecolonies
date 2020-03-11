package com.minecolonies.coremod.colony.colonyEvents.raidEvents.pirateEvent;

import com.ldtteam.structures.helpers.Structure;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.util.LanguageHandler;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.colonyEvents.EventStatus;
import com.minecolonies.api.colony.colonyEvents.IColonyEvent;
import com.minecolonies.api.colony.colonyEvents.IColonyRaidEvent;
import com.minecolonies.api.colony.colonyEvents.IColonyStructureSpawnEvent;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.entity.mobs.RaiderMobUtils;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.ColonyState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.colony.colonyEvents.NBTTags.*;
import static com.minecolonies.api.util.constant.TranslationConstants.PIRATES_SAILING_OFF_MESSAGE;
import static com.minecolonies.api.util.constant.TranslationConstants.RAID_EVENT_MESSAGE_PIRATE;

/**
 * The Pirate raid event, spawns a ship with pirate spawners onboard.
 */
public class PirateRaidEvent implements IColonyRaidEvent, IColonyStructureSpawnEvent
{
    /**
     * This raids event id, registry entries use res locations as ids.
     */
    public static final ResourceLocation PIRATE_RAID_EVENT_TYPE_ID = new ResourceLocation(Constants.MOD_ID, "pirate_raid");

    /**
     * NBT Tags
     */
    public static final String TAG_DAYS_LEFT     = "pirateDaysLeft";
    public static final String TAG_SPAWNER_COUNT = "spawnerCount";
    public static final String TAG_SHIPSIZE      = "shipSize";

    /**
     * The max distance for spawning pirates when the ship is unloaded
     */
    private static final int MAX_LANDING_DISTANCE = 200;

    /**
     * Min distance from the TH to be allowed to spawn pirates
     */
    private static final int MIN_CENTER_DISTANCE = 200;

    /**
     * The max amount of allowed pirates in addition to spawners
     */
    private static final int ADD_MAX_PIRATES = 10;

    /**
     * The current raidstatus
     */
    private EventStatus status = EventStatus.STARTING;

    /**
     * The ID of this raid
     */
    private int id = 0;

    /**
     * The associated colony
     */
    private IColony colony;

    /**
     * The ships spawnpoint
     */
    private BlockPos spawnPoint;

    /**
     * The events shipsize
     */
    private ShipSize shipSize;

    /**
     * The current amount of spawners left for this ship.
     */
    private int spawnerCount;

    /**
     * The days the event lasts
     */
    private int daysToGo = MineColonies.getConfig().getCommon().daysUntilPirateshipsDespawn.get();

    /**
     * Reference to the currently active pirates for this event.
     */
    private Map<Entity, UUID> pirates = new WeakHashMap<>();

    /**
     * Entities which are to be respawned in a loaded chunk.
     */
    private List<Tuple<EntityType, BlockPos>> respawns = new ArrayList<>();

    /**
     * Create a new Pirate raid event.
     */
    public PirateRaidEvent(@NotNull final IColony colony)
    {
        this.colony = colony;
        id = colony.getEventManager().getAndTakeNextEventID();
    }

    @Override
    public void onStart()
    {
        status = EventStatus.PROGRESSING;
        daysToGo = MineColonies.getConfig().getCommon().daysUntilPirateshipsDespawn.get();
        spawnerCount = shipSize.spawnerCount;

        final Structure structure =
          new Structure(colony.getWorld(), Structures.SCHEMATICS_PREFIX + PirateEventUtils.PIRATESHIP_FOLDER + shipSize.schematicName, new PlacementSettings());
        structure.rotate(BlockPosUtil.getRotationFromRotations(0), colony.getWorld(), spawnPoint, Mirror.NONE);

        if (!PirateEventUtils.canPlaceShipAt(spawnPoint, structure, colony.getWorld()))
        {
            spawnPoint = spawnPoint.down();
        }

        if (!PirateEventUtils.spawnPirateShip(spawnPoint, colony.getWorld(), colony, shipSize.schematicName, id))
        {
            // Pirate event not successfully started.
            status = EventStatus.CANCELED;
            return;
        }

        LanguageHandler.sendPlayersMessage(
          colony.getImportantMessageEntityPlayers(),
          RAID_EVENT_MESSAGE_PIRATE + shipSize.messageID, BlockPosUtil.calcDirection(colony.getCenter(), spawnPoint), colony.getName());

        colony.markDirty();
    }

    @Override
    public void onUpdate()
    {
        colony.getRaiderManager().setNightsSinceLastRaid(0);

        if (!respawns.isEmpty())
        {
            for (final Tuple<EntityType, BlockPos> entry : respawns)
            {
                final BlockPos spawnPos = PirateEventUtils.getLoadedPositionTowardsCenter(entry.getB(), colony, MAX_LANDING_DISTANCE, spawnPoint, MIN_CENTER_DISTANCE, 10);
                if (spawnPos != null)
                {
                    RaiderMobUtils.spawn(entry.getA(), 1, spawnPos, colony.getWorld(), colony, id);
                }
            }
            respawns.clear();
            return;
        }

        // Spawns landing troops.
        if (pirates.size() < spawnerCount * 2)
        {
            final BlockPos spawnPos = PirateEventUtils.getLoadedPositionTowardsCenter(spawnPoint.add(0, 0, -2), colony, MAX_LANDING_DISTANCE, spawnPoint, MIN_CENTER_DISTANCE, 10);
            if (spawnPos != null)
            {
                for (final EntityType mobType : shipSize.pirates)
                {
                    RaiderMobUtils.spawn(mobType, 1, spawnPos, colony.getWorld(), colony, id);
                }
            }
        }

        // Mark entities when spies exist
        if (colony.getRaiderManager().areSpiesEnabled())
        {
            for (final Entity entity : getEntities())
            {
                if (entity instanceof LivingEntity)
                {
                    ((LivingEntity) entity).addPotionEffect(new EffectInstance(Effects.GLOWING, 550));
                }
            }
        }
    }

    @Override
    public void onFinish()
    {
        LanguageHandler.sendPlayersMessage(colony.getImportantMessageEntityPlayers(),
          PIRATES_SAILING_OFF_MESSAGE,
          BlockPosUtil.calcDirection(colony.getCenter(), spawnPoint),
          colony.getName(),
          colony.getName());
        for (final Entity entity : pirates.keySet())
        {
            entity.remove();
        }
    }

    @Override
    public void onTileEntityBreak(final TileEntity te)
    {
        if (te instanceof MobSpawnerTileEntity)
        {
            spawnerCount--;
            // remove at nightfall after spawners are killed.
            if (spawnerCount <= 0)
            {
                daysToGo = 1;
            }
        }
    }

    @Override
    public void onNightFall()
    {
        daysToGo--;
        if (daysToGo <= 0)
        {
            status = EventStatus.DONE;
        }
    }

    @Override
    public void onEntityDeath(final LivingEntity entity)
    {
        pirates.remove(entity);
    }

    @Override
    public void registerEntity(final Entity entity)
    {
        if (!(entity instanceof AbstractEntityMinecoloniesMob) || !entity.isAlive())
        {
            entity.remove();
            return;
        }

        if (pirates.keySet().size() < getMaxPirates())
        {
            pirates.put(entity, entity.getUniqueID());
        }
        else
        {
            entity.remove();
        }
    }

    /**
     * Called when an entity is removed
     *
     * @param entity
     */
    @Override
    public void unregisterEntity(final Entity entity)
    {
        if (!pirates.containsKey(entity) || status != EventStatus.PROGRESSING || colony.getState() != ColonyState.ACTIVE)
        {
            return;
        }

        pirates.remove(entity);
        // Respawn as a new entity in a loaded chunk, if not too close.
        respawns.add(new Tuple<>(entity.getType(), entity.getPosition()));
    }

    /**
     * Get the allowed amount of pirates this event can have.
     *
     * @return
     */
    private int getMaxPirates()
    {
        return spawnerCount * 2 + ADD_MAX_PIRATES;
    }

    /**
     * Sets the ship size for this event.
     *
     * @param shipSize
     */
    public void setShipSize(final ShipSize shipSize)
    {
        this.shipSize = shipSize;
    }

    @Override
    public void setColony(@NotNull final IColony colony)
    {
        this.colony = colony;
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
        return new ArrayList(pirates.keySet());
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
        return PIRATE_RAID_EVENT_TYPE_ID;
    }

    @Override
    public List<Tuple<String, BlockPos>> getSchematicSpawns()
    {
        final List<Tuple<String, BlockPos>> paths = new ArrayList<>();
        paths.add(new Tuple<>(Structures.SCHEMATICS_PREFIX + PirateEventUtils.PIRATESHIP_FOLDER + shipSize.schematicName, spawnPoint));
        return paths;
    }

    @Override
    public CompoundNBT writeToNBT(final CompoundNBT compound)
    {
        compound.putInt(TAG_EVENT_ID, id);
        compound.putInt(TAG_DAYS_LEFT, daysToGo);
        compound.putInt(TAG_EVENT_STATUS, status.ordinal());
        compound.putInt(TAG_SPAWNER_COUNT, spawnerCount);
        BlockPosUtil.write(compound, TAG_SPAWN_POS, spawnPoint);
        compound.putInt(TAG_SHIPSIZE, shipSize.ordinal());
        return compound;
    }

    @Override
    public void readFromNBT(final CompoundNBT compound)
    {
        id = compound.getInt(TAG_EVENT_ID);
        status = EventStatus.values()[compound.getInt(TAG_EVENT_STATUS)];
        daysToGo = compound.getInt(TAG_DAYS_LEFT);
        spawnerCount = compound.getInt(TAG_SPAWNER_COUNT);
        spawnPoint = BlockPosUtil.read(compound, TAG_SPAWN_POS);
        shipSize = ShipSize.values()[compound.getInt(TAG_SHIPSIZE)];
    }

    /**
     * Loads the raid event from the given nbt.
     *
     * @param colony   the events colony
     * @param compound the NBT compound
     * @return
     */
    public static IColonyEvent loadFromNBT(@NotNull final IColony colony, @NotNull final CompoundNBT compound)
    {
        final PirateRaidEvent raidEvent = new PirateRaidEvent(colony);
        raidEvent.readFromNBT(compound);
        return raidEvent;
    }
}
