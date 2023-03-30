package com.minecolonies.coremod.quests;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.quests.IColonyQuest;
import com.minecolonies.api.quests.IQuestData;
import com.minecolonies.api.quests.IQuestObjective;
import com.minecolonies.coremod.quests.triggers.ITriggerReturnData;
import net.minecraft.resources.ResourceLocation;

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
     * List of quest triggers assigned to this quest data including the trigger order.
     */
    private final Function<IColony, List<ITriggerReturnData>> questTriggerList;

    private final List<IQuestObjective> objectives;

    private final int maxOccurrence;

    // We want two different things. a) A global "quest holder" kind of thing that has all the fields that we read from the json (e.g. the necessary triggers, side effects, etc)

    public QuestData(final ResourceLocation questID, final int maxOccurrence, final Function<IColony, List<ITriggerReturnData>> questTriggerList, final List<IQuestObjective> questObjectives)
    {
        this.questID = questID;
        this.questTriggerList = questTriggerList;
        this.maxOccurrence = maxOccurrence;
        this.objectives = questObjectives;
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
        return 10; //todo read from file
    }

    @Override
    public int getMaxOccurrence()
    {
        return maxOccurrence;
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
}
