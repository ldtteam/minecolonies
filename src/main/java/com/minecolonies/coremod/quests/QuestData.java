package com.minecolonies.coremod.quests;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.quests.IColonyQuest;
import com.minecolonies.api.quests.IQuestData;
import com.minecolonies.api.quests.IQuestObjective;
import com.minecolonies.api.quests.IQuestReward;
import com.minecolonies.coremod.quests.triggers.ITriggerReturnData;
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

    // We want two different things. a) A global "quest holder" kind of thing that has all the fields that we read from the json (e.g. the necessary triggers, side effects, etc)

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
    public void unlockQuestRewards(final IColony colony, final Player player, final IColonyQuest colonyQuest)
    {
        for (final IQuestReward questReward : questRewards)
        {
            questReward.applyReward(colony, player, colonyQuest);
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
