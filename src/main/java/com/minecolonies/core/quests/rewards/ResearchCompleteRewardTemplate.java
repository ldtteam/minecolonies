package com.minecolonies.core.quests.rewards;

import com.google.gson.JsonObject;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.quests.IQuestInstance;
import com.minecolonies.api.quests.IQuestRewardTemplate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import static com.minecolonies.api.quests.QuestParseConstant.DETAILS_KEY;
import static com.minecolonies.api.quests.QuestParseConstant.ID_KEY;

/**
 * Research complete based reward.
 */
public class ResearchCompleteRewardTemplate implements IQuestRewardTemplate
{
    /**
     * The research to complete
     */
    private final ResourceLocation research;

    /**
     * Setup the research reward.
     * @param research the research.
     */
    public ResearchCompleteRewardTemplate(final ResourceLocation research)
    {
        this.research = research;
    }

    /**
     * Create the reward.
     * @param jsonObject the json to read from.
     * @return the reward object.
     */
    public static IQuestRewardTemplate createReward(final JsonObject jsonObject)
    {
        JsonObject details = jsonObject.getAsJsonObject(DETAILS_KEY);
        final ResourceLocation research = new ResourceLocation(details.get(ID_KEY).getAsString());
        return new ResearchCompleteRewardTemplate(research);
    }
    @Override
    public void applyReward(final IColony colony, final Player player, final IQuestInstance colonyQuest)
    {
        colony.getResearchManager().getResearchTree().finishResearch(research);
        colony.getResearchManager().markDirty();
    }
}
