package com.minecolonies.api.entity.citizen;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.client.render.modeltype.IModelType;
import com.minecolonies.api.client.render.modeltype.modularcitizen.CitizenRenderContainer;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.entity.ai.DesiredActivity;
import com.minecolonies.api.entity.ai.pathfinding.IWalkToProxy;
import com.minecolonies.api.entity.citizen.citizenhandlers.*;
import com.minecolonies.api.entity.pathfinding.AbstractAdvancedPathNavigate;
import com.minecolonies.api.entity.pathfinding.PathingStuckHandler;
import com.minecolonies.api.entity.pathfinding.registry.IPathNavigateRegistry;
import com.minecolonies.api.inventory.InventoryCitizen;
import com.minecolonies.api.sounds.EventType;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.SoundUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ShieldItem;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.minecolonies.api.util.constant.CitizenConstants.*;

/**
 * The abstract citizen entity.
 */
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.CouplingBetweenObjects"})
public abstract class AbstractEntityCitizen extends AbstractCivilianEntity implements INamedContainerProvider
{
    /**
     * Used texture mapping.
     */
    private Map<String, String> textureMapping = new HashMap<>();

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
    public static final DataParameter<String>   DATA_STYLE           = EntityDataManager.createKey(AbstractEntityCitizen.class, DataSerializers.STRING);
    public static final DataParameter<String>   DATA_TEXTURE_SUFFIX  = EntityDataManager.createKey(AbstractEntityCitizen.class, DataSerializers.STRING);
    public static final DataParameter<Integer>  DATA_SUFFIX_COLOR    = EntityDataManager.createKey(AbstractEntityCitizen.class, DataSerializers.VARINT);
    public static final DataParameter<Integer>  DATA_HAIR_COLOR      = EntityDataManager.createKey(AbstractEntityCitizen.class, DataSerializers.VARINT);
    public static final DataParameter<Integer>  DATA_EYE_COLOR       = EntityDataManager.createKey(AbstractEntityCitizen.class, DataSerializers.VARINT);

    /**
     * The default model.
     */
    private IModelType modelId = BipedModelType.SETTLER;

    /**
     * The texture id.
     */
    private int textureId;

    /**
     * The citizen's render settings.
     */
    public final CitizenRenderContainer citizenRenderSettings;

    /**
     * Additional render data.
     */
    private String renderMetadata = "";

    /**
     * The gender, true if female.
     */
    private boolean female;

    /**
     * When true, update citizenRenderSettings from the colonist metadata on next opportunity.
     */
    private boolean renderSettingsDirty = false;

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
     * Constructor for a new citizen typed entity.
     *
     * @param type  the Entity type.
     * @param world the world.
     */
    public AbstractEntityCitizen(final EntityType<? extends AgeableEntity> type, final World world)
    {
        super(type, world);
        citizenRenderSettings = world.isRemote ? new CitizenRenderContainer() : null;
    }

