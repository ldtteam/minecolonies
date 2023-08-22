package com.minecolonies.coremod.quests;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.quests.FinishedQuest;
import com.minecolonies.api.quests.IQuestInstance;
import com.minecolonies.api.quests.IQuestTemplate;
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
    private final Map<ResourceLocation, IQuestInstance> availableQuests = new HashMap<>();

    /**
     * All quests that have been finished. And how often.
     */
    private final Map<ResourceLocation, Integer> finishedQuests = new HashMap<>();

    /**
     * All quests in progress.
     */
    private final Map<ResourceLocation, IQuestInstance> inProgressQuests = new HashMap<>();

    /**
     * Unlocked quest requirements.
     */
    private final List<ResourceLocation> unlockedQuests = new ArrayList<>();

    /**
     * Cached mapped results for the finished quests.
     */
    private List<FinishedQuest> finishedQuestsCache = null;

    /**
     * Quest reputation.
     */
    private double questReputation = 0;

    /**
     * This manager's colony
     */
    private final IColony colony;

    public QuestManager(final IColony colony)
    {
        this.colony = colony;
    }

    @Override
    public boolean attemptAcceptQuest(final ResourceLocation questID, final Player player)
    {
        final IQuestInstance quest = availableQuests.getOrDefault(questID, null);
        if (quest == null || !quest.isValid(colony))
        {
            return false;
        }
        this.inProgressQuests.put(questID, quest);
        this.availableQuests.remove(questID);
        return true;
    }

    @Override
    public void alterReputation(final double difference)
    {
        this.questReputation += difference;
    }

    @Override
    public double getReputation()
    {
        return this.questReputation;
    }

    @Override
    public void completeQuest(final ResourceLocation questId)
    {
        if (inProgressQuests.containsKey(questId))
        {
            inProgressQuests.remove(questId);
            finishedQuests.put(questId, finishedQuests.getOrDefault(questId, 0) + 1);
        }
        else if (availableQuests.containsKey(questId))
        {
            // When a player short-cut quits a job without accepting it. (E.g. been there, done that options).
            availableQuests.remove(questId);
            finishedQuests.put(questId, finishedQuests.getOrDefault(questId, 0) + 1);
        }

        finishedQuestsCache = null;
    }

    @Override
    public void onColonyTick()
    {
        for (final Map.Entry<ResourceLocation, IQuestTemplate> quest : GLOBAL_SERVER_QUESTS.entrySet())
        {
            if (availableQuests.containsKey(quest.getKey())
                  || inProgressQuests.containsKey(quest.getKey())
                  || finishedQuests.getOrDefault(quest.getKey(), 0) >= quest.getValue().getMaxOccurrence())
            {
                continue;
            }

            boolean missingParent = false;
            for (final ResourceLocation parent: quest.getValue().getParents())
            {
                if (!finishedQuests.containsKey(parent))
                {
                    missingParent = true;
                    break;
                }
            }

            if (missingParent)
            {
                continue;
            }

            final IQuestInstance colonyQuest = quest.getValue().attemptStart(colony);
            if (colonyQuest != null)
            {
                this.availableQuests.put(quest.getKey(), colonyQuest);
            }
        }

        for (final Map.Entry<ResourceLocation, IQuestInstance> availableQuest : new ArrayList<>(availableQuests.entrySet()))
        {
            if (!GLOBAL_SERVER_QUESTS.containsKey(availableQuest.getKey()) || !availableQuest.getValue().isValid(colony))
            {
                availableQuest.getValue().onDeletion();
                this.availableQuests.remove(availableQuest.getKey());
            }
        }

        for (final Map.Entry<ResourceLocation, IQuestInstance> inProgressQuest : new ArrayList<>(inProgressQuests.entrySet()))
        {
            if (!GLOBAL_SERVER_QUESTS.containsKey(inProgressQuest.getKey()) || !inProgressQuest.getValue().isValid(colony))
            {
                inProgressQuest.getValue().onDeletion();
                this.inProgressQuests.remove(inProgressQuest.getKey());
            }
        }
    }

    @Override
    public void deleteQuest(final ResourceLocation questID)
    {
        this.availableQuests.remove(questID);
        this.inProgressQuests.remove(questID);
    }

    @Override
    public IQuestInstance getAvailableOrInProgressQuest(final ResourceLocation questId)
    {
        return availableQuests.containsKey(questId) ? availableQuests.get(questId) : inProgressQuests.get(questId);
    }

    @Override
    public void onWorldLoad()
    {
        for (final IQuestInstance colonyQuest : inProgressQuests.values())
        {
            colonyQuest.onWorldLoad();
        }
    }

    @Override
    public void unlockQuest(final ResourceLocation questId)
    {
        this.unlockedQuests.add(questId);
    }

    @Override
    public boolean isUnlocked(final ResourceLocation questId)
    {
        return this.unlockedQuests.contains(questId);
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag managerCompound = new CompoundTag();

        final ListTag availableListTag = new ListTag();
        for (final Map.Entry<ResourceLocation, IQuestInstance> available : availableQuests.entrySet())
        {
            availableListTag.add(available.getValue().serializeNBT());
        }
        managerCompound.put(TAG_AVAILABLE, availableListTag);

        final ListTag inProgressListTag = new ListTag();
        for (final Map.Entry<ResourceLocation, IQuestInstance> inProgress : inProgressQuests.entrySet())
        {
            inProgressListTag.add(inProgress.getValue().serializeNBT());
        }
        managerCompound.put(TAG_IN_PROGRESS, inProgressListTag);

        final ListTag finishedListTag = new ListTag();
        for (final Map.Entry<ResourceLocation, Integer> finished : finishedQuests.entrySet())
        {
            final CompoundTag finishedTag = new CompoundTag();
            finishedTag.putString(TAG_ID, finished.getKey().toString());
            finishedTag.putInt(TAG_QUANTITY, finished.getValue());
            finishedListTag.add(finishedTag);
        }
        managerCompound.put(TAG_FINISHED, finishedListTag);

        final ListTag unlockedListTag = new ListTag();
        for (final ResourceLocation unlocked : unlockedQuests)
        {
            final CompoundTag unlockedTag = new CompoundTag();
            unlockedTag.putString(TAG_ID, unlocked.toString());
            unlockedListTag.add(unlockedTag);
        }
        managerCompound.put(TAG_UNLOCKED, unlockedListTag);
        managerCompound.putDouble(TAG_REPUTATION, questReputation);

        return managerCompound;
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
    {
        final Map<ResourceLocation, IQuestInstance> localAvailableQuests = new HashMap<>();
        final ListTag availableListTag = nbt.getList(TAG_AVAILABLE, Tag.TAG_COMPOUND);
        for (final Tag element : availableListTag)
        {
            final ResourceLocation key = new ResourceLocation(((CompoundTag) element).getString(TAG_ID));
            final IQuestInstance colonyQuest = availableQuests.containsKey(key) ? availableQuests.get(key) : new QuestInstance(colony);
            colonyQuest.deserializeNBT((CompoundTag) element);
            localAvailableQuests.put(colonyQuest.getId(), colonyQuest);
        }

        this.availableQuests.clear();
        this.availableQuests.putAll(localAvailableQuests);

        final Map<ResourceLocation, IQuestInstance> localInProgressQuests = new HashMap<>();
        final ListTag inProgressListTag = nbt.getList(TAG_IN_PROGRESS, Tag.TAG_COMPOUND);
        for (final Tag element : inProgressListTag)
        {
            final ResourceLocation key = new ResourceLocation(((CompoundTag) element).getString(TAG_ID));
            final IQuestInstance colonyQuest = this.inProgressQuests.containsKey(key) ? this.inProgressQuests.get(key) : new QuestInstance(colony);
            colonyQuest.deserializeNBT((CompoundTag) element);
            localInProgressQuests.put(colonyQuest.getId(), colonyQuest);
        }

        this.inProgressQuests.clear();
        this.inProgressQuests.putAll(localInProgressQuests);


        this.finishedQuests.clear();
        final ListTag finishedListTag = nbt.getList(TAG_FINISHED, Tag.TAG_COMPOUND);
        for (final Tag element : finishedListTag)
        {
            this.finishedQuests.put(new ResourceLocation(((CompoundTag) element).getString(TAG_ID)), ((CompoundTag) element).getInt(TAG_QUANTITY));
        }

        this.unlockedQuests.clear();
        final ListTag unlockedListTag = nbt.getList(TAG_UNLOCKED, Tag.TAG_COMPOUND);
        for (final Tag element : unlockedListTag)
        {
            this.unlockedQuests.add(new ResourceLocation(((CompoundTag) element).getString(TAG_ID)));
        }
        this.questReputation = nbt.getDouble(TAG_REPUTATION);
    }

    @Override
    public List<IQuestInstance> getAvailableQuests()
    {
        return new ArrayList<>(availableQuests.values());
    }

    @Override
    public List<IQuestInstance> getInProgressQuests()
    {
        return new ArrayList<>(inProgressQuests.values());
    }

    @Override
    public List<FinishedQuest> getFinishedQuests()
    {
        if (finishedQuestsCache == null)
        {
            List<FinishedQuest> data = new ArrayList<>();
            for (Map.Entry<ResourceLocation, Integer> entry : finishedQuests.entrySet())
            {
                IQuestTemplate template = GLOBAL_SERVER_QUESTS.get(entry.getKey());
                data.add(new FinishedQuest(template, entry.getValue()));
            }
            finishedQuestsCache = Collections.unmodifiableList(data);
        }
        return finishedQuestsCache;
    }
}
