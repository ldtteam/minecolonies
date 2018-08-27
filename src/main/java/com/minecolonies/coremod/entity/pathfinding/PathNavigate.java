package com.minecolonies.coremod.entity.pathfinding;

import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.BlockUtils;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.entity.EntityCitizen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Minecolonies async PathNavigate.
 */
public class PathNavigate extends PathNavigateGround
{
    private static final double ON_PATH_SPEED_MULTIPLIER = 1.3D;

    /**
     * The range multiplier for the lumberjack.
     */
    //private static final int RANGE_MULTIPLIER_LJ = 4;

    //  Parent class private members
    private final EntityLiving ourEntity;

    private double walkSpeed = 1.0D;

    @Nullable
    private BlockPos     destination;
    @Nullable
    private BlockPos     originalDestination;
    @Nullable
    private Future<Path> future;
    @Nullable
    private PathResult   pathResult;

    /**
     * Instantiates the navigation of an ourEntity.
     *
     * @param entity the ourEntity.
     * @param world  the world it is in.
     */
    public PathNavigate(@NotNull final EntityLiving entity, final World world)
    {
        super(entity, world);
        this.ourEntity = entity;

        this.nodeProcessor = new WalkNodeProcessor();
    }

    @Nullable
    @Override
    protected PathFinder getPathFinder()
    {
        return null;
    }

    @Override
    protected boolean canNavigate()
    {
        return true;
    }

    @NotNull
    @Override
    protected Vec3d getEntityPosition()
    {
        return this.ourEntity.getPositionVector();
    }

    @Nullable
    @Override
    public Path getPathToPos(@NotNull final BlockPos pos)
    {
        //Because this directly returns Path we can't do it async.
        return null;
    }

    @Override
    protected boolean isDirectPathBetweenPoints(final Vec3d start, final Vec3d end, final int sizeX, final int sizeY, final int sizeZ)
    {
        // TODO improve road walking. This is better in some situations, but still not great.
        return !BlockUtils.isPathBlock(world.getBlockState(new BlockPos(start.x, start.y - 1, start.z)).getBlock())
                 && super.isDirectPathBetweenPoints(start, end, sizeX, sizeY, sizeZ);
    }

    /**
     * Get the destination from the path.
     *
     * @return the destination position.
     */
    public BlockPos getDestination()
    {
        return destination;
    }

    public double getSpeed()
    {
        return walkSpeed;
    }

    @Override
    public void setSpeed(final double d)
    {
        walkSpeed = d;
    }

    @Override
    public boolean tryMoveToXYZ(final double x, final double y, final double z, final double speed)
    {
        moveToXYZ(x, y, z, speed);
        return true;
    }

    @Override
    public boolean tryMoveToEntityLiving(@NotNull final Entity e, final double speed)
    {
        return tryMoveToXYZ(e.posX, e.posY, e.posZ, speed);
    }

    /**
     * Try to move to a certain position.
     *
     * @param x     the x target.
     * @param y     the y target.
     * @param z     the z target.
     * @param speed the speed to walk.
     * @return the PathResult.
     */
    @Nullable
    public PathResult moveToXYZ(final double x, final double y, final double z, final double speed)
    {
        final int newX = MathHelper.floor(x);
        final int newY = (int) y;
        final int newZ = MathHelper.floor(z);


        if ((destination != null
               && BlockPosUtil.isEqual(destination, newX, newY, newZ))
              || (originalDestination != null
                    && BlockPosUtil.isEqual(originalDestination, newX, newY, newZ)
                    && pathResult != null
                    && pathResult.isInProgress()))
        {
            return pathResult;
        }

        @NotNull final BlockPos start = AbstractPathJob.prepareStart(ourEntity);
        @NotNull final BlockPos dest = new BlockPos(newX, newY, newZ);

        return setPathJob(
          new PathJobMoveToLocation(CompatibilityUtils.getWorld(ourEntity), start, dest, (int) getPathSearchRange()),
          dest, speed);
    }

    @Nullable
    private PathResult setPathJob(
                                   @NotNull final AbstractPathJob job,
                                   final BlockPos dest,
                                   final double speed)
    {
        clearPath();

        this.destination = dest;
        this.originalDestination = dest;
        this.walkSpeed = speed;

        future = Pathfinding.enqueue(job);
        pathResult = job.getResult();
        return pathResult;
    }

