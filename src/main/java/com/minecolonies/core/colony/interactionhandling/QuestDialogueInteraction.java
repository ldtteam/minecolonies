package com.minecolonies.core.colony.interactionhandling;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.ICitizen;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.interactionhandling.IChatPriority;
import com.minecolonies.api.quests.IFinalQuestDialogueAnswer;
import com.minecolonies.api.quests.IQuestDialogueAnswer;
import com.minecolonies.api.quests.IQuestInstance;
import com.minecolonies.api.quests.IQuestManager;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.Network;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIBasic;
import com.minecolonies.core.network.messages.server.colony.InteractionResponse;
import com.minecolonies.core.quests.objectives.DialogueObjectiveTemplateTemplate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.minecolonies.api.colony.interactionhandling.ModInteractionResponseHandlers.QUEST;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * A simple quest dialogue interaction that deals with different dialogue trees.
 */
public class QuestDialogueInteraction extends StandardInteraction
{
    /**
     * Three icon options.
     */
    private static final ResourceLocation QUEST_START_ICON = new ResourceLocation(Constants.MOD_ID, "textures/icons/queststart.png");
    private static final ResourceLocation QUEST_NEXT_TASK_ICON = new ResourceLocation(Constants.MOD_ID, "textures/icons/nexttask.png");
    private static final ResourceLocation QUEST_WAITING_TASK_ICON = new ResourceLocation(Constants.MOD_ID, "textures/icons/opentask.png");

    /**
     * Currently open colony quest.
     */
    protected IQuestInstance colonyQuest;

    /**
     * The respective citizen.
     */
    protected final ICitizen citizen;

    /**
     * The quest resource location.
     */
    protected ResourceLocation questId;

    /**
     * Index in the quest.
     */
    protected int index;

    /**
     * The current dialogue element.
     */
    protected DialogueObjectiveTemplateTemplate.DialogueElement startElement = null;

    /**
     * The current dialogue element.
     */
    protected DialogueObjectiveTemplateTemplate.DialogueElement currentElement = null;

    /**
     * Some finished flag to make it disappear more quickly on the client side.
     */
    protected boolean finished = false;

    public QuestDialogueInteraction(final Component inquiry, final IChatPriority priority, final ResourceLocation location, final int index, final ICitizenData citizenData)
    {
        super(inquiry, null, priority);
        this.questId = location;
        this.index = index;
        this.currentElement = ((DialogueObjectiveTemplateTemplate) IQuestManager.GLOBAL_SERVER_QUESTS.get(questId).getObjective(index)).getDialogueTree();
        this.startElement = currentElement;
        this.colonyQuest = citizenData.getColony().getQuestManager().getAvailableOrInProgressQuest(questId);
        this.citizen = citizenData;
    }

    public QuestDialogueInteraction(final ICitizen data)
    {
        super(data);
        this.colonyQuest = data.getColony().getQuestManager().getAvailableOrInProgressQuest(questId);
        this.citizen = data;
    }

    @Override
    public void onServerResponseTriggered(final int responseId, final Player player, final ICitizenData data)
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
                ((IFinalQuestDialogueAnswer) result).applyToQuest(player, data.getColony().getQuestManager().getAvailableOrInProgressQuest(questId));
                if (!(result instanceof IQuestDialogueAnswer.CloseUIDialogueAnswer))
                {
                    finished = true;
                    currentElement = null;
                    data.getColony().markDirty();
                    return;
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
                Network.getNetwork().sendToServer(new InteractionResponse(data.getColonyId(), data.getId(), player.level().dimension(), Component.literal(questId.toString()), responseId));
                this.currentElement = this.startElement;
                finished = true;
                return true;
            }
            else if (result instanceof DialogueObjectiveTemplateTemplate.DialogueElement)
            {
                Network.getNetwork().sendToServer(new InteractionResponse(data.getColonyId(), data.getId(), player.level().dimension(), Component.literal(questId.toString()), responseId));
                this.currentElement = (DialogueObjectiveTemplateTemplate.DialogueElement) result;
                return false;
            }
        }

        return true;
    }

    @Override
    public void onOpened(final Player player)
    {
        super.onOpened(player);
        if (colonyQuest == null && citizen != null)
        {
            colonyQuest = citizen.getColony().getQuestManager().getAvailableOrInProgressQuest(questId);
        }
    }

    @Override
    public Component getId()
    {
        return Component.literal(this.questId.toString());
    }

    @Override
    public void onClosed()
    {
        this.currentElement = this.startElement;
    }

    @Override
    public Component getInquiry()
    {
        return processText(currentElement.getText());
    }

    /**
     * Process the text to include the participant names.
     * @return the processed text.
     */
    private Component processText(final Component text)
    {
        // TODO: this is not ideal, we should do something more clever and preserve the item subcomponents for tooltips etc
        String localText = text.getString();
        if (localText.contains("$") && colonyQuest != null)
        {
            localText = localText.replace("$0", citizen.getColony().getCitizen(this.colonyQuest.getQuestGiverId()).getName());
            int index = 1;
            for (final int participant : this.colonyQuest.getParticipants())
            {
                localText = localText.replace("$" + index, citizen.getColony().getCitizen(participant).getName());
            }
        }
        if (localText.contains("$d") && colonyQuest != null && colonyQuest.getCurrentObjectiveInstance() != null)
        {
            localText = localText.replace("$d", String.valueOf(colonyQuest.getCurrentObjectiveInstance().getMissingQuantity()));
        }
        return Component.literal(localText);
    }

    @Override
    public boolean isVisible(final Level world)
    {
        return !finished;
    }

    @Override
    public List<Component> getPossibleResponses()
    {
        return currentElement == null ? Collections.emptyList() : currentElement.getOptions().stream().map(this::processText).collect(Collectors.toList());
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag tag = super.serializeNBT();
        tag.putString(TAG_QUEST_ID, questId.toString());
        tag.putInt(TAG_QUEST_INDEX, index);
        tag.putBoolean(TAG_FINISHED, finished);
        return tag;
    }

    @Override
    public void deserializeNBT(final @NotNull CompoundTag compoundNBT)
    {
        super.deserializeNBT(compoundNBT);
        this.questId = new ResourceLocation(compoundNBT.getString(TAG_QUEST_ID));
        this.index = compoundNBT.getInt(TAG_QUEST_INDEX);
        this.currentElement = ((DialogueObjectiveTemplateTemplate) IQuestManager.GLOBAL_SERVER_QUESTS.get(questId).getObjective(index)).getDialogueTree();
        this.startElement = currentElement;
        this.finished = compoundNBT.getBoolean(TAG_FINISHED);
    }

    @Override
    public String getType()
    {
        return QUEST.getPath();
    }

    @Override
    public ResourceLocation getInteractionIcon()
    {
        if (colonyQuest == null)
        {
            colonyQuest = citizen.getColony().getQuestManager().getAvailableOrInProgressQuest(questId);
        }

        if (colonyQuest != null && colonyQuest.getCurrentObjectiveInstance() != null && !colonyQuest.getCurrentObjectiveInstance().isFulfilled())
        {
            return QUEST_WAITING_TASK_ICON;
        }
        return index == 0 ? QUEST_START_ICON : QUEST_NEXT_TASK_ICON;
    }

    @Override
    public boolean isValid(final ICitizenData citizen)
    {
        return currentElement != null && citizen.isParticipantOfQuest(questId) && citizen.getColony().getQuestManager().getAvailableOrInProgressQuest(questId) != null && citizen.getColony().getQuestManager().getAvailableOrInProgressQuest(questId).getObjectiveIndex() == index;
    }
}
