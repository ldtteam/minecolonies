package com.minecolonies.coremod.quests.objectives;

import com.google.gson.JsonObject;
import com.minecolonies.api.quests.IColonyQuest;
import com.minecolonies.api.quests.IDialogueObjective;
import com.minecolonies.api.quests.IObjectiveData;
import com.minecolonies.api.quests.IQuestObjective;

/**
 * Dialogue type of objective.
 */
public class DialogueObjective implements IDialogueObjective
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

    @Override
    public IObjectiveData init(final IColonyQuest colonyQuest)
    {
        if (target == 0)
        {
            colonyQuest.getQuestGiver().openDialogue(colonyQuest, colonyQuest.getIndex());
        }
        else
        {
            colonyQuest.getParticipant(target).openDialogue(colonyQuest, colonyQuest.getIndex());
        }
        return null;
    }
}
