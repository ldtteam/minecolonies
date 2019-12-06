package com.minecolonies.coremod.colony.managers;

import com.ldtteam.structurize.management.StructureName;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.managers.interfaces.IRaiderManager;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InstantStructurePlacer;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.blockout.Log;
import com.minecolonies.coremod.colony.Colony;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.ColonyConstants.*;
import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

public class RaidManager implements IRaiderManager
{
    /**
     * Whether there will be a raid in this colony tonight.
     */
    private boolean raidTonight = false;

    /**
     * Whether or not the raid has been calculated for today.
     */
    private boolean raidBeenCalculated = false;

    /**
     * Whether or not this colony may have Raider events. (set via command)
     */
    private boolean haveBarbEvents = true;

    /**
     * Last raider spawnpoints.
     */
    private final List<BlockPos> lastSpawnPoints = new ArrayList<>();

    /**
     * A map of schematics which should despawn after some time.
     */
    private final Map<BlockPos, Tuple<String, Long>> schematicMap = new HashMap<>();

    /**
     * The colony of the manager.
     */
    private final Colony colony;

    /**
     * List of raiders registered to the colony.
     */
    private final List<UUID> horde = new ArrayList<>();

    /**
     * Creates the RaidManager for a colony.
     *
     * @param colony the colony.
     */
    public RaidManager(final Colony colony)
    {
        this.colony = colony;
    }

    @Override
    public boolean canHaveRaiderEvents()
    {
        return this.haveBarbEvents;
    }

    @Override
    public boolean hasRaidBeenCalculated()
    {
        return this.raidBeenCalculated;
    }

    @Override
    public boolean willRaidTonight()
    {
        return this.raidTonight;
    }

    @Override
    public void setCanHaveRaiderEvents(final boolean canHave)
    {
        this.haveBarbEvents = canHave;
    }

    @Override
    public void addRaiderSpawnPoint(final BlockPos pos)
    {
        lastSpawnPoints.add(pos);
    }

    @Override
    public void setHasRaidBeenCalculated(final boolean hasSet)
    {
        this.raidBeenCalculated = hasSet;
    }

    @Override
    public void setWillRaidTonight(final boolean willRaid)
    {
        this.raidTonight = willRaid;
    }

    @Override
    public BlockPos getRandomOutsiderInDirection(final EnumFacing directionX, final EnumFacing directionZ)
    {
        final BlockPos center = colony.getCenter();
        final World world = colony.getWorld();

        if (world == null)
        {
            return center;
        }

        final List<BlockPos> positions = colony.getWayPoints().keySet().stream().filter(
          pos -> isInDirection(directionX, directionZ, pos.subtract(center))).collect(Collectors.toList());
        positions.addAll(colony.getBuildingManager().getBuildings().keySet().stream().filter(
          pos -> isInDirection(directionX, directionZ, pos.subtract(center))).collect(Collectors.toList()));

        BlockPos thePos = center;
        double distance = 0;
        IBuilding theBuilding = null;
        for (final BlockPos pos : positions)
        {
            final double currentDistance = center.distanceSq(pos);
            if (currentDistance > distance && world.isAreaLoaded(pos, DEFAULT_SPAWN_RADIUS))
            {
                distance = currentDistance;
                thePos = pos;
                theBuilding = colony.getBuildingManager().getBuilding(thePos);
            }
        }

        int minDistance = 0;
        if (theBuilding != null)
        {
            final Tuple<Tuple<Integer, Integer>, Tuple<Integer, Integer>> corners = theBuilding.getCorners();
            minDistance
              = Math.max(corners.getFirst().getFirst() - corners.getFirst().getSecond(), corners.getSecond().getFirst() - corners.getSecond().getSecond());
        }

        if (thePos.equals(center))
        {
            return center;
        }

        int radius = DEFAULT_SPAWN_RADIUS;
        while (world.isAreaLoaded(thePos, radius))
        {
            radius += DEFAULT_SPAWN_RADIUS;
        }

        final int dist = Math.max(minDistance, Math.min(radius, MAX_SPAWN_RADIUS));
        thePos = thePos.offset(directionX, dist);
        thePos = thePos.offset(directionZ, dist);

        final int randomDegree = world.rand.nextInt((int) WHOLE_CIRCLE);
        final double rads = (double) randomDegree / HALF_A_CIRCLE * Math.PI;

        final double x = Math.round(thePos.getX() + 3 * Math.sin(rads));
        final double z = Math.round(thePos.getZ() + 3 * Math.cos(rads));

        return new BlockPos(x, thePos.getY(), z);
    }

    @Override
    public List<BlockPos> getLastSpawnPoints()
    {
        return new ArrayList<>(lastSpawnPoints);
    }

    @Override
    public void registerRaider(@NotNull final AbstractEntityMinecoloniesMob raider)
    {
        this.horde.add(raider.getUniqueID());
    }

    @Override
    public void unregisterRaider(@NotNull final AbstractEntityMinecoloniesMob raider, final WorldServer world)
    {
        for (final UUID uuid : new ArrayList<>(horde))
        {
            final Entity raiderEntity = world.getEntityFromUuid(uuid);
            if (raiderEntity == null || !raiderEntity.isEntityAlive() || uuid.equals(raider.getUniqueID()))
            {
                horde.remove(uuid);
            }
        }

        sendHordeMessage();
    }

