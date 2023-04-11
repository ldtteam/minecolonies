package com.minecolonies.api.quests.registries;

import com.google.gson.JsonObject;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.quests.*;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;
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
    static IForgeRegistry<RewardEntry> getQuestRewardsRegistry()
    {
        return IMinecoloniesAPI.getInstance().getQuestRewardRegistry();
    }

    /**
     * Get the objective registry.
     * @return the reward registry.
     */
    static IForgeRegistry<ObjectiveEntry> getQuestObjectiveRegistry()
    {
        return IMinecoloniesAPI.getInstance().getQuestObjectiveRegistry();
    }

    /**
     * Get the trigger registry.
     * @return the reward registry.
     */
    static IForgeRegistry<TriggerEntry> getQuestTriggerRegistry()
    {
        return IMinecoloniesAPI.getInstance().getQuestTriggerRegistry();
    }

    /**
     * Get the dialogue answer result registry.
     * @return the reward registry.
     */
    static IForgeRegistry<DialogueAnswerEntry> getDialogueAnswerResultRegistry()
    {
        return IMinecoloniesAPI.getInstance().getQuestDialogueAnswerRegistry();
    }

    /**
     * Quest reward entry type.
     */
    public static class RewardEntry
    {
        //todo create instance getters

        private final Function<JsonObject, IQuestReward> producer;

        public RewardEntry(final Function<JsonObject, IQuestReward> productionFunction)
        {
            this.producer = productionFunction;
        }

        /**
         * Create one from json.
         * @param jsonObject the input.
         * @return the reward.
         */
        public IQuestReward produce(final JsonObject jsonObject)
        {
            return producer.apply(jsonObject);
        }
    }

    /**
     * Quest objective entry type.
     */
    public static class ObjectiveEntry
    {
        private final Function<JsonObject, IQuestObjective> producer;

        public ObjectiveEntry(final Function<JsonObject, IQuestObjective> productionFunction)
        {
            this.producer = productionFunction;
        }

        /**
         * Create one from json.
         * @param jsonObject the input.
         * @return the objective.
         */
        public IQuestObjective produce(final JsonObject jsonObject)
        {
            return producer.apply(jsonObject);
        }
    }

    /**
     * Quest trigger entry type.
     */
    public static class TriggerEntry
    {
        private final Function<JsonObject, IQuestTrigger> producer;

        public TriggerEntry(final Function<JsonObject, IQuestTrigger> productionFunction)
        {
            this.producer = productionFunction;
        }

        /**
         * Create one from json.
         * @param jsonObject the input.
         * @return the trigger.
         */
        public IQuestTrigger produce(final JsonObject jsonObject)
        {
            return producer.apply(jsonObject);
        }
    }

    /**
     * Quest dialogue entry type.
     */
    public static class DialogueAnswerEntry
    {
        private final Function<JsonObject, IAnswerResult> producer;

        public DialogueAnswerEntry(final Function<JsonObject, IAnswerResult> productionFunction)
        {
            this.producer = productionFunction;
        }

        /**
         * Create one from json.
         * @param jsonObject the input.
         * @return the answer result.
         */
        public IAnswerResult produce(final JsonObject jsonObject)
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

    public static ResourceLocation DIALOGUE_OBJECTIVE_ID   = new ResourceLocation(Constants.MOD_ID, "dialogue");
    public static ResourceLocation BREAKBLOCK_OBJECTIVE_ID = new ResourceLocation(Constants.MOD_ID, "breakblock");
    public static ResourceLocation DELIVERY_OBJECTIVE_ID   = new ResourceLocation(Constants.MOD_ID, "delivery");
    public static ResourceLocation KILLENTITY_OBJECTIVE_ID = new ResourceLocation(Constants.MOD_ID, "killentity");

    public static ResourceLocation STATE_TRIGGER_ID   = new ResourceLocation(Constants.MOD_ID, "state");
    public static ResourceLocation RANDOM_TRIGGER_ID  = new ResourceLocation(Constants.MOD_ID, "random");
    public static ResourceLocation CITIZEN_TRIGGER_ID = new ResourceLocation(Constants.MOD_ID, "citizen");

    public static ResourceLocation DIALOGUE_ANSWER_ID = new ResourceLocation(Constants.MOD_ID, "dialogue");
    public static ResourceLocation RETURN_ANSWER_ID   = new ResourceLocation(Constants.MOD_ID, "return");
    public static ResourceLocation CANCEL_ANSWER_ID   = new ResourceLocation(Constants.MOD_ID, "cancel");
    public static ResourceLocation GOTO_ANSWER_ID     = new ResourceLocation(Constants.MOD_ID, "advanceobjective");


    public static RegistryObject<RewardEntry>  itemReward;
    public static RegistryObject <RewardEntry> skillReward;
    public static RegistryObject <RewardEntry> researchReward;
    public static RegistryObject <RewardEntry> raidReward;
    public static RegistryObject <RewardEntry> relationshipReward;
    public static RegistryObject <RewardEntry> happinessReward;

    public static RegistryObject <ObjectiveEntry> dialogueObjective;
    public static RegistryObject <ObjectiveEntry> breakBlockObjective;
    public static RegistryObject <ObjectiveEntry> deliveryObjective;
    public static RegistryObject <ObjectiveEntry> killEntityObjective;

    public static RegistryObject <TriggerEntry> stateTrigger;
    public static RegistryObject <TriggerEntry> randomTrigger;
    public static RegistryObject <TriggerEntry> citizenTrigger;

    public static RegistryObject <DialogueAnswerEntry> dialogueAnswerResult;
    public static RegistryObject <DialogueAnswerEntry> returnAnswerResult;
    public static RegistryObject <DialogueAnswerEntry> cancelAnswerResult;
    public static RegistryObject <DialogueAnswerEntry> gotoAnswerResult;

}
