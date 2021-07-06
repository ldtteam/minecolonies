package com.minecolonies.coremod.entity.mobs.aitasks;

import com.minecolonies.api.colony.colonyEvents.EventStatus;
import com.minecolonies.api.colony.colonyEvents.IColonyEvent;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.EntityUtils;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.HordeRaidEvent;
import com.minecolonies.coremod.entity.pathfinding.GeneralEntityWalkToProxy;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

import static com.minecolonies.api.util.constant.Constants.LEVITATION_EFFECT;

import net.minecraft.entity.ai.goal.Goal.Flag;

/**
 * Raider Pathing Class
 */
public class EntityAIWalkToRandomHuts extends Goal
{
    private static final int THREE_MINUTES_TICKS = 3600;

    /**
     * The moving entity.
     */
    protected final AbstractEntityMinecoloniesMob entity;

    /**
     * The world.
     */
    protected final World world;

    /**
     * The set speed.
     */
    protected final double speed;

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
     * Update invterval of the AI
     */
    private static int UPDATE_INTERVAL = 40;

    /**
     * Ticktimer for the update rate
     */
    int tickTimer = 0;

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
        this.world = creatureIn.getCommandSenderWorld();
        this.setFlags(EnumSet.of(Flag.MOVE));
        campFireWalkTimer = world.random.nextInt(1000);
        proxy = new GeneralEntityWalkToProxy(entity);
    }

    @Override
    public boolean canUse()
    {
        if (!this.entity.isAlive() || this.entity.getColony() == null || (entity.getTarget() != null && !EntityUtils.isFlying(entity.getTarget())) || (
          entity.getKillCredit() != null && !EntityUtils.isFlying(entity.getTarget())))
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
        stuckTime = 0;
        lastIndex = -1;
        entity.setStuckCounter(0);
    }

    /**
     * Is executed when the ai Starts Executing
     */
    @Override
    public void start()
    {
        targetBlock = getRandomBuilding();
        hadPath = false;
        resetStuckCounters();
    }

    @Override
    public void stop()
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
        if (this.entity.hasEffect(LEVITATION_EFFECT))
        {
            return true;
        }

        if (proxy.walkToBlock(site, range, true))
        {
            // true if we're at the site.
            return true;
        }

        if (entity.getNavigation().getPath() == null || entity.getNavigation().getPath().isDone())
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
            if (entity.getNavigation().getPath().getNextNodeIndex() == lastIndex)
            {
                // Stuck when we have a path, but are not progressing on it
                stuckTime++;
            }
            else if (entity.getNavigation().getPath().getNextNodeIndex() > 5)
            {
                // Not stuck when progressing on a slightly longer path(no short unstuck-path)
                resetStuckCounters();
            }
            lastIndex = entity.getNavigation().getPath().getNextNodeIndex();
        }

        hadPath = entity.getNavigation().getPath() != null && !entity.getNavigation().getPath().isDone();

        if (stuckTime * UPDATE_INTERVAL > THREE_MINUTES_TICKS)
        {
            return true;
        }

        return false;
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

            campFireWalkTimer = world.random.nextInt(1000);
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
