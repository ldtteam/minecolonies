package com.minecolonies.coremod.entity.mobs.aitasks;

import com.minecolonies.api.colony.colonyEvents.EventStatus;
import com.minecolonies.api.colony.colonyEvents.IColonyEvent;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.HordeRaidEvent;
import com.minecolonies.coremod.entity.pathfinding.GeneralEntityWalkToProxy;
import net.minecraft.block.*;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.fluid.Fluids;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.util.constant.Constants.LEVITATION_EFFECT;
import static com.minecolonies.api.util.constant.Constants.SECONDS_A_MINUTE;

/**
 * Raider Pathing Class
 */
public class EntityAIWalkToRandomHuts extends Goal
{
    /**
     * Min distance to the target block which is considered too close.
     */
    private static final double MIN_TP_DIST = 100;

    /**
     * The moving entity.
     */
    protected final AbstractEntityMinecoloniesMob entity;

    /**
     * All directions.
     */
    private final List<Direction> directions = Arrays.asList(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);

    /**
     * The world.
     */
    protected final World world;

    /**
     * The set speed.
     */
    protected final double speed;

    /**
     * Random obj.
     */
    private final Random random = new Random();

    /**
     * The target block.
     */
    private BlockPos targetBlock;

    /**
     * Walk to proxy.
     */
    private GeneralEntityWalkToProxy proxy;

    /**
     * The last path index he was at.
     */
    private int lastIndex = -1;

    /**
     * Time the entity is at the same position already.
     */
    private int stuckTime = 1;

    /**
     * The amount of passed ticks.
     */
    private int passedTicks = 0;

    /**
     * Update invterval of the AI
     */
    private static int UPDATE_INTERVAL = 40;

    /**
     * Ticktimer for the update rate
     */
    int tickTimer = 0;

    /**
     * Counter for distance/time stuck.
     */
    private int totalStuckTime = 0;

    /**
     * Timer for walking randomly between campfires
     */
    private int campFireWalkTimer = 0;

    /**
     * Whether the entity had a path last update
     */
    boolean hadPath = false;

    /**
     * Constructor for AI
     *
     * @param creatureIn the creature that the AI applies to
     * @param speedIn    The speed at which the Entity walks
     */
    public EntityAIWalkToRandomHuts(final AbstractEntityMinecoloniesMob creatureIn, final double speedIn)
    {
        super();
        this.entity = creatureIn;
        this.speed = speedIn;
        this.world = creatureIn.getEntityWorld();
        this.setMutexFlags(EnumSet.of(Flag.MOVE));
        campFireWalkTimer = world.rand.nextInt(1000);
        proxy = new GeneralEntityWalkToProxy(entity);
    }

