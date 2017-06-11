package com.minecolonies.api.util.constants;

public interface IToolType
{
    String getName();
    /**
     * Whether or not the tool use material.
     *
     * such as wood, gold, stone, iron or diamond
     * @return true if using material
     */
    boolean hasVariableMaterials();
}

