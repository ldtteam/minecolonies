package com.minecolonies.core.quests;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.quests.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Instance of a specific quest type
 */
public class QuestInstance implements IQuestInstance
{
    /**
     * Colony reference
     */
    private final IColony colony;

    /**
     * The id of this quest
     */
    private ResourceLocation questTemplateID;

    /**
     * Quest giver.
     */
    private int questGiver = Integer.MIN_VALUE;

    /**
     * Quest participants.
     */
    private final List<Integer> questParticipants = new ArrayList<>();

    /**
     * The day the quest was assigned to the colony.
     */
    private int assignmentStart;

    /**
     * Current objective progress.
     */
    private int objectiveProgress = 0;

    /**
     * Tracking data of the currently active objective.
     */
    private IObjectiveInstance currentObjectiveInstance = null;

    /**
     * The player that accepted this.
     */
    private UUID assignedPlayer = null;

    /**
     * Create a new colony quest.
     * @param questTemplateID the global id of the quest.
     * @param colony the colony it belongs to.
     * @param triggerReturnData the trigger return data that made this quest available.
     */
    public QuestInstance(final ResourceLocation questTemplateID, final IColony colony, final List<ITriggerReturnData<?>> triggerReturnData)
    {
        this.colony = colony;
        this.questTemplateID = questTemplateID;
        this.assignmentStart = colony.getDay();
        questParticipants.clear();

        for (final ITriggerReturnData<?> data : triggerReturnData)
        {
            if (data.getContent() instanceof IQuestGiver && questGiver == Integer.MIN_VALUE)
            {
                questGiver = ((ICitizenData) data.getContent()).getId();
                ((ICitizenData) data.getContent()).assignQuest(this);
            }
            else if (data.getContent() instanceof IQuestParticipant)
            {
                questParticipants.add(((ICitizenData) data.getContent()).getId());
                ((ICitizenData) data.getContent()).addQuestParticipation(this);
            }
        }
        IQuestManager.GLOBAL_SERVER_QUESTS.get(questTemplateID).getObjective(this.objectiveProgress).startObjective(this);
    }

    /**
     * Create an empty colony quest obj to deserialize the data from.
     * @param colony the colony it is assigned to.
     */
    public QuestInstance(final IColony colony)
    {
        this.colony = colony;
    }

    public IColony getColony()
    {
        return colony;
    }

    @Override
    public UUID getAssignedPlayer()
    {
        return assignedPlayer;
    }

    @Override
    public void onStart(final Player player, final IColony colony)
    {
        // Reset quest timeout on acceptance.
        assignmentStart = colony.getDay();
        assignedPlayer = player.getUUID();
        colony.getQuestManager().attemptAcceptQuest(this.getId(), player);
        // activeEffects.addAll(type.createEffectsFor(this));
    }

    @Override
    public IQuestGiver getQuestGiver()
    {
        return colony.getCitizenManager().getCivilian(questGiver);
    }

    @Override
    public int getQuestGiverId()
    {
        return questGiver;
    }

    @Override
    public boolean isValid(final IColony colony)
    {
        if (questGiver == Integer.MIN_VALUE || colony.getCitizenManager().getCivilian(questGiver) == null )
        {
            return false;
        }

        for (final int participant : questParticipants)
        {
            if (colony.getCitizenManager().getCivilian(participant) == null)
            {
                return false;
            }
        }
        return colony.getDay() - assignmentStart < IQuestManager.GLOBAL_SERVER_QUESTS.get(questTemplateID).getQuestTimeout();
    }

    @Override
    public ResourceLocation getId()
    {
        return questTemplateID;
    }

    @Override
    public void onDeletion()
    {
        if (questGiver != Integer.MIN_VALUE && colony.getCitizenManager().getCivilian(questGiver) != null)
        {
            colony.getCitizenManager().getCivilian(questGiver).onQuestDeletion(this.getId());
        }

        for (final int participant : questParticipants)
        {
            if (colony.getCitizenManager().getCivilian(participant) != null)
            {
                colony.getCitizenManager().getCivilian(participant).onQuestDeletion(this.getId());
            }
        }

        final IQuestTemplate instance = IQuestManager.GLOBAL_SERVER_QUESTS.get(questTemplateID);
        if (instance != null && instance.getObjective(this.objectiveProgress) != null)
        {
            instance.getObjective(this.objectiveProgress).onCancellation(this);
        }
        colony.getQuestManager().deleteQuest(this.questTemplateID);
    }

    @Override
    public void advanceObjective(final Player player)
    {
        this.advanceObjective(player, this.objectiveProgress + 1);
    }

    @Override
    public void onWorldLoad()
    {
        IQuestManager.GLOBAL_SERVER_QUESTS.get(questTemplateID).getObjective(this.objectiveProgress).onWorldLoad(this);
    }

