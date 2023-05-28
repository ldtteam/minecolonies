package com.minecolonies.coremod.quests.objectives;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.quests.IObjectiveInstance;
import com.minecolonies.api.quests.IQuestInstance;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/**
 * Specific objective for building upgrade.
 */
public interface IBuildingUpgradeObjectiveTemplate
{
    /**
     * Callback for block upgrade event
     *
     * @param progressData the objective data.
     * @param colonyQuest the quest.
     * @param level reached lvl.
     */
    void onBuildingUpgrade(IObjectiveInstance progressData, final IQuestInstance colonyQuest, final int level);
}
