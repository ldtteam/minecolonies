package com.minecolonies.api.quests;

import com.google.gson.JsonObject;
import net.minecraft.world.entity.player.Player;

import static com.minecolonies.api.quests.QuestParseConstant.GO_TO_ID;

/**
 * Possible answer results in a dialogue tree.
 */
public interface IQuestDialogueAnswer
{
    /**
     * Type that will close the quest without alteration.
     */
    class CloseUIDialogueAnswer implements IFinalQuestDialogueAnswer
    {
        @Override
        public void applyToQuest(final Player player, final IQuestInstance quest)
        {
            // Do nothing, just close UI.
        }
    }

    /**
     * Will advance the quest to another objective.
     */
    class NextObjectiveDialogueAnswer implements IQuestPositiveDialogueAnswer
    {
        /**
         * The next objective to go to.
         */
        private final int nextObjective;

        /**
         * Create a new go to result.
         * @param nextObjective the next obj index.
         */
        public NextObjectiveDialogueAnswer(final int nextObjective)
        {
            this.nextObjective = nextObjective;
        }

        /**
         * Create the go to result from json.
         * @param jsonObject the json obj.
         */
        public NextObjectiveDialogueAnswer(final JsonObject jsonObject)
        {
            this.nextObjective = jsonObject.get(GO_TO_ID).getAsInt();
        }

        @Override
        public void applyToQuest(final Player player, final IQuestInstance quest)
        {
            quest.advanceObjective(player, nextObjective);
        }
    }

    /**
     * Cancel request and remove from in-progress and available pool for now.
     */
    class QuestCancellationDialogueAnswer implements IFinalQuestDialogueAnswer
    {
        @Override
        public void applyToQuest(final Player player, final IQuestInstance quest)
        {
            quest.onDeletion();
        }
    }
}
