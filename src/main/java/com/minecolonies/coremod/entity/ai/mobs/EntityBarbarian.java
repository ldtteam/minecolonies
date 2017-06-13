package com.minecolonies.coremod.entity.ai.mobs;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.*;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.pathfinding.GeneralEntityWalkToProxy;
import com.minecolonies.coremod.entity.pathfinding.PathNavigate;
import net.minecraft.entity.EntityLiving;
import com.minecolonies.coremod.sounds.BarbarianSounds;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
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
    private BlockPos targetBlock;

    private final Colony colony = ColonyManager.getClosestColony(world, this.getPosition());

    public EntityBarbarian(World worldIn)
    {
        super(worldIn);
    }

    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(35.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(2.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(this.getHealthBasedOnRaidLevel());
    }

    @Override
    protected void initEntityAI()
    {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
        this.tasks.addTask(3, new EntityAIAttackMelee(this, 2.3D, true));
        this.tasks.addTask(4, new EntityAIWalkToRandomHuts(this, 2.0D));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.applyEntityAI();
    }

    @Override
    protected SoundEvent getHurtSound() {
        return BarbarianSounds.barbarianHurt;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return BarbarianSounds.barbarianDeath;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return BarbarianSounds.barbarianSay;
    }

    protected double getHealthBasedOnRaidLevel()
    {
        if (colony != null)
        {
            int raidLevel = (int) (colony.getRaidLevel() * (Configurations.barbarianHordeDifficulty * 0.2));
            return 25 + raidLevel;
        }
        return 25.0D;
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
        if (buildingArray != null && buildingArray.length != 0)
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
}
