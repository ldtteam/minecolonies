package com.minecolonies.coremod.entity.ai.mobs.barbarians;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.entity.ai.mobs.util.BarbarianSpawnUtils;
import com.minecolonies.coremod.entity.ai.mobs.util.BarbarianUtils;
import com.minecolonies.coremod.items.ItemChiefSword;
import com.minecolonies.coremod.sounds.BarbarianSounds;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.stream.Stream;

/**
 * Abstract for all Barbarian entities.
 */
public abstract class AbstractEntityBarbarian extends EntityMob
{
    /**
     * String to store the existing time to NBT.
     */
    private static final String TAG_TIME = "time";

    /**
     * The amount of EXP to drop on entity death.
     */
    private static final int BARBARIAN_EXP_DROP = 1;

    private static final int BARBARIAN_HORDE_DIFFICULTY_FIVE = 5;

    /**
     * Values used to choose whether or not to play sound
     */
    private static final int OUT_OF_ONE_HUNDRED = 100;

    private static final int ONE = 1;

    /**
     * Values used for sword effect.
     */
    private static final Potion SPEED_EFFECT                = Potion.getPotionById(1);
    private static final int    TIME_TO_COUNTDOWN           = 240;
    private static final int    COUNTDOWN_SECOND_MULTIPLIER = 4;
    private static final int    SPEED_EFFECT_DISTANCE       = 7;
    private static final int    SPEED_EFFECT_DURATION       = 160;
    private static final int    SPEED_EFFECT_MULTIPLIER     = 2;

    /**
     * Amount of ticks to despawn the barbarian.
     */
    private static final int TICKS_TO_DESPAWN = Constants.TICKS_SECOND * Constants.SECONDS_A_MINUTE * 10;

    /**
     * Randomly execute it every this ticks.
     */
    private static final int EVERY_X_TICKS = 20;

    /**
     * Sets the barbarians target colony on spawn Thus it never changes.
     */
    private Colony colony;

    /**
     * Random object.
     */
    private final Random random = new Random();
    /**
     * Current count of ticks.
     */
    private int currentCount = 0;
    /**
     * The world time when the barbarian spawns.
     */
    private long worldTimeAtSpawn = 0;
    /**
     * The current tick since creation.
     */
    private int currentTick = 1;

    /**
     * Constructor method for Abstract Barbarians.
     *
     * @param world the world.
     */
    public AbstractEntityBarbarian(final World world)
    {
        super(world);
    }

    @Override
    protected void initEntityAI()
    {
        BarbarianSpawnUtils.setBarbarianAI(this);
    }

    @Override
    protected void entityInit()
    {
        worldTimeAtSpawn = world.getTotalWorldTime();
        this.enablePersistence();
        super.entityInit();
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

    @Nullable
    @Override
    protected SoundEvent getAmbientSound()
    {
        return BarbarianSounds.barbarianSay;
    }

    @Nullable
    @Override
    protected ResourceLocation getLootTable()
    {
        return BarbarianSpawnUtils.getBarbarianLootTable(this);
    }

    @Override
    protected boolean canDespawn()
    {
        return shouldDespawn() || getColony() == null;
    }

    public Colony getColony()
    {
        if (colony == null)
        {
            colony = ColonyManager.getClosestColony(CompatibilityUtils.getWorld(this), this.getPosition());
        }

        return colony;
    }

    /**
     * Should the barbs despawn.
     *
     * @return true if so.
     */
    private boolean shouldDespawn()
    {
        return worldTimeAtSpawn != 0 && (world.getTotalWorldTime() - worldTimeAtSpawn) >= TICKS_TO_DESPAWN;
    }

    /**
     * We have loot_tables for a reason, this is done to disable equipped items dropping on death.
     *
     * @param wasRecentlyHit  Was the barbarian recently hit?
     * @param lootingModifier Was the barbarian hit with a sword with looting Enchantment?
     */
    @Override
    protected void dropEquipment(final boolean wasRecentlyHit, final int lootingModifier)
    {
        // We have loot_tables for a reason, this is done to disable equipped items dropping on death.
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(final DifficultyInstance difficulty, @Nullable final IEntityLivingData livingdata)
    {
        BarbarianSpawnUtils.setBarbarianEquipment(this);
        return super.onInitialSpawn(difficulty, livingdata);
    }

    @Override
    public void onLivingUpdate()
    {
        if (currentTick % (random.nextInt(EVERY_X_TICKS) + 1) == 0)
        {
            if (worldTimeAtSpawn == 0)
            {
                worldTimeAtSpawn = world.getTotalWorldTime();
            }

            if (shouldDespawn())
            {
                this.setDead();
            }

            if (this.getHeldItemMainhand() != null && SPEED_EFFECT != null && this.getHeldItemMainhand().getItem() instanceof ItemChiefSword
                  && Configurations.gameplay.barbarianHordeDifficulty >= BARBARIAN_HORDE_DIFFICULTY_FIVE
                  && currentCount <= 0)
            {
                final Stream<AbstractEntityBarbarian> barbarians = BarbarianUtils.getBarbariansCloseToEntity(this, SPEED_EFFECT_DISTANCE).stream();
                barbarians.forEach(entity -> entity.addPotionEffect(new PotionEffect(SPEED_EFFECT, SPEED_EFFECT_DURATION, SPEED_EFFECT_MULTIPLIER)));
                currentCount = COUNTDOWN_SECOND_MULTIPLIER * TIME_TO_COUNTDOWN;
            }
            else
            {
                --currentCount;
            }
        }
        currentTick++;

        super.onLivingUpdate();
    }

    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSourceIn)
    {
        return BarbarianSounds.barbarianHurt;
    }

    @Override
    protected SoundEvent getDeathSound()
    {
        return BarbarianSounds.barbarianDeath;
    }

    public void applyInternalEntityAttributes()
    {
        BarbarianSpawnUtils.setBarbarianAttributes(this, getColony());
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound compound)
    {
        compound.setLong(TAG_TIME, worldTimeAtSpawn);
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(final NBTTagCompound compound)
    {
        worldTimeAtSpawn = compound.getLong(TAG_TIME);
        super.readFromNBT(compound);
    }

    @Override
    protected void onDeathUpdate()
    {
        if (!(this.getAttackingEntity() instanceof EntityPlayer) && (this.recentlyHit > 0 && this.canDropLoot() && world.getGameRules().getBoolean("doMobLoot")))
        {
            final int experience = EntityXPOrb.getXPSplit(BARBARIAN_EXP_DROP);
            CompatibilityUtils.spawnEntity(world, new EntityXPOrb(world, this.posX, this.posY, this.posZ, experience));
        }
        super.onDeathUpdate();
    }
}
