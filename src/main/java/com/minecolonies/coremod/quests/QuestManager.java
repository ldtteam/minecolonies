package com.minecolonies.coremod.quests;

import com.minecolonies.api.colony.IColony;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.*;

/**
 * Quest manager of each colony.
 */
public class QuestManager implements IQuestManager
{
    /**
     * All quests that have been unlocked.
     */
    private final Map<UUID, IQuest> availableQuests = new HashMap<>();

    /**
     * All quests that have been finished.
     */
    private final List<UUID> finishedQuests = new ArrayList<>();

    /**
     * All quests in progress.
     */
    private final Map<UUID, IQuest> inProgressQuests = new HashMap<>();

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
    public boolean attemptAcceptQuest(final UUID questID, final Player player)
    {
        final IQuest quest = GLOBAL_SERVER_QUESTS.get(questID);
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
    public void concludeQuest(final UUID questId)
    {
        if (inProgressQuests.containsKey(questId))
        {
            inProgressQuests.remove(questId);
            finishedQuests.add(questId);
        }
    }

    @Override
    public void onColonyTick()
    {
        for (final Map.Entry<UUID, IQuest> quest : GLOBAL_SERVER_QUESTS.entrySet())
        {
            if (quest.getValue().canStart(colony.getColonyTag()))
            {
                availableQuests.put(quest.getKey(), quest.getValue());
            }
        }

        for (final Map.Entry<UUID, IQuest> quest : new ArrayList<>(availableQuests.entrySet()))
        {
            if (!quest.getValue().isValid(colony))
            {
                availableQuests.remove(quest.getKey());
            }
        }

        for (final Map.Entry<UUID, IQuest> quest : new ArrayList<>(inProgressQuests.entrySet()))
        {
            if (!quest.getValue().isValid(colony))
            {
                availableQuests.remove(quest.getKey());
            }
        }
    }

    @Override
    public CompoundTag serializeNBT()
    {
        return null;
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
    {

    }
}
