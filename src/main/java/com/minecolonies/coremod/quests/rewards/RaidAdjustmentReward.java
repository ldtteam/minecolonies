package com.minecolonies.coremod.quests.rewards;

import com.google.gson.JsonObject;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.quests.IColonyQuest;
import com.minecolonies.api.quests.IQuestReward;
import net.minecraft.world.entity.player.Player;

import static com.minecolonies.api.util.constant.QuestParseConstant.CHANGE_KEY;
import static com.minecolonies.api.util.constant.QuestParseConstant.DETAILS_KEY;

/**
 * Raid adjustment reward.
 */
public class RaidAdjustmentReward implements IQuestReward
{
    /**
     * The raid adjustment quantity (can be negative).
     */
    private final int qty;

    /**
     * Setup the research reward.
     * @param qty the research.
     */
    public RaidAdjustmentReward(final int qty)
    {
        this.qty = qty;
    }

    /**
     * Create the reward.
     * @param jsonObject the json to read from.
     * @return the reward object.
     */
    public static IQuestReward createReward(final JsonObject jsonObject)
    {
        final JsonObject details = jsonObject.getAsJsonObject(DETAILS_KEY);
        final int change = details.get(CHANGE_KEY).getAsInt();
        return new RaidAdjustmentReward(change);
    }
    @Override
    public void applyReward(final IColony colony, final Player player, final IColonyQuest colonyQuest)
    {
        colony.getRaiderManager().setNightsSinceLastRaid(colony.getRaiderManager().getNightsSinceLastRaid() + qty);
    }
}
