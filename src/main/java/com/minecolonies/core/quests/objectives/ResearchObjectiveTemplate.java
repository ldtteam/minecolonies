package com.minecolonies.core.quests.objectives;

import com.google.gson.JsonObject;
import com.minecolonies.api.quests.IObjectiveInstance;
import com.minecolonies.api.quests.IQuestDialogueAnswer;
import com.minecolonies.api.quests.IQuestInstance;
import com.minecolonies.api.quests.IQuestObjectiveTemplate;
import com.minecolonies.api.research.IGlobalResearch;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.core.colony.Colony;
import com.minecolonies.core.event.QuestObjectiveEventHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.quests.QuestParseConstant.*;

/**
 * Objective type tracking research.
 */
public class ResearchObjectiveTemplate extends DialogueObjectiveTemplateTemplate implements IResearchObjectiveTemplate
{
    /**
     * The research to execute.
     */
    private final ResourceLocation researchId;

    /**
     * Next objective to go to, on fulfillment. -1 if final objective.
     */
    private final int nextObjective;

    /**
     * Create a new objective of this type.
     *
     * @param target        the target citizen.
     * @param rewards       the rewards this unlocks.
     */
    public ResearchObjectiveTemplate(
      final int target,
      final ResourceLocation researchId,
      final int nextObjective,
      final List<Integer> rewards)
    {
        super(target, buildDialogueTree(researchId), rewards);
        this.researchId = researchId;
        this.nextObjective = nextObjective;
    }

    @NotNull
    private static DialogueElement buildDialogueTree(final ResourceLocation researchId)
    {
        final IGlobalResearch research = IGlobalResearchTree.getInstance().getResearch(researchId);

        final Component text  = Component.translatable("com.minecolonies.coremod.questobjectives.research", MutableComponent.create((research.getName())));

        final AnswerElement answer1 = new AnswerElement(Component.translatable("com.minecolonies.coremod.questobjectives.answer.later"),
          new IQuestDialogueAnswer.CloseUIDialogueAnswer());
        final AnswerElement answer2 = new AnswerElement(Component.translatable("com.minecolonies.coremod.questobjectives.answer.cancel"),
          new IQuestDialogueAnswer.QuestCancellationDialogueAnswer());
        return new DialogueElement(text, List.of(answer1, answer2));
    }

    /**
     * Parse the build research objective from json.
     *
     * @param jsonObject the json to parse it from.
     * @return a new objective object.
     */
    public static IQuestObjectiveTemplate createObjective(final JsonObject jsonObject)
    {
        JsonObject details = jsonObject.getAsJsonObject(DETAILS_KEY);

        final int target = details.get(TARGET_KEY).getAsInt();
        final ResourceLocation researchId = new ResourceLocation(details.get(BUILDING_KEY).getAsString());

        final int nextObj = details.has(NEXT_OBJ_KEY) ? details.get(NEXT_OBJ_KEY).getAsInt() : -1;

        return new ResearchObjectiveTemplate(target, researchId, nextObj, parseRewards(jsonObject));
    }

    @Override
    public IObjectiveInstance startObjective(final IQuestInstance colonyQuest)
    {
        super.startObjective(colonyQuest);

        if (colonyQuest.getColony() instanceof Colony colony)
        {
            if (colony.getResearchManager().getResearchTree().isComplete(this.researchId))
            {
                return colonyQuest.advanceObjective(colonyQuest.getColony().getWorld().getPlayerByUUID(colonyQuest.getAssignedPlayer()), nextObjective);
            }

            QuestObjectiveEventHandler.trackResearch(this.researchId, colonyQuest);
        }

        return null;
    }

    /**
     * Advances the quest if finished, does the necessary cleanup as well.
     *
     * @param colonyQuest the quest instance.
     * @return the next quest or null if not finished.
     */
    private boolean advanceIfFinished(final IQuestInstance colonyQuest)
    {
        if (colonyQuest.getColony() instanceof Colony colony && colony.getResearchManager().getResearchTree().isComplete(this.researchId))
        {
            cleanupListener(colonyQuest);
            colonyQuest.advanceObjective(colonyQuest.getColony().getWorld().getPlayerByUUID(colonyQuest.getAssignedPlayer()), nextObjective);
            return true;
        }
        return false;
    }

    /**
     * Cleanup the listener of this objective,
     *
     * @param colonyQuest the listener.
     */
    private void cleanupListener(final IQuestInstance colonyQuest)
    {
        if (colonyQuest.getColony() instanceof Colony)
        {
            // Only serverside cleanup.
            QuestObjectiveEventHandler.stopTrackingResearch(this.researchId, colonyQuest);
        }
    }

    @Override
    public Component getProgressText(final IQuestInstance quest, final Style style)
    {
        final IGlobalResearch research = IGlobalResearchTree.getInstance().getResearch(this.researchId);

        return Component.translatable("com.minecolonies.coremod.questobjectives.research.progress", MutableComponent.create(research.getName()));
    }

    @Override
    public void onCancellation(final IQuestInstance colonyQuest)
    {
        cleanupListener(colonyQuest);
    }

    @Override
    public void onWorldLoad(final IQuestInstance colonyQuest)
    {
        super.onWorldLoad(colonyQuest);
        if (colonyQuest.getColony() instanceof Colony colony && !advanceIfFinished(colonyQuest))
        {
            QuestObjectiveEventHandler.trackResearch(this.researchId, colonyQuest);
        }
    }

    @Override
    public void onResearchCompletion(final IQuestInstance colonyQuest)
    {
        cleanupListener(colonyQuest);
        colonyQuest.advanceObjective(colonyQuest.getColony().getWorld().getPlayerByUUID(colonyQuest.getAssignedPlayer()), nextObjective);
    }
}
