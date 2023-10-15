package com.minecolonies.api.quests;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Quest objective interface for all objectives.
 */
public interface IQuestObjectiveTemplate
{
    /**
     * Initialization of an objective.
     *
     * @param colonyQuest the colony quest it belongs to.
     * @return potentially related objective data.
     */
    @Nullable
    IObjectiveInstance startObjective(final IQuestInstance colonyQuest);

    /**
     * On objective abort.
     *
     * @param colonyQuest related colony quest.
     */
    default void onCancellation(final IQuestInstance colonyQuest) {}

    /**
     * On world load trigger.
     *
     * @param colonyQuest the quest.
     */
    default void onWorldLoad(IQuestInstance colonyQuest) {}

    /**
     * Get a {@link Component} instance with the text containing the progress of this objective.
     *
     * @param quest the quest to get the info from.
     * @param style the style to use on subcomponents.
     * @return the chat component.
     */
    Component getProgressText(IQuestInstance quest, Style style);

    /**
     * Get objective data related to the objective.
     *
     * @return the data, default null.
     */
    @Nullable
    default IObjectiveInstance createObjectiveInstance()
    {
        return null;
    }

    /**
     * Get the list of reward unlocks from this objective.
     *
     * @return the unlocked rewards by this objective.
     */
    List<Integer> getRewardUnlocks();
}
