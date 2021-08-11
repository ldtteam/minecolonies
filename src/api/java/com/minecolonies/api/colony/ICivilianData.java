package com.minecolonies.api.colony;

import com.minecolonies.api.colony.interactionhandling.IInteractionResponseHandler;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.entity.citizen.AbstractCivilianEntity;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Data for all civilians of a colony, can be citizen/trader/visitor etc
 */
public interface ICivilianData extends ICitizen, INBTSerializable<CompoundNBT>
{
    /**
     * Return the entity instance of the civilian data. Respawn the civilian if needed.
     *
     * @return {@link AbstractEntityCitizen} of the civilian data.
     */

    /**
     * Sets the entity of the civilian data.
     *
     * @param civilian {@link AbstractCivilianEntity} instance of the civilian data.
     */
    void setEntity(@Nullable AbstractCivilianEntity civilian);

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
    void initForNewCivilian();

    /**
     * Initializes the entities values from citizen data.
     */
    void initEntityValues();

    /**
     * Sets a gender and generates a new name
     * @param isFemale
     */
    void setGenderAndGenerateName(@NotNull boolean isFemale);

    /**
     * Sets the gender
     *
     * @param isFemale
     */
    void setGender(boolean isFemale);

    /**
     * Returns the texture id for the civilian.
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
     * Updates {@link AbstractCivilianEntity} for the instance.
     */
    void updateEntityIfNecessary();

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
     * Create a blocking request.
     *
     * @param requested the request to create.
     * @param <R>       the Type
     * @return the token of the request.
     */
    <R extends IRequestable> IToken<?> createRequest(@NotNull R requested);

    /**
     * Create an async request.
     *
     * @param requested the request to create.
     * @param <R>       the Type
     * @return the token of the request.
     */
    <R extends IRequestable> IToken<?> createRequestAsync(@NotNull R requested);

    /**
     * Called on request canceled.
     *
     * @param token the token to be canceled.
     */
    void onRequestCancelled(@NotNull IToken<?> token);

    /**
     * Check if a request is async.
     *
     * @param token the token to check.
     * @return true if it is.
     */
    boolean isRequestAsync(@NotNull IToken<?> token);

    /**
     * Trigger the response on the server side.
     *
     * @param key      the key of the component.
     * @param response the triggered response.
     * @param player   the world it was triggered in.
     */
    void onResponseTriggered(@NotNull ITextComponent key, @NotNull ITextComponent response, PlayerEntity player);

    /**
     * Tick the data to update values.
     */
    void tick();

    /**
     * Trigger a possible interaction.
     *
     * @param handler the new handler.
     */
    void triggerInteraction(@NotNull IInteractionResponseHandler handler);

    /**
     * Get the texture suffix.
     *
     * @return the suffix.
     */
    String getTextureSuffix();

    /**
     * Set the texture suffix.
     *
     * @param suffix the suffix to set.
     */
    void setSuffix(String suffix);

    /**
     * Gets the entity
     *
     * @return
     */
    Optional<? extends AbstractCivilianEntity> getEntity();
}
