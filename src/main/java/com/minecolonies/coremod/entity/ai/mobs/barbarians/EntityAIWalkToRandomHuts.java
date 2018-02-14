package com.minecolonies.coremod.entity.ai.mobs.barbarians;

import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.entity.pathfinding.GeneralEntityWalkToProxy;
import com.minecolonies.coremod.entity.pathfinding.PathNavigate;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Random;

/**
 * Barbarian Pathing Class
 */
public class EntityAIWalkToRandomHuts extends EntityAIBase
{

    protected final EntityCreature entity;
    protected final World          world;
    protected final double         speed;
    protected Colony         colony;
    /**
     * The navigator for this entity.
     */
    private final PathNavigate newNavigator;
    private         BlockPos       targetBlock;
    /**
     * Walk to proxy.
     */
    private GeneralEntityWalkToProxy proxy;
    private       Field        navigatorField;

    /**
     * Constructor for AI
     *
     * @param creatureIn the creature that the AI applies to
     * @param speedIn    The speed at which the Entity walks
     */
    public EntityAIWalkToRandomHuts(final EntityCreature creatureIn, final double speedIn)
    {
        super();
        this.entity = creatureIn;
        this.speed = speedIn;
        this.world = creatureIn.getEntityWorld();
        this.newNavigator = new PathNavigate(entity, world);
        updateNavigatorField();
        this.newNavigator.setCanSwim(true);
        this.newNavigator.setEnterDoors(false);
        this.setMutexBits(1);
    }

    /**
     * Updates the navigator field for the AI
     */
    private synchronized void updateNavigatorField()
    {
        if (navigatorField == null)
        {
            final Field[] fields = EntityLiving.class.getDeclaredFields();
            for (@NotNull final Field field : fields)
            {
                if (field.getType().equals(net.minecraft.pathfinding.PathNavigate.class))
                {
                    field.setAccessible(true);
                    navigatorField = field;
                    break;
                }
            }
        }

        if (navigatorField == null)
        {
            throw new IllegalStateException("Navigator field should not be null, contact developers.");
        }

        try
        {
            navigatorField.set(entity, this.newNavigator);
        }
        catch (final IllegalAccessException e)
        {
            Log.getLogger().error("Navigator error", e);
        }
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
        updateNavigatorField();
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
        return proxy.walkToBlock(site, range);
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
            final int random = new Random().nextInt(buildingArray.length);
            final AbstractBuilding building = (AbstractBuilding) buildingArray[random];

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
