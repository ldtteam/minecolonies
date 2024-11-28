package com.minecolonies.api.entity.citizen;

import com.google.common.collect.Lists;
import com.minecolonies.api.client.render.modeltype.IModelType;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.entity.ai.statemachine.states.EntityState;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateStateMachine;
import com.minecolonies.api.entity.citizen.citizenhandlers.*;
import com.minecolonies.api.entity.other.MinecoloniesMinecart;
import com.minecolonies.api.entity.pathfinding.proxy.IWalkToProxy;
import com.minecolonies.api.entity.pathfinding.registry.IPathNavigateRegistry;
import com.minecolonies.api.inventory.InventoryCitizen;
import com.minecolonies.api.sounds.EventType;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.core.entity.pathfinding.navigation.AbstractAdvancedPathNavigate;
import com.minecolonies.core.entity.pathfinding.navigation.PathingStuckHandler;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.util.constant.CitizenConstants.*;

/**
 * The abstract citizen entity.
 */
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.CouplingBetweenObjects"})
public abstract class AbstractEntityCitizen extends AbstractCivilianEntity implements MenuProvider
{
    public static final int ENTITY_AI_TICKRATE = 5;

    /**
     * Citizens swim speed factor
     */
    private static final double CITIZEN_SWIM_BONUS = 2.0;

