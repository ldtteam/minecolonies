package com.minecolonies.api.quests;

import net.minecraft.world.entity.player.Player;

/**
 * Quest objective interface only for action types (e.g. deliveries).
 */
public interface IQuestActionObjective
{
    /**
     * Check if the objective is ready to move on.
     * @param colonyQuest the objective belongs to.
     * @return true if so.
     */
    boolean isReady(final Player player, final IColonyQuest colonyQuest);

    /**
     * Attempt to resolve an objective.
     * @param colonyQuest the objective belongs to.
     * @return true if so.
     */
    boolean tryResolve(final Player player, final IColonyQuest colonyQuest);

    /**
     * Dialogue tree when the conditions are fulfilled.
     * @return the dialogue to play.
     */
    IDialogueObjective.DialogueElement getReadyDialogueTree();
}
