package com.minecolonies.core.entity.pathfinding.pathjobs;

import com.ldtteam.domumornamentum.block.decorative.FloatingCarpetBlock;
import com.ldtteam.domumornamentum.block.decorative.PanelBlock;
import com.minecolonies.api.blocks.decorative.AbstractBlockMinecoloniesConstructionTape;
import com.minecolonies.api.blocks.huts.AbstractBlockMinecoloniesDefault;
import com.minecolonies.api.entity.pathfinding.*;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.ShapeUtil;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.Network;
import com.minecolonies.core.blocks.BlockDecorationController;
import com.minecolonies.core.entity.pathfinding.CachingBlockLookup;
import com.minecolonies.core.entity.pathfinding.ChunkCache;
import com.minecolonies.core.entity.pathfinding.MNode;
import com.minecolonies.core.entity.pathfinding.PathPointExtended;
import com.minecolonies.core.network.messages.client.SyncPathMessage;
import com.minecolonies.core.network.messages.client.SyncPathReachedMessage;
import com.minecolonies.core.util.WorkerUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.Callable;

import static com.minecolonies.api.util.constant.PathingConstants.*;

/**
 * Abstract class for Jobs that run in the multithreaded path finder.
 */
public abstract class AbstractPathJob implements Callable<Path>, IPathJob
{
    /**
     * Start position to path from.
     */
    @NotNull
    protected final BlockPos start;

    /**
     * End position trying to reach.
     */
    protected BlockPos end = null;

    /**
     * The pathing cache.
     */
    @NotNull
    protected final LevelReader world;

    /**
     * Cached block lookup
     */
    protected CachingBlockLookup cachedBlockLookup;

    /**
     * The result of the path calculation.
     */
    protected final PathResult result;

    /**
     * Max range used to calculate the number of nodes we visit (square of maxrange).
     */
    protected int maxRange;

    /**
     * Queue of all open nodes.
     */
    private Queue<MNode> nodesOpen = new PriorityQueue<>(500);

    /**
     * Queue of all the visited nodes.
     */
    private Int2ObjectOpenHashMap<MNode> nodesVisited = new Int2ObjectOpenHashMap<>();

    //  Debug Rendering
    protected     boolean    debugDrawEnabled     = false;
    @Nullable
    protected     Set<MNode> debugNodesVisited    = new HashSet<>();
    @Nullable
    protected     Set<MNode> debugNodesNotVisited = new HashSet<>();
    @Nullable
    protected     Set<MNode> debugNodesPath       = new HashSet<>();
    //  May be faster, but can produce strange results
    private final boolean    allowJumpPointSearchTypeWalk;
    private       int        totalNodesAdded      = 0;
    private       int        totalNodesVisited    = 0;

    /**
     * Which citizens are being tracked by which players.
     */
    public static final Map<Player, UUID> trackingMap = new HashMap<>();

    /**
     * Type of restriction.
     */
    private final AbstractAdvancedPathNavigate.RestrictionType restrictionType;

    /**
     * Are xz restrictions hard or soft.
     */
    private final boolean hardXzRestriction;

    /**
     * The cost values for certain nodes.
     */
    private PathingOptions pathingOptions = new PathingOptions();

    /**
     * The restriction parameters
     */
    private int maxX;
    private int minX;
    private int maxZ;
    private int minZ;
    private int maxY;
    private int minY;

    /**
     * The entity this job belongs to.
     */
    protected WeakReference<LivingEntity> entity;

    /**
     * Mutable pos used ot retrieve world info directly
     */
    protected BlockPos.MutableBlockPos tempWorldPos = new BlockPos.MutableBlockPos();

    /**
     * AbstractPathJob constructor.
     *
     * @param world  the world within which to path.
     * @param start  the start position from which to path from.
     * @param end    the end position to path to.
     * @param range  maximum path range.
     * @param entity the entity.
     */
    public AbstractPathJob(final Level world, @NotNull final BlockPos start, @NotNull final BlockPos end, final int range, final LivingEntity entity)
    {
        this(world, start, end, range, new PathResult<AbstractPathJob>(), entity);
    }

    /**
     * AbstractPathJob constructor.
     *
     * @param world  the world within which to path.
     * @param start  the start position from which to path from.
     * @param end    the end position to path to
     * @param range  maximum path range.
     * @param result path result.
     * @param entity the entity.
     * @see AbstractPathJob#AbstractPathJob(Level, BlockPos, BlockPos, int, LivingEntity)
     */
    public AbstractPathJob(final Level world, @NotNull final BlockPos start, @NotNull final BlockPos end, final int range, final PathResult result, final LivingEntity entity)
    {
        final int minX = Math.min(start.getX(), end.getX()) - (range / 2);
        final int minZ = Math.min(start.getZ(), end.getZ()) - (range / 2);
        final int maxX = Math.max(start.getX(), end.getX()) + (range / 2);
        final int maxZ = Math.max(start.getZ(), end.getZ()) + (range / 2);

        this.restrictionType = AbstractAdvancedPathNavigate.RestrictionType.NONE;
        this.hardXzRestriction = false;

        this.world = new ChunkCache(world, new BlockPos(minX, world.getMinBuildHeight(), minZ), new BlockPos(maxX, world.getMaxBuildHeight(), maxZ), range, world.dimensionType());

        this.start = new BlockPos(start);
        this.end = end;

        cachedBlockLookup = new CachingBlockLookup(start, this.world);
        this.maxRange = range;

        this.result = result;
        result.setJob(this);
        allowJumpPointSearchTypeWalk = false;

        if (entity != null && trackingMap.containsValue(entity.getUUID()))
        {
            debugDrawEnabled = true;
            debugNodesVisited = new HashSet<>();
            debugNodesNotVisited = new HashSet<>();
            debugNodesPath = new HashSet<>();
        }
        this.entity = new WeakReference<>(entity);
    }

    /**
     * AbstractPathJob constructor.
     *
     * @param world            the world within which to path.
     * @param start            the start position from which to path from.
     * @param startRestriction start of restricted area.
     * @param endRestriction   end of restricted area.
     * @param range            range^2 is used as cap for visited node count
     * @param hardRestriction  if <code>true</code> start has to be inside the restricted area (otherwise the search immidiately finishes) -
     *                         node visits outside the area are not allowed, isAtDestination is called on every node, if <code>false</code>
     *                         restricted area only applies to calling isAtDestination thus searching outside area is allowed
     * @param result           path result.
     * @param entity           the entity.
     */
    public AbstractPathJob(
      final Level world,
      final BlockPos start,
      final BlockPos startRestriction,
      final BlockPos endRestriction,
      final int range,
      final boolean hardRestriction,
      final PathResult result,
      final LivingEntity entity,
      final AbstractAdvancedPathNavigate.RestrictionType restrictionType)
    {
        this(world, start, startRestriction, endRestriction, range, Vec3i.ZERO, hardRestriction, result, entity, restrictionType);
    }

