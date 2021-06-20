package com.minecolonies.api.entity.mobs;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.colonyEvents.IColonyCampFireRaidEvent;
import com.minecolonies.api.colony.colonyEvents.IColonyEvent;
import com.minecolonies.api.enchants.ModEnchants;
import com.minecolonies.api.entity.CustomGoalSelector;
import com.minecolonies.api.entity.pathfinding.AbstractAdvancedPathNavigate;
import com.minecolonies.api.entity.pathfinding.IStuckHandlerEntity;
import com.minecolonies.api.entity.pathfinding.PathingStuckHandler;
import com.minecolonies.api.entity.pathfinding.registry.IPathNavigateRegistry;
import com.minecolonies.api.items.IChiefSwordItem;
import com.minecolonies.api.sounds.RaiderSounds;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.ITeleporter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Random;

import static com.minecolonies.api.entity.mobs.RaiderMobUtils.MOB_ATTACK_DAMAGE;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.RaiderConstants.*;

/**
 * Abstract for all Barbarian entities.
 */
public abstract class AbstractEntityMinecoloniesMob extends MobEntity implements IStuckHandlerEntity
{
    /**
     * Difficulty at which raiders team up
     */
    private static final double TEAM_DIFFICULTY = 2.0d;

    /**
     * The percent of life taken per damage modifier
     */
    private static final float HP_PERCENT_PER_DMG = 0.03f;

    /**
     * The max amount of damage converted to scaling
     */
    private static final int MAX_SCALED_DAMAGE = 7;

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
     * Counts entity collisions
     */
    private int collisionCounter = 0;

    /**
     * Whether the entity is possibly stuck
     */
    private boolean canBeStuck = true;

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
     * Constructor method for Abstract Barbarians.
     *
     * @param world the world.
     * @param type  the entity type.
     */
    public AbstractEntityMinecoloniesMob(final EntityType<? extends AbstractEntityMinecoloniesMob> type, final World world)
    {
        super(type, world);
        worldTimeAtSpawn = world.getGameTime();
        this.enablePersistence();
        this.goalSelector = new CustomGoalSelector(this.goalSelector);
        this.targetSelector = new CustomGoalSelector(this.targetSelector);
        this.experienceValue = BARBARIAN_EXP_DROP;
        RaiderMobUtils.setupMobAi(this);
        this.setInvulnerable(true);
        RaiderMobUtils.setEquipment(this);
    }

    @Override
    public void applyEntityCollision(@NotNull final Entity entityIn)
    {
        if (invulTime > 0 || (collisionCounter += 3) > COLL_THRESHOLD)
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

    @Override
    public boolean canDespawn(final double distanceToClosestPlayer)
    {
        return shouldDespawn() || (world != null && world.isAreaLoaded(this.getPosition(), 3) && getColony() == null);
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
            this.newNavigator.getNodeProcessor().setCanEnterDoors(true);
            newNavigator.getPathingOptions().withJumpDropCost(1.1D);
            PathingStuckHandler stuckHandler = PathingStuckHandler.createStuckHandler()
                                                 .withTakeDamageOnStuck(0.4f)
                                                 .withBuildLeafBridges()
                                                 .withPlaceLadders();

            if (MinecoloniesAPIProxy.getInstance().getConfig().getServer().doBarbariansBreakThroughWalls.get())
            {
                stuckHandler.withBlockBreaks();
                stuckHandler.withCompleteStuckBlockBreak(6);
            }

            newNavigator.setStuckHandler(stuckHandler);
        }
        return newNavigator;
    }

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
    public void writeAdditional(final CompoundNBT compound)
    {
        compound.putLong(TAG_TIME, worldTimeAtSpawn);
        compound.putInt(TAG_STUCK_COUNTER, stuckCounter);
        compound.putInt(TAG_LADDER_COUNTER, ladderCounter);
        compound.putInt(TAG_COLONY_ID, this.colony == null ? 0 : colony.getID());
        compound.putInt(TAG_EVENT_ID, eventID);
        super.writeAdditional(compound);
    }


    /**
     * Prevent raiders from travelling to other dimensions through portals.
     */
    @Nullable
    @Override
    public Entity changeDimension(@NotNull final ServerWorld serverWorld, @NotNull final ITeleporter teleporter)
    {
        return null;
    }

