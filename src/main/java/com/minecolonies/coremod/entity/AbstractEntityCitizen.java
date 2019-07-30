package com.minecolonies.coremod.entity;

import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.render.BipedModelType;
import com.minecolonies.coremod.util.SoundUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemShield;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

import static com.minecolonies.api.util.constant.CitizenConstants.*;
import static com.minecolonies.api.util.constant.Constants.ONE_HUNDRED_PERCENT;

/**
 * The abstract citizen entity.
 */
public abstract class AbstractEntityCitizen extends EntityAgeable implements IBaseEntityCitizen
{

    public static final DataParameter<Integer> DATA_LEVEL     = EntityDataManager.createKey(AbstractEntityCitizen.class, DataSerializers.VARINT);
    public static final DataParameter<Integer>     DATA_TEXTURE   = EntityDataManager.createKey(AbstractEntityCitizen.class, DataSerializers.VARINT);
    public static final DataParameter<Integer>     DATA_IS_FEMALE = EntityDataManager.createKey(AbstractEntityCitizen.class, DataSerializers.VARINT);
    public static final DataParameter<Integer>     DATA_COLONY_ID = EntityDataManager.createKey(AbstractEntityCitizen.class, DataSerializers.VARINT);
    public static final DataParameter<Integer>     DATA_CITIZEN_ID = EntityDataManager.createKey(AbstractEntityCitizen.class, DataSerializers.VARINT);
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

    /**
     * Constructor for a new citizen typed entity.
     *
     * @param world the world.
     */
    public AbstractEntityCitizen(final World world)
    {
        super(world);
    }

    @Override
    public EntityAITasks getTasks()
    {
        return tasks;
    }

    @Override
    public int getTicksExisted()
    {
        return ticksExisted;
    }

    /**
     * We override this method and execute no code to avoid citizens travelling
     * to the nether.
     *
     * @param dimensionIn dimension to travel to.
     */
    @Override
    @Nullable
    public Entity changeDimension(final int dimensionIn)
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
    public void setCanCaptureDrops(final boolean b)
    {
        this.captureDrops = b;
    }

    @Override
    public float getPreviousRotationPitch()
    {
        return prevRotationPitch;
    }

    @Override
    public float getPreviousRotationYaw()
    {
        return prevRotationYaw;
    }

    @Override
    public float getPreviousRenderYawOffset()
    {
        return prevRenderYawOffset;
    }

    @Override
    public float getRenderYawOffset()
    {
        return renderYawOffset;
    }

    @Override
    public double getPreviousPosX()
    {
        return prevPosX;
    }

    @Override
    public double getPreviousPosY()
    {
        return prevPosY;
    }

    @Override
    public double getPreviousPosZ()
    {
        return prevPosZ;
    }

    @NotNull
    @Override
    public EnumActionResult applyPlayerInteraction(final EntityPlayer player, final Vec3d vec, final EnumHand hand)
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
     * @return the model.
     */
    @Override
    public BipedModelType getModelID()
    {
        return modelId;
    }

    /**
     * Getter of the resource location of the texture.
     *
     * @return location of the texture.
     */
    @Override
    public ResourceLocation getTexture()
    {
        return texture;
    }

    /**
     * Sets the textures of all citizens and distinguishes between male and
     * female.
     */
    @Override
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
    @Override
    public EntityAgeable createChild(@NotNull final EntityAgeable child)
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

    @Override
    public double getPosX()
    {
        return posX;
    }

    @Override
    public double getPosY()
    {
        return posY;
    }

    @Override
    public double getPosZ()
    {
        return posZ;
    }

    /**
     * Getter which checks if the citizen is female.
     *
     * @return true if female.
     */
    @Override
    public boolean isFemale()
    {
        return female;
    }

    /**
     * Set the gender.
     * @param female true if female, false if male.
     */
    @Override
    public void setFemale(final boolean female)
    {
        this.female = female;
    }

    /**
     * Set the texture id.
     * @param textureId the id of the texture.
     */
    @Override
    public void setTextureId(final int textureId)
    {
        this.textureId = textureId;
    }

    /**
     * Set the render meta data.
     * @param renderMetadata the metadata to set.
     */
    @Override
    public void setRenderMetadata(final String renderMetadata)
    {
        this.renderMetadata = renderMetadata;
    }

    /**
     * Getter for the render metadata.
     * @return the meta data.
     */
    @Override
    public String getRenderMetadata()
    {
        return renderMetadata;
    }

    @Override
    protected void updateEquipmentIfNeeded(final EntityItem itemEntity)
    {
        //Just do nothing!
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
    @Override
    public Random getRandom()
    {
        return rand;
    }

    @Override
    public int getOffsetTicks()
    {
        return this.ticksExisted + OFFSET_TICK_MULTIPLIER * this.getEntityId();
    }

    @Override
    public boolean isActiveItemStackBlocking()
    {
        return getActiveItemStack().getItem() instanceof ItemShield;
    }

    /**
     * Entities treat being on ladders as not on ground; this breaks navigation
     * logic.
     */
    @Override
    protected void updateFallState(final double y, final boolean onGroundIn, @NotNull final IBlockState state, @NotNull final BlockPos pos)
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
     * Set the rotation of the citizen.
     * @param yaw the rotation yaw.
     * @param pitch the rotation pitch.
     */
    @Override
    public void setOwnRotation(final float yaw, final float pitch)
    {
        this.setRotation(yaw, pitch);
    }

    /**
     * Set the model id.
     * @param model the model.
     */
    @Override
    public void setModelId(final BipedModelType model)
    {
        this.modelId = model;
    }

    /**
     * Update the armswing progress.
     */
    @Override
    public void updateArmSwingProg()
    {
        this.updateArmSwingProgress();
    }

    /**
     * Getter for the texture id.
     * @return the texture id.
     */
    @Override
    public int getTextureId()
    {
        return this.textureId;
    }

    /**
     * Check if recently hit.
     * @return the count of how often.
     */
    @Override
    public int getRecentlyHit()
    {
        return recentlyHit;
    }

    /**
     * Check if can drop loot.
     * @return true if so.
     */
    @Override
    public boolean checkCanDropLoot()
    {
        return canDropLoot();
    }
}
