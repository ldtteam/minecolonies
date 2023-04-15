package com.minecolonies.api.quests;

import com.minecolonies.api.colony.IColony;
import net.minecraft.world.entity.player.Player;

/**
 * Quest reward interface for all reward types.
 */
public interface IQuestRewardTemplate
{
    /**
     * Apply the reward to colony and player.
     * @param colony the involved colony.
     * @param player the involved player.
     * @param colonyQuest the related quest.
     */
    void applyReward(final IColony colony, final Player player, final IQuestInstance colonyQuest);
}
