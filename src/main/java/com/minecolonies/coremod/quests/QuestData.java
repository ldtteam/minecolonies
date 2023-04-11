package com.minecolonies.coremod.quests;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.quests.IColonyQuest;
import com.minecolonies.api.quests.IQuestData;
import com.minecolonies.api.quests.IQuestObjective;
import com.minecolonies.api.quests.IQuestReward;
import com.minecolonies.api.quests.ITriggerReturnData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.function.Function;

/**
 * Instance of a specific quest type
 */
public class QuestData implements IQuestData
{
    /**
     * The unique id of this quest.
     */
    private final ResourceLocation questID;

    /**
     * List of parents to fulfill before this can start.
     */
    private final List<ResourceLocation> parents;

    /**
     * List of quest triggers assigned to this quest data including the trigger order.
     */
    private final Function<IColony, List<ITriggerReturnData>> questTriggerList;

    private final List<IQuestObjective> objectives;

    private final List<IQuestReward> questRewards;


    private final int maxOccurrence;

    private final String name;

    /**
     * How long the quest stays active/available.
     */
    private final int questTimeout;

    /**
     * Create new quest data.
     * @param questID the id of the quest.
     * @param name the name of the quest.
     * @param parents the parent quests.
     * @param maxOccurrence the max num of occurrences.
     * @param questTriggerList the list of triggers.
     * @param questObjectives the quest objectives.
     * @param questTimeout the time until it times out.
     * @param questRewards its rewards
     */
    public QuestData(final ResourceLocation questID, final String name,
      final List<ResourceLocation> parents,
      final int maxOccurrence, final Function<IColony, List<ITriggerReturnData>> questTriggerList, final List<IQuestObjective> questObjectives, final int questTimeout, final List<IQuestReward> questRewards)
    {
        this.questID = questID;
        this.name = name;
        this.parents = parents;
        this.questTriggerList = questTriggerList;
        this.maxOccurrence = maxOccurrence;
        this.objectives = questObjectives;
        this.questTimeout = questTimeout;
        this.questRewards = questRewards;
    }

    @Override
    public IColonyQuest attemptStart(final IColony colony)
    {
        final List<ITriggerReturnData> triggerReturnData = questTriggerList.apply(colony);
        return triggerReturnData == null ? null : new ColonyQuest(questID, colony, triggerReturnData);
    }

    @Override
    public int getQuestTimeout()
    {
        return questTimeout;
    }

    @Override
    public int getMaxOccurrence()
    {
        return maxOccurrence;
    }

    @Override
    public void unlockQuestRewards(final IColony colony, final Player player, final IColonyQuest colonyQuest, final List<Integer> unlockedRewards)
    {
        int index = 0;
        for (final IQuestReward questReward : questRewards)
        {
            if (unlockedRewards.contains(index))
            {
                questReward.applyReward(colony, player, colonyQuest);
            }
            index+=1;
        }
    }

    @Override
    public IQuestObjective getObjective(final int index)
    {
        return objectives.get(index);
    }

    @Override
    public int getObjectiveCount()
    {
        return objectives.size();
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public List<ResourceLocation> getParents()
    {
        return this.parents;
    }
}
