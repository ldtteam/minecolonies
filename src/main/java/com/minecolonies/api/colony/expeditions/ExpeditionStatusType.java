package com.minecolonies.api.colony.expeditions;

import net.minecraft.ChatFormatting;

/**
 * Enum describing the different status types of an expedition.
 */
public enum ExpeditionStatusType
{
    /**
     * The expedition is ongoing.
     */
    ONGOING(ChatFormatting.BLACK),
    /**
     * The expedition has been successfully completed.
     */
    SUCCESSFUL(ChatFormatting.DARK_GREEN),
    /**
     * The expedition has not been successfully completed.
     */
    UNSUCCESSFUL(ChatFormatting.DARK_RED);

    /**
     * The display color for the status type.
     */
    private final ChatFormatting displayColor;

    /**
     * Internal constructor.
     */
    ExpeditionStatusType(final ChatFormatting displayColor)
    {
        this.displayColor = displayColor;
    }

    /**
     * Get the display color for the status type.
     *
     * @return the formatting.
     */
    public ChatFormatting getDisplayColor()
    {
        return displayColor;
    }
}