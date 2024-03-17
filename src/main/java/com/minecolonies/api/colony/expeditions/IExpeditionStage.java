package com.minecolonies.api.colony.expeditions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Interface for an expedition stage, an expedition can contain multiple stages, each with its own rewards/unlocks.
 */
public interface IExpeditionStage
{
    /**
     * Get a map of mobs killed during this expedition stage, entries contain the mob type and their amount killed.
     *
     * @return the list of kills.
     */
    @NotNull
    List<MobKill> getKills();

    /**
     * Adds a kill to this stage.
     *
     * @param type the entity type that got killed.
     */
    void addKill(final EntityType<?> type);

    /**
     * Get a members instance of what members were lost during this stage.
     *
     * @return which members were lost during this part of the expedition.
     */
    @NotNull
    List<IExpeditionMember<?>> getMembersLost();

    /**
     * Adds a member that got lost during this stage.
     *
     * @param member the member that were lost.
     */
    void memberLost(final IExpeditionMember<?> member);

    /**
     * Write this stage to compound data.
     *
     * @param compound the compound tag.
     */
    void write(final CompoundTag compound);
}