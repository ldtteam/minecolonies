package com.minecolonies.coremod.entity.ai.mobs;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.items.ItemChiefSword;
import com.minecolonies.coremod.items.ModItems;
import com.minecolonies.coremod.sounds.BarbarianSounds;
import com.minecolonies.coremod.util.BarbarianUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class EntityChiefBarbarian extends EntityMob
{
    private final        Colony           colony                          = ColonyManager.getClosestColony(world, this.getPosition());
    public static final  ResourceLocation LOOT                            = new ResourceLocation(Constants.MOD_ID, "EntityChiefBarbarianDrops");
    private static final Potion           SPEED_EFFECT                    = Potion.getPotionById(1);
    private static final int              TIME_TO_COUNTDOWN               = 30;
    private static final int              COUNTDOWN_SECOND_MULTIPLIER     = 4;
    private static final int              SPEED_EFFECT_DISTANCE           = 7;
    private static final int              SPEED_EFFECT_DURATION           = 32;
    private static final int              SPEED_EFFECT_MULTIPLIER         = 2;
    private static final int              BARBARIAN_HORDE_DIFFICULTY_FIVE = 5;

    private int currentCount = 0;

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
     * Values used to choose whether or not to play sound
     */
    private static final int OUT_OF_ONE_HUNDRED = 100;
    private static final int ONE                = 1;

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

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(final DifficultyInstance difficulty, @Nullable final IEntityLivingData livingdata)
    {
        this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.chiefSword));
        return super.onInitialSpawn(difficulty, livingdata);
    }

    /**
     * Done so that the Chief does not drop his equipped Chief Sword, because it's supposed to be rare
     *
     * @param wasRecentlyHit  Boolean value, of whether it was recently hit
     * @param lootingModifier The modifier applied by a looting enchantment
     */
    @Override
    protected void dropEquipment(final boolean wasRecentlyHit, final int lootingModifier)
    {
        //Do not drop Equipment because we don't want the Chief Sword dropped any time other that 1 in 1000.
    }

    @Override
    public void onLivingUpdate()
    {
        if (this.getHeldItemMainhand().getItem() instanceof ItemChiefSword && Configurations.barbarianHordeDifficulty >= BARBARIAN_HORDE_DIFFICULTY_FIVE && currentCount <= 0)
        {
            final Stream<EntityLivingBase> barbarians = BarbarianUtils.getBarbariansCloseToEntity(this, SPEED_EFFECT_DISTANCE);
            barbarians.forEach(entity -> entity.addPotionEffect(new PotionEffect(SPEED_EFFECT, SPEED_EFFECT_DURATION, SPEED_EFFECT_MULTIPLIER)));
            currentCount = COUNTDOWN_SECOND_MULTIPLIER * TIME_TO_COUNTDOWN;
        }
        else
        {
            --currentCount;
        }

        super.onLivingUpdate();
    }

    /**
     * Sets the entity's health based on the raidLevel
     *
     * @return returns the health in the form of a double
     */
    private double getHealthBasedOnRaidLevel()
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
    public void playLivingSound()
    {
        final SoundEvent soundevent = this.getAmbientSound();

        if (soundevent != null && world.rand.nextInt(OUT_OF_ONE_HUNDRED) <= ONE)
        {
            this.playSound(soundevent, this.getSoundVolume(), this.getSoundPitch());
        }
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable()
    {
        return LOOT;
    }
}
