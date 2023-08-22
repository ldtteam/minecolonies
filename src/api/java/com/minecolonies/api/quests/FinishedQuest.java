package com.minecolonies.api.quests;

/**
 * Container class for a finished quest, containing the quest template and how often it got finished.
 *
 * @param template      the quest template.
 * @param finishedCount how often this quest got completed.
 */
public record FinishedQuest(IQuestTemplate template, int finishedCount)
{
}
