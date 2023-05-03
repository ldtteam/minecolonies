package com.minecolonies.api.quests;

import com.minecolonies.api.colony.IColony;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/**
 * Quest Data Instance.
 */
public interface IQuestTemplate
{
    /**
     * This is where we actually check if the colony fulfills the quest requirements.
     * @param colony the colony to check.
     * @return the colony quest instance if successful.
     */
    IQuestInstance attemptStart(final IColony colony);

    /**
     * Timeout in ingame days until an available quest or in progress quest gets deleted.
     * @return the timeout.
     */
    int getQuestTimeout();

    /**
     * How often we can redo this quest.
     * @return the number.
     */
    int getMaxOccurrence();

    /**
     * Unlock the quest rewards.
     *
     * @param colony          the colony.
     * @param player          the player.
     * @param colonyQuest     the quest.
     * @param unlockedRewards the applicable rewards.
     */
    void unlockQuestRewards(IColony colony, Player player, final IQuestInstance colonyQuest, final List<Integer> unlockedRewards);

    /**
     * Get the objective at a given index.
     * @param index the index of the objective.
     * @return the current objective.
     */
    IQuestObjectiveTemplate getObjective(final int index);

    /**
     * Get the objective count of this quest.
     * @return the count.
     */
    int getObjectiveCount();

    /**
     * The name of the quest.
     * @return the name of the quest.
     */
    Component getName();

    /**
     * Get the list of parent quests.
     * @return the list of parent quests.
     */
    List<ResourceLocation> getParents();
}
