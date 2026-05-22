package com.minecolonies.core.colony.buildings.workerbuildings;

import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.core.colony.buildings.modules.AnimalHerdingModule;
import com.minecolonies.core.colony.buildings.modules.BuildingModules;
import com.minecolonies.core.colony.buildings.modules.settings.IntSetting;
import com.minecolonies.core.colony.buildings.modules.settings.SettingKey;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.Items;

import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.SchematicTagConstants.TAG_GROUNDLEVEL;
import static com.minecolonies.api.util.constant.SchematicTagConstants.TAG_PATROL_POINT;

/**
 * Building of the stable.
 * Supports cavalry military units and the Stablemaster job.
 */
public class BuildingStable extends AbstractBuildingGuards
{

    public static final float CAVALRY_PATROL_RANGE_BOOST = 1.5f;

    /**
     * Tag for the structurize tags designating stall positions.
     */
    private final static String STALL_STRUCTURE_TAG = "stall";

    /**
     * NBT tag for the last time the guards patrolled from this stable.
     */
    private static final String NBT_LAST_PATROL_TAG    = "lastPatrolTime";

    /**
     * Setting key for the patrol interval.
     */
    public static final ISettingKey<IntSetting> PATROL_INTERVAL =
      new SettingKey<>(IntSetting.class, new ResourceLocation(com.minecolonies.api.util.constant.Constants.MOD_ID, "patrolinterval"));

    /**
     * The last time the guards patrolled from this stable.
     */
    private long lastPatrolTime = 0;

    /**
     * The last stable position used.
     */
    private int lastStable = -1;
    
    /**
     * Constructor.
     * 
     * @param colony the colony.
     * @param pos the position of the building.
     */
    public BuildingStable(@NotNull IColony colony, BlockPos pos)
    {
        super(colony, pos);
    }

    /**
     * Gets the schematic name of the building.
     *
     * @return the schematic name of the building.
     */
    @Override
    public String getSchematicName()
    {
        return ModBuildings.STABLE_ID;
    }

    /**
     * The herding module for the stable.
     */
    public static class HerdingModule extends AnimalHerdingModule
    {

        public HerdingModule()
        {
            super(ModJobs.stablemaster.get(), a -> a instanceof Horse, new ItemStorage(Items.GOLDEN_APPLE, 2));
        }
    }

    /**
     * Reads the tag positions
     */
    public List<BlockPos> stallPositions()
    {
        List<BlockPos> stallPositions = getLocationsFromTag(STALL_STRUCTURE_TAG);
        
        if (stallPositions.isEmpty())
        {
            Log.getLogger().warn("Colony {} has a stable with no stall positions (blueprint {}) at {}. Use the '" + STALL_STRUCTURE_TAG + "' tag to add some.", 
                getColony().getID(), getBlueprintPath(), getPosition());
        }

        return stallPositions;
    }

    /**
     * Gets the next stable position to use for a horse. Just keeps iterating the aviable positions, 
     * so we do not have to keep track of what horse is where.
     *
     * @return horse stable position
     */
    public BlockPos getNextStallPosition()
    {
        List<BlockPos> stallPositions = stallPositions();

        if (stallPositions.isEmpty())
        {
            return null;
        }

        lastStable++;

        if (lastStable >= stallPositions.size())
        {
            lastStable = 0;
        }

        return stallPositions.get(lastStable);
    }

