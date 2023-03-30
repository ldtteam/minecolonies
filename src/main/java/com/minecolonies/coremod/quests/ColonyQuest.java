package com.minecolonies.coremod.quests;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.quests.*;
import com.minecolonies.coremod.quests.triggers.ITriggerReturnData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Instance of a specific quest type
 */
public class ColonyQuest implements IColonyQuest
{
    /**
     * Colony reference
     */
    private final IColony colony;

    /**
     * The id of this quest
     */
    private ResourceLocation questID;

    /**
     * Quest giver.
     */
    private int questGiver = Integer.MIN_VALUE;

    /**
     * Quest giver.
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
    //private ObjectiveData objectiveData;

    /**
     * Create a new colony quest.
     * @param questID the global id of the quest.
     * @param colony the colony it belongs to.
     * @param triggerReturnData the trigger return data that made this quest available.
     */
    protected ColonyQuest(final ResourceLocation questID, final IColony colony, final List<ITriggerReturnData> triggerReturnData)
    {
        this.colony = colony;
        this.questID = questID;
        this.assignmentStart = colony.getDay();

        for (final ITriggerReturnData data : triggerReturnData)
        {
            if (data.get() instanceof IQuestGiver && questGiver == Integer.MIN_VALUE)
            {
                questGiver = ((ICitizenData) data.get()).getId();
                ((ICitizenData) data.get()).assignQuest(this);
            }
            else if (data.get() instanceof IQuestParticipant)
            {
                questParticipants.add(((ICitizenData) data.get()).getId());
                ((ICitizenData) data.get()).addQuestParticipation(this);
            }
        }
        IQuestManager.GLOBAL_SERVER_QUESTS.get(questID).getObjective(this.objectiveProgress).init(this);
    }

    /**
     * Create an empty colony quest obj to deserialize the data from.
     * @param colony the colony it is assigned to.
     */
    public ColonyQuest(final IColony colony)
    {
        this.colony = colony;
    }

    public IColony getColony()
    {
        return colony;
    }

    @Override
    public void onStart(final Player player, final IColony colony)
    {
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
        if (questGiver == Integer.MIN_VALUE || !colony.getCitizenManager().getCivilian(questGiver).isAlive() )
        {
            return false;
        }

        for (final int participant : questParticipants)
        {
            if (!colony.getCitizenManager().getCivilian(participant).isAlive())
            {
                return false;
            }
        }
        return colony.getDay() - assignmentStart < IQuestManager.GLOBAL_SERVER_QUESTS.get(questID).getQuestTimeout();
    }

    @Override
    public ResourceLocation getId()
    {
        return questID;
    }

    @Override
    public void onDeletion()
    {
        if (questGiver != Integer.MIN_VALUE)
        {
            colony.getCitizenManager().getCivilian(questGiver).onQuestDeletion(this.getId());
        }

        for (final int participant : questParticipants)
        {
            colony.getCitizenManager().getCivilian(participant).onQuestDeletion(this.getId());
        }

        colony.getQuestManager().deactivateQuest(this.questID);
    }

    @Override
    public void advanceObjective(final Player player, final int nextObjective)
    {
        if (this.objectiveProgress == 0)
        {
            this.onStart(player, getColony());
        }
        this.objectiveProgress = nextObjective;

        final IQuestData questData = IQuestManager.GLOBAL_SERVER_QUESTS.get(questID);
        if (this.objectiveProgress >= questData.getObjectiveCount())
        {
            this.onCompletion();
            return;
        }

        questData.getObjective(this.objectiveProgress).init(this);
    }

    @Override
    public void onCompletion()
    {
        colony.getQuestManager().concludeQuest(this.getId());
        final ICitizenData questGiverData = colony.getCitizenManager().getCivilian(questGiver);
        if (questGiverData != null)
        {
            questGiverData.onQuestCompletion(this.questID);
        }

        for (final int partId : questParticipants)
        {
            final ICitizenData partData = colony.getCitizenManager().getCivilian(partId);
            if (partData != null)
            {
                partData.onQuestCompletion(this.questID);
            }
        }
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compoundNBT = new CompoundTag();
        compoundNBT.putString(TAG_ID, questID.toString());
        compoundNBT.putInt(TAG_ASSIGN_START, assignmentStart);
        compoundNBT.putInt(TAG_PROGRESS, objectiveProgress);
        compoundNBT.putInt(TAG_QUEST_GIVER, questGiver);

        final ListTag participantList = new ListTag();
        for (final int citizenData : this.questParticipants)
        {
            participantList.add(IntTag.valueOf(citizenData));
        }
        compoundNBT.put(TAG_PARTICIPANTS, participantList);

        return compoundNBT;
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
    {
        questID = new ResourceLocation(nbt.getString(TAG_ID));
        assignmentStart = nbt.getInt(TAG_ASSIGN_START);
        objectiveProgress = nbt.getInt(TAG_PROGRESS);
        questGiver = nbt.getInt(TAG_QUEST_GIVER);

        final ListTag participantList = nbt.getList(TAG_PARTICIPANTS, Tag.TAG_INT);
        for (final Tag tag : participantList)
        {
            questParticipants.add(((IntTag) tag).getAsInt());
        }
    }

    @Override
    public int getIndex()
    {
        return objectiveProgress;
    }

    @Override
    public IQuestParticipant getParticipant(final int target)
    {
        return colony.getCitizenManager().getCivilian(questParticipants.get(target) - 1);
    }

    @Override
    public List<Integer> getParticipants()
    {
        return questParticipants;
    }
}
