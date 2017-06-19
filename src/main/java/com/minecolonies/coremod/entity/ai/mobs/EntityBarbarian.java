package com.minecolonies.coremod.entity.ai.mobs;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.entity.EntityCitizen;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityBarbarian extends EntityMob
{
    /**
     * Un-catagorized values for the barbarian
     */
    public static final ResourceLocation LOOT = new ResourceLocation(Constants.MOD_ID, "EntityBarbarianDrops");

    /**
     * Values used for AI Task's Priorities
     */
    private static final int PRIORITY_ZERO  = 0;
    private static final int PRIORITY_ONE   = 1;
    private static final int PRIORITY_TWO   = 2;
    private static final int PRIORITY_THREE = 3;
    private static final int PRIORITY_FOUR  = 4;
    private static final int PRIORITY_FIVE  = 5;
    private static final int PRIORITY_SIX   = 6;
    private static final int PRIORITY_SEVEN = 7;
    private static final int PRIORITY_EIGHT = 8;

    /**
     * Other various values used for AI Tasks
     */
    private static final double MOVE_TOWARDS_RESTRICTION_SPEED = 1.0D;
    private static final double MOVE_THROUGH_VILLAGE_SPEED     = 1.0D;
    private static final float  MAX_WATCH_DISTANCE             = 8.0F;

    /**
     * Values used for mob attributes
     */
    private static final double FOLLOW_RANGE          = 35.0D;
    private static final double MOVEMENT_SPEED        = 0.2D;
    private static final double ATTACK_DAMAGE         = 2.0D;
    private static final double ARMOR                 = 2.0D;
    private static final double BARBARIAN_BASE_HEALTH = 25;

    /**
     * Constructor method for Barbarian Entity
     *
     * @param worldIn The world that it is in/
     */
    public EntityBarbarian(final World worldIn)
    {
        super(worldIn);
    }

    @Override
    protected void initEntityAI()
    {
        this.tasks.addTask(PRIORITY_ZERO, new EntityAISwimming(this));
        this.tasks.addTask(PRIORITY_FIVE, new EntityAIMoveTowardsRestriction(this, MOVE_TOWARDS_RESTRICTION_SPEED));
        this.tasks.addTask(PRIORITY_SEVEN, new EntityAIWatchClosest(this, EntityPlayer.class, MAX_WATCH_DISTANCE));
        this.tasks.addTask(PRIORITY_EIGHT, new EntityAILookIdle(this));
        this.tasks.addTask(PRIORITY_SIX, new EntityAIMoveThroughVillage(this, MOVE_THROUGH_VILLAGE_SPEED, false));
        this.targetTasks.addTask(PRIORITY_THREE, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
        this.targetTasks.addTask(PRIORITY_FOUR, new EntityAINearestAttackableTarget<>(this, EntityCitizen.class, true));
    }

    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(FOLLOW_RANGE);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(MOVEMENT_SPEED);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(ARMOR);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(BARBARIAN_BASE_HEALTH);
    }

    @Nullable
    @Override
    protected ResourceLocation getLootTable()
    {
        return LOOT;
    }
}
