package com.minecolonies.coremod.quests.triggers;

import com.google.gson.JsonObject;
import com.minecolonies.api.colony.IColony;

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
        return new RandomQuestTrigger(randomQuestTriggerJson.get("rarity").getAsInt());
    }

    @Override
    public ITriggerReturnData isFulfilledForColony(final IColony colony)
    {
        return new BooleanTriggerReturnData(colony.getWorld().random.nextInt(oneInChance) <= 1);
    }
}
