package com.minecolonies.coremod.quests.objectives;

import com.minecolonies.api.quests.IColonyQuest;
import com.minecolonies.api.quests.IObjectiveData;
import net.minecraft.world.entity.player.Player;

/**
 * Specific objective for entity killing.
 */
public interface IKillEntityObjective
{
    /**
     * Callback for entity kill event
     *
     * @param progressData the objective data.
     * @param player the involved player.
     */
    void onEntityKill(IObjectiveData progressData, final IColonyQuest colonyQuest, final Player player);
}
