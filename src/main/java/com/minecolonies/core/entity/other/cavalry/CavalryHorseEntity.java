package com.minecolonies.core.entity.other.cavalry;

import java.util.UUID;
import javax.annotation.Nonnull;
import com.minecolonies.api.colony.IAnimalData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.managers.interfaces.IAnimalDataView;
import com.minecolonies.api.colony.managers.interfaces.IManagedAnimal;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.DamageSourceKeys;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.CitizenConstants;
import com.minecolonies.api.util.constant.NbtTagConstants;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.minecolonies.core.entity.mobs.AnimalColonyHandler;
import com.minecolonies.core.entity.mobs.IAnimalColonyHandler;
import com.minecolonies.core.entity.pathfinding.navigation.AbstractAdvancedPathNavigate;
import com.minecolonies.core.entity.pathfinding.navigation.MinecoloniesAdvancedPathNavigate;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.animal.horse.Variant;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStandGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;

/**
 * Cavalry Horse Entity class for Minecolonies.
 * Extends the vanilla Horse entity with custom behavior for cavalry units.
 */
public class CavalryHorseEntity extends Horse implements IManagedAnimal<CavalryHorseEntity>
{
    public static final EntityDataAccessor<Integer>  DATA_COLONY_ID         = SynchedEntityData.defineId(CavalryHorseEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer>  DATA_MANAGED_ANIMAL_ID = SynchedEntityData.defineId(CavalryHorseEntity.class, EntityDataSerializers.INT);

    /**
     * The key used to store whether the horse is reserved for cavalry.
     */
    private static final String RESERVE_KEY = "horse_reserved_for_cavalry";


    /** 
     * The base width and height of the horse entity. 
     * Note that the width is deliberately slim to allow 1-wide pathing for cavalry units. 
     * Base height matches vanilla horse height.
     */
    public static final float SLIM_W = 0.70F;
    public static final float BASE_H = 1.6F;

    /**
     * The offset used to adjust the position of the rider on the horse.
     */
    private static final float SEATING_OFFSET = 0.40F;

    /**
     * The cooldown for logging when debugging.
     */
    public static final int LOG_COOLDOWN_INTERVAL = 200;
    private int logCooldown = 0;

    public static final float COMBAT_READINESS_THRESHOLD = .66f;

    /**
     * The animal colony handler.
     */
    private IAnimalColonyHandler animalColonyHandler = null;

    /**
     * The animal data associated with this cavalry horse.
     */
    IAnimalData animalData;

    /**
     * Animal data view.
     */
    private IAnimalDataView animalDataView;

    /**
     * The limit after which a reservation expires (in ticks).
     */
    private static final int RESERVATION_EXPIRATION_LIMIT = 200;
    private int reservationExpiration = 0;

    /**
     * The timepoint at which the entity last collided
     */
    private long lastHorizontalCollision = 0;

    /**
     * Constructor for CavalryHorseEntity.
     *
     * @param type  The entity type
     * @param level The level the entity is in
     */
    public CavalryHorseEntity(EntityType<? extends Horse> type, Level level)
    {
        super(type, level);
        this.setMaxUpStep(1.1F);
        this.animalColonyHandler = new AnimalColonyHandler(this);
    }

    /**
     * Registers the goals for this entity.
     * <p>
     * This sets the float goal, follow parent goal, breed goal, validate stable goal, return to stable goal, water avoiding random stroll goal, look at player goal, and random look around goal.
     * If the entity can perform rearing, it also sets the random stand goal.
     */
    @Override
    public void registerGoals()
    {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new FollowParentGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new BreedGoal(this, 1.0D));
        // this.goalSelector.addGoal(5, new ValidateStableGoal(this));
        // this.goalSelector.addGoal(6, new ReturnToStableGoal(this, 1.15, 20.0));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.7D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        if (this.canPerformRearing())
        {
            this.goalSelector.addGoal(10, new RandomStandGoal(this));
        }
    }

