package com.minecolonies.coremod.entity.ai.mobs;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.entity.EntityCitizen;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * Abstract Class implementing the Archer Barbarian
 */
public abstract class AbstractArcherBarbarian extends EntityMob implements IRangedAttackMob
{
    /* default */ private final        Colony                           colony                = ColonyManager.getClosestColony(world, this.getPosition());
    /* default */ private static final DataParameter<Boolean>           SWINGING_ARMS         =
      EntityDataManager.<Boolean>createKey(AbstractArcherBarbarian.class, DataSerializers.BOOLEAN);
    /* default */ private final        EntityAIAttackRangedBowBarbarian aiArrowAttack         = new EntityAIAttackRangedBowBarbarian(this, 1.0D, 20, 15.0F);
    /* default */ private static final float                            ENTITY_WIDTH          = 0.6F;
    /* default */ private static final float                            ENTITY_HEIGHT         = 1.99F;
    /* default */ private static final int                              DIFFICULTY_SUBTRACTER = 14;
    /* default */ private static final int                              DIFFICULTY_MULTIPLIER = 4;
    /* default */ private static final int                              BOW_DAMAGE            = 20;
    /* default */ private static final int                              BOW_DAMAGE_HARD       = 40;
    /* default */ private static final double                           GRAVITY_MULTIPLIER    = 0.20000000298023224D;
    /* default */ private static final double                           ENTITY_Y_OFFSET       = -0.6D;
    /* default */ private static final float                            ARROW_VELOCITY        = 1.6F;
    /* default */ private static final float                            SOUND_MULTIPLIER      = 0.4F;
    /* default */ private static final float                            SOUND_ADDER           = 0.8F;
    /* default */ private static final float                            ENTITY_EYE_HEIGHT     = 1.74F;
    /* default */ private static final float                            RANDOM_FLOAT_EQUATER  = 0.55F;

    /**
     * Defines the default values for the Entity's attributes.
     */
    /* default */ private static final double FOLLOW_RANGE          = 35.0D;
    /* default */ private static final double MOVEMENT_SPEED        = 0.2D;
    /* default */ private static final double ATTACK_DAMAGE         = 1.0D;
    /* default */ private static final double ARMOR                 = 2.0D;
    /* default */ private static final double BARBARIAN_BASE_HEALTH = 25;

    /**
     * Defines the default values for the various AI Task's properties
     */
    /* default */ private static final int   PRIORITY_ONE       = 1;
    /* default */ private static final int   PRIORITY_TWO       = 2;
    /* default */ private static final int   PRIORITY_THREE     = 3;
    /* default */ private static final int   PRIORITY_FOUR      = 3;
    /* default */ private static final int   PRIORITY_FIVE      = 5;
    /* default */ private static final int   PRIORITY_SIX       = 6;
    /* default */ private static final float MAX_WATCH_DISTANCE = 8.0F;

