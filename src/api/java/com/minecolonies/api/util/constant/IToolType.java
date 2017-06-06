package com.minecolonies.api.util.constant;

public interface IToolType
{
    public String getName();
    /**
     * Whether or not the tool use material.
     *
     * such as wood, gold, stone, iron or diamond
     * @return true if using material
     */
    public boolean hasVariableMaterials();
}