    @Override
    public boolean shouldExecute()
    {
        if (!(this.entity.isAlive() && this.entity.getColony() != null && entity.getAttackTarget() == null && entity.getAttackingEntity() == null))
        {
            return false;
        }

        if (entity.getColony() != null)
        {
            final IColonyEvent event = entity.getColony().getEventManager().getEventByID(entity.getEventID());
            if (event == null)
            {
                return false;
            }

            if (event.getStatus() == EventStatus.PREPARING && event instanceof HordeRaidEvent)
            {
                walkToCampFire();
                return false;
            }
        }

        return true;
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    @Override
    public void tick()
    {
        if (++tickTimer < UPDATE_INTERVAL)
        {
            return;
        }
        tickTimer = 0;

        if (this.isEntityAtSiteWithMove(targetBlock, 2))
        {
            targetBlock = getRandomBuilding();
            resetStuckCounters();
        }
    }

    /**
     * Resets stuck counters
     */
    private void resetStuckCounters()
    {
        passedTicks = 0;
        stuckTime = 0;
        lastIndex = -1;
        entity.setStuckCounter(0);
        totalStuckTime = 0;
    }

    /**
     * Is executed when the ai Starts Executing
     */
    @Override
    public void startExecuting()
    {
        targetBlock = getRandomBuilding();
        hadPath = false;
        resetStuckCounters();
    }

    @Override
    public void resetTask()
    {
        targetBlock = getRandomBuilding();
        hadPath = false;
        resetStuckCounters();
    }

    /**
     * returns whether the entity as at a site with a move, And moves it
     *
     * @param site  The site which to move to or check if it is already there
     * @param range The distance to the site that the entity must be within to return true
     * @return whether the entity is at the site or not
     */
    private boolean isEntityAtSiteWithMove(@NotNull final BlockPos site, final int range)
    {
        if (this.entity.isPotionActive(LEVITATION_EFFECT))
        {
            return true;
        }

        if (proxy.walkToBlock(site, range, true))
        {
            // true if we're at the site.
            return true;
        }

        if (entity.getNavigator().getPath() == null || entity.getNavigator().getPath().isFinished())
        {
            // With no path reset the last path index point to -1
            lastIndex = -1;

            if (!hadPath)
            {
                // Stuck when we have no path and had no path last update before(10 ticks)
                stuckTime++;
            }
        }
        else
        {
            if (entity.getNavigator().getPath().getCurrentPathIndex() == lastIndex)
            {
                // Stuck when we have a path, but are not progressing on it
                stuckTime++;

                if (totalStuckTime > 0 && trySkipAheadOnPath())
                {
                    resetStuckCounters();
                }
            }
            else if (entity.getNavigator().getPath().getCurrentPathIndex() > 5)
            {
                // Not stuck when progressing on a slightly longer path(no short unstuck-path)
                resetStuckCounters();
            }
            lastIndex = entity.getNavigator().getPath().getCurrentPathIndex();
        }

        hadPath = entity.getNavigator().getPath() != null && !entity.getNavigator().getPath().isFinished();

        // Stuck timout
        passedTicks += UPDATE_INTERVAL;
        final long targetDist = BlockPosUtil.getDistance2D(entity.getPosition(), targetBlock);
        if (entity.getStuckCounter() >= 5 || targetDist * SECONDS_A_MINUTE < passedTicks)
        {
            if (handleTotalStuck(targetDist))
            {
                resetStuckCounters();
                targetBlock = getRandomBuilding();
                return false;
            }
        }

        if (stuckTime > 5)
        {
            handleEntityBeingStuck();
        }

        return false;
    }

    /**
     * Handles beeing completly stuck, teleports the entity a little.
     * 
     * @param targetDist the current distance from the target.
     * @return whether the handling worked.
     */
    private boolean handleTotalStuck(final long targetDist)
    {
        // Try reseting the target first
        totalStuckTime++;

        if (trySkipAheadOnPath())
        {
            return true;
        }

        if (totalStuckTime == 1)
        {
            passedTicks -= UPDATE_INTERVAL * 2;
            entity.setStuckCounter(entity.getStuckCounter() - 2);
            targetBlock = entity.getColony().getRaiderManager().getRandomBuilding();
            return false;
        }

        if (targetDist > MIN_TP_DIST)
        {
            final BlockPos tpPos = BlockPosUtil.getFloor(entity.getPosition().offset(BlockPosUtil.getXZFacing(entity.getPosition(), targetBlock), 5), world);
            entity.setPositionAndUpdate(tpPos.getX(), tpPos.getY(), tpPos.getZ());
            return true;
        }

        return false;
    }

    /**
     * Tries to skip ahead on an existing path.
     * 
     * @return whether we skipped ahead on the current path.
     */
    private boolean trySkipAheadOnPath()
    {
        final Path path = entity.getNavigator().getPath();
        if (path != null && !path.isFinished())
        {
            if (path.getCurrentPathLength() >= 5)
            {
                final PathPoint pathPoint = path.getPathPointFromIndex(4);
                entity.setPositionAndUpdate(pathPoint.x, pathPoint.y, pathPoint.z);
                return true;
            }
        }
        return false;
    }

    /**
     * Handles the entity being stuck, resets the path, places temporary blocks and ladders/block breaks if needed.
     */
    private void handleEntityBeingStuck()
    {
        entity.getNavigator().clearPath();
        stuckTime = 0;
        entity.setStuckCounter(entity.getStuckCounter() + 1);
        final BlockPos front = entity.getPosition().down().offset(entity.getHorizontalFacing());

        // Places temporary leaf block to walk on
        if ((world.isAirBlock(front) && world.isAirBlock(front.down(3))) || world.getBlockState(front).getBlock() == Blocks.LAVA
              || world.getBlockState(front).getFluidState().getFluid() == Fluids.FLOWING_LAVA)
        {
            world.setBlockState(front, Blocks.ACACIA_LEAVES.getDefaultState());
        }

        // try to path away from the stuck pos every 50 ticks when stuck
        if (entity.getStuckCounter() % 5 == 0)
        {
            entity.getNavigator().moveAwayFromXYZ(entity.getPosition(), random.nextInt(4), 2);
        }
        else
        {
            handleBarbarianMovementSpecials();
        }
    }

    /**
     * Places ladders and breaks blocks in a random direction
     */
    private void handleBarbarianMovementSpecials()
    {
        Collections.shuffle(directions);
        // Switch between placing ladders and block breaks, every 5 times
        if (entity.getStuckCounter() % 10 < 5)
        {
            handleBarbarianLadderPlacement();
        }
        else if (MineColonies.getConfig().getCommon().doBarbariansBreakThroughWalls.get())
        {
            handleBarbarianBlockBreakment();
        }
    }

    /**
     * Handles the ladder placement, if the upper or lower ladder exists it places one with the same orientation, else it uses a random one.
     */
    private void handleBarbarianLadderPlacement()
    {
        final BlockState ladderHere = world.getBlockState(entity.getPosition());
        final BlockState ladderUp = world.getBlockState(entity.getPosition().up());

        if (ladderHere.getBlock() == Blocks.LADDER && ladderUp.getBlock() != Blocks.LADDER && !ladderUp.getMaterial().isLiquid())
        {
            final Direction facingHere = ladderHere.get(LadderBlock.FACING);
            final BlockState upState = world.getBlockState(entity.getPosition().up().offset(facingHere));

            if (upState.getMaterial().isSolid() && Blocks.LADDER.isValidPosition(ladderHere, world, entity.getPosition().up()))
            {
                world.setBlockState(entity.getPosition().up(), ladderHere);
            }
        }
        else if (ladderUp.getBlock() == Blocks.LADDER && ladderHere.getBlock() != Blocks.LADDER && !ladderHere.getMaterial().isLiquid())
        {
            final Direction facingUp = ladderUp.get(LadderBlock.FACING);
            final BlockState downState = world.getBlockState(entity.getPosition().offset(facingUp.getOpposite()));

            if (downState.getMaterial().isSolid() && Blocks.LADDER.isValidPosition(ladderUp, world, entity.getPosition()))
            {
                world.setBlockState(entity.getPosition(), ladderUp);
            }
        }
        else if (ladderUp.getBlock() != Blocks.LADDER && ladderHere.getBlock() != Blocks.LADDER)
        {
            handleNextLadderPlacement();
        }
    }

    /**
     * Handles the brock breaking, only collideable - nonsolid blocks are broken. Doors only at hard difficulty
     */
    private void handleBarbarianBlockBreakment()
    {
        for (final Direction dir : directions)
        {
            final BlockPos posToDestroy = entity.getPosition().up(random.nextInt(3)).offset(dir);
            final BlockState state = world.getBlockState(posToDestroy);
            if (!(state.getBlock() instanceof AirBlock) && !state.getMaterial().isLiquid() || (state.getBlock() instanceof DoorBlock
                                                                                                 && world.getDifficulty() == Difficulty.HARD))
            {
                world.destroyBlock(posToDestroy, true);
            }
        }
    }

    /**
     * Places the next ladder in a random direction.
     */
    private void handleNextLadderPlacement()
    {
        for (final Direction dir : directions)
        {
            final BlockState toPlace = Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, dir.getOpposite());
            final BlockState state = world.getBlockState(entity.getPosition().up().offset(dir));
            if (state.getMaterial().isSolid() && Blocks.LADDER.isValidPosition(toPlace, world, entity.getPosition().offset(dir)))
            {
                world.setBlockState(entity.getPosition().up(), toPlace);
                break;
            }
        }
    }

    /**
     * Gets a random building from the raidmanager.
     *
     * @return A random building
     */
    private BlockPos getRandomBuilding()
    {
        if (entity.getColony() == null)
        {
            return null;
        }

        return entity.getColony().getRaiderManager().getRandomBuilding();
    }

    private void walkToCampFire()
    {
        campFireWalkTimer -= 4;
        if (campFireWalkTimer < 0)
        {
            final BlockPos campFire = ((HordeRaidEvent) entity.getColony().getEventManager().getEventByID(entity.getEventID())).getRandomCampfire();

            if (campFire == null)
            {
                return;
            }

            campFireWalkTimer = world.rand.nextInt(1000);
            targetBlock = BlockPosUtil.getRandomPosition(world,
              campFire,
              BlockPos.ZERO,
              3,
              6);
            if (targetBlock != null && targetBlock != BlockPos.ZERO)
            {
                this.isEntityAtSiteWithMove(targetBlock, 3);
            }
        }
    }
}
