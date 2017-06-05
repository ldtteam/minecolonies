package com.minecolonies.api.entity;

import com.minecolonies.api.client.render.Model;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.inventory.InventoryCitizen;
import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.INpc;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * ------------ Class not Documented ------------
 */
public abstract class Citizen extends EntityAgeable implements ICommandSender, ICapabilitySerializable<NBTTagCompound>, INpc
{
    public Citizen(final World worldIn)
    {
        super(worldIn);
    }

    @Nullable
    public abstract IJob getColonyJob();

    /**
     * Defines job changes and state changes of the citizen.
     *
     * @param job the set job.
     */
    public abstract void onJobChanged(@Nullable IJob job);

    public abstract int getLevel();

    public abstract void setRenderMetadata(String metadata);

    /**
     * calculate this workers building.
     *
     * @return the building or null if none present.
     */
    @Nullable
    public abstract IBuilding getWorkBuilding();

    public abstract CitizenStatus getStatus();

    public abstract void setStatus(CitizenStatus status);

    /**
     * On Inventory change, mark the building dirty.
     */
    public abstract void onInventoryChanged();

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
     * Change the citizens Rotation to look at said block.
     *
     * @param block the block he should look at.
     */
    public abstract void faceBlock(@Nullable BlockPos block);

    /**
     * Collect exp orbs around the entity.
     */
    public abstract void gatherXp();

    /**
     * Add experience points to citizen.
     * Increases the citizen level if he has sufficient experience.
     * This will reset the experience.
     *
     * @param xp the amount of points added.
     */
    public abstract void addExperience(double xp);

    /**
     * ExperienceLevel getter.
     *
     * @return citizen ExperienceLevel value.
     */
    public abstract int getExperienceLevel();

    /**
     * Trigger the corresponding death achievement.
     *
     * @param source The damage source.
     * @param job    The job of the citizen.
     */
    public abstract void triggerDeathAchievement(DamageSource source, IJob job);

    @Nullable
    public abstract ICitizenData getCitizenData();

    @Nullable
    public abstract IColony getColony();

    public abstract int getOffsetTicks();

    public abstract Model getModelID();

    /**
     * Server-specific update for the EntityCitizen.
     */
    public abstract void updateColonyServer();

    /**
     * Assigns a citizen to a colony.
     *
     * @param c    the colony.
     * @param data the data of the new citizen.
     */
    public abstract void setColony(@Nullable IColony c, @Nullable ICitizenData data);

    /**
     * Getter for the last job.
     *
     * @return the last job he had.
     */
    @NotNull
    public abstract String getLastJob();

    /**
     * Sets the last job of the citizen.
     *
     * @param jobName the job he last had.
     */
    public abstract void setLastJob(@NotNull String jobName);

    /**
     * Getter of the citizens random object.
     *
     * @return random object.
     */
    public abstract Random getRandom();

    /**
     * Return this citizens inventory.
     *
     * @return the inventory this citizen has.
     */
    @NotNull
    public abstract InventoryCitizen getInventoryCitizen();

    /**
     * Getter of the resource location of the texture.
     *
     * @return location of the texture.
     */
    public abstract ResourceLocation getTexture();

    /**
     * Getter which checks if the citizen is female.
     *
     * @return true if female.
     */
    public abstract boolean isFemale();

    /**
     * Clears the colony of the citizen.
     */
    public abstract void clearColony();

    public abstract boolean isAtHome();

    public abstract boolean isInventoryFull();

    /**
     * Lets the citizen tryToEat to replentish saturation.
     */
    public abstract void tryToEat();

    @NotNull
    public abstract DesiredCitizenActivity getDesiredActivity();

    /**
     * Returns the first slot in the inventory with a specific item.
     *
     * @param targetItem the item.
     * @param itemDamage the damage value
     * @return the slot.
     */
    public abstract int findFirstSlotInInventoryWith(Item targetItem, int itemDamage);

    /**
     * Returns the first slot in the inventory with a specific block.
     *
     * @param block      the block.
     * @param itemDamage the damage value
     * @return the slot.
     */
    public abstract int findFirstSlotInInventoryWith(Block block, int itemDamage);

    /**
     * Returns the amount of a certain block in the inventory.
     *
     * @param block      the block.
     * @param itemDamage the damage value
     * @return the quantity.
     */
    public abstract int getItemCountInInventory(Block block, int itemDamage);

    /**
     * Returns the amount of a certain item in the inventory.
     *
     * @param targetItem the block.
     * @param itemDamage the damage value.
     * @return the quantity.
     */
    public abstract int getItemCountInInventory(Item targetItem, int itemDamage);

    /**
     * Checks if citizen has a certain block in the inventory.
     *
     * @param block      the block.
     * @param itemDamage the damage value
     * @return true if so.
     */
    public abstract boolean hasItemInInventory(Block block, int itemDamage);

    /**
     * Checks if citizen has a certain item in the inventory.
     *
     * @param item       the item.
     * @param itemDamage the damage value
     * @return true if so.
     */
    public abstract boolean hasItemInInventory(Item item, int itemDamage);

    /**
     * Removes the currently held item.
     */
    public abstract void removeHeldItem();

    /**
     * Sets the currently held item.
     *
     * @param slot from the inventory slot.
     */
    public abstract void setHeldItem(int slot);

    /**
     * Swing entity arm, create sound and particle effects.
     * <p>
     * Will not break the block.
     *
     * @param blockPos Block position.
     */
    public abstract void hitBlockWithToolInHand(@Nullable BlockPos blockPos);

    /**
     * Damage the current held item.
     *
     * @param damage amount of damage.
     */
    public abstract void damageItemInHand(int damage);

    /**
     * Swing entity arm, create sound and particle effects.
     * <p>
     * This will break the block (different sound and particles),
     * and damage the tool in the citizens hand.
     *
     * @param blockPos Block position.
     */
    public abstract void breakBlockWithToolInHand(@Nullable BlockPos blockPos);

    /**
     * Sends a localized message from the citizen containing a language string
     * with a key and arguments.
     *
     * @param key  the key to retrieve the string.
     * @param args additional arguments.
     */
    public abstract void sendLocalizedChat(String key, Object... args);

    /**
     * Intelligence getter.
     *
     * @return citizen intelligence value.
     */
    public abstract int getIntelligence();

    /**
     * Charisma getter.
     *
     * @return citizen Charisma value.
     */
    public abstract int getCharisma();

    /**
     * Strength getter.
     *
     * @return citizen Strength value.
     */
    public abstract int getStrength();

    /**
     * Endurance getter.
     *
     * @return citizen Endurance value.
     */
    public abstract int getEndurance();

    /**
     * Dexterity getter.
     *
     * @return citizen Dexterity value.
     */
    public abstract int getDexterity();

    /**
     * Set the skill modifier which defines how fast a citizen levels in a
     * certain skill.
     *
     * @param modifier input modifier.
     */
    public abstract void setSkillModifier(int modifier);

    /**
     * Called when the citizen wakes up.
     */
    public abstract void onWakeUp();

    /**
     * Method to get the world from a Citizen.
     */
    public abstract World getWorld();
}
