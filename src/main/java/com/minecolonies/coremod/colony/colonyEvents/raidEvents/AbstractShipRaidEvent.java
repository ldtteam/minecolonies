package com.minecolonies.coremod.colony.colonyEvents.raidEvents;

import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.colony.ColonyState;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.colonyEvents.EventStatus;
import com.minecolonies.api.colony.colonyEvents.IColonyRaidEvent;
import com.minecolonies.api.colony.colonyEvents.IColonyStructureSpawnEvent;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.entity.mobs.RaiderMobUtils;
import com.minecolonies.api.entity.pathfinding.PathResult;
import com.minecolonies.api.util.*;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.pirateEvent.ShipBasedRaiderUtils;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.pirateEvent.ShipSize;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.pathfinding.Path;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.BossInfo;
import net.minecraft.world.server.ServerBossInfo;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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
    private static final int WAYPOINT_SPACING = 30;

    /**
     * NBT Tags
     */
    public static final String TAG_DAYS_LEFT     = "pirateDaysLeft";
    public static final String TAG_SPAWNER_COUNT = "spawnerCount";
    public static final String TAG_POS           = "pos";
    public static final String TAG_SPAWNERS      = "spawners";
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
     * The raids visual raidbar
     */
    protected final ServerBossInfo raidBar = new ServerBossInfo(new StringTextComponent("Colony Raid"), BossInfo.Color.RED, BossInfo.Overlay.NOTCHED_10);

    /**
     * The ID of this raid
     */
    private int id;

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
     * The days the event lasts
     */
    private int daysToGo = MineColonies.getConfig().getServer().daysUntilPirateshipsDespawn.get();

    /**
     * Reference to the currently active pirates for this event.
     */
    private Map<Entity, UUID> raiders = new WeakHashMap<>();

    /**
     * Entities which are to be respawned in a loaded chunk.
     */
    private List<Tuple<EntityType<?>, BlockPos>> respawns = new ArrayList<>();

    /**
     * Rotation of the ship to spawn
     */
    private int shipRotation = 0;

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
    private PathResult spawnPathResult;

    /**
     * Waypoints helping raiders travel
     */
    private List<BlockPos> wayPoints = new ArrayList<>();

    /**
     * Create a new ship based raid event.
     *
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
        daysToGo = MineColonies.getConfig().getServer().daysUntilPirateshipsDespawn.get();

        CreativeBuildingStructureHandler structure =
          new CreativeBuildingStructureHandler(colony.getWorld(),
            spawnPoint,
            Structures.SCHEMATICS_PREFIX + ShipBasedRaiderUtils.SHIP_FOLDER + shipSize.schematicPrefix + this.getShipDesc(),
            new PlacementSettings(),
            true);
        structure.getBluePrint().rotateWithMirror(BlockPosUtil.getRotationFromRotations(shipRotation), Mirror.NONE, colony.getWorld());

        if (spawnPathResult != null && spawnPathResult.isDone())
        {
            final Path path = spawnPathResult.getPath();
            if (path != null && path.canReach())
            {
                final BlockPos endpoint = path.getEndNode().asBlockPos().below();
                if (ShipBasedRaiderUtils.canPlaceShipAt(endpoint, structure.getBluePrint(), colony.getWorld()))
                {
                    spawnPoint = endpoint;
                    structure =
                      new CreativeBuildingStructureHandler(colony.getWorld(),
                        spawnPoint,
                        Structures.SCHEMATICS_PREFIX + ShipBasedRaiderUtils.SHIP_FOLDER + shipSize.schematicPrefix + this.getShipDesc(),
                        new PlacementSettings(),
                        true);
                    structure.getBluePrint().rotateWithMirror(BlockPosUtil.getRotationFromRotations(shipRotation), Mirror.NONE, colony.getWorld());
                }
            }
            this.wayPoints = ShipBasedRaiderUtils.createWaypoints(colony.getWorld(), path, WAYPOINT_SPACING);
        }

        if (!ShipBasedRaiderUtils.canPlaceShipAt(spawnPoint, structure.getBluePrint(), colony.getWorld()))
        {
            spawnPoint = spawnPoint.below();
        }

        if (!ShipBasedRaiderUtils.spawnPirateShip(spawnPoint, colony.getWorld(), colony, shipSize.schematicPrefix + this.getShipDesc(), this, shipRotation))
        {
            // Ship event not successfully started.
            status = EventStatus.CANCELED;
            return;
        }

        updateRaidBar();

        MessageUtils.format(RAID_EVENT_MESSAGE_PIRATE + shipSize.messageID, BlockPosUtil.calcDirection(colony.getCenter(), spawnPoint), colony.getName())
          .sendTo(colony).forManagers();
        colony.markDirty();
    }

    /**
     * Updates the raid bar
     */
    protected void updateRaidBar()
    {
        final ITextComponent directionName = BlockPosUtil.calcDirection(colony.getCenter(), spawnPoint);
        raidBar.setName(getDisplayName().append(" - ").append(directionName));
        for (final PlayerEntity player : colony.getPackageManager().getCloseSubscribers())
        {
            raidBar.addPlayer((ServerPlayerEntity) player);
        }
        raidBar.setVisible(true);
    }

    /**
     * Gets the raids display name
     *
     * @return
     */
    protected abstract IFormattableTextComponent getDisplayName();

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
                final BlockPos spawnPos = ShipBasedRaiderUtils.getLoadedPositionTowardsCenter(entry.getB(), colony, MAX_LANDING_DISTANCE, spawnPoint, MIN_CENTER_DISTANCE, 10);
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
            BlockPos spawnPos = ShipBasedRaiderUtils.getLoadedPositionTowardsCenter(spawnPoint, colony, MAX_LANDING_DISTANCE, spawnPoint, MIN_CENTER_DISTANCE, 10);
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
                    ((LivingEntity) entity).addEffect(new EffectInstance(Effects.GLOWING, 550));
                }
            }
        }
    }

    @Override
    public void onFinish()
    {
        MessageUtils.format(PIRATES_SAILING_OFF_MESSAGE, BlockPosUtil.calcDirection(colony.getCenter(), spawnPoint), colony.getName())
          .sendTo(colony).forManagers();
        for (final Entity entity : raiders.keySet())
        {
            entity.remove();
        }

        raidBar.setVisible(false);
        raidBar.removeAllPlayers();
    }

    @Override
    public void onTileEntityBreak(final TileEntity te)
    {
        if (te instanceof MobSpawnerTileEntity)
        {
            spawners.remove(te.getBlockPos());

            raidBar.setPercent((float) spawners.size() / maxSpawners);
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
    }

    @Override
    public void registerEntity(final Entity entity)
    {
        if (!(entity instanceof AbstractEntityMinecoloniesMob) || !entity.isAlive() || status != EventStatus.PROGRESSING)
        {
            entity.remove();
            return;
        }

        if (raiders.keySet().size() < getMaxRaiders())
        {
            raiders.put(entity, entity.getUUID());
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
        respawns.add(new Tuple<>(entity.getType(), new BlockPos(entity.position())));
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
        paths.add(new Tuple<>(Structures.SCHEMATICS_PREFIX + ShipBasedRaiderUtils.SHIP_FOLDER + shipSize.schematicPrefix + this.getShipDesc(), spawnPoint));
        return paths;
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        CompoundNBT compound = new CompoundNBT();
        compound.putInt(TAG_EVENT_ID, id);
        compound.putInt(TAG_DAYS_LEFT, daysToGo);
        compound.putInt(TAG_EVENT_STATUS, status.ordinal());

        @NotNull final ListNBT spawnerListCompound = new ListNBT();
        for (@NotNull final BlockPos entry : spawners)
        {
            @NotNull final CompoundNBT spawnerCompound = new CompoundNBT();
            spawnerCompound.put(TAG_POS, NBTUtil.writeBlockPos(entry));
            spawnerListCompound.add(spawnerCompound);
        }
        compound.put(TAG_SPAWNERS, spawnerListCompound);

        compound.putInt(TAG_SPAWNER_COUNT, maxSpawners);
        BlockPosUtil.write(compound, TAG_SPAWN_POS, spawnPoint);
        compound.putInt(TAG_SHIPSIZE, shipSize.ordinal());
        compound.putInt(TAG_SHIPROTATION, shipRotation);
        BlockPosUtil.writePosListToNBT(compound, TAG_WAYPOINT, wayPoints);
        return compound;
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        id = compound.getInt(TAG_EVENT_ID);
        status = EventStatus.values()[compound.getInt(TAG_EVENT_STATUS)];
        daysToGo = compound.getInt(TAG_DAYS_LEFT);

        @NotNull final ListNBT spawnerListCompound = compound.getList(TAG_SPAWNERS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < spawnerListCompound.size(); i++)
        {
            spawners.add(NBTUtil.readBlockPos(spawnerListCompound.getCompound(i).getCompound(TAG_POS)));
        }

        maxSpawners = compound.getInt(TAG_SPAWNER_COUNT);
        spawnPoint = BlockPosUtil.read(compound, TAG_SPAWN_POS);
        shipSize = ShipSize.values()[compound.getInt(TAG_SHIPSIZE)];
        shipRotation = compound.getInt(TAG_SHIPROTATION);
        wayPoints = BlockPosUtil.readPosListFromNBT(compound, TAG_WAYPOINT);
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
}
