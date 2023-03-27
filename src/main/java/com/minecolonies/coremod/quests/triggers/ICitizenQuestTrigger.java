package com.minecolonies.coremod.quests.triggers;

import com.minecolonies.coremod.colony.Colony;

/**
 * Quest trigger interface.
 */
public interface ICitizenQuestTrigger extends IQuestTrigger
{
    int getSelectedCitizens();
}
