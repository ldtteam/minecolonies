package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.quests.IDialogueObjectiveTemplate;
import com.minecolonies.api.quests.IQuestDialogueAnswer;
import com.minecolonies.api.quests.registries.QuestRegistries;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.apiimp.CommonMinecoloniesAPIImpl;
import com.minecolonies.core.quests.objectives.*;
import com.minecolonies.core.quests.rewards.*;
import com.minecolonies.core.quests.triggers.*;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.minecolonies.api.quests.registries.QuestRegistries.*;

public final class ModQuestInitializer
{
    public final static DeferredRegister<QuestRegistries.ObjectiveEntry>
      DEFERRED_REGISTER_OBJECTIVE = DeferredRegister.create(CommonMinecoloniesAPIImpl.QUEST_OBJECTIVES, Constants.MOD_ID);

    public final static DeferredRegister<QuestRegistries.TriggerEntry>
      DEFERRED_REGISTER_TRIGGER = DeferredRegister.create(CommonMinecoloniesAPIImpl.QUEST_TRIGGERS, Constants.MOD_ID);

    public final static DeferredRegister<QuestRegistries.RewardEntry>
      DEFERRED_REGISTER_REWARD = DeferredRegister.create(CommonMinecoloniesAPIImpl.QUEST_REWARDS, Constants.MOD_ID);

    public final static DeferredRegister<QuestRegistries.DialogueAnswerEntry>
      DEFERRED_REGISTER_ANSWER_RESULT = DeferredRegister.create(CommonMinecoloniesAPIImpl.QUEST_ANSWER_RESULTS, Constants.MOD_ID);


