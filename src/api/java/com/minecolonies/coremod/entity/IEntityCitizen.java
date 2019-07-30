package com.minecolonies.coremod.entity;

import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.entity.ai.DesiredActivity;
import com.minecolonies.api.entity.ai.pathfinding.IWalkToProxy;
import com.minecolonies.coremod.colony.ICitizenData;
import com.minecolonies.coremod.entity.citizenhandlers.*;
import com.minecolonies.coremod.entity.pathfinding.AbstractAdvancedPathNavigate;
import com.minecolonies.coremod.inventory.InventoryCitizen;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.ai.EntityLookHelper;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

import static com.minecolonies.api.util.constant.Suppression.UNCHECKED;

public interface IEntityCitizen extends ICommandSender, ICapabilitySerializable<NBTTagCompound>, IBaseEntityCitizen
{
    /**
     * Set the metadata for rendering.
     *
     * @param metadata the metadata required.
     */
    void setRenderMetadata(String metadata);

    /**
     * Get the ILocation of the citizen.
     *
     * @return an ILocation object which contains the dimension and is unique.
     */
    ILocation getLocation();

    /**
     * Checks if a worker is at his working site.
     * If he isn't, sets it's path to the location
     *
     * @param site  the place where he should walk to
     * @param range Range to check in
     * @return True if worker is at site, otherwise false.
     */
    boolean isWorkerAtSiteWithMove(@NotNull BlockPos site, int range);

    Team getTeam();

    boolean attackEntityFrom(@NotNull DamageSource damageSource, float damage);

    @SuppressWarnings(UNCHECKED)
    @Override
    <T> T getCapability(@NotNull Capability<T> capability, EnumFacing facing);

    @Override
    boolean hasCapability(@NotNull Capability<?> capability, EnumFacing facing);

    /**
     * Called when the mob's health reaches 0.
     *
     * @param damageSource the attacking entity.
     */
    void onDeath(@NotNull DamageSource damageSource);

    /**
     * Getter for the citizendata.
     * Tries to get it from the colony is the data is null.
     *
     * @return the data.
     */
    @Nullable
    ICitizenData getCitizenData();

    /**
     * Called when a player tries to interact with a citizen.
     *
     * @param player which interacts with the citizen.
     * @return If citizen should interact or not.
     */
    boolean processInteract(EntityPlayer player, @NotNull EnumHand hand);

    void entityInit();

    void writeEntityToNBT(NBTTagCompound compound);

    void readEntityFromNBT(NBTTagCompound compound);

    /**
     * Called frequently so the entity can update its state every tick as
     * required. For example, zombies and skeletons. use this to react to
     * sunlight and start to burn.
     */
    void onLivingUpdate();

    void setCustomNameTag(@NotNull String name);

    /**
     * Applies healthmodifiers for Guards based on level
     */
    void increaseHPForGuards();

    /**
     * Remove all healthmodifiers from a citizen
     */
    void removeAllHealthModifiers();

    /**
     * Remove healthmodifier by name.
     *
     * @param modifierName Name of the modifier to remove, see e.g. GUARD_HEALTH_MOD_LEVEL_NAME
     */
    void removeHealthModifier(String modifierName);

    @NotNull
    AbstractAdvancedPathNavigate getNavigator();

    /**
     * Return this citizens inventory.
     *
     * @return the inventory this citizen has.
     */
    @NotNull
    InventoryCitizen getInventoryCitizen();

    @NotNull
    IItemHandler getItemHandlerCitizen();

    /**
     * Returns the home position of each citizen (His house or town hall).
     *
     * @return location
     */
    @NotNull
    BlockPos getHomePosition();

    /**
     * Mark the citizen dirty to synch the data with the client.
     */
    void markDirty();

    @NotNull
    DesiredActivity getDesiredActivity();

    /**
     * Sets the size of the citizen entity
     *
     * @param width  Width
     * @param height Height
     */
    void setCitizensize(@NotNull float width, @NotNull float height);

    boolean isChild();

    /**
     * Sets whether this entity is a child
     *
     * @param isChild boolean
     */
    void setIsChild(boolean isChild);

    /**
     * Play move away sound when running from an entity.
     */
    void playMoveAwaySound();

    /**
     * Get the path proxy of the citizen.
     *
     * @return the proxy.
     */
    IWalkToProxy getProxy();

    /**
     * Decrease the saturation of the citizen for 1 action.
     */
    void decreaseSaturationForAction();

    /**
     * Decrease the saturation of the citizen for 1 action.
     */
    void decreaseSaturationForContinuousAction();