    /**
     * AbstractPathJob constructor.
     *
     * @param world            the world within which to path.
     * @param start            the start position from which to path from.
     * @param startRestriction start of restricted area.
     * @param endRestriction   end of restricted area.
     * @param range            range^2 is used as cap for visited node count
     * @param grow             adjustment for restricted area, can be either shrink or grow, is applied in both of xz directions after
     *                         getting min/max box values
     * @param hardRestriction  if <code>true</code> start has to be inside the restricted area (otherwise the search immidiately finishes) -
     *                         node visits outside the area are not allowed, isAtDestination is called on every node, if <code>false</code>
     *                         restricted area only applies to calling isAtDestination thus searching outside area is allowed
     * @param result           path result.
     * @param entity           the entity.
     */
    public AbstractPathJob(
      final Level world,
      @NotNull final BlockPos start,
      final BlockPos startRestriction,
      final BlockPos endRestriction,
      final int range,
      final Vec3i grow,
      final boolean hardRestriction,
      final PathResult result,
      final LivingEntity entity,
      final AbstractAdvancedPathNavigate.RestrictionType restrictionType)
    {
        this.minX = Math.min(startRestriction.getX(), endRestriction.getX()) - grow.getX();
        this.minZ = Math.min(startRestriction.getZ(), endRestriction.getZ()) - grow.getZ();
        this.maxX = Math.max(startRestriction.getX(), endRestriction.getX()) + grow.getX();
        this.maxZ = Math.max(startRestriction.getZ(), endRestriction.getZ()) + grow.getZ();
        this.minY = Math.min(startRestriction.getY(), endRestriction.getY()) - grow.getY();
        this.maxY = Math.max(startRestriction.getY(), endRestriction.getY()) + grow.getY();

        this.restrictionType = restrictionType;
        this.hardXzRestriction = hardRestriction;

        this.world = new ChunkCache(world, new BlockPos(minX, world.getMinBuildHeight(), minZ), new BlockPos(maxX, world.getMaxBuildHeight(), maxZ), range, world.dimensionType());

        this.start = start;
        cachedBlockLookup = new CachingBlockLookup(start, this.world);
        this.maxRange = range;

        this.result = result;
        result.setJob(this);

        this.allowJumpPointSearchTypeWalk = false;

        if (entity != null && trackingMap.containsValue(entity.getUUID()))
        {
            debugDrawEnabled = true;
            debugNodesVisited = new HashSet<>();
            debugNodesNotVisited = new HashSet<>();
            debugNodesPath = new HashSet<>();
        }
        this.entity = new WeakReference<>(entity);
    }

    /**
     * Sync the path of a given mob to the client.
     *
     * @param mob the tracked mob.
     */
    public void synchToClient(final LivingEntity mob)
    {
        for (final Iterator<Map.Entry<Player, UUID>> iter = trackingMap.entrySet().iterator(); iter.hasNext(); )
        {
            final Map.Entry<Player, UUID> entry = iter.next();
            if (entry.getKey().isRemoved())
            {
                iter.remove();
            }
            else if (entry.getValue().equals(mob.getUUID()))
            {
                Network.getNetwork().sendToPlayer(new SyncPathMessage(debugNodesVisited, debugNodesNotVisited, debugNodesPath), (ServerPlayer) entry.getKey());
            }
        }
    }

    /**
     * Set the set of reached blocks to the client.
     *
     * @param reached the reached blocks.
     * @param mob     the tracked mob.
     */
    public static void synchToClient(final HashSet<BlockPos> reached, final Mob mob)
    {
        if (reached.isEmpty())
        {
            return;
        }

        for (final Map.Entry<Player, UUID> entry : trackingMap.entrySet())
        {
            if (entry.getValue().equals(mob.getUUID()))
            {
                Network.getNetwork().sendToPlayer(new SyncPathReachedMessage(reached), (ServerPlayer) entry.getKey());
            }
        }
    }

    protected boolean onLadderGoingUp(@NotNull final MNode currentNode, @NotNull final int dX, final int dY, final int dZ)
    {
        return currentNode.isLadder() && (dY >= 0 || dX != 0 || dZ != 0);
    }

    /**
     * @return true if a restricted area has been defined.
     */
    public boolean isRestricted()
    {
        return restrictionType != AbstractAdvancedPathNavigate.RestrictionType.NONE;
    }

    /**
     * Generates a good path starting location for the entity to path from, correcting for the following conditions. - Being in water: pathfinding in water occurs along the
     * surface; adjusts position to surface. - Being in a fence space: finds correct adjacent position which is not a fence space, to prevent starting path. from within the fence
     * block.
     *
     * @param entity Entity for the pathfinding operation.
     * @return ChunkCoordinates for starting location.
     */
    public static BlockPos prepareStart(@NotNull final LivingEntity entity)
    {
        @NotNull BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(Mth.floor(entity.getX()),
          Mth.floor(entity.getY()),
          Mth.floor(entity.getZ()));
        final Level level = entity.level;
        BlockState bs = level.getBlockState(pos);
        // 1 Up when we're standing within this collision shape
        final VoxelShape collisionShape = bs.getCollisionShape(level, pos);
        final boolean isFineToStandIn = canStandInSolidBlock(bs);
        if (bs.blocksMotion() && !isFineToStandIn && collisionShape.max(Direction.Axis.Y) > 0)
        {
            final double relPosX = Math.abs(entity.getX() % 1);
            final double relPosZ = Math.abs(entity.getZ() % 1);

            for (final AABB box : collisionShape.toAabbs())
            {
                if (relPosX >= box.minX && relPosX <= box.maxX
                      && relPosZ >= box.minZ && relPosZ <= box.maxZ
                      && box.maxY > 0)
                {
                    pos.set(pos.getX(), pos.getY() + 1, pos.getZ());
                    bs = level.getBlockState(pos);
                    break;
                }
            }
        }

        BlockState down = level.getBlockState(pos.below());
        while (canStandInSolidBlock(bs) && canStandInSolidBlock(down) && !down.getBlock().isLadder(down, level, pos.below(), entity) && down.getFluidState().isEmpty())
        {
            pos.move(Direction.DOWN, 1);
            bs = down;
            down = level.getBlockState(pos.below());

            if (pos.getY() < entity.getCommandSenderWorld().getMinBuildHeight())
            {
                return entity.blockPosition();
            }
        }

        final Block b = bs.getBlock();

        if (entity.isInWater())
        {
            while (!bs.getFluidState().isEmpty())
            {
                pos.set(pos.getX(), pos.getY() + 1, pos.getZ());
                bs = level.getBlockState(pos);
            }
        }
        else if (b instanceof FenceBlock || b instanceof WallBlock || b instanceof AbstractBlockMinecoloniesDefault || (bs.blocksMotion() && !canStandInSolidBlock(bs)))
        {
            //Push away from fence
            final double dX = entity.getX() - Math.floor(entity.getX());
            final double dZ = entity.getZ() - Math.floor(entity.getZ());

            if (dX < HALF_A_BLOCK && dZ < HALF_A_BLOCK)
            {
                if (dZ < dX)
                {
                    pos.set(pos.getX(), pos.getY(), pos.getZ() - 1);
                }
                else
                {
                    pos.set(pos.getX() - 1, pos.getY(), pos.getZ());
                }
            }
            else
            {
                if (dZ > dX)
                {
                    pos.set(pos.getX(), pos.getY(), pos.getZ() + 1);
                }
                else
                {
                    pos.set(pos.getX() + 1, pos.getY(), pos.getZ());
                }
            }
        }

        return pos.immutable();
    }

