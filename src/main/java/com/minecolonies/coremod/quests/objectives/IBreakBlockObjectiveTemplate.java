package com.minecolonies.coremod.quests.objectives;

import com.minecolonies.api.quests.IQuestInstance;
import com.minecolonies.api.quests.IObjectiveInstance;
import net.minecraft.world.entity.player.Player;

/**
 * Specific objective for block breaking.
 */
public interface IBreakBlockObjectiveTemplate
{
    /**
     * Callback for block break event
     *
     * @param blockMiningProgressData the objective data.
     * @param player the involved player.
     */
    void onBlockBreak(IObjectiveInstance blockMiningProgressData, final IQuestInstance colonyQuest, final Player player);
}
