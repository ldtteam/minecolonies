package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.quests.IAnswerResult;
import com.minecolonies.api.quests.IDialogueObjective;
import com.minecolonies.api.quests.registries.QuestRegistries;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.quests.objectives.BreakBlockObjective;
import com.minecolonies.coremod.quests.objectives.DeliveryObjective;
import com.minecolonies.coremod.quests.objectives.DialogueObjective;
import com.minecolonies.coremod.quests.objectives.KillEntityObjective;
import com.minecolonies.coremod.quests.rewards.*;
import com.minecolonies.coremod.quests.triggers.CitizenQuestTrigger;
import com.minecolonies.coremod.quests.triggers.RandomQuestTrigger;
import com.minecolonies.coremod.quests.triggers.StateQuestTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;

import static com.minecolonies.api.quests.registries.QuestRegistries.*;

public final class ModQuestInitializer
{
    public final static DeferredRegister<QuestRegistries.ObjectiveEntry>
      DEFERRED_REGISTER_OBJECTIVE = DeferredRegister.create(new ResourceLocation(Constants.MOD_ID, "questobjectives"), Constants.MOD_ID);

    public final static DeferredRegister<QuestRegistries.TriggerEntry>
      DEFERRED_REGISTER_TRIGGER = DeferredRegister.create(new ResourceLocation(Constants.MOD_ID, "questtriggers"), Constants.MOD_ID);

    public final static DeferredRegister<QuestRegistries.RewardEntry>
      DEFERRED_REGISTER_REWARD = DeferredRegister.create(new ResourceLocation(Constants.MOD_ID, "questrewards"), Constants.MOD_ID);

    public final static DeferredRegister<QuestRegistries.DialogueAnswerEntry>
      DEFERRED_REGISTER_ANSWER_RESULT = DeferredRegister.create(new ResourceLocation(Constants.MOD_ID, "questanswerresults"), Constants.MOD_ID);


    private ModQuestInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModResearchRequirementInitializer but this is a Utility class.");
    }

    static
    {
        QuestRegistries.deliveryObjective = DEFERRED_REGISTER_OBJECTIVE.register(DELIVERY_OBJECTIVE_ID.getPath(), () -> new QuestRegistries.ObjectiveEntry(DeliveryObjective::createObjective));
        QuestRegistries.dialogueObjective = DEFERRED_REGISTER_OBJECTIVE.register(DIALOGUE_OBJECTIVE_ID.getPath(), () -> new QuestRegistries.ObjectiveEntry(DialogueObjective::createObjective));
        QuestRegistries.killEntityObjective = DEFERRED_REGISTER_OBJECTIVE.register(KILLENTITY_OBJECTIVE_ID.getPath(), () -> new QuestRegistries.ObjectiveEntry(KillEntityObjective::createObjective));
        QuestRegistries.breakBlockObjective = DEFERRED_REGISTER_OBJECTIVE.register(BREAKBLOCK_OBJECTIVE_ID.getPath(), () -> new QuestRegistries.ObjectiveEntry(BreakBlockObjective::createObjective));

        QuestRegistries.randomTrigger = DEFERRED_REGISTER_TRIGGER.register(RANDOM_TRIGGER_ID.getPath(), () -> new QuestRegistries.TriggerEntry(RandomQuestTrigger::createStateTrigger));
        QuestRegistries.stateTrigger = DEFERRED_REGISTER_TRIGGER.register(STATE_TRIGGER_ID.getPath(), () -> new QuestRegistries.TriggerEntry(StateQuestTrigger::createStateTrigger));
        QuestRegistries.citizenTrigger = DEFERRED_REGISTER_TRIGGER.register(CITIZEN_TRIGGER_ID.getPath(), () -> new QuestRegistries.TriggerEntry(CitizenQuestTrigger::createStateTrigger));

        QuestRegistries.itemReward = DEFERRED_REGISTER_REWARD.register(ITEM_REWARD_ID.getPath(), () -> new QuestRegistries.RewardEntry(ItemReward::createReward));
        QuestRegistries.skillReward = DEFERRED_REGISTER_REWARD.register(SKILL_REWARD_ID.getPath(), () -> new QuestRegistries.RewardEntry(SkillReward::createReward));
        QuestRegistries.researchReward = DEFERRED_REGISTER_REWARD.register(RESEARCH_REWARD_ID.getPath(), () -> new QuestRegistries.RewardEntry(ResearchCompleteReward::createReward));
        QuestRegistries.raidReward = DEFERRED_REGISTER_REWARD.register(RAID_REWARD_ID.getPath(), () -> new QuestRegistries.RewardEntry(RaidAdjustmentReward::createReward));
        QuestRegistries.relationshipReward = DEFERRED_REGISTER_REWARD.register(RELATIONSHIP_REWARD_ID.getPath(), () -> new QuestRegistries.RewardEntry(RelationshipReward::createReward));
        QuestRegistries.happinessReward = DEFERRED_REGISTER_REWARD.register(HAPPINESS_REWARD_ID.getPath(), () -> new QuestRegistries.RewardEntry(HappinessReward::createReward));

        QuestRegistries.dialogueAnswerResult = DEFERRED_REGISTER_ANSWER_RESULT.register(DIALOGUE_ANSWER_ID.getPath(), () -> new QuestRegistries.DialogueAnswerEntry(IDialogueObjective.DialogueElement::parse));
        QuestRegistries.returnAnswerResult = DEFERRED_REGISTER_ANSWER_RESULT.register(RETURN_ANSWER_ID.getPath(), () -> new QuestRegistries.DialogueAnswerEntry(json -> new IAnswerResult.ReturnResult()));
        QuestRegistries.gotoAnswerResult = DEFERRED_REGISTER_ANSWER_RESULT.register(GOTO_ANSWER_ID.getPath(), () -> new QuestRegistries.DialogueAnswerEntry(IAnswerResult.GoToResult::new));
        QuestRegistries.cancelAnswerResult = DEFERRED_REGISTER_ANSWER_RESULT.register(CANCEL_ANSWER_ID.getPath(), () -> new QuestRegistries.DialogueAnswerEntry(json -> new IAnswerResult.CancelResult()));
    }
}