    /**
     * Check if this a valid state to stand in.
     *
     * @param state the state to check.
     * @return true if so.
     */
    private static boolean canStandInSolidBlock(final BlockState state)
    {
        return state.getBlock() instanceof DoorBlock || state.getBlock() instanceof TrapDoorBlock || (state.getBlock() instanceof PanelBlock && state.getValue(PanelBlock.OPEN))
                 || !state.getBlock().properties.hasCollision;
    }

    /**
     * Sets the direction where the ladder is facing.
     *
     * @param world the world in.
     * @param pos   the position.
     * @param p     the path.
     */
    private static void setLadderFacing(@NotNull final LevelReader world, final BlockPos pos, @NotNull final PathPointExtended p)
    {
        final BlockState state = world.getBlockState(pos);
        final Block block = state.getBlock();
        if (block instanceof VineBlock)
        {
            if (state.getValue(VineBlock.SOUTH))
            {
                p.setLadderFacing(Direction.NORTH);
            }
            else if (state.getValue(VineBlock.WEST))
            {
                p.setLadderFacing(Direction.EAST);
            }
            else if (state.getValue(VineBlock.NORTH))
            {
                p.setLadderFacing(Direction.SOUTH);
            }
            else if (state.getValue(VineBlock.EAST))
            {
                p.setLadderFacing(Direction.WEST);
            }
        }
        else if (block instanceof LadderBlock)
        {
            p.setLadderFacing(state.getValue(LadderBlock.FACING));
        }
        else
        {
            p.setLadderFacing(Direction.UP);
        }
    }

    /**
     * Checks if entity is on a ladder.
     *
     * @param current the path node.
     * @param next    the next path point.
     * @return true if on a ladder.
     */
    private static boolean onALadder(@NotNull final MNode current, @Nullable final MNode next)
    {
        return next != null && current.isLadder() && (current.x == next.x && current.z == next.z);
    }

    /**
     * Generate a pseudo-unique key for identifying a given node by it's coordinates Encodes the lowest 12 bits of x,z and all useful bits of y. This creates unique keys for all
     * blocks within a 4096x256x4096 cube, which is FAR bigger volume than one should attempt to pathfind within
     *
     * @return key for node in map
     */
    private static int computeNodeKey(@NotNull final int x, final int y, final int z)
    {
        return ((x & 0xFFF) << SHIFT_X_BY)
                 | ((y & 0xFF) << SHIFT_Y_BY)
                 | (z & 0xFFF);
    }

    /**
     * Compute the cost (immediate 'g' value) of moving from the parent space to the new space.
     *
     * @param isSwimming true is the current node would require the citizen to swim.
     * @param onPath     checks if the node is on a path.
     * @param onRails    checks if the node is a rail block.
     * @param railsExit  the exit of the rails.
     * @param swimStart  if its the swim start.
     * @return cost to move from the parent to the new position.
     */
    protected double computeCost(
      final int dX, final int dY, final int dZ,
      final boolean isSwimming,
      final boolean onPath,
      final boolean onRails,
      final boolean railsExit,
      final boolean swimStart,
      final boolean corner,
      final BlockState state,
      final int x, final int y, final int z)
    {
        double cost = Math.abs(dX) + Math.abs(dZ) + Math.abs(dY);

        if (cachedBlockLookup.getBlockState(x, y, z).getBlock() == Blocks.CAVE_AIR)
        {
            cost *= pathingOptions.caveAirCost;
        }

        if (dY != 0 && !(cachedBlockLookup.getBlockState(x, y - 1, z).is(BlockTags.STAIRS)))
        {
            if (dY > 0)
            {
                cost *= pathingOptions.jumpCost * 5;
            }
            else if (pathingOptions.dropCost != 1)
            {
                cost *= pathingOptions.dropCost * Math.abs(dY * dY);
            }
        }

        if (cachedBlockLookup.getBlockState(x, y, z).hasProperty(BlockStateProperties.OPEN))
        {
            cost *= pathingOptions.traverseToggleAbleCost;
        }

        if (onPath)
        {
            cost *= pathingOptions.onPathCost;
        }

        if (onRails)
        {
            cost *= pathingOptions.onRailCost;
        }

        if (railsExit)
        {
            cost *= pathingOptions.railsExitCost;
        }

        if (state.is(BlockTags.CLIMBABLE) && !(state.getBlock() instanceof LadderBlock))
        {
            cost *= pathingOptions.nonLadderClimbableCost;
        }

        if (isSwimming)
        {
            if (swimStart)
            {
                cost *= pathingOptions.swimCostEnter;
            }
            else
            {
                cost *= pathingOptions.swimCost;
            }
        }

        return cost;
    }

    private static boolean nodeClosed(@Nullable final MNode node)
    {
        return node != null && node.isClosed();
    }

    private boolean calculateSwimming(@NotNull final LevelReader world, final int x, final int y, final int z, @Nullable final MNode node)
    {
        return (node == null) ? SurfaceType.isWater(world, tempWorldPos.set(x, y - 1, z)) : node.isSwimming();
    }

    @Override
    public PathResult getResult()
    {
        return result;
    }

    @Override
    public PathingOptions getPathingOptions()
    {
        return pathingOptions;
    }

    /**
     * Callable method for initiating asynchronous task.
     *
     * @return path to follow or null.
     */
    @Override
    public final Path call()
    {
        try
        {
            return search();
        }
        catch (final Exception e)
        {
            // Log everything, so exceptions of the pathfinding-thread show in Log
            Log.getLogger().warn("Pathfinding Exception", e);
        }

        return null;
    }

