package com.minecolonies.coremod.quests.objectives;

import com.minecolonies.api.quests.IQuestInstance;
import com.minecolonies.api.quests.IObjectiveData;
import net.minecraft.world.entity.player.Player;

/**
 * Specific objective for block breaking.
 */
public interface IBreakBlockObjective
{
    /**
     * Callback for block break event
     *
     * @param blockMiningProgressData the objective data.
     * @param player the involved player.
     */
    void onBlockBreak(IObjectiveData blockMiningProgressData, final IQuestInstance colonyQuest, final Player player);
}
