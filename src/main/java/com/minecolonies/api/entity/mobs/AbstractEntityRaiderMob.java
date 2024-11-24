package com.minecolonies.api.entity.mobs;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.colonyEvents.IColonyCampFireRaidEvent;
import com.minecolonies.api.colony.colonyEvents.IColonyEvent;
import com.minecolonies.api.enchants.ModEnchants;
import com.minecolonies.api.entity.CustomGoalSelector;
import com.minecolonies.api.entity.ai.combat.CombatAIStates;
import com.minecolonies.api.entity.ai.combat.threat.IThreatTableEntity;
import com.minecolonies.api.entity.ai.combat.threat.ThreatTable;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateStateMachine;
import com.minecolonies.api.entity.other.AbstractFastMinecoloniesEntity;
import com.minecolonies.api.entity.pathfinding.registry.IPathNavigateRegistry;
import com.minecolonies.api.items.IChiefSwordItem;
import com.minecolonies.api.sounds.RaiderSounds;
import com.minecolonies.api.util.ColonyUtils;
import com.minecolonies.api.util.DamageSourceKeys;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.entity.pathfinding.navigation.AbstractAdvancedPathNavigate;
import com.minecolonies.core.entity.pathfinding.navigation.PathingStuckHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.util.ITeleporter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import static com.minecolonies.api.entity.citizen.AbstractEntityCitizen.ENTITY_AI_TICKRATE;
import static com.minecolonies.api.entity.mobs.RaiderMobUtils.MOB_ATTACK_DAMAGE;
import static com.minecolonies.api.util.constant.ColonyManagerConstants.NO_COLONY_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.RaiderConstants.*;

/**
 * Abstract for all raider entities.
 */
public abstract class AbstractEntityRaiderMob extends AbstractFastMinecoloniesEntity implements IThreatTableEntity, Enemy
{
    /**
     * The percent of life taken per damage modifier
     */
    private static final float HP_PERCENT_PER_DMG = 0.03f;

    /**
     * The max amount of damage converted to scaling
     */
    private static final int MAX_SCALED_DAMAGE = 7;

    /**
     * Minimum damage done before thorns effect can happen
     */
    private static final float MIN_THORNS_DAMAGE = 30;

    /**
     * 1 in X Chance that thorns effect happens
     */
    private static final int THORNS_CHANCE            = 5;

    /**
     * Set the colony raided if raider is in the wrong colony.
     */
    private static final int COLONY_SET_RAIDED_CHANCE = 20;

    /**
     * The New PathNavigate navigator.
     */
    protected AbstractAdvancedPathNavigate newNavigator;

    /**
     * Sets the barbarians target colony on spawn Thus it never changes.
     */
    private IColony colony;

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
     * The raids event id.
     */
    private int eventID = 0;

    /**
     * Whether this entity is registered with the colony yet.
     */
    private boolean isRegistered = false;

    /**
     * The invulnerability timer for spawning, to prevent suffocate/grouping damage.
     */
    private int invulTime = 2 * 20;

    /**
     * Environmental damage cooldown timer
     */
    private int envDmgCooldown = 0;

    /**
     * Environmental damage interval
     */
    private int envDamageInterval = 5;

    /**
     * Environmental damage immunity
     */
    private boolean envDamageImmunity = false;

    /**
     * Temporary Environmental damage immunity shortly after spawning.
     */
    private boolean tempEnvDamageImmunity = true;

    /**
     * Counts entity collisions
     */
    private int collisionCounter = 0;

    /**
     * The collision threshold
     */
    private final static int    COLL_THRESHOLD = 50;
    private final static String RAID_TEAM      = "RAIDERS_TEAM";

    /**
     * Mob difficulty
     */
    private double difficulty = 1.0d;

    /**
     * The threattable of the mob
     */
    private ThreatTable threatTable = new ThreatTable<>(this);

    /**
     * Last chunk pos.
     */
    private ChunkPos lastChunkPos = null;

