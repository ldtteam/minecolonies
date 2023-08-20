package com.minecolonies.coremod.client.gui.questlog;

import com.ldtteam.blockui.Pane;
import com.minecolonies.api.colony.IColonyView;

import java.util.List;

/**
 * Interface for quest log renderers.
 *
 * @param <T> the quest instance type.
 */
public interface WindowQuestLogQuestRenderer<T>
{
    /**
     * Get the list of quests for this window.
     *
     * @param colonyView the colony instance.
     * @return a list of quest items.
     */
    List<T> getQuestItems(IColonyView colonyView);

    /**
     * Renderer method for an individual quest item. Called via the scrolling list.
     *
     * @param quest      the current quest instance.
     * @param colonyView the colony view instance.
     * @param row        the parenting row pane.
     */
    void renderQuestItem(final T quest, final IColonyView colonyView, final Pane row);

    /**
     * Method to instantiate quest tracking, does not have to be implemented.
     *
     * @param quest the quest instance.
     */
    default void trackQuest(final T quest) {}
}