    /**
     * Perform the search.
     *
     * @return Path of a path to the given location, a best-effort, or null.
     */
    @Nullable
    protected Path search()
    {
        MNode bestNode = getAndSetupStartNode();

        double bestNodeResultScore = Double.MAX_VALUE;

        while (!nodesOpen.isEmpty())
        {
            if (Thread.currentThread().isInterrupted())
            {
                return null;
            }

            final MNode currentNode = nodesOpen.poll();

            totalNodesVisited++;

            // Limiting max amount of nodes mapped
            if (totalNodesVisited > maxRange * maxRange)
            {
                break;
            }
            currentNode.setCounterVisited(totalNodesVisited);

            handleDebugOptions(currentNode);
            currentNode.setClosed();

            final boolean isViablePosition = isInRestrictedArea(currentNode.x, currentNode.y, currentNode.z)
                                               && SurfaceType.getSurfaceType(world,
              cachedBlockLookup.getBlockState(currentNode.x, currentNode.y - 1, currentNode.z),
              tempWorldPos.set(currentNode.x, currentNode.y - 1, currentNode.z))
                                                    == SurfaceType.WALKABLE;
            if (isViablePosition && isAtDestination(currentNode))
            {
                bestNode = currentNode;
                result.setPathReachesDestination(true);
                break;
            }

            //  If this is the closest node to our destination, treat it as our best node
            final double nodeResultScore =
              getNodeResultScore(currentNode);
            if (isViablePosition && nodeResultScore < bestNodeResultScore && !currentNode.isCornerNode())
            {
                bestNode = currentNode;
                bestNodeResultScore = nodeResultScore;
            }

            // if xz soft-restricted we can walk outside the restricted area to be able to find ways around back to the area
            if (!hardXzRestriction || isViablePosition)
            {
                walkCurrentNode(currentNode);
            }
        }

        @NotNull final Path path = finalizePath(bestNode);

        return path;
    }

    private void handleDebugOptions(final MNode currentNode)
    {
        if (debugDrawEnabled)
        {
            addNodeToDebug(currentNode);
        }

        if (MineColonies.getConfig().getServer().pathfindingDebugVerbosity.get() == DEBUG_VERBOSITY_FULL)
        {
            Log.getLogger().info(String.format("Examining node [%d,%d,%d] ; g=%f ; f=%f",
              currentNode.x, currentNode.y, currentNode.z, currentNode.getCost(), currentNode.getScore()));
        }
    }

    private void addNodeToDebug(final MNode currentNode)
    {
        debugNodesNotVisited.remove(currentNode);
        debugNodesVisited.add(currentNode);
    }

    private void addPathNodeToDebug(final MNode node)
    {
        debugNodesVisited.remove(node);
        debugNodesPath.add(node);
    }

    private void walkCurrentNode(@NotNull final MNode currentNode)
    {
        cachedBlockLookup.resetToNextPos(currentNode.x, currentNode.y, currentNode.z);

        int dX = 0;
        int dY = 0;
        int dZ = 0;

        if (currentNode.parent != null)
        {
            dX = currentNode.x - currentNode.parent.x;
            dY = currentNode.y - currentNode.parent.y;
            dZ = currentNode.z - currentNode.parent.z;
        }

        //  On a ladder, we can go 1 straight-up
        if (onLadderGoingUp(currentNode, dX, dY, dZ))
        {
            walk(currentNode, 0, 1, 0);
        }

        //  We can also go down 1, if the lower block is a ladder
        if (onLadderGoingDown(currentNode, dX, dY, dZ))
        {
            walk(currentNode, 0, -1, 0);
        }

        // Only explore downwards when dropping
        if ((currentNode.parent == null || !(currentNode.parent.x == currentNode.x && currentNode.parent.y == currentNode.y - 1 && currentNode.parent.z == currentNode.z))
              && currentNode.isCornerNode())
        {
            walk(currentNode, 0, -1, 0);
            return;
        }

        // Walk downwards node if passable
        if (isPassable(currentNode.x, currentNode.y - 1, currentNode.z, false, currentNode.parent) && (!currentNode.isSwimming() && isLiquid(cachedBlockLookup.getBlockState(
          currentNode.x,
          currentNode.y - 1,
          currentNode.z))))
        {
            walk(currentNode, 0, -1, 0);
        }

        // N
        if (dZ <= 0)
        {
            walk(currentNode, 0, 0, -1);
        }

        // E
        if (dX >= 0)
        {
            walk(currentNode, 1, 0, 0);
        }

        // S
        if (dZ >= 0)
        {
            walk(currentNode, 0, 0, 1);
        }

        // W
        if (dX <= 0)
        {
            walk(currentNode, -1, 0, 0);
        }
    }

    protected boolean onLadderGoingDown(@NotNull final MNode currentNode, final int dX, final int dY, final int dZ)
    {
        return (dY <= 0 || dX != 0 || dZ != 0) && isLadder(currentNode.x, currentNode.y - 1, currentNode.z);
    }

    @NotNull
    private MNode getAndSetupStartNode()
    {
        @NotNull final MNode startNode = new MNode(start.getX(), start.getY(), start.getZ(),
          computeHeuristic(start.getX(), start.getY(), start.getZ()));

        if (isLadder(start.getX(), start.getY(), start.getZ()))
        {
            startNode.setLadder();
        }
        else if (isLiquid(cachedBlockLookup.getBlockState(start.below())))
        {
            startNode.setSwimming();
        }

        startNode.setOnRails(pathingOptions.canUseRails() && cachedBlockLookup.getBlockState(start).getBlock() instanceof BaseRailBlock);

        nodesOpen.offer(startNode);
        nodesVisited.put(computeNodeKey(start.getX(), start.getY(), start.getZ()), startNode);

        ++totalNodesAdded;

        return startNode;
    }

    /**
     * Check if this is a liquid state for swimming.
     *
     * @param state the state to check.
     * @return true if so.
     */
    public boolean isLiquid(final BlockState state)
    {
        return state.liquid() || (!state.blocksMotion() && !state.getFluidState().isEmpty());
    }