    private final EntityAIAttackMelee aiAttackOnCollide = new EntityAIAttackMelee(this, 1.2D, false)
    {
        /**
         * Resets the task
         */
        public void resetTask()
        {
            super.resetTask();
            AbstractArcherBarbarian.this.setSwingingArms(false);
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        public void startExecuting()
        {
            super.startExecuting();
            AbstractArcherBarbarian.this.setSwingingArms(true);
        }
    };

    public AbstractArcherBarbarian(final World worldIn)
    {
        super(worldIn);
        this.setSize(ENTITY_WIDTH, ENTITY_HEIGHT);
    }

    protected void initEntityAI()
    {
        this.tasks.addTask(PRIORITY_ONE, new EntityAISwimming(this));
        this.tasks.addTask(PRIORITY_TWO, new EntityAIRestrictSun(this));
        this.tasks.addTask(PRIORITY_THREE, new EntityAIAvoidEntity(this, EntityWolf.class, 6.0F, 1.0D, 1.2D));
        this.tasks.addTask(PRIORITY_FIVE, new EntityAIWalkToRandomHuts(this, 2.0D));
        this.tasks.addTask(PRIORITY_SIX, new EntityAIWatchClosest(this, EntityPlayer.class, MAX_WATCH_DISTANCE));
        this.tasks.addTask(PRIORITY_SIX, new EntityAILookIdle(this));
        this.targetTasks.addTask(PRIORITY_ONE, new EntityAIHurtByTarget(this, false, new Class[0]));
        this.targetTasks.addTask(PRIORITY_TWO, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
        this.targetTasks.addTask(PRIORITY_THREE, new EntityAINearestAttackableTarget(this, EntityCitizen.class, true));
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

    protected double getHealthBasedOnRaidLevel()
    {
        if (colony != null)
        {
            final int raidLevel = (int) (colony.getRaidLevel() * 1.5);
            return BARBARIAN_BASE_HEALTH + raidLevel;
        }
        return BARBARIAN_BASE_HEALTH;
    }

    protected void entityInit()
    {
        super.entityInit();
        this.dataManager.register(SWINGING_ARMS, Boolean.FALSE);
    }

    /**
     * Get this Entity's EnumCreatureAttribute
     */
    public EnumCreatureAttribute getCreatureAttribute()
    {
        return EnumCreatureAttribute.UNDEAD;
    }

    /**
     * Handles updating while being ridden by an entity
     */
    public void updateRidden()
    {
        super.updateRidden();

        if (this.getRidingEntity() instanceof EntityCreature)
        {
            final EntityCreature entitycreature = (EntityCreature) this.getRidingEntity();
            this.renderYawOffset = entitycreature.renderYawOffset;
        }
    }

    /**
     * Called when the mob's health reaches 0.
     */
    public void onDeath(final DamageSource cause)
    {
        super.onDeath(cause);

        if (cause.getSourceOfDamage() instanceof EntityArrow && cause.getEntity() instanceof EntityPlayer)
        {
            final EntityPlayer entityplayer = (EntityPlayer) cause.getEntity();
            final double d0 = entityplayer.posX - this.posX;
            final double d1 = entityplayer.posZ - this.posZ;

            final double distance = 2500.0D;

            if (d0 * d0 + d1 * d1 >= distance)
            {
                entityplayer.addStat(AchievementList.SNIPE_SKELETON);
            }
        }
    }

    /**
     * Gives armor or weapon for entity based on given DifficultyInstance
     */
    protected void setEquipmentBasedOnDifficulty(final DifficultyInstance difficulty)
    {
        super.setEquipmentBasedOnDifficulty(difficulty);
        this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
    }

    /**
     * Called only once on an entity when first time spawned, via egg, mob spawner, natural spawning etc, but not called
     * when entity is reloaded from nbt. Mainly used for initializing attributes and inventory
     */
    @Nullable
    public IEntityLivingData onInitialSpawn(final DifficultyInstance difficulty, @Nullable final IEntityLivingData living)
    {
        final IEntityLivingData livingdata = super.onInitialSpawn(difficulty, living);
        this.setEquipmentBasedOnDifficulty(difficulty);
        this.setEnchantmentBasedOnDifficulty(difficulty);
        this.setCombatTask();
        this.setCanPickUpLoot(this.rand.nextFloat() < RANDOM_FLOAT_EQUATER * difficulty.getClampedAdditionalDifficulty());

        return livingdata;
    }

    /**
     * sets this entity's combat AI.
     */
    public void setCombatTask()
    {
        if (this.world != null && !this.world.isRemote)
        {
            this.tasks.removeTask(this.aiAttackOnCollide);
            this.tasks.removeTask(this.aiArrowAttack);
            final ItemStack itemstack = this.getHeldItemMainhand();

            if (itemstack.getItem() == Items.BOW)
            {
                int i = BOW_DAMAGE;

                if (this.world.getDifficulty() != EnumDifficulty.HARD)
                {
                    i = BOW_DAMAGE_HARD;
                }

                this.aiArrowAttack.setAttackCooldown(i);
                this.tasks.addTask(PRIORITY_FOUR, this.aiArrowAttack);
            }
            else
            {
                this.tasks.addTask(PRIORITY_FOUR, this.aiAttackOnCollide);
            }
        }
    }

    /**
     * Attack the specified entity using a ranged attack.
     */
    public void attackEntityWithRangedAttack(final EntityLivingBase target, final float distanceFactor)
    {
        final EntityArrow entityarrow = this.getArrow(distanceFactor);
        final double d0 = target.posX - this.posX;
        final double d1 = target.getEntityBoundingBox().minY + (double) (target.height / 3.0F) - entityarrow.posY;
        final double d2 = target.posZ - this.posZ;
        final double d3 = (double) MathHelper.sqrt(d0 * d0 + d2 * d2);
        entityarrow.setThrowableHeading(d0,
          d1 + d3 * GRAVITY_MULTIPLIER,
          d2,
          ARROW_VELOCITY,
          (float) (DIFFICULTY_SUBTRACTER - this.world.getDifficulty().getDifficultyId() * DIFFICULTY_MULTIPLIER));
        this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * SOUND_MULTIPLIER + SOUND_ADDER));
        this.world.spawnEntity(entityarrow);
    }

    protected EntityArrow getArrow(final float arrow)
    {
        final EntityTippedArrow entitytippedarrow = new EntityTippedArrow(this.world, this);
        entitytippedarrow.setEnchantmentEffectsFromEntity(this, arrow);
        return entitytippedarrow;
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(final NBTTagCompound compound)
    {
        super.readEntityFromNBT(compound);
        this.setCombatTask();
    }

    public void setItemStackToSlot(final EntityEquipmentSlot slotIn, final ItemStack stack)
    {
        super.setItemStackToSlot(slotIn, stack);

        if (!this.world.isRemote && slotIn == EntityEquipmentSlot.MAINHAND)
        {
            this.setCombatTask();
        }
    }

    public float getEyeHeight()
    {
        return ENTITY_EYE_HEIGHT;
    }

    /**
     * Returns the Y Offset of this entity.
     */
    public double getYOffset()
    {
        return ENTITY_Y_OFFSET;
    }

    @SideOnly(Side.CLIENT)
    public boolean isSwingingArms()
    {
        return ((Boolean) this.dataManager.get(SWINGING_ARMS)).booleanValue();
    }

    public void setSwingingArms(final boolean swingingArms)
    {
        this.dataManager.set(SWINGING_ARMS, Boolean.valueOf(swingingArms));
    }
}
