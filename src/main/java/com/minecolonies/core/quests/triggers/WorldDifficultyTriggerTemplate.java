package com.minecolonies.core.quests.triggers;

import com.google.gson.JsonObject;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.quests.IQuestTriggerTemplate;
import com.minecolonies.api.quests.ITriggerReturnData;
import net.minecraft.world.Difficulty;

import static com.minecolonies.api.quests.QuestParseConstant.DIFFICULTY_KEY;

/**
 * World difficulty quest trigger.
 */
public class WorldDifficultyTriggerTemplate implements IQuestTriggerTemplate
{
    /**
     * World difficulty.
     */
    private final Difficulty difficulty;

    /**
     * Create a new instance of this trigger.
     */
    public WorldDifficultyTriggerTemplate(final Difficulty difficulty)
    {
        this.difficulty = difficulty;
    }

    /**
     * Create a new trigger directly from json.
     * @param jsonObj the json associated to this trigger.
     */
    public static WorldDifficultyTriggerTemplate createDifficultyTrigger(final JsonObject jsonObj)
    {
        return new WorldDifficultyTriggerTemplate(Difficulty.valueOf(jsonObj.get(DIFFICULTY_KEY).getAsString()));
    }

    @Override
    public ITriggerReturnData canTriggerQuest(final IColony colony)
    {
        return new BooleanTriggerReturnData(colony.getWorld().getDifficulty() == difficulty);
    }
}