    /**
     * Generate the path to the target node.
     *
     * @param targetNode the node to path to.
     * @return the path.
     */
    @NotNull
    private Path finalizePath(final MNode targetNode)
    {
        //  Compute length of path, since we need to allocate an array.  This is cheaper/faster than building a List
        //  and converting it.  Yes, we have targetNode.steps, but I do not want to rely on that being accurate (I might
        //  fudge that value later on for cutoff purposes
        int pathLength = 1;
        int railsLength = 0;
        @Nullable MNode node = targetNode;
        while (node.parent != null)
        {
            ++pathLength;
            if (node.isOnRails())
            {
                ++railsLength;
            }
            node = node.parent;
        }

        @NotNull final Node[] points = new Node[pathLength];
        points[0] = new PathPointExtended(new BlockPos(node.x, node.y, node.z));
        if (debugDrawEnabled)
        {
            addPathNodeToDebug(node);
        }


        @Nullable MNode nextInPath = null;
        @Nullable Node next = null;
        node = targetNode;
        while (node.parent != null)
        {
            if (debugDrawEnabled)
            {
                addPathNodeToDebug(node);
            }

            --pathLength;

            @NotNull final BlockPos pos = new BlockPos(node.x, node.y, node.z);

            if (node.isSwimming())
            {
                //  Not truly necessary but helps prevent them spinning in place at swimming nodes
                pos.offset(BLOCKPOS_DOWN);
            }

            @NotNull final PathPointExtended p = new PathPointExtended(pos);
            if (railsLength >= MineColonies.getConfig().getServer().minimumRailsToPath.get())
            {
                p.setOnRails(node.isOnRails());
                if (p.isOnRails() && (!node.parent.isOnRails() || node.parent.parent == null))
                {
                    p.setRailsEntry();
                }
                else if (p.isOnRails() && points.length > pathLength + 1)
                {
                    final PathPointExtended point = ((PathPointExtended) points[pathLength + 1]);
                    if (!point.isOnRails())
                    {
                        point.setRailsExit();
                    }
                }
            }

            //  Climbing on a ladder?
            if (nextInPath != null && onALadder(node, nextInPath))
            {
                p.setOnLadder(true);
                if (nextInPath.y > pos.getY())
                {
                    //  We only care about facing if going up
                    //In the case of BlockVines (Which does not have Direction) we have to check the metadata of the vines... bitwise...
                    setLadderFacing(world, pos, p);
                }
            }
            else if (onALadder(node, node.parent))
            {
                p.setOnLadder(true);
            }

            if (next != null)
            {
                next.cameFrom = p;
            }
            next = p;
            points[pathLength] = p;

            nextInPath = node;
            node = node.parent;
        }

        doDebugPrinting(points);

        return new Path(Arrays.asList(points), getPathTargetPos(targetNode), isAtDestination(targetNode));
    }

    /**
     * Creates the path for the given points
     *
     * @param finalNode
     * @return
     */
    protected BlockPos getPathTargetPos(final MNode finalNode)
    {
        return new BlockPos(finalNode.x, finalNode.y, finalNode.z);
    }

    /**
     * Turns on debug printing.
     *
     * @param points the points to print.
     */
    private void doDebugPrinting(@NotNull final Node[] points)
    {
        if (MineColonies.getConfig().getServer().pathfindingDebugVerbosity.get() > DEBUG_VERBOSITY_NONE)
        {
            Log.getLogger().info("Path found:");

            for (@NotNull final Node p : points)
            {
                Log.getLogger().info(String.format("Step: [%d,%d,%d]", p.x, p.y, p.z));
            }

            Log.getLogger().info(String.format("Total Nodes Visited %d / %d", totalNodesVisited, totalNodesAdded));
        }
    }

    /**
     * Compute the heuristic cost ('h' value) of a given position x,y,z.
     * <p>
     * Returning a value of 0 performs a breadth-first search. Returning a value less than actual possible cost to goal guarantees shortest path, but at computational expense.
     * Returning a value exactly equal to the cost to the goal guarantees shortest path and least expense (but generally. only works when path is straight and unblocked). Returning
     * a value greater than the actual cost to goal produces good, but not perfect paths, and is fast. Returning a very high value (such that 'h' is very high relative to 'g') then
     * only 'h' (the heuristic) matters as the search will be a very fast greedy best-first-search, ignoring cost weighting and distance.
     *
     * @return the heuristic.
     */
    protected abstract double computeHeuristic(final int x, final int y, final int z);

    /**
     * Return true if the given node is a viable final destination, and the path should generate to here.
     *
     * @param n Node to test.
     * @return true if the node is a viable destination.
     */
    protected abstract boolean isAtDestination(MNode n);

    /**
     * Compute a 'result score' for the Node; if no destination is determined, the node that had the highest 'result' score is used.
     *
     * @param n Node to test.
     * @return score for the node.
     */
    protected abstract double getNodeResultScore(MNode n);

    /**
     * "Walk" from the parent in the direction specified by the delta, determining the new x,y,z position for such a move and adding or updating a node, as appropriate.
     *
     * @param parent Node being walked from.
     * @return true if a node was added or updated when attempting to move in the given direction.
     */
    protected final boolean walk(@NotNull final MNode parent, int dX, int dY, int dZ)
    {
        int nextX = parent.x + dX;
        int nextY = parent.y + dY;
        int nextZ = parent.z + dZ;

        //  Can we traverse into this node?  Fix the y up
        final int newY = getGroundHeight(parent, nextX, nextY, nextZ);

        if (newY < world.getMinBuildHeight())
        {
            return false;
        }

        boolean corner = false;
        if (nextY != newY)
        {
            if (parent.isCornerNode() && (dX != 0 || dZ != 0))
            {
                return false;
            }

            // if the new position is above the current node, we're taking the node directly above
            if (!parent.isCornerNode() && newY - parent.y > 0 && (parent.parent == null || !equalPositions(parent.parent.x,
              parent.parent.y,
              parent.parent.z,
              parent.x,
              parent.y + newY - nextY,
              parent.z)))
            {
                dX = 0;
                dY = newY - nextY;
                dZ = 0;

                nextX = parent.x + dX;
                nextY = parent.y + dY;
                nextZ = parent.z + dZ;
                corner = true;
            }
            // If we're going down, take the air-corner before going to the lower node
            else if (!parent.isCornerNode() && newY - parent.y < 0 && (dX != 0 || dZ != 0) &&
                       (parent.parent == null || (parent.x != parent.parent.x || parent.y - 1 != parent.parent.y || parent.z != parent.parent.z)))
            {
                dY = 0;

                nextX = parent.x + dX;
                nextY = parent.y + dY;
                nextZ = parent.z + dZ;

                corner = true;
            }
            // Fix up normal y
            else
            {
                dX = 0;
                dY = newY - nextY;
                dZ = 0;

                nextY = newY;
            }
        }

        int nodeKey = computeNodeKey(nextX, nextY, nextZ);
        MNode node = nodesVisited.get(nodeKey);
        if (nodeClosed(node))
        {
            //  Early out on closed nodes (closed = expanded from)
            return false;
        }

        final boolean isSwimming = calculateSwimming(world, nextX, nextY, nextZ, node);

        if (isSwimming && !pathingOptions.canSwim())
        {
            return false;
        }

        final boolean swimStart = isSwimming && !parent.isSwimming();
        final BlockState state = cachedBlockLookup.getBlockState(nextX, nextY, nextZ);
        final boolean onRoad = WorkerUtil.isPathBlock(cachedBlockLookup.getBlockState(nextX, nextY - 1, nextZ).getBlock());
        final boolean onRails = pathingOptions.canUseRails() && cachedBlockLookup.getBlockState(nextX, corner ? nextY - 1 : nextY, nextZ).getBlock() instanceof BaseRailBlock;
        final boolean railsExit = !onRails && parent != null && parent.isOnRails();
        //  Cost may have changed due to a jump up or drop

        // TODO: cost computation is off for corners
        double stepCost = computeCost(dX, dY, dZ, isSwimming, onRoad, onRails, railsExit, swimStart, corner, state, nextX, nextY, nextZ);
        stepCost = calcAdditionalCost(stepCost, parent, nextX, nextY, nextZ, state);

        final double heuristic = computeHeuristic(nextX, nextY, nextZ);
        final double cost = parent.getCost() + stepCost;
        final double score = cost + heuristic;

        if (node == null)
        {
            node = createNode(parent, nextX, nextY, nextZ, nodeKey, isSwimming, heuristic, cost, score);
            node.setOnRails(onRails);
            node.setCornerNode(corner);
        }
        else if (updateCurrentNode(parent, node, heuristic, cost, score))
        {
            return false;
        }

        nodesOpen.offer(node);

        //  Jump Point Search-ish optimization:
        // If this node was a (heuristic-based) improvement on our parent,
        // lets go another step in the same direction...
        performJumpPointSearch(parent, dX, dY, dZ, node);

        return true;
    }

