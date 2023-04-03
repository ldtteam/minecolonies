package com.minecolonies.coremod.quests.objectives;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.minecolonies.api.quests.IColonyQuest;
import com.minecolonies.api.quests.IDialogueObjective;
import com.minecolonies.api.quests.IObjectiveData;
import com.minecolonies.api.quests.IQuestObjective;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
     * Reward unlocks from the objective.
     */
    private final List<Integer>  rewardUnlocks;

    /**
     * Create a new dialogue objective.
     * @param target the target of the dialogue.
     * @param dialogueTree the dialogue tree.
     */
    public DialogueObjective(final int target, final DialogueElement dialogueTree, final List<Integer> rewards)
    {
        this.target = target;
        this.dialogueTree = dialogueTree;
        this.rewardUnlocks = ImmutableList.copyOf(rewards);
    }

    /**
     * Getter for the dialogue tree.
     * @return the tree.
     */
    public DialogueElement getDialogueTree()
    {
        return dialogueTree;
    }

    @Override
    public List<Integer> getRewardUnlocks()
    {
        return this.rewardUnlocks;
    }

    /**
     * Parse the dialogue objective from json.
     * @param jsonObject the json to parse it from.
     * @return a new objective object.
     */
    public static IQuestObjective createObjective(final JsonObject jsonObject)
    {
        return new DialogueObjective(jsonObject.get("target").getAsInt(),
          DialogueElement.parse(jsonObject),
            parseRewards(jsonObject));
    }

    /**
     * Parse the specific reward array from the objective.
     * @param jsonObject the object to get it from.
     * @return the unlocked rewards.
     */
    public static List<Integer> parseRewards(final JsonObject jsonObject)
    {
        if (!jsonObject.has("unlocks-rewards"))
        {
            return Collections.emptyList();
        }

        final List<Integer> rewardList = new ArrayList<>();
        final JsonArray jsonArray = jsonObject.get("unlocks-rewards").getAsJsonArray();
        for (int i = 0; i < jsonArray.size(); i++)
        {
            rewardList.add(jsonArray.get(i).getAsInt());
        }

        return rewardList;
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
