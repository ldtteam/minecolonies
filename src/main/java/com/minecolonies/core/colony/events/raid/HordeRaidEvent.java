package com.minecolonies.core.colony.events.raid;

import com.minecolonies.api.colony.ColonyState;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.colonyEvents.*;
import com.minecolonies.api.entity.citizen.happiness.ExpirationBasedHappinessModifier;
import com.minecolonies.api.entity.citizen.happiness.StaticHappinessSupplier;
import com.minecolonies.api.entity.mobs.AbstractEntityRaiderMob;
import com.minecolonies.api.entity.mobs.RaiderMobUtils;
import com.minecolonies.core.entity.pathfinding.pathresults.PathResult;
import com.minecolonies.api.sounds.RaidSounds;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.MessageUtils.MessagePriority;
import com.minecolonies.api.util.constant.NbtTagConstants;
import com.minecolonies.core.colony.events.raid.barbarianEvent.Horde;
import com.minecolonies.core.colony.events.raid.pirateEvent.ShipBasedRaiderUtils;
import com.minecolonies.core.network.messages.client.PlayAudioMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.Path;

import java.util.*;

import static com.minecolonies.api.util.constant.ColonyConstants.SMALL_HORDE_SIZE;
import static com.minecolonies.api.util.constant.Constants.TAG_COMPOUND;
import static com.minecolonies.api.util.constant.HappinessConstants.RAIDWITHOUTDEATH;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.core.colony.events.raid.pirateEvent.PirateRaidEvent.TAG_DAYS_LEFT;

/**
 * Horde raid event for the colony, triggers a horde that spawn and attack the colony.
 */
public abstract class HordeRaidEvent implements IColonyRaidEvent, IColonyCampFireRaidEvent
{
    /**
     * Spacing between waypoints
     */
    private static final int WAYPOINT_SPACING = 20;

    /**
     * The max distance a barbarian is allowed to spawn from the original spawn position
     */
    public static int MAX_SPAWN_DEVIATION = 300;

    /**
     * The max distance to search for a loaded blockpos on a respawn try
     */
    public static int MAX_RESPAWN_DEVIATION = 10 * 16;

    /**
     * The minimum distance to the colony center where mobs are allowed to spawn
     */
    public static int MIN_CENTER_DISTANCE = 100;

    /**
     * The amount of entities overall
     */
    protected Horde horde;

    /**
     * The raids visual raidbar
     */
    protected final ServerBossEvent raidBar = new ServerBossEvent(Component.literal("Colony Raid"), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);

    /**
     * The references to living raiders left
     */
    protected Map<Entity, UUID> normal  = new WeakHashMap<>();
    protected Map<Entity, UUID> archers = new WeakHashMap<>();
    protected Map<Entity, UUID> boss    = new WeakHashMap<>();

    /**
     * List of respawns to do
     */
    private List<Tuple<EntityType<?>, BlockPos>> respawns = new ArrayList<>();

    /**
     * Currently active campfires
     */
    private List<BlockPos> campFires = new ArrayList<>();

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
    protected EventStatus status = EventStatus.STARTING;

    /**
     * Days the event can last, to make sure it eventually despawns.
     */
    private int daysToGo = 3;

    /**
     * Time the campfires are active
     */
    private int campFireTime = 0;

    /**
     * The path result towards the intended spawn point
     */
    private PathResult spawnPathResult;

    /**
     * If this was a mercy end.
     */
    private boolean mercyEnd = false;

    /**
     * Waypoints helping raiders travel
     */
    private List<BlockPos> wayPoints = new ArrayList<>();

    public HordeRaidEvent(IColony colony)
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
        entities.addAll(boss.keySet());
        entities.addAll(normal.keySet());
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

    /**
     * Called when an entity is removed
     *
     * @param entity the entity to unregister.
     */
    @Override
    public void unregisterEntity(final Entity entity)
    {
        if (!(archers.containsKey(entity) || boss.containsKey(entity) || normal.containsKey(entity)) || status != EventStatus.PROGRESSING
              || colony.getState() != ColonyState.ACTIVE)
        {
            return;
        }

        archers.remove(entity);
        boss.remove(entity);
        normal.remove(entity);

        // Respawn as a new entity in a loaded chunk, if not too close.
        respawns.add(new Tuple<>(entity.getType(), entity.blockPosition()));
    }

    @Override
    public void onEntityDeath(final LivingEntity entity)
    {
        if (entity instanceof AbstractEntityRaiderMob)
        {
            colony.getRaiderManager().onRaiderDeath((AbstractEntityRaiderMob) entity);
        }
    }

    /**
     * Spawn a specific horde.
     *
     * @param spawnPos        the pos to spawn them at.
     * @param colony          the colony to spawn them for.
     * @param id              the raid event id.
     * @param numberOfArchers the archers.
     * @param numberOfBosses  the bosses.
     * @param numberOfRaiders the normal raiders.
     */
    protected void spawnHorde(final BlockPos spawnPos, final IColony colony, final int id, final int numberOfBosses, final int numberOfArchers, final int numberOfRaiders)
    {
        RaiderMobUtils.spawn(getNormalRaiderType(), numberOfRaiders, spawnPos, colony.getWorld(), colony, id);
        RaiderMobUtils.spawn(getBossRaiderType(), numberOfBosses, spawnPos, colony.getWorld(), colony, id);
        RaiderMobUtils.spawn(getArcherRaiderType(), numberOfArchers, spawnPos, colony.getWorld(), colony, id);
    }

