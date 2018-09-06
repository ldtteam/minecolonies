package com.minecolonies.coremod.entity.ai.mobs.barbarians;

import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.entity.pathfinding.GeneralEntityWalkToProxy;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockLadder;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.util.constant.BarbarianConstants.*;

/**
 * Barbarian Pathing Class
 */
public class EntityAIWalkToRandomHuts extends EntityAIBase
{

    /**
     * The moving entity.
     */
    protected final AbstractEntityBarbarian entity;

    /**
     * All directions.
     */
    private final List<EnumFacing> directions = Arrays.asList(EnumFacing.HORIZONTALS);

    /**
     * The world.
     */
    protected final World world;

    /**
     * The set speed.
     */
    protected final double speed;

    /**
     * The colony.
     */
    protected Colony colony;

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
    private int stuckTime = 0;

    /**
     * Constructor for AI
     *
     * @param creatureIn the creature that the AI applies to
     * @param speedIn    The speed at which the Entity walks
     */
    public EntityAIWalkToRandomHuts(final AbstractEntityBarbarian creatureIn, final double speedIn)
    {
        super();
        this.entity = creatureIn;
        this.speed = speedIn;
        this.world = creatureIn.getEntityWorld();
        lastPos = entity.getPosition();
        this.setMutexBits(1);
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
     * Returns whether an in-progress EntityAIBase should continue executing
     *
     * @return Boolean value of whether or not to continue executing
     */
    @Override
    public boolean shouldContinueExecuting()
    {
        return !this.entity.getNavigator().noPath() && this.entity.isEntityAlive();
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
        if (proxy == null)
        {
            proxy = new GeneralEntityWalkToProxy(entity);
        }

        if (new AxisAlignedBB(entity.getPosition()).expand(1, 1, 1)
              .intersects(new AxisAlignedBB(lastPos)))
        {
            stuckTime++;
        }
        else
        {
            stuckTime = 0;
        }

        if (stuckTime > EVERY_X_TICKS)
        {
            entity.getNavigator().clearPath();
            stuckTime = 0;
            entity.setStuckCounter(entity.getStuckCounter() + 1);

            if (entity.getStuckCounter() > 1)
            {
                Collections.shuffle(directions);

                entity.setStuckCounter(0);
                entity.setLadderCounter(entity.getLadderCounter() + 1);

                if (entity.getLadderCounter() <= LADDERS_TO_PLACE)
                {
                    for (final EnumFacing dir : directions)
                    {
                        if (world.getBlockState(entity.getPosition().offset(dir)).getMaterial().isSolid())
                        {
                            if (random.nextBoolean())
                            {
                                world.setBlockState(entity.getPosition().up(), Blocks.LADDER.getDefaultState().withProperty(BlockLadder.FACING, dir.getOpposite()));
                            }
                            else
                            {
                                world.setBlockState(entity.getPosition(), Blocks.LADDER.getDefaultState().withProperty(BlockLadder.FACING, dir.getOpposite()));
                            }
                            break;
                        }
                    }
                }
                else
                {
                    for (final EnumFacing dir : directions)
                    {
                        if (world.getBlockState(entity.getPosition().offset(dir)).getMaterial().isSolid())
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
                                    posToDestroy = entity.getPosition().up();
                                    break;
                            }
                            world.destroyBlock(posToDestroy, true);
                            break;
                        }
                    }
                }
            }
            else
            {
                entity.getNavigator().moveAwayFromXYZ(entity.getPosition(), random.nextInt(4), 2);
            }
            return false;
        }

        lastPos = entity.getPosition();
        return proxy.walkToBlock(site, range, true);
    }

    /**
     * gets a random building from the nearby colony
     *
     * @return A random building
     */
    private BlockPos getRandomBuilding()
    {
        if (getColony() == null)
        {
            return null;
        }

        final Collection<AbstractBuilding> buildingList = getColony().getBuildingManager().getBuildings().values();
        final Object[] buildingArray = buildingList.toArray();
        if (buildingArray.length != 0)
        {
            final int rand = random.nextInt(buildingArray.length);
            final AbstractBuilding building = (AbstractBuilding) buildingArray[rand];

            return building.getLocation();
        }
        else
        {
            return null;
        }
    }

    public Colony getColony()
    {
        if (colony == null)
        {
            colony = ColonyManager.getClosestColony(world, entity.getPosition());
        }

        return colony;
    }
}
