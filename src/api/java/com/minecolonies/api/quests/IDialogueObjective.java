package com.minecolonies.api.quests;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.IMinecoloniesAPI;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dialogue type of objective interface.
 */
public interface IDialogueObjective extends IQuestObjective
{

    /**
     * Getter for the dialogue tree.
     * @return the tree.
     */
    DialogueElement getDialogueTree();


    /**
     * A dialogue element in the dialogue objective.
     */
    class DialogueElement implements IAnswerResult
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
        public String getText()
        {
            return this.text;
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
    class AnswerElement
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
            return new AnswerElement(jsonObject.get("answer").getAsString(), IMinecoloniesAPI.getInstance().getQuestDialogueAnswerRegistry().getValue(new ResourceLocation(resultObj.get("type").getAsString())).produce(resultObj));
        }
    }
}
