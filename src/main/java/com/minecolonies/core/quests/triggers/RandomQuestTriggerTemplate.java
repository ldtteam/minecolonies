package com.minecolonies.core.quests.triggers;

import com.google.gson.JsonObject;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.quests.IQuestTriggerTemplate;
import com.minecolonies.api.quests.ITriggerReturnData;

import static com.minecolonies.api.quests.QuestParseConstant.RARITY_ID;

/**
 * Random quest trigger.
 */
public class RandomQuestTriggerTemplate implements IQuestTriggerTemplate
{
    /**
     * There is a 1 in oneInChance, chance of this quest to be triggered.
     */
    private final int oneInChance;

    /**
     * Create a new instance of this trigger.
     * @param oneInChance the chance for this.
     */
    public RandomQuestTriggerTemplate(final int oneInChance)
    {
        this.oneInChance = oneInChance;
    }

    /**
     * Create a new trigger directly from json.
     * @param randomQuestTriggerJson the json associated to this trigger.
     */
    public static RandomQuestTriggerTemplate createStateTrigger(final JsonObject randomQuestTriggerJson)
    {
        return new RandomQuestTriggerTemplate(randomQuestTriggerJson.get(RARITY_ID).getAsInt());
    }

    @Override
    public ITriggerReturnData canTriggerQuest(final IColony colony)
    {
        return new BooleanTriggerReturnData(oneInChance > 0 && colony.getWorld().random.nextInt(oneInChance) < 1);
    }
}