    /**
     * Get the default attributes with their values.
     * @return the attribute modifier map.
     */
    public static AttributeModifierMap.MutableAttribute getDefaultAttributes()
    {
        return LivingEntity.registerAttributes()
                 .createMutableAttribute(Attributes.MAX_HEALTH, BASE_MAX_HEALTH)
                 .createMutableAttribute(Attributes.MOVEMENT_SPEED, BASE_MOVEMENT_SPEED)
                 .createMutableAttribute(Attributes.FOLLOW_RANGE, BASE_PATHFINDING_RANGE);
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
    public Entity changeDimension(final ServerWorld p_241206_1_)
    {
        return null;
    }

    @NotNull
    public BlockPos getPosition()
    {
        return new BlockPos(getPosX(), getPosY(), getPosZ());
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
    public boolean canPassengerSteer()
    {
        return false;
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
    public ActionResultType applyPlayerInteraction(final PlayerEntity player, final Vector3d vec, final Hand hand)
    {
        if (!player.world.isRemote())
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
     * Sets the textures of all citizens and distinguishes between male and female.
     */
    public void setTexture()
    {
        if (!CompatibilityUtils.getWorldFromCitizen(this).isRemote)
        {
            return;
        }

        // TODO: convert to a more serious tag-based system.
        final String style;
        if(this.getDataManager().get(DATA_STYLE).contains("medieval"))
        {
            style = "medieval";
        }
        else
        {
            // the citizenRenderSettings will automatically convert styles without backing resources into defaults, so don't need to worry about them here.
            style = this.getDataManager().get(DATA_STYLE);
        }
        citizenRenderSettings.updateCitizen(this.getDataManager().get(DATA_HAIR_COLOR), this.getDataManager().get(DATA_EYE_COLOR), this.getDataManager().get(DATA_SUFFIX_COLOR));
        if(this.modelId instanceof BipedModelType)
        {
            if(this.modelId == BipedModelType.STUDENT && rand.nextBoolean())
            {
                this.modelId = BipedModelType.STUDENT_MONK;
            }
            citizenRenderSettings.updateWorker(style, female, (BipedModelType) this.modelId, this.textureId);
        }
        if(getCitizenDiseaseHandler().isSick())
        {
            citizenRenderSettings.updateIllness(style, female, IColonyManager.getInstance().getCompatibilityManager().getDisease(getCitizenDiseaseHandler().getDisease()).getIdentifier());
        }

        renderSettingsDirty = false;
    }

    /**
     * Get the citizen data view.
     *
     * @return the view.
     */
    public abstract ICitizenDataView getCitizenDataView();


    public CitizenRenderContainer getRenderSettings()
    {
        return citizenRenderSettings;
    }

    /**
     * Getter of the resource location of the texture.
     *
     * @return location of the texture.
     */
    @NotNull
    public ResourceLocation getTexture()
    {
        if (renderSettingsDirty && IMinecoloniesAPI.getInstance().getCitizenResourceRegistry().isLoaded())
        {
            setTexture();
        }
        return citizenRenderSettings.textureBase;
    }

    /**
     * Set the texture dirty.
     */
    public void setTextureDirty()
    {
        this.renderSettingsDirty = true;
    }

    /**
     * Set the used texture mapping (from style to actual folder).
     * @param style the colony style.
     * @param used the actual folder.
     */
    public void setTextureMapping(final String style, final String used)
    {
        this.textureMapping.put(style, used);
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
     * @return the child.
     */
    @Nullable
    @Override
    public AgeableEntity func_241840_a(final ServerWorld world, final AgeableEntity parent)
    {
        return null;
    }

    @Override
    protected void registerData()
    {
        super.registerData();
        dataManager.register(DATA_TEXTURE_SUFFIX, "_b");
        dataManager.register(DATA_TEXTURE, 0);
        dataManager.register(DATA_LEVEL, 0);
        dataManager.register(DATA_STYLE, "default");
        dataManager.register(DATA_IS_FEMALE, 0);
        dataManager.register(DATA_MODEL, BipedModelType.SETTLER.name());
        dataManager.register(DATA_RENDER_METADATA, "");
        dataManager.register(DATA_IS_ASLEEP, false);
        dataManager.register(DATA_IS_CHILD, false);
        dataManager.register(DATA_BED_POS, new BlockPos(0, 0, 0));
        dataManager.register(DATA_SUFFIX_COLOR, 0);
        dataManager.register(DATA_EYE_COLOR, 0);
        dataManager.register(DATA_HAIR_COLOR, 0);
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
            this.pathNavigate.setStuckHandler(PathingStuckHandler.createStuckHandler().withTeleportOnFullStuck().withTeleportSteps(5));
        }
        return pathNavigate;
    }

    /**
     * Ignores entity collisions are colliding for a while, solves stuck e.g. for many trying to take the same door
     *
     * @param entityIn entity to collide with
     */
    @Override
    public void applyEntityCollision(@NotNull final Entity entityIn)
    {
        if ((collisionCounter += 2) > COLL_THRESHOLD)
        {
            if (collisionCounter > COLL_THRESHOLD * 2)
            {
                collisionCounter = 0;
            }

            return;
        }
        super.applyEntityCollision(entityIn);
    }

    @Override
    public void livingTick()
    {
        super.livingTick();
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
        this.renderSettingsDirty = true;
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
     * Set the texture ID, which controls colonist variation textures.
     *
     * @param textureId the variant ID for the colonist appearance.
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
     * Entities treat being on ladders as not on ground; this breaks navigation logic.
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
    @Nullable
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

    /**
     * The Handler to check if the citizen is sick.
     *
     * @return the instance of the handler.
     */
    public abstract ICitizenDiseaseHandler getCitizenDiseaseHandler();

    public abstract void setCitizenDiseaseHandler(ICitizenDiseaseHandler citizenDiseaseHandler);

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

    public abstract void setCitizenSleepHandler(ICitizenSleepHandler citizenSleepHandler);

    public abstract void setCitizenJobHandler(ICitizenJobHandler citizenJobHandler);

    public abstract void setCitizenItemHandler(ICitizenItemHandler citizenItemHandler);

    public abstract void setCitizenChatHandler(ICitizenChatHandler citizenChatHandler);

    public abstract void setCitizenExperienceHandler(ICitizenExperienceHandler citizenExperienceHandler);

    /**
     * Get if the citizen is fleeing from an attacker.
     *
     * @return true if so.
     */
    public abstract boolean isCurrentlyFleeing();

    /**
     * Calls a guard for help against an attacker.
     *
     * @param attacker       the attacking entity
     * @param guardHelpRange the squaredistance in which we search for nearby guards
     */
    public abstract void callForHelp(final Entity attacker, final int guardHelpRange);

    /**
     * Sets the fleeing state
     *
     * @param fleeing true if fleeing.
     */
    public abstract void setFleeingState(final boolean fleeing);

    /**
     * Setter for the citizen pose.
     *
     * @param pose the pose to set.
     */
    public void updatePose(final Pose pose)
    {
        setPose(pose);
    }
}