    /**
     * Raiders AI statemachine
     */
    private ITickRateStateMachine<IState> ai = new TickRateStateMachine<>(CombatAIStates.NO_TARGET, e -> Log.getLogger().warn(e), ENTITY_AI_TICKRATE);

    /**
     * Constructor method for Abstract Barbarians.
     *
     * @param world the world.
     * @param type  the entity type.
     */
    public AbstractEntityRaiderMob(final EntityType<? extends AbstractEntityRaiderMob> type, final Level world)
    {
        super(type, world);
        worldTimeAtSpawn = world.getGameTime();
        this.setPersistenceRequired();
        this.goalSelector = new CustomGoalSelector(this.goalSelector);
        this.targetSelector = new CustomGoalSelector(this.targetSelector);
        this.xpReward = BARBARIAN_EXP_DROP;
        IMinecoloniesAPI.getInstance().getMobAIRegistry().applyToMob(this);
        this.setInvulnerable(true);
        RaiderMobUtils.setEquipment(this);
    }

    /**
     * Ignores cramming
     */
    @Override
    public void pushEntities()
    {
        if (collisionCounter > COLL_THRESHOLD)
        {
            return;
        }

        super.pushEntities();
    }

    @Override
    public void push(@NotNull final Entity entityIn)
    {
        if (invulTime > 0)
        {
            return;
        }

        if ((collisionCounter += 3) > COLL_THRESHOLD)
        {
            if (collisionCounter > (COLL_THRESHOLD * 3))
            {
                collisionCounter = 0;
            }

            return;
        }

        super.push(entityIn);
    }

