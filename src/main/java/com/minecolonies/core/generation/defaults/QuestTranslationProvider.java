package com.minecolonies.core.generation.defaults;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.util.Log;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.resource.PathPackResources;
import org.jetbrains.annotations.NotNull;

import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.minecolonies.api.quests.QuestParseConstant.*;
import static com.minecolonies.api.quests.registries.QuestRegistries.DIALOGUE_OBJECTIVE_ID;
import static com.minecolonies.api.util.constant.Constants.MOD_ID;
import static com.minecolonies.core.quests.QuestParsingConstants.*;

/**
 * Magic translator for quests.  This parses the existing quest JSON files and moves the dialogue elements to
 * translation resources, so that translations can be provided for them.
 *
 * This requires that the 'source' quests under src/main/resources/data/minecolonies/quests only contain en-US
 * text and do not already contain translation keys.
 */
public class QuestTranslationProvider implements DataProvider
{
    private final PackOutput packOutput;

    public QuestTranslationProvider(@NotNull final PackOutput packOutput)
    {
        this.packOutput = packOutput;
    }

    @NotNull
    @Override
    public String getName()
    {
        return "QuestTranslationProvider";
    }

    @NotNull
    @Override
    public CompletableFuture<?> run(@NotNull final CachedOutput cache)
    {
        final PackOutput.PathProvider questProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "quests");
        final List<CompletableFuture<?>> quests = new ArrayList<>();

        final JsonObject langJson = new JsonObject();
        try (final PackResources pack = new PathPackResources(MOD_ID + ".src", false, Path.of("..", "src", "main", "resources")))
        {
            pack.listResources(PackType.SERVER_DATA, MOD_ID, "quests", (questId, stream) ->
            {
                if (!questId.getPath().endsWith(".json"))
                {
                    return;
                }

                final ResourceLocation questPath = new ResourceLocation(questId.getNamespace(), questId.getPath().replace("quests/", "").replace(".json", ""));
                final String baseKey = questPath.getNamespace() + ".quests." + questPath.getPath().replace("/", ".");

                quests.add(CompletableFuture.supplyAsync(() ->
                {
                    try
                    {
                        final JsonObject json;
                        try (final InputStreamReader reader = new InputStreamReader(stream.get()))
                        {
                            json = GsonHelper.parse(reader);
                        }

                        processQuest(langJson, baseKey, json);

                        return json;
                    }
                    catch (final Exception e)
                    {
                        Log.getLogger().error("Failed to process {}", questPath.toString(), e);
                        return null;
                    }
                }, Util.backgroundExecutor()).thenComposeAsync(json ->
                {
                    if (json != null)
                    {
                        return DataProvider.saveStable(cache, json, questProvider.json(questPath));
                    }
                    return CompletableFuture.completedFuture(null);
                }, Util.backgroundExecutor()));
            });
        }

        return CompletableFuture.allOf(quests.toArray(CompletableFuture[]::new))
                .thenComposeAsync(v -> saveLanguage(cache, langJson), Util.backgroundExecutor());
    }

    @NotNull
    private CompletableFuture<?> saveLanguage(@NotNull final CachedOutput cache,
                                              @NotNull final JsonObject langJson)
    {
        final PackOutput.PathProvider langProvider = packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "lang");
        final Path langFile = langProvider.file(new ResourceLocation(MOD_ID, "quests"), "json");
        return DataProvider.saveStable(cache, langJson, langFile);
    }

    private void processQuest(final JsonObject langJson, final String baseKey, final JsonObject json)
    {
        final String name = json.get(NAME).getAsString();
        langJson.addProperty(baseKey, name);
        json.addProperty(NAME, baseKey);

        int objectiveCount = 0;
        for (final JsonElement objectivesJson : json.get(QUEST_OBJECTIVES).getAsJsonArray())
        {
            final String objectiveKey = baseKey + ".obj" + objectiveCount;
            final JsonObject objective = objectivesJson.getAsJsonObject();
            processObjective(langJson, objectiveKey, objective);
            ++objectiveCount;
        }
    }

    private void processObjective(final JsonObject langJson, final String baseKey, final JsonObject json)
    {
        final ResourceLocation type = new ResourceLocation(json.get(TYPE).getAsString());
        if (type.equals(DIALOGUE_OBJECTIVE_ID))
        {
            langJson.addProperty(baseKey, json.get(TEXT_ID).getAsString());
            json.addProperty(TEXT_ID, baseKey);

            int answerCount = 0;
            for (final JsonElement answerJson : json.get(OPTIONS_ID).getAsJsonArray())
            {
                final String answerKey = baseKey + ".answer" + answerCount;
                langJson.addProperty(answerKey, answerJson.getAsJsonObject().get(ANSWER_ID).getAsString());
                answerJson.getAsJsonObject().addProperty(ANSWER_ID, answerKey);

                final JsonObject result = answerJson.getAsJsonObject().get(RESULT_ID).getAsJsonObject();
                processObjective(langJson, answerKey + ".reply", result);
                ++answerCount;
            }
        }
    }

}