    @Override
    public void setMercyEnd()
    {
        this.mercyEnd = true;
    }

    /**
     * Prepares the horde event, makes them wait at campfires for a while,deciding on their plans.
     */
    private void prepareEvent()
    {
        if (--campFireTime <= 0)
        {
            // Start raiding
            status = EventStatus.PROGRESSING;
        }
    }

    /**
     * Spawns a few campfires around the pos, depending on raid size
     *
     * @param pos the pos to spawn it at.
     */
    private void spawnCampFires(final BlockPos pos)
    {
        final int fireCount = Math.max(1, horde.hordeSize / 5);
        for (int i = 0; i < fireCount; i++)
        {
            for (int tries = 0; tries < 3; tries++)
            {
                BlockPos spawn = BlockPosUtil.getRandomPosition(colony.getWorld(), pos, BlockPos.ZERO, 3, 7);
                if (spawn != BlockPos.ZERO)
                {
                    colony.getWorld().setBlockAndUpdate(spawn, Blocks.CAMPFIRE.defaultBlockState());
                    campFires.add(spawn);
                    break;
                }
            }
        }
    }

    @Override
    public void onFinish()
    {
        for (final Entity entity : getEntities())
        {
            entity.remove(Entity.RemovalReason.DISCARDED);
        }

        for (final BlockPos pos : campFires)
        {
            colony.getWorld().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }

        raidBar.setVisible(false);
        raidBar.removeAllPlayers();

        if (horde.hordeSize > 0)
        {
            if (mercyEnd)
            {
                MessageUtils.format(ALL_BARBARIANS_MERCY_MESSAGE, colony.getName()).sendTo(colony).forManagers();
            }
            else
            {
                MessageUtils.format(ALL_BARBARIANS_KILLED_MESSAGE, colony.getName()).sendTo(colony).forManagers();
            }
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

    public void setHorde(final Horde horde)
    {
        this.horde = horde;
    }

    /**
     * Returns a random campfire
     *
     * @return a random campfire.
     */
    public BlockPos getRandomCampfire()
    {
        if (campFires.isEmpty())
        {
            return null;
        }
        return campFires.get(colony.getWorld().random.nextInt(campFires.size()));
    }

    @Override
    public void onStart()
    {
        if (spawnPathResult != null && spawnPathResult.isDone())
        {
            final Path path = spawnPathResult.getPath();
            if (path != null && path.canReach())
            {
                spawnPoint = path.getEndNode().asBlockPos();
            }
            this.wayPoints = ShipBasedRaiderUtils.createWaypoints(colony.getWorld(), path, WAYPOINT_SPACING);
        }

        final BlockPos spawnPos = ShipBasedRaiderUtils.getLoadedPositionTowardsCenter(spawnPoint, colony, MAX_SPAWN_DEVIATION, spawnPoint, MIN_CENTER_DISTANCE, 10);
        if (spawnPos == null)
        {
            status = EventStatus.CANCELED;
            return;
        }

        status = EventStatus.PREPARING;
        spawnCampFires(spawnPos);
        final double dist = colony.getCenter().distSqr(spawnPos);
        if (dist < MIN_CENTER_DISTANCE * MIN_CENTER_DISTANCE)
        {
            campFireTime = 6;
        }
        else
        {
            campFireTime = 3;
        }

        spawnHorde(spawnPos, colony, id, horde.numberOfBosses, horde.numberOfArchers, horde.numberOfRaiders);

        updateRaidBar();

        MessageUtils.format(RAID_EVENT_MESSAGE + horde.getMessageID(), BlockPosUtil.calcDirection(colony.getCenter(), spawnPoint), colony.getName())
          .withPriority(MessagePriority.DANGER)
          .sendTo(colony)
          .forManagers();
        Log.getLogger().debug("Raiders coming from: " + spawnPoint.toShortString() + " towards colony: " + colony.getName());

        PlayAudioMessage audio = new PlayAudioMessage(horde.initialSize <= SMALL_HORDE_SIZE ? RaidSounds.WARNING_EARLY : RaidSounds.WARNING, SoundSource.RECORDS);
        PlayAudioMessage.sendToAll(getColony(), false, false, audio);
    }

    /**
     * Get the assigned colony.
     *
     * @return the colony.
     */
    public IColony getColony()
    {
        return colony;
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
        if (status == EventStatus.PREPARING)
        {
            prepareEvent();
        }

        updateRaidBar();

        colony.getRaiderManager().setNightsSinceLastRaid(0);

        if (horde.hordeSize <= 0)
        {
            status = EventStatus.DONE;
        }

        if (!respawns.isEmpty())
        {
            for (final Tuple<EntityType<?>, BlockPos> entry : respawns)
            {
                final BlockPos spawnPos = ShipBasedRaiderUtils.getLoadedPositionTowardsCenter(entry.getB(), colony, MAX_RESPAWN_DEVIATION, spawnPoint, MIN_CENTER_DISTANCE, 10);
                if (spawnPos != null)
                {
                    RaiderMobUtils.spawn(entry.getA(), 1, spawnPos, colony.getWorld(), colony, id);
                }
            }
            respawns.clear();
            return;
        }

        if (boss.size() + archers.size() + normal.size() < horde.numberOfBosses + horde.numberOfRaiders + horde.numberOfArchers)
        {
            final BlockPos spawnPos = ShipBasedRaiderUtils.getLoadedPositionTowardsCenter(spawnPoint, colony, MAX_RESPAWN_DEVIATION, spawnPoint, MIN_CENTER_DISTANCE, 10);
            if (spawnPos != null)
            {
                spawnHorde(spawnPos, colony, id, horde.numberOfBosses - boss.size(), horde.numberOfArchers - archers.size(), horde.numberOfRaiders - normal.size());
            }
        }

        if (horde.numberOfBosses + horde.numberOfRaiders + horde.numberOfArchers < Math.round(horde.initialSize * 0.05))
        {
            status = EventStatus.DONE;
        }

        for (final Entity entity : getEntities())
        {
            if (!entity.isAlive() || !WorldUtil.isEntityBlockLoaded(colony.getWorld(), entity.blockPosition()))
            {
                entity.remove(Entity.RemovalReason.DISCARDED);
                respawns.add(new Tuple<>(entity.getType(), entity.blockPosition()));
                continue;
            }

            if (colony.getRaiderManager().areSpiesEnabled())
            {
                ((LivingEntity) entity).addEffect(new MobEffectInstance(MobEffects.GLOWING, 550));
            }
        }
    }

    /**
     * Sends the right horde message.
     */
    protected void sendHordeMessage()
    {
        int total = 0;

        for (IColonyEvent event : colony.getEventManager().getEvents().values())
        {
            if (event instanceof HordeRaidEvent)
            {
                total += ((HordeRaidEvent) event).horde.hordeSize;
            }
        }
        raidBar.setProgress((float) horde.hordeSize / horde.initialSize);

        if (total == 0)
        {
            MessageUtils.format(ALL_BARBARIANS_KILLED_MESSAGE, colony.getName()).sendTo(colony).forManagers();

            PlayAudioMessage audio = new PlayAudioMessage(horde.initialSize <= SMALL_HORDE_SIZE ? RaidSounds.VICTORY_EARLY : RaidSounds.VICTORY, SoundSource.RECORDS);
            PlayAudioMessage.sendToAll(getColony(), false, true, audio);

            if (colony.getRaiderManager().getLostCitizen() == 0)
            {
                colony.getCitizenManager().injectModifier(new ExpirationBasedHappinessModifier(RAIDWITHOUTDEATH, 1.0, new StaticHappinessSupplier(2.0), 3));
            }
        }
        else if (total > 0 && total <= SMALL_HORDE_SIZE)
        {
            MessageUtils.format(ONLY_X_BARBARIANS_LEFT_MESSAGE, total).sendTo(colony).forManagers();
        }
    }

    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag compound = new CompoundTag();
        compound.putInt(TAG_EVENT_ID, id);
        BlockPosUtil.write(compound, TAG_SPAWN_POS, spawnPoint);
        ListTag campFiresNBT = new ListTag();

        for (final BlockPos pos : campFires)
        {
            campFiresNBT.add(BlockPosUtil.write(new CompoundTag(), NbtTagConstants.TAG_POS, pos));
        }

        compound.put(TAG_CAMPFIRE_LIST, campFiresNBT);
        compound.putInt(TAG_EVENT_STATUS, status.ordinal());
        compound.putInt(TAG_DAYS_LEFT, daysToGo);
        horde.writeToNbt(compound);

        BlockPosUtil.writePosListToNBT(compound, TAG_WAYPOINT, wayPoints);
        return compound;
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        id = compound.getInt(TAG_EVENT_ID);
        setHorde(Horde.loadFromNbt(compound));
        spawnPoint = BlockPosUtil.read(compound, TAG_SPAWN_POS);

        for (final Tag posCompound : compound.getList(TAG_CAMPFIRE_LIST, TAG_COMPOUND))
        {
            campFires.add(BlockPosUtil.read((CompoundTag) posCompound, NbtTagConstants.TAG_POS));
        }

        status = EventStatus.values()[compound.getInt(TAG_EVENT_STATUS)];
        daysToGo = compound.getInt(TAG_DAYS_LEFT);
        wayPoints = BlockPosUtil.readPosListFromNBT(compound, TAG_WAYPOINT);
    }

    @Override
    public void setCampFireTime(final int time)
    {
        campFireTime = time;
    }

    @Override
    public void addSpawner(final BlockPos pos)
    {
        // do noting
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
