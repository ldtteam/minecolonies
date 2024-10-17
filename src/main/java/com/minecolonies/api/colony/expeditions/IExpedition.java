package com.minecolonies.api.colony.expeditions;

import com.minecolonies.api.colony.colonyEvents.EventStatus;
import com.minecolonies.core.colony.expeditions.ExpeditionStage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Main interface for an expedition instance.
 */
public interface IExpedition
{
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
    Collection<ExpeditionStage> getResults();

    /**
     * Advances the current stage of the expedition.
     * Adding a new stage on top of the current one.
     *
     * @param header the header for this new stage.
     */
    void advanceStage(final Component header);

    /**
     * Adds a reward to the current newest stage.
     *
     * @param itemStack the item to add to the stage.
     */
    void rewardFound(final ItemStack itemStack);

    /**
     * Adds a kill to the current newest stage.
     *
     * @param encounterId the type of the encounter.
     */
    void mobKilled(final ResourceLocation encounterId);

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