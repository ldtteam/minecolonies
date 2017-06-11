package com.minecolonies.coremod.entity.ai.mobs;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.coremod.colony.*;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.pathfinding.EntityCitizenWalkToProxy;
import com.minecolonies.coremod.entity.pathfinding.GeneralEntityWalkToProxy;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Random;


/**
 * Created by Asherslab on 5/6/17.
 */
public class EntityBarbarian extends EntityMob
{

    /**
     * Walk to proxy.
     */
    private GeneralEntityWalkToProxy proxy;
    BlockPos targetBlock;

    final Colony colony = ColonyManager.getClosestColony(world, this.getPosition());

    public EntityBarbarian(World worldIn)
    {
        super(worldIn);
    }

    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(35.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.13D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(2.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(this.getHealthBasedOnRaidLevel());
    }

    protected double getHealthBasedOnRaidLevel()
    {
        if (colony != null)
        {
            int raidLevel = (int) (colony.getRaidLevel()*(Configurations.barbarianHordeDifficulty * 0.2));
            return 25+raidLevel;
        }
        return 25.0D;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if(targetBlock != null)
        {
            this.isWorkerAtSiteWithMove(targetBlock, 2);
        }
        else
        {
            targetBlock = getRandomBuilding();
            this.isWorkerAtSiteWithMove(targetBlock, 2);
        }
    }

    /**
     * Checks if a worker is at his working site.
     * If he isn't, sets it's path to the location
     *
     * @param site  the place where he should walk to
     * @param range Range to check in
     * @return True if worker is at site, otherwise false.
     */
    public boolean isWorkerAtSiteWithMove(@NotNull final BlockPos site, final int range)
    {
        if (proxy == null)
        {
            proxy = new GeneralEntityWalkToProxy(this);
        }
        return proxy.walkToBlock(site, range, true);
    }

    @Override
    protected void initEntityAI()
    {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
        this.tasks.addTask(3, new EntityAIAttackMelee(this, 2.3D, true));
        //this.tasks.addTask(3, new EntityAIWalkToRandomHuts(this, 2.0D));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.applyEntityAI();
    }

    private void applyEntityAI()
    {
        this.tasks.addTask(6, new EntityAIMoveThroughVillage(this, 1.0D, false));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityCitizen.class, true));
    }

    @Override
    public World getEntityWorld()
    {
        return world;
    }

    @Override
    public int getMaxSpawnedInChunk()
    {
        return 5;
    }

    @Override
    protected boolean canDespawn()
    {
        return world.isDaytime();
    }

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
