package com.minecolonies.coremod.quests.rewards;

import com.google.gson.JsonObject;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.quests.IQuestInstance;
import com.minecolonies.api.quests.IQuestRewardTemplate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import static com.minecolonies.api.quests.QuestParseConstant.*;

/**
 * Quest unlock reward template.
 */
public class UnlockQuestRewardTemplate implements IQuestRewardTemplate
{
    /**
     * The quest to unlock
     */
    private final ResourceLocation questId;

    /**
     * Setup the quest unlock reward.
     */
    public UnlockQuestRewardTemplate(final ResourceLocation questId)
    {
        this.questId = questId;
    }

    /**
     * Create the reward.
     * @param jsonObject the json to read from.
     * @return the reward object.
     */
    public static IQuestRewardTemplate createReward(final JsonObject jsonObject)
    {
        JsonObject details = jsonObject.getAsJsonObject(DETAILS_KEY);
        final String id = details.get(ID_KEY).getAsString();

        return new UnlockQuestRewardTemplate(new ResourceLocation(id));
    }
    @Override
    public void applyReward(final IColony colony, final Player player, final IQuestInstance colonyQuest)
    {
        colony.getQuestManager().unlockQuest(this.questId);
    }
}
