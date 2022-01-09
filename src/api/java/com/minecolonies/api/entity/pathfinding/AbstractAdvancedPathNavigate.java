package com.minecolonies.api.entity.pathfinding;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AbstractAdvancedPathNavigate extends GroundPathNavigation
{
    /**
     * Type of restriction.
     */
    public enum RestrictionType
    {
        NONE,
        XZ,
        XYZ
    }

    //  Parent class private members
    protected final Mob    ourEntity;
    @Nullable
    protected       BlockPos     destination;
    protected       double       walkSpeedFactor = 1.0D;
    @Nullable
    protected       BlockPos     originalDestination;

    /**
     * The navigators node costs
     */
    private PathingOptions pathingOptions = new PathingOptions();

    public AbstractAdvancedPathNavigate(
      final Mob entityLiving,
      final Level worldIn)
    {
        super(entityLiving, worldIn);
        this.ourEntity = mob;
    }

    /**
     * Get the destination from the path.
     *
     * @return the destination position.
     */
    @Nullable
    public BlockPos getDestination()
    {
        return destination;
    }

    /**
     * Used to path away from a position.
     *
     * @param currentPosition the position to avoid.
     * @param range the range he should move out of.
     * @param speed the speed to run at.
     * @param safeDestination if the destination is save and should be set.
     * @return the result of the pathing.
     */
    public abstract PathResult moveAwayFromXYZ(final BlockPos currentPosition, final double range, final double speed, final boolean safeDestination);

    /**
     * Try to move to a certain position.
     *
     * @param x     the x target.
     * @param y     the y target.
     * @param z     the z target.
     * @param speed the speed to walk.
     * @return the PathResult.
     */
    public abstract PathResult moveToXYZ(final double x, final double y, final double z, final double speed);

    /**
     * Used to find a water.
     *
     * @param searchRange in the range.
     * @param speed walking speed.
     * @param ponds a list of ponds.
     * @return the result of the search.
     */
    public abstract WaterPathResult moveToWater(final int searchRange, final double speed, final List<Tuple<BlockPos, BlockPos>> ponds);

    /**
     * Used to path away from a ourEntity.
     *
     * @param target        the ourEntity.
     * @param distance the distance to move to.
     * @param combatMovementSpeed    the speed to run at.
     * @return the result of the pathing.
     */
    public abstract PathResult moveAwayFromLivingEntity(final Entity target, final double distance, final double combatMovementSpeed);

    /**
     * Attempt to move to a specific pos.
     * @param position the position to move to.
     * @param speed the speed.
     * @return true if successful.
     */
    public abstract boolean tryMoveToBlockPos(final BlockPos position, final double speed);

    /**
     * Used to path towards a random pos.
     *
     * @param range the range he should move out of.
     * @param speed the speed to run at.
     * @return the result of the pathing.
     */
    public abstract PathResult moveToRandomPos(final double range, final double speed);

    /**
     * Used to path towards a random pos.
     *
     * @param range the range he should move out of.
     * @param speed the speed to run at.
     * @param pos the pos to circle around.
     * @return the result of the pathing.
     */
    public abstract PathResult moveToRandomPosAroundX(final int range, final double speed, final BlockPos pos);

    /**
     * Used to path towards a random pos within some restrictions
     *
     * @param range the range he should move out of.
     * @param speed the speed to run at.
     * @param corners the corners they can't leave.
     * @return the result of the pathing.
     */
    public abstract PathResult moveToRandomPos(final int range, final double speed, final net.minecraft.util.Tuple<BlockPos, BlockPos> corners, final RestrictionType restrictionType);

    /**
     * Used to find a tree.
     *
     * @param startRestriction the start of the restricted area.
     * @param endRestriction   the end of the restricted area.
     * @param speed            walking speed.
     * @param excludedTrees       the trees which should be cut.
     * @return the result of the search.
     */
    public abstract TreePathResult moveToTree(
      final BlockPos startRestriction,
      final BlockPos endRestriction,
      final double speed,
      final List<ItemStorage> excludedTrees,
      final int dyntreesize,
      final IColony colony);

    /**
     * Used to find a tree.
     *
     * @param range      in the range.
     * @param speed      walking speed.
     * @param excludedTrees the trees which should be cut.
     * @return the result of the search.
     */
    public abstract TreePathResult moveToTree(final int range, final double speed, final List<ItemStorage> excludedTrees, final int dyntreesize, final IColony colony);

    /**
     * Used to move a living ourEntity with a speed.
     *
     * @param e     the ourEntity.
     * @param speed the speed.
     * @return the result.
     */
    public abstract PathResult moveToLivingEntity(@NotNull final Entity e, final double speed);

    /**
     * Get the pathing options
     *
     * @return the pathing options.
     */
    public PathingOptions getPathingOptions()
    {
        return pathingOptions;
    }

    /**
     * Get the entity of this navigator
     *
     * @return mobentity
     */
    public Mob getOurEntity()
    {
        return ourEntity;
    }

    /**
     * Gets the desired to go position
     *
     * @return desired go to pos
     */
    public abstract BlockPos getDesiredPos();

    /**
     * Sets the stuck handler for this navigator
     *
     * @param stuckHandler handler to use
     */
    public abstract void setStuckHandler(final IStuckHandler stuckHandler);

    public abstract void setSwimSpeedFactor(double factor);
}
