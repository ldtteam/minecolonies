package com.minecolonies.coremod.entity.ai.mobs;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.entity.ai.mobs.util.BarbarianUtils;
import com.minecolonies.coremod.entity.ai.mobs.util.MobSpawnUtils;
import com.minecolonies.coremod.entity.pathfinding.PathNavigate;
import com.minecolonies.coremod.items.ItemChiefSword;
import com.minecolonies.coremod.sounds.BarbarianSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Random;

import static com.minecolonies.api.util.constant.BarbarianConstants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Abstract for all Barbarian entities.
 */
public abstract class AbstractEntityMinecoloniesMob extends EntityMob
{
    /**
     * The New PathNavigate navigator.
     */
    private PathNavigate newNavigator;

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
    private int currentTick = 0;

    /**
     * Amount of time the barb got stuck.
     */
    private int stuckCounter = 1;

    /**
     * Amount of time the barb got stuck.
     */
    private int ladderCounter = 0;

    /**
     * Constructor method for Abstract Barbarians.
     *
     * @param world the world.
     */
    public AbstractEntityMinecoloniesMob(final World world)
    {
        super(world);
    }

    @Override
    protected void initEntityAI()
    {
        MobSpawnUtils.setMobAI(this);
    }

    @Override
    protected void entityInit()
    {
        worldTimeAtSpawn = world.getTotalWorldTime();
        this.enablePersistence();
        super.entityInit();
    }

    @Override
    public void applyEntityCollision(@NotNull final Entity entityIn)
    {
        if (entityIn instanceof AbstractEntityMinecoloniesMob
              && ((stuckCounter > 0 || ladderCounter > 0 || ((AbstractEntityMinecoloniesMob) entityIn).stuckCounter > 0 || ((AbstractEntityMinecoloniesMob) entityIn).ladderCounter > 0)))
        {
            return;
        }
        super.applyEntityCollision(entityIn);
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
        return MobSpawnUtils.getBarbarianLootTable(this);
    }

    @Override
    protected boolean canDespawn()
    {
        return shouldDespawn() || getColony() == null;
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

    @Override
    public void onLivingUpdate()
    {
        if(world.isRemote)
        {
            super.onLivingUpdate();
            return;
        }

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

            if(currentCount <= 0)
            {
                currentCount = COUNTDOWN_SECOND_MULTIPLIER * TIME_TO_COUNTDOWN;
                MobSpawnUtils.setMobAttributes(this, getColony());

                if (this.getHeldItemMainhand() != null && SPEED_EFFECT != null && this.getHeldItemMainhand().getItem() instanceof ItemChiefSword
                        && Configurations.gameplay.barbarianHordeDifficulty >= BARBARIAN_HORDE_DIFFICULTY_FIVE)
                {
                    BarbarianUtils.getBarbariansCloseToEntity(this, SPEED_EFFECT_DISTANCE)
                            .stream().filter(entity -> !entity.isPotionActive(SPEED_EFFECT))
                            .forEach(entity -> entity.addPotionEffect(new PotionEffect(SPEED_EFFECT, SPEED_EFFECT_DURATION, SPEED_EFFECT_MULTIPLIER)));
                }
            }
            else
            {
                --currentCount;
            }
        }
        currentTick++;

        super.onLivingUpdate();
    }

    /**
     * Get the stack counter.
     * @return the amount it got stuck already.
     */
    public int getStuckCounter()
    {
        return stuckCounter;
    }

    /**
     * Set the stack counter.
     * @param stuckCounter the amount.
     */
    public void setStuckCounter(final int stuckCounter)
    {
        this.stuckCounter = stuckCounter;
    }

    /**
     * Get the ladder counter.
     * @return the amount it got stuck and placed a ladder already.
     */
    public int getLadderCounter()
    {
        return ladderCounter;
    }

    /**
     * Set the ladder counter.
     * @param ladderCounter the amount.
     */
    public void setLadderCounter(final int ladderCounter)
    {
        this.ladderCounter = ladderCounter;
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

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound compound)
    {
        compound.setLong(TAG_TIME, worldTimeAtSpawn);
        compound.setInteger(TAG_STUCK_COUNTER, stuckCounter);
        compound.setInteger(TAG_LADDER_COUNTER, ladderCounter);
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(final NBTTagCompound compound)
    {
        worldTimeAtSpawn = compound.getLong(TAG_TIME);
        stuckCounter = compound.getInteger(TAG_STUCK_COUNTER);
        ladderCounter = compound.getInteger(TAG_LADDER_COUNTER);

        super.readFromNBT(compound);
    }

    @NotNull
    @Override
    public PathNavigate getNavigator()
    {
        if (this.newNavigator == null)
        {
            this.newNavigator = new PathNavigate(this, world);
            this.navigator = newNavigator;
            this.newNavigator.setCanSwim(true);
            this.newNavigator.setEnterDoors(false);
        }
        return newNavigator;
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(final DifficultyInstance difficulty, @Nullable final IEntityLivingData livingdata)
    {
        MobSpawnUtils.setEquipment(this);
        return super.onInitialSpawn(difficulty, livingdata);
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

    public Colony getColony()
    {
        if (!world.isRemote && colony == null)
        {
            colony = ColonyManager.getClosestColony(CompatibilityUtils.getWorld(this), this.getPosition());
            if (colony != null)
            {
                colony.getRaiderManager().registerRaider(this);
            }
        }

        return colony;
    }

    @Override
    public void onDeath(final DamageSource cause)
    {
        super.onDeath(cause);
        if (!world.isRemote && getColony() != null)
        {
            getColony().getRaiderManager().unregisterRaider(this, (WorldServer) world);
        }
    }
}
