package com.minecolonies.coremod.quests.rewards;

import com.google.gson.JsonObject;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.quests.IQuestInstance;
import com.minecolonies.api.quests.IQuestRewardTemplate;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.CitizenData;
import net.minecraft.world.entity.player.Player;

import static com.minecolonies.api.quests.QuestParseConstant.*;

/**
 * Relationship adjustment quest reward.
 */
public class RelationshipRewardTemplate implements IQuestRewardTemplate
{
    /**
     * The first target citizen to apply things to.
     */
    private final int target1;

    /**
     * The second target citizen to apply things to.
     */
    private final int target2;

    /**
     * The relationship adjustment.
     */
    private final String type;

    /**
     * Setup the item reward.
     */
    public RelationshipRewardTemplate(final int target1, final int target2, final String type)
    {
        this.target1 = target1;
        this.target2 = target2;
        this.type = type;
    }

    /**
     * Create the reward.
     * @param jsonObject the json to read from.
     * @return the reward object.
     */
    public static IQuestRewardTemplate createReward(final JsonObject jsonObject)
    {
        JsonObject details = jsonObject.getAsJsonObject(DETAILS_KEY);
        final int target1 = details.get(TARGET1_KEY).getAsInt();
        final int target2 = details.get(TARGET2_KEY).getAsInt();
        final String type = jsonObject.get(TYPE_KEY).getAsString();

        return new RelationshipRewardTemplate(target1, target2, type);
    }
    @Override
    public void applyReward(final IColony colony, final Player player, final IQuestInstance colonyQuest)
    {
        try
        {
            final ICitizenData citTarget1;
            if (this.target1 == 0)
            {
                citTarget1 = ((CitizenData) colonyQuest.getQuestGiver());
            }
            else
            {
                citTarget1 = ((CitizenData) colonyQuest.getParticipant(target1));
            }

            final ICitizenData citTarget2;
            if (this.target2 == 0)
            {
                citTarget2 = ((CitizenData) colonyQuest.getQuestGiver());
            }
            else
            {
                citTarget2 = ((CitizenData) colonyQuest.getParticipant(target2));
            }

            if (citTarget1 != null && citTarget2 != null)
            {
                if (type.equals("couple"))
                {
                    if (citTarget1.getPartner() == null && citTarget2.getPartner() == null)
                    {
                        citTarget1.setPartner(citTarget2.getId());
                        citTarget2.setPartner(citTarget1.getId());
                    }
                }
                else
                {
                    if (citTarget1.getPartner() != null && citTarget2.getPartner() != null
                          && citTarget1.getPartner().getId() == citTarget2.getId()
                          && citTarget2.getPartner().getId() == citTarget1.getId())
                    {
                        citTarget1.setPartner(0);
                        citTarget2.setPartner(0);
                    }
                }
            }
        }
        catch (final Exception ex)
        {
            Log.getLogger().warn("Couldn't apply relationship quest reward. Probably one of the citizens is missing.");
        }
    }
}
