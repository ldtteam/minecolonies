package com.minecolonies.coremod.quests.triggers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.quests.IQuestTrigger;
import com.minecolonies.api.quests.ITriggerReturnData;

import java.util.Collections;
import java.util.List;

import static com.minecolonies.api.util.constant.QuestParseConstant.MATCH_ID;
import static com.minecolonies.api.util.constant.QuestParseConstant.STATE_ID;

/**
 * Random quest trigger.
 */
public class CitizenQuestTrigger implements IQuestTrigger
{
    /**
     * The tag we are trying to match.
     */
    public final JsonElement matchTag;

    /**
     * Create a new state quest trigger.
     * @param match the state to match.
     */
    public CitizenQuestTrigger(final JsonElement match)
    {
        this.matchTag = match;
    }

    /**
     * Create a new trigger directly from json.
     * @param questTriggerJson the json associated to this trigger.
     */
    public static CitizenQuestTrigger createStateTrigger(final JsonObject questTriggerJson)
    {
        final JsonObject subObj = questTriggerJson.get(STATE_ID).getAsJsonObject();
        return new CitizenQuestTrigger(subObj.get(MATCH_ID));
    }

    @Override
    public ITriggerReturnData isFulfilledForColony(final IColony colony)
    {
        final List<ICitizenData> citizenDataList = colony.getCitizenManager().getCitizens();
        Collections.shuffle(citizenDataList);
        for (final ICitizenData data : citizenDataList)
        {
            if (matchNbt(data.serializeNBT(), matchTag))
            {
                return new CitizenTriggerReturnData(data);
            }
        }
        return new CitizenTriggerReturnData(null);
    }
}
