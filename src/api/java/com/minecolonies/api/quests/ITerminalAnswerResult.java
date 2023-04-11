package com.minecolonies.api.quests;

import net.minecraft.world.entity.player.Player;

/**
 * Terminal type answer. Will close the interaction.
 */
public interface ITerminalAnswerResult extends IAnswerResult
{

    /**
     * Apply the objective to colony quest. This only applies to the terminal ones!
     * @param player the player triggering it.
     * @param quest the quest to apply itself to.
     */
    void applyToQuest(final Player player, IColonyQuest quest);
}
