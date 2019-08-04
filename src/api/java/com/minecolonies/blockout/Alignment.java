package com.ldtteam.blockout;

import org.jetbrains.annotations.NotNull;

/**
 * Alignment enum which can be used to different levels of alignment.
 */
public enum Alignment
{
    // RelativePosition determines how the x,y coordinates of an item are relative
    // to the position of the parent.  Corner to matching corner
    // E.g, TopLeft x,y is from top left corner of parent to top left of item
    // while BOTTOM_RIGHT is from bottom right corner of parent to bottom right of item
    // Do not use negative values; BOTTOM_RIGHT(10,10) is 10 pixels inset left and up.
    TOP_LEFT("top left"),
    TOP_MIDDLE("top horizontal"),
    TOP_RIGHT("top right"),
    MIDDLE_LEFT("vertical left"),
    MIDDLE("vertical horizontal"),
    MIDDLE_RIGHT("vertical right"),
    BOTTOM_LEFT("bottom left"),
    BOTTOM_MIDDLE("bottom horizontal"),
    BOTTOM_RIGHT("bottom right");

    private final boolean rightAligned;
    private final boolean bottomAligned;
    private final boolean horizontalCentered;
    private final boolean verticalCentered;

    Alignment(@NotNull final String attributes)
    {
        rightAligned = attributes.contains("right");
        bottomAligned = attributes.contains("bottom");
        horizontalCentered = attributes.contains("horizontal");
        verticalCentered = attributes.contains("vertical");
    }

    public boolean isRightAligned()
    {
        return rightAligned;
    }

    public boolean isBottomAligned()
    {
        return bottomAligned;
    }

    public boolean isHorizontalCentered()
    {
        return horizontalCentered;
    }

    public boolean isVerticalCentered()
    {
        return verticalCentered;
    }
}
