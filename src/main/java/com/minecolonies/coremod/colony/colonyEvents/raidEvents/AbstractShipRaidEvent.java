package com.minecolonies.coremod.colony.colonyEvents.raidEvents;

import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.util.LanguageHandler;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.colonyEvents.EventStatus;
import com.minecolonies.api.colony.colonyEvents.IColonyRaidEvent;
import com.minecolonies.api.colony.colonyEvents.IColonyStructureSpawnEvent;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.entity.mobs.RaiderMobUtils;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.CreativeBuildingStructureHandler;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.ColonyState;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.pirateEvent.ShipBasedRaiderUtils;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.pirateEvent.ShipSize;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.colony.colonyEvents.NBTTags.*;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * The Ship based raid event, spawns a ship with raider spawners onboard.
 */
public abstract class AbstractShipRaidEvent implements IColonyRaidEvent, IColonyStructureSpawnEvent
{
    /**
     * NBT Tags
     */
    public static final String TAG_DAYS_LEFT     = "pirateDaysLeft";
    public static final String TAG_SPAWNER_COUNT = "spawnerCount";
    public static final String TAG_SHIPSIZE      = "shipSize";
    public static final String TAG_SHIPROTATION  = "shipRotation";
    public static final String TAG_KILLED        = "killed";

    /**
     * The max distance for spawning raiders when the ship is unloaded
     */
    private static final int MAX_LANDING_DISTANCE = 200;

    /**
     * Min distance from the TH to be allowed to spawn raiders
     */
    private static final int MIN_CENTER_DISTANCE = 200;

    /**
     * The max amount of allowed raiders in addition to spawners
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
    private Map<Entity, UUID> raiders = new WeakHashMap<>();

    /**
     * Entities which are to be respawned in a loaded chunk.
     */
    private List<Tuple<EntityType, BlockPos>> respawns = new ArrayList<>();

    /**
     * If a citizen was killed during the raid.
     */
    private boolean killedCitizenInRaid = false;

    /**
     * Rotation of the ship to spawn
     */
    private int shipRotation = 0;

    /**
     * Create a new ship based raid event.
     * @param colony the colony.
     */
    public AbstractShipRaidEvent(@NotNull final IColony colony)
    {
        this.colony = colony;
        id = colony.getEventManager().getAndTakeNextEventID();
    }

