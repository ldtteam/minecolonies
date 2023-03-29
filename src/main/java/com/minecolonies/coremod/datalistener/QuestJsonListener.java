package com.minecolonies.coremod.datalistener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.quests.IAnswerResult;
import com.minecolonies.api.quests.IQuestData;
import com.minecolonies.api.quests.IQuestManager;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.quests.*;
import com.minecolonies.coremod.quests.objectives.DeliveryObjective;
import com.minecolonies.coremod.quests.objectives.DialogueObjective;
import com.minecolonies.api.quests.IQuestObjective;
import com.minecolonies.coremod.quests.triggers.IQuestTrigger;
import com.minecolonies.coremod.quests.triggers.ITriggerReturnData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

import static com.minecolonies.coremod.quests.QuestParsingConstants.*;
import static com.minecolonies.coremod.quests.QuestParsingConstants.BRACE_CLOSE;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Loader for Json based crafter specific recipes
 */
public class QuestJsonListener extends SimpleJsonResourceReloadListener
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    //todo make this a forge registry!
    private static Map<String, Function<JsonObject, IQuestObjective>> QUEST_OBJECTIVE_REGISTRY = new HashMap<>();
    static
    {
        QUEST_OBJECTIVE_REGISTRY.put("dialogue", DialogueObjective::createObjective);
        QUEST_OBJECTIVE_REGISTRY.put("delivery", DeliveryObjective::createObjective);
    }

    /**
     * Set up the core loading, with the directory in the datapack that contains this data Directory is: <namespace>/quests/<path>
     */
    public QuestJsonListener()
    {
        super(GSON, "quests");
    }

    @Override
    protected void apply(final Map<ResourceLocation, JsonElement> jsonElementMap, final @NotNull ResourceManager resourceManager, final @NotNull ProfilerFiller profiler)
    {
        Log.getLogger().info("Loading quests from data");

        // We start by clearing all old requests.
        IQuestManager.GLOBAL_SERVER_QUESTS.clear();

        for (final Map.Entry<ResourceLocation, JsonElement> entry : jsonElementMap.entrySet())
        {
            final ResourceLocation fileResLoc = entry.getKey();
            final JsonObject questDataJson = entry.getValue().getAsJsonObject();

            try
            {
                final IQuestData data = loadDataFromJson(fileResLoc, questDataJson);
                IQuestManager.GLOBAL_SERVER_QUESTS.put(fileResLoc, data);
            }
            catch (Exception e)
            {
                Log.getLogger().error("Skipping quest: " + fileResLoc + " due to parsing error:", e);
            }
        }

        Log.getLogger().info("Finished loading quests from data");
    }


    public IQuestData loadDataFromJson(final ResourceLocation questId, final JsonObject jsonObject) throws Exception
    {
        // Init the two - move to forge registry later.
        IQuestTrigger.TriggerTypes.values();
        IAnswerResult.ResultOption.values();

        final List<IQuestTrigger> questTriggers = new ArrayList<>();
        // Read quest triggers
        for (final JsonElement triggerJson : jsonObject.get(QUEST_TRIGGERS).getAsJsonArray())
        {
            final JsonObject triggerObj = triggerJson.getAsJsonObject();
            final String type = triggerObj.get(TYPE).getAsString();

            try
            {
                questTriggers.add(IQuestTrigger.QUEST_TRIGGER_REGISTRY.get(type).apply(triggerObj));
            }
            catch (final Exception ex)
            {
                throw new Exception("Failed loading triggers for type: " + type, ex);
            }
        }

        final List<IQuestObjective> questObjectives = new ArrayList<>();
        for (final JsonElement objectivesJson : jsonObject.get(QUEST_OBJECTIVES).getAsJsonArray())
        {
            final JsonObject objectiveObj = objectivesJson.getAsJsonObject();
            final String type = objectiveObj.get(TYPE).getAsString();
            try
            {
                questObjectives.add(QUEST_OBJECTIVE_REGISTRY.get(type).apply(objectiveObj));
            }
            catch (final Exception ex)
            {
                throw new Exception("Failed loading objectives for type: " + type, ex);
            }
        }

        String order = "";
        if (jsonObject.has(TRIGGER_ORDER))
        {
            order = jsonObject.get(TRIGGER_ORDER).getAsString();
        }

        final int maxOccurrences;
        if (jsonObject.has(MAX_OCC))
        {
            maxOccurrences = jsonObject.get(MAX_OCC).getAsInt();
        }
        else
        {
            maxOccurrences = 1;
        }

        return new QuestData(questId, maxOccurrences, parseTriggerOrder(questId, order, questTriggers), questObjectives);

        /*

        // How often the quest is allowed to repeat, default infinite
        if (jsonObject.has(QUEST_REPEATING))
        {
            repeatingtimes = jsonObject.get(QUEST_REPEATING).getAsInt();
        }

        // Necessary completed pre-quests
        if (jsonObject.has(QUEST_PRE_QUESTS)) todo: OKay, not a single parent request, but list<parentrequest>
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
        }*/
    }

    // Unused yet
    private Function<IColony, List<ITriggerReturnData>> parseTriggerOrder(final ResourceLocation questId, final String order, final List<IQuestTrigger> triggers)
    {
        // Default and.
        if (order.isEmpty())
        {
            return colony -> {
                final List<ITriggerReturnData> returnList = new ArrayList<>();

                for (final IQuestTrigger trigger: triggers)
                {
                    ITriggerReturnData returnData = trigger.isFulfilledForColony(colony);
                    if (returnData.isPositive())
                    {
                        returnList.add(returnData);
                    }
                    else
                    {
                        return null;
                    }
                }
                return returnList;
            };
        }

        // Test
        //order = "(2 && (1 || 3)) && ((1 && 2) || 3)";
        // Replace whitespaces
        //order = order.replaceAll("\\s+", "");

        // Split by words and braces, but keep the chars
        final List<String> values = Arrays.asList(order.replaceAll("\\s+","").split("((?<=\\w)|(?=\\w)|(?<=[)(])|(?=[)(]))"));

        final List<String> types = new ArrayList<>();
        for (String value : values)
        {
            try
            {
                // Try parsing to number. We only allow numbers.
                Integer.parseInt(value);
                if (!types.contains(value))
                {
                    types.add(value);
                }
            }
            catch (final Exception ex)
            {
                //ignore
            }
        }

        if (types.size() != triggers.size())
        {
            Log.getLogger().error("Failed to Parse Quest Triggers. Mismatch number of triggers in order for quest: " + questId.toString());
            return null;
        }

        final Map<String, IQuestTrigger> triggerMap = new HashMap<>();
        for (int i = 0; i < triggers.size(); i++)
        {
            triggerMap.put(String.valueOf(i+1), triggers.get(i));
        }

        return colony -> evaluate(colony, triggerMap, new ArrayList<>(values), new ArrayList<>());
    }

    /**
     * Recursively parses a string condition to a predicate
     *
     * @param colony the colony.
     * @param data split string data
     * @return predicate from data
     */
    private List<ITriggerReturnData> evaluate(final IColony colony, final Map<String, IQuestTrigger> triggerMap, final List<String> data, final List<ITriggerReturnData> lastReturnData)
    {
        final String current = data.get(0);
        data.remove(0);
        switch (current)
        {
            case OR:
                //
                return lastReturnData != null ? lastReturnData : evaluate(colony, triggerMap, data, lastReturnData);
            case AND:
                return lastReturnData == null ? null : evaluate(colony, triggerMap, data, lastReturnData);
            case NOT:
                return evaluate(colony, triggerMap, data, lastReturnData) == null ? lastReturnData : null;
            case BRACE_OPEN:
                final List<ITriggerReturnData> currentReturnData = lastReturnData;
                final List<ITriggerReturnData> result = evaluate(colony, triggerMap, data, new ArrayList<>());
                result.addAll(currentReturnData);
                return evaluate(colony, triggerMap, data, result);
            case BRACE_CLOSE:
                return lastReturnData;
            case EMPTY:
                return evaluate(colony, triggerMap, data, lastReturnData);
            default:
            {
                final IQuestTrigger trigger = triggerMap.get(current);
                final ITriggerReturnData returnData = trigger.isFulfilledForColony(colony);
                if (returnData.isPositive())
                {
                    lastReturnData.add(returnData);
                    return evaluate(colony, triggerMap, data, lastReturnData);
                }
                return evaluate(colony, triggerMap, data, null);
            }
        }
    }
}