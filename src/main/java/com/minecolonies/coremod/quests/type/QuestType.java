package com.minecolonies.coremod.quests.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.quests.IQuest;
import com.minecolonies.coremod.quests.Quest;
import com.minecolonies.coremod.quests.type.effects.IQuestEffect;
import com.minecolonies.coremod.quests.type.effects.SetWorkerIdleEffect;
import com.minecolonies.coremod.quests.type.effects.TrackTreeChoppingEffect;
import com.minecolonies.coremod.quests.type.rewards.IQuestReward;
import com.minecolonies.coremod.quests.type.rewards.ItemStackQuestReward;
import com.minecolonies.coremod.quests.type.triggers.BuildingUpgradeTrigger;
import com.minecolonies.coremod.quests.type.triggers.IQuestTrigger;
import com.minecolonies.coremod.quests.type.triggers.RandomAppearanceTrigger;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.minecolonies.coremod.quests.type.QuestParsingConstants.*;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Quest type, representing a type of quest as defined in json.
 */
public class QuestType implements IQuestType
{
    // Registry placeholders, move to forge registry
    public static final Map<ResourceLocation, BiFunction<JsonObject, IQuest, IQuestEffect>> effectRegistry  = new HashMap<>();
    public static final Map<ResourceLocation, Function<JsonObject, IQuestTrigger>>          triggerRegistry = new HashMap<>();
    public static final Map<ResourceLocation, Function<JsonObject, IQuestReward>>           rewardRegistry  = new HashMap<>();

    /**
     * ID's and data used for creating the actual effects
     */
    private Map<ResourceLocation, JsonObject> effectData = new HashMap<>();

    /**
     *
     */
    private Map<String, IQuestTrigger> triggers = new HashMap<>();

    /**
     * ID's and data used for creating the actual effects
     */
    private List<IQuestReward> rewards = new ArrayList<>();

    private final ResourceLocation typeID;

    private String                 questCategory  = "none";
    private int                    repeatingtimes = 0;
    private List<ResourceLocation> preQuests      = new ArrayList<>();
    private List<ResourceLocation> followupQuests = new ArrayList<>();

    private JsonElement locationData = null;

    public QuestType(final ResourceLocation questID)
    {
        this.typeID = questID;
        effectRegistry.put(SetWorkerIdleEffect.ID, (json, quest) -> new SetWorkerIdleEffect(quest));
        effectRegistry.put(TrackTreeChoppingEffect.ID, (json, quest) -> new TrackTreeChoppingEffect(quest));
        triggerRegistry.put(BuildingUpgradeTrigger.ID, json -> new BuildingUpgradeTrigger(this));
        triggerRegistry.put(RandomAppearanceTrigger.ID, json -> new RandomAppearanceTrigger(this));
        rewardRegistry.put(ItemStackQuestReward.ID, json -> new ItemStackQuestReward(this));
    }

    @Override
    public ResourceLocation getID()
    {
        return typeID;
    }

    @Override
    public List<IQuestEffect> createEffectsFor(final Quest quest)
    {
        // Generate new object from resloc
        //return effects;
        List<IQuestEffect> effects = new ArrayList<>();
        for (final Map.Entry<ResourceLocation, JsonObject> entry : effectData.entrySet())
        {
            effects.add(effectRegistry.get(entry.getKey()).apply(entry.getValue(), quest));
        }

        return effects;
    }