    /**
     * Calculates additional costs if needed for node
     *
     * @param stepCost
     * @param parent
     * @param state
     * @return
     */
    protected double calcAdditionalCost(final double stepCost, final MNode parent, final int x, final int y, final int z, final BlockState state)
    {
        return stepCost;
    }

    private void performJumpPointSearch(@NotNull final MNode parent, final int dX, final int dY, final int dZ, @NotNull final MNode node)
    {
        if (allowJumpPointSearchTypeWalk && node.getHeuristic() <= parent.getHeuristic())
        {
            walk(node, dX, dY, dZ);
        }
    }

    @NotNull
    private MNode createNode(
      final MNode parent, final int x, final int y, final int z, final int nodeKey,
      final boolean isSwimming, final double heuristic, final double cost, final double score)
    {
        final MNode node;
        node = new MNode(parent, x, y, z, cost, heuristic, score);
        nodesVisited.put(nodeKey, node);
        if (debugDrawEnabled)
        {
            debugNodesNotVisited.add(node);
        }

        if (isLadder(x, y, z))
        {
            node.setLadder();
        }

        if (isSwimming)
        {
            node.setSwimming();
        }

        totalNodesAdded++;
        node.setCounterAdded(totalNodesAdded);
        return node;
    }

    private boolean updateCurrentNode(@NotNull final MNode parent, @NotNull final MNode node, final double heuristic, final double cost, final double score)
    {
        //  This node already exists
        if (score >= node.getScore())
        {
            return true;
        }

        if (!nodesOpen.remove(node))
        {
            return true;
        }

        node.parent = parent;
        node.setSteps(parent.getSteps() + 1);
        node.setCost(cost);
        node.setHeuristic(heuristic);
        node.setScore(score);
        return false;
    }

    /**
     * Get the height of the ground at the given x,z coordinate, within 1 step of y.
     *
     * @param parent parent node.
     * @return y height of first open, viable block above ground, or -1 if blocked or too far a drop.
     */
    protected int getGroundHeight(final MNode parent, final int x, final int y, final int z)
    {
        if (isLiquid(cachedBlockLookup.getBlockState(x, y + 1, z)))
        {
            return -100;
        }
        //  Check (y+1) first, as it's always needed, either for the upper body (level),
        //  lower body (headroom drop) or lower body (jump up)
        if (checkHeadBlock(parent, x, y, z))
        {
            return handleTargetNotPassable(parent, x, y + 1, z, cachedBlockLookup.getBlockState(x, y + 1, z));
        }

        //  Now check the block we want to move to
        final BlockState target = cachedBlockLookup.getBlockState(x, y, z);
        if (!isPassable(target, x, y, z, parent, false))
        {
            return handleTargetNotPassable(parent, x, y, z, target);
        }

        //  Do we have something to stand on in the target space?
        final BlockState below = cachedBlockLookup.getBlockState(x, y - 1, z);
        final SurfaceType walkability = SurfaceType.getSurfaceType(world, below, tempWorldPos.set(x, y - 1, z));
        if (walkability == SurfaceType.WALKABLE)
        {
            //  Level path
            return y;
        }
        else if (walkability == SurfaceType.NOT_PASSABLE)
        {
            return -100;
        }

        return handleNotStanding(parent, x, y, z, below);
    }

    private int handleNotStanding(@Nullable final MNode parent, final int x, final int y, final int z, @NotNull final BlockState below)
    {
        final boolean isSwimming = parent != null && parent.isSwimming();

        if (isLiquid(below))
        {
            return handleInLiquid(x, y, z, below, isSwimming);
        }

        if (isLadder(below, x, y - 1, z))
        {
            return y;
        }

        return checkDrop(parent, x, y, z, isSwimming);
    }

    private int checkDrop(@Nullable final MNode parent, final int x, final int y, final int z, final boolean isSwimming)
    {
        final boolean canDrop = parent != null && !parent.isLadder();
        //  Nothing to stand on
        if (!canDrop || ((parent.x != x || parent.z != z) && isPassable(parent.x, parent.y - 1, parent.z, false, parent)
                           &&
                           SurfaceType.getSurfaceType(world, cachedBlockLookup.getBlockState(parent.x, parent.y - 1, parent.z), tempWorldPos.set(parent.x, parent.y - 1, parent.z))
                             == SurfaceType.DROPABLE))
        {
            return -100;
        }

        for (int i = 2; i <= 10; i++)
        {
            final BlockState below = cachedBlockLookup.getBlockState(x, y - i, z);
            if (SurfaceType.getSurfaceType(world, below, tempWorldPos.set(x, y, z)) == SurfaceType.WALKABLE && i <= 3 || isLiquid(below))
            {
                //  Level path
                return y - i + 1;
            }
            else if (!below.isAir())
            {
                return -100;
            }
        }

        return -100;
    }

