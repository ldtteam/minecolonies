package com.minecolonies.api.entity.mobs;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.entity.CustomGoalSelector;
import com.minecolonies.api.entity.pathfinding.AbstractAdvancedPathNavigate;
import com.minecolonies.api.entity.pathfinding.registry.IPathNavigateRegistry;
import com.minecolonies.api.items.IChiefSwordItem;
import com.minecolonies.api.sounds.BarbarianSounds;
import net.minecraft.entity.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Random;

import static com.minecolonies.api.colony.colonyEvents.NBTTags.TAG_EVENT_ID;
import static com.minecolonies.api.entity.mobs.RaiderMobUtils.MOB_ATTACK_DAMAGE;
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
        this.experienceValue = BARBARIAN_EXP_DROP;
        RaiderMobUtils.setupMobAi(this);
        this.setInvulnerable(true);
        RaiderMobUtils.setEquipment(this);
        getAttributes().registerAttribute(MOB_ATTACK_DAMAGE);
    }

    @Override
    public void applyEntityCollision(@NotNull final Entity entityIn)
    {
        if (invulTime < 0 && entityIn instanceof AbstractEntityMinecoloniesMob
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
            this.newNavigator.getNodeProcessor().setCanEnterDoors(true);
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
        compound.putInt(TAG_EVENT_ID, eventID);
        super.writeAdditional(compound);
    }

    @Override
    public Entity changeDimension(DimensionType dimensionIn, net.minecraftforge.common.util.ITeleporter teleporter)
    {
        return this;
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
                      && MinecoloniesAPIProxy.getInstance().getConfig().getCommon().barbarianHordeDifficulty.get() >= BARBARIAN_HORDE_DIFFICULTY_FIVE)
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

    @Override
    public ILivingEntityData onInitialSpawn(
      final IWorld worldIn, final DifficultyInstance difficultyIn, final SpawnReason reason, @Nullable final ILivingEntityData spawnDataIn, @Nullable final CompoundNBT dataTag)
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
}
