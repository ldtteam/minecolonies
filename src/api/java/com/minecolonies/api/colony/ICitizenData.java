package com.minecolonies.api.colony;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingWorker;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenHappinessHandler;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenSkillHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Random;

public interface ICitizenData extends ICivilianData
{
    /**
     * Maximum saturation of a citizen.
     */
    int MAX_SATURATION = 20;

    /**
     * When a building is destroyed, inform the citizen so it can do any cleanup of associations that the building's. own IBuilding.onDestroyed did not do.
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
     * Returns the job of the citizen.
     *
     * @return Job of the citizen.
     */
    IJob<?> getJob();

    /**
     * Sets the job of this citizen.
     *
     * @param job Job of the citizen.
     */
    void setJob(IJob<?> job);

    /**
     * Returns the job subclass needed. Returns null on type mismatch.
     *
     * @param type the type of job wanted.
     * @param <J>  The job type returned.
     * @return the job this citizen has.
     */
    @Nullable
    <J extends IJob<?>> J getJob(@NotNull Class<J> type);

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
     * Sets the citizens current saturation.
     *
     * @param saturation to set
     */
    void setSaturation(double saturation);

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
     * The Handler for the citizens happiness.
     *
     * @return the instance of the handler
     */
    ICitizenHappinessHandler getCitizenHappinessHandler();

    /**
     * Get the citizen skill handler.
     *
     * @return the handler.
     */
    ICitizenSkillHandler getCitizenSkillHandler();

    /**
     * Schedule restart and cleanup.
     *
     * @param player the player scheduling it.
     */
    void scheduleRestart(ServerPlayerEntity player);

    /**
     * AI will be restarted, also restart building etc
     *
     * @return true if so.
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
     * Check if the citizen just ate.
     *
     * @return true if so.
     */
    boolean justAte();

    /**
     * Set or reset if the citizen just ate.
     *
     * @param justAte true if justAte, false to reset.
     */
    void setJustAte(boolean justAte);

    /**
     * Get the clean job modifier. Primary skill + secondary divided by 4.
     *
     * @return the int modifier.
     */
    int getJobModifier();

    /**
     * If is idle at job.
     *
     * @return true if so.
     */
    boolean isIdleAtJob();

    /**
     * Set idle at job.
     *
     * @param idle true if so.
     */
    void setIdleAtJob(final boolean idle);

    /**
     * Gets the entity
     * @return
     */
    @Override
    Optional<AbstractEntityCitizen> getEntity();

    /**
     * Gets the citizen's status
     *
     * @return status
     */
    VisibleCitizenStatus getStatus();

    /**
     * Sets the citizens status
     *
     * @param status status to set
     */
    void setVisibleStatus(VisibleCitizenStatus status);

    /**
     * Get the random var of the citizen.
     * @return the random.
     */
    Random getRandom();

    /**
     * Applies the effects of research to the data's entity
     */
    void applyResearchEffects();

    /**
     * Triggered when the citizen is going to sleep
     */
    void onGoSleep();

    /**
     * Sets the next position to respawn at
     *
     * @param pos position to set
     */
    void setNextRespawnPosition(final BlockPos pos);
}
