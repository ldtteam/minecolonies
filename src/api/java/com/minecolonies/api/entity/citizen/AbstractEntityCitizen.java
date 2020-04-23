package com.minecolonies.api.entity.citizen;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.client.render.modeltype.IModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.entity.ai.DesiredActivity;
import com.minecolonies.api.entity.ai.pathfinding.IWalkToProxy;
import com.minecolonies.api.entity.citizen.citizenhandlers.*;
import com.minecolonies.api.entity.pathfinding.AbstractAdvancedPathNavigate;
import com.minecolonies.api.entity.pathfinding.registry.IPathNavigateRegistry;
import com.minecolonies.api.inventory.InventoryCitizen;
import com.minecolonies.api.sounds.EventType;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.block.BlockState;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
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
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

import static com.minecolonies.api.util.constant.CitizenConstants.*;

/**
 * The abstract citizen entity.
 */
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.CouplingBetweenObjects"})
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
    private IModelType modelId = BipedModelType.SETTLER;

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

    private AbstractAdvancedPathNavigate pathNavigate;

    /**
     * Constructor for a new citizen typed entity.
     *
     * @param type the Entity type.
     * @param world the world.
     */
    public AbstractEntityCitizen(final EntityType<? extends AgeableEntity> type, final World world)
    {
        super(type, world);
    }

    public GoalSelector getTasks()
    {
        return goalSelector;
    }

    public int getTicksExisted()
    {
        return ticksExisted;
    }

    /**
     * We override this method and execute no code to avoid citizens travelling to the nether.
     *
     * @param dimensionIn dimension to travel to.
     */
    @Override
    public Entity changeDimension(DimensionType dimensionIn, ITeleporter teleporter)
    {
        return null;
    }

    @NotNull
    @Override
    public BlockPos getPosition()
    {
        return new BlockPos(getPosX(), getPosY(), getPosZ());
    }

    @Override
    public boolean canPickUpLoot()
    {
        return true;
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
        if (!(player instanceof ClientPlayerEntity))
        {
            SoundUtils.playSoundAtCitizenWith(CompatibilityUtils.getWorldFromCitizen(this), this.getPosition(), EventType.INTERACTION, this.getCitizenData());
        }

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
     * Sets the textures of all citizens and distinguishes between male and
     * female.
     */
    public void setTexture()
    {
        if (!CompatibilityUtils.getWorldFromCitizen(this).isRemote)
        {
            return;
        }

        final IModelType model = getModelType();

        final String textureBase = "textures/entity/" + model.getTextureBase() + (female ? "female" : "male");
        final int moddedTextureId = (textureId % model.getNumTextures()) + 1;
        texture = new ResourceLocation(Constants.MOD_ID, textureBase + moddedTextureId + renderMetadata + ".png");
    }

    /**
     * Getter of the resource location of the texture.
     *
     * @return location of the texture.
     */
    @NotNull
    public ResourceLocation getTexture()
    {
        if (texture == null)
        {
            setTexture();
        }
        return texture;
    }

    /**
     * Get the model assigned to the citizen.
     *
     * @return the model.
     */
    public IModelType getModelType()
    {
        return modelId;
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
    protected void registerData()
    {
        super.registerData();
        dataManager.register(DATA_TEXTURE, 0);
        dataManager.register(DATA_LEVEL, 0);
        dataManager.register(DATA_IS_FEMALE, 0);
        dataManager.register(DATA_MODEL, BipedModelType.SETTLER.name());
        dataManager.register(DATA_RENDER_METADATA, "");
        dataManager.register(DATA_IS_ASLEEP, false);
        dataManager.register(DATA_IS_CHILD, false);
        dataManager.register(DATA_BED_POS, new BlockPos(0, 0, 0));
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
            this.pathNavigate.getPathingOptions().setEnterDoors(true);
            this.pathNavigate.getPathingOptions().setCanOpenDoors(true);
            this.navigator.getNodeProcessor().setCanOpenDoors(true);
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
    public void setModelId(final IModelType model)
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
        dataManager.set(DATA_TEXTURE, textureId);
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
    protected void registerAttributes()
    {
        super.registerAttributes();

        getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(BASE_MAX_HEALTH);
        getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(BASE_MOVEMENT_SPEED);

        //path finding search range
        getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(BASE_PATHFINDING_RANGE);
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
            final int px = MathHelper.floor(getPosX());
            final int py = (int) getPosY();
            final int pz = MathHelper.floor(getPosZ());

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
     * The Handler to check if the citizen is sick.
     *
     * @return the instance of the handler.
     */
    public abstract ICitizenDiseaseHandler getCitizenDiseaseHandler();

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
