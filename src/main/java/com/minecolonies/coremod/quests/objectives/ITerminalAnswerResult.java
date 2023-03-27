package com.minecolonies.coremod.quests.objectives;

import com.google.gson.JsonObject;
import com.minecolonies.api.quests.IColonyQuest;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public interface ITerminalAnswerResult extends IAnswerResult
{

    /**
     * Apply the objective to colony quest. This only applies to the terminal ones!
     * @param quest the quest to apply itself to.
     */
    void applyToQuest(IColonyQuest quest);
}