    @Override
    public void loadDataFromJson(final JsonObject jsonObject, Map<ResourceLocation, JsonObject> allQuests) throws Exception
    {
        // Category
        if (jsonObject.has(QUEST_CATEGORY))
        {
            questCategory = jsonObject.get(QUEST_CATEGORY).getAsString();
        }

        // Read quest triggers
        for (final JsonElement triggerJson : jsonObject.get(QUEST_TRIGGERS).getAsJsonArray())
        {
            // List of complex objects
            final JsonObject effectJsonObj = triggerJson.getAsJsonObject();

            if (!effectJsonObj.has(ID))
            {
                throw new Exception("Missing id for " + QUEST_TRIGGERS);
            }

            final String id = effectJsonObj.get(ID).getAsString();
            ResourceLocation triggerID = getResourceLocation(id.replace("%d", ""));

            if (triggers.containsKey(id))
            {
                throw new Exception("Duplicate quest trigger id: " + id);
            }

            if (!triggerRegistry.containsKey(triggerID))
            {
                throw new Exception("Unkown/unregistered quest trigger: for " + triggerID);
            }

            triggers.put(id, triggerRegistry.get(triggerID).apply(effectJsonObj));
        }

        if (jsonObject.has(QUEST_LOCATION))
        {
            locationData = jsonObject.get(QUEST_LOCATION);
        }

        // How often the quest is allowed to repeat, default infinite
        if (jsonObject.has(QUEST_REPEATING))
        {
            repeatingtimes = jsonObject.get(QUEST_REPEATING).getAsInt();
        }

        // Necessary completed pre-quests
        if (jsonObject.has(QUEST_PRE_QUESTS))
        {
            preQuests = new ArrayList<>();
            for (final JsonElement effectJson : jsonObject.get(QUEST_PRE_QUESTS).getAsJsonArray())
            {
                final ResourceLocation preQuest = getResourceLocation(effectJson.getAsString());
                if (!allQuests.containsKey(preQuest))
                {
                    throw new Exception("Parsing failure: Missing quest-json for pre-quest: " + preQuest);
                }
                preQuests.add(getResourceLocation(effectJson.getAsString()));
            }
        }

        // Quests triggered after completion
        if (jsonObject.has(QUEST_FOLLOWUP_QUESTS))
        {
            followupQuests = new ArrayList<>();
            for (final JsonElement effectJson : jsonObject.get(QUEST_FOLLOWUP_QUESTS).getAsJsonArray())
            {
                final ResourceLocation preQuest = getResourceLocation(effectJson.getAsString());
                if (!allQuests.containsKey(preQuest))
                {
                    throw new Exception("Parsing failure: Missing quest-json for followup-quest: " + preQuest);
                }
                followupQuests.add(getResourceLocation(effectJson.getAsString()));
            }
        }

        // Read effect ID's and save data for creating them

        if (!jsonObject.has(QUEST_EFFECTS))
        {
            throw new Exception("Parsing failure: Missing effects for quest");
        }

        effectData = new HashMap<>();
        for (final JsonElement effectJson : jsonObject.get(QUEST_EFFECTS).getAsJsonArray())
        {
            // List of complex objects
            final JsonObject effectJsonObj = effectJson.getAsJsonObject();

            if (!effectJsonObj.has(ID))
            {
                throw new Exception("Missing ID for " + QUEST_EFFECTS + " in quest type: ");
            }

            final ResourceLocation effectID = getResourceLocation(effectJsonObj.get(ID).getAsString());
            if (!effectRegistry.containsKey(effectID))
            {
                throw new Exception("Unkown/unregistered quest effect: for " + effectID);
            }

            effectData.put(effectID, effectJsonObj);
        }

        // Read quest rewards
        rewards = new ArrayList<>();
        for (final JsonElement rewardJson : jsonObject.get(QUEST_REWARDS).getAsJsonArray())
        {
            // List of complex objects
            final JsonObject effectJsonObj = rewardJson.getAsJsonObject();

            if (!effectJsonObj.has(ID))
            {
                throw new Exception("Missing ID for " + QUEST_REWARDS);
            }

            final ResourceLocation rewardID = getResourceLocation(effectJsonObj.get(ID).getAsString());
            if (!rewardRegistry.containsKey(rewardID))
            {
                throw new Exception("Unkown/unregistered quest reward: for " + rewardID);
            }

            effectData.put(rewardID, effectJsonObj);
        }
    }

    /**
     * Prases a string to a resourcelocation with namespace and path, throws exception on failure
     *
     * @param string string to parse
     * @return res location
     * @throws ResourceLocationException error exception
     */
    private ResourceLocation getResourceLocation(String string) throws ResourceLocationException
    {
        if (string != null && !string.equals(EMPTY))
        {
            String[] split = string.split(":");
            if (split.length == 2)
            {
                return new ResourceLocation(split[0], split[1]);
            }
        }

        throw new ResourceLocationException("Cannot parse:" + string + " to a valid resource location");
    }

    // Unused yet
    private Predicate<IColony> parseTriggerOrder(String order, final Map<String, IQuestTrigger> triggers)
    {
        // Default or'ing
        if (order.equals(""))
        {
            Predicate<IColony> pred = null;

            for (final Map.Entry<String, IQuestTrigger> entry : triggers.entrySet())
            {
                final IQuestTrigger trigger = entry.getValue();
                if (pred == null)
                {

                    pred = trigger::shouldTrigger;
                    continue;
                }

                pred = pred.or(trigger::shouldTrigger);
            }
        }

        // Test
        order = "(b && (a || c)) && ((a && b) || c)";

        // Replace whitespaces
        order = order.replaceAll("\\s+", "");

        // Split by words and braces, but keep the chars
        final String[] values = order.split("((?<=\\w)|(?=\\w)|(?<=[)(])|(?=[)(]))");

        index = 0;
        final Predicate<IColony> pred = parse(c -> true, values);

        return pred;
    }

    /**
     * Recursively parses a string condition to a predicate
     *
     * @param predicate starting predicate
     * @param data split string data
     * @return predicate from data
     */
    int index = 0;

    private Predicate<IColony> parse(final Predicate<IColony> predicate, final String[] data)
    {
        if (index >= data.length)
        {
            return predicate;
        }

        final String current = data[index++];
        switch (current)
        {
            case OR:
                return predicate.or(parse(predicate, data));
            case AND:
                return predicate.and(parse(predicate, data));
            case NOT:
                return parse(predicate, data).negate();
            case BRACE_OPEN:
                return parse(parse(null, data), data);
            case BRACE_CLOSE:
                return predicate;
            case EMPTY:
                return parse(predicate, data);
            default:
            {
                final IQuestTrigger trigger = triggers.get(current);
                if (trigger == null)
                {
                    Log.getLogger().error("Error parsing condition order, not a valid symbol:" + current + " for quest: " + this.getID());
                    return c -> false;
                }

                return parse(trigger::shouldTrigger, data);
            }
        }
    }
}
