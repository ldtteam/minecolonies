package com.minecolonies.api.quests;

import com.google.gson.JsonObject;
import net.minecraft.world.entity.player.Player;

/**
 * Possible answer results in a dialogue tree.
 */
public interface IAnswerResult
{
    /**
     * Type that will close the quest without alteration.
     */
    class ReturnResult implements ITerminalAnswerResult
    {
        @Override
        public void applyToQuest(final Player player, final IColonyQuest quest)
        {
            // Do nothing, just close UI.
        }
    }

    /**
     * Will advance the quest to another objective.
     */
    class GoToResult implements IResolveResult
    {
        /**
         * The next objective to go to.
         */
        private final int nextObjective;

        /**
         * Create a new go to result.
         * @param nextObjective the next obj index.
         */
        public GoToResult(final int nextObjective)
        {
            this.nextObjective = nextObjective;
        }

        /**
         * Create the go to result from json.
         * @param jsonObject the json obj.
         */
        public GoToResult(final JsonObject jsonObject)
        {
            this.nextObjective = jsonObject.get("go-to").getAsInt();
        }

        @Override
        public void applyToQuest(final Player player, final IColonyQuest quest)
        {
            quest.advanceObjective(player, nextObjective);
        }
    }

    /**
     * Cancel request and remove from in-progress and available pool for now.
     */
    class CancelResult implements ITerminalAnswerResult
    {
        @Override
        public void applyToQuest(final Player player, final IColonyQuest quest)
        {
            quest.onDeletion();
        }
    }
}
