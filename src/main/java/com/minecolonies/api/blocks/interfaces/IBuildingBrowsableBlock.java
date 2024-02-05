package com.minecolonies.api.blocks.interfaces;

import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Right-clicking this block in the air triggers the building browser window interface.
 */
public interface IBuildingBrowsableBlock
{
    /**
     * Return false if you want to prevent the building search behaviour for some reason.  Client-side only.
     */
    default boolean shouldBrowseBuildings(@NotNull final PlayerInteractEvent.RightClickItem event)
    {
        return true;
    }
}