    @Override
    boolean equals(Object obj);

    @Override
    int hashCode();

    /**
     * Getter for the citizen id.
     *
     * @return the id.
     */
    int getCitizenId();

    /**
     * Setter for the citizen id.
     *
     * @param id the id to set.
     */
    void setCitizenId(int id);

    /**
     * Setter for the citizen data.
     *
     * @param data the data to set.
     */
    void setCitizenData(@Nullable ICitizenData data);

    /**
     * Getter for the current position.
     * Only approximated position, used for stuck checking.
     *
     * @return the current position.
     */
    BlockPos getCurrentPosition();

    /**
     * Setter for the current position.
     *
     * @param currentPosition the position to set.
     */
    void setCurrentPosition(BlockPos currentPosition);

    /**
     * Spawn eating particles for the citizen.
     */
    void spawnEatingParticle();

    /**
     * The Handler for all experience related methods.
     *
     * @return the instance of the handler.
     */
    ICitizenExperienceHandler getCitizenExperienceHandler();

    /**
     * The Handler for all chat related methods.
     *
     * @return the instance of the handler.
     */
    ICitizenChatHandler getCitizenChatHandler();

    /**
     * The Handler for all status related methods.
     *
     * @return the instance of the handler.
     */
    ICitizenStatusHandler getCitizenStatusHandler();

    /**
     * The Handler for all item related methods.
     *
     * @return the instance of the handler.
     */
    ICitizenItemHandler getCitizenItemHandler();

    /**
     * The Handler for all inventory related methods.
     *
     * @return the instance of the handler.
     */
    ICitizenInventoryHandler getCitizenInventoryHandler();

    /**
     * The Handler for all colony related methods.
     *
     * @return the instance of the handler.
     */
    ICitizenColonyHandler getCitizenColonyHandler();

    /**
     * The Handler for all job related methods.
     *
     * @return the instance of the handler.
     */
    ICitizenJobHandler getCitizenJobHandler();

    /**
     * The Handler for all job related methods.
     *
     * @return the instance of the handler.
     */
    ICitizenSleepHandler getCitizenSleepHandler();

    /**
     * The Handler to check if a citizen is stuck.
     *
     * @return the instance of the handler.
     */
    ICitizenStuckHandler getCitizenStuckHandler();

    /**
     * Check if the citizen can eat now by considering the state and the job tasks.
     *
     * @return true if so.
     */
    boolean isOkayToEat();

    /**
     * Check if the citizen can be fed.
     * @return true if so.
     */
    boolean shouldBeFed();

    /**
     * Check if the citizen is just idling at their job and can eat now.
     *
     * @return true if so.
     */
    boolean isIdlingAtJob();

    /**
     * Call this to set if the citizen should mourn or not.
     *
     * @param mourning indicate if the citizen should mourn
     */
    void setMourning(boolean mourning);

    /**
     * Returns a value that indicate if the citizen is in mourning.
     *
     * @return indicate if the citizen is mouring
     */
    boolean isMourning();

    IAttributeInstance getEntityAttribute(IAttribute attribute);

    void dismountRidingEntity();

    void setLocationAndAngles(double x, double y, double z, float yaw, float pitch);

    float getRotationYaw();

    float getRotationPitch();

    void setCanPickUpLoot(boolean pickUpLoot);

    void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack);

    boolean isDead();

    void setDead();

    int getEntityId();

    Random getRNG();

    int getGrowingAge();

    ItemStack getHeldItemMainhand();

    ItemStack getHeldItemOffhand();

    void setHeldItem(EnumHand hand, ItemStack stack);

    AxisAlignedBB getEntityBoundingBox();

    void addPotionEffect(PotionEffect effect);

    EntityMoveHelper getMoveHelper();

    double getDistanceSq(double posX, double posY, double posZ);

    EntityLivingBase getRevengeTarget();

    EntityLivingBase getLastAttackedEntity();

    void setLastAttackedEntity(Entity lastAttackedEntity);

    boolean canEntityBeSeen(Entity entity);

    void swingArm(EnumHand handToSwing);

    void setActiveHand(EnumHand offHand);

    void faceEntity(Entity target, float turnAround, float turnAround1);

    EntityLookHelper getLookHelper();

    void playSound(SoundEvent entityPlayerAttackSweep, float basicVolume, float randomPitch);

    ItemStack getHeldItem(EnumHand hand);

    float getEyeHeight();

    void move(MoverType self, double x, double y, double z);

    boolean isHandActive();

    EnumHand getActiveHand();
}
