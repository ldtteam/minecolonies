package com.minecolonies.coremod.colony.interactionhandling;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.ICitizen;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.interactionhandling.IChatPriority;
import com.minecolonies.api.quests.*;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIBasic;
import com.minecolonies.coremod.network.messages.server.colony.InteractionResponse;
import com.minecolonies.coremod.quests.objectives.DialogueObjective;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static com.minecolonies.api.colony.interactionhandling.ModInteractionResponseHandlers.QUEST_ACTION;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;

/**
 * A simple interaction which displays until an acceptable response is clicked
 */
public class QuestActionInteraction extends QuestDialogueInteraction
{
    /**
     * Two icon options.
     */
    private static final ResourceLocation QUEST_START_ICON = new ResourceLocation(Constants.MOD_ID, "textures/icons/queststart.png");
    private static final ResourceLocation QUEST_NEXT_TASK_ICON = new ResourceLocation(Constants.MOD_ID, "textures/icons/nexttask.png");
    private static final ResourceLocation QUEST_WAITING_TASK_ICON = new ResourceLocation(Constants.MOD_ID, "textures/icons/opentask.png");

    public QuestActionInteraction(final Component inquiry, final IChatPriority priority, final ResourceLocation location, final int index, final ICitizenData citizenData)
    {
        super(inquiry, priority, location, index, citizenData);
    }

    public QuestActionInteraction(final ICitizen data)
    {
        super(data);
    }

    @Override
    public void onServerResponseTriggered(final Component response, final Player player, final ICitizenData data)
    {
        final IQuestObjective objective = IQuestManager.GLOBAL_SERVER_QUESTS.get(questId).getObjective(index);
        triggerResponseState(player, objective);
        if (currentElement != null && colonyQuest != null)
        {
            final IAnswerResult result = this.currentElement.getOptionResult(response);
            if (result instanceof ITerminalAnswerResult)
            {
                if (result instanceof IResolveResult)
                {
                    if (((IQuestActionObjective) objective).isReady(player, colonyQuest) && ((IQuestActionObjective) objective).tryResolve(player, colonyQuest))
                    {
                        ((ITerminalAnswerResult) result).applyToQuest(player, data.getColony().getQuestManager().getAvailableOrInProgressQuest(questId));
                        finished = true;
                    }
                }
                else
                {
                    if (!(result instanceof IAnswerResult.ReturnResult))
                    {
                        currentElement = null;
                        data.getColony().markDirty();
                        return;
                    }
                }
                currentElement = startElement;
            }
            else if (result instanceof DialogueObjective.DialogueElement)
            {
                this.currentElement = (DialogueObjective.DialogueElement) result;
                if (data != null && data.getJob() != null)
                {
                    ((AbstractEntityAIBasic) data.getJob().getWorkerAI()).setDelay(TICKS_SECOND * 3);
                }
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean onClientResponseTriggered(final Component response, final Player player, final ICitizenDataView data, final BOWindow window)
    {
        if (colonyQuest == null)
        {
            colonyQuest = data.getColony().getQuestManager().getAvailableOrInProgressQuest(questId);
        }
        if (currentElement != null && colonyQuest != null)
        {
            final IAnswerResult result = this.currentElement.getOptionResult(response);
            if (result instanceof ITerminalAnswerResult)
            {
                Network.getNetwork().sendToServer(new InteractionResponse(data.getColonyId(), data.getId(), player.level.dimension(), Component.literal(colonyQuest.getId().toString()), response));
                this.currentElement = this.startElement;
                return true;
            }
            else if (result instanceof DialogueObjective.DialogueElement)
            {
                Network.getNetwork().sendToServer(new InteractionResponse(data.getColonyId(), data.getId(), player.level.dimension(), Component.literal(colonyQuest.getId().toString()), response));
                this.currentElement = (DialogueObjective.DialogueElement) result;
                return false;
            }
        }

        //+todo quest: QuestObjectives can have an ObjectiveData object that is stored in the colony quest. We only need this for the currently active objective.
        // This can store the necessary data. Delivery requests and dialogue quests have "EmptyData". The ready check might check on the data, or general on the player.

        // todo this data we can then also check for a dif icon.

        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onOpened(final Player player)
    {
        final IQuestObjective objective = IQuestManager.GLOBAL_SERVER_QUESTS.get(questId).getObjective(index);
        triggerResponseState(player, objective);
    }

    /**
     * Trigger response state.
     *
     * @param player    the player for.
     * @param objective the objective to check.
     */
    private void triggerResponseState(final Player player, final IQuestObjective objective)
    {
        if (objective instanceof IQuestActionObjective)
        {
            if (((IQuestActionObjective) objective).isReady(player, colonyQuest))
            {
                currentElement = ((IQuestActionObjective) objective).getReadyDialogueTree();
            }
            else
            {
                currentElement = ((IDialogueObjective) objective).getDialogueTree();
            }
        }
    }

    @Override
    public String getType()
    {
        return QUEST_ACTION.getPath();
    }

    @Override
    public ResourceLocation getInteractionIcon()
    {
        return index == 0 ? QUEST_START_ICON : QUEST_NEXT_TASK_ICON;
    }
}
