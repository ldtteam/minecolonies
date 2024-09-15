package com.minecolonies.api.colony.managers.interfaces.expeditions;

import com.minecolonies.api.colony.expeditions.ExpeditionFinishedStatus;
import com.minecolonies.api.colony.expeditions.ExpeditionStatus;
import com.minecolonies.core.colony.expeditions.colony.ColonyExpeditionBuilder;
import com.minecolonies.core.colony.expeditions.colony.types.ColonyExpeditionType;
import com.minecolonies.core.items.ItemExpeditionSheet.ExpeditionSheetContainerManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Interface for the colony expedition manager. From here all outgoing expeditions to external places are managed.
 */
public interface IColonyExpeditionManager extends INBTSerializable<CompoundTag>
{
    /**
     * Get a list of all the active expeditions in the colony.
     *
     * @return an unmodifiable list.
     */
    List<ColonyExpedition> getActiveExpeditions();

    /**
     * Get a list of all the finished expeditions in the colony.
     *
     * @return an unmodifiable list.
     */
    List<ColonyExpedition> getFinishedExpeditions();

    /**
     * Get the created expedition with the given id.
     *
     * @param id the expedition id.
     * @return the expedition instance or null.
     */
    @Nullable
    CreatedExpedition getCreatedExpedition(int id);

    /**
     * Get the active expedition with the given id.
     *
     * @param id the expedition id.
     * @return the expedition instance or null.
     */
    @Nullable
    ColonyExpedition getActiveExpedition(final int id);

    /**
     * Get the finished expedition with the given id.
     *
     * @param id the expedition id.
     * @return the expedition instance or null.
     */
    @Nullable
    FinishedExpedition getFinishedExpedition(int id);

    /**
     * Get the status of a given expedition.
     *
     * @param id the expedition id.
     * @return the expedition status.
     */
    @NotNull
    ExpeditionStatus getExpeditionStatus(final int id);

    /**
     * Register a new expedition to the manager.
     *
     * @param id               the expedition id.
     * @param expeditionTypeId the expedition type id.
     * @return true if successfully added.
     */
    boolean addExpedition(final int id, final ResourceLocation expeditionTypeId);

    /**
     * Mark as expedition as accepted.
     *
     * @param id the expedition id.
     * @return true if the expedition exists.
     */
    boolean acceptExpedition(final int id);

    /**
     * Mark as expedition as finished.
     *
     * @param id      the expedition id.
     * @param builder the builder used for getting extra expedition information.
     * @return true if the expedition exists.
     */
    boolean startExpedition(final int id, final ColonyExpeditionBuilder builder);

    /**
     * Mark as expedition as finished.
     *
     * @param id     the expedition id.
     * @param status the status of how the expedition finished.
     * @return true if the expedition exists.
     */
    boolean finishExpedition(final int id, final ExpeditionFinishedStatus status);

    /**
     * Removes a previously created expedition, in case the expedition was requested to be cancelled.
     *
     * @param id the expedition id.
     */
    void removeCreatedExpedition(int id);

    /**
     * Get whether another expedition may be started (determines if another visitor may spawn).
     *
     * @return true if so.
     */
    boolean canStartNewExpedition();

    /**
     * Check that determines if expeditions to a given dimension may be sent or not.
     *
     * @param dimension the target dimension.
     * @return whether the target dimension is allowed or not.
     */
    boolean canGoToDimension(final ResourceKey<Level> dimension);

    /**
     * Check if all requirements for a given expedition type are met.
     *
     * @param expeditionTypeId the expedition type id.
     * @param inventory        the inventory to check requirements against.
     * @return true if so.
     */
    boolean meetsRequirements(final ResourceLocation expeditionTypeId, final ExpeditionSheetContainerManager inventory);

    /**
     * Check if all requirements for a given expedition type are met.
     *
     * @param expeditionType the expedition type instance.
     * @param inventory      the inventory to check requirements against.
     * @return true if so.
     */
    boolean meetsRequirements(final ColonyExpeditionType expeditionType, final ExpeditionSheetContainerManager inventory);

    /**
     * Unlock nether expeditions.
     */
    void unlockNether();

    /**
     * Unlock end expeditions.
     */
    void unlockEnd();

    /**
     * Whether the expedition manager class is dirty and the client needs to be updated.
     *
     * @return true if so.
     */
    boolean isDirty();

    /**
     * Update the dirty flag of the expedition manager.
     *
     * @param dirty the new dirty state.
     */
    void setDirty(boolean dirty);
}