package com.minecolonies.api.entity.mobs;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.entity.CustomGoalSelector;
import com.minecolonies.api.entity.mobs.util.BarbarianUtils;
import com.minecolonies.api.entity.pathfinding.AbstractAdvancedPathNavigate;
import com.minecolonies.api.entity.pathfinding.registry.IPathNavigateRegistry;
import com.minecolonies.api.items.IChiefSwordItem;
import com.minecolonies.api.sounds.BarbarianSounds;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.MobSpawnUtils;
import net.minecraft.entity.*;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.GameRules;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Random;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.RaiderConstants.*;

/**
 * Abstract for all Barbarian entities.
 */
public abstract class AbstractEntityMinecoloniesMob extends MobEntity
{
    /**
     * The New PathNavigate navigator.
     */
    private AbstractAdvancedPathNavigate newNavigator;

    /**
     * Sets the barbarians target colony on spawn Thus it never changes.
     */
    private IColony colony;

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
    public AbstractEntityMinecoloniesMob(final EntityType type, final World world)
    {
        super(type, world);
        worldTimeAtSpawn = world.getGameTime();
        this.enablePersistence();
        this.goalSelector = new CustomGoalSelector(this.goalSelector);
        this.targetSelector = new CustomGoalSelector(this.targetSelector);
        MobSpawnUtils.setupMobAi(this);
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
    public void playAmbientSound()
    {
        super.playAmbientSound();
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

    @Override
    public boolean canDespawn(final double distanceToClosestPlayer)
    {
        return shouldDespawn() || (world != null && world.isAreaLoaded(this.getPosition(), 3) && getColony() == null );
    }

    /**
     * Should the barbs despawn.
     *
     * @return true if so.
     */
    private boolean shouldDespawn()
    {
        return worldTimeAtSpawn != 0 && (world.getGameTime() - worldTimeAtSpawn) >= TICKS_TO_DESPAWN;
    }

    @NotNull
    @Override
    public AbstractAdvancedPathNavigate getNavigator()
    {
        if (this.newNavigator == null)
        {
            this.newNavigator = IPathNavigateRegistry.getInstance().getNavigateFor(this);
            this.navigator = newNavigator;
            this.newNavigator.setCanSwim(true);
            this.newNavigator.getNodeProcessor().setCanOpenDoors(false);
        }
        return newNavigator;
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

    @NotNull
    @Override
    public void writeAdditional(final CompoundNBT compound)
    {
        compound.putLong(TAG_TIME, worldTimeAtSpawn);
        compound.putInt(TAG_STUCK_COUNTER, stuckCounter);
        compound.putInt(TAG_LADDER_COUNTER, ladderCounter);
        compound.putInt(TAG_COLONY_ID, this.colony == null ? 0 : colony.getID());
        super.writeAdditional(compound);
    }

    @Override
    public void readAdditional(final CompoundNBT compound)
    {
        worldTimeAtSpawn = compound.getLong(TAG_TIME);
        stuckCounter = compound.getInt(TAG_STUCK_COUNTER);
        ladderCounter = compound.getInt(TAG_LADDER_COUNTER);
        if (compound.keySet().contains(TAG_COLONY_ID))
        {
            final int colonyId = compound.getInt(TAG_COLONY_ID);
            if (colonyId != 0)
            {
                setColony(IColonyManager.getInstance().getColonyByWorld(colonyId, world));
            }
        }
        super.readAdditional(compound);
    }

    @Override
    public void livingTick()
    {
        if (world.isRemote)
        {
            super.livingTick();
            return;
        }

        if (currentTick % (random.nextInt(EVERY_X_TICKS) + 1) == 0)
        {
            if (worldTimeAtSpawn == 0)
            {
                worldTimeAtSpawn = world.getGameTime();
            }

            if (shouldDespawn())
            {
                this.remove();
            }

            if (currentCount <= 0)
            {
                currentCount = COUNTDOWN_SECOND_MULTIPLIER * TIME_TO_COUNTDOWN;
                MobSpawnUtils.setMobAttributes(this, getColony());

                if (!this.getHeldItemMainhand().isEmpty() && SPEED_EFFECT != null && this.getHeldItemMainhand().getItem() instanceof IChiefSwordItem
                      && MinecoloniesAPIProxy.getInstance().getConfig().getCommon().barbarianHordeDifficulty.get() >= BARBARIAN_HORDE_DIFFICULTY_FIVE)
                {
                    BarbarianUtils.getBarbariansCloseToEntity(this, SPEED_EFFECT_DISTANCE)
                      .stream().filter(entity -> !entity.isPotionActive(Effects.SPEED))
                      .forEach(entity -> entity.addPotionEffect(new EffectInstance(Effects.SPEED, SPEED_EFFECT_DURATION, SPEED_EFFECT_MULTIPLIER)));
                }
            }
            else
            {
                --currentCount;
            }
        }
        currentTick++;

        super.livingTick();
    }

    @Nullable
    @Override
    public ILivingEntityData onInitialSpawn(
      final IWorld worldIn, final DifficultyInstance difficultyIn, final SpawnReason reason, @Nullable final ILivingEntityData spawnDataIn, @Nullable final CompoundNBT dataTag)
    {
        MobSpawnUtils.setEquipment(this);
        return super.onInitialSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
    }

    @Override
    protected void onDeathUpdate()
    {
        if (!(this.getAttackingEntity() instanceof PlayerEntity) && (this.recentlyHit > 0 && this.canDropLoot() && world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)))
        {
            final int experience = ExperienceOrbEntity.getXPSplit(BARBARIAN_EXP_DROP);
            CompatibilityUtils.addEntity(world, new ExperienceOrbEntity(world, this.posX, this.posY, this.posZ, experience));
        }
        super.onDeathUpdate();
    }

    /**
     * Getter for the colony.
     * Gets a value from the ColonyManager if null.
     * @return the colony the barbarian is assigned to attack.e
     */
    public IColony getColony()
    {
        if (!world.isRemote && colony == null)
        {
            this.setColony(IColonyManager.getInstance().getClosestColony(CompatibilityUtils.getWorldFromEntity(this), this.getPosition()));
        }

        return colony;
    }

    /**
     * Registers the entity with the colony.
     */
    public void registerWithColony()
    {
        getColony();
    }

    @Override
    public void onDeath(@NotNull final DamageSource cause)
    {
        super.onDeath(cause);
        if (!world.isRemote && getColony() != null)
        {
            getColony().getRaiderManager().unregisterRaider(this, (ServerWorld) world);
        }
    }

    @Override
    protected void registerAttributes()
    {
        super.registerAttributes();
        this.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
    }

    /**
     * Set the colony to raid.
     * @param colony the colony to set.
     */
    public void setColony(final IColony colony)
    {
        if (colony != null)
        {
            this.colony = colony;
            this.colony.getRaiderManager().registerRaider(this);
        }
    }
}