    @Override
    public boolean setPath(@Nullable final Path path, final double speed)
    {
        if (path == null)
        {
            this.currentPath = null;
            return false;
        }

        final int pathLength = path.getCurrentPathLength();
        Path tempPath = null;
        if (pathLength > 0 && !(path.getPathPointFromIndex(0) instanceof PathPointExtended))
        {
            //  Fix vanilla PathPoints to be PathPointExtended
            @NotNull final PathPointExtended[] newPoints = new PathPointExtended[pathLength];

            for (int i = 0; i < pathLength; ++i)
            {
                final PathPoint point = path.getPathPointFromIndex(i);
                newPoints[i] = new PathPointExtended(new BlockPos(point.x, point.y, point.z));
            }

            tempPath = new Path(newPoints);

            final PathPointExtended finalPoint = newPoints[pathLength - 1];
            destination = new BlockPos(finalPoint.x, finalPoint.y, finalPoint.z);
        }

        return super.setPath(tempPath == null ? path : tempPath, speed);
    }

    @Override
    public void onUpdateNavigation()
    {
        if (future != null)
        {
            if (!future.isDone())
            {
                return;
            }

            try
            {
                if (future.get() == null)
                {
                    future = null;
                    return;
                }

                setPath(future.get(), walkSpeed);

                pathResult.setPathLength(getPath().getCurrentPathLength());
                pathResult.setStatus(PathResult.Status.IN_PROGRESS_FOLLOWING);

                final PathPoint p = getPath().getFinalPathPoint();
                if (p != null && destination == null)
                {
                    destination = new BlockPos(p.x, p.y, p.z);

                    //  AbstractPathJob with no destination, did reach it's destination
                    pathResult.setPathReachesDestination(true);
                }
            }
            catch (@NotNull InterruptedException | ExecutionException e)
            {
                Log.getLogger().catching(e);
            }

            future = null;
        }

        int oldIndex = this.noPath() ? 0 : this.getPath().getCurrentPathIndex();
        super.onUpdateNavigation();

        //  Ladder Workaround
        if (!this.noPath())
        {
            @NotNull final PathPointExtended pEx = (PathPointExtended) this.getPath().getPathPointFromIndex(this.getPath().getCurrentPathIndex());

            if (pEx.isOnLadder())
            {
                final Vec3d vec3 = this.getPath().getPosition(this.ourEntity);

                if (vec3.squareDistanceTo(ourEntity.posX, vec3.y, ourEntity.posZ) < Math.random() * 0.1)
                {
                    //This way he is less nervous and gets up the ladder
                    double newSpeed = 0.05;
                    switch (pEx.getLadderFacing())
                    {
                        //  Any of these values is climbing, so adjust our direction of travel towards the ladder
                        case NORTH:
                            vec3.add(0, 0, 1);
                            break;
                        case SOUTH:
                            vec3.add(0, 0, -1);
                            break;
                        case WEST:
                            vec3.add(1, 0, 0);
                            break;
                        case EAST:
                            vec3.add(-1, 0, 0);
                            break;
                        //  Any other value is going down, so lets not move at all
                        default:
                            newSpeed = 0;
                            break;
                    }

                    this.ourEntity.getMoveHelper().setMoveTo(vec3.x, vec3.y, vec3.z, newSpeed);
                }
            }
            else if (ourEntity.isInWater())
            {
                //  Prevent shortcuts when swimming
                final int curIndex = this.getPath().getCurrentPathIndex();
                if (curIndex > 0
                      && (curIndex + 1) < this.getPath().getCurrentPathLength()
                      && this.getPath().getPathPointFromIndex(curIndex - 1).y != pEx.y)
                {
                    //  Work around the initial 'spin back' when dropping into water
                    oldIndex = curIndex + 1;
                }

                this.getPath().setCurrentPathIndex(oldIndex);

                Vec3d vec3d = this.getPath().getPosition(this.ourEntity);

                if (vec3d.squareDistanceTo(new Vec3d(ourEntity.posX, vec3d.y, ourEntity.posZ)) < 0.1
                      && Math.abs(ourEntity.posY - vec3d.y) < 0.5)
                {
                    this.getPath().setCurrentPathIndex(this.getPath().getCurrentPathIndex() + 1);
                    if (this.noPath())
                    {
                        return;
                    }

                    vec3d = this.getPath().getPosition(this.ourEntity);
                }

                this.ourEntity.getMoveHelper().setMoveTo(vec3d.x, vec3d.y, vec3d.z, walkSpeed);
            }
            else
            {
                if (BlockUtils.isPathBlock(world.getBlockState(ourEntity.getPosition().down()).getBlock()))
                {
                    speed = ON_PATH_SPEED_MULTIPLIER * walkSpeed;
                }
                else
                {
                    speed = walkSpeed;
                }
            }
        }

        if (pathResult != null && noPath())
        {
            pathResult.setStatus(PathResult.Status.COMPLETE);
            pathResult = null;
        }
    }

