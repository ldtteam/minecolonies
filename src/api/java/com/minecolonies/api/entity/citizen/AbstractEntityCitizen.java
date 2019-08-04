package com.minecolonies.api.entity.citizen;

import com.minecolonies.api.client.render.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.entity.ai.DesiredActivity;
import com.minecolonies.api.entity.ai.pathfinding.IWalkToProxy;
import com.minecolonies.api.entity.citizen.citizenhandlers.*;
import com.minecolonies.api.entity.pathfinding.AbstractAdvancedPathNavigate;
import com.minecolonies.api.entity.pathfinding.registry.IPathNavigateRegistry;
import com.minecolonies.api.inventory.InventoryCitizen;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.block.BlockState;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ShieldItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

import static com.minecolonies.api.util.constant.CitizenConstants.*;
import static com.minecolonies.api.util.constant.Constants.ONE_HUNDRED_PERCENT;

/**
 * The abstract citizen entity.
 */
public abstract class AbstractEntityCitizen extends AgeableEntity implements ICapabilitySerializable<CompoundNBT>, INamedContainerProvider
{

    public static final DataParameter<Integer>  DATA_LEVEL           = EntityDataManager.createKey(AbstractEntityCitizen.class, DataSerializers.VARINT);
    public static final DataParameter<Integer>  DATA_TEXTURE         = EntityDataManager.createKey(AbstractEntityCitizen.class, DataSerializers.VARINT);
    public static final DataParameter<Integer>  DATA_IS_FEMALE       = EntityDataManager.createKey(AbstractEntityCitizen.class, DataSerializers.VARINT);
    public static final DataParameter<Integer>  DATA_COLONY_ID       = EntityDataManager.createKey(AbstractEntityCitizen.class, DataSerializers.VARINT);
    public static final DataParameter<Integer>  DATA_CITIZEN_ID      = EntityDataManager.createKey(AbstractEntityCitizen.class, DataSerializers.VARINT);
    public static final DataParameter<String>   DATA_MODEL           = EntityDataManager.createKey(AbstractEntityCitizen.class, DataSerializers.STRING);
    public static final DataParameter<String>   DATA_RENDER_METADATA = EntityDataManager.createKey(AbstractEntityCitizen.class, DataSerializers.STRING);
    public static final DataParameter<Boolean>  DATA_IS_ASLEEP       = EntityDataManager.createKey(AbstractEntityCitizen.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean>  DATA_IS_CHILD        = EntityDataManager.createKey(AbstractEntityCitizen.class, DataSerializers.BOOLEAN);
    public static final DataParameter<BlockPos> DATA_BED_POS         = EntityDataManager.createKey(AbstractEntityCitizen.class, DataSerializers.BLOCK_POS);

    /**
     * The default model.
     */
    private BipedModelType modelId = BipedModelType.SETTLER;

    /**
     * The texture id.
     */
    private int textureId;

    /**
     * Additional render data.
     */
    private String renderMetadata;

    /**
     * The gender, true if female.
     */
    private boolean female;

    /**
     * The texture.
     */
    private ResourceLocation texture;

    private AbstractAdvancedPathNavigate pathNavigate;

    /**
     * Constructor for a new citizen typed entity.
     *
     * @param world the world.
     */
    public AbstractEntityCitizen(final World world)
    {
        super(world);
    }

    public GoalSelector getTasks()
    {
        return goalSelector;
    }

    public int getTicksExisted()
    {
        return ticksExisted;
    }

    @Nullable
    @Override
    public Entity changeDimension(@NotNull final DimensionType destination)
    {
        return null;
    }

    @NotNull
    @Override
    public BlockPos getPosition()
    {
        return new BlockPos(posX, posY, posZ);
    }

    @Override
    public boolean canPickUpLoot()
    {
        return super.canPickUpLoot();
    }

    public float getPreviousRotationPitch()
    {
        return prevRotationPitch;
    }

    public float getPreviousRotationYaw()
    {
        return prevRotationYaw;
    }

    public float getPreviousRenderYawOffset()
    {
        return prevRenderYawOffset;
    }

    public float getRenderYawOffset()
    {
        return renderYawOffset;
    }

    public double getPreviousPosX()
    {
        return prevPosX;
    }

    public double getPreviousPosY()
    {
        return prevPosY;
    }

    public double getPreviousPosZ()
    {
        return prevPosZ;
    }

    @NotNull
    @Override
    public ActionResultType applyPlayerInteraction(final PlayerEntity player, final Vec3d vec, final Hand hand)
    {
        SoundUtils.playInteractionSoundAtCitizenWithChance(CompatibilityUtils.getWorldFromCitizen(this), this.getPosition(), ONE_HUNDRED_PERCENT, this);
        return super.applyPlayerInteraction(player, vec, hand);
    }

    /**
     * Returns false if the newer Entity AI code should be run.
     */
    @Override
    public boolean isAIDisabled()
    {
        return false;
    }

    /**
     * Get the model assigned to the citizen.
     *
     * @return the model.
     */
    public BipedModelType getModelID()
    {
        return modelId;
    }

    /**
     * Getter of the resource location of the texture.
     *
     * @return location of the texture.
     */
    public ResourceLocation getTexture()
    {
        return texture;
    }

    /**
     * Sets the textures of all citizens and distinguishes between male and
     * female.
     */
    public void setTexture()
    {
        if (!CompatibilityUtils.getWorldFromCitizen(this).isRemote)
        {
            return;
        }

        final BipedModelType model = getModelID();

        final String textureBase = "textures/entity/" + model.textureBase + (female ? "Female" : "Male");
        final int moddedTextureId = (textureId % model.numTextures) + 1;
        texture = new ResourceLocation(Constants.MOD_ID, textureBase + moddedTextureId + renderMetadata + ".png");
    }

    /**
     * For the time being we don't want any childrens of our colonists.
     *
     * @param child the ageable entity.
     * @return the child.
     */
    @Nullable
    @Override
    public AgeableEntity createChild(@NotNull final AgeableEntity child)
    {
        return null;
    }

    @Override
    public void entityInit()
    {
        super.entityInit();
        dataManager.register(DATA_TEXTURE, 0);
        dataManager.register(DATA_LEVEL, 0);
        dataManager.register(DATA_IS_FEMALE, 0);
        dataManager.register(DATA_MODEL, BipedModelType.SETTLER.name());
        dataManager.register(DATA_RENDER_METADATA, "");
        dataManager.register(DATA_IS_ASLEEP, false);
        dataManager.register(DATA_IS_CHILD, false);
        dataManager.register(DATA_BED_POS, new BlockPos(0, 0, 0));
    }

    public double getPosX()
    {
        return posX;
    }

    public double getPosY()
    {
        return posY;
    }

    public double getPosZ()
    {
        return posZ;
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
    public AbstractAdvancedPathNavigate getNavigator()
    {
        if (this.pathNavigate == null)
        {
            this.pathNavigate = IPathNavigateRegistry.getInstance().getNavigateFor(this);
            this.navigator = pathNavigate;
            this.pathNavigate.setCanSwim(true);
        }
        return pathNavigate;
    }

    /**
     * Set the rotation of the citizen.
     *
     * @param yaw   the rotation yaw.
     * @param pitch the rotation pitch.
     */
    public void setOwnRotation(final float yaw, final float pitch)
    {
        this.setRotation(yaw, pitch);
    }

    /**
     * Set the model id.
     *
     * @param model the model.
     */
    public void setModelId(final BipedModelType model)
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
        this.renderMetadata = renderMetadata;
        dataManager.set(DATA_RENDER_METADATA, getRenderMetadata());
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
     * Applies attributes like health, charisma etc to the citizens.
     */
    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();

        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(BASE_MAX_HEALTH);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(BASE_MOVEMENT_SPEED);

        //path finding search range
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(BASE_PATHFINDING_RANGE);
    }

    /**
     * Getter of the citizens random object.
     *
     * @return random object.
     */
    public Random getRandom()
    {
        return rand;
    }

    public int getOffsetTicks()
    {
        return this.ticksExisted + OFFSET_TICK_MULTIPLIER * this.getEntityId();
    }

    @Override
    public boolean isActiveItemStackBlocking()
    {
        return getActiveItemStack().getItem() instanceof ShieldItem;
    }

    /**
     * Check if recently hit.
     *
     * @return the count of how often.
     */
    public int getRecentlyHit()
    {
        return recentlyHit;
    }

    /**
     * Entities treat being on ladders as not on ground; this breaks navigation
     * logic.
     */
    @Override
    protected void updateFallState(final double y, final boolean onGroundIn, @NotNull final BlockState state, @NotNull final BlockPos pos)
    {
        if (!onGround)
        {
            final int px = MathHelper.floor(posX);
            final int py = (int) posY;
            final int pz = MathHelper.floor(posZ);

            this.onGround =
              CompatibilityUtils.getWorldFromCitizen(this).getBlockState(new BlockPos(px, py, pz)).getBlock().isLadder(world.getBlockState(
                new BlockPos(px, py, pz)), world, new BlockPos(px, py, pz), this);
        }

        super.updateFallState(y, onGroundIn, state, pos);
    }

    /**
     * Update the armswing progress.
     */
    public void updateArmSwingProg()
    {
        this.updateArmSwingProgress();
    }

    /**
     * Check if can drop loot.
     *
     * @return true if so.
     */
    public boolean checkCanDropLoot()
    {
        return canDropLoot();
    }

    /**
     * Get the ILocation of the citizen.
     *
     * @return an ILocation object which contains the dimension and is unique.
     */
    public abstract ILocation getLocation();

    /**
     * Checks if a worker is at his working site.
     * If he isn't, sets it's path to the location
     *
     * @param site  the place where he should walk to
     * @param range Range to check in
     * @return True if worker is at site, otherwise false.
     */
    public abstract boolean isWorkerAtSiteWithMove(@NotNull BlockPos site, int range);

    /**
     * Getter for the citizendata.
     * Tries to get it from the colony is the data is null.
     *
     * @return the data.
     */
    @Nullable
    public abstract ICitizenData getCitizenData();

    /**
     * Setter for the citizen data.
     *
     * @param data the data to set.
     */
    public abstract void setCitizenData(@Nullable ICitizenData data);

    /**
     * Applies healthmodifiers for Guards based on level
     */
    public abstract void increaseHPForGuards();

    /**
     * Remove all healthmodifiers from a citizen
     */
    public abstract void removeAllHealthModifiers();

    /**
     * Remove healthmodifier by name.
     *
     * @param modifierName Name of the modifier to remove, see e.g. GUARD_HEALTH_MOD_LEVEL_NAME
     */
    public abstract void removeHealthModifier(String modifierName);

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
     * Mark the citizen dirty to synch the data with the client.
     */
    public abstract void markDirty();

    @NotNull
    public abstract DesiredActivity getDesiredActivity();

    /**
     * Sets the size of the citizen entity
     *
     * @param width  Width
     * @param height Height
     */
    public abstract void setCitizensize(@NotNull float width, @NotNull float height);

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
     * Getter for the citizen id.
     *
     * @return the id.
     */
    public abstract int getCitizenId();

    /**
     * Setter for the citizen id.
     *
     * @param id the id to set.
     */
    public abstract void setCitizenId(int id);

    /**
     * Getter for the current position.
     * Only approximated position, used for stuck checking.
     *
     * @return the current position.
     */
    public abstract BlockPos getCurrentPosition();

    /**
     * Setter for the current position.
     *
     * @param currentPosition the position to set.
     */
    public abstract void setCurrentPosition(BlockPos currentPosition);

    /**
     * Spawn eating particles for the citizen.
     */
    public abstract void spawnEatingParticle();

    /**
     * The Handler for all experience related methods.
     *
     * @return the instance of the handler.
     */
    public abstract ICitizenExperienceHandler getCitizenExperienceHandler();

    /**
     * The Handler for all chat related methods.
     *
     * @return the instance of the handler.
     */
    public abstract ICitizenChatHandler getCitizenChatHandler();

    /**
     * The Handler for all status related methods.
     *
     * @return the instance of the handler.
     */
    public abstract ICitizenStatusHandler getCitizenStatusHandler();

    /**
     * The Handler for all item related methods.
     *
     * @return the instance of the handler.
     */
    public abstract ICitizenItemHandler getCitizenItemHandler();

    /**
     * The Handler for all inventory related methods.
     *
     * @return the instance of the handler.
     */
    public abstract ICitizenInventoryHandler getCitizenInventoryHandler();

    /**
     * The Handler for all colony related methods.
     *
     * @return the instance of the handler.
     */
    public abstract ICitizenColonyHandler getCitizenColonyHandler();

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

    /**
     * The Handler to check if a citizen is stuck.
     *
     * @return the instance of the handler.
     */
    public abstract ICitizenStuckHandler getCitizenStuckHandler();

    /**
     * Check if the citizen can eat now by considering the state and the job tasks.
     *
     * @return true if so.
     */
    public abstract boolean isOkayToEat();

    /**
     * Check if the citizen can be fed.
     *
     * @return true if so.
     */
    public abstract boolean shouldBeFed();

    /**
     * Check if the citizen is just idling at their job and can eat now.
     *
     * @return true if so.
     */
    public abstract boolean isIdlingAtJob();

    /**
     * Returns a value that indicate if the citizen is in mourning.
     *
     * @return indicate if the citizen is mouring
     */
    public abstract boolean isMourning();

    /**
     * Call this to set if the citizen should mourn or not.
     *
     * @param mourning indicate if the citizen should mourn
     */
    public abstract void setMourning(boolean mourning);

    public abstract float getRotationYaw();

    public abstract float getRotationPitch();

    public abstract boolean isDead();








}
