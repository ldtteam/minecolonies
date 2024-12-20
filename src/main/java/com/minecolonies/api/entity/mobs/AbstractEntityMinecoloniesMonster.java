package com.minecolonies.api.entity.mobs;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.entity.CustomGoalSelector;
import com.minecolonies.api.entity.ai.combat.CombatAIStates;
import com.minecolonies.api.entity.ai.combat.threat.IThreatTableEntity;
import com.minecolonies.api.entity.ai.combat.threat.ThreatTable;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateStateMachine;
import com.minecolonies.api.entity.other.AbstractFastMinecoloniesEntity;
import com.minecolonies.api.entity.pathfinding.registry.IPathNavigateRegistry;
import com.minecolonies.api.sounds.RaiderSounds;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.MathUtils;
import com.minecolonies.core.entity.pathfinding.navigation.AbstractAdvancedPathNavigate;
import com.minecolonies.core.entity.pathfinding.navigation.PathingStuckHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.ITeleporter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import static com.minecolonies.api.entity.citizen.AbstractEntityCitizen.ENTITY_AI_TICKRATE;
import static com.minecolonies.api.entity.mobs.RaiderMobUtils.MOB_ATTACK_DAMAGE;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.RaiderConstants.*;

/**
 * Abstract for all villain entities.
 */
public abstract class AbstractEntityMinecoloniesMonster extends AbstractFastMinecoloniesEntity implements IThreatTableEntity, Enemy
{
    /**
     * The New PathNavigate navigator.
     */
    protected AbstractAdvancedPathNavigate newNavigator;

    /**
     * The invulnerability timer for spawning, to prevent suffocate/grouping damage.
     */
    private int invulTime = 2 * 20;

    /**
     * Texture id of the pirates.
     */
    private int textureId;

    /**
     * Counts entity collisions
     */
    private int collisionCounter = 0;

    /**
     * The collision threshold
     */
    private final static int    COLL_THRESHOLD = 50;

    /**
     * The threattable of the mob
     */
    private ThreatTable threatTable = new ThreatTable<>(this);

    /**
     * Raiders AI statemachine
     */
    private ITickRateStateMachine<IState> ai = new TickRateStateMachine<>(CombatAIStates.NO_TARGET, e -> Log.getLogger().warn(e), ENTITY_AI_TICKRATE);

    /**
     * Initial spawn pos of the entity.
     */
    private BlockPos spawnPos = null;

    /**
     * Constructor method for Abstract minecolonies mobs.
     *
     * @param world the world.
     * @param type  the entity type.
     */
    public AbstractEntityMinecoloniesMonster(final EntityType<? extends AbstractEntityMinecoloniesMonster> type, final Level world)
    {
        super(type, world);
        this.setPersistenceRequired();
        this.goalSelector = new CustomGoalSelector(this.goalSelector);
        this.targetSelector = new CustomGoalSelector(this.targetSelector);
        this.xpReward = BARBARIAN_EXP_DROP;
        IMinecoloniesAPI.getInstance().getMobAIRegistry().applyToMob(this);
        RaiderMobUtils.setEquipment(this);
    }

    /**
     * Constructor method for Abstract minecolonies mobs.
     *
     * @param world the world.
     * @param type  the entity type.
     * @param textureCount the texture count.
     */
    public AbstractEntityMinecoloniesMonster(final EntityType<? extends AbstractEntityMinecoloniesMonster> type, final Level world, final int textureCount)
    {
        this(type, world);
        this.textureId = MathUtils.RANDOM.nextInt(textureCount);
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


    /**
     * Get the specific raider type of this raider.
     *
     * @return the type enum.
     */
    public abstract RaiderType getRaiderType();

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
            newNavigator.getPathingOptions().setEnterDoors(true);
            newNavigator.getPathingOptions().setCanOpenDoors(true);
            newNavigator.getPathingOptions().withDropCost(1D);
            newNavigator.getPathingOptions().withJumpCost(1D);
            newNavigator.getPathingOptions().setPassDanger(true);
            PathingStuckHandler stuckHandler = PathingStuckHandler.createStuckHandler();

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

    /**
     * Prevent raiders from travelling to other dimensions through portals.
     */
    @Nullable
    @Override
    public Entity changeDimension(@NotNull final ServerLevel serverWorld, @NotNull final ITeleporter teleporter)
    {
        return null;
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

        final double armor = difficulty * ARMOR;
        this.getAttribute(Attributes.ARMOR).setBaseValue(armor);

        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(baseHealth);
        this.setHealth(this.getMaxHealth());
    }

    @Override
    public void aiStep()
    {
        if (!this.isAlive())
        {
            return;
        }

        if (this.spawnPos == null && this.blockPosition() != BlockPos.ZERO)
        {
            this.spawnPos = this.blockPosition();
        }

        updateSwingTime();
        if (collisionCounter > 0)
        {
            collisionCounter--;
        }

        if (level().isClientSide)
        {
            super.aiStep();
            return;
        }

        if (tickCount % ENTITY_AI_TICKRATE == 0)
        {
            ai.tick();
        }

        super.aiStep();
    }

    @Override
    public boolean hurt(@NotNull final DamageSource damageSource, final float damage)
    {
        if (damageSource.getEntity() instanceof AbstractEntityMinecoloniesMonster)
        {
            return false;
        }

        if (damageSource.getEntity() instanceof LivingEntity attacker)
        {
            if (threatTable.getThreatFor(attacker) == -1)
            {
                for (final AbstractEntityMinecoloniesMonster monster : level.getEntitiesOfClass(AbstractEntityMinecoloniesMonster.class, AABB.ofSize(position(), 20,5,20)))
                {
                    monster.threatTable.addThreat(attacker, 0);
                }
            }
            threatTable.addThreat(attacker, (int) damage);
        }

        if (damageSource.typeHolder().is(DamageTypes.FELL_OUT_OF_WORLD))
        {
            return super.hurt(damageSource, damage);
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

    @Override
    public void addAdditionalSaveData(final CompoundTag compound)
    {
        if (spawnPos != null)
        {
            compound.putLong(TAG_SPAWN_POS, spawnPos.asLong());
        }
        super.addAdditionalSaveData(compound);
    }

    @Override
    public void readAdditionalSaveData(final CompoundTag compound)
    {
        if (compound.contains(TAG_SPAWN_POS))
        {
            this.spawnPos = BlockPos.of(compound.getLong(TAG_SPAWN_POS));
        }
        super.readAdditionalSaveData(compound);
    }

    /**
     * Disallow pushing from fluids to prevent stuck
     *
     * @return
     */
    @Override
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

    /**
     * Texture id of the mob.
     * @return the texture id.
     */
    public int getTextureId()
    {
        return textureId;
    }

    /**
     * Getter for the initial spawn pos of the entity.
     * @return the pos.
     */
    public BlockPos getSpawnPos()
    {
        return this.spawnPos;
    }

    /**
     * Get the mobs difficulty
     *
     * @return difficulty
     */
    public double getDifficulty()
    {
        return 1;
    }
}
