package com.minecolonies.api.entity.pathfinding;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.Tuple;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.Future;

public abstract class AbstractAdvancedPathNavigate extends GroundPathNavigator
{
    //  Parent class private members
    protected final MobEntity    ourEntity;
    @Nullable
    protected       BlockPos     destination;
    protected       double       walkSpeed = 1.0D;
    @Nullable
    protected       BlockPos     originalDestination;
    @Nullable
    protected       Future<Path> calculationFuture;

    /**
     * The navigators node costs
     */
    private PathingOptions pathingOptions = new PathingOptions();

    public AbstractAdvancedPathNavigate(
      final MobEntity entityLiving,
      final World worldIn)
    {
        super(entityLiving, worldIn);
        this.ourEntity = entity;
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
     * @return the result of the pathing.
     */
    public abstract PathResult moveAwayFromXYZ(final BlockPos currentPosition, final double range, final double speed);

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
    public abstract RandomPathResult moveToRandomPos(final double range, final double speed);

    /**
     * Used to find a tree.
     *
     * @param startRestriction the start of the restricted area.
     * @param endRestriction   the end of the restricted area.
     * @param speed            walking speed.
     * @param treesToCut       the trees which should be cut.
     * @return the result of the search.
     */
    public abstract TreePathResult moveToTree(
      final BlockPos startRestriction,
      final BlockPos endRestriction,
      final double speed,
      final List<ItemStorage> treesToCut,
      final IColony colony);

    /**
     * Used to find a tree.
     *
     * @param range      in the range.
     * @param speed      walking speed.
     * @param treesToCut the trees which should be cut.
     * @return the result of the search.
     */
    public abstract TreePathResult moveToTree(final int range, final double speed, final List<ItemStorage> treesToCut, final IColony colony);

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
    public MobEntity getOurEntity()
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
}
