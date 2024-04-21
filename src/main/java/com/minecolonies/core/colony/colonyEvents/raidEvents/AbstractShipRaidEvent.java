package com.minecolonies.core.colony.colonyEvents.raidEvents;

import com.ldtteam.structurize.api.RotationMirror;
import com.ldtteam.structurize.storage.ServerFutureProcessor;
import com.ldtteam.structurize.storage.StructurePacks;
import com.minecolonies.api.colony.ColonyState;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.colonyEvents.EventStatus;
import com.minecolonies.api.colony.colonyEvents.IColonyRaidEvent;
import com.minecolonies.api.colony.colonyEvents.IColonyStructureSpawnEvent;
import com.minecolonies.api.entity.mobs.AbstractEntityRaiderMob;
import com.minecolonies.api.entity.mobs.RaiderMobUtils;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.MessageUtils.MessagePriority;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.core.colony.colonyEvents.raidEvents.pirateEvent.ShipBasedRaiderUtils;
import com.minecolonies.core.colony.colonyEvents.raidEvents.pirateEvent.ShipSize;
import com.minecolonies.core.entity.pathfinding.pathresults.PathResult;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.util.constant.Constants.STORAGE_STYLE;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * The Ship based raid event, spawns a ship with raider spawners onboard.
 */
public abstract class AbstractShipRaidEvent implements IColonyRaidEvent, IColonyStructureSpawnEvent
{
    /**
     * Spacing between waypoints
     */
    protected static final int WAYPOINT_SPACING = 30;

    /**
     * NBT Tags
     */
    public static final String TAG_DAYS_LEFT     = "pirateDaysLeft";
    public static final String TAG_SPAWNER_COUNT = "spawnerCount";
    public static final String TAG_POS           = "pos";
    public static final String TAG_SPAWNERS      = "spawners";
    public static final String TAG_SHIPSIZE      = "shipSize";
    public static final String TAG_SHIP_ROTMIR   = "shipRotMir";
    public static final String TAG_RAIDER_THRESHOLD_TRACKER = "raiderkillthresholdtracker";
    public static final String TAG_MAX_RAIDER_COUNT         = "maxRaiderCount";

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
    protected EventStatus status = EventStatus.STARTING;

    /**
     * The raids visual raidbar
     */
    protected final ServerBossEvent raidBar = new ServerBossEvent(Component.literal("Colony Raid"), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);

    /**
     * The ID of this raid
     */
    private int id;

    /**
     * The associated colony
     */
    protected IColony colony;

    /**
     * The ships spawnpoint
     */
    protected BlockPos spawnPoint;

    /**
     * The events shipsize
     */
    protected ShipSize shipSize;

    /**
     * The days the event lasts
     */
    private int daysToGo = 3;

    /**
     * Reference to the currently active pirates for this event.
     */
    protected Map<Entity, UUID> raiders = new WeakHashMap<>();

    /**
     * Entities which are to be respawned in a loaded chunk.
     */
    private List<Tuple<EntityType<?>, BlockPos>> respawns = new ArrayList<>();

    /**
     * Rotation of the ship to spawn
     */
    protected RotationMirror shipRotationMirror = RotationMirror.NONE;

    /**
     * List of all spawners.
     */
    private List<BlockPos> spawners = new ArrayList<>();

    /**
     * Count of spawners.
     */
    private int maxSpawners = 0;

    /**
     * The path result towards the intended spawn point
     */
    protected PathResult spawnPathResult;

    /**
     * Waypoints helping raiders travel
     */
    protected List<BlockPos> wayPoints = new ArrayList<>();

    /**
     * Max raider count.
     */
    protected int maxRaiderCount = 0;

    /**
     * Raider kill count.
     */
    protected int spawnerThresholdKillTracker = 0;

    /**
     * Create a new ship based raid event.
     *
     * @param colony the colony.
     */
    public AbstractShipRaidEvent(@NotNull final IColony colony)
    {
        this.colony = colony;
        this.id = colony.getEventManager().getAndTakeNextEventID();
    }

    @Override
    public void onStart()
    {
        status = EventStatus.PREPARING;

        ServerFutureProcessor.queueBlueprint(new ServerFutureProcessor.BlueprintProcessingData(StructurePacks.getBlueprintFuture(STORAGE_STYLE,
          "decorations" + ShipBasedRaiderUtils.SHIP_FOLDER + shipSize.schematicPrefix + this.getShipDesc() + ".blueprint"), colony.getWorld(), (blueprint -> {
            blueprint.setRotationMirror(shipRotationMirror, colony.getWorld());

            if (spawnPathResult != null && spawnPathResult.isDone())
            {
                final Path path = spawnPathResult.getPath();
                if (path != null && path.canReach())
                {
                    final BlockPos endpoint = path.getEndNode().asBlockPos().below();
                    if (ShipBasedRaiderUtils.canPlaceShipAt(endpoint, blueprint, colony.getWorld()))
                    {
                        spawnPoint = endpoint;
                    }
                }
                this.wayPoints = ShipBasedRaiderUtils.createWaypoints(colony.getWorld(), path, WAYPOINT_SPACING);
            }

            if (!ShipBasedRaiderUtils.canPlaceShipAt(spawnPoint, blueprint, colony.getWorld()))
            {
                spawnPoint = spawnPoint.below();
            }

            if (!ShipBasedRaiderUtils.spawnPirateShip(spawnPoint, colony, blueprint, this))
            {
                // Ship event not successfully started.
                status = EventStatus.CANCELED;
                return;
            }

            updateRaidBar();

            MessageUtils.format(RAID_EVENT_MESSAGE_PIRATE + shipSize.messageID, BlockPosUtil.calcDirection(colony.getCenter(), spawnPoint), colony.getName())
              .withPriority(MessagePriority.DANGER)
              .sendTo(colony).forManagers();
            colony.markDirty();
        })));
    }

