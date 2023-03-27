package com.minecolonies.coremod.quests.objectives;

import com.google.gson.JsonObject;
import com.minecolonies.api.quests.IColonyQuest;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Possible answer results in a dialogue tree.
 */
public interface IAnswerResult
{
    /**
     * Answer type registry.
     */
    Map<String, Function<JsonObject, IAnswerResult>> ANSWER_REGISTRY = new HashMap<>();

    /**
     * Result type option.
     */
    enum ResultOption
    {
        DIALOGUE("dialogue", json -> DialogueObjective.DialogueElement.parse(json)),
        RETURN("return", json -> new ReturnResult()),
        GO_TO("advanceObjective", json -> new GoToResult(json)),
        CANCEL("cancel", json -> new CancelResult());

        ResultOption(final String id, final Function<JsonObject, IAnswerResult> answerCreator)
        {
            ANSWER_REGISTRY.put(id, answerCreator);
        }
    }

    /**
     * Type that will close the quest without alteration.
     */
    class ReturnResult implements ITerminalAnswerResult
    {
        @Override
        public void applyToQuest(final IColonyQuest quest)
        {
            // Do nothing, just close UI.
        }
    }

    /**
     * Will advance the quest to another objective.
     */
    class GoToResult implements ITerminalAnswerResult
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
        public void applyToQuest(final IColonyQuest quest)
        {
            quest.advanceObjective(nextObjective);
        }
    }

    /**
     * Cancel request and remove from in-progress and available pool for now.
     */
    class CancelResult implements ITerminalAnswerResult
    {
        @Override
        public void applyToQuest(final IColonyQuest quest)
        {
            quest.onDeletion();
        }
    }
}