    public static final EntityDataAccessor<Integer>  DATA_LEVEL           = SynchedEntityData.defineId(AbstractEntityCitizen.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer>  DATA_TEXTURE         = SynchedEntityData.defineId(AbstractEntityCitizen.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer>  DATA_IS_FEMALE       = SynchedEntityData.defineId(AbstractEntityCitizen.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer>  DATA_COLONY_ID       = SynchedEntityData.defineId(AbstractEntityCitizen.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer>  DATA_CITIZEN_ID      = SynchedEntityData.defineId(AbstractEntityCitizen.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<String>   DATA_MODEL           = SynchedEntityData.defineId(AbstractEntityCitizen.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String>   DATA_RENDER_METADATA = SynchedEntityData.defineId(AbstractEntityCitizen.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Boolean>  DATA_IS_ASLEEP       = SynchedEntityData.defineId(AbstractEntityCitizen.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean>  DATA_IS_CHILD        = SynchedEntityData.defineId(AbstractEntityCitizen.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<BlockPos> DATA_BED_POS         = SynchedEntityData.defineId(AbstractEntityCitizen.class, EntityDataSerializers.BLOCK_POS);
    public static final EntityDataAccessor<String>   DATA_STYLE           = SynchedEntityData.defineId(AbstractEntityCitizen.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String>   DATA_TEXTURE_SUFFIX  = SynchedEntityData.defineId(AbstractEntityCitizen.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String>   DATA_JOB             = SynchedEntityData.defineId(AbstractEntityCitizen.class, EntityDataSerializers.STRING);

    /**
     * The default model.
     */
    private ResourceLocation modelId = ModModelTypes.SETTLER_ID;

    /**
     * The texture id.
     */
    private int textureId;

    /**
     * Additional render data.
     */
    private String renderMetadata = "";

    /**
     * The gender, true if female.
     */
    private boolean female;

    /**
     * The texture.
     */
    private ResourceLocation texture;

    /**
     * Was the texture initiated with the citizen view.
     */
    private boolean textureDirty = true;

    private AbstractAdvancedPathNavigate pathNavigate;

    /**
     * Counts entity collisions
     */
    private int collisionCounter = 0;

    /**
     * The collision threshold
     */
    private final static int COLL_THRESHOLD = 50;

    /**
     * Flag to check if the equipment is dirty.
     */
    private boolean isEquipmentDirty = true;

    /**
     * The AI for citizens, controlling different global states
     */
    protected ITickRateStateMachine<IState> entityStateController = new TickRateStateMachine<>(EntityState.INIT,
      e -> Log.getLogger()
        .warn("Citizen " + getDisplayName().getString() + " id:" + (getCitizenData() != null ? getCitizenData().getId() : -1) + "from colony: "
                + getCitizenColonyHandler().getColonyId() + " state controller exception", e), ENTITY_AI_TICKRATE);

    /**
     * Constructor for a new citizen typed entity.
     *
     * @param type  the Entity type.
     * @param world the world.
     */
    public AbstractEntityCitizen(final EntityType<? extends PathfinderMob> type, final Level world)
    {
        super(type, world);
    }

    /**
     * Get the default attributes with their values.
     *
     * @return the attribute modifier map.
     */
    public static AttributeSupplier.Builder getDefaultAttributes()
    {
        return LivingEntity.createLivingAttributes()
          .add(Attributes.MAX_HEALTH, BASE_MAX_HEALTH)
          .add(Attributes.MOVEMENT_SPEED, BASE_MOVEMENT_SPEED)
          .add(Attributes.FOLLOW_RANGE, BASE_PATHFINDING_RANGE);
    }

    public GoalSelector getTasks()
    {
        return goalSelector;
    }

    public int getTicksExisted()
    {
        return tickCount;
    }

    /**
     * Disable vanilla's item picking stuff as we're doing it ourselves
     */
    @Override
    public boolean canPickUpLoot()
    {
        return false;
    }

    /**
     * Disable vanilla steering logic for villagers
     */
    @Override
    public boolean isControlledByLocalInstance()
    {
        return this.isEffectiveAi();
    }

    /**
     * Calculate adjusted damage.
     * This doesn't actually damage armor, for non-player entities.
     *
     * @param source
     * @param damage
     * @return
     */
    public float calculateDamageAfterAbsorbs(DamageSource source, float damage)
    {
        float newDamage = this.getDamageAfterArmorAbsorb(source, damage);
        return this.getDamageAfterMagicAbsorb(source, newDamage);
    }

    @NotNull
    @Override
    public InteractionResult interactAt(final Player player, final Vec3 vec, final InteractionHand hand)
    {
        if (!player.level().isClientSide())
        {
            if (this.getPose() == Pose.SLEEPING)
            {
                SoundUtils.playSoundAtCitizenWith(CompatibilityUtils.getWorldFromCitizen(this), this.blockPosition(), EventType.OFF_TO_BED, this.getCitizenData(), 100);
            }
            else if (getCitizenData() != null && getCitizenData().isIdleAtJob())
            {
                SoundUtils.playSoundAtCitizenWith(CompatibilityUtils.getWorldFromCitizen(this), this.blockPosition(), EventType.MISSING_EQUIPMENT, this.getCitizenData(), 100);
            }
            else
            {
                SoundUtils.playSoundAtCitizenWith(CompatibilityUtils.getWorldFromCitizen(this), this.blockPosition(), EventType.INTERACTION, this.getCitizenData(), 100);
            }
        }

        return super.interactAt(player, vec, hand);
    }

    /**
     * Returns false if the newer Entity AI code should be run.
     */
    @Override
    public boolean isNoAi()
    {
        return false;
    }

    /**
     * Sets the textures of all citizens and distinguishes between male and female.
     */
    public void setTexture()
    {
        if (!CompatibilityUtils.getWorldFromCitizen(this).isClientSide)
        {
            return;
        }

        final IModelType modelType = IModelTypeRegistry.getInstance().getModelType(getModelType());
        if (modelType == null)
        {
            Log.getLogger().error("Null model type for: " + getModelType() + " of: " + this);
            textureDirty = false;
            return;
        }

        texture = modelType.getTexture(this);
        textureDirty = false;
    }

    /**
     * Get the citizen data view.
     *
     * @return the view.
     */
    public abstract ICitizenDataView getCitizenDataView();

    /**
     * Getter of the resource location of the texture.
     *
     * @return location of the texture.
     */
    @NotNull
    public ResourceLocation getTexture()
    {
        if (texture == null
              || textureDirty
              || !texture.getPath().contains(getEntityData().get(DATA_STYLE))
              || !texture.getPath().contains(getEntityData().get(DATA_TEXTURE_SUFFIX)))
        {
            setTexture();
        }
        return texture;
    }

    /**
     * Set the texture dirty.
     */
    public void setTextureDirty()
    {
        this.textureDirty = true;
    }

    /**
     * Get the model assigned to the citizen.
     *
     * @return the model.
     */
    public ResourceLocation getModelType()
    {
        return modelId;
    }

    @Override
    protected void defineSynchedData()
    {
        super.defineSynchedData();
        entityData.define(DATA_TEXTURE_SUFFIX, "_b");
        entityData.define(DATA_TEXTURE, 0);
        entityData.define(DATA_LEVEL, 0);
        entityData.define(DATA_STYLE, "default");
        entityData.define(DATA_IS_FEMALE, 0);
        entityData.define(DATA_MODEL, ModModelTypes.SETTLER_ID.toString());
        entityData.define(DATA_RENDER_METADATA, "");
        entityData.define(DATA_IS_ASLEEP, false);
        entityData.define(DATA_IS_CHILD, false);
        entityData.define(DATA_BED_POS, new BlockPos(0, 0, 0));
        entityData.define(DATA_JOB, "");
    }

    /**
     * Getter which checks if the citizen is female.
     *
     * @return true if female.
     */
    public boolean isFemale()
    {
        return female;
    }

    /**
     * Set the gender.
     *
     * @param female true if female, false if male.
     */
    public void setFemale(final boolean female)
    {
        this.female = female;
    }

    @NotNull
    @Override
    public AbstractAdvancedPathNavigate getNavigation()
    {
        if (this.pathNavigate == null)
        {
            this.pathNavigate = IPathNavigateRegistry.getInstance().getNavigateFor(this);
            this.navigation = pathNavigate;
            this.pathNavigate.setCanFloat(true);
            this.pathNavigate.setSwimSpeedFactor(CITIZEN_SWIM_BONUS);
            this.pathNavigate.getPathingOptions().setEnterDoors(true);
            this.pathNavigate.getPathingOptions().setCanOpenDoors(true);
            this.pathNavigate.setStuckHandler(PathingStuckHandler.createStuckHandler().withTeleportOnFullStuck().withTeleportSteps(5));
        }
        return pathNavigate;
    }

    /**
     * Don't push if we're ignoring being pushed
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

    /**
     * Ignores entity collisions are colliding for a while, solves stuck e.g. for many trying to take the same door
     *
     * @param entityIn entity to collide with
     */
    @Override
    public void push(@NotNull final Entity entityIn)
    {
        if ((collisionCounter += 2) > COLL_THRESHOLD)
        {
            if (collisionCounter > COLL_THRESHOLD * 2)
            {
                collisionCounter = 0;
            }

            return;
        }

        if (this.vehicle instanceof MinecoloniesMinecart)
        {
            return;
        }
        super.push(entityIn);
    }

    @Override
    public void onPlayerCollide(final Player player)
    {
        if (getCitizenData() == null)
        {
            super.onPlayerCollide(player);
            return;
        }

        final IJob<?> job = getCitizenData().getJob();
        if (job == null || !job.isGuard())
        {
            super.onPlayerCollide(player);
        }
        else
        {
            // guards push the player out of their way
            push(player);
        }
    }

    @Override
    public boolean isPushable()
    {
        if (this.vehicle instanceof MinecoloniesMinecart)
        {
            return false;
        }
        return super.isPushable();
    }

    @Override
    public void aiStep()
    {
        super.aiStep();
        if (tickCount % ENTITY_AI_TICKRATE == 0)
        {
            entityStateController.tick();
        }
        updateSwingTime();
        if (collisionCounter > 0)
        {
            collisionCounter--;
        }
    }

    /**
     * Set the rotation of the citizen.
     *
     * @param yaw   the rotation yaw.
     * @param pitch the rotation pitch.
     */
    public void setOwnRotation(final float yaw, final float pitch)
    {
        this.setRot(yaw, pitch);
    }

    /**
     * Set the model id.
     *
     * @param model the model.
     */
    public void setModelId(final ResourceLocation model)
    {
        this.modelId = model;
    }

    /**
     * Set the render meta data.
     *
     * @param renderMetadata the metadata to set.
     */
    public void setRenderMetadata(final String renderMetadata)
    {
        if (renderMetadata.equals(getRenderMetadata()))
        {
            return;
        }
        this.renderMetadata = renderMetadata;
        entityData.set(DATA_RENDER_METADATA, getRenderMetadata());
    }

    /**
     * Getter for the texture id.
     *
     * @return the texture id.
     */
    public int getTextureId()
    {
        return this.textureId;
    }

    /**
     * Set the texture id.
     *
     * @param textureId the id of the texture.
     */
    public void setTextureId(final int textureId)
    {
        this.textureId = textureId;
        entityData.set(DATA_TEXTURE, textureId);
    }

    /**
     * Getter for the render metadata.
     *
     * @return the meta data.
     */
    public String getRenderMetadata()
    {
        return renderMetadata;
    }

    /**
     * Getter of the citizens random object.
     *
     * @return random object.
     */
    public RandomSource getRandom()
    {
        return random;
    }

    public int getOffsetTicks()
    {
        return this.tickCount + OFFSET_TICK_MULTIPLIER * this.getId();
    }

    @Override
    public boolean isBlocking()
    {
        return getUseItem().getItem() instanceof ShieldItem;
    }

    /**
     * Check if recently hit.
     *
     * @return the count of how often.
     */
    public int getRecentlyHit()
    {
        return lastHurtByPlayerTime;
    }

    /**
     * Check if can drop loot.
     *
     * @return true if so.
     */
    public boolean checkCanDropLoot()
    {
        return shouldDropExperience();
    }

    /**
     * Get the ILocation of the citizen.
     *
     * @return an ILocation object which contains the dimension and is unique.
     */
    public abstract ILocation getLocation();

    /**
     * Checks if a worker is at his working site. If he isn't, sets it's path to the location
     *
     * @param site  the place where he should walk to
     * @param range Range to check in
     * @return True if worker is at site, otherwise false.
     */
    public abstract boolean isWorkerAtSiteWithMove(@NotNull BlockPos site, int range);

    /**
     * Getter for the citizendata. Tries to get it from the colony is the data is null.
     *
     * @return the data.
     */
    public abstract ICitizenData getCitizenData();

    /**
     * Return this citizens inventory.
     *
     * @return the inventory this citizen has.
     */
    @NotNull
    public abstract InventoryCitizen getInventoryCitizen();

    @NotNull
    public abstract IItemHandler getItemHandlerCitizen();

    /**
     * Sets whether this entity is a child
     *
     * @param isChild boolean
     */
    public abstract void setIsChild(boolean isChild);

    /**
     * Play move away sound when running from an entity.
     */
    public abstract void playMoveAwaySound();

    /**
     * Get the path proxy of the citizen.
     *
     * @return the proxy.
     */
    public abstract IWalkToProxy getProxy();

    /**
     * Decrease the saturation of the citizen for 1 action.
     */
    public abstract void decreaseSaturationForAction();

    /**
     * Decrease the saturation of the citizen for 1 action.
     */
    public abstract void decreaseSaturationForContinuousAction();

    /**
     * The Handler for all experience related methods.
     *
     * @return the instance of the handler.
     */
    public abstract ICitizenExperienceHandler getCitizenExperienceHandler();

    /**
     * The Handler for all inventory related methods.
     *
     * @return the instance of the handler.
     */
    public abstract ICitizenInventoryHandler getCitizenInventoryHandler();

    public abstract void setCitizenInventoryHandler(ICitizenInventoryHandler citizenInventoryHandler);

    /**
     * The Handler for all colony related methods.
     *
     * @return the instance of the handler.
     */
    public abstract ICitizenColonyHandler getCitizenColonyHandler();

    public abstract void setCitizenColonyHandler(ICitizenColonyHandler citizenColonyHandler);

    /**
     * The Handler for all job related methods.
     *
     * @return the instance of the handler.
     */
    public abstract ICitizenJobHandler getCitizenJobHandler();

    /**
     * The Handler for all job related methods.
     *
     * @return the instance of the handler.
     */
    public abstract ICitizenSleepHandler getCitizenSleepHandler();

    public abstract float getRotationYaw();

    public abstract float getRotationPitch();

    public abstract boolean isDead();

    public abstract void setCitizenSleepHandler(ICitizenSleepHandler citizenSleepHandler);

    public abstract void setCitizenJobHandler(ICitizenJobHandler citizenJobHandler);

    public abstract void setCitizenExperienceHandler(ICitizenExperienceHandler citizenExperienceHandler);

    /**
     * Calls a guard for help against an attacker.
     *
     * @param attacker       the attacking entity
     * @param guardHelpRange the squaredistance in which we search for nearby guards
     */
    public abstract void callForHelp(final Entity attacker, final int guardHelpRange);

    @Override
    public void detectEquipmentUpdates()
    {
        if (this.isEquipmentDirty && tickCount % 20 == randomVariance)
        {
            this.isEquipmentDirty = false;
            List<Pair<EquipmentSlot, ItemStack>> list = Lists.newArrayListWithCapacity(6);

            list.add(new Pair<>(EquipmentSlot.CHEST, getItemBySlot(EquipmentSlot.CHEST)));
            list.add(new Pair<>(EquipmentSlot.FEET, getItemBySlot(EquipmentSlot.FEET)));
            list.add(new Pair<>(EquipmentSlot.HEAD, getItemBySlot(EquipmentSlot.HEAD)));
            list.add(new Pair<>(EquipmentSlot.LEGS, getItemBySlot(EquipmentSlot.LEGS)));
            list.add(new Pair<>(EquipmentSlot.OFFHAND, getItemBySlot(EquipmentSlot.OFFHAND)));
            list.add(new Pair<>(EquipmentSlot.MAINHAND, getItemBySlot(EquipmentSlot.MAINHAND)));
            ((ServerLevel) this.level).getChunkSource().broadcast(this, new ClientboundSetEquipmentPacket(this.getId(), list));
        }
    }

    @Override
    public void setItemSlot(final EquipmentSlot slot, @NotNull final ItemStack newItem)
    {
        if (!level.isClientSide)
        {
            final ItemStack previous = getItemBySlot(slot);
            if (!ItemStackUtils.compareItemStacksIgnoreStackSize(previous, newItem, false, true))
            {
                markEquipmentDirty();
            }
        }
        super.setItemSlot(slot, newItem);
    }

    /**
     * On armor removal.
     * @param stack the removed armor.
     */
    public void onArmorRemove(final ItemStack stack, final EquipmentSlot equipmentSlot)
    {
        this.getAttributes().removeAttributeModifiers(stack.getAttributeModifiers(equipmentSlot));
    }

    /**
     * On armor equip.
     * @param stack the added armor.
     */
    public void onArmorAdd(final ItemStack stack, final EquipmentSlot equipmentSlot)
    {
        this.getAttributes().addTransientAttributeModifiers(stack.getAttributeModifiers(equipmentSlot));
    }

    /**
     * Mark the equipment as dirty.
     */
    public void markEquipmentDirty()
    {
        this.isEquipmentDirty = true;
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

    /**
     * Get the entities state controller
     *
     * @return
     */
    public ITickRateStateMachine<IState> getEntityStateController()
    {
        return entityStateController;
    }

    @Override
    public boolean isSleeping()
    {
        return getCitizenSleepHandler().isAsleep();
    }

    @Override
    public int getTeamColor()
    {
        if (getCitizenColonyHandler().getColony() == null)
        {
            return super.getTeamColor();
        }
        return getCitizenColonyHandler().getColony().getTeamColonyColor().getColor();
    }

    @Override
    @NotNull
    public Component getDisplayName()
    {
        if (getCitizenColonyHandler().getColony() == null)
        {
            return super.getDisplayName();
        }
        if (getName() instanceof MutableComponent mutableComponent)
        {
            return mutableComponent.withStyle(getCitizenColonyHandler().getColony().getTeamColonyColor()).withStyle((style) -> style.withHoverEvent(this.createHoverEvent()).withInsertion(this.getStringUUID()));
        }
        return super.getDisplayName();
    }
}