    /**
     * Updates the raid bar
     */
    protected void updateRaidBar()
    {
        final Component directionName = BlockPosUtil.calcDirection(colony.getCenter(), spawnPoint).getLongText();
        raidBar.setName(getDisplayName().append(" - ").append(directionName));
        for (final Player player : colony.getPackageManager().getCloseSubscribers())
        {
            raidBar.addPlayer((ServerPlayer) player);
        }
        raidBar.setVisible(true);
    }

    /**
     * Gets the raids display name
     *
     * @return
     */
    protected abstract MutableComponent getDisplayName();

    @Override
    public void onUpdate()
    {
        status = EventStatus.PROGRESSING;
        colony.getRaiderManager().setNightsSinceLastRaid(0);

        if (spawners.size() <= 0 && raiders.size() == 0 && respawns.isEmpty())
        {
            status = EventStatus.WAITING;
            return;
        }

        updateRaidBar();

        if (!respawns.isEmpty())
        {
            for (final Tuple<EntityType<?>, BlockPos> entry : respawns)
            {
                final BlockPos spawnPos = ShipBasedRaiderUtils.getLoadedPositionTowardsCenter(entry.getB(), colony, MAX_LANDING_DISTANCE, spawnPoint, MIN_CENTER_DISTANCE, 10, this.isUnderWater());
                if (spawnPos != null)
                {
                    RaiderMobUtils.spawn(entry.getA(), 1, spawnPos, colony.getWorld(), colony, id);
                }
            }
            respawns.clear();
            return;
        }

        spawners.removeIf(spawner -> colony.getWorld() != null
                                       && WorldUtil.isBlockLoaded(colony.getWorld(), spawner)
                                       && colony.getWorld().getBlockState(spawner).getBlock() != Blocks.SPAWNER);

        // Spawns landing troops.
        if (raiders.size() < spawners.size() * 2)
        {
            BlockPos spawnPos = ShipBasedRaiderUtils.getLoadedPositionTowardsCenter(spawnPoint, colony, MAX_LANDING_DISTANCE, spawnPoint, MIN_CENTER_DISTANCE, 10, this.isUnderWater());
            if (spawnPos != null)
            {
                // Find nice position on the ship
                if (spawnPos.distSqr(spawnPoint) < 25)
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
                    ((LivingEntity) entity).addEffect(new MobEffectInstance(MobEffects.GLOWING, 550));
                }
            }
        }
    }

    /**
     * If this is an underwater ship based event.
     * @return true if so.
     */
    public boolean isUnderWater()
    {
        return false;
    }

    @Override
    public void setMercyEnd()
    {
        // Noop, the sailing away message is fine.
    }

    @Override
    public void onFinish()
    {
        MessageUtils.format(PIRATES_SAILING_OFF_MESSAGE, BlockPosUtil.calcDirection(colony.getCenter(), spawnPoint), colony.getName())
          .sendTo(colony).forManagers();
        for (final Entity entity : raiders.keySet())
        {
            entity.remove(Entity.RemovalReason.DISCARDED);
        }

        raidBar.setVisible(false);
        raidBar.removeAllPlayers();
    }

    @Override
    public void onTileEntityBreak(final BlockEntity te)
    {
        if (te instanceof SpawnerBlockEntity)
        {
            spawners.remove(te.getBlockPos());

            raidBar.setProgress((float) spawners.size() / maxSpawners);
            // remove at nightfall after spawners are killed.
            if (spawners.isEmpty())
            {
                daysToGo = 2;
                MessageUtils.format(ALL_PIRATE_SPAWNERS_DESTROYED_MESSAGE, colony.getName()).sendTo(colony).forManagers();
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
        if (raiders.isEmpty() && spawners.isEmpty())
        {
            status = EventStatus.WAITING;
            MessageUtils.format(ALL_PIRATES_KILLED_MESSAGE, colony.getName()).sendTo(colony).forManagers();
        }
        spawnerThresholdKillTracker++;

        if (!spawners.isEmpty() && spawnerThresholdKillTracker > maxRaiderCount/maxSpawners)
        {
            MessageUtils.format(STRUCTURE_SPAWNER_BREAKS, colony.getName()).sendTo(colony).forManagers();
            colony.getWorld().removeBlock(spawners.remove(0), false);
            raidBar.setProgress((float) spawners.size() / maxSpawners);
            spawnerThresholdKillTracker = 0;
            if (spawners.isEmpty())
            {
                daysToGo = 1;
                MessageUtils.format(ALL_PIRATE_SPAWNERS_DESTROYED_MESSAGE, colony.getName()).sendTo(colony).forManagers();
            }
        }
    }

    @Override
    public void registerEntity(final Entity entity)
    {
        if (!(entity instanceof AbstractEntityRaiderMob) || !entity.isAlive() || status != EventStatus.PROGRESSING)
        {
            entity.remove(Entity.RemovalReason.DISCARDED);
            return;
        }

        if (raiders.keySet().size() < getMaxRaiders())
        {
            raiders.put(entity, entity.getUUID());
        }
        else
        {
            entity.remove(Entity.RemovalReason.DISCARDED);
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
        respawns.add(new Tuple<>(entity.getType(), entity.blockPosition()));
    }

    /**
     * Get the allowed amount of raiders this event can have.
     *
     * @return the max number of raiders.
     */
    private int getMaxRaiders()
    {
        return spawners.size() * 2 + ADD_MAX_PIRATES;
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
     * @param shipRotationMirror the ship rotation.
     */
    public void setShipRotation(final RotationMirror shipRotationMirror)
    {
        this.shipRotationMirror = shipRotationMirror;
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
        return new ArrayList<>(raiders.keySet());
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
        paths.add(new Tuple<>("decorations" + ShipBasedRaiderUtils.SHIP_FOLDER + shipSize.schematicPrefix + this.getShipDesc() + ".blueprint", spawnPoint));
        return paths;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag compound = new CompoundTag();
        compound.putInt(TAG_EVENT_ID, id);
        compound.putInt(TAG_DAYS_LEFT, daysToGo);
        compound.putInt(TAG_EVENT_STATUS, status.ordinal());

        @NotNull final ListTag spawnerListCompound = new ListTag();
        for (@NotNull final BlockPos entry : spawners)
        {
            @NotNull final CompoundTag spawnerCompound = new CompoundTag();
            spawnerCompound.put(TAG_POS, NbtUtils.writeBlockPos(entry));
            spawnerListCompound.add(spawnerCompound);
        }
        compound.put(TAG_SPAWNERS, spawnerListCompound);

        compound.putInt(TAG_SPAWNER_COUNT, maxSpawners);
        BlockPosUtil.write(compound, TAG_SPAWN_POS, spawnPoint);
        compound.putInt(TAG_SHIPSIZE, shipSize.ordinal());
        compound.putByte(TAG_SHIP_ROTMIR, (byte) shipRotationMirror.ordinal());
        BlockPosUtil.writePosListToNBT(compound, TAG_WAYPOINT, wayPoints);
        compound.putInt(TAG_MAX_RAIDER_COUNT, maxRaiderCount);
        compound.putInt(TAG_RAIDER_THRESHOLD_TRACKER, spawnerThresholdKillTracker);
        return compound;
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        id = compound.getInt(TAG_EVENT_ID);
        status = EventStatus.values()[compound.getInt(TAG_EVENT_STATUS)];
        daysToGo = compound.getInt(TAG_DAYS_LEFT);

        @NotNull final ListTag spawnerListCompound = compound.getList(TAG_SPAWNERS, Tag.TAG_COMPOUND);
        for (int i = 0; i < spawnerListCompound.size(); i++)
        {
            spawners.add(NbtUtils.readBlockPos(spawnerListCompound.getCompound(i).getCompound(TAG_POS)));
        }

        maxSpawners = compound.getInt(TAG_SPAWNER_COUNT);
        spawnPoint = BlockPosUtil.read(compound, TAG_SPAWN_POS);
        shipSize = ShipSize.values()[compound.getInt(TAG_SHIPSIZE)];
        shipRotationMirror = RotationMirror.values()[compound.getByte(TAG_SHIP_ROTMIR)];
        wayPoints = BlockPosUtil.readPosListFromNBT(compound, TAG_WAYPOINT);
        maxRaiderCount = compound.getInt(TAG_MAX_RAIDER_COUNT);
        spawnerThresholdKillTracker = compound.getInt(TAG_RAIDER_THRESHOLD_TRACKER);
    }

    @Override
    public void addSpawner(final BlockPos pos)
    {
        this.spawners.add(pos);
        maxSpawners++;
    }

    @Override
    public List<BlockPos> getWayPoints()
    {
        return wayPoints;
    }

    /**
     * Set the pathing for this raids spawnpoint
     *
     * @param result pathing result to wait for
     */
    public void setSpawnPath(final PathResult result)
    {
        this.spawnPathResult = result;
    }

    @Override
    public boolean isRaidActive()
    {
        if (getStatus() == EventStatus.PROGRESSING)
        {
            return !spawners.isEmpty() || !raiders.isEmpty() || !respawns.isEmpty();
        }
        return getStatus() == EventStatus.PROGRESSING ||getStatus() == EventStatus.PREPARING;
    }

    @Override
    public void setMaxRaiderCount(final int maxRaiderCount)
    {
        this.maxRaiderCount = maxRaiderCount;
    }
}
