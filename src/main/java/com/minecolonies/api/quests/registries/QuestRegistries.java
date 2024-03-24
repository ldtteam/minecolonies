package com.minecolonies.api.quests.registries;

import com.google.gson.JsonObject;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.quests.IQuestDialogueAnswer;
import com.minecolonies.api.quests.IQuestObjectiveTemplate;
import com.minecolonies.api.quests.IQuestRewardTemplate;
import com.minecolonies.api.quests.IQuestTriggerTemplate;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import java.util.function.Function;

/**
 * All quest registries related things.
 */
public class QuestRegistries
{
    /**
     * Get the reward registry.
     * @return the reward registry.
     */
    static Registry<RewardEntry> getQuestRewardsRegistry()
    {
        return IMinecoloniesAPI.getInstance().getQuestRewardRegistry();
    }

    /**
     * Get the objective registry.
     * @return the reward registry.
     */
    static Registry<ObjectiveEntry> getQuestObjectiveRegistry()
    {
        return IMinecoloniesAPI.getInstance().getQuestObjectiveRegistry();
    }

    /**
     * Get the trigger registry.
     * @return the reward registry.
     */
    static Registry<TriggerEntry> getQuestTriggerRegistry()
    {
        return IMinecoloniesAPI.getInstance().getQuestTriggerRegistry();
    }

    /**
     * Get the dialogue answer result registry.
     * @return the reward registry.
     */
    static Registry<DialogueAnswerEntry> getDialogueAnswerResultRegistry()
    {
        return IMinecoloniesAPI.getInstance().getQuestDialogueAnswerRegistry();
    }

    /**
     * Quest reward entry type.
     */
    public static class RewardEntry
    {
        //todo create instance getters

        private final Function<JsonObject, IQuestRewardTemplate> producer;

        public RewardEntry(final Function<JsonObject, IQuestRewardTemplate> productionFunction)
        {
            this.producer = productionFunction;
        }

        /**
         * Create one from json.
         * @param jsonObject the input.
         * @return the reward.
         */
        public IQuestRewardTemplate produce(final JsonObject jsonObject)
        {
            return producer.apply(jsonObject);
        }
    }

    /**
     * Quest objective entry type.
     */
    public static class ObjectiveEntry
    {
        private final Function<JsonObject, IQuestObjectiveTemplate> producer;

        public ObjectiveEntry(final Function<JsonObject, IQuestObjectiveTemplate> productionFunction)
        {
            this.producer = productionFunction;
        }

        /**
         * Create one from json.
         * @param jsonObject the input.
         * @return the objective.
         */
        public IQuestObjectiveTemplate produce(final JsonObject jsonObject)
        {
            return producer.apply(jsonObject);
        }
    }

    /**
     * Quest trigger entry type.
     */
    public static class TriggerEntry
    {
        private final Function<JsonObject, IQuestTriggerTemplate> producer;

        public TriggerEntry(final Function<JsonObject, IQuestTriggerTemplate> productionFunction)
        {
            this.producer = productionFunction;
        }

        /**
         * Create one from json.
         * @param jsonObject the input.
         * @return the trigger.
         */
        public IQuestTriggerTemplate produce(final JsonObject jsonObject)
        {
            return producer.apply(jsonObject);
        }
    }

    /**
     * Quest dialogue entry type.
     */
    public static class DialogueAnswerEntry
    {
        private final Function<JsonObject, IQuestDialogueAnswer> producer;

        public DialogueAnswerEntry(final Function<JsonObject, IQuestDialogueAnswer> productionFunction)
        {
            this.producer = productionFunction;
        }

        /**
         * Create one from json.
         * @param jsonObject the input.
         * @return the answer result.
         */
        public IQuestDialogueAnswer produce(final JsonObject jsonObject)
        {
            return producer.apply(jsonObject);
        }
    }

    public static ResourceLocation ITEM_REWARD_ID         = new ResourceLocation(Constants.MOD_ID, "item");
    public static ResourceLocation SKILL_REWARD_ID        = new ResourceLocation(Constants.MOD_ID, "skill");
    public static ResourceLocation RESEARCH_REWARD_ID     = new ResourceLocation(Constants.MOD_ID, "research");
    public static ResourceLocation RAID_REWARD_ID         = new ResourceLocation(Constants.MOD_ID, "raid");
    public static ResourceLocation RELATIONSHIP_REWARD_ID = new ResourceLocation(Constants.MOD_ID, "relationship");
    public static ResourceLocation HAPPINESS_REWARD_ID    = new ResourceLocation(Constants.MOD_ID, "happiness");
    public static ResourceLocation UNLOCK_QUEST_REWARD_ID     = new ResourceLocation(Constants.MOD_ID, "unlockquest");
    public static ResourceLocation QUEST_REPUTATION_REWARD_ID = new ResourceLocation(Constants.MOD_ID, "questreputation");

