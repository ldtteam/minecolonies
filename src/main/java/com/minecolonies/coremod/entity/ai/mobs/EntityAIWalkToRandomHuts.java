package com.minecolonies.coremod.entity.ai.mobs;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Random;

/**
 * Created by Asher on 7/6/17.
 */
public class EntityAIWalkToRandomHuts extends EntityAIBase
{

    protected final EntityCreature entity;
    private BlockPos targetBlock;
    protected final World world;
    private double movePosX;
    private double movePosY;
    private double movePosZ;
    protected final double speed;
    protected boolean mustUpdate;
    protected final Colony colony;
    protected Vec3d vec3d;

    public EntityAIWalkToRandomHuts(EntityCreature creatureIn, double speedIn)
    {
        this.entity = creatureIn;
        this.speed = speedIn;
        this.world = creatureIn.getEntityWorld();
        this.colony = ColonyManager.getClosestColony(world, creatureIn.getPosition());
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute()
    {
        if (this.targetBlock == null)
        {
        this.targetBlock = getRandomBuilding();
        }

        if (this.targetBlock == null)
        {
            return false;
        }
        else
        {
            Vec3d vec3d = RandomPositionGenerator.findRandomTargetBlockTowards(this.entity, 16, 7, new Vec3d(this.targetBlock.getX(), this.targetBlock.getY(), this.targetBlock.getZ()));
            if (vec3d == null)
            {
                return false;
            }
            else
            {
                this.movePosX = vec3d.xCoord;
                this.movePosY = vec3d.yCoord;
                this.movePosZ = vec3d.zCoord;
                return true;
            }
        }
    }

    @Nullable
    protected BlockPos getPosition()
    {
        //colony.getCenter();
        colony.getBuildings();
        return colony.getCenter();
        //return RandomPositionGenerator.findRandomTarget(this.entity, 10, 7);
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting()
    {
        return !this.entity.getNavigator().noPath() && this.entity.isEntityAlive();
    }

    public void startExecuting()
    {
        this.entity.getNavigator().tryMoveToXYZ(this.movePosX, this.movePosY, this.movePosZ, this.speed);
    }

    public void makeUpdate()
    {
        this.mustUpdate = true;
    }

    @org.jetbrains.annotations.Nullable
    private BlockPos getRandomBuilding()
    {
        if (colony == null)
        {
            return null;
        }

        final Collection<AbstractBuilding> buildingList = colony.getBuildings().values();
        final Object[] buildingArray = buildingList.toArray();
        if (buildingArray != null && buildingArray.length != 0) {
            final int random = new Random().nextInt(buildingArray.length);
            final AbstractBuilding building = (AbstractBuilding) buildingArray[random];

            return building.getLocation();
        }
        else
        {
            return null;
        }
    }

}
