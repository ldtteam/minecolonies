package com.minecolonies.coremod.quests.rewards;

import com.google.gson.JsonObject;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.quests.IQuestInstance;
import com.minecolonies.api.quests.IQuestRewardTemplate;
import com.minecolonies.coremod.colony.CitizenData;
import net.minecraft.world.entity.player.Player;

import static com.minecolonies.api.quests.QuestParseConstant.*;

/**
 * Skill addition quest reward.
 */
public class SkillRewardTemplate implements IQuestRewardTemplate
{
    /**
     * The skill to assign to a target citizen.
     */
    private final Skill skill;

    /**
     * The target citizen to apply things to.
     */
    private final int target;

    /**
     * The number of levels to apply.
     */
    private final int qty;

    /**
     * Setup the item reward.
     */
    public SkillRewardTemplate(final Skill skill, final int target, final int qty)
    {
        this.skill = skill;
        this.target = target;
        this.qty = qty;
    }

    /**
     * Create the reward.
     * @param jsonObject the json to read from.
     * @return the reward object.
     */
    public static IQuestRewardTemplate createReward(final JsonObject jsonObject)
    {
        JsonObject details = jsonObject.getAsJsonObject(DETAILS_KEY);
        final int target = details.get(TARGET_KEY).getAsInt();
        final int qty = details.get(QUANTITY_KEY).getAsInt();
        final Skill skill = Skill.valueOf(details.get(SKILL_KEY).getAsString());

        return new SkillRewardTemplate(skill, target, qty);
    }
    @Override
    public void applyReward(final IColony colony, final Player player, final IQuestInstance colonyQuest)
    {
        if (target == 0)
        {
            ((CitizenData) colonyQuest.getQuestGiver()).getCitizenSkillHandler().incrementLevel(skill, qty);
        }
        else
        {
            ((CitizenData) colonyQuest.getParticipant(target)).getCitizenSkillHandler().incrementLevel(skill, qty);
        }
    }
}