    @Override
    public void onStart()
    {
        status = EventStatus.PREPARING;
        daysToGo = MineColonies.getConfig().getCommon().daysUntilPirateshipsDespawn.get();
        spawnerCount = shipSize.spawnerCount;

        final CreativeBuildingStructureHandler structure =
          new CreativeBuildingStructureHandler(colony.getWorld(), spawnPoint, Structures.SCHEMATICS_PREFIX + ShipBasedRaiderUtils.SHIP_FOLDER + shipSize.schematicPrefix + this.getShipDesc(), new PlacementSettings(), true);
        structure.getBluePrint().rotateWithMirror(BlockPosUtil.getRotationFromRotations(shipRotation), Mirror.NONE, colony.getWorld());

        if (!ShipBasedRaiderUtils.canPlaceShipAt(spawnPoint, structure.getBluePrint(), colony.getWorld()))
        {
            spawnPoint = spawnPoint.down();
        }

        if (!ShipBasedRaiderUtils.spawnPirateShip(spawnPoint, colony.getWorld(), colony, shipSize.schematicPrefix + this.getShipDesc(), this, shipRotation))
        {
            // Ship event not successfully started.
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
        status = EventStatus.PROGRESSING;
        colony.getRaiderManager().setNightsSinceLastRaid(0);

        if (!respawns.isEmpty())
        {
            for (final Tuple<EntityType, BlockPos> entry : respawns)
            {
                final BlockPos spawnPos = ShipBasedRaiderUtils.getLoadedPositionTowardsCenter(entry.getB(), colony, MAX_LANDING_DISTANCE, spawnPoint, MIN_CENTER_DISTANCE, 10);
                if (spawnPos != null)
                {
                    RaiderMobUtils.spawn(entry.getA(), 1, spawnPos, colony.getWorld(), colony, id);
                }
            }
            respawns.clear();
            return;
        }

        // Spawns landing troops.
        if (raiders.size() < spawnerCount * 2)
        {
            BlockPos spawnPos = ShipBasedRaiderUtils.getLoadedPositionTowardsCenter(spawnPoint, colony, MAX_LANDING_DISTANCE, spawnPoint, MIN_CENTER_DISTANCE, 10);
            if (spawnPos != null)
            {
                // Find nice position on the ship
                if (spawnPos.distanceSq(spawnPoint) < 25)
                {
                    spawnPos = ShipBasedRaiderUtils.findSpawnPosOnShip(spawnPos, colony.getWorld(), 3);
                }

                RaiderMobUtils.spawn(getNormalRaiderType(), shipSize.normal, spawnPos, colony.getWorld(), colony, id);
                RaiderMobUtils.spawn(getArcherRaiderType(), shipSize.archer, spawnPos, colony.getWorld(), colony, id);
                RaiderMobUtils.spawn(getBossRaiderType(), shipSize.boss, spawnPos, colony.getWorld(), colony, id);
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
        for (final Entity entity : raiders.keySet())
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
                status = EventStatus.WAITING;
                LanguageHandler.sendPlayersMessage(colony.getImportantMessageEntityPlayers(),ALL_PIRATE_SPAWNERS_DESTROYED_MESSAGE);
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
        raiders.remove(entity);
        if (raiders.isEmpty() && spawnerCount == 0)
        {
            LanguageHandler.sendPlayersMessage(colony.getImportantMessageEntityPlayers(),ALL_PIRATES_KILLED_MESSAGE);
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

        if (raiders.keySet().size() < getMaxRaiders())
        {
            raiders.put(entity, entity.getUniqueID());
        }
        else
        {
            entity.remove();
        }
    }

    /**
     * Called when an entity is removed
     *
     * @param entity the entity to unregister.
     */
    @Override
    public void unregisterEntity(final Entity entity)
    {
        if (!raiders.containsKey(entity) || status != EventStatus.PROGRESSING || colony.getState() != ColonyState.ACTIVE)
        {
            return;
        }

        raiders.remove(entity);
        // Respawn as a new entity in a loaded chunk, if not too close.
        respawns.add(new Tuple<>(entity.getType(), entity.getPosition()));
    }

    /**
     * Get the allowed amount of raiders this event can have.
     *
     * @return the max number of raiders.
     */
    private int getMaxRaiders()
    {
        return spawnerCount * 2 + ADD_MAX_PIRATES;
    }

    /**
     * Sets the ship size for this event.
     *
     * @param shipSize the ship size.
     */
    public void setShipSize(final ShipSize shipSize)
    {
        this.shipSize = shipSize;
    }

    /**
     * Sets the ships rotation
     *
     * @param shipRotation the ship rotation.
     */
    public void setShipRotation(final int shipRotation)
    {
        this.shipRotation = shipRotation;
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
        return new ArrayList(raiders.keySet());
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
    public List<Tuple<String, BlockPos>> getSchematicSpawns()
    {
        final List<Tuple<String, BlockPos>> paths = new ArrayList<>();
        paths.add(new Tuple<>(Structures.SCHEMATICS_PREFIX + ShipBasedRaiderUtils.SHIP_FOLDER + shipSize.schematicPrefix + this.getShipDesc(), spawnPoint));
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
        compound.putBoolean(TAG_KILLED, killedCitizenInRaid);
        compound.putInt(TAG_SHIPROTATION, shipRotation);
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
        killedCitizenInRaid = compound.getBoolean(TAG_KILLED);
        shipRotation = compound.getInt(TAG_SHIPROTATION);
    }

    @Override
    public void setKilledCitizenInRaid()
    {
        killedCitizenInRaid = true;
    }
}
