package com.minecolonies.coremod.quests.objectives;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.quests.IQuestObjective;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dialogue type of objective.
 */
public class DialogueObjective implements IQuestObjective
{
    /**
     * The quest participant target of this dialogue (0 if questgiver).
     */
    private final int target;

    /**
     * The dialogue tree.
     */
    private final DialogueElement dialogueTree;

    /**
     * Create a new dialogue objective.
     * @param target the target of the dialogue.
     * @param dialogueTree the dialogue tree.
     */
    public DialogueObjective(final int target, final DialogueElement dialogueTree)
    {
        this.target = target;
        this.dialogueTree = dialogueTree;
    }

     /*"target": 0,
       "dialogue": [
    {
        "text": "Hi, I am $0, how are you?",
                  "options": [
        {
            "answer": "Im alright, and you?",
                        "result": {
                        "type": "dialogue",
                      "text": "Could you bring me an Apple? I'm really hungry!",
                                "options": [
            {
                "answer": "Sure!",
                            "result": {
                "type": "advanceObjective",
                          "go-to": 1
            }
    */

    /**
     * Getter for the dialogue tree.
     * @return the tree.
     */
    public DialogueElement getDialogueTree()
    {
        return dialogueTree;
    }

    /**
     * Parse the dialogue objective from json.
     * @param jsonObject the json to parse it from.
     * @return a new objective object.
     */
    public static IQuestObjective createObjective(final JsonObject jsonObject)
    {
        return new DialogueObjective(jsonObject.get("target").getAsInt(), DialogueElement.parse(jsonObject.getAsJsonObject("dialogue")));
    }

    /**
     * A dialogue element in the dialogue objective.
     */
    public static class DialogueElement implements IAnswerResult
    {
        /**
         * The text the participant says.
         */
        private final String text;

        /**
         * The player options.
         */
        private final List<AnswerElement> answers;

        /**
         * Create a new dialogue element.
         * @param text the participant.
         * @param answers the player answers.
         */
        public DialogueElement(final String text, final List<AnswerElement> answers)
        {
            this.text = text;
            this.answers = answers;
        }

        /**
         * Parse the element from json.
         * @param jsonObject the json to parse it from.
         * @return a new element.
         */
        public static DialogueElement parse(final JsonObject jsonObject)
        {
            final String text = jsonObject.get("text").getAsString();
            final List<AnswerElement> answerElementList = new ArrayList<>();
            for (final JsonElement answerOption : jsonObject.getAsJsonArray("options"))
            {
                answerElementList.add(AnswerElement.parse(answerOption.getAsJsonObject()));
            }
            return new DialogueElement(text, answerElementList);
        }

        /**
         * Getter for the element text.
         * @return the text.
         */
        public Component getText()
        {
            return Component.literal(this.text);
        }

        /**
         * Get all the response options.
         * @return the response option.
         */
        public List<Component> getOptions()
        {
            return answers.stream().map(answerElement -> Component.literal(answerElement.text)).collect(Collectors.toList());
        }

        /**
         * Get the matching answer result.
         * @param response the triggered response to match.
         * @return the next answer.
         */
        public IAnswerResult getOptionResult(final Component response)
        {
            for (final AnswerElement answerElement : answers)
            {
                if (answerElement.text.equals(response.getString()))
                {
                    return answerElement.answerResult;
                }
            }
            return null;
        }
    }

    /**
     * An answer element part of a dialogue path.
     */
    public static class AnswerElement
    {
        /**
         * The text the player displays.
         */
        private final String text;

        /**
         * The result from the player answer.
         */
        private final IAnswerResult answerResult;

        /**
         * Create a new answer element.
         * @param text the text for the player.
         * @param answerResult the result from the choice.
         */
        public AnswerElement(final String text, final IAnswerResult answerResult)
        {
            this.text = text;
            this.answerResult = answerResult;
        }

        /**
         * Parse the answer element from json.
         * @param jsonObject the json obj.
         * @return the answer element.
         */
        public static AnswerElement parse(final JsonObject jsonObject)
        {
            final JsonObject resultObj = jsonObject.getAsJsonObject("result");
            return new AnswerElement(jsonObject.get("answer").getAsString(), IAnswerResult.ANSWER_REGISTRY.get(resultObj.get("type").getAsString()).apply(resultObj));
        }
    }
}
