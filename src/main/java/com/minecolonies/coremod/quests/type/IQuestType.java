package com.minecolonies.coremod.quests.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.coremod.quests.Quest;
import com.minecolonies.coremod.quests.type.sideeffects.IQuestSideEffect;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

public interface IQuestType
{
    ResourceLocation getID();

    List<IQuestSideEffect> createEffectsFor(final Quest quest);

    /**
     * Reads data from the json, either on load or on reload
     *
     * @param jsonObject
     * @param allQuests
     */
    void loadDataFromJson(final JsonObject jsonObject, Map<ResourceLocation, JsonElement> allQuests) throws Exception;
}
