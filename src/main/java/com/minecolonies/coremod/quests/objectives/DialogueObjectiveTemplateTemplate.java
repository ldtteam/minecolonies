package com.minecolonies.coremod.quests.objectives;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.minecolonies.api.colony.ICitizen;
import com.minecolonies.api.quests.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.minecolonies.api.quests.QuestParseConstant.TARGET_KEY;
import static com.minecolonies.api.quests.QuestParseConstant.UNLOCKS_REWARDS_KEY;

/**
 * Dialogue type of objective.
 */
public class DialogueObjectiveTemplateTemplate implements IDialogueObjectiveTemplate
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
    public DialogueObjectiveTemplateTemplate(final int target, final DialogueElement dialogueTree, final List<Integer> rewards)
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
    public static IQuestObjectiveTemplate createObjective(final JsonObject jsonObject)
    {
        return new DialogueObjectiveTemplateTemplate(jsonObject.get(TARGET_KEY).getAsInt(),
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
        if (!jsonObject.has(UNLOCKS_REWARDS_KEY))
        {
            return Collections.emptyList();
        }

        final List<Integer> rewardList = new ArrayList<>();
        final JsonArray jsonArray = jsonObject.get(UNLOCKS_REWARDS_KEY).getAsJsonArray();
        for (int i = 0; i < jsonArray.size(); i++)
        {
            rewardList.add(jsonArray.get(i).getAsInt());
        }

        return rewardList;
    }

    @Override
    public @NotNull IObjectiveInstance startObjective(final IQuestInstance colonyQuest)
    {
        if (target == 0)
        {
            colonyQuest.getQuestGiver().openDialogue(colonyQuest, colonyQuest.getIndex());
        }
        else
        {
            colonyQuest.getParticipant(target).openDialogue(colonyQuest, colonyQuest.getIndex());
        }
        return createObjectiveInstance();
    }

    @Override
    public @NotNull IObjectiveInstance createObjectiveInstance()
    {
        return new DialogueProgressInstance(this);
    }

    /**
     * Progress data of this objective.
     */
    public static class DialogueProgressInstance implements IDialogueObjectInstance
    {
        /**
         * Tag for has interacted the NBT state of this progress instance.
         */
        private static final String TAG_HAS_INTERACTED = "hasInteracted";

        /**
         * The template belonging to this progress instance.
         */
        private final DialogueObjectiveTemplateTemplate template;

        /**
         * Whether the player has completed the dialogue interaction.
         */
        private boolean hasInteracted = false;

        /**
         * Default constructor.
         */
        public DialogueProgressInstance(final DialogueObjectiveTemplateTemplate template)
        {
            this.template = template;
        }

        @Override
        public boolean isFulfilled()
        {
            return hasInteracted;
        }

        @Override
        public int getMissingQuantity()
        {
            return hasInteracted ? 0 : 1;
        }

        @Override
        public Component getProgressText(final IQuestInstance quest, final Style style)
        {
            final ICitizen citizen = quest.getColony().getCitizen(template.target == 0 ? quest.getQuestGiverId() : template.target - 1);
            if (citizen != null)
            {
                return Component.translatable("com.minecolonies.coremod.questobjectives.answer.progress", citizen.getName()).setStyle(style);
            }
            else
            {
                return Component.empty();
            }
        }

        @Override
        public void setHasInteracted(boolean hasInteracted)
        {
            this.hasInteracted = hasInteracted;
        }

        @Override
        public CompoundTag serializeNBT()
        {
            final CompoundTag compoundTag = new CompoundTag();
            compoundTag.putBoolean(TAG_HAS_INTERACTED, hasInteracted);
            return compoundTag;
        }

        @Override
        public void deserializeNBT(final CompoundTag compound)
        {
            this.hasInteracted = compound.getBoolean(TAG_HAS_INTERACTED);
        }
    }
}
