package com.minecolonies.core.quests.rewards;

import com.google.gson.JsonObject;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.quests.IQuestInstance;
import com.minecolonies.api.quests.IQuestRewardTemplate;
import net.minecraft.world.entity.player.Player;

import static com.minecolonies.api.quests.QuestParseConstant.CHANGE_KEY;
import static com.minecolonies.api.quests.QuestParseConstant.DETAILS_KEY;

/**
 * Raid adjustment reward.
 */
public class RaidAdjustmentRewardTemplate implements IQuestRewardTemplate
{
    /**
     * The raid adjustment quantity (can be negative).
     */
    private final int qty;

    /**
     * Setup the research reward.
     * @param qty the research.
     */
    public RaidAdjustmentRewardTemplate(final int qty)
    {
        this.qty = qty;
    }

    /**
     * Create the reward.
     * @param jsonObject the json to read from.
     * @return the reward object.
     */
    public static IQuestRewardTemplate createReward(final JsonObject jsonObject)
    {
        final JsonObject details = jsonObject.getAsJsonObject(DETAILS_KEY);
        final int change = details.get(CHANGE_KEY).getAsInt();
        return new RaidAdjustmentRewardTemplate(change);
    }
    @Override
    public void applyReward(final IColony colony, final Player player, final IQuestInstance colonyQuest)
    {
        colony.getRaiderManager().setNightsSinceLastRaid(colony.getRaiderManager().getNightsSinceLastRaid() + qty);
    }
}