    /**
     * Deserializes the compound tag and sets the last patrol time.
     * @param compound the compound tag to read from.
     */
    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);
        this.lastPatrolTime = compound.getLong(NBT_LAST_PATROL_TAG);
    }

    /**
     * Serializes the data of this building to NBT.
     * @return the serialized compound tag.
     */
    @Override
    public CompoundTag serializeNBT()
    {        
        final CompoundTag compound = super.serializeNBT();
        compound.putLong(NBT_LAST_PATROL_TAG, lastPatrolTime);
        return compound;
    }

    /**
     * Returns the task that the guards should perform when patrolling.
     * <p>
     * This can be either 'patrol', 'patrol_mine', or 'follow'.
     * <p>
     * The task is determined by the setting in the Stable Settings module.
     * @return the task to perform when patrolling
     */
    @Override
    public String getTask()
    {
        return getModule(BuildingModules.STABLE_SETTINGS).getSetting(GUARD_TASK).getValue();
    }

    /**
     * Gets the last time the guards patrolled from this stable.
     * @return the game time of when the guards last patrolled.
     */    
    public long getLastPatrolTime()
    {
        return lastPatrolTime;
    }

    /**
     * Sets the last time the guards patrolled from this stable.
     * @param lastPatrolTime the time in milliseconds since the epoch
     */
    public void setLastPatrolTime(long lastPatrolTime)
    {
        this.lastPatrolTime = lastPatrolTime;
    }

    /**
     * Returns the time in minutes since the last patrol from this stable.
     * This is based on the game time of the world.
     * @return the time in minutes since the last patrol.
     */
    public int minutesSinceLastPatrol()
    {
        long ticks = this.getColony().getWorld().getGameTime() - lastPatrolTime;
        int minutes = (int) ticks / TICKS_SECOND / 60;
        return minutes;
    }

    /**
     * Gets the patrol distance for cavalry guards assigned to this stable.
     * This range is based on the base patrol distance of guards, and is multiplied by a constant to
     * give cavalry guards a wider patrol range.
     * @return the patrol distance for cavalry guards assigned to this stable.
     */
    @Override
    public int getPatrolDistance()
    {
        int patrolDistance = super.getPatrolDistance();

        return (int) (patrolDistance * CAVALRY_PATROL_RANGE_BOOST);
    }

    /**
     * Initiate the next patrol.
     */
    @Override
    public void startPatrolNext()
    {
        if (minutesSinceLastPatrol() < getSetting(PATROL_INTERVAL).getValue())
        {
            setPatrolTimer(TICKS_SECOND * 60);
            return;
        }

        setLastPatrolTime(getColony().getWorld().getGameTime());
        super.startPatrolNext();
    }

    /**
     * Get a patrol target.
     */
    @Override
    protected BlockPos getRandomPatrolTarget()
    {
        BlockPos buildingPos = getColony().getServerBuildingManager().getRandomBuilding(cavalryPatrolFilter());

        return patrolPointForBuilding(buildingPos);
    }

    /**
     * If the building structure includes potential patrol points, pick one and use it.
     * Otherwise, use the hut (or tagged ground-level) Y and nominate one of the exterior corners.
     *
     * @param targetPos the building position to patrol.
     * @return a patrol point designated by a tag, a building corner, or the target position.
     */
    public BlockPos patrolPointForBuilding(final BlockPos targetPos)
    {
        if (targetPos == null || BlockPos.ZERO.equals(targetPos))
        {
            return null;
        }

        IBuilding targetBuilding = getColony().getServerBuildingManager().getBuilding(targetPos);

        if (targetBuilding == null)
        {
            return targetPos;
        }

        final List<BlockPos> patrolPoints = targetBuilding.getLocationsFromTag(TAG_PATROL_POINT);
        final RandomSource rand = getColony().getWorld().random;

        if (patrolPoints != null && !patrolPoints.isEmpty())
        {
            return patrolPoints.get(rand.nextInt(patrolPoints.size()));
        }

        if (targetBuilding.getParent() != null && !BlockPos.ZERO.equals(targetBuilding.getParent()))
        {
            return patrolPointForBuilding(targetBuilding.getParent());
        }

        final List<BlockPos> groundLevel = targetBuilding.getLocationsFromTag(TAG_GROUNDLEVEL);
        final int groundY =
            (groundLevel != null && !groundLevel.isEmpty()) ? groundLevel.get(0).getY() : targetBuilding.getPosition().below().getY();

        final Tuple<BlockPos, BlockPos> corners = targetBuilding.getCorners();
        if (corners == null)
        {
            final BlockPos hut = targetBuilding.getPosition();
            return new BlockPos(hut.getX(), groundY, hut.getZ());
        }

        final BlockPos a = corners.getA();
        final BlockPos b = corners.getB();

        switch (rand.nextInt(4))
        {
            case 0:
                return new BlockPos(a.getX(), groundY, a.getZ());
            case 1:
                return new BlockPos(a.getX(), groundY, b.getZ());
            case 2:
                return new BlockPos(b.getX(), groundY, b.getZ());
            default:
                return new BlockPos(b.getX(), groundY, a.getZ());
        }
    }

    /*
     * Filter for buildings that cavalry patrols.
     */
    public static Predicate<IBuilding> cavalryPatrolFilter()
    {
        return b -> b instanceof BuildingStable || b instanceof BuildingGateHouse;
    }
}
