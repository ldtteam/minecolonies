package com.minecolonies.core.quests;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.quests.FinishedQuest;
import com.minecolonies.api.quests.IQuestInstance;
import com.minecolonies.api.quests.IQuestManager;
import com.minecolonies.api.quests.IQuestTemplate;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

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
    private final Map<Identifier, IQuestInstance> availableQuests = new HashMap<>();

    /**
     * All quests that have been finished. And how often.
     */
    private final Map<Identifier, Integer> finishedQuests = new HashMap<>();

    /**
     * All quests in progress.
     */
    private final Map<Identifier, IQuestInstance> inProgressQuests = new HashMap<>();

    /**
     * Unlocked quest requirements.
     */
    private final List<Identifier> unlockedQuests = new ArrayList<>();

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

    /**
     * Tracks if data needs to be sent
     */
    private boolean isDirty = true;

    public QuestManager(final IColony colony)
    {
        this.colony = colony;
    }

    @Override
    public boolean attemptAcceptQuest(final Identifier questID, final Player player)
    {
        final IQuestInstance quest = availableQuests.getOrDefault(questID, null);
        if (quest == null || !quest.isValid(colony))
        {
            return false;
        }
        this.inProgressQuests.put(questID, quest);
        this.availableQuests.remove(questID);
        markDirty();
        return true;
    }

    @Override
    public void alterReputation(final double difference)
    {
        this.questReputation += difference;
        markDirty();
    }

    @Override
    public double getReputation()
    {
        return this.questReputation;
    }

    @Override
    public void completeQuest(final Identifier questId)
    {
        if (inProgressQuests.containsKey(questId))
        {
            inProgressQuests.remove(questId);
            finishedQuests.put(questId, finishedQuests.getOrDefault(questId, 0) + 1);
            markDirty();
        }
        else if (availableQuests.containsKey(questId))
        {
            // When a player short-cut quits a job without accepting it. (E.g. been there, done that options).
            availableQuests.remove(questId);
            finishedQuests.put(questId, finishedQuests.getOrDefault(questId, 0) + 1);
            markDirty();
        }

        finishedQuestsCache = null;
    }

    @Override
    public void onColonyTick()
    {
        for (final Map.Entry<Identifier, IQuestTemplate> quest : GLOBAL_SERVER_QUESTS.entrySet())
        {
            if (availableQuests.containsKey(quest.getKey())
                  || inProgressQuests.containsKey(quest.getKey())
                  || finishedQuests.getOrDefault(quest.getKey(), 0) >= quest.getValue().getMaxOccurrence())
            {
                continue;
            }

            boolean missingParent = false;
            for (final Identifier parent: quest.getValue().getParents())
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
                markDirty();
            }
        }

        for (final Map.Entry<Identifier, IQuestInstance> availableQuest : new ArrayList<>(availableQuests.entrySet()))
        {
            if (!GLOBAL_SERVER_QUESTS.containsKey(availableQuest.getKey()) || !availableQuest.getValue().isValid(colony))
            {
                availableQuest.getValue().onDeletion();
                this.availableQuests.remove(availableQuest.getKey());
                markDirty();
            }
        }

        for (final Map.Entry<Identifier, IQuestInstance> inProgressQuest : new ArrayList<>(inProgressQuests.entrySet()))
        {
            if (!GLOBAL_SERVER_QUESTS.containsKey(inProgressQuest.getKey()) || !inProgressQuest.getValue().isValid(colony))
            {
                inProgressQuest.getValue().onDeletion();
                this.inProgressQuests.remove(inProgressQuest.getKey());
                markDirty();
            }
        }
    }

    @Override
    public void deleteQuest(final Identifier questID)
    {
        this.availableQuests.remove(questID);
        this.inProgressQuests.remove(questID);
        markDirty();
    }

    @Override
    public IQuestInstance getAvailableOrInProgressQuest(final Identifier questId)
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
    public void unlockQuest(final Identifier questId)
    {
        this.unlockedQuests.add(questId);
        markDirty();
    }

    @Override
    public boolean isUnlocked(final Identifier questId)
    {
        return this.unlockedQuests.contains(questId);
    }

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider)
    {
        final CompoundTag managerCompound = new CompoundTag();

        final ListTag availableListTag = new ListTag();
        for (final Map.Entry<Identifier, IQuestInstance> available : availableQuests.entrySet())
        {
            availableListTag.add(available.getValue().serializeNBT(provider));
        }
        managerCompound.put(TAG_AVAILABLE, availableListTag);

        final ListTag inProgressListTag = new ListTag();
        for (final Map.Entry<Identifier, IQuestInstance> inProgress : inProgressQuests.entrySet())
        {
            inProgressListTag.add(inProgress.getValue().serializeNBT(provider));
        }
        managerCompound.put(TAG_IN_PROGRESS, inProgressListTag);

        final ListTag finishedListTag = new ListTag();
        for (final Map.Entry<Identifier, Integer> finished : finishedQuests.entrySet())
        {
            final CompoundTag finishedTag = new CompoundTag();
            finishedTag.putString(TAG_ID, finished.getKey().toString());
            finishedTag.putInt(TAG_QUANTITY, finished.getValue());
            finishedListTag.add(finishedTag);
        }
        managerCompound.put(TAG_FINISHED, finishedListTag);

        final ListTag unlockedListTag = new ListTag();
        for (final Identifier unlocked : unlockedQuests)
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
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, final CompoundTag nbt)
    {
        final Map<Identifier, IQuestInstance> localAvailableQuests = new HashMap<>();
        final ListTag availableListTag = nbt.getListOrEmpty(TAG_AVAILABLE);
        for (final Tag element : availableListTag)
        {
            final Identifier key = Identifier.parse(((CompoundTag) element).getStringOr(TAG_ID, ""));
            if (GLOBAL_SERVER_QUESTS.containsKey(key))
            {
                final IQuestInstance colonyQuest = availableQuests.containsKey(key) ? availableQuests.get(key) : new QuestInstance(colony);
                colonyQuest.deserializeNBT(provider, (CompoundTag) element);
                localAvailableQuests.put(colonyQuest.getId(), colonyQuest);
            }
        }

        this.availableQuests.clear();
        this.availableQuests.putAll(localAvailableQuests);

        final Map<Identifier, IQuestInstance> localInProgressQuests = new HashMap<>();
        final ListTag inProgressListTag = nbt.getListOrEmpty(TAG_IN_PROGRESS);
        for (final Tag element : inProgressListTag)
        {
            final Identifier key = Identifier.parse(((CompoundTag) element).getStringOr(TAG_ID, ""));
            if (GLOBAL_SERVER_QUESTS.containsKey(key))
            {
                final IQuestInstance colonyQuest = this.inProgressQuests.containsKey(key) ? this.inProgressQuests.get(key) : new QuestInstance(colony);
                colonyQuest.deserializeNBT(provider,(CompoundTag) element);
                localInProgressQuests.put(colonyQuest.getId(), colonyQuest);
            }
        }

        this.inProgressQuests.clear();
        this.inProgressQuests.putAll(localInProgressQuests);


        this.finishedQuests.clear();
        final ListTag finishedListTag = nbt.getListOrEmpty(TAG_FINISHED);
        for (final Tag element : finishedListTag)
        {
            this.finishedQuests.put(Identifier.parse(((CompoundTag) element).getStringOr(TAG_ID, "")), ((CompoundTag) element).getIntOr(TAG_QUANTITY, 0));
        }
        finishedQuestsCache = null;

        this.unlockedQuests.clear();
        final ListTag unlockedListTag = nbt.getListOrEmpty(TAG_UNLOCKED);
        for (final Tag element : unlockedListTag)
        {
            this.unlockedQuests.add(Identifier.parse(((CompoundTag) element).getStringOr(TAG_ID, "")));
        }
        this.questReputation = nbt.getDoubleOr(TAG_REPUTATION, 0.0D);
    }

    @Override
    public void serialize(final RegistryFriendlyByteBuf buf, final boolean hasNewSubscribers)
    {
        buf.writeBoolean(isDirty || hasNewSubscribers);
        if (isDirty || hasNewSubscribers)
        {
            buf.writeNbt(serializeNBT(buf.registryAccess()));
        }
    }

    @Override
    public void deserialize(final RegistryFriendlyByteBuf buf)
    {
        final boolean hasData = buf.readBoolean();
        if (hasData)
        {
            deserializeNBT(buf.registryAccess(), buf.readNbt());
        }
    }

    @Override
    public void markDirty()
    {
        isDirty = true;
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
            for (Map.Entry<Identifier, Integer> entry : finishedQuests.entrySet())
            {
                IQuestTemplate template = GLOBAL_SERVER_QUESTS.get(entry.getKey());
                if (template != null)
                {
                    data.add(new FinishedQuest(template, entry.getValue()));
                }
            }
            finishedQuestsCache = Collections.unmodifiableList(data);
        }
        return finishedQuestsCache;
    }

    @Override
    public void injectAvailableQuest(final IQuestInstance questInstance)
    {
        this.availableQuests.put(questInstance.getId(), questInstance);
        markDirty();
    }
}
