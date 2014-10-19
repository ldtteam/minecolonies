package com.blockout;

public enum Alignment
{
    // RelativePosition determines how the x,y coordinates of an item are relative
    // to the position of the parent.  Corner to matching corner
    // E.g, TopLeft x,y is from top left corner of parent to top left of item
    // while BottomRight is from bottom right corner of parent to bottom right of item
    // Do not use negative values; BottomRight(10,10) is 10 pixels inset left and up.
    TopLeft     ("top left"),
    TopMiddle   ("top horizontal"),
    TopRight    ("top right"),
    MiddleLeft  ("vertical left"),
    Middle      ("vertical horizontal"),
    MiddleRight ("vertical right"),
    BottomLeft  ("bottom left"),
    BottomMiddle("bottom horizontal"),
    BottomRight ("bottom right");

    Alignment(String attributes)
    {
        rightAligned = attributes.contains("right");
        bottomAligned = attributes.contains("bottom");
        horizontalCentered = attributes.contains("horizontal");
        verticalCentered = attributes.contains("vertical");
    }

    boolean rightAligned;
    boolean bottomAligned;
    boolean horizontalCentered;
    boolean verticalCentered;
}
