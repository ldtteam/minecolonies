package com.minecolonies.coremod.colony.interactionhandling;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.ICitizen;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.interactionhandling.IChatPriority;
import com.minecolonies.api.quests.IQuestManager;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.network.messages.server.colony.InteractionResponse;
import com.minecolonies.coremod.quests.objectives.DialogueObjective;
import com.minecolonies.coremod.quests.objectives.IAnswerResult;
import com.minecolonies.coremod.quests.objectives.ITerminalAnswerResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.colony.interactionhandling.ModInteractionResponseHandlers.QUEST;
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
    private static final ResourceLocation QUEST_GIVER_ICON = new ResourceLocation(Constants.MOD_ID, "textures/icons/questgiver.png");
    private static final ResourceLocation QUEST_TARGET_ICON = new ResourceLocation(Constants.MOD_ID, "textures/icons/questtarget.png");

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
    private DialogueObjective.DialogueElement currentElement = null;

    public QuestInteraction(final Component inquiry, final IChatPriority priority, final ResourceLocation location, final int index)
    {
        super(inquiry, null, priority);
        this.questId = location;
        this.index = index;
        this.currentElement = ((DialogueObjective) IQuestManager.GLOBAL_SERVER_QUESTS.get(questId).getObjective(index)).getDialogueTree();
    }

    public QuestInteraction(final ICitizen data)
    {
        super(data);
    }

    @Override
    public void onServerResponseTriggered(final Component response, final Player player, final ICitizenData data)
    {
        if (currentElement != null)
        {
            final IAnswerResult result = this.currentElement.getOptionResult(response);
            if (result instanceof DialogueObjective.DialogueElement)
            {
                this.currentElement = (DialogueObjective.DialogueElement) result;
            }
            else if (result instanceof ITerminalAnswerResult)
            {
                ((ITerminalAnswerResult) result).applyToQuest(data.getColony().getQuestManager().getAvailableOrInProgressQuest(questId));
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
            if (result instanceof DialogueObjective.DialogueElement)
            {
                this.currentElement = (DialogueObjective.DialogueElement) result;
                Network.getNetwork().sendToServer(new InteractionResponse(data.getColonyId(), data.getId(), player.level.dimension(), this.getInquiry(), response));
                return false;
            }
            Network.getNetwork().sendToServer(new InteractionResponse(data.getColonyId(), data.getId(), player.level.dimension(), this.getInquiry(), response));
        }
        //todo quest: Advancing objective needs to disable this directly and potentially add a new interaction for the next objective to the next citizen.
        // Generally objectives should have an "on reach" which does the interaction assignment.

        //todo quest: we want to replace placerholders like $0 with the respective citizen with the right index from the quest $0 = giver, $1 is first element in participant
        //todo quest: On cancel action or on ui close (needs new event) we want to restore this here. We can detect the return one

        //todo quest: Delivery objective. Its a dialogue objective with "Did you bring x?". On "yes here you here you are", we try deducting,
        // if unsuccessful, we open other interaction with he?


        //todo quest: make all objectives dialogue objectives where the citizen will ask for the task completeness. Like "did you do this already??".
        // And when finished they advance anyway to the correct one =D

        return true;
    }

    @Override
    public Component getInquiry()
    {
        return currentElement.getText();
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
    }

    @Override
    public String getType()
    {
        return QUEST.getPath();
    }

    @Override
    public ResourceLocation getInteractionIcon()
    {
        return index == 0 ? QUEST_GIVER_ICON : QUEST_TARGET_ICON;
    }

    @Override
    public boolean isValid(final ICitizenData citizen)
    {
        return currentElement != null && citizen.hasQuestOpen(questId) && citizen.getColony().getQuestManager().getAvailableOrInProgressQuest(questId).getIndex() == index;
    }
}
