package com.minecolonies.coremod.colony.colonyEvents.raidEvents.pirateEvent;

import com.ldtteam.structures.helpers.Structure;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.colonyEvents.EventStatus;
import com.minecolonies.api.colony.colonyEvents.IColonyEvent;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.entity.mobs.RaiderMobUtils;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.ColonyState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
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
public class PirateRaidEvent implements IColonyEvent, IRaidEvent
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
    private int daysToGo = Configurations.gameplay.daysUntilPirateshipsDespawn;

    /**
     * Reference to the currently active pirates for this event.
     */
    Map<Entity, UUID> pirates = new WeakHashMap<>();

    /**
     * Entities which are to be respawned in a loaded chunk.
     */
    List<Tuple<ResourceLocation, BlockPos>> respawns = new ArrayList<>();

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
        daysToGo = Configurations.gameplay.daysUntilPirateshipsDespawn;
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
            for (final Tuple<ResourceLocation, BlockPos> entry : respawns)
            {
                final BlockPos spawnPos = PirateEventUtils.getLoadedPositionTowardsCenter(entry.getSecond(), colony, MAX_LANDING_DISTANCE, spawnPoint, MIN_CENTER_DISTANCE, 10);
                if (spawnPos != null)
                {
                    RaiderMobUtils.spawn(entry.getFirst(), 1, spawnPos, colony.getWorld(), colony, id);
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
                for (final ResourceLocation mobType : shipSize.pirates)
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
                if (entity instanceof EntityLivingBase)
                {
                    ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(MobEffects.GLOWING, 550));
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
            entity.getEntityWorld().isBlockLoaded(entity.getPosition());
            entity.setDead();
        }
    }

    @Override
    public void onTileEntityBreak(final TileEntity te)
    {
        if (te instanceof TileEntityMobSpawner)
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
    public void onEntityDeath(final EntityLiving entity)
    {
        pirates.remove(entity);
    }

    @Override
    public void registerEntity(final Entity entity)
    {
        if (!(entity instanceof AbstractEntityMinecoloniesMob))
        {
            entity.setDead();
            return;
        }

        if (pirates.keySet().size() < getMaxPirates())
        {
            pirates.put(entity, entity.getUniqueID());
        }
        else
        {
            entity.setDead();
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
        final ResourceLocation entityID = net.minecraftforge.fml.common.registry.EntityRegistry.getEntry(entity.getClass()).getRegistryName();
        respawns.add(new Tuple<>(entityID, entity.getPosition()));
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
    public BlockPos getStartPos()
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
    public NBTTagCompound writeToNBT(final NBTTagCompound compound)
    {
        compound.setInteger(TAG_EVENT_ID, id);
        compound.setInteger(TAG_DAYS_LEFT, daysToGo);
        compound.setInteger(TAG_EVENT_STATUS, status.ordinal());
        compound.setInteger(TAG_SPAWNER_COUNT, spawnerCount);
        BlockPosUtil.writeToNBT(compound, TAG_SPAWN_POS, spawnPoint);
        compound.setInteger(TAG_SHIPSIZE, shipSize.ordinal());
        return compound;
    }

    @Override
    public void readFromNBT(final NBTTagCompound compound)
    {
        id = compound.getInteger(TAG_EVENT_ID);
        status = EventStatus.values()[compound.getInteger(TAG_EVENT_STATUS)];
        daysToGo = compound.getInteger(TAG_DAYS_LEFT);
        spawnerCount = compound.getInteger(TAG_SPAWNER_COUNT);
        spawnPoint = BlockPosUtil.readFromNBT(compound, TAG_SPAWN_POS);
        shipSize = ShipSize.values()[compound.getInteger(TAG_SHIPSIZE)];
    }

    /**
     * Loads the raid event from the given nbt.
     *
     * @param colony   the events colony
     * @param compound the NBT compound
     * @return
     */
    public static IColonyEvent loadFromNBT(@NotNull final IColony colony, @NotNull final NBTTagCompound compound)
    {
        final PirateRaidEvent raidEvent = new PirateRaidEvent(colony);
        raidEvent.readFromNBT(compound);
        return raidEvent;
    }
}
