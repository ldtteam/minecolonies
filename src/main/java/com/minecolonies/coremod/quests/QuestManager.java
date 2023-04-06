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

    @Override
    public boolean attemptAcceptQuest(final ResourceLocation questID, final Player player)
    {
        final IColonyQuest quest = availableQuests.getOrDefault(questID, null);
        if (quest == null || !quest.isValid(colony))
        {
            return false;
        }
        this.inProgressQuests.put(questID, quest);
        this.availableQuests.remove(questID);
        return true;
    }

    @Override
    public void concludeQuest(final ResourceLocation questId)
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

            final IColonyQuest colonyQuest = quest.getValue().attemptStart(colony);
            if (colonyQuest != null)
            {
                this.availableQuests.put(quest.getKey(), colonyQuest);
            }
        }

        for (final Map.Entry<ResourceLocation, IColonyQuest> availableQuest : new ArrayList<>(availableQuests.entrySet()))
        {
            if (!GLOBAL_SERVER_QUESTS.containsKey(availableQuest.getKey()) || !availableQuest.getValue().isValid(colony))
            {
                availableQuest.getValue().onDeletion();
                this.availableQuests.remove(availableQuest.getKey());
            }
        }

        for (final Map.Entry<ResourceLocation, IColonyQuest> inProgressQuest : new ArrayList<>(inProgressQuests.entrySet()))
        {
            if (!GLOBAL_SERVER_QUESTS.containsKey(inProgressQuest.getKey()) || !inProgressQuest.getValue().isValid(colony))
            {
                inProgressQuest.getValue().onDeletion();
                this.inProgressQuests.remove(inProgressQuest.getKey());
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
    public void onWorldLoad()
    {
        for (final IColonyQuest colonyQuest : inProgressQuests.values())
        {
            colonyQuest.onWorldLoad();
        }
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
            inProgressQuests.put(colonyQuest.getId(), colonyQuest);
        }

        final ListTag finishedListTag = nbt.getList(TAG_FINISHED, Tag.TAG_COMPOUND);
        for (final Tag element : finishedListTag)
        {
            finishedQuests.put(new ResourceLocation(((CompoundTag) element).getString(TAG_ID)), ((CompoundTag) element).getInt(TAG_QUANTITY));
        }
    }
}
