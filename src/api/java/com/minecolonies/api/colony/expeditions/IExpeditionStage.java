package com.minecolonies.api.colony.expeditions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Interface for an expedition stage, an expedition can contain multiple stages, each with its own rewards/unlocks.
 */
public interface IExpeditionStage
{
    /**
     * A list of items obtained during this expedition stage.
     * Note: Adventure tokens are mixed raw into this list. Parsing them is up to implementation to handle.
     *
     * @return the list of items.
     */
    List<ItemStack> getRewards();

    /**
     * Adds a reward to this stage.
     *
     * @param itemStack the item to add to the stage.
     */
    void addReward(final ItemStack itemStack);

    /**
     * Get a map of mobs killed during this expedition stage, entries contain the mob type and their amount killed.
     *
     * @return the map of kills.
     */
    Map<EntityType<?>, Integer> getKills();

    /**
     * Adds a kill to this stage.
     *
     * @param type the entity type that got killed.
     */
    void rewardFound(final EntityType<?> type);

    /**
     * Get a members instance of what members were lost during this stage.
     *
     * @return which members were lost during this part of the expedition.
     */
    @Nullable
    List<IExpeditionMember> getMembersLost();

    /**
     * Adds a member that got lost during this stage.
     *
     * @param member the member that were lost.
     */
    void memberLost(final IExpeditionMember member);

    /**
     * Write this stage to compound data.
     *
     * @param compound the compound tag.
     */
    void write(final CompoundTag compound);
}