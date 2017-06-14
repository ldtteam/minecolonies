package com.minecolonies.coremod.entity.ai.mobs;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.sounds.BarbarianSounds;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Created by Asherslab on 5/6/17.
 */
public class EntityBarbarian extends EntityMob
{
    private final Colony colony = ColonyManager.getClosestColony(world, this.getPosition());

    /**
     * defines the default values for the Entity's attributes.
     */
    /* default */ private static final double FOLLOW_RANGE          = 35.0D;
    /* default */ private static final double MOVEMENT_SPEED        = 0.2D;
    /* default */ private static final double ATTACK_DAMAGE         = 2.0D;
    /* default */ private static final double ARMOR                 = 2.0D;
    /* default */ private static final double BARBARIAN_BASE_HEALTH = 25;

    /**
     * Defines the default values for the various AI Task's priorities.
     */
    /* default */ private static final int PRIORITY_ZERO   = 1;
    /* default */ private static final int PRIORITY_TWO   = 2;
    /* default */ private static final int PRIORITY_THREE = 3;
    /* default */ private static final int PRIORITY_FOUR = 4;
    /* default */ private static final int PRIORITY_FIVE = 5;
    /* default */ private static final int PRIORITY_SIX   = 6;
    /* default */ private static final int PRIORITY_EIGHT  = 8;

    public static final ResourceLocation LOOT = new ResourceLocation(Constants.MOD_ID, "EntityBarbarianDrops");

    public EntityBarbarian(final World worldIn)
    {
        super(worldIn);
    }

    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(FOLLOW_RANGE);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(MOVEMENT_SPEED);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(ARMOR);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(this.getHealthBasedOnRaidLevel());
    }

    @Override
    protected void initEntityAI()
    {
        this.tasks.addTask(PRIORITY_ZERO, new EntityAISwimming(this));
        this.tasks.addTask(PRIORITY_FIVE, new EntityAIMoveTowardsRestriction(this, 1.0D));
        this.tasks.addTask(PRIORITY_TWO, new EntityAIBarbarianAttackMelee(this));
        this.tasks.addTask(PRIORITY_FOUR, new EntityAIWalkToRandomHuts(this, 2.0D));
        this.tasks.addTask(PRIORITY_EIGHT, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(PRIORITY_EIGHT, new EntityAILookIdle(this));
        this.tasks.addTask(PRIORITY_SIX, new EntityAIMoveThroughVillage(this, 1.0D, false));
        this.targetTasks.addTask(PRIORITY_TWO, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
        this.targetTasks.addTask(PRIORITY_THREE, new EntityAINearestAttackableTarget(this, EntityCitizen.class, true));
    }

    @Override
    protected SoundEvent getHurtSound()
    {
        return BarbarianSounds.barbarianHurt;
    }

    @Override
    protected SoundEvent getDeathSound()
    {
        return BarbarianSounds.barbarianDeath;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound()
    {
        return BarbarianSounds.barbarianSay;
    }

    private double getHealthBasedOnRaidLevel()
    {
        if (colony != null)
        {
            final int raidLevel = (int) (colony.getRaidLevel() * (Configurations.barbarianHordeDifficulty * 0.2));
            return BARBARIAN_BASE_HEALTH + raidLevel;
        }
        return BARBARIAN_BASE_HEALTH;
    }

    @Override
    protected boolean canDespawn()
    {
        return world.isDaytime();
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable()
    {
        return LOOT;
    }
}
