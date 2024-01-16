package com.minecolonies.core.generation.defaults;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.resource.PathPackResources;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

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
    private final DataGenerator generator;

    public QuestTranslationProvider(@NotNull final DataGenerator generator)
    {
        this.generator = generator;
    }

    @NotNull
    @Override
    public String getName()
    {
        return "QuestTranslationProvider";
    }

    @Override
    public void run(@NotNull final CachedOutput cache) throws IOException
    {
        final DataGenerator.PathProvider langProvider = generator.createPathProvider(DataGenerator.Target.RESOURCE_PACK, "lang");
        final DataGenerator.PathProvider questProvider = generator.createPathProvider(DataGenerator.Target.DATA_PACK, "quests");

        final JsonObject langJson = new JsonObject();
        try (final PackResources pack = new PathPackResources(MOD_ID + ".src", Path.of("..", "src", "main", "resources")))
        {
            for (final ResourceLocation questId : pack.getResources(PackType.SERVER_DATA, MOD_ID,
                    "quests", id -> id.getPath().endsWith(".json")))
            {
                final ResourceLocation questPath = new ResourceLocation(questId.getNamespace(), questId.getPath().replace("quests/", "").replace(".json", ""));
                final String baseKey = questPath.getNamespace() + ".quests." + questPath.getPath().replace("/", ".");

                final JsonObject json;
                try (final InputStreamReader reader = new InputStreamReader(pack.getResource(PackType.SERVER_DATA, questId)))
                {
                    json = GsonHelper.parse(reader);
                }

                processQuest(langJson, baseKey, json);

                DataProvider.saveStable(cache, json, questProvider.json(questPath));
            }
        }

        DataProvider.saveStable(cache, langJson, langProvider.file(new ResourceLocation(MOD_ID, "quests"), "json"));
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
