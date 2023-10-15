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
import com.minecolonies.coremod.quests.objectives.DialogueObjectiveTemplateTemplate;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static com.minecolonies.api.colony.interactionhandling.ModInteractionResponseHandlers.QUEST_ACTION;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;

/**
 * More specific quest dialogue interaction that also tries to fulfill a deliver request.
 */
public class QuestDeliveryInteraction extends QuestDialogueInteraction
{
    /**
     * Two icon options.
     */
    private static final ResourceLocation QUEST_START_ICON = new ResourceLocation(Constants.MOD_ID, "textures/icons/queststart.png");
    private static final ResourceLocation QUEST_NEXT_TASK_ICON = new ResourceLocation(Constants.MOD_ID, "textures/icons/nexttask.png");

    public QuestDeliveryInteraction(final Component inquiry, final IChatPriority priority, final ResourceLocation location, final int index, final ICitizenData citizenData)
    {
        super(inquiry, priority, location, index, citizenData);
    }

    public QuestDeliveryInteraction(final ICitizen data)
    {
        super(data);
    }

    @Override
    public void onServerResponseTriggered(final int responseId, final Player player, final ICitizenData data)
    {
        if (colonyQuest == null)
        {
            colonyQuest = data.getColony().getQuestManager().getAvailableOrInProgressQuest(questId);
        }
        final IQuestObjectiveTemplate objective = IQuestManager.GLOBAL_SERVER_QUESTS.get(questId).getObjective(index);
        triggerResponseState(player, objective);
        if (currentElement != null && colonyQuest != null)
        {
            final IQuestDialogueAnswer result = this.currentElement.getOptionResult(responseId);
            if (result instanceof IFinalQuestDialogueAnswer)
            {
                if (result instanceof IQuestPositiveDialogueAnswer)
                {
                    if (((IQuestDeliveryObjective) objective).hasItem(player, colonyQuest) && ((IQuestDeliveryObjective) objective).tryDiscountItem(player, colonyQuest))
                    {
                        ((IFinalQuestDialogueAnswer) result).applyToQuest(player, colonyQuest);
                        finished = true;
                    }
                }
                else
                {
                    if (!(result instanceof IQuestDialogueAnswer.CloseUIDialogueAnswer))
                    {
                        currentElement = null;
                        data.getColony().markDirty();
                        return;
                    }
                }
                currentElement = startElement;
            }
            else if (result instanceof DialogueObjectiveTemplateTemplate.DialogueElement)
            {
                this.currentElement = (DialogueObjectiveTemplateTemplate.DialogueElement) result;
                if (data != null && data.getJob() != null)
                {
                    ((AbstractEntityAIBasic) data.getJob().getWorkerAI()).setDelay(TICKS_SECOND * 3);
                }
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean onClientResponseTriggered(final int responseId, final Player player, final ICitizenDataView data, final BOWindow window)
    {
        if (colonyQuest == null)
        {
            colonyQuest = data.getColony().getQuestManager().getAvailableOrInProgressQuest(questId);
        }
        if (currentElement != null && colonyQuest != null)
        {
            final IQuestDialogueAnswer result = this.currentElement.getOptionResult(responseId);
            if (result instanceof IFinalQuestDialogueAnswer)
            {
                Network.getNetwork().sendToServer(new InteractionResponse(data.getColonyId(), data.getId(), player.level.dimension(), Component.literal(colonyQuest.getId().toString()), responseId));
                this.currentElement = this.startElement;
                return true;
            }
            else if (result instanceof DialogueObjectiveTemplateTemplate.DialogueElement)
            {
                Network.getNetwork().sendToServer(new InteractionResponse(data.getColonyId(), data.getId(), player.level.dimension(), Component.literal(colonyQuest.getId().toString()), responseId));
                this.currentElement = (DialogueObjectiveTemplateTemplate.DialogueElement) result;
                return false;
            }
        }

        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onOpened(final Player player)
    {
        final IQuestObjectiveTemplate objective = IQuestManager.GLOBAL_SERVER_QUESTS.get(questId).getObjective(index);
        triggerResponseState(player, objective);
    }

    /**
     * Trigger response state.
     *
     * @param player    the player for.
     * @param objective the objective to check.
     */
    private void triggerResponseState(final Player player, final IQuestObjectiveTemplate objective)
    {
        if (objective instanceof final IQuestDeliveryObjective delivery)
        {
            if (delivery.hasItem(player, colonyQuest))
            {
                currentElement = delivery.getReadyDialogueTree();
            }
            else
            {
                currentElement = delivery.getDialogueTree();
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