    private ModQuestInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModQuestInitializer but this is a Utility class.");
    }

    static
    {
        QuestRegistries.deliveryObjective = DEFERRED_REGISTER_OBJECTIVE.register(DELIVERY_OBJECTIVE_ID.getPath(), () -> new QuestRegistries.ObjectiveEntry(
          DeliveryObjectiveTemplateTemplate::createObjective));
        QuestRegistries.dialogueObjective = DEFERRED_REGISTER_OBJECTIVE.register(DIALOGUE_OBJECTIVE_ID.getPath(), () -> new QuestRegistries.ObjectiveEntry(
          DialogueObjectiveTemplateTemplate::createObjective));
        QuestRegistries.killEntityObjective = DEFERRED_REGISTER_OBJECTIVE.register(KILLENTITY_OBJECTIVE_ID.getPath(), () -> new QuestRegistries.ObjectiveEntry(
          KillEntityObjectiveTemplateTemplate::createObjective));
        QuestRegistries.breakBlockObjective = DEFERRED_REGISTER_OBJECTIVE.register(BREAKBLOCK_OBJECTIVE_ID.getPath(), () -> new QuestRegistries.ObjectiveEntry(
          BreakBlockObjectiveTemplate::createObjective));
        QuestRegistries.placeBlockObjective = DEFERRED_REGISTER_OBJECTIVE.register(PLACEBLOCK_OBJECTIVE_ID.getPath(), () -> new QuestRegistries.ObjectiveEntry(
          PlaceBlockObjectiveTemplate::createObjective));
        QuestRegistries.buildBuildingObjective = DEFERRED_REGISTER_OBJECTIVE.register(BUILD_BUILDING_OBJECTIVE_ID.getPath(), () -> new QuestRegistries.ObjectiveEntry(
          BuildBuildingObjectiveTemplate::createObjective));
        QuestRegistries.researchObjective = DEFERRED_REGISTER_OBJECTIVE.register(RESEARCH_OBJECTIVE_ID.getPath(), () -> new QuestRegistries.ObjectiveEntry(
          ResearchObjectiveTemplate::createObjective));

        QuestRegistries.randomTrigger = DEFERRED_REGISTER_TRIGGER.register(RANDOM_TRIGGER_ID.getPath(), () -> new QuestRegistries.TriggerEntry(RandomQuestTriggerTemplate::createStateTrigger));
        QuestRegistries.stateTrigger = DEFERRED_REGISTER_TRIGGER.register(STATE_TRIGGER_ID.getPath(), () -> new QuestRegistries.TriggerEntry(StateQuestTriggerTemplate::createStateTrigger));
        QuestRegistries.citizenTrigger = DEFERRED_REGISTER_TRIGGER.register(CITIZEN_TRIGGER_ID.getPath(), () -> new QuestRegistries.TriggerEntry(CitizenQuestTriggerTemplate::createStateTrigger));
        QuestRegistries.unlockTrigger = DEFERRED_REGISTER_TRIGGER.register(UNLOCK_TRIGGER_ID.getPath(), () -> new QuestRegistries.TriggerEntry(UnlockQuestTriggerTemplate::createUnlockTrigger));
        QuestRegistries.questReputationTrigger = DEFERRED_REGISTER_TRIGGER.register(QUEST_REPUTATION_TRIGGER_ID.getPath(), () -> new QuestRegistries.TriggerEntry(QuestReputationTriggerTemplate::createQuestReputationTrigger));
        QuestRegistries.worldDifficultyTrigger = DEFERRED_REGISTER_TRIGGER.register(WORLD_DIFFICULTY_TRIGGER_ID.getPath(), () -> new QuestRegistries.TriggerEntry(WorldDifficultyTriggerTemplate::createDifficultyTrigger));

        QuestRegistries.itemReward = DEFERRED_REGISTER_REWARD.register(ITEM_REWARD_ID.getPath(), () -> new QuestRegistries.RewardEntry(ItemRewardTemplate::createReward));
        QuestRegistries.skillReward = DEFERRED_REGISTER_REWARD.register(SKILL_REWARD_ID.getPath(), () -> new QuestRegistries.RewardEntry(SkillRewardTemplate::createReward));
        QuestRegistries.researchReward = DEFERRED_REGISTER_REWARD.register(RESEARCH_REWARD_ID.getPath(), () -> new QuestRegistries.RewardEntry(ResearchCompleteRewardTemplate::createReward));
        QuestRegistries.raidReward = DEFERRED_REGISTER_REWARD.register(RAID_REWARD_ID.getPath(), () -> new QuestRegistries.RewardEntry(RaidAdjustmentRewardTemplate::createReward));
        QuestRegistries.relationshipReward = DEFERRED_REGISTER_REWARD.register(RELATIONSHIP_REWARD_ID.getPath(), () -> new QuestRegistries.RewardEntry(RelationshipRewardTemplate::createReward));
        QuestRegistries.happinessReward = DEFERRED_REGISTER_REWARD.register(HAPPINESS_REWARD_ID.getPath(), () -> new QuestRegistries.RewardEntry(HappinessRewardTemplate::createReward));
        QuestRegistries.unlockQuestReward = DEFERRED_REGISTER_REWARD.register(UNLOCK_QUEST_REWARD_ID.getPath(), () -> new QuestRegistries.RewardEntry(UnlockQuestRewardTemplate::createReward));
        QuestRegistries.questReputationReward = DEFERRED_REGISTER_REWARD.register(QUEST_REPUTATION_REWARD_ID.getPath(), () -> new QuestRegistries.RewardEntry(QuestReputationRewardTemplate::createReward));

        QuestRegistries.dialogueAnswerResult = DEFERRED_REGISTER_ANSWER_RESULT.register(DIALOGUE_ANSWER_ID.getPath(), () -> new QuestRegistries.DialogueAnswerEntry(IDialogueObjectiveTemplate.DialogueElement::parse));
        QuestRegistries.returnAnswerResult = DEFERRED_REGISTER_ANSWER_RESULT.register(RETURN_ANSWER_ID.getPath(), () -> new QuestRegistries.DialogueAnswerEntry(json -> new IQuestDialogueAnswer.CloseUIDialogueAnswer()));
        QuestRegistries.gotoAnswerResult = DEFERRED_REGISTER_ANSWER_RESULT.register(GOTO_ANSWER_ID.getPath(), () -> new QuestRegistries.DialogueAnswerEntry(IQuestDialogueAnswer.NextObjectiveDialogueAnswer::new));
        QuestRegistries.cancelAnswerResult = DEFERRED_REGISTER_ANSWER_RESULT.register(CANCEL_ANSWER_ID.getPath(), () -> new QuestRegistries.DialogueAnswerEntry(json -> new IQuestDialogueAnswer.QuestCancellationDialogueAnswer()));
    }
}
