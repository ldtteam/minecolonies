package com.minecolonies.coremod.entity.ai.mobs;

import com.minecolonies.api.colony.IColony;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.monster.IMob;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.UUID;

public interface IBaseMinecoloniesMob extends ICommandSender, ICapabilitySerializable<NBTTagCompound>, IMob
{
    void applyEntityCollision(@NotNull Entity entityIn);

    void playLivingSound();

    void onLivingUpdate();

    /**
     * Get the stack counter.
     * @return the amount it got stuck already.
     */
    int getStuckCounter();

    /**
     * Set the stack counter.
     * @param stuckCounter the amount.
     */
    void setStuckCounter(int stuckCounter);

    /**
     * Get the ladder counter.
     * @return the amount it got stuck and placed a ladder already.
     */
    int getLadderCounter();

    /**
     * Set the ladder counter.
     * @param ladderCounter the amount.
     */
    void setLadderCounter(int ladderCounter);

    @NotNull
    NBTTagCompound writeToNBT(NBTTagCompound compound);

    void readFromNBT(NBTTagCompound compound);

    @NotNull
    PathNavigate getNavigator();

    @Nullable
    IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata);

    /**
     * Getter for the colony.
     * Gets a value from the ColonyManager if null.
     * @return the colony the barbarian is assigned to attack.e
     */
    IColony getColony();

    void onDeath(@NotNull DamageSource cause);

    /**
     * Set the colony to raid.
     * @param colony the colony to set.
     */
    void setColony(IColony colony);

    boolean isPotionActive(Potion potionIn);

    void addPotionEffect(PotionEffect potioneffectIn);

    UUID getUniqueID();
}
