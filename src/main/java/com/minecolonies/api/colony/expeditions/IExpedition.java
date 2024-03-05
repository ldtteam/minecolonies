package com.minecolonies.api.colony.expeditions;

import com.minecolonies.api.colony.colonyEvents.EventStatus;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Main interface for an expedition instance.
 */
public interface IExpedition
{
    /**
     * Get the status of the expedition.
     *
     * @return the current status.
     */
    ExpeditionStatus getStatus();

    /**
     * Set the status of the expedition.
     *
     * @param status the new status.
     */
    void setStatus(final ExpeditionStatus status);

    /**
     * Get all the members of the expedition.
     *
     * @return the expedition members.
     */
    @NotNull
    Collection<IExpeditionMember<?>> getMembers();

    /**
     * Get the equipment given to the expedition at start time.
     *
     * @return the equipment list.
     */
    Collection<ItemStack> getEquipment();

    /**
     * Get the currently active members of the expedition.
     *
     * @return the expedition members.
     */
    @NotNull
    Collection<IExpeditionMember<?>> getActiveMembers();

    /**
     * The results of this expedition.
     * Yields null as long as the stage of the expedition is not {@link EventStatus#DONE}.
     *
     * @return a list of stages containing results per expedition stage.
     */
    Collection<IExpeditionStage> getResults();

    /**
     * Advances the current stage of the expedition.
     * Adding a new stage on top of the current one.
     */
    void advanceStage();

    /**
     * Adds a reward to the current newest stage.
     *
     * @param itemStack the item to add to the stage.
     */
    void rewardFound(final ItemStack itemStack);

    /**
     * Adds a kill to the current newest stage.
     *
     * @param type the entity type that got killed.
     */
    void mobKilled(final EntityType<?> type);

    /**
     * Adds a member that got lost during the current newest stage.
     *
     * @param member the member that were lost.
     */
    void memberLost(final IExpeditionMember<?> member);

    /**
     * Write this expedition to compound data.
     *
     * @param compound the compound tag.
     */
    void write(final CompoundTag compound);
}