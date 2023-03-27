package com.minecolonies.coremod.quests;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.quests.IColonyQuest;
import com.minecolonies.api.quests.IQuestData;
import com.minecolonies.api.quests.IQuestManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.*;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Quest manager of each colony.
 */
public class QuestManager implements IQuestManager
{
    /**
     * All quests that have been unlocked.
     */
    private final Map<ResourceLocation, IColonyQuest> availableQuests = new HashMap<>();

    /**
     * All quests that have been finished. And how often.
     */
    private final Map<ResourceLocation, Integer> finishedQuests = new HashMap<>();

    /**
     * All quests in progress.
     */
    private final Map<ResourceLocation, IColonyQuest> inProgressQuests = new HashMap<>();

    /**
     * This manager's colony
     */
    private final IColony colony;

    public QuestManager(final IColony colony)
    {
        this.colony = colony;
    }

    /**
     * Have player attempt to accept a colony quest.
     * @param questID the unique id of the quest.
     * @param player the player trying to accept it.
     * @return true if successful.
     */
    public boolean attemptAcceptQuest(final ResourceLocation questID, final Player player)
    {
        final IColonyQuest quest = availableQuests.getOrDefault(questID, null);
        if (quest == null || !quest.isValid(colony))
        {
            return false;
        }
        quest.onStart(player, colony);
        return true;
    }

    /**
     * Conclude a given quest. This is called FROM the quest, to the colony.
     * @param questId the unique id of the quest.
     */
    public void concludeQuest(final ResourceLocation questId)
    {
        if (inProgressQuests.containsKey(questId))
        {
            inProgressQuests.remove(questId);
            finishedQuests.put(questId, finishedQuests.getOrDefault(questId, 0) + 1);
        }
    }

    @Override
    public void onColonyTick()
    {
        for (final Map.Entry<ResourceLocation, IQuestData> quest : GLOBAL_SERVER_QUESTS.entrySet())
        {
            if (availableQuests.containsKey(quest.getKey())
                  || inProgressQuests.containsKey(quest.getKey())
                  || finishedQuests.getOrDefault(quest.getKey(), 0) >= quest.getValue().getMaxOccurrence())
            {
                continue;
            }

            final IColonyQuest colonyQuest = quest.getValue().attemptStart(colony);
            if (colonyQuest != null)
            {
                availableQuests.put(quest.getKey(), colonyQuest);
            }
        }

        for (final Map.Entry<ResourceLocation, IColonyQuest> availableQuest : new ArrayList<>(availableQuests.entrySet()))
        {
            if (!GLOBAL_SERVER_QUESTS.containsKey(availableQuest.getKey()) || !availableQuest.getValue().isValid(colony))
            {
                availableQuest.getValue().onDeletion();
                availableQuests.remove(availableQuest.getKey());
            }
        }

        for (final Map.Entry<ResourceLocation, IColonyQuest> inProgressQuest : new ArrayList<>(inProgressQuests.entrySet()))
        {
            if (!GLOBAL_SERVER_QUESTS.containsKey(inProgressQuest.getKey()) || !inProgressQuest.getValue().isValid(colony))
            {
                inProgressQuest.getValue().onDeletion();
                availableQuests.remove(inProgressQuest.getKey());
            }
        }
    }

    @Override
    public void deactivateQuest(final ResourceLocation questID)
    {
        this.availableQuests.remove(questID);
        this.inProgressQuests.remove(questID);
    }

    @Override
    public IColonyQuest getAvailableOrInProgressQuest(final ResourceLocation questId)
    {
        return availableQuests.containsKey(questId) ? availableQuests.get(questId) : inProgressQuests.get(questId);
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag managerCompound = new CompoundTag();

        final ListTag availableListTag = new ListTag();
        for (Map.Entry<ResourceLocation, IColonyQuest> available : availableQuests.entrySet())
        {
            availableListTag.add(available.getValue().serializeNBT());
        }
        managerCompound.put(TAG_AVAILABLE, availableListTag);

        final ListTag inProgressListTag = new ListTag();
        for (Map.Entry<ResourceLocation, IColonyQuest> inProgress : inProgressQuests.entrySet())
        {
            inProgressListTag.add(inProgress.getValue().serializeNBT());
        }
        managerCompound.put(TAG_IN_PROGRESS, inProgressListTag);

        final ListTag finishedListTag = new ListTag();
        for (Map.Entry<ResourceLocation, Integer> finished : finishedQuests.entrySet())
        {
            final CompoundTag finishedTag = new CompoundTag();
            finishedTag.putString(TAG_ID, finished.getKey().toString());
            finishedTag.putInt(TAG_QUANTITY, finished.getValue());
            finishedListTag.add(finishedTag);
        }
        managerCompound.put(TAG_FINISHED, finishedListTag);

        return managerCompound;
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
    {
        final ListTag availableListTag = nbt.getList(TAG_AVAILABLE, Tag.TAG_COMPOUND);
        for (final Tag element : availableListTag)
        {
            final IColonyQuest colonyQuest = new ColonyQuest(colony);
            colonyQuest.deserializeNBT((CompoundTag) element);
            availableQuests.put(colonyQuest.getId(), colonyQuest);
        }

        final ListTag inProgressListTag = nbt.getList(TAG_IN_PROGRESS, Tag.TAG_COMPOUND);
        for (final Tag element : inProgressListTag)
        {
            final IColonyQuest colonyQuest = new ColonyQuest(colony);
            colonyQuest.deserializeNBT((CompoundTag) element);
            availableQuests.put(colonyQuest.getId(), colonyQuest);
        }

        final ListTag finishedListTag = nbt.getList(TAG_IN_PROGRESS, Tag.TAG_COMPOUND);
        for (final Tag element : finishedListTag)
        {
            finishedQuests.put(new ResourceLocation(((CompoundTag) element).getString(TAG_ID)), ((CompoundTag) element).getInt(TAG_QUANTITY));
        }
    }
}