    @Override
    public void readAdditional(final CompoundNBT compound)
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
                setColony(IColonyManager.getInstance().getColonyByWorld(colonyId, world));
            }
        }

        if (colony == null || eventID == 0)
        {
            this.remove();
        }

        super.readAdditional(compound);
    }

    @Override
    public void livingTick()
    {
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
                this.onDeath(new DamageSource("despawn"));
                this.remove();
                return;
            }

            if (!isRegistered)
            {
                registerWithColony();
            }

            if (currentCount <= 0)
            {
                currentCount = COUNTDOWN_SECOND_MULTIPLIER * TIME_TO_COUNTDOWN;

                if (!this.getHeldItemMainhand().isEmpty() && SPEED_EFFECT != null && this.getHeldItemMainhand().getItem() instanceof IChiefSwordItem
                      && MinecoloniesAPIProxy.getInstance().getConfig().getServer().barbarianHordeDifficulty.get() >= BARBARIAN_HORDE_DIFFICULTY_FIVE)
                {
                    RaiderMobUtils.getBarbariansCloseToEntity(this, SPEED_EFFECT_DISTANCE)
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

    @org.jetbrains.annotations.Nullable
    @Override
    public ILivingEntityData onInitialSpawn(
      final IServerWorld worldIn,
      final DifficultyInstance difficultyIn,
      final SpawnReason reason,
      @org.jetbrains.annotations.Nullable final ILivingEntityData spawnDataIn,
      @org.jetbrains.annotations.Nullable final CompoundNBT dataTag)
    {
        RaiderMobUtils.setEquipment(this);
        return super.onInitialSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
    }

    @Override
    public void remove()
    {
        if (!world.isRemote && colony != null && eventID > 0)
        {
            colony.getEventManager().unregisterEntity(this, eventID);
        }
        super.remove();
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
            remove();
            return;
        }
        RaiderMobUtils.setMobAttributes(this, getColony());
        colony.getEventManager().registerEntity(this, eventID);
        isRegistered = true;
    }

    @Override
    public void onDeath(@NotNull final DamageSource cause)
    {
        super.onDeath(cause);
        if (!world.isRemote && getColony() != null)
        {
            getColony().getEventManager().onEntityDeath(this, eventID);
        }
    }

    @Override
    public boolean attackEntityFrom(@NotNull final DamageSource damageSource, final float damage)
    {
        if (damageSource.getImmediateSource() == null)
        {
            if (envDamageImmunity)
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
        else if (!world.isRemote())
        {
            final IColonyEvent event = colony.getEventManager().getEventByID(eventID);
            if (event instanceof IColonyCampFireRaidEvent)
            {
                ((IColonyCampFireRaidEvent) event).setCampFireTime(0);
            }

            final Entity source = damageSource.getTrueSource();
            if (source instanceof PlayerEntity)
            {
                final float raiderDamageEnchantLevel = EnchantmentHelper.getEnchantmentLevel(ModEnchants.raiderDamage, ((PlayerEntity) source).getHeldItemMainhand());

                // Up to 7 damage are converted to health scaling damage, 7 is the damage of a diamond sword
                float baseScalingDamage = Math.min(damage, MAX_SCALED_DAMAGE);
                float totalWithScaled =
                  Math.max(damage, (damage - baseScalingDamage) + baseScalingDamage * HP_PERCENT_PER_DMG * this.getMaxHealth() * (1 + (raiderDamageEnchantLevel / 5)));
                return super.attackEntityFrom(damageSource, totalWithScaled);
            }
        }

        return super.attackEntityFrom(damageSource, damage);
    }

    /**
     * Get the default attributes with their values.
     * @return the attribute modifier map.
     */
    public static AttributeModifierMap.MutableAttribute getDefaultAttributes()
    {
        return LivingEntity.registerAttributes()
                 .createMutableAttribute(MOB_ATTACK_DAMAGE)
                 .createMutableAttribute(Attributes.MAX_HEALTH)
                 .createMutableAttribute(Attributes.ARMOR)
                 .createMutableAttribute(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
                 .createMutableAttribute(Attributes.FOLLOW_RANGE, FOLLOW_RANGE * 2)
                 .createMutableAttribute(Attributes.ATTACK_DAMAGE, Attributes.ATTACK_DAMAGE.getDefaultValue());
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

    /**
     * Get the position (blckpos)
     * @return the pos.
     */
    public BlockPos getPosition()
    {
        return new BlockPos(getPosX(), getPosY(), getPosZ());
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
     * Initializes entity stats for a given raidlevel and difficulty
     *
     * @param baseHealth basehealth for this raid/difficulty
     * @param difficulty difficulty
     * @param baseDamage basedamage for this raid/difficulty
     */
    public void initStatsFor(final double baseHealth, final double difficulty, final double baseDamage)
    {
        this.getAttribute(MOB_ATTACK_DAMAGE).setBaseValue(baseDamage);

        this.difficulty = difficulty;
        final double armor = difficulty * ARMOR;
        this.getAttribute(Attributes.ARMOR).setBaseValue(armor);
        this.setEnvDamageInterval((int) (BASE_ENV_DAMAGE_RESIST * difficulty));

        if (difficulty >= 1.4d)
        {
            this.setEnvDamageImmunity(true);
        }

        if (difficulty >= TEAM_DIFFICULTY)
        {
            world.getScoreboard().addPlayerToTeam(getScoreboardName(), checkOrCreateTeam());
        }

        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(baseHealth);
        this.setHealth(this.getMaxHealth());
    }

    /**
     * Creates or gets the scoreboard team
     *
     * @return Scoreboard team
     */
    private ScorePlayerTeam checkOrCreateTeam()
    {
        if (this.world.getScoreboard().getTeam(getTeamName()) == null)
        {
            this.world.getScoreboard().createTeam(getTeamName());
            this.world.getScoreboard().getTeam(getTeamName()).setAllowFriendlyFire(false);
        }
        return this.world.getScoreboard().getTeam(getTeamName());
    }

    /**
     * Gets the scoreboard team name
     *
     * @return
     */
    protected String getTeamName()
    {
        return RAID_TEAM;
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

    @Override
    public boolean canBeStuck()
    {
        return canBeStuck;
    }

    /**
     * Sets whether the entity currently could be stuck
     *
     * @param canBeStuck true if its possible to be stuck
     */
    public void setCanBeStuck(final boolean canBeStuck)
    {
        this.canBeStuck = canBeStuck;
    }
}
