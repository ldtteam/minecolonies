package com.minecolonies.coremod.quests.rewards;

import com.google.gson.JsonObject;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.quests.IQuestInstance;
import com.minecolonies.api.quests.IQuestReward;
import com.minecolonies.coremod.colony.CitizenData;
import net.minecraft.world.entity.player.Player;

import static com.minecolonies.api.util.constant.HappinessConstants.QUEST;
import static com.minecolonies.api.quests.QuestParseConstant.*;

/**
 * Happiness inducing reward.
 */
public class HappinessReward implements IQuestReward
{
    /**
     * Happiness boost.
     */
    private final int qty;

    /**
     * Days to hold.
     */
    private final int days;

    /**
     * Target to affect.
     */
    private final int target;

    /**
     * Setup the research reward.
     */
    public HappinessReward(final int target, final int qty, final int days)
    {
        this.target = target;
        this.qty = qty;
        this.days = days;
    }

    /**
     * Create the reward.
     * @param jsonObject the json to read from.
     * @return the reward object.
     */
    public static IQuestReward createReward(final JsonObject jsonObject)
    {
        JsonObject details = jsonObject.getAsJsonObject(DETAILS_KEY);
        final int target = details.get(TARGET_KEY).getAsInt();
        final int qty = details.get(QUANTITY_KEY).getAsInt();
        final int days = details.get(DAYS_KEY).getAsInt();

        return new HappinessReward(target, qty, days);
    }

    @Override
    public void applyReward(final IColony colony, final Player player, final IQuestInstance colonyQuest)
    {
        if (this.target == 0)
        {
            ((CitizenData) colonyQuest.getQuestGiver()).getCitizenHappinessHandler().getModifier(QUEST).setModifier(qty, days);
        }
        else
        {
            ((CitizenData) colonyQuest.getParticipant(this.target)).getCitizenHappinessHandler().getModifier(QUEST).setModifier(qty, days);
        }
    }
}
