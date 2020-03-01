package com.minecolonies.api.entity.mobs;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.entity.pathfinding.AbstractAdvancedPathNavigate;
import com.minecolonies.api.entity.pathfinding.registry.IPathNavigateRegistry;
import com.minecolonies.api.items.IChiefSwordItem;
import com.minecolonies.api.sounds.BarbarianSounds;
import com.minecolonies.api.util.CompatibilityUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
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
public abstract class AbstractEntityMinecoloniesMob extends EntityMob
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
    public AbstractEntityMinecoloniesMob(final World world)
    {
        super(world);
        this.setEntityInvulnerable(true);
        RaiderMobUtils.setEquipment(this);
        getAttributeMap().registerAttribute(MOB_ATTACK_DAMAGE);
    }

    @Override
    protected void initEntityAI()
    {
        RaiderMobUtils.setupMobAi(this);
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
        if (invulTime < 0 && entityIn instanceof AbstractEntityMinecoloniesMob
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

    @Override
    protected boolean canDespawn()
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

    @NotNull
    @Override
    public AbstractAdvancedPathNavigate getNavigator()
    {
        if (this.newNavigator == null)
        {
            this.newNavigator = IPathNavigateRegistry.getInstance().getNavigateFor(this);
            this.navigator = newNavigator;
            this.newNavigator.setCanSwim(true);
            this.newNavigator.setEnterDoors(true);
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
    public NBTTagCompound writeToNBT(final NBTTagCompound compound)
    {
        compound.setLong(TAG_TIME, worldTimeAtSpawn);
        compound.setInteger(TAG_STUCK_COUNTER, stuckCounter);
        compound.setInteger(TAG_LADDER_COUNTER, ladderCounter);
        compound.setInteger(TAG_COLONY_ID, this.colony == null ? 0 : colony.getID());
        compound.setInteger(TAG_EVENT_ID, eventID);
        return super.writeToNBT(compound);
    }

    @Override
    public Entity changeDimension(int dimensionIn, net.minecraftforge.common.util.ITeleporter teleporter)
    {
        return this;
    }

    @Override
    public void readFromNBT(final NBTTagCompound compound)
    {
        worldTimeAtSpawn = compound.getLong(TAG_TIME);
        stuckCounter = compound.getInteger(TAG_STUCK_COUNTER);
        ladderCounter = compound.getInteger(TAG_LADDER_COUNTER);
        eventID = compound.getInteger(TAG_EVENT_ID);
        if (compound.hasKey(TAG_COLONY_ID))
        {
            final int colonyId = compound.getInteger(TAG_COLONY_ID);
            if (colonyId != 0)
            {
                setColony(IColonyManager.getInstance().getColonyByWorld(colonyId, world));
            }
        }

        if (colony == null || eventID == 0)
        {
            this.setDead();
        }

        super.readFromNBT(compound);
    }

    @Override
    public void onLivingUpdate()
    {
        if (invulTime > 0)
        {
            invulTime--;
        }
        else
        {
            this.setEntityInvulnerable(false);
        }

        if (world.isRemote)
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
                this.onDeath(new DamageSource("despawn"));
                this.setDead();
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
                      && Configurations.gameplay.barbarianHordeDifficulty >= BARBARIAN_HORDE_DIFFICULTY_FIVE)
                {
                    RaiderMobUtils.getBarbariansCloseToEntity(this, SPEED_EFFECT_DISTANCE)
                      .stream().filter(entity -> !entity.isPotionActive(SPEED_EFFECT))
                      .forEach(entity -> entity.addPotionEffect(new PotionEffect(SPEED_EFFECT, SPEED_EFFECT_DURATION, 0)));
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

    @Override
    public void setDead()
    {
        super.setDead();

        if (!world.isRemote && colony != null && eventID > 0)
        {
            colony.getEventManager().unregisterEntity(this, eventID);
        }
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
        if (colony == null || eventID == 0 || isDead)
        {
            setDead();
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