    /**
     * Called when the entity's data is updated from the server. If the entity has a citizen colony handler, it calls the handler's onSyncDataUpdate method.
     * If the entity is on the client side and the data accessor is DATA_STYLE, it checks if the entity's style is in the list of valid styles and if not, it sets the style to the first valid style in the list.
     * @param dataAccessor The data accessor which contains the updated data.
     */
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> dataAccessor)
    {
        super.onSyncedDataUpdated(dataAccessor);
        if (animalColonyHandler != null)
        {
            animalColonyHandler.onSyncedDataUpdated(dataAccessor);
        }
    }

    /**
     * This method is called by the entity every tick to update its state.
     * It handles registration of the entity to the closest colony if it was summoned into the world via an ops command.
     * It also handles updating the client side of the managed animal data and the animal colony handler.
     */
    @Override
    public void aiStep()
    {
        super.aiStep();

        int colonyId = getColonyId();

        // if the entity is summoned into the world with an ops command rather than created by the stablemaster, this "autoregisters" the entity as a managed animal to the closest colony.
        if (colonyId == 0 && !CompatibilityUtils.getWorldFromEntity(this).isClientSide)
        {
            IColony colony = IColonyManager.getInstance().getClosestColony(level, this.getOnPos());

            if (colony == null)
            {
                return;
            }

            if (getAnimalData() == null)
            {
                animalData = colony.getAnimalManager().createAndRegisterAnimalData(this);
            } 

            colonyId =  colony.getID();
            setColonyId(colonyId);
        }

        if (CompatibilityUtils.getWorldFromEntity(this).isClientSide)
        {
            animalColonyHandler.updateColonyClient();

            if (animalColonyHandler.getColonyId() != 0 && getManagedAnimalId() != 0 && getOffsetTicks() % CitizenConstants.TICKS_20 == 0)
            {
                final IColonyView colonyView = IColonyManager.getInstance().getColonyView(animalColonyHandler.getColonyId(), level.dimension());
                if (colonyView != null)
                {
                    this.animalDataView = colonyView.getAnimal(getManagedAnimalId());
                }
            }
        }
        else
        {
            animalColonyHandler.registerWithColony(colonyId, getManagedAnimalId());
        }
    }
    

    /**
     * Defines the synced data for this entity.
     */
    @Override
    protected void defineSynchedData() 
    {
        super.defineSynchedData();
        this.entityData.define(DATA_COLONY_ID, 0);
        this.entityData.define(DATA_MANAGED_ANIMAL_ID, 0);
    }

    /**
     * Gets the accessor for the colony ID of this entity.
     *
     * @return the accessor for the colony ID
     */
    @Override
    public EntityDataAccessor<Integer> getColonyIdAccessor()
    {
        return DATA_COLONY_ID;
    }

    @Override
    public int getColonyId()
    {
        return entityData.get(DATA_COLONY_ID);
    }

    @Override
    public void setColonyId(final int colonyId)
    {
        entityData.set(DATA_COLONY_ID, colonyId);
        animalColonyHandler.setColonyId(colonyId);
    }

    /**
     * Gets the accessor for the animal ID of this entity.
     * 
     * @return the accessor for the animal ID
     */
    @Override
    public EntityDataAccessor<Integer> getAnimalIdAccessor()
    {
        return DATA_MANAGED_ANIMAL_ID;
    }

    /**
     * Get the managed animal ID of this entity.
     *
     * @return the managed animal ID
     */
    @Override
    public int getManagedAnimalId()
    {
        return entityData.get(DATA_MANAGED_ANIMAL_ID);
    }


    @Override
    public void setManagedAnimalId(final int managedAnimalId)
    {   
        entityData.set(DATA_MANAGED_ANIMAL_ID, managedAnimalId);
    }

    /**
     * Check if the horse has a valid cavalry rider.
     * 
     * The rider must be an instance of EntityCitizen and have a valid job handler.
     * The job handler must also be an instance of JobCavalry.
     * 
     * @return true if the horse has a valid cavalry rider, false otherwise.
     */
    public boolean hasCavalryRider()
    {
        if (this.getPassengers().isEmpty())
        {
            return false;
        }
        Entity rider = this.getFirstPassenger();
        if (!(rider instanceof EntityCitizen guard) || guard.getCitizenJobHandler() == null)
        {
            return false;
        }

        /* Implementation will be finalized in Cavalry PR 4 of 4 */
        return false;
    }

    /**
     * Checks if the horse has a trainer entity (i.e. an entity which is leashing the horse) and if that entity is a citizen.
     * This is used to determine if the horse should be slim (using the vanilla width/height) or can be wide (using the custom width/height).
     *
     * @return true if the horse has a trainer citizen, false otherwise.
     */
    public boolean hasTrainer()
    {
        Entity trainer = this.getLeashHolder();

        if (trainer == null)
        {
            return false;
        }

        return trainer instanceof EntityCitizen;
    }

    /**
     * Adjusts the standing eye height based on the pose and dimensions given. For cavalry horses, this height is the same as the
     * standard horse height, since the width change doesn’t affect the eye height.
     * 
     * @param pose the pose of the horse
     * @param dims the dimensions of the horse
     * @return the adjusted eye height
     */
    @Override
    protected float getStandingEyeHeight(@Nonnull Pose pose, @Nonnull EntityDimensions dims)
    {
        return super.getStandingEyeHeight(pose, dims);
    }
    
    /**
     * Adds a passenger to the horse, dropping the leash and setting the navigation options of any citizen passenger
     * to not enter or open doors.
     *
     * @param passenger the passenger to add
     */
    @Override
    protected void addPassenger(@Nonnull Entity passenger)
    {
        super.addPassenger(passenger);
        dropLeash(true, false);
        
        if (passenger instanceof EntityCitizen guard) 
        {
            AbstractAdvancedPathNavigate nav = guard.getNavigation();

            if (nav instanceof MinecoloniesAdvancedPathNavigate mcnav) 
            {
                mcnav.getPathingOptions().setEnterDoors(false);
                mcnav.getPathingOptions().setCanOpenDoors(false);
            }
        }
    }

    /**
     * Called when a passenger is removed from this horse.
     * 
     * @param passenger the passenger being removed
     */
    @Override
    protected void removePassenger(@Nonnull Entity passenger)
    {
        super.removePassenger(passenger);

        if (passenger instanceof EntityCitizen guard) {
            AbstractAdvancedPathNavigate nav = guard.getNavigation();

            if (nav instanceof MinecoloniesAdvancedPathNavigate mcnav) {
                mcnav.getPathingOptions().setEnterDoors(true);
                mcnav.getPathingOptions().setCanOpenDoors(true);
            }
        }
    }

    /**
     * Sets the entity that this horse is leashed to.
     * 
     * @param entity the entity to leash to
     * @param sendPacket whether to send a packet to clients
     */

    @Override
    public void setLeashedTo(@Nonnull Entity entity, boolean sendPacket)
    {
        super.setLeashedTo(entity, sendPacket);
    }

    /**
     * Drops the leash for this horse, optionally sending a packet to clients and dropping the lead item.
     * 
     * @param broadcastPacket whether to send a packet to clients
     * @param dropLeadItem whether to drop the lead item
     */
    @Override
    public void dropLeash(boolean broadcastPacket, boolean dropLeadItem)
    {
        super.dropLeash(broadcastPacket, dropLeadItem);
    }

    /**
     * Gets the offset that passengers are riding at relative to the horse's y position. In this case, we lower the seat by 0.35 units
     * to make the rider's feet line up with the saddle. This is important for the cavalry horse model.
     * 
     * @return the double value of the y offset
     */
    @Override
    public double getPassengersRidingOffset()
    {
        double vanilla = super.getPassengersRidingOffset();
        double seatLowering = SEATING_OFFSET;

        return vanilla - seatLowering;
    }

    /**
     * Creates a new PathNavigation for this horse entity, overriding the default vanilla horse navigation. This allows the horse to
     * navigate the world in a way that is more suitable for guards.
     * 
     * @param level the level to spawn the new entity in
     * @return the new PathNavigation for this horse entity
     */
    @Override
    protected PathNavigation createNavigation(@Nonnull Level level)
    {
        MinecoloniesAdvancedPathNavigate pathNavigation = new MinecoloniesAdvancedPathNavigate(this, level);
        pathNavigation.getPathingOptions().setEnterDoors(false);
        pathNavigation.getPathingOptions().setEnterGates(true);
        pathNavigation.getPathingOptions().setCanOpenDoors(false);
        pathNavigation.getPathingOptions().withDropCost(1D);
        pathNavigation.getPathingOptions().withJumpCost(1D);
        pathNavigation.getPathingOptions().setPassDanger(false);
        pathNavigation.getPathingOptions().setCanSwim(true);
        pathNavigation.setCanFloat(true);

        return pathNavigation;
    }

    /** 
     * Allow breeding even when not tamed. Keep vanilla species rules. 
     * 
     */
    @Override
    public boolean canMate(@Nonnull Animal other)
    {
        if (other == this) return false;
        if (!(other instanceof AbstractHorse)) return false;
        if (this.isBaby() || other.isBaby()) return false;

        // vanilla enforces isTamed() here; we intentionally skip that
        if (other instanceof Mule) return false;
        if ((this instanceof Horse && (other instanceof Horse || other instanceof Donkey)))
        {
            return true;
        }
        return false;
    }

    /**
     * Called every tick to update the horse. 
     */
    @Override
    public void tick()
    {
        super.tick();

        if (!level().isClientSide)
        {

            if (hasReservation())
            {
                if (reservationExpiration > RESERVATION_EXPIRATION_LIMIT) 
                {
                    clearReservation(); 
                    reservationExpiration = 0;
                } 
                else
                {
                    reservationExpiration++;
                }
            }

            if (!isReadyForCombat())
            {
                this.getPassengers().forEach(Entity::stopRiding);
            }

            Entity rider = this.getControllingPassenger();
            if (rider instanceof EntityCitizen cavunit)
            {
                MinecoloniesAdvancedPathNavigate nav = (MinecoloniesAdvancedPathNavigate) this.getNavigation();

                // Try to keep our rider looking where the horse is heading...
                if (nav.getPath() != null && !nav.getPath().isDone())
                {
                    BlockPos next = nav.getPath().getNextNodePos();
                    cavunit.getLookControl().setLookAt(next.getX() + 0.5, next.getY(), next.getZ() + 0.5, 30.0F, 30.0F);
                }
            }
        }
    }

    /**
     * Logs all active goals for this horse, including both the wrapped goal selector goals
     * and the target selector goals. This is useful for debugging purposes.
     * This function will only be executed every LOG_COOLDOWN_INTERVAL ticks.
     */
    public void logActiveGoals()
    {
        if (logCooldown > 0)
        {
            logCooldown--;
            return;
        }

        logCooldown = LOG_COOLDOWN_INTERVAL;

        for (WrappedGoal wrapped : this.goalSelector.getAvailableGoals())
        {
            Goal goal = wrapped.getGoal();
            if (wrapped.isRunning())
            {
                Log.getLogger().info("Active Wrapped Goal for horse {}: {}", this.getUUID(), goal.getClass().getSimpleName());
            }
        }

        for (WrappedGoal wrapped : this.targetSelector.getAvailableGoals())
        {
            Goal goal = wrapped.getGoal();
            if (wrapped.isRunning())
            {
                Log.getLogger().info("Active Target Goal for horse {}: {}", this.getUUID(), goal.getClass().getSimpleName());
            }
        }
    }

    /**
     * Creates a new CavalryHorseEntity from a vanilla AbstractHorse, attempting to preserve as much information as possible.
     * 
     * @param level   the level to spawn the new entity in
     * @param vanilla the vanilla horse to convert
     * @return the new CavalryHorseEntity, or null if the conversion failed
     */
    public static CavalryHorseEntity createFromVanilla(IColony colony, Level level, AbstractHorse vanilla)
    {
        if (level.isClientSide) return null;

        // If already a CavalryHorseEntity, return it
        if (vanilla instanceof CavalryHorseEntity) return (CavalryHorseEntity) vanilla;

        // If not a living vanilla horse, return null
        if (vanilla == null || !vanilla.isAlive() || vanilla.isVehicle()) return null;

        // --- Snapshot generic AbstractHorse state ---
        final boolean wasTamed = vanilla.isTamed();
        final UUID owner = vanilla.getOwnerUUID();
        final int temper = vanilla.getTemper();
        final double health = vanilla.getHealth();
        final String customName = vanilla.hasCustomName() ? vanilla.getName().getString() : null;

        AttributeInstance healthAttr = vanilla.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance speedAttr = vanilla.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance jumpAttr = vanilla.getAttribute(Attributes.JUMP_STRENGTH);

        // TODO: Create research that improves the capability of CavalryHorses
        double maxHealth = healthAttr != null ? healthAttr.getBaseValue() * 1.25 : 30.0D;
        double moveSpeed = speedAttr != null ? speedAttr.getBaseValue() * 1.25 : 0.25D;
        double jumpStrength = jumpAttr != null ? 0.7D : 0.7D;

        // --- Snapshot Horse-specific state (variant/armor) if applicable ---
        Variant variant = null;
        if (vanilla instanceof Horse h)
        {
            variant = h.getVariant();
        }

        // Leash (if any)
        Entity leashHolder = vanilla.getLeashHolder();

        // Convert to CavalryHorseEntity
        CavalryHorseEntity cav = vanilla.convertTo(ModEntities.CAVALRY_HORSE, true);
        if (cav == null) return null;

        IAnimalData animalData = colony.getAnimalManager().createAndRegisterAnimalData(cav);
        cav.setAnimalData(animalData);
        cav.setColonyId(colony.getID());

        // Re-apply attributes & health
        AttributeInstance cavHealthAttr = cav.getAttribute(Attributes.MAX_HEALTH);
        if (cavHealthAttr != null)
        {
            cavHealthAttr.setBaseValue(maxHealth);
        }

        AttributeInstance cavSpeedAttr = cav.getAttribute(Attributes.MOVEMENT_SPEED);
        if (cavSpeedAttr != null)
        {
            cavSpeedAttr.setBaseValue(moveSpeed);
        }

        AttributeInstance cavJumpAttr = cav.getAttribute(Attributes.JUMP_STRENGTH);
        if (cavJumpAttr != null)
        {
            cavJumpAttr.setBaseValue(jumpStrength);
        }
        cav.setMaxUpStep(1.1F);

        cav.setHealth((float) Math.min(health, maxHealth));

        // Re-apply AbstractHorse state
        cav.setTamed(wasTamed);
        cav.setOwnerUUID(owner);
        cav.setTemper(temper);
        cav.setPersistenceRequired();

        // Re-apply Horse-specific visuals
        if (variant != null)
        {
            cav.setVariant(variant);
        }

        // Name & leash
        if (customName != null)
        {
            cav.setCustomName(Component.literal(customName));
        }

        if (leashHolder != null)
        {
            cav.setLeashedTo(leashHolder, true);
        }

        return cav;
    }

    /**
     * Whether this entity should be saved to disk.
     * <p>As CavelryHorse entities are always saved to disk, this method always returns true.
     */
    @Override
    public boolean shouldBeSaved()
    {
        return true;
    }

    /**
     * Applies damage to this entity.
     * A portion of that damage is also applied to the combat cooldown counter.
     *
     * @param damageSource the source of the damage
     * @param damageAmount the amount of damage to apply
     * @return true if the damage was applied, false otherwise
     */
    @Override
    public boolean hurt(@Nonnull DamageSource damageSource, float damageAmount)
    {
       
        if (level().isClientSide)
        {
            return true;
        }

        // Percentage of damage applied to combat cooldown
        float cooldownImpact = .40f;

        if (damageSource.is(DamageTypeTags.IS_EXPLOSION) && damageSource.getEntity() instanceof Creeper) 
        {
            // TODO: Introduce research to improve explosion damage mitigation.
            damageAmount *= 0.30f;
        }

        if (damageSource.is(DamageSourceKeys.STUCK_DAMAGE))
        {
            damageAmount *= 0.0f;
        }

        // TODO: Create research that provides combat cooldown mitigation
        animalData.setCombatCooldown(animalData.getCombatCooldown() + (damageAmount * cooldownImpact));
        animalData.markDirty();

        return super.hurt(damageSource, damageAmount);
    }

    /**
     * Adds additional data to the CompoundTag that is specific to this entity type. This data is saved to disk and can be read back in
     * when the entity is loaded. The data added is as follows: - stablePos: The position of the stable block, or null if not set. -
     * stableDim: The dimension of the stable block, or null if not set.
     */
    @Override
    public void addAdditionalSaveData(@Nonnull CompoundTag tag)
    {
        super.addAdditionalSaveData(tag);

        tag.putInt(NbtTagConstants.TAG_COLONY_ID, animalColonyHandler.getColonyId());
        if (animalData != null)
        {
            tag.putInt(NbtTagConstants.TAG_MANAGED_ANIMALID, getManagedAnimalId());
        }
    }


    /**
     * Reads additional data from the given CompoundTag that is specific to this entity type.
     * <p>
     * This method is called when the entity is loaded from disk, and the data read is used to initialize the entity.
     * <p>
     * The data that is read is as follows:
     * - TAG_COLONY_ID: The id of the colony that the entity is associated with.
     * - TAG_MANAGED_ANIMALID: The id of the managed animal data that is associated with the entity.
     * <p>
     * If the TAG_MANAGED_ANIMALID is not present, then a new managed animal data is created and associated with the entity.
     * Other persisted data is managed through the associated IAnimalData.
     */
    @Override
    public void readAdditionalSaveData(@Nonnull CompoundTag tag)
    {
        super.readAdditionalSaveData(tag);

        if (tag.contains(NbtTagConstants.TAG_COLONY_ID))
        {
            int colonyId = tag.getInt(NbtTagConstants.TAG_COLONY_ID);
            setColonyId(colonyId);

            if (tag.contains(NbtTagConstants.TAG_MANAGED_ANIMALID))
            {
                setManagedAnimalId(tag.getInt(NbtTagConstants.TAG_MANAGED_ANIMALID));
            }
            else
            {
                IColony colony = animalColonyHandler.getColony();

                if (colony != null)
                {
                    animalData = colony.getAnimalManager().createAndRegisterAnimalData(this);
                }
            }
        }
    }

    /**
     * Prepares the horse for combat by reducing its combat cooldown by the specified amount of combat readiness. 
     * This amount is subtracted from the horse's current combat cooldown.
     * 
     * @param combatReadiness the amount of combat readiness to subtract from the horse's combat cooldown.
     */
    public void prepareForCombat(float combatReadiness)
    {
        animalData.setCombatCooldown(animalData.getCombatCooldown() - Math.abs(combatReadiness));
    }

    /**
     * Returns whether the horse is ready for combat. This is calculated by seeing if the combat cooldown is less than or equal 
     * to the readiness threshold relative to the maximum health of the horse.
     * 
     * @return true if the horse is ready for combat, false otherwise.
     */
    public boolean isReadyForCombat()
    {
        if (animalData == null) return false;

        return animalData.getCombatCooldown() <= (this.getMaxHealth() * COMBAT_READINESS_THRESHOLD);
    }

    /**
     * A minor horizontal collision is one that occurs when the horse has moved into a solid block. This is different from a major
     * collision, which is when the horse has moved into another entity. This method is overridden to set the lastHorizontalCollision
     * field to the current game time when a minor collision occurs.
     * 
     * @param vec3 the movement vector of the horse
     * @return true if the horse moved into a solid block, false otherwise
     */
    @Override
    protected boolean isHorizontalCollisionMinor(@Nonnull Vec3 vec3)
    {
        lastHorizontalCollision = level.getGameTime();
        return super.isHorizontalCollisionMinor(vec3);
    }

    /**
     * Whether the horse collided in the last 10 ticks
     *
     * @return
     */
    public boolean hadHorizontalCollission()
    {
        return level.getGameTime() - lastHorizontalCollision < 10;
    }

    /**
     * Reserves the horse for the given entity. When the horse is reserved, it will not be able to be mounted or interacted with
     * by other entities until the reservation is cleared.
     *
     * @param reserver the entity to reserve the horse for
     */
    public void reserve(Entity reserver) 
    { 
        CompoundTag data = this.getPersistentData();
        data.putUUID(RESERVE_KEY, reserver.getUUID());
        reservationExpiration = 0;
    }

    /**
     * Clears the reservation on the horse. When the reservation is cleared, 
     * the horse can once again be mounted and interacted with by other entities.
     */
    public void clearReservation()
    {
        this.getPersistentData().remove(RESERVE_KEY);
    }

    /**
     * Clears the reservation on the horse if it is reserved by the entity with the given UUID.
     *
     * @param reserver the entity to check against
     * @return true if the reservation was cleared, false otherwise
     */
    public boolean clearFor(Entity reserver)
    {
        CompoundTag data = this.getPersistentData();
        if (data.contains(RESERVE_KEY, Tag.TAG_INT_ARRAY))
        {
            UUID who = data.getUUID(RESERVE_KEY);
            if (reserver.getUUID().equals(who))
            {
                clearReservation();
                return true;
            }
        }

        return false;
    }

    /**
     * Retrieves the UUID of the entity that has reserved the horse, or null if no one has reserved it.
     * @param horse the horse to query
     * @return the UUID of the reserver, or null if no one has reserved it
     */
    public UUID reservedBy()
    {
        CompoundTag data = this.getPersistentData();
        return data.contains(RESERVE_KEY, Tag.TAG_INT_ARRAY) ? data.getUUID(RESERVE_KEY) : null;
    }

    /**
     * Returns true if the horse is reserved by any entity, false otherwise.
     * @param horse the horse to check
     * @return true if the horse is reserved, false otherwise
     */
    public boolean hasReservation()
    {
        return reservedBy() != null;
    }

    /**
     * Returns the stable building of the horse if it exists. If the horse does not have a stable block position, 
     * or if the block position is not a stable building, this method returns null.
     * 
     * @return the stable building of the horse, or null if the horse does not have one.
     */
    public IBuilding getStableBuilding()
    {
        if (animalData == null) return null;

        IBuilding building = animalData.getHomeBuilding();

        return building;
    }

    /**
     * Checks if the horse is currently within the boundaries of its stable building.
     * <p>
     * If the horse does not have a stable, returns false.
     * </p>
     * <p>
     * If the horse is currently within the boundaries of its stable building, returns true. Otherwise, returns false.
     * </p>
     * @return true if the horse is within its stable building, false otherwise
     */
    public boolean isInStable()
    {
        IBuilding stable = getStableBuilding();
        if (stable == null) return false;
        
        if (stable.isInBuilding(this.getOnPos())) {
            return true;
        }

        return false;
    }

    /**
     * Returns the animal data associated with this horse.
     *
     * @return the animal data associated with this horse.
     */
    @Override
    public IAnimalData getAnimalData()
    {
        return animalData;
    }

    /**
     * Returns the animal data view associated with this horse.
     *
     * @return the animal data view associated with this horse.
     */
    @Override
    public IAnimalDataView getAnimalDataView()
    {
        return animalDataView;
    }

    /**
     * Sets the animal data associated with this horse.
     *
     * @param data The animal data associated with this horse.
     */
    @Override
    public void setAnimalData(IAnimalData data)
    {
        if (data == null) 
        {
            return;
        }

        this.animalData = data;
    }

    /**
     * Returns the entity itself. This is used in the IManagedAnimal interface to get the entity associated with the managed animal.
     * <p>
     * This method is used to get the entity associated with the managed animal, which in this case is the horse entity.
     * <p>
     * @return the horse entity associated with the managed animal.
     */
    @Override
    public CavalryHorseEntity getEntity()
    {
        return this;
    }
}
