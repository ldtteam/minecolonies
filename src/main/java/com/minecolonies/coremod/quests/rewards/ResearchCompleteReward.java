package com.minecolonies.coremod.quests.rewards;

import com.google.gson.JsonObject;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.quests.IColonyQuest;
import com.minecolonies.api.quests.IQuestReward;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/**
 * Research complete based reward.
 */
public class ResearchCompleteReward implements IQuestReward
{
    /**
     * The research to complete
     */
    private final ResourceLocation research;

    /**
     * Setup the research reward.
     * @param research the research.
     */
    public ResearchCompleteReward(final ResourceLocation research)
    {
        this.research = research;
    }

    /**
     * Create the reward.
     * @param jsonObject the json to read from.
     * @return the reward object.
     */
    public static IQuestReward createReward(final JsonObject jsonObject)
    {
        JsonObject details = jsonObject.getAsJsonObject("details");
        final ResourceLocation research = new ResourceLocation(details.get("id").getAsString());
        return new ResearchCompleteReward(research);
    }
    @Override
    public void applyReward(final IColony colony, final Player player, final IColonyQuest colonyQuest)
    {
        colony.getResearchManager().getResearchTree().finishResearch(research);
    }
}