    private int handleInLiquid(final int x, final int y, final int z, @NotNull final BlockState below, final boolean isSwimming)
    {
        if (isSwimming)
        {
            //  Already swimming in something, or allowed to swim and this is water
            return y;
        }

        if (pathingOptions.canSwim() && SurfaceType.isWater(world, tempWorldPos.set(x, y - 1, z)))
        {
            //  This is water, and we are allowed to swim
            return y;
        }

        //  Not allowed to swim or this isn't water, and we're on dry land
        return -100;
    }

    private int handleTargetNotPassable(@Nullable final MNode parent, final int x, final int y, final int z, @NotNull final BlockState target)
    {
        final boolean canJump = parent != null && !parent.isLadder() && !parent.isSwimming();
        //  Need to try jumping up one, if we can
        if (!canJump || SurfaceType.getSurfaceType(world, target, tempWorldPos.set(x, y, z)) != SurfaceType.WALKABLE)
        {
            return -100;
        }

        //  Check for headroom in the target space
        if (!isPassable(x, y + 2, z, false, parent))
        {
            final VoxelShape bb1 = cachedBlockLookup.getBlockState(x, y, z).getCollisionShape(world, tempWorldPos.set(x, y, z));
            final VoxelShape bb2 = cachedBlockLookup.getBlockState(x, y + 2, z).getCollisionShape(world, tempWorldPos.set(x, y + 2, z));
            if ((y + 2 + getStartY(bb2, 1)) - (y + getEndY(bb1, 0)) < 2)
            {
                return -100;
            }
        }

        if (!canLeaveBlock(x, y + 2, z, parent, true))
        {
            return -100;
        }

        //  Check for jump room from the origin space
        if (!isPassable(parent.x, parent.y + 2, parent.z, false, parent))
        {
            final VoxelShape bb1 = cachedBlockLookup.getBlockState(x, y, z).getCollisionShape(world, tempWorldPos.set(x, y, z));
            final VoxelShape bb2 = cachedBlockLookup.getBlockState(parent.x, parent.y + 2, parent.z).getCollisionShape(world, tempWorldPos.set(parent.x, parent.y + 2, parent.z));
            if ((parent.y + 2 + getStartY(bb2, 1)) - (y + getEndY(bb1, 0)) < 2)
            {
                return -100;
            }
        }


        final BlockState parentBelow = cachedBlockLookup.getBlockState(parent.x, parent.y - 1, parent.z);
        final VoxelShape parentBB = parentBelow.getCollisionShape(world, tempWorldPos.set(parent.x, parent.y - 1, parent.z));

        double parentY = ShapeUtil.max(parentBB, Direction.Axis.Y);
        double parentMaxY = parentY + parent.y - 1;
        final double targetMaxY = ShapeUtil.max(target.getCollisionShape(world, tempWorldPos.set(x, y, z)), Direction.Axis.Y) + y;
        if (targetMaxY - parentMaxY < MAX_JUMP_HEIGHT)
        {
            return y + 1;
        }
        if (target.is(BlockTags.STAIRS)
              && parentY - HALF_A_BLOCK < MAX_JUMP_HEIGHT
              && target.getValue(StairBlock.HALF) == Half.BOTTOM
              && BlockPosUtil.getXZFacing(parent.x, parent.z, x, z) == target.getValue(StairBlock.FACING))
        {
            return y + 1;
        }
        return -100;
    }

    private boolean checkHeadBlock(@Nullable final MNode parent, int x, int y, int z)
    {
        final VoxelShape bb = cachedBlockLookup.getBlockState(x, y, z).getCollisionShape(world, tempWorldPos.set(x, y, z));
        if (!canLeaveBlock(x, y + 1, z, parent, true))
        {
            return true;
        }

        if (!isPassable(x, y + 1, z, true, parent))
        {
            // TODO: Checking +1 and -1 seems odd? probably one intended to be current instead
            final VoxelShape bb1 = cachedBlockLookup.getBlockState(x, y - 1, z).getCollisionShape(world, tempWorldPos.set(x, y - 1, z));
            final VoxelShape bb2 = cachedBlockLookup.getBlockState(x, y + 1, z).getCollisionShape(world, tempWorldPos.set(x, y + 1, z));
            if ((y + 1 + getStartY(bb2, 1)) - (y - 1 + getEndY(bb1, 0)) < 2)
            {
                return true;
            }
            if (parent != null)
            {
                final VoxelShape bb3 =
                  cachedBlockLookup.getBlockState(parent.x, parent.y - 1, parent.z).getCollisionShape(world, tempWorldPos.set(parent.x, parent.y - 1, parent.z));
                if ((y + 1 + getStartY(bb2, 1)) - (parent.y - 1 + getEndY(bb3, 0)) < 1.75)
                {
                    return true;
                }
            }
        }

        if (parent != null)
        {
            final BlockState hereState = cachedBlockLookup.getBlockState(x, y - 1, z);
            final VoxelShape bb2 = cachedBlockLookup.getBlockState(x, y + 1, z).getCollisionShape(world, tempWorldPos.set(x, y + 1, z));
            if ((y + 1 + getStartY(bb2, 1)) - (y + getEndY(bb, 0)) >= 2)
            {
                return false;
            }

            return isLiquid(hereState) && !isPassable(x, y, z, false, parent);
        }
        return false;
    }

    /**
     * Get the start y of a voxelshape.
     *
     * @param bb  the voxelshape.
     * @param def the default if empty.
     * @return the start y.
     */
    private double getStartY(final VoxelShape bb, final int def)
    {
        return ShapeUtil.isEmpty(bb) ? def : ShapeUtil.min(bb, Direction.Axis.Y);
    }

    /**
     * Get the end y of a voxelshape.
     *
     * @param bb  the voxelshape.
     * @param def the default if empty.
     * @return the end y.
     */
    private double getEndY(final VoxelShape bb, final int def)
    {
        return ShapeUtil.isEmpty(bb) ? def : ShapeUtil.max(bb, Direction.Axis.Y);
    }

