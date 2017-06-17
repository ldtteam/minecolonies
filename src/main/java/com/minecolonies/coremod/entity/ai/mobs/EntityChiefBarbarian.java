package com.minecolonies.coremod.entity.ai.mobs;

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

public class EntityChiefBarbarian extends EntityMob
{
    private final       Colony           colony = ColonyManager.getClosestColony(world, this.getPosition());
    public static final ResourceLocation LOOT   = new ResourceLocation(Constants.MOD_ID, "EntityChiefBarbarianDrops");

    /**
     * defines the default values for the Entity's attributes.
     */
    private static final double FOLLOW_RANGE          = 35.0D;
    private static final double MOVEMENT_SPEED        = 0.2D;
    private static final double ATTACK_DAMAGE         = 2.0D;
    private static final double ARMOR                 = 2.0D;
    private static final double BARBARIAN_BASE_HEALTH = 25;

    /**
     * Defines the default values for the various AI Task's priorities.
     */
    private static final int   PRIORITY_ZERO      = 1;
    private static final int   PRIORITY_TWO       = 2;
    private static final int   PRIORITY_THREE     = 3;
    private static final int   PRIORITY_FOUR      = 4;
    private static final int   PRIORITY_FIVE      = 5;
    private static final int   PRIORITY_SIX       = 6;
    private static final int   PRIORITY_EIGHT     = 8;
    private static final float MAX_WATCH_DISTANCE = 8.0F;

    /**
     * Constructor method for entity
     *
     * @param worldIn The world that the entity is in
     */
    public EntityChiefBarbarian(final World worldIn)
    {
        super(worldIn);
        this.getAlwaysRenderNameTag();
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

    /**
     * Sets the entity's health based on the raidLevel
     * @return returns the health in the form of a double
     */
    protected double getHealthBasedOnRaidLevel()
    {
        if (colony != null)
        {
            final int raidLevel = (int) (colony.getRaidLevel() * 1.5);
            return BARBARIAN_BASE_HEALTH + raidLevel;
        }
        return BARBARIAN_BASE_HEALTH;
    }

    @Override
    protected void initEntityAI()
    {
        this.tasks.addTask(PRIORITY_ZERO, new EntityAISwimming(this));
        this.tasks.addTask(PRIORITY_FIVE, new EntityAIMoveTowardsRestriction(this, 1.0D));
        this.tasks.addTask(PRIORITY_THREE, new EntityAIBarbarianAttackMelee(this));
        this.tasks.addTask(PRIORITY_FOUR, new EntityAIWalkToRandomHuts(this, 2.0D));
        this.tasks.addTask(PRIORITY_EIGHT, new EntityAIWatchClosest(this, EntityPlayer.class, MAX_WATCH_DISTANCE));
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

    @Override
    @Nullable
    protected ResourceLocation getLootTable()
    {
        return LOOT;
    }
}
