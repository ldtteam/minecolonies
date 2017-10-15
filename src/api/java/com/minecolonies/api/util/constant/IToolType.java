package com.minecolonies.api.util.constant;

public interface IToolType
{
    /**
     * Returns the name of the tooltype. Also known as the ToolClass.
     *
     * @return The name of the tool type.
     */
    String getName();

    /**
     * Whether or not the tool use material.
     *
     * such as wood, gold, stone, iron or diamond
     * @return true if using material
     */
    boolean hasVariableMaterials();
}

