package com.minecolonies.coremod.datalistener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.quests.QuestManager;
import com.minecolonies.coremod.quests.type.IQuestType;
import com.minecolonies.coremod.quests.type.QuestType;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

/**
 * Loader for Json based crafter specific recipes
 */
public class QuestJsonListener extends JsonReloadListener
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
    protected void apply(Map<ResourceLocation, JsonObject> jsonData, IResourceManager resourceManager, IProfiler profiler)
    {
        Log.getLogger().info("Loading quests from data");

        for (Map.Entry<ResourceLocation, JsonObject> entry : jsonData.entrySet())
        {
            final ResourceLocation fileResLoc = entry.getKey();
            final JsonObject questTypeJson = entry.getValue();

            if (QuestManager.getTypeByID(fileResLoc) != null)
            {
                final IQuestType type = QuestManager.getTypeByID(fileResLoc);
                try
                {
                    type.loadDataFromJson(questTypeJson, jsonData);
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
                    questType.loadDataFromJson(questTypeJson, jsonData);
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