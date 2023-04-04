package com.minecolonies.coremod.quests.triggers;

import com.minecolonies.api.quests.IQuestTrigger;

/**
 * Quest trigger interface.
 */
public interface ICitizenQuestTrigger extends IQuestTrigger
{
    int getSelectedCitizens();
}
