package com.minecolonies.coremod.entity;

import com.minecolonies.coremod.client.render.BipedModelType;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.INpc;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntitySenses;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.UUID;

public interface IBaseEntityCitizen extends ICommandSender, ICapabilitySerializable<NBTTagCompound>, INpc
{

    int getTicksExisted();

    EntityAITasks getTasks();

    UUID getUniqueID();

    EntityDataManager getDataManager();

    /**
     * We override this method and execute no code to avoid citizens travelling
     * to the nether.
     *
     * @param dimensionIn dimension to travel to.
     */
    @Nullable
    Entity changeDimension(int dimensionIn);

    @NotNull
    @Override
    BlockPos getPosition();

    @NotNull
    EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand);

    /**
     * Returns false if the newer Entity AI code should be run.
     */
    boolean isAIDisabled();

    /**
     * Get the model assigned to the citizen.
     * @return the model.
     */
    BipedModelType getModelID();

    /**
     * Getter of the resource location of the texture.
     *
     * @return location of the texture.
     */
    ResourceLocation getTexture();

    /**
     * Sets the textures of all citizens and distinguishes between male and
     * female.
     */
    void setTexture();

    /**
     * For the time being we don't want any childrens of our colonists.
     *
     * @param child the ageable entity.
     * @return the child.
     */
    EntityAgeable createChild(@NotNull EntityAgeable child);

    void entityInit();

    /**
     * Getter which checks if the citizen is female.
     *
     * @return true if female.
     */
    boolean isFemale();

    /**
     * Set the gender.
     * @param female true if female, false if male.
     */
    void setFemale(boolean female);

    /**
     * Set the texture id.
     * @param textureId the id of the texture.
     */
    void setTextureId(int textureId);

    /**
     * Set the render meta data.
     * @param renderMetadata the metadata to set.
     */
    void setRenderMetadata(String renderMetadata);

    /**
     * Getter for the render metadata.
     * @return the meta data.
     */
    String getRenderMetadata();

    /**
     * Getter of the citizens random object.
     *
     * @return random object.
     */
    Random getRandom();

    int getOffsetTicks();

    boolean isActiveItemStackBlocking();

    /**
     * Set the rotation of the citizen.
     * @param yaw the rotation yaw.
     * @param pitch the rotation pitch.
     */
    void setOwnRotation(float yaw, float pitch);

    /**
     * Set the model id.
     * @param model the model.
     */
    void setModelId(BipedModelType model);

    /**
     * Update the armswing progress.
     */
    void updateArmSwingProg();

    /**
     * Getter for the texture id.
     * @return the texture id.
     */
    int getTextureId();

    /**
     * Check if recently hit.
     * @return the count of how often.
     */
    int getRecentlyHit();

    /**
     * Check if can drop loot.
     * @return true if so.
     */
    boolean checkCanDropLoot();

    float getHealth();

    float getMaxHealth();

    double getPosX();

    double getPosY();

    double getPosZ();

    void resetActiveHand();

    float getAIMoveSpeed();

    void setCanCaptureDrops(boolean b);

    double getYOffset();

    float getSwingProgress(float partialTicks);

    float getPreviousRotationPitch();

    float getPreviousRotationYaw();

    float getPreviousRenderYawOffset();

    float getRenderYawOffset();

    double getPreviousPosX();

    double getPreviousPosY();

    double getPreviousPosZ();

    boolean isSneaking();

    EntitySenses getEntitySenses();
}
