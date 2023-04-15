package com.minecolonies.coremod.quests.triggers;

import com.google.gson.JsonObject;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.quests.IQuestTrigger;
import com.minecolonies.api.quests.ITriggerReturnData;

import static com.minecolonies.api.quests.QuestParseConstant.RARITY_ID;

/**
 * Random quest trigger.
 */
public class RandomQuestTrigger implements IQuestTrigger
{
    /**
     * There is a 1 in oneInChance, chance of this quest to be triggered.
     */
    private final int oneInChance;

    /**
     * Create a new instance of this trigger.
     * @param oneInChance the chance for this.
     */
    public RandomQuestTrigger(final int oneInChance)
    {
        this.oneInChance = oneInChance;
    }

    /**
     * Create a new trigger directly from json.
     * @param randomQuestTriggerJson the json associated to this trigger.
     */
    public static RandomQuestTrigger createStateTrigger(final JsonObject randomQuestTriggerJson)
    {
        return new RandomQuestTrigger(randomQuestTriggerJson.get(RARITY_ID).getAsInt());
    }

    @Override
    public ITriggerReturnData canTriggerQuest(final IColony colony)
    {
        return new BooleanTriggerReturnData(colony.getWorld().random.nextInt(oneInChance) <= 1);
    }
}
