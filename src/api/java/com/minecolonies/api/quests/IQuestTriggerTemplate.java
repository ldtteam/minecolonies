package com.minecolonies.api.quests;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.minecolonies.api.colony.IColony;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;

/**
 * Quest triggers are used to check if a colony fulfills certain conditions for a quest to be made available.
 */
public interface IQuestTriggerTemplate
{
    /**
     * Check if the quest trigger condition is fulfilled.
     * @param colony the colony the quest is in.
     * @return true if so.
     */
    ITriggerReturnData canTriggerQuest(final IColony colony);

    /**
     * Check if the quest trigger condition is fulfilled.
     * @param questId the quest to try to trigger.
     * @param colony the colony the quest is in.
     * @return true if so.
     */
    default ITriggerReturnData canTriggerQuest(final ResourceLocation questId, final IColony colony)
    {
        return canTriggerQuest(colony);
    }

    /**
     * Match a nbt tag and a json element tag.
     * @param nbtTag the nbt tag to check.
     * @param matchTag the element tag to check.
     * @return true if the matchTag fits into the nbtTag or if they match.
     */
    static boolean matchNbt(final Tag nbtTag, final JsonElement matchTag)
    {
        return matchNbt(nbtTag, matchTag, 1);
    }

    /**
     * Match a nbt tag and a json element tag.
     * @param nbtTag the nbt tag to check.
     * @param matchTag the element tag to check.
     * @param count the number of elements to match in a list.
     * @return true if the matchTag fits into the nbtTag or if they match.
     */
    static boolean matchNbt(final Tag nbtTag, final JsonElement matchTag, final int count)
    {
        if (nbtTag instanceof final CompoundTag nbtCompound)
        {
            if (!(matchTag instanceof JsonObject matchObject))
            {
                return false;
            }

            for (String key : matchObject.keySet())
            {
                if (!nbtCompound.contains(key))
                {
                    return false;
                }

                if (!matchNbt(nbtCompound.get(key), matchObject.get(key)))
                {
                    return false;
                }
            }
            return true;
        }

        if (nbtTag instanceof ListTag nbtList)
        {
            // Check if we're trying to match an element in the list.
            int matchCount = 0;
            for (final Tag tag : nbtList)
            {
                if (matchNbt(tag, matchTag))
                {
                    matchCount++;
                    if (matchCount >= count)
                    {
                        return true;
                    }
                }
            }

            // This can also be partial matching (e.g. find 3 elements).
            if (!(matchTag instanceof JsonArray arrayTag))
            {
                return false;
            }

            for (final JsonElement element: arrayTag)
            {
                boolean matched = false;
                for (final Tag tag : nbtList)
                {
                    if (matchNbt(tag, element))
                    {
                        matched = true;
                        break;
                    }
                }

                if (!matched)
                {
                    return false;
                }
            }
            return true;
        }

        // Don't handle non primitives from here on.
        if (!(matchTag instanceof JsonPrimitive))
        {
            return false;
        }

        // Full equals for string.
        if (nbtTag instanceof StringTag && ((JsonPrimitive) matchTag).isString())
        {
            return nbtTag.getAsString().equals(matchTag.getAsString());
        }
        else if (nbtTag instanceof ByteTag && ((JsonPrimitive) matchTag).isBoolean())
        {
            return (((ByteTag) nbtTag).getAsByte() == 0) != matchTag.getAsBoolean();
        }
        // Larger equals for numbers.
        else if (nbtTag instanceof NumericTag && ((JsonPrimitive) matchTag).isNumber())
        {
            return ((NumericTag) nbtTag).getAsDouble() >= matchTag.getAsDouble();
        }
        return false;
    }
}