    @Override
    public IObjectiveInstance advanceObjective(final Player player, final int nextObjective)
    {
        final IQuestTemplate questData = IQuestManager.GLOBAL_SERVER_QUESTS.get(questTemplateID);
        final IQuestObjectiveTemplate questObjectiveTemplate = questData.getObjective(this.objectiveProgress);

        // Always when advancing an objective, get the rewards from the current objective.
        final List<Integer> rewards = questObjectiveTemplate.getRewardUnlocks();
        if(!rewards.isEmpty())
        {
            questData.unlockQuestRewards(colony, player, this, rewards);
        }

        colony.markDirty();
        if (nextObjective == -1)
        {
            this.onCompletion();
            return null;
        }

        if (this.objectiveProgress == 0)
        {
            this.onStart(player, getColony());
        }
        this.objectiveProgress = nextObjective;
        if (this.objectiveProgress >= questData.getObjectiveCount())
        {
            this.onCompletion();
            return null;
        }
        currentObjectiveInstance = questData.getObjective(this.objectiveProgress).startObjective(this);
        return currentObjectiveInstance;
    }

    @Override
    public void onCompletion()
    {
        colony.getQuestManager().completeQuest(this.getId());
        final ICitizenData questGiverData = colony.getCitizenManager().getCivilian(questGiver);
        if (questGiverData != null)
        {
            questGiverData.onQuestCompletion(this.questTemplateID);
        }

        for (final int partId : questParticipants)
        {
            final ICitizenData partData = colony.getCitizenManager().getCivilian(partId);
            if (partData != null)
            {
                partData.onQuestCompletion(this.questTemplateID);
            }
        }

        if (assignedPlayer != null)
        {
            final Player player = colony.getWorld().getPlayerByUUID(assignedPlayer);
            if (player != null)
            {
                final IQuestTemplate questData = IQuestManager.GLOBAL_SERVER_QUESTS.get(questTemplateID);
                player.sendSystemMessage(Component.translatableEscape("com.minecolonies.coremod.quest.completed", questData.getName()));
            }
        }
    }

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider)
    {
        final CompoundTag compoundNBT = new CompoundTag();
        compoundNBT.putString(TAG_ID, questTemplateID.toString());
        compoundNBT.putInt(TAG_ASSIGN_START, assignmentStart);
        compoundNBT.putInt(TAG_PROGRESS, objectiveProgress);
        compoundNBT.putInt(TAG_QUEST_GIVER, questGiver);
        final ListTag participantList = new ListTag();
        for (final int citizenData : this.questParticipants)
        {
            participantList.add(IntTag.valueOf(citizenData));
        }
        compoundNBT.put(TAG_PARTICIPANTS, participantList);

        if (currentObjectiveInstance != null)
        {
            compoundNBT.put(TAG_OBJECTIVE, this.currentObjectiveInstance.serializeNBT(provider));
        }

        if (assignedPlayer != null)
        {
            compoundNBT.putUUID(TAG_PLAYER, assignedPlayer);
        }

        return compoundNBT;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, final CompoundTag nbt)
    {
        questTemplateID = ResourceLocation.parse(nbt.getString(TAG_ID));
        assignmentStart = nbt.getInt(TAG_ASSIGN_START);
        objectiveProgress = nbt.getInt(TAG_PROGRESS);
        questGiver = nbt.getInt(TAG_QUEST_GIVER);

        final ListTag participantList = nbt.getList(TAG_PARTICIPANTS, Tag.TAG_INT);
        for (final Tag tag : participantList)
        {
            questParticipants.add(((IntTag) tag).getAsInt());
        }

        if (nbt.contains(TAG_OBJECTIVE))
        {
            final IObjectiveInstance data = IQuestManager.GLOBAL_SERVER_QUESTS.get(questTemplateID).getObjective(objectiveProgress).createObjectiveInstance();
            data.deserializeNBT(provider, nbt.getCompound(TAG_OBJECTIVE));
            this.currentObjectiveInstance = data;
        }

        if (nbt.contains(TAG_PLAYER))
        {
            assignedPlayer = nbt.getUUID(TAG_PLAYER);
        }
    }

    @Override
    public int getObjectiveIndex()
    {
        return objectiveProgress;
    }

    @Override
    public IQuestParticipant getParticipant(final int target)
    {
        return colony.getCitizenManager().getCivilian(questParticipants.get(target-1));
    }

    @Override
    public List<Integer> getParticipants()
    {
        return questParticipants;
    }

    @Override
    public int getQuestTarget()
    {
        final IQuestObjectiveTemplate objective = QuestManager.GLOBAL_SERVER_QUESTS.get(questTemplateID).getObjective(getObjectiveIndex());
        if (objective == null)
        {
            return getQuestGiverId();
        }
        final int target = objective.getTarget();
        if (target == 0)
        {
            return getQuestGiverId();
        }
        if (target <= questParticipants.size())
        {
            return questParticipants.get(target - 1);
        }
        return getQuestGiverId();
    }

    @Override
    @Nullable
    public IObjectiveInstance getCurrentObjectiveInstance()
    {
        return currentObjectiveInstance;
    }
}
