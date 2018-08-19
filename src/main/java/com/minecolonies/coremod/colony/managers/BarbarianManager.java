package com.minecolonies.coremod.colony.managers;

import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.managers.interfaces.IBarbarianManager;
import com.minecolonies.coremod.entity.ai.mobs.barbarians.AbstractEntityBarbarian;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.ColonyConstants.*;
import static com.minecolonies.api.util.constant.Constants.HALF_A_CIRCLE;
import static com.minecolonies.api.util.constant.Constants.WHOLE_CIRCLE;
import static com.minecolonies.api.util.constant.TranslationConstants.ALL_BARBARIANS_KILLED_MESSAGE;
import static com.minecolonies.api.util.constant.TranslationConstants.ONLY_X_BARBARIANS_LEFT_MESSAGE;

public class BarbarianManager implements IBarbarianManager
{
    /**
     * Whether there will be a raid in this colony tonight.
     */
    private boolean raidTonight                             = false;

    /**
     * Whether or not the raid has been calculated for today.
     */
    private boolean raidBeenCalculated = false;

    /**
     * Whether or not this colony may have Barbarian events. (set via command)
     */
    private boolean haveBarbEvents = true;

    /**
     * Last barbarian spawnpoints.
     */
    private final List<BlockPos> lastSpawnPoints = new ArrayList<>();

    /**
     * The colony of the manager.
     */
    private final Colony colony;

    /**
     * List of barbarians registered to the colony.
     */
    private final List<UUID> horde = new ArrayList<>();

    /**
     * Creates the BarbarianManager for a colony.
     * @param colony the colony.
     */
    public BarbarianManager(final Colony colony)
    {
        this.colony = colony;
    }

    @Override
    public boolean canHaveBarbEvents()
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
    public void setCanHaveBarbEvents(final boolean canHave)
    {
        this.haveBarbEvents = canHave;
    }

    @Override
    public void addBarbarianSpawnPoint(final BlockPos pos)
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
        AbstractBuilding theBuilding = null;
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
    public void registerBarbarian(@NotNull final AbstractEntityBarbarian abstractEntityBarbarian)
    {
        this.horde.add(abstractEntityBarbarian.getUniqueID());
    }

    @Override
    public void unregisterBarbarian(@NotNull final AbstractEntityBarbarian abstractEntityBarbarian, final WorldServer world)
    {
        for(final UUID uuid : new ArrayList<>(horde))
        {
            final Entity barbarian = world.getEntityFromUuid(uuid);
            if(barbarian == null || !barbarian.isEntityAlive() || uuid.equals(abstractEntityBarbarian.getUniqueID()))
            {
                horde.remove(uuid);
            }
        }

        sendHordeMessage();
    }

    private void sendHordeMessage()
    {
        if(horde.isEmpty())
        {
            LanguageHandler.sendPlayersMessage(colony.getMessageEntityPlayers(), ALL_BARBARIANS_KILLED_MESSAGE);
        }
        else if(horde.size() <= SMALL_HORDE_SIZE)
        {
            LanguageHandler.sendPlayersMessage(colony.getMessageEntityPlayers(), ONLY_X_BARBARIANS_LEFT_MESSAGE, horde.size());
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
    public List<AbstractEntityBarbarian> getHorde(final WorldServer world)
    {
        final List<AbstractEntityBarbarian> barbarians = new ArrayList<>();
        for (final UUID uuid : new ArrayList<>(horde))
        {
            final Entity barbarian = world.getEntityFromUuid(uuid);
            if (!(barbarian instanceof AbstractEntityBarbarian) || !barbarian.isEntityAlive())
            {
                horde.remove(uuid);
                sendHordeMessage();
            }
            else
            {
                barbarians.add((AbstractEntityBarbarian) barbarian);
            }

        }
        return barbarians;
    }
}
