package com.minecolonies.core.quests.triggers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.quests.IQuestTriggerTemplate;
import com.minecolonies.api.quests.ITriggerReturnData;
import com.minecolonies.api.util.constant.ColonyConstants;

import java.util.Collections;
import java.util.List;

import static com.minecolonies.api.quests.QuestParseConstant.*;

/**
 * Random quest trigger.
 */
public class CitizenQuestTriggerTemplate implements IQuestTriggerTemplate
{
    /**
     * The tag we are trying to match.
     */
    public JsonElement matchTag = null;

    /**
     * The tag we are trying to NOT match.
     */
    public JsonElement notMatchTag = null;

    /**
     * Create a new state quest trigger.
     * @param tag the state to match.
     */
    public CitizenQuestTriggerTemplate(final JsonObject tag)
    {
        if (tag.has(MATCH_ID))
        {
            this.matchTag = tag.get(MATCH_ID);
        }

        if (tag.has(NOT_MATCH_ID))
        {
            this.notMatchTag = tag.get(NOT_MATCH_ID);
        }
    }

    /**
     * Create a new trigger directly from json.
     * @param questTriggerJson the json associated to this trigger.
     */
    public static CitizenQuestTriggerTemplate createStateTrigger(final JsonObject questTriggerJson)
    {
        final JsonObject subObj = questTriggerJson.get(STATE_ID).getAsJsonObject();
        return new CitizenQuestTriggerTemplate(subObj);
    }

    @Override
    public ITriggerReturnData canTriggerQuest(final IColony colony)
    {
        final List<ICitizenData> citizenDataList = colony.getCitizenManager().getCitizens();
        if (matchTag == null && notMatchTag == null)
        {
            if (citizenDataList.isEmpty())
            {
                return new CitizenTriggerReturnData(null);
            }
            return new CitizenTriggerReturnData(citizenDataList.get(ColonyConstants.rand.nextInt(citizenDataList.size())));
        }

        Collections.shuffle(citizenDataList);
        for (final ICitizenData data : citizenDataList)
        {
            if (matchTag != null && !IQuestTriggerTemplate.matchNbt(data.serializeNBT(), matchTag))
            {
                continue;
            }

            if (notMatchTag != null && IQuestTriggerTemplate.matchNbt(data.serializeNBT(), notMatchTag))
            {
                continue;
            }
            return new CitizenTriggerReturnData(data);

        }
        return new CitizenTriggerReturnData(null);
    }
}
