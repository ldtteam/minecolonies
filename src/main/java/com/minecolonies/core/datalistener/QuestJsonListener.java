package com.minecolonies.core.datalistener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.quests.*;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.network.messages.client.GlobalQuestSyncMessage;
import com.minecolonies.core.quests.*;
import com.minecolonies.api.quests.IQuestTriggerTemplate;
import com.minecolonies.api.quests.ITriggerReturnData;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

import static com.minecolonies.core.generation.DataGeneratorConstants.COLONY_QUESTS_DIR;
import static com.minecolonies.core.quests.QuestParsingConstants.*;
import static com.minecolonies.core.quests.QuestParsingConstants.BRACE_CLOSE;

/**
 * Loader for Json based quest data.
 */
public class QuestJsonListener extends SimpleJsonResourceReloadListener
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * The last json map.
     */
    private static Map<ResourceLocation, JsonElement> globalJsonElementMap = new HashMap<>();

    /**
     * Set up the core loading, with the directory in the datapack that contains this data Directory is: <namespace>/colony/quests/<path>
     */
    public QuestJsonListener()
    {
        super(GSON, COLONY_QUESTS_DIR);
    }

    /**
     * Sync to client.
     * @param player to send it to.
     */
    public static void sendGlobalQuestPackets(final ServerPlayer player)
    {
        final FriendlyByteBuf byteBuf = new FriendlyByteBuf(Unpooled.buffer());
        byteBuf.writeInt(globalJsonElementMap.size());
        for (final Map.Entry<ResourceLocation, JsonElement> entry : globalJsonElementMap.entrySet())
        {
            byteBuf.writeResourceLocation(entry.getKey());
            byteBuf.writeByteArray(entry.getValue().toString().getBytes());
        }
        new GlobalQuestSyncMessage(byteBuf).sendToPlayer(player);
    }

    /**
     * Read the data from the packet and parse it.
     * @param byteBuf pck.
     */
    public static void readGlobalQuestPackets(final FriendlyByteBuf byteBuf)
    {
        globalJsonElementMap.clear();
        final int size = byteBuf.readInt();
        for (int i = 0; i < size; i++)
        {
            globalJsonElementMap.put(byteBuf.readResourceLocation(), GSON.fromJson(new String(byteBuf.readByteArray()), JsonObject.class));
        }
        apply(globalJsonElementMap);
    }

    @Override
    protected void apply(final Map<ResourceLocation, JsonElement> jsonElementMap, final @NotNull ResourceManager resourceManager, final @NotNull ProfilerFiller profiler)
    {
        globalJsonElementMap.clear();
        globalJsonElementMap.putAll(jsonElementMap);
        apply(jsonElementMap);
    }

    /**
     * Our universal apply.
     * @param jsonElementMap the map.
     */
    private static void apply(final Map<ResourceLocation, JsonElement> jsonElementMap)
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
                final IQuestTemplate data = loadDataFromJson(fileResLoc, questDataJson);
                IQuestManager.GLOBAL_SERVER_QUESTS.put(fileResLoc, data);
            }
            catch (Exception e)
            {
                Log.getLogger().error("Skipping quest: " + fileResLoc + " due to parsing error:", e);
            }
        }

        Log.getLogger().info("Finished loading quests from data");
    }

    public static IQuestTemplate loadDataFromJson(final ResourceLocation questId, final JsonObject jsonObject) throws Exception
    {
        final List<IQuestTriggerTemplate> questTriggers = new ArrayList<>();
        // Read quest triggers
        for (final JsonElement triggerJson : jsonObject.get(QUEST_TRIGGERS).getAsJsonArray())
        {
            final JsonObject triggerObj = triggerJson.getAsJsonObject();
            final String type = triggerObj.get(TYPE).getAsString();

            try
            {
                questTriggers.add(IMinecoloniesAPI.getInstance().getQuestTriggerRegistry().get(new ResourceLocation(type)).produce(triggerObj));
            }
            catch (final Exception ex)
            {
                throw new Exception("Failed loading triggers for type: " + type, ex);
            }
        }

        final List<IQuestObjectiveTemplate> questObjectives = new ArrayList<>();
        for (final JsonElement objectivesJson : jsonObject.get(QUEST_OBJECTIVES).getAsJsonArray())
        {
            final JsonObject objectiveObj = objectivesJson.getAsJsonObject();
            final String type = objectiveObj.get(TYPE).getAsString();
            try
            {
                questObjectives.add(IMinecoloniesAPI.getInstance().getQuestObjectiveRegistry().get(new ResourceLocation(type)).produce(objectiveObj));
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

        final int questTimeout;
        if (jsonObject.has(TIMEOUT))
        {
            questTimeout = jsonObject.get(TIMEOUT).getAsInt();
        }
        else
        {
            questTimeout = 10;
        }

        final Component questName = Component.translatableEscape(jsonObject.get(NAME).getAsString());

        final List<IQuestRewardTemplate> questRewards = new ArrayList<>();
        for (final JsonElement objectivesJson : jsonObject.get(QUEST_REWARDS).getAsJsonArray())
        {
            final JsonObject objectiveObj = objectivesJson.getAsJsonObject();
            final String type = objectiveObj.get(TYPE).getAsString();
            try
            {
                questRewards.add(IMinecoloniesAPI.getInstance().getQuestRewardRegistry().get(new ResourceLocation(type)).produce(objectiveObj));
            }
            catch (final Exception ex)
            {
                throw new Exception("Failed loading rewards for type: " + type, ex);
            }
        }

        final List<ResourceLocation> parents = new ArrayList<>();
        for (final JsonElement objectivesJson : jsonObject.get(QUEST_PARENTS).getAsJsonArray())
        {
            try
            {
                parents.add(new ResourceLocation(objectivesJson.getAsString()));
            }
            catch (final Exception ex)
            {
                throw new Exception("Failed loading parents: ", ex);
            }
        }

        return new QuestTemplate(questId, questName, parents, maxOccurrences, parseTriggerOrder(questId, order, questTriggers), questObjectives, questTimeout, questRewards);

        /*


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
        }*/
    }

    // Unused yet
    private static Function<IColony, List<ITriggerReturnData<?>>> parseTriggerOrder(final ResourceLocation questId, final String order, final List<IQuestTriggerTemplate> triggers)
    {
        // Default and.
        if (order.isEmpty())
        {
            return colony -> {
                final List<ITriggerReturnData<?>> returnList = new ArrayList<>();

                for (final IQuestTriggerTemplate trigger: triggers)
                {
                    ITriggerReturnData<?> returnData = trigger.canTriggerQuest(questId, colony);
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

        final Map<String, IQuestTriggerTemplate> triggerMap = new HashMap<>();
        for (int i = 0; i < triggers.size(); i++)
        {
            triggerMap.put(String.valueOf(i+1), triggers.get(i));
        }

        if (values.isEmpty())
        {
            return colony -> new ArrayList<>();
        }
        else
        {
            ExpressionNode expressionTree = null;

            int[] depth = new int[100];
            int current = 0;
            for (int i = values.size() - 1; i >= 0; i--)
            {
                final String arg = values.get(i);
                switch (arg)
                {
                    case BRACE_OPEN:
                        for (int d = 0; d < depth[current]; d++)
                        {
                            expressionTree = expressionTree.parent;
                        }

                        depth[current] = 0;
                        current--;
                        break;
                    case BRACE_CLOSE:
                        current++;
                        break;
                    case OR:
                    case AND:
                    case NOT:
                        // make this depth dependent, and allow between insertion.
                        final ExpressionNode node = new ExpressionNode(arg);
                        if (expressionTree.parent != null)
                        {
                            ExpressionNode previous = expressionTree.parent;
                            while (true)
                            {
                                if (previous.childB == expressionTree)
                                {
                                    previous.childB = node;
                                    break;
                                }
                                else if (previous.childA == expressionTree)
                                {
                                    previous.childA = node;
                                    break;
                                }
                                else
                                {
                                    previous = previous.parent;
                                }
                            }
                            node.parent = previous;
                        }

                        depth[current]++;

                        node.childA = expressionTree;
                        node.childA.parent = node;

                        expressionTree = node;

                        break;
                    default:
                        if (expressionTree == null)
                        {
                            expressionTree = new ExpressionNode(arg);
                        }
                        else
                        {
                            expressionTree = expressionTree.append(arg);
                        }
                        break;
                }
            }
            ExpressionNode root = expressionTree;
            while (root.parent != null)
            {
                root = root.parent;
            }
            final ExpressionNode finalExpressionTree = root;
            return colony -> evaluate(colony, triggerMap, finalExpressionTree, new HashMap<>(), questId);
        }
    }

    /**
     * Recursively parses a string condition to a predicate
     *
     * @param colony the colony.
     * @return predicate from data
     */
    private static List<ITriggerReturnData<?>> evaluate(final IColony colony, final Map<String, IQuestTriggerTemplate> triggerMap, final ExpressionNode expressionTree, final Map<String, ITriggerReturnData<?>> triggerDataCache, final ResourceLocation questId)
    {
        switch (expressionTree.expression)
        {
            case OR ->
            {
                if (expressionTree.childB != null)
                {
                    final List<ITriggerReturnData<?>> immReturn = evaluate(colony, triggerMap, expressionTree.childB, triggerDataCache, questId);
                    if (immReturn != null)
                    {
                        return immReturn;
                    }
                }
                if (expressionTree.childA != null)
                {
                    return evaluate(colony, triggerMap, expressionTree.childA, triggerDataCache, questId);
                }
                return null;
            }
            case AND ->
            {
                final List<ITriggerReturnData<?>> returnDataList = new ArrayList<>();
                if (expressionTree.childB != null)
                {
                    final List<ITriggerReturnData<?>> immReturn = evaluate(colony, triggerMap, expressionTree.childB, triggerDataCache, questId);
                    if (immReturn == null)
                    {
                        return null;
                    }
                    returnDataList.addAll(immReturn);
                }
                if (expressionTree.childA != null)
                {
                    final List<ITriggerReturnData<?>> immReturn = evaluate(colony, triggerMap, expressionTree.childA, triggerDataCache, questId);
                    if (immReturn == null)
                    {
                        return null;
                    }
                    returnDataList.addAll(immReturn);
                }
                return returnDataList;
            }
            default ->
            {
                final IQuestTriggerTemplate trigger = triggerMap.get(expressionTree.expression);
                final ITriggerReturnData<?> returnData = triggerDataCache.computeIfAbsent(expressionTree.expression, s -> trigger.canTriggerQuest(questId, colony));
                if (returnData.isPositive())
                {
                    return List.of(returnData);
                }
                return null;
            }
        }
    }

    public static class ExpressionNode
    {
        public String expression;
        public ExpressionNode childA;
        public ExpressionNode childB;
        public ExpressionNode parent;

        public ExpressionNode(final String expression)
        {
            this.expression = expression;
        }

        public ExpressionNode append(String expression)
        {
            if (childA == null)
            {
                childA = new ExpressionNode(expression);
                childA.parent = this;
                return childA;
            }
            else if (childB == null)
            {
                childB = new ExpressionNode(expression);
                childB.parent = this;
                return childB;
            }
            return null;
        }
    }
}