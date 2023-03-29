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
import com.mojang.authlib.minecraft.TelemetrySession;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.colony.interactionhandling.ModInteractionResponseHandlers.QUEST;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_QUEST_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_QUEST_INDEX;

/**
 * A simple interaction which displays until an acceptable response is clicked
 */
public class QuestInteraction extends StandardInteraction
{
    /**
     * Two icon options.
     */
    private static final ResourceLocation QUEST_START_ICON = new ResourceLocation(Constants.MOD_ID, "textures/icons/queststart.png");
    private static final ResourceLocation QUEST_NEXT_TASK_ICON = new ResourceLocation(Constants.MOD_ID, "textures/icons/nexttask.png");
    private static final ResourceLocation QUEST_WAITING_TASK_ICON = new ResourceLocation(Constants.MOD_ID, "textures/icons/opentask.png");

    /**
     * Currently open colony quest.
     */
    private final IColonyQuest colonyQuest;

    /**
     * The respective citizen.
     */
    private final ICitizen citizen;

    /**
     * The quest resource location.
     */
    private ResourceLocation questId;

    /**
     * Index in the quest.
     */
    private int index;

    /**
     * The current dialogue element.
     */
    private DialogueObjective.DialogueElement startElement = null;

    /**
     * The current dialogue element.
     */
    private DialogueObjective.DialogueElement currentElement = null;

    public QuestInteraction(final Component inquiry, final IChatPriority priority, final ResourceLocation location, final int index, final ICitizenData citizenData)
    {
        super(inquiry, null, priority);
        this.questId = location;
        this.index = index;
        this.currentElement = ((DialogueObjective) IQuestManager.GLOBAL_SERVER_QUESTS.get(questId).getObjective(index)).getDialogueTree();
        this.startElement = currentElement;
        this.colonyQuest = citizenData.getColony().getQuestManager().getAvailableOrInProgressQuest(questId);
        this.citizen = citizenData;
    }

    public QuestInteraction(final ICitizen data)
    {
        super(data);
        this.colonyQuest = data.getColony().getQuestManager().getAvailableOrInProgressQuest(questId);
        this.citizen = data;
    }

    @Override
    public void onServerResponseTriggered(final Component response, final Player player, final ICitizenData data)
    {
        triggerResponseState(player);
        if (currentElement != null)
        {
            final IAnswerResult result = this.currentElement.getOptionResult(response);
            if (result instanceof ITerminalAnswerResult)
            {
                ((ITerminalAnswerResult) result).applyToQuest(player, data.getColony().getQuestManager().getAvailableOrInProgressQuest(questId));
                data.getColony().markDirty();
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
        if (currentElement != null)
        {
            final IAnswerResult result = this.currentElement.getOptionResult(response);
            if (result instanceof ITerminalAnswerResult)
            {
                Network.getNetwork().sendToServer(new InteractionResponse(data.getColonyId(), data.getId(), player.level.dimension(), Component.literal(startElement.getText()), response));
                this.currentElement = this.startElement;
                return true;
            }
            else if (result instanceof DialogueObjective.DialogueElement)
            {
                Network.getNetwork().sendToServer(new InteractionResponse(data.getColonyId(), data.getId(), player.level.dimension(), Component.literal(startElement.getText()), response));
                this.currentElement = (DialogueObjective.DialogueElement) result;
                return false;
            }
        }
        //todo, instant shut down this interaction, kill it with fire when advance or index ++ somehow.

        //todo quest: QuestObjectives can have an ObjectiveData object that is stored in the colony quest. We only need this for the currently active objective.
        // This can store the necessary data. Delivery requests and dialogue quests have "EmptyData". The ready check might check on the data, or general on the player.

        return true;
    }

    @Override
    public void onClosed()
    {
        this.currentElement = this.startElement;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onOpened(final Player player)
    {
        triggerResponseState(player);
    }

    /**
     * Trigger response state.
     * @param player the player for.
     */
    private void triggerResponseState(final Player player)
    {
        final IQuestObjective objective = IQuestManager.GLOBAL_SERVER_QUESTS.get(questId).getObjective(index);
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
    public Component getInquiry()
    {
        return Component.literal(processText(currentElement.getText()));
    }

    /**
     * Process the text to include the participant names.
     * @return the processed text.
     */
    private String processText(final String text)
    {
        String localText = text;
        if (localText.contains("$"))
        {
            localText = localText.replace("$0", citizen.getColony().getCitizen(this.colonyQuest.getQuestGiverId()).getName());
            int index = 1;
            for (final int participant : this.colonyQuest.getParticipants())
            {
                localText = localText.replace("$" + index, citizen.getColony().getCitizen(participant).getName());
            }
            this.colonyQuest.getQuestGiver();
        }
        return localText;
    }

    @Override
    public List<Component> getPossibleResponses()
    {
        return currentElement.getOptions();
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag tag = super.serializeNBT();
        tag.putString(TAG_QUEST_ID, questId.toString());
        tag.putInt(TAG_QUEST_INDEX, index);

        return tag;
    }

    @Override
    public void deserializeNBT(final @NotNull CompoundTag compoundNBT)
    {
        super.deserializeNBT(compoundNBT);
        this.questId = new ResourceLocation(compoundNBT.getString(TAG_QUEST_ID));
        this.index = compoundNBT.getInt(TAG_QUEST_INDEX);
        this.currentElement = ((DialogueObjective) IQuestManager.GLOBAL_SERVER_QUESTS.get(questId).getObjective(index)).getDialogueTree();
        this.startElement = currentElement;
    }

    @Override
    public String getType()
    {
        return QUEST.getPath();
    }

    @Override
    public ResourceLocation getInteractionIcon()
    {
        return index == 0 ? QUEST_START_ICON : QUEST_NEXT_TASK_ICON;
    }

    @Override
    public boolean isValid(final ICitizenData citizen)
    {
        return currentElement != null && citizen.hasQuestOpen(questId) && citizen.getColony().getQuestManager().getAvailableOrInProgressQuest(questId).getIndex() == index;
    }
}