    @Override
    public void playAmbientSound()
    {
        super.playAmbientSound();
        final SoundEvent soundevent = this.getAmbientSound();
        if (soundevent != null && level().random.nextInt(OUT_OF_ONE_HUNDRED) <= ONE)
        {
            this.playSound(soundevent, this.getSoundVolume(), this.getVoicePitch());
        }
    }

    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer)
    {
        return shouldDespawn() || (level() != null && level().isAreaLoaded(this.blockPosition(), 3) && getColony() == null);
    }

    /**
     * Get the specific raider type of this raider.
     *
     * @return the type enum.
     */
    public abstract RaiderType getRaiderType();

    /**
     * Should the barbs despawn.
     *
     * @return true if so.
     */
    private boolean shouldDespawn()
    {
        return worldTimeAtSpawn != 0 && (level().getGameTime() - worldTimeAtSpawn) >= TICKS_TO_DESPAWN;
    }

    @NotNull
    @Override
    public AbstractAdvancedPathNavigate getNavigation()
    {
        if (this.newNavigator == null)
        {
            this.newNavigator = IPathNavigateRegistry.getInstance().getNavigateFor(this);
            this.navigation = newNavigator;
            this.newNavigator.setCanFloat(true);
            newNavigator.setSwimSpeedFactor(getSwimSpeedFactor());
            this.newNavigator.getPathingOptions().setEnterDoors(true);
            newNavigator.getPathingOptions().withDropCost(1D);
            newNavigator.getPathingOptions().withJumpCost(1D);
            newNavigator.getPathingOptions().setPassDanger(true);
            PathingStuckHandler stuckHandler = PathingStuckHandler.createStuckHandler()
              .withTakeDamageOnStuck(0.4f)
              .withBuildLeafBridges()
              .withChanceToByPassMovingAway(0.20)
              .withPlaceLadders();

            if (MinecoloniesAPIProxy.getInstance().getConfig().getServer().raidersbreakblocks.get())
            {
                stuckHandler.withBlockBreaks();
                stuckHandler.withCompleteStuckBlockBreak(6);
            }

            newNavigator.setStuckHandler(stuckHandler);
        }
        return newNavigator;
    }

    /**
     * Get the swim speed factor
     *
     * @return speed factor
     */
    public abstract double getSwimSpeedFactor();

    /**
     * Get the stack counter.
     *
     * @return the amount it got stuck already.
     */
    public int getStuckCounter()
    {
        return stuckCounter;
    }

    /**
     * Set the stack counter.
     *
     * @param stuckCounter the amount.
     */
    public void setStuckCounter(final int stuckCounter)
    {
        this.stuckCounter = stuckCounter;
    }

    /**
     * Get the ladder counter.
     *
     * @return the amount it got stuck and placed a ladder already.
     */
    public int getLadderCounter()
    {
        return ladderCounter;
    }

    /**
     * Set the ladder counter.
     *
     * @param ladderCounter the amount.
     */
    public void setLadderCounter(final int ladderCounter)
    {
        this.ladderCounter = ladderCounter;
    }

    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSourceIn)
    {
        return RaiderSounds.raiderSounds.get(getRaiderType()).get(RaiderSounds.RaiderSoundTypes.HURT);
    }

    @Override
    protected SoundEvent getDeathSound()
    {
        return RaiderSounds.raiderSounds.get(getRaiderType()).get(RaiderSounds.RaiderSoundTypes.DEATH);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound()
    {
        return RaiderSounds.raiderSounds.get(getRaiderType()).get(RaiderSounds.RaiderSoundTypes.SAY);
    }

    @Override
    public void addAdditionalSaveData(final CompoundTag compound)
    {
        compound.putLong(TAG_TIME, worldTimeAtSpawn);
        compound.putInt(TAG_STUCK_COUNTER, stuckCounter);
        compound.putInt(TAG_LADDER_COUNTER, ladderCounter);
        compound.putInt(TAG_COLONY_ID, this.colony == null ? 0 : colony.getID());
        compound.putInt(TAG_EVENT_ID, eventID);
        super.addAdditionalSaveData(compound);
    }


    /**
     * Prevent raiders from travelling to other dimensions through portals.
     */
    @Nullable
    @Override
    public Entity changeDimension(@NotNull final ServerLevel serverWorld, @NotNull final ITeleporter teleporter)
    {
        return null;
    }

    @Override
    public void readAdditionalSaveData(final CompoundTag compound)
    {
        worldTimeAtSpawn = compound.getLong(TAG_TIME);
        stuckCounter = compound.getInt(TAG_STUCK_COUNTER);
        ladderCounter = compound.getInt(TAG_LADDER_COUNTER);
        eventID = compound.getInt(TAG_EVENT_ID);
        if (compound.contains(TAG_COLONY_ID))
        {
            final int colonyId = compound.getInt(TAG_COLONY_ID);
            if (colonyId != 0)
            {
                setColony(IColonyManager.getInstance().getColonyByWorld(colonyId, level()));
            }
        }

        if (colony == null || eventID == 0)
        {
            this.remove(RemovalReason.DISCARDED);
        }

        super.readAdditionalSaveData(compound);
    }

    @Override
    public void aiStep()
    {
        if (!this.isAlive())
        {
            return;
        }

        updateSwingTime();

        if (invulTime > 0)
        {
            invulTime--;
        }
        else
        {
            this.setInvulnerable(false);
        }

        if (collisionCounter > 0)
        {
            collisionCounter--;
        }

        if (level().isClientSide)
        {
            super.aiStep();
            return;
        }

        if (currentTick % (random.nextInt(EVERY_X_TICKS) + 1) == 0)
        {
            envDmgCooldown--;
            if (worldTimeAtSpawn == 0)
            {
                worldTimeAtSpawn = level().getGameTime();
            }

            if (this.chunkPosition() != lastChunkPos)
            {
                this.lastChunkPos = this.chunkPosition();
                if (random.nextInt(COLONY_SET_RAIDED_CHANCE) <= 0)
                {
                    this.onEnterChunk(this.lastChunkPos);
                }
            }

            if (shouldDespawn())
            {
                this.die(level().damageSources().source(DamageSourceKeys.DESPAWN));
                this.remove(RemovalReason.DISCARDED);
                return;
            }

            if (!isRegistered)
            {
                registerWithColony();
            }

            if (currentCount <= 0)
            {
                currentCount = COUNTDOWN_SECOND_MULTIPLIER * TIME_TO_COUNTDOWN;

                if (!this.getMainHandItem().isEmpty() && SPEED_EFFECT != null && this.getMainHandItem().getItem() instanceof IChiefSwordItem
                      && MinecoloniesAPIProxy.getInstance().getConfig().getServer().raidDifficulty.get() >= BARBARIAN_HORDE_DIFFICULTY_FIVE)
                {
                    RaiderMobUtils.getBarbariansCloseToEntity(this, SPEED_EFFECT_DISTANCE)
                      .stream().filter(entity -> !entity.hasEffect(MobEffects.MOVEMENT_SPEED))
                      .forEach(entity -> entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, SPEED_EFFECT_DURATION, SPEED_EFFECT_MULTIPLIER)));
                }
            }
            else
            {
                --currentCount;
            }
        }
        currentTick++;

        if (isRegistered && tickCount % ENTITY_AI_TICKRATE == 0)
        {
            ai.tick();
        }

        super.aiStep();
    }

    /**
     * Even on when a raider entered a new chunk.
     * @param newChunkPos the new chunk pos.
     */
    private void onEnterChunk(final ChunkPos newChunkPos)
    {
        final LevelChunk chunk = colony.getWorld().getChunk(newChunkPos.x, newChunkPos.z);
        final int owningColonyId = ColonyUtils.getOwningColony(chunk);
        if (owningColonyId != NO_COLONY_ID && colony.getID() != owningColonyId)
        {
            final IColony tempColony = IColonyManager.getInstance().getColonyByWorld(owningColonyId, level);
            tempColony.getRaiderManager().setPassThroughRaid();
        }
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
      final ServerLevelAccessor worldIn,
      final DifficultyInstance difficultyIn,
      final MobSpawnType reason,
      @Nullable final SpawnGroupData spawnDataIn,
      @Nullable final CompoundTag dataTag)
    {
        RaiderMobUtils.setEquipment(this);
        return super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
    }

    @Override
    public void remove(@NotNull final RemovalReason reason)
    {
        if (!level.isClientSide && colony != null && eventID > 0)
        {
            colony.getEventManager().unregisterEntity(this, eventID);
        }
        super.remove(reason);
    }

    /**
     * Getter for the colony.
     *
     * @return the colony the barbarian is assigned to attack.e
     */
    public IColony getColony()
    {
        return colony;
    }

    /**
     * Registers the entity with the colony.
     */
    public void registerWithColony()
    {
        if (colony == null || eventID == 0 || dead)
        {
            remove(RemovalReason.DISCARDED);
            return;
        }
        RaiderMobUtils.setMobAttributes(this, getColony());
        colony.getEventManager().registerEntity(this, eventID);
        isRegistered = true;
    }

    @Override
    public void die(@NotNull final DamageSource cause)
    {
        super.die(cause);
        if (!level().isClientSide && getColony() != null)
        {
            getColony().getEventManager().onEntityDeath(this, eventID);
        }
    }

    @Override
    public boolean hurt(@NotNull final DamageSource damageSource, final float damage)
    {
        if (damageSource.getEntity() instanceof AbstractEntityRaiderMob)
        {
            return false;
        }

        if (damageSource.getEntity() instanceof LivingEntity)
        {
            threatTable.addThreat((LivingEntity) damageSource.getEntity(), (int) damage);
        }

        if (damageSource.typeHolder().is(DamageTypes.FELL_OUT_OF_WORLD))
        {
            return super.hurt(damageSource, damage);
        }

        if (damageSource.getDirectEntity() == null)
        {
            if (envDamageImmunity || tempEnvDamageImmunity)
            {
                return false;
            }

            if (--envDmgCooldown <= 0)
            {
                envDmgCooldown = envDamageInterval;
            }
            else
            {
                return false;
            }
        }
        else if (!level().isClientSide())
        {
            final IColonyEvent event = colony.getEventManager().getEventByID(eventID);
            if (event instanceof IColonyCampFireRaidEvent)
            {
                ((IColonyCampFireRaidEvent) event).setCampFireTime(0);
            }

            final Entity source = damageSource.getEntity();
            if (source instanceof Player)
            {
                if (damage > MIN_THORNS_DAMAGE && random.nextInt(THORNS_CHANCE) == 0)
                {
                    source.hurt(level().damageSources().thorns(this), damage * 0.5f);
                }

                final float raiderDamageEnchantLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchants.raiderDamage.get(), ((Player) source).getMainHandItem());

                // Up to 7 damage are converted to health scaling damage, 7 is the damage of a diamond sword
                float baseScalingDamage = Math.min(damage, MAX_SCALED_DAMAGE);
                float totalWithScaled =
                  Math.max(damage, (damage - baseScalingDamage) + baseScalingDamage * HP_PERCENT_PER_DMG * this.getMaxHealth() * (1 + (raiderDamageEnchantLevel / 5)));
                return super.hurt(damageSource, totalWithScaled);
            }
        }

        return super.hurt(damageSource, damage);
    }

    /**
     * Get the default attributes with their values.
     * @return the attribute modifier map.
     */
    public static AttributeSupplier.Builder getDefaultAttributes()
    {
        return LivingEntity.createLivingAttributes()
                 .add(MOB_ATTACK_DAMAGE.get())
                 .add(Attributes.MAX_HEALTH)
                 .add(Attributes.ARMOR)
                 .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
                 .add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE * 2)
                 .add(Attributes.ATTACK_DAMAGE, Attributes.ATTACK_DAMAGE.getDefaultValue());
    }

    /**
     * Set the colony to raid.
     *
     * @param colony the colony to set.
     */
    public void setColony(final IColony colony)
    {
        if (colony != null)
        {
            this.colony = colony;
        }
    }

    public int getEventID()
    {
        return eventID;
    }

    public void setEventID(final int eventID)
    {
        this.eventID = eventID;
    }

    /**
     * Sets the environmental damage interval
     *
     * @param interval damage interval
     */
    public void setEnvDamageInterval(final int interval)
    {
        envDamageInterval = interval;
    }

    /**
     * Sets the immunity to environmental damage
     *
     * @param immunity whether immune
     */
    public void setEnvDamageImmunity(final boolean immunity)
    {
        envDamageImmunity = immunity;
    }


    /**
     * Sets the temporary immunity to environmental damage
     *
     * @param immunity whether immune
     */
    public void setTempEnvDamageImmunity(final boolean immunity)
    {
        tempEnvDamageImmunity = immunity;
    }

    /**
     * Initializes entity stats for a given raidlevel and difficulty
     *
     * @param baseHealth basehealth for this raid/difficulty
     * @param difficulty difficulty
     * @param baseDamage basedamage for this raid/difficulty
     */
    public void initStatsFor(final double baseHealth, final double difficulty, final double baseDamage)
    {
        this.getAttribute(MOB_ATTACK_DAMAGE.get()).setBaseValue(baseDamage);

        this.difficulty = difficulty;
        final double armor = difficulty * ARMOR;
        this.getAttribute(Attributes.ARMOR).setBaseValue(armor);
        this.setEnvDamageInterval((int) (BASE_ENV_DAMAGE_RESIST * difficulty));

        if (difficulty >= 1.4d)
        {
            this.setEnvDamageImmunity(true);
        }

        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(baseHealth);
        this.setHealth(this.getMaxHealth());
    }

    /**
     * Get the mobs difficulty
     *
     * @return difficulty
     */
    public double getDifficulty()
    {
        return difficulty;
    }

    /**
     * Disallow pushing from fluids to prevent stuck
     *
     * @return
     */
    public boolean isPushedByFluid()
    {
        return false;
    }

    @Override
    public ThreatTable getThreatTable()
    {
        return threatTable;
    }

    /**
     * Get the AI machine
     *
     * @return ai statemachine
     */
    public ITickRateStateMachine<IState> getAI()
    {
        return ai;
    }

    @Override
    public int getTeamId()
    {
        // All raiders are in the same team. You're doomed!
        return -1;
    }
}
