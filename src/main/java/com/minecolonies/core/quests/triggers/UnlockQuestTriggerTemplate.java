package com.minecolonies.core.quests.triggers;

import com.google.gson.JsonObject;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.quests.IQuestTriggerTemplate;
import com.minecolonies.api.quests.ITriggerReturnData;
import net.minecraft.resources.ResourceLocation;

/**
 * Unlock quest trigger.
 */
public class UnlockQuestTriggerTemplate implements IQuestTriggerTemplate
{
    /**
     * Create a new instance of this trigger.
     */
    public UnlockQuestTriggerTemplate()
    {

    }

    /**
     * Create a new trigger directly from json.
     * @param ignoreJson the json associated to this trigger.
     */
    public static UnlockQuestTriggerTemplate createUnlockTrigger(final JsonObject ignoreJson)
    {
        return new UnlockQuestTriggerTemplate();
    }

    @Override
    public ITriggerReturnData canTriggerQuest(final IColony colony)
    {
        return new BooleanTriggerReturnData(false);
    }

    @Override
    public ITriggerReturnData canTriggerQuest(final ResourceLocation questId, final IColony colony)
    {
        return new BooleanTriggerReturnData(colony.getQuestManager().isUnlocked(questId));
    }
}
