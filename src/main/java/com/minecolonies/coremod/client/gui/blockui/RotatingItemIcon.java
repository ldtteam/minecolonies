package com.minecolonies.coremod.client.gui.blockui;

import com.ldtteam.blockui.controls.ItemIcon;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Variation of the ItemIcon which accepts multiple items and slowly rotates between them.
 * TODO: This needs to be ported to BlockUI at a later stage, with a proper animation handler,
 *  instead of being backed by a timer, increased through the draw methods.
 *  Conferred with Nightenom that we keep this in Minecolonies for the time being.
 */
public class RotatingItemIcon extends ItemIcon
{
    /**
     * The full list of items.
     */
    private List<ItemStack> items = new ArrayList<>();

    /**
     * The duration to switch between items, in milliseconds.
     */
    private int duration = 2000;

    /**
     * The index of the current item which is being displayed.
     */
    private int itemIndex = 0;

    /**
     * The millis since last change.
     */
    private long lastUpdateMillis = System.currentTimeMillis();

    /**
     * Default constructor.
     */
    public RotatingItemIcon()
    {
    }

    /**
     * Set all possible items.
     *
     * @param items the full list of items.
     */
    public void setItems(@NotNull final List<ItemStack> items)
    {
        if (items.isEmpty())
        {
            throw new IllegalArgumentException("Items list must contain at least one item.");
        }

        this.items = items;
        resetState();
    }

    /**
     * The duration to switch between items, in ticks.
     * Defaults to 40 (2 seconds), minimum of 1 tick.
     *
     * @param duration the new duration.
     */
    public void setDuration(final int duration)
    {
        if (duration < 1)
        {
            throw new IllegalArgumentException("Duration cannot be zero or negative.");
        }

        this.duration = duration;
        resetState();
    }

    @Override
    public void drawSelf(final PoseStack ms, final double mx, final double my)
    {
        long currentMillis = System.currentTimeMillis();
        if ((currentMillis - this.lastUpdateMillis) >= this.duration)
        {
            this.lastUpdateMillis = currentMillis;
            this.itemIndex++;
            this.itemIndex %= this.items.size();

            updateItem();
        }

        super.drawSelf(ms, mx, my);
    }

    /**
     * Reset the component state back to a 0-index start.
     */
    private void resetState()
    {
        this.itemIndex = 0;
        this.lastUpdateMillis = System.currentTimeMillis();
        updateItem();
    }

    /**
     * Handle item updates.
     */
    private void updateItem()
    {
        final ItemStack newItem = this.items.get(this.itemIndex);
        this.setItem(newItem);
    }
}
