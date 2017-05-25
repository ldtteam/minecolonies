package com.minecolonies.api.util;

import com.minecolonies.api.util.IToolType;

public enum ToolType implements IToolType
{
    NONE (""),
    PICKAXE ("pickaxe"),
    SHOVEL ("shovel"),
    AXE ("axe"),
    HOE ("hoe"),
    WEAPON ("weapon"),
    BOW ("bow"),
    FISHINGROD("rod");

    private final String name;

    private ToolType(final String s)
    {
        name = s;
    }

    public String getName()
    {
        return this.name;
    }

    public static ToolType getToolType(final String tool)
    {
        for (ToolType toolType : ToolType.values())
        {
             if (toolType.getName().equals(tool))
             {
                 return toolType;
             }
        }
        return NONE;
    }
}

