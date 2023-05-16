package com.minecolonies.coremod.quests.triggers;

import com.google.gson.JsonObject;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.quests.IQuestTriggerTemplate;
import com.minecolonies.api.quests.ITriggerReturnData;

import static com.minecolonies.api.quests.QuestParseConstant.REPUTATION_ID;

/**
 * Quest reputation quest trigger.
 */
public class QuestReputationTriggerTemplate implements IQuestTriggerTemplate
{
    /**
     * Min quantity.
     */
    private final double minQuantity;

    /**
     * Create a new instance of this trigger.
     */
    public QuestReputationTriggerTemplate(final double minQuantity)
    {
        this.minQuantity = minQuantity;
    }

    /**
     * Create a new trigger directly from json.
     * @param jsonObj the json associated to this trigger.
     */
    public static QuestReputationTriggerTemplate createQuestReputationTrigger(final JsonObject jsonObj)
    {
        return new QuestReputationTriggerTemplate(jsonObj.get(REPUTATION_ID).getAsDouble());
    }

    @Override
    public ITriggerReturnData canTriggerQuest(final IColony colony)
    {
        return new BooleanTriggerReturnData(colony.getQuestManager().getReputation() >= minQuantity);
    }
}