    /**
     * Is the space passable.
     *
     * @param block  the block we are checking.
     * @param parent the parent node.
     * @param head   the head position.
     * @return true if the block does not block movement.
     */
    protected boolean isPassable(@NotNull final BlockState block, final int x, final int y, final int z, final MNode parent, final boolean head)
    {
        if (!canLeaveBlock(x, y, z, parent, head))
        {
            return false;
        }

        if (!block.isAir())
        {
            final VoxelShape shape = block.getCollisionShape(world, tempWorldPos.set(x, y, z));
            if (ShapeUtil.max(shape, Direction.Axis.Y) < 0.5 && SurfaceType.isDangerous(cachedBlockLookup.getBlockState(x, y - 1, z)))
            {
                return false;
            }
            if (block.blocksMotion() && !(ShapeUtil.isEmpty(shape) || ShapeUtil.max(shape, Direction.Axis.Y) <= 0.1))
            {
                if (block.getBlock() instanceof TrapDoorBlock || block.getBlock() instanceof PanelBlock)
                {
                    int parentY = parent == null ? start.getY() : parent.y;
                    if (head)
                    {
                        parentY++;
                    }

                    final int dY = y - parentY;

                    final Direction direction = BlockPosUtil.getXZFacing(parent == null ? start.getX() : parent.x, parent == null ? start.getZ() : parent.z, x, z);
                    final Direction facing = block.getValue(TrapDoorBlock.FACING);

                    if (block.getBlock() instanceof PanelBlock && !block.getValue(PanelBlock.OPEN))
                    {
                        if (dY == 0)
                        {
                            return (head && block.getValue(PanelBlock.HALF) == Half.TOP);
                        }

                        if (head && dY == 1 && block.getValue(PanelBlock.HALF) == Half.TOP)
                        {
                            return true;
                        }

                        if (!head && dY == -1 && block.getValue(PanelBlock.HALF) == Half.BOTTOM)
                        {
                            return true;
                        }

                        return false;
                    }

                    // We can enter a space of a trapdoor if it's facing the same direction
                    if (direction == facing.getOpposite())
                    {
                        return true;
                    }

                    // We cannot enter a space of a trapdoor if its facing the opposite direction.
                    if (direction == facing)
                    {
                        return false;
                    }

                    return true;
                }
                else
                {
                    return pathingOptions.canEnterDoors() && (block.getBlock() instanceof DoorBlock || block.getBlock() instanceof FenceGateBlock)
                             || block.getBlock() instanceof AbstractBlockMinecoloniesConstructionTape
                             || block.getBlock() instanceof PressurePlateBlock
                             || block.getBlock() instanceof BlockDecorationController
                             || block.getBlock() instanceof SignBlock
                             || block.getBlock() instanceof AbstractBannerBlock
                             || !block.getBlock().properties.hasCollision;
                }
            }
            else if (SurfaceType.isDangerous(block))
            {
                return false;
            }
            else
            {
                if (isLadder(block, x, y, z))
                {
                    return true;
                }

                if (ShapeUtil.isEmpty(shape) || ShapeUtil.max(shape, Direction.Axis.Y) <= 0.1 && !isLiquid((block)) && (block.getBlock() != Blocks.SNOW
                                                                                                                          || block.getValue(SnowLayerBlock.LAYERS) == 1))
                {
                    final BlockPathTypes pathType = block.getBlockPathType(world, tempWorldPos.set(x, y, z), (Mob) entity.get());
                    if (pathType == null || pathType.getDanger() == null)
                    {
                        return true;
                    }
                }
                return false;
            }
        }

        return true;
    }

    protected boolean isPassable(final int x, final int y, final int z, final boolean head, final MNode parent)
    {
        final BlockState state = cachedBlockLookup.getBlockState(x, y, z);
        final VoxelShape shape = state.getCollisionShape(world, tempWorldPos.set(x, y, z));
        if (ShapeUtil.isEmpty(shape) || ShapeUtil.max(shape, Direction.Axis.Y) <= 0.1)
        {
            return !head
                     || !(state.getBlock() instanceof WoolCarpetBlock || state.getBlock() instanceof FloatingCarpetBlock)
                     || isLadder(state, x, y, z);
        }
        return isPassable(state, x, y, z, parent, head);
    }

    /**
     * Check if we can leave the block at this pos.
     *
     * @param parent the parent pos (to check if we can leave)
     * @return true if so.
     */
    private boolean canLeaveBlock(final int x, final int y, final int z, final MNode parent, final boolean head)
    {
        int parentX = parent == null ? start.getX() : parent.x;
        int parentY = parent == null ? start.getY() : parent.y;
        int parentZ = parent == null ? start.getZ() : parent.z;
        if (head)
        {
            parentY++;
        }

        final int dY = y - parentY;

        final BlockState parentBlock = cachedBlockLookup.getBlockState(parentX, parentY, parentZ);
        if (parentBlock.getBlock() instanceof TrapDoorBlock || parentBlock.getBlock() instanceof PanelBlock)
        {
            if (!parentBlock.getValue(TrapDoorBlock.OPEN))
            {
                if (dY != 0)
                {
                    if (parentBlock.getBlock() instanceof TrapDoorBlock)
                    {
                        return true;
                    }
                    return (head && parentBlock.getValue(PanelBlock.HALF) == Half.TOP && dY < 0) || (!head && parentBlock.getValue(PanelBlock.HALF) == Half.BOTTOM
                                                                                                       && dY > 0);
                }
                return true;
            }
            if (x - parentX != 0 || z - parentZ != 0)
            {
                // Check if we can leave the current block, there might be a trapdoor or panel blocking us.
                final Direction direction = BlockPosUtil.getXZFacing(parentX, parentZ, x, z);
                final Direction facing = parentBlock.getValue(TrapDoorBlock.FACING);
                if (direction == facing.getOpposite())
                {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Is the block a ladder.
     *
     * @param blockState block to check.
     * @return true if the block is a ladder.
     */
    protected boolean isLadder(@NotNull final BlockState blockState, final int x, final int y, final int z)
    {
        return blockState.isLadder(world, tempWorldPos.set(x, y, z), entity.get()) && (blockState.getBlock() instanceof LadderBlock || pathingOptions.canClimbNonLadders());
    }

    protected boolean isLadder(final int x, final int y, final int z)
    {
        return isLadder(cachedBlockLookup.getBlockState(x, y, z), x, y, z);
    }

    /**
     * Sets the pathing options
     *
     * @param pathingOptions the pathing options to set.
     */
    public void setPathingOptions(final PathingOptions pathingOptions)
    {
        this.pathingOptions.importFrom(pathingOptions);
    }

    /**
     * Check if in restricted area.
     *
     * @return true if so.
     */
    public boolean isInRestrictedArea(final int x, final int y, final int z)
    {
        if (restrictionType == AbstractAdvancedPathNavigate.RestrictionType.NONE)
        {
            return true;
        }

        final boolean isInXZ = x <= maxX && z <= maxZ && z >= minZ && x >= minX;
        if (!isInXZ)
        {
            return false;
        }

        if (restrictionType == AbstractAdvancedPathNavigate.RestrictionType.XZ)
        {
            return true;
        }
        return y <= maxY && y >= minY;
    }

    public static boolean equalPositions(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2)
    {
        return x1 == x2 && y1 == y2 && z1 == z2;
    }
}
