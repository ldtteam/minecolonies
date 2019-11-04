package com.minecolonies.api.colony;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingWorker;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenHappinessHandler;
import com.minecolonies.api.inventory.InventoryCitizen;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Random;

public interface ICitizenData extends INBTSerializable<NBTTagCompound>
{
    /**
     * Maximum saturation of a citizen.
     */
    int MAX_SATURATION = 10;

    /**
     * Return the entity instance of the citizen data. Respawn the citizen if
     * needed.
     *
     * @return {@link AbstractEntityCitizen} of the citizen data.
     */
    @NotNull
    Optional<AbstractEntityCitizen> getCitizenEntity();

    /**
     * Sets the entity of the citizen data.
     *
     * @param citizen {@link AbstractEntityCitizen} instance of the citizen data.
     */
    void setCitizenEntity(@Nullable AbstractEntityCitizen citizen);

    /**
     * Marks the instance dirty.
     */
    void markDirty();

    /**
     * Returns the colony of the citizen.
     *
     * @return colony of the citizen.
     */
    IColony getColony();

    /**
     * Returns the id of the citizen.
     *
     * @return id of the citizen.
     */
    int getId();

    /**
     * Initializes a new citizen, when not read from nbt
     */
    void initForNewCitizen();

    /**
     * Returns the name of the citizen.
     *
     * @return name of the citizen.
     */
    String getName();

    /**
     * Returns true if citizen is female, false for male.
     *
     * @return true for female, false for male.
     */
    boolean isFemale();

    /**
     * Sets wether this citizen is female.
     *
     * @param isFemale true if female
     */
    void setIsFemale(@NotNull boolean isFemale);

    /**
     * Check if the citizen is paused.
     */
    void setPaused(boolean p);

    /**
     * Check if the citizen is paused.
     *
     * @return true for paused, false for working.
     */
    boolean isPaused();

    /**
     * Returns the texture id for the citizen.
     *
     * @return texture ID.
     */
    int getTextureId();

    /**
     * Returns whether or not the instance is dirty.
     *
     * @return true when dirty, otherwise false.
     */
    boolean isDirty();

    /**
     * Markt the instance not dirty.
     */
    void clearDirty();

    /**
     * When a building is destroyed, inform the citizen so it can do any cleanup
     * of associations that the building's. own IBuilding.onDestroyed did
     * not do.
     *
     * @param building building that is destroyed.
     */
    void onRemoveBuilding(IBuilding building);

    /**
     * Returns the home building of the citizen.
     *
     * @return home building.
     */
    @Nullable
    IBuilding getHomeBuilding();

    /**
     * Sets the home of the citizen.
     *
     * @param building home building.
     */
    void setHomeBuilding(@Nullable IBuilding building);

    /**
     * Returns the work building of a citizen.
     *
     * @return home building of a citizen.
     */
    @Nullable
    IBuildingWorker getWorkBuilding();

    /**
     * Sets the work building of a citizen.
     *
     * @param building work building.
     */
    void setWorkBuilding(@Nullable IBuildingWorker building);

    /**
     * Updates {@link AbstractEntityCitizen} for the instance.
     */
    void updateCitizenEntityIfNecessary();

    /**
     * Returns the job of the citizen.
     *
     * @return Job of the citizen.
     */
    IJob getJob();

    /**
     * Sets the job of this citizen.
     *
     * @param job Job of the citizen.
     */
    void setJob(IJob job);

    /**
     * Returns the job subclass needed. Returns null on type mismatch.
     *
     * @param type the type of job wanted.
     * @param <J>  The job type returned.
     * @return the job this citizen has.
     */
    @Nullable
    <J extends IJob> J getJob(@NotNull Class<J> type);

    /**
     * Writes the citizen data to a byte buf for transition.
     *
     * @param buf Buffer to write to.
     */
    void serializeViewNetworkData(@NotNull ByteBuf buf);

    /**
     * Returns the levels of the citizen.
     *
     * @return levels of the citizen.
     */
    int getLevel();

    /**
     * Sets the levels of the citizen.
     *
     * @param lvl the new levels for the citizen.
     */
    void setLevel(int lvl);

    /**
     * Adds experiences of the citizen.
     *
     * @param xp the amount of xp to add.
     */
    void addExperience(double xp);

    /**
     * Levelup actions for the citizen, increases levels and notifies the Citizen's Job
     */
    void levelUp();

    /**
     * Returns the default chance to levelup
     */
    int getChanceToLevel();

    /**
     * Getter for the saturation.
     *
     * @param extraSaturation the extra saturation
     */
    void increaseSaturation(double extraSaturation);

    /**
     * Getter for the saturation.
     *
     * @param extraSaturation the saturation to remove.
     */
    void decreaseSaturation(double extraSaturation);