    private void sendHordeMessage()
    {
        if (horde.isEmpty())
        {
            LanguageHandler.sendPlayersMessage(colony.getImportantMessageEntityPlayers(), ALL_BARBARIANS_KILLED_MESSAGE);
        }
        else if (horde.size() <= SMALL_HORDE_SIZE)
        {
            LanguageHandler.sendPlayersMessage(colony.getMessageEntityPlayers(), ONLY_X_BARBARIANS_LEFT_MESSAGE, horde.size());
        }
    }

    /**
     * Updates the pirates ship sailing away.
     *
     * @param colony the colony being ticked.
     */
    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        for (final Map.Entry<BlockPos, Tuple<String, Long>> entry : new HashMap<>(schematicMap).entrySet())
        {
            if (entry.getKey().equals(BlockPos.ORIGIN))
            {
                schematicMap.remove(entry.getKey());
            }
            else if (entry.getValue().getSecond() + TICKS_SECOND * SECONDS_A_MINUTE * MINUTES_A_DAY * Configurations.gameplay.daysUntilPirateshipsDespawn < colony.getWorld()
                                                                                                                                                              .getWorldTime())
            {
                // Load the backup from before spawning
                try
                {
                    InstantStructurePlacer.loadAndPlaceStructureWithRotation(colony.getWorld(),
                      new StructureName("cache", "backup", entry.getValue().getFirst()).toString() + this.colony.getID() + this.colony.getDimension() + entry.getKey(),
                      entry.getKey(),
                      0,
                      Mirror.NONE,
                      true);
                }
                catch (final NullPointerException | ArrayIndexOutOfBoundsException e)
                {
                    Log.getLogger().warn("Unable to retrieve backed up structure. This can happen when updating to a newer version!");
                }

                schematicMap.remove(entry.getKey());
                LanguageHandler.sendPlayersMessage(
                  colony.getImportantMessageEntityPlayers(),
                  PIRATES_SAILING_OFF_MESSAGE, colony.getName());
                return;
            }
        }
    }

    /**
     * Check if a certain vector matches two directions.
     *
     * @param directionX the direction x.
     * @param directionZ the direction z.
     * @param vector     the vector.
     * @return true if so.
     */
    private static boolean isInDirection(final EnumFacing directionX, final EnumFacing directionZ, final BlockPos vector)
    {
        return EnumFacing.getFacingFromVector(vector.getX(), 0, 0) == directionX && EnumFacing.getFacingFromVector(0, 0, vector.getZ()) == directionZ;
    }

    @Override
    public List<AbstractEntityMinecoloniesMob> getHorde(final WorldServer world)
    {
        final List<AbstractEntityMinecoloniesMob> raiders = new ArrayList<>();
        for (final UUID uuid : new ArrayList<>(horde))
        {
            final Entity raider = world.getEntityFromUuid(uuid);
            if (!(raider instanceof AbstractEntityMinecoloniesMob) || !raider.isEntityAlive())
            {
                horde.remove(uuid);
                sendHordeMessage();
            }
            else
            {
                raiders.add((AbstractEntityMinecoloniesMob) raider);
            }
        }
        return raiders;
    }

    @Override
    public void registerRaiderOriginSchematic(final String schematicName, final BlockPos position, final long worldTime)
    {
        schematicMap.put(position, new Tuple<>(schematicName, worldTime));
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        if (compound.hasKey(TAG_RAID_MANAGER))
        {
            schematicMap.clear();
            final NBTTagCompound raiderCompound = compound.getCompoundTag(TAG_RAID_MANAGER);
            final NBTTagList raiderTags = raiderCompound.getTagList(TAG_SCHEMATIC_LIST, Constants.NBT.TAG_COMPOUND);
            schematicMap.putAll(NBTUtils.streamCompound(raiderTags)
                                  .collect(Collectors.toMap(raiderTagCompound -> BlockPosUtil.readFromNBT(raiderTagCompound, TAG_POS),
                                    raiderTagCompound -> new Tuple<>(raiderTagCompound.getString(TAG_NAME), raiderTagCompound.getLong(TAG_TIME)))));
        }
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        final NBTTagCompound raiderCompound = new NBTTagCompound();
        @NotNull final NBTTagList raiderTagList = schematicMap.entrySet().stream()
                                                    .map(this::writeMapEntryToNBT)
                                                    .collect(NBTUtils.toNBTTagList());

        raiderCompound.setTag(TAG_SCHEMATIC_LIST, raiderTagList);
        compound.setTag(TAG_RAID_MANAGER, raiderCompound);
    }

    /**
     * Writes the map entry to NBT of the schematic map.
     *
     * @param entry the entry to write to NBT.
     * @return an NBTTAGCompound
     */
    private NBTTagCompound writeMapEntryToNBT(final Map.Entry<BlockPos, Tuple<String, Long>> entry)
    {
        final NBTTagCompound compound = new NBTTagCompound();
        BlockPosUtil.writeToNBT(compound, TAG_POS, entry.getKey());
        compound.setString(TAG_NAME, entry.getValue().getFirst());
        compound.setLong(TAG_TIME, entry.getValue().getSecond());

        return compound;
    }
}
