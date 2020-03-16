package com.minecolonies.api.colony;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingWorker;
import com.minecolonies.api.colony.interactionhandling.IInteractionResponseHandler;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenHappinessHandler;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenSkillHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface ICitizenData extends ICitizen, INBTSerializable<CompoundNBT>
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
     * Initializes a new citizen, when not read from nbt
     */
    void initForNewCitizen();

    /**
     * Initializes the entities values from citizen data.
     */
    void initEntityValues();

    /**
     * Sets wether this citizen is female.
     *
     * @param isFemale true if female
     */
    void setIsFemale(@NotNull boolean isFemale);

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
    void serializeViewNetworkData(@NotNull PacketBuffer buf);

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
     * Get the citizen skill handler.
     * @return the handler.
     */
    ICitizenSkillHandler getCitizenSkillHandler();

    /**
     * Schedule restart and cleanup.
     */
    void scheduleRestart(ServerPlayerEntity player);

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
     * Trigger the response on the server side.
     * @param key the key of the component.
     * @param response the triggered response.
     * @param world the world it was triggered in.
     */
    void onResponseTriggered(@NotNull final ITextComponent key, @NotNull final ITextComponent response, final World world);

    /**
     * Tick the citizen data to update values.
     */
    void tick();

    /**
     * Trigger a possible interaction.
     * @param handler the new handler.
     */
    void triggerInteraction(@NotNull final IInteractionResponseHandler handler);

    int getJobModifier();
}
