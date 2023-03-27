package com.minecolonies.coremod.quests.triggers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.colony.IColony;
import net.minecraft.nbt.*;

import java.util.List;

/**
 * Random quest trigger.
 */
public class StateQuestTrigger implements IQuestTrigger
{
    /**
     * Path of keys to the nbt we are trying to match,
     */
    public final List<String> nbtPath;

    /**
     * The tag we are trying to match.
     */
    public final JsonElement matchTag;

    /**
     * How often we have to match.
     */
    public final int matchCount;

    /**
     * Create a new state quest trigger.
     * @param paths the path to the state to match.
     * @param match the state to match.
     * @param count the number of matches we have to find.
     */
    public StateQuestTrigger(final String[] paths, final JsonElement match, final int count)
    {
        this.nbtPath = List.of(paths);
        this.matchTag = match;
        this.matchCount = count;
    }

    /**
     * Create a new trigger directly from json.
     * @param questTriggerJson the json associated to this trigger.
     */
    public static StateQuestTrigger createStateTrigger(final JsonObject questTriggerJson)
    {
        final JsonObject subObj = questTriggerJson.get("state").getAsJsonObject();
        return new StateQuestTrigger(subObj.get("path").getAsString().split("/"),
          subObj.get("match"),
          subObj.has("count") ? subObj.get("count").getAsInt() : 1);

    }

    @Override
    public ITriggerReturnData isFulfilledForColony(final IColony colony)
    {
        Tag subPathCompound = colony.getColonyTag();
        for (final String subPath : nbtPath)
        {
            if (subPathCompound instanceof CompoundTag && ((CompoundTag) subPathCompound).contains(subPath))
            {
                subPathCompound = ((CompoundTag) subPathCompound).get(subPath);
            }
            else
            {
                return new BooleanTriggerReturnData(false);
            }
        }

        if (subPathCompound == null)
        {
            return new BooleanTriggerReturnData(false);
        }


        return new BooleanTriggerReturnData(matchNbt(subPathCompound, matchTag, matchCount));
    }
}
