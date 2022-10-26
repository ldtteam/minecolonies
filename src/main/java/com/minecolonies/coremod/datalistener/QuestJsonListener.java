package com.minecolonies.coremod.datalistener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.quests.QuestManager;
import com.minecolonies.coremod.quests.type.IQuestType;
import com.minecolonies.coremod.quests.type.QuestType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Loader for Json based crafter specific recipes
 */
public class QuestJsonListener extends SimpleJsonResourceReloadListener
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

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

        for (final Map.Entry<ResourceLocation, JsonElement> entry : jsonElementMap.entrySet())
        {
            final ResourceLocation fileResLoc = entry.getKey();
            final JsonObject questTypeJson = entry.getValue().getAsJsonObject();

            if (QuestManager.getTypeByID(fileResLoc) != null)
            {
                final IQuestType type = QuestManager.getTypeByID(fileResLoc);
                try
                {
                    type.loadDataFromJson(questTypeJson, jsonElementMap);
                }
                catch (Exception e)
                {
                    Log.getLogger().error("Skipping quest: " + fileResLoc + " due to parsing error:", e);
                    QuestManager.removeAvailableQuestType(type);
                    continue;
                }
            }
            else
            {
                final IQuestType questType = new QuestType(fileResLoc);
                try
                {
                    questType.loadDataFromJson(questTypeJson, jsonElementMap);
                }
                catch (Exception e)
                {
                    Log.getLogger().error("Skipping quest: " + fileResLoc + " due to parsing error", e);
                    continue;
                }
                QuestManager.addAvailableQuestType(questType);
            }
        }

        Log.getLogger().info("Finished loading quests from data");
    }
}