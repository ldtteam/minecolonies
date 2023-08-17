package com.minecolonies.api.quests;

import com.minecolonies.api.quests.IQuestTemplate;

public record FinishedQuest(IQuestTemplate template, int finishedCount)
{
}