    public static ResourceLocation DIALOGUE_OBJECTIVE_ID   = new ResourceLocation(Constants.MOD_ID, "dialogue");
    public static ResourceLocation BREAKBLOCK_OBJECTIVE_ID = new ResourceLocation(Constants.MOD_ID, "breakblock");
    public static ResourceLocation DELIVERY_OBJECTIVE_ID   = new ResourceLocation(Constants.MOD_ID, "delivery");
    public static ResourceLocation KILLENTITY_OBJECTIVE_ID = new ResourceLocation(Constants.MOD_ID, "killentity");
    public static ResourceLocation PLACEBLOCK_OBJECTIVE_ID = new ResourceLocation(Constants.MOD_ID, "placeblock");
    public static ResourceLocation BUILD_BUILDING_OBJECTIVE_ID = new ResourceLocation(Constants.MOD_ID, "buildbuilding");

    public static ResourceLocation STATE_TRIGGER_ID       = new ResourceLocation(Constants.MOD_ID, "state");
    public static ResourceLocation RANDOM_TRIGGER_ID      = new ResourceLocation(Constants.MOD_ID, "random");
    public static ResourceLocation CITIZEN_TRIGGER_ID     = new ResourceLocation(Constants.MOD_ID, "citizen");
    public static ResourceLocation UNLOCK_TRIGGER_ID      = new ResourceLocation(Constants.MOD_ID, "unlock");
    public static ResourceLocation QUEST_REPUTATION_TRIGGER_ID  = new ResourceLocation(Constants.MOD_ID, "questreputation");
    public static ResourceLocation WORLD_DIFFICULTY_TRIGGER_ID  = new ResourceLocation(Constants.MOD_ID, "difficulty");

    public static ResourceLocation DIALOGUE_ANSWER_ID = new ResourceLocation(Constants.MOD_ID, "dialogue");
    public static ResourceLocation RETURN_ANSWER_ID   = new ResourceLocation(Constants.MOD_ID, "return");
    public static ResourceLocation CANCEL_ANSWER_ID   = new ResourceLocation(Constants.MOD_ID, "cancel");
    public static ResourceLocation GOTO_ANSWER_ID     = new ResourceLocation(Constants.MOD_ID, "advanceobjective");


    public static DeferredHolder<RewardEntry, RewardEntry>  itemReward;
    public static DeferredHolder<RewardEntry, RewardEntry> skillReward;
    public static DeferredHolder<RewardEntry, RewardEntry> researchReward;
    public static DeferredHolder<RewardEntry, RewardEntry> raidReward;
    public static DeferredHolder<RewardEntry, RewardEntry> relationshipReward;
    public static DeferredHolder<RewardEntry, RewardEntry> happinessReward;
    public static DeferredHolder<RewardEntry, RewardEntry> unlockQuestReward;
    public static DeferredHolder<RewardEntry, RewardEntry> questReputationReward;

    public static DeferredHolder<ObjectiveEntry, ObjectiveEntry> dialogueObjective;
    public static DeferredHolder<ObjectiveEntry, ObjectiveEntry> breakBlockObjective;
    public static DeferredHolder<ObjectiveEntry, ObjectiveEntry> deliveryObjective;
    public static DeferredHolder<ObjectiveEntry, ObjectiveEntry> killEntityObjective;
    public static DeferredHolder<ObjectiveEntry, ObjectiveEntry> placeBlockObjective;
    public static DeferredHolder<ObjectiveEntry, ObjectiveEntry> buildBuildingObjective;

    public static DeferredHolder<TriggerEntry, TriggerEntry> stateTrigger;
    public static DeferredHolder<TriggerEntry, TriggerEntry> randomTrigger;
    public static DeferredHolder<TriggerEntry, TriggerEntry> citizenTrigger;
    public static DeferredHolder<TriggerEntry, TriggerEntry> unlockTrigger;
    public static DeferredHolder<TriggerEntry, TriggerEntry> questReputationTrigger;
    public static DeferredHolder<TriggerEntry, TriggerEntry> worldDifficultyTrigger;

    public static DeferredHolder<DialogueAnswerEntry, DialogueAnswerEntry> dialogueAnswerResult;
    public static DeferredHolder<DialogueAnswerEntry, DialogueAnswerEntry> returnAnswerResult;
    public static DeferredHolder<DialogueAnswerEntry, DialogueAnswerEntry> cancelAnswerResult;
    public static DeferredHolder<DialogueAnswerEntry, DialogueAnswerEntry> gotoAnswerResult;

}
