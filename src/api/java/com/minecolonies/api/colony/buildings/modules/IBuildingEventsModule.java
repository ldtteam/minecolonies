package com.minecolonies.api.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.IBuilding;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Module interface for all building based events.
 */
public interface IBuildingEventsModule extends IBuildingModule
{
    /**
     * On destruction hook of the building, calling into the modules.
     */
    default void onDestroyed() { }

    /**
     * Upgrade complete module hook.
     * @param newLevel the new level.
     */
    default void onUpgradeComplete(int newLevel) { }

    /**
     * On building move hook for modules.
     * @param oldBuilding the building that is going to be moved.
     */
    default void onBuildingMove(IBuilding oldBuilding) { }

    /**
     * Specific wakeup hook in modules.
     */
    default void onWakeUp() { }

    /**
     * On player entering hook.
     * @param player the player that entered the building.
     */
    default void onPlayerEnterBuilding(PlayerEntity player) { }
}