    /**
     * Set the citizen name.
     *
     * @param name the name to set.
     */
    void setName(String name);

    /**
     * Returns the experiences of the citizen.
     *
     * @return experiences of the citizen.
     */
    double getExperience();

    /**
     * Strength getter.
     *
     * @return citizen Strength value.
     */
    int getStrength();

    /**
     * Endurance getter.
     *
     * @return citizen Endurance value.
     */
    int getEndurance();

    /**
     * Charisma getter.
     *
     * @return citizen Charisma value.
     */
    int getCharisma();

    /**
     * Intelligence getter.
     *
     * @return citizen Intelligence value.
     */
    int getIntelligence();

    /**
     * Dexterity getter.
     *
     * @return citizen Dexterity value.
     */
    int getDexterity();

    /**
     * Set the last position of the citizen.
     *
     * @param lastPosition the last position.
     */
    void setLastPosition(BlockPos lastPosition);

    /**
     * Get the last position of the citizen.
     *
     * @return the last position.
     */
    BlockPos getLastPosition();

    /**
     * Getter for the saturation.
     *
     * @return the saturation.
     */
    double getSaturation();

    /**
     * Getter for the inventory.
     *
     * @return the direct reference to the citizen inventory.
     */
    InventoryCitizen getInventory();

    /**
     * Check if citizen is asleep.
     *
     * @return true if so.
     */
    boolean isAsleep();

    /**
     * Getter for the bedPos.
     *
     * @return the bedPos.
     */
    BlockPos getBedPos();

    /**
     * Set asleep.
     *
     * @param asleep true if asleep.
     */
    void setAsleep(boolean asleep);

    /**
     * Set the bed pos.
     *
     * @param bedPos the pos to set.
     */
    void setBedPos(BlockPos bedPos);

    /**
     * Create a blocking request.
     *
     * @param requested the request to create.
     * @param <R>       the Type
     * @return the token of the request.
     */
    <R extends IRequestable> IToken createRequest(@NotNull R requested);

    /**
     * Create an async request.
     *
     * @param requested the request to create.
     * @param <R>       the Type
     * @return the token of the request.
     */
    <R extends IRequestable> IToken createRequestAsync(@NotNull R requested);

    /**
     * Called on request canceled.
     *
     * @param token the token to be canceled.
     */
    void onRequestCancelled(@NotNull IToken token);

    /**
     * Check if a request is async.
     *
     * @param token the token to check.
     * @return true if it is.
     */
    boolean isRequestAsync(@NotNull IToken token);

    /**
     * The Handler for the citizens happiness.
     *
     * @return the instance of the handler
     */
    ICitizenHappinessHandler getCitizenHappinessHandler();

    /**
     * Try a random levels up.
     */
    void tryRandomLevelUp(Random random);

    /**
     * Try a random levels up.
     *
     * @param customChance set to 0 to not use, chance for levelup is 1/customChance
     */
    void tryRandomLevelUp(Random random, int customChance);

    /**
     * Schedule restart and cleanup.
     */
    void scheduleRestart(EntityPlayerMP player);

    /**
     * AI will be restarted, also restart building etc
     */
    boolean shouldRestart();

    /**
     * Restart done successfully
     */
    void restartDone();

    /**
     * Set the child flag.
     *
     * @param isChild boolean
     */
    void setIsChild(boolean isChild);

    /**
     * Is this citizen a child?
     *
     * @return boolean
     */
    boolean isChild();

    /**
     * Set the strength of the citizen
     *
     * @param strength value to set
     */
    void setStrength(@NotNull int strength);

    /**
     * Set the endurance of the citizen
     *
     * @param endurance value to set
     */
    void setEndurance(@NotNull int endurance);

    /**
     * Set the charisma of the citizen
     *
     * @param charisma value to set
     */
    void setCharisma(@NotNull int charisma);

    /**
     * Set the intelligence of the citizen
     *
     * @param intelligence value to set
     */
    void setIntelligence(@NotNull int intelligence);

    /**
     * Set the dexterity of the citizen
     *
     * @param dexterity value to set
     */
    void setDexterity(@NotNull int dexterity);

    /**
     * Get the max health
     */
    double getMaxHealth();

    /**
     * Get the current healh
     */
    double getHealth();

    /**
     * Check if the citizen just ate.
     * @return true if so.
     */
    boolean justAte();

    /**
     * Set or reset if the citizen just ate.
     * @param justAte true if justAte, false to reset.
     */
    void setJustAte(boolean justAte);

    /**
     * Drain experience from the worker.
     * @param maxDrain the max to drain.
     * @return the drained amount including a configured draining bonus.
     */
    double drainExperience(int maxDrain);

    /**
     * Directly spend a cetain number of experiment levels.
     * @param levels the levels to spend.
     */
    void spendLevels(int levels);
}
