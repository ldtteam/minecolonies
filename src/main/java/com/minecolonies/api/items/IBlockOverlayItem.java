package com.minecolonies.api.items;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * An interface to be implemented by items that want to render overlays while the player is holding the item.
 */
public interface IBlockOverlayItem
{
    /**
     * Called client-side only.
     * @return a list of overlay boxes that should be rendered for this item.
     */
    @NotNull
    List<OverlayBox> getOverlayBoxes(@NotNull final Level world, @NotNull final Player player, @NotNull final ItemStack stack);

    /**
     * Details about the overlay box to draw.
     * @param bounds            the bounds of the box.
     * @param color             the line color.
     * @param width             the line width.
     * @param showThroughBlocks true to display through blocks.
     */
    record OverlayBox(AABB bounds, int color, float width, boolean showThroughBlocks) { }
}