    @Override
    protected void pathFollow()
    {
        final int curNode = currentPath.getCurrentPathIndex();
        final int curNodeNext = curNode + 1;
        if (curNodeNext < currentPath.getCurrentPathLength())
        {
            final PathPointExtended pEx = (PathPointExtended) currentPath.getPathPointFromIndex(curNode);
            final PathPointExtended pExNext = (PathPointExtended) currentPath.getPathPointFromIndex(curNodeNext);

            //  If current node is bottom of a ladder, then stay on this node until
            //  the ourEntity reaches the bottom, otherwise they will try to head out early
            if (pEx.isOnLadder() && pEx.getLadderFacing() == EnumFacing.DOWN
                  && !pExNext.isOnLadder())
            {
                final Vec3d vec3 = getEntityPosition();
                if ((vec3.y - (double) pEx.y) < 0.001)
                {
                    this.currentPath.setCurrentPathIndex(curNodeNext);
                }

                this.checkForStuck(vec3);
                return;
            }
        }

        super.pathFollow();
    }

    /**
     * If null path or reached the end.
     */
    @Override
    public boolean noPath()
    {
        return future == null && super.noPath();
    }

    @Override
    public void clearPath()
    {
        if (future != null)
        {
            future.cancel(true);
            future = null;
        }

        if (pathResult != null)
        {
            pathResult.setStatus(PathResult.Status.CANCELLED);
            pathResult = null;
        }

        destination = null;
        super.clearPath();
    }

    /**
     * Used to find a tree.
     *
     * @param range      in the range.
     * @param speed      walking speed.
     * @param treesToCut the trees which should be cut.
     * @return the result of the search.
     */
    public PathJobFindTree.TreePathResult moveToTree(final int range, final double speed, final List<ItemStorage> treesToCut, final Colony colony)
    {
        @NotNull BlockPos start = AbstractPathJob.prepareStart(ourEntity);
        final BlockPos buildingPos = ((EntityCitizen) entity).getCitizenColonyHandler().getWorkBuilding().getLocation();

        if (BlockPosUtil.getDistance2D(buildingPos, entity.getPosition()) > range * 4)
        {
            start = buildingPos;
        }

        return (PathJobFindTree.TreePathResult) setPathJob(
          new PathJobFindTree(CompatibilityUtils.getWorld(entity), start, buildingPos, range, treesToCut, colony), null, speed);
    }

    /**
     * Used to find a water.
     *
     * @param range in the range.
     * @param speed walking speed.
     * @param ponds a list of ponds.
     * @return the result of the search.
     */
    @Nullable
    public PathJobFindWater.WaterPathResult moveToWater(final int range, final double speed, final List<BlockPos> ponds)
    {
        @NotNull final BlockPos start = AbstractPathJob.prepareStart(ourEntity);
        return (PathJobFindWater.WaterPathResult) setPathJob(
          new PathJobFindWater(CompatibilityUtils.getWorld(ourEntity), start, ((EntityCitizen) ourEntity).getCitizenColonyHandler().getWorkBuilding().getLocation(), range, ponds), null, speed);
    }

    /**
     * Used to move a living ourEntity with a speed.
     *
     * @param e     the ourEntity.
     * @param speed the speed.
     * @return the result.
     */
    @Nullable
    public PathResult moveToEntityLiving(@NotNull final Entity e, final double speed)
    {
        return moveToXYZ(e.posX, e.posY, e.posZ, speed);
    }

    /**
     * Used to path away from a ourEntity.
     *
     * @param e        the ourEntity.
     * @param distance the distance to move to.
     * @param speed    the speed to run at.
     * @return the result of the pathing.
     */
    @Nullable
    public PathResult moveAwayFromEntityLiving(@NotNull final Entity e, final double distance, final double speed)
    {
        return moveAwayFromXYZ(e.getPosition(), distance, speed);
    }

    /**
     * Used to path away from a position.
     *
     * @param avoid the position to avoid.
     * @param range the range he should move out of.
     * @param speed the speed to run at.
     * @return the result of the pathing.
     */
    @Nullable
    public PathResult moveAwayFromXYZ(final BlockPos avoid, final double range, final double speed)
    {
        @NotNull final BlockPos start = AbstractPathJob.prepareStart(ourEntity);

        return setPathJob(
          new PathJobMoveAwayFromLocation(CompatibilityUtils.getWorld(ourEntity), start, avoid, (int) range, (int) getPathSearchRange()),
          null, speed);
    }
}
