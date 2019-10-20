package com.minecolonies.coremod.entity.mobs.aitasks;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.entity.pathfinding.PathResult;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.entity.pathfinding.GeneralEntityWalkToProxy;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LadderBlock;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.util.constant.Constants.LEVITATION_EFFECT;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.RaiderConstants.LADDERS_TO_PLACE;

/**
 * Raider Pathing Class
 */
public class EntityAIWalkToRandomHuts extends Goal
{

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
     * The last position he was at.
     */
    private BlockPos lastPos = null;

    /**
     * Time the entity is at the same position already.
     */
    private int stuckTime = 1;

    /**
     * Time the entity is not stuck.
     */
    private int notStuckTime = 0;

    /**
     * The amount of passed ticks.
     */
    private int passedTicks = 0;

    /**
     * The pathresult of trying to move away from a point
     */
    private PathResult moveAwayPath;

    /**
     * Constructor for AI
     *  @param creatureIn the creature that the AI applies to
     * @param speedIn    The speed at which the Entity walks
     */
    public EntityAIWalkToRandomHuts(final AbstractEntityMinecoloniesMob creatureIn, final double speedIn)
    {
        super();
        this.entity = creatureIn;
        this.speed = speedIn;
        this.world = creatureIn.getEntityWorld();
        lastPos = entity.getPosition();
        this.setMutexFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean shouldExecute()
    {
        if (this.targetBlock == null)
        {
            this.targetBlock = getRandomBuilding();
        }

        return this.targetBlock != null;
    }

    /**
     * Returns whether an in-progress Goal should continue executing
     *
     * @return Boolean value of whether or not to continue executing
     */
    @Override
    public boolean shouldContinueExecuting()
    {
        return !this.entity.getNavigator().noPath() && this.entity.isAlive();
    }

    /**
     * Is executed when the ai Starts Executing
     */
    @Override
    public void startExecuting()
    {
        if (targetBlock != null)
        {
            if (this.isEntityAtSiteWithMove(targetBlock, 2))
            {
                targetBlock = getRandomBuilding();
            }
        }
        else
        {
            targetBlock = getRandomBuilding();
        }
        lastPos = entity.getPosition();
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

        if (handleProxyWhileMoving())
        {
            return proxy.walkToBlock(site, range, true);
        }
        updateStuckTimers();

        lastPos = entity.getPosition();

        if (resetStuckTimers())
        {
            return true;
        }

        if (stuckTime > 2)
        {
            return handleEntityBeingStuck();
        }

        return proxy.walkToBlock(site, range, true);
    }

    private boolean handleProxyWhileMoving()
    {
        passedTicks++;
        if (proxy == null)
        {
            proxy = new GeneralEntityWalkToProxy(entity);
        }

        if (passedTicks % TICKS_SECOND != 0)
        {
            return true;
        }
        passedTicks = 0;
        return false;
    }

    private void updateStuckTimers()
    {
        if (new AxisAlignedBB(entity.getPosition()).expand(1, 1, 1).expand(-1, -1, -1)
              .intersects(new AxisAlignedBB(lastPos)))
        {
            final BlockPos front = entity.getPosition().down().offset(entity.getHorizontalFacing());
            final VoxelShape collisionBox = world.getBlockState(entity.getPosition()).getCollisionShape(world, entity.getPosition());
            if (!world.getBlockState(front).getMaterial().isSolid()
                  || world.getBlockState(entity.getPosition().up().offset(entity.getHorizontalFacing())).getMaterial().isSolid()
                  || (!collisionBox.isEmpty() && collisionBox.getBoundingBox().maxY > 1.0))
            {
                stuckTime++;
            }
            else
            {
                notStuckTime++;
            }
        }
        else
        {
            stuckTime = 0;
            notStuckTime++;
        }
    }

    private boolean resetStuckTimers()
    {
        if (notStuckTime > 1)
        {
            entity.setStuckCounter(0);
            entity.setLadderCounter(0);
            notStuckTime = 0;
            stuckTime = 0;
            return true;
        }
        return false;
    }

    private boolean handleEntityBeingStuck()
    {
        entity.getNavigator().clearPath();
        entity.setStuckCounter(entity.getStuckCounter() + 1);
        final BlockPos front = entity.getPosition().down().offset(entity.getHorizontalFacing());

        if (world.isAirBlock(front) || world.getBlockState(front).getBlock() == Blocks.LAVA)
        {
            notStuckTime = 0;
            world.setBlockState(front, Blocks.COBBLESTONE.getDefaultState());
        }

        if (entity.getStuckCounter() > 1 && MineColonies.getConfig().getCommon().doBarbariansBreakThroughWalls.get())
        {
            handleBarbarianMovementSpecials();
        }
        else
        {
            if (moveAwayPath == null || !moveAwayPath.isInProgress())
            {
                moveAwayPath = entity.getNavigator().moveAwayFromXYZ(entity.getPosition(), random.nextInt(4), 2);
            }
        }
        return false;
    }

    private void handleBarbarianMovementSpecials()
    {
        Collections.shuffle(directions);

        entity.setLadderCounter(entity.getLadderCounter() + 1);
        notStuckTime = 0;
        final BlockState ladderHere = world.getBlockState(entity.getPosition());
        final BlockState ladderUp = world.getBlockState(entity.getPosition().up());
        if (entity.getLadderCounter() <= LADDERS_TO_PLACE || random.nextBoolean())
        {
            handleBarbarianLadderPlacement(ladderHere, ladderUp);
        }
        else
        {
            handleBarbarianBlockBreakment();
        }
    }

    private void handleBarbarianLadderPlacement(final BlockState ladderHere, final BlockState ladderUp)
    {
        if (ladderHere.getBlock() == Blocks.LADDER && ladderUp.getBlock() != Blocks.LADDER && !ladderHere.getMaterial().isLiquid())
        {
            world.setBlockState(entity.getPosition().up(), ladderHere);
        }
        else if (ladderUp.getBlock() == Blocks.LADDER && ladderHere.getBlock() != Blocks.LADDER && !ladderUp.getMaterial().isLiquid())
        {
            world.setBlockState(entity.getPosition(), ladderUp);
        }
        else if (ladderUp.getBlock() != Blocks.LADDER && ladderHere.getBlock() != Blocks.LADDER)
        {
            handleNextLadderPlacement(ladderHere);
        }
    }

    private void handleBarbarianBlockBreakment()
    {
        for (final Direction dir : directions)
        {
            final BlockState state = world.getBlockState(entity.getPosition().offset(dir));
            if ((state.getMaterial().isSolid() && state.getBlock() != Blocks.LADDER) || state.getBlock() instanceof DoorBlock)
            {
                final BlockPos posToDestroy;
                posToDestroy = breakRandomBlockIn(dir);
                world.destroyBlock(posToDestroy, true);
                break;
            }
        }
    }

    private void handleNextLadderPlacement(final BlockState ladderHere)
    {
        for (final Direction dir : directions)
        {
            if (world.getBlockState(entity.getPosition().offset(dir)).getMaterial().isSolid())
            {
                if (random.nextBoolean())
                {
                    world.setBlockState(entity.getPosition().up(), Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, dir.getOpposite()));
                }
                else if (!ladderHere.getMaterial().isLiquid())
                {
                    world.setBlockState(entity.getPosition(), Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, dir.getOpposite()));
                }
                break;
            }
        }
    }

    @NotNull
    private BlockPos breakRandomBlockIn(final Direction dir)
    {
        final BlockPos posToDestroy;
        switch (random.nextInt(4))
        {
            case 1:
                posToDestroy = entity.getPosition().offset(dir).up();
                break;
            case 2:
                posToDestroy = entity.getPosition().offset(dir);
                break;
            default:
                posToDestroy = entity.getPosition().up(2);
                break;
        }
        return posToDestroy;
    }

    /**
     * gets a random building from the nearby colony
     *
     * @return A random building
     */
    private BlockPos getRandomBuilding()
    {
        if (entity.getColony() == null)
        {
            return null;
        }

        final Collection<IBuilding> buildingList = entity.getColony().getBuildingManager().getBuildings().values();
        final Object[] buildingArray = buildingList.toArray();
        if (buildingArray.length != 0)
        {
            final int rand = random.nextInt(buildingArray.length);
            final IBuilding building = (IBuilding) buildingArray[rand];

            return building.getPosition();
        }
        else
        {
            return null;
        }
    }
}
